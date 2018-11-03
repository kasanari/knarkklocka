package se.jakob.knarkklocka

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleService
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository
import se.jakob.knarkklocka.utils.*
import se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID
import java.text.DateFormat
import java.util.*



class AlarmService : LifecycleService() {
    private val tag = "AlarmService"
    private val mRepository: AlarmRepository =
            InjectorUtils.getAlarmRepository(this)
    private var mIsRegistered: Boolean = false
    private val binder: IBinder = Binder()
    private var isBound: Boolean = false


    private val mActionsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            when (action) {
                ACTION_STOP_ALARM -> stopAlarm()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "Service got created")
        // Register the broadcast receiver
        val filter = IntentFilter(ACTION_STOP_ALARM)
        //filter.addAction(ACTION_SNOOZE_ALARM);
        registerReceiver(mActionsReceiver, filter)
        mIsRegistered = true
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        WakeLocker.acquire(this)
        val id = intent.getLongExtra(EXTRA_ALARM_ID, -1)
        val action = intent.action
        if (action != null) {
            runOnIoThread { handleIntent(action, id) }
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
        Klaxon.stopVibrate(this)
        stopForeground(true)
    }

    override fun onDestroy() {
        Log.d(tag, "AlarmService.onDestroy() called")
        super.onDestroy()
        if (mIsRegistered) {
            unregisterReceiver(mActionsReceiver)
            mIsRegistered = false
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
}
