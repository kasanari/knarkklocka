package se.jakob.knarkklocka

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository
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


    private val actionsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                ACTION_STOP_ALARM -> stopAlarm()
            }
                ACTION_ALARM_HANDLED -> {
                    alarmIsHandled = true
        }
    }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service got created")
        // Register the broadcast receiver
        val filter = IntentFilter(ACTION_STOP_ALARM)
        filter.addAction(ACTION_ALARM_HANDLED)
        repository = InjectorUtils.getAlarmRepository(this)
        //filter.addAction(ACTION_SNOOZE_ALARM);
        registerReceiver(actionsReceiver, filter)
        isRegistered = true
    }

    private fun listenToAlarm(id : Long) {
        repository.getLiveAlarmByID(id).observe(this, androidx.lifecycle.Observer { alarm ->
            alarm?.let {
                AlarmNotificationsUtils.clearAllNotifications(this)
                when(alarm.state) {
                    AlarmState.STATE_WAITING, AlarmState.STATE_DEAD, AlarmState.STATE_SNOOZING -> null
                    AlarmState.STATE_ACTIVE -> AlarmNotificationsUtils.showActiveAlarmNotification(this, alarm)
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
            handleIntent(action, id)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun activateAlarm(alarm: Alarm) {
        if (BuildConfig.DEBUG) {
            val df = DateFormat.getTimeInstance(DateFormat.SHORT)
            val debugString = String.format(
                    Locale.getDefault(),
                    "Activating alarm with id %d due %s",
                    alarm.id,
                    df.format(alarm.endTime))
            Log.d(TAG, debugString)
        }
        if (alarm.snoozes < 10) { //Check if alarm has already been snoozed a bunch of times
            if (alarm.active) {
                Log.e(TAG, "Service attempted to activate an already activated alarm!")
            } else {
                alarm.activate()
                repository.safeUpdate(alarm)
                startAlarm(alarm)
            }
        } else { // if it has, then kill it
            alarm.kill()
            repository.safeUpdate(alarm)
            stopAlarm()
            stopSelf()
        }
    }

    /*Intent handler meant to be run on separate thread*/
    private fun handleIntent(action: String, id: Long) = GlobalScope.launch {
        repository.getAlarmByID(id)?.let { alarm ->
            when (action) {
                ACTION_ACTIVATE_ALARM -> {
                   activateAlarm(alarm)
                }
                ACTION_STOP_ALARM -> {
                    stopAlarm()
                    stopSelf()
                }
            }
        }
    }

    private fun startAlarm(alarm: Alarm) {
        AlarmNotificationsUtils.clearAllNotifications(this)
        Klaxon.vibrateAlarm(this)
        AlarmNotificationsUtils.showActiveAlarmNotification(this, alarm)
    }

    private fun stopAlarm() {
        if (alarmIsHandled) {
        Klaxon.stopVibrate(this)
        stopForeground(true)
            stopSelf()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "AlarmService.onDestroy() called")
        super.onDestroy()
        if (isRegistered) {
            unregisterReceiver(actionsReceiver)
            isRegistered = false
        }
        stopAlarm()
        WakeLocker.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        isBound = true
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBound = false
        return super.onUnbind(intent)
    }

    companion object {
        private const val TAG = "AlarmService"
    }
}
