package se.jakob.knarkklocka

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.util.Log
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository
import se.jakob.knarkklocka.data.AlarmState
import se.jakob.knarkklocka.utils.*
import se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID
import java.text.DateFormat
import java.util.*


class AlarmService : LifecycleService() {
    private lateinit var repository: AlarmRepository
    private var isRegistered: Boolean = false
    private val binder: IBinder = Binder()
    private var isBound: Boolean = false
    private var alarmIsHandled = false
    private lateinit var currentAlarm: Alarm
    
    private var serviceJob: Job = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val timeoutLength = 5 * MINUTE_IN_MILLIS
    private var timeoutRunning = false

    private val alarmCallback = AlarmManager.OnAlarmListener {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Timeout reached. Snoozing...")
        }
        if (!alarmIsHandled) {
            TimerUtils.startSnoozeTimer(this, currentAlarm)
            alarmIsHandled = true
            stopAlarm()
        }
    }

    private val actionsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
             intent.action?.let { a ->
                 currentAlarm?.let { alarm ->
                     handleBroadcastAction(a, alarm)
                }
                }
            }
        }

    override fun onCreate() {
        super.onCreate()
        // Register the broadcast receiver
        val filter = IntentFilter().apply {
            addAction(ACTION_STOP_ALARM)
            addAction(ACTION_ALARM_HANDLED)
            addAction(ACTION_DISMISS_ALARM)
            addAction(ACTION_SNOOZE_ALARM)
        }
        repository = InjectorUtils.getAlarmRepository(this)
        registerReceiver(actionsReceiver, filter)
        isRegistered = true
        Log.d(TAG, "AlarmService was created.")
    }

    private fun listenToAlarm(id: Long) {
        repository.getLiveAlarmByID(id).observe(this, androidx.lifecycle.Observer { alarm ->
            alarm?.let {
                currentAlarm = alarm
                when (alarm.state) {
                    AlarmState.STATE_WAITING, AlarmState.STATE_DEAD, AlarmState.STATE_SNOOZING -> null
                    AlarmState.STATE_ACTIVE -> {
                        AlarmNotificationsUtils.showActiveAlarmNotification(this, alarm)
                    }
                    AlarmState.STATE_MISSED -> AlarmNotificationsUtils.showMissedAlarmNotification(this, alarm)
                }
            }
        })
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        WakeLocker.acquire(this)
        val id = intent.getLongExtra(EXTRA_ALARM_ID, -1)
        intent.action?.let { action ->
            listenToAlarm(id)
            serviceScope.launch {
                handleIntent(action, id)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private suspend fun activateAlarm(alarm: Alarm) {
        if (BuildConfig.DEBUG) {
            val df = DateFormat.getTimeInstance(DateFormat.SHORT)
            val debugString = String.format(
                    Locale.getDefault(),
                    "Activating alarm with id %d due %s",
                    alarm.id,
                    df.format(alarm.endTime))
            Log.d(TAG, debugString)
        }
        if (alarm.snoozes < 5) { //Check if alarm has already been snoozed a bunch of times
            if (alarm.active) {
                Log.e(TAG, "Service attempted to activate an already activated alarm!")
            } else {
                AlarmStateChanger.activate(alarm, repository)
                startAlarm()
            }
        } else { // if it has, then set it as missed
            AlarmStateChanger.miss(alarm, repository)
            stopSelf()
        }
    }

    private fun handleBroadcastAction(action: String, alarm: Alarm) {
        when (action) {
            ACTION_STOP_ALARM -> {
                stopAlarm()
            }
            ACTION_ALARM_HANDLED -> {
                alarmIsHandled = true
            }
            ACTION_SNOOZE_ALARM -> {
                alarmIsHandled = true
                TimerUtils.startSnoozeTimer(applicationContext, alarm)
                Log.d(TAG, "Service received action to snooze alarm.")
                stopAlarm()
            }
            ACTION_DISMISS_ALARM -> {
                alarmIsHandled = true
                serviceScope.launch {
                    TimerUtils.cancelAlarm(applicationContext, alarm.id)
                    AlarmStateChanger.sleep(alarm, repository)
                    TimerUtils.startMainTimer(applicationContext)
                    Log.d(TAG, "Service received action to dismiss alarm.")
                    stopAlarm()
                }
            }
        }
    }

    /*Intent handler meant to be run on separate thread*/
    private suspend fun handleIntent(action: String, id: Long) {
        repository.getAlarmByID(id)?.let { alarm ->
            when (action) {
                ACTION_ACTIVATE_ALARM -> {
                    activateAlarm(alarm)
                }
                ACTION_STOP_ALARM -> {
                    stopAlarm()
                }
                ACTION_SLEEP -> {
                    if (!alarmIsHandled) {
                        alarmIsHandled = true
                        TimerUtils.cancelAlarm(applicationContext, alarm.id)
                    AlarmStateChanger.sleep(alarm, repository)
                    Log.d(TAG, "Service received action to cancel alarm.")
                }
                    stopAlarm()
                }
                ACTION_RESTART_ALARM -> {
                    if (!alarmIsHandled) {
                        alarmIsHandled = true
                        TimerUtils.startMainTimer(applicationContext)
                        Log.d(TAG, "Service received action to restart alarm.")
                }
                    stopAlarm()
                }
                else -> {
                    stopSelf()
                }
            }
        }
    }

    private fun startAlarm() {
        Klaxon.vibrateAlarm(applicationContext)
    }

    private fun stopAlarm() {
        if (alarmIsHandled) {
            Klaxon.stopVibrate(applicationContext)
            stopForeground(true)
            stopSelf()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "AlarmService.onDestroy() called.")
        super.onDestroy()
        if (isRegistered) {
            unregisterReceiver(actionsReceiver)
            isRegistered = false
        }
        serviceJob.cancel()
        stopTimeout()
        WakeLocker.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        isBound = true
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBound = false
        if (!alarmIsHandled) {
            Log.d(TAG, "AlarmActivity stopped but alarm was not handled!")
            serviceScope.launch {
                AlarmStateChanger.miss(currentAlarm, repository)
            }
            if (BuildConfig.DEBUG) {
                startTimeout(10000)
            } else {
                startTimeout(timeoutLength)
            }
        }
        return super.onUnbind(intent)
    }

    private fun startTimeout(length: Long) {
        if (!timeoutRunning) {
        getSystemService(AlarmManager::class.java).run {
            setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + length, "tag", alarmCallback, null)
                timeoutRunning = true
            }
        }
    }

    private fun stopTimeout() {
        if (timeoutRunning) {
        getSystemService(AlarmManager::class.java).run {
            cancel(alarmCallback)
                timeoutRunning = false
            }
        }
    }

    companion object {
        private const val TAG = "AlarmService"
    }
}
