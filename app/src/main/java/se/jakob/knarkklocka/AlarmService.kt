package se.jakob.knarkklocka

import android.arch.lifecycle.LifecycleService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.support.annotation.MainThread
import android.util.Log
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository
import se.jakob.knarkklocka.data.AlarmState
import se.jakob.knarkklocka.utils.ACTION_ACTIVATE_ALARM
import se.jakob.knarkklocka.utils.ACTION_STOP_ALARM
import se.jakob.knarkklocka.utils.InjectorUtils
import se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID
import se.jakob.knarkklocka.utils.runOnIoThread
import java.text.DateFormat
import java.util.*



class AlarmService : LifecycleService() {
    private val tag = "AlarmService"
    private val mRepository: AlarmRepository =
            InjectorUtils.getAlarmRepository(this)
    private var mIsRegistered: Boolean = false


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
        val id = intent.getLongExtra(EXTRA_ALARM_ID, -1)
        val action = intent.action
        if (action != null) {
            runOnIoThread { handleIntent(action, id) }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /*Intent handler meant to be run on separate thread*/
    @MainThread
    private fun handleIntent(action: String, id: Long) {
        val alarm = mRepository.getAlarmByID(id)
        when (action) {
            ACTION_ACTIVATE_ALARM -> {
                if (BuildConfig.DEBUG) {
                    val df = DateFormat.getTimeInstance(DateFormat.SHORT)
                    val debugString = String.format(Locale.getDefault(), "Activating alarm with id %d due %s", id, df.format(alarm.endTime))
                    Log.d(tag, debugString)
                }

                if (alarm.snoozes < 10) {
                    mRepository.changeAlarmState(id, AlarmState.STATE_ACTIVE)
                    startAlarm(alarm)
                } else {
                    alarm.state = AlarmState.STATE_DEAD
                    mRepository.update(alarm)
                    stopAlarm()
                    stopSelf()
                }
            }

            ACTION_STOP_ALARM -> {
                stopAlarm()
                stopSelf()
            }
        }
    }

    private fun startAlarm(alarm: Alarm) {
        AlarmNotificationsUtils.clearAllNotifications(this)
        WakeLocker.acquire(this)
        Klaxon.vibrateAlarm(this)
        AlarmNotificationsUtils.showActiveAlarmNotification(this, alarm)
    }

    private fun stopAlarm() {
        Klaxon.stopVibrate(this)
        stopForeground(true)
        WakeLocker.release()
    }

    override fun onDestroy() {
        Log.d(tag, "AlarmService.onDestroy() called")
        super.onDestroy()
        if (mIsRegistered) {
            unregisterReceiver(mActionsReceiver)
            mIsRegistered = false
        }
        stopAlarm()
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return null
    }
}
