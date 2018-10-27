package se.jakob.knarkklocka.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import se.jakob.knarkklocka.AlarmBroadcasts
import se.jakob.knarkklocka.AlarmService
import se.jakob.knarkklocka.BuildConfig
import se.jakob.knarkklocka.TimerActivity
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState
import java.text.DateFormat
import java.util.*

/**
 * Created by Jakob Nyberg on 2018-03-26.
 */

object TimerUtils {


    const val EXTRA_ALARM_ID = "alarm-id"

    private const val TAG = "TimerUtils"
    private const val ALARM_INTENT_ID = 76          //Arbitrary unique ID for the alarm intent
    private const val TIMER_ACTIVITY_INTENT_ID = 34 //Arbitrary unique ID for the TimerActivity intent

    /**
     * Returns whatever pending intent i am using at the moment
     */
    private fun getPI(context: Context, id: Long): PendingIntent {
        return getAlarmServicePendingIntent(context, id)
    }

    fun isAlarmSet(context: Context, id: Long) : Boolean {
        val alarmIntent = getAlarmServiceIntent(context, id)
        val service = PendingIntent.getForegroundService(
                context,
                ALARM_INTENT_ID,
                alarmIntent,
                PendingIntent.FLAG_NO_CREATE)
        return service != null
    }

     fun getAlarmServiceIntent(context: Context, id: Long) : Intent {
        val alarmIntent = Intent(context, AlarmService::class.java)
        alarmIntent.putExtra(EXTRA_ALARM_ID, id)
        alarmIntent.action = ACTION_ACTIVATE_ALARM
        return alarmIntent
    }

    /**
     * Returns the pending intent that starts [AlarmService]
     */
    private fun getAlarmServicePendingIntent(context: Context, id: Long): PendingIntent {
        val alarmIntent = getAlarmServiceIntent(context, id)
        return PendingIntent.getForegroundService(
                context,
                ALARM_INTENT_ID,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Returns the pending intent that starts [TimerActivity]
     */
    fun getTimerActivityIntent(context: Context): PendingIntent {
        val timerIntent = Intent(context, TimerActivity::class.java)
        return PendingIntent.getActivity(context, TIMER_ACTIVITY_INTENT_ID, timerIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Creates a new AlarmClock with the system [AlarmManager]
     */
    private fun setNewAlarmClock(context: Context, id: Long, endTime: Date) {
        cancelAlarm(context, -1) /* Remove existing alarm */
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        val pendingAlarmIntent = getPI(context, id) /* Set AlarmService as the intent to start when alarm goes off */

        val showAlarmPI = getTimerActivityIntent(context)
        val alarmClockInfo = AlarmManager.AlarmClockInfo(endTime.time, showAlarmPI) /* Setup alarm clock info */

        alarmManager.setAlarmClock(alarmClockInfo, pendingAlarmIntent) /* Register the alarm with the AlarmManager */

        if (BuildConfig.DEBUG) {
            val df = DateFormat.getTimeInstance(DateFormat.SHORT)
            val debugString = String.format(Locale.getDefault(), "Set alarm with id %d due %s", id, df.format(endTime))
            Log.d(TAG, debugString)
        }
    }

    fun cancelAlarm(context: Context, id: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        AlarmNotificationsUtils.clearAllNotifications(context)
        AlarmBroadcasts.broadcastStopAlarm(context) /* Stop any vibration */
        val pendingAlarmIntent = getPI(context, id) /*Create the same intent as the registered alarm in order to cancel it*/
        alarmManager.cancel(pendingAlarmIntent)
        pendingAlarmIntent.cancel()
        if (BuildConfig.DEBUG) {
            val debugString = String.format(Locale.getDefault(), "Cancelled alarm with id %d", id)
            Log.d(TAG, debugString)
        }
    }

    fun startMainTimer(context: Context) {
        val timerDuration = PreferenceUtils.getMainTimerLength(context)
        val startTime = Calendar.getInstance().time
        val endTime = Calendar.getInstance().apply { add(Calendar.MILLISECOND, timerDuration.toInt()) }.time
        val alarm = Alarm(AlarmState.STATE_WAITING, startTime, endTime)
        val repository = InjectorUtils.getAlarmRepository(context)
        runOnIoThread {
            val id = repository.insert(alarm)
            setNewAlarmClock(context, id, alarm.endTime)
        }
        AlarmNotificationsUtils.showWaitingAlarmNotification(context, alarm)
    }

    fun startSnoozeTimer(context: Context, alarm: Alarm) : Date {
        val snoozeDuration = PreferenceUtils.getSnoozeTimerLength(context)
        val newEndTime = Calendar.getInstance().apply { add(Calendar.MILLISECOND, snoozeDuration.toInt()) }.time
        setNewAlarmClock(context, alarm.id, newEndTime)
        alarm.snooze(newEndTime)
        AlarmNotificationsUtils.showSnoozingAlarmNotification(context, alarm)
        val repository = InjectorUtils.getAlarmRepository(context)
        repository.update(alarm)
        return newEndTime
    }
}
