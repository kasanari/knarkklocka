package se.jakob.knarkklocka.utils

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import se.jakob.knarkklocka.*
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState
import java.text.DateFormat
import java.util.*

/**
 * Created by Jakob Nyberg on 2018-03-26.
 * Various utility functions for managing alarms.
 */

object TimerUtils {


    const val EXTRA_ALARM_ID = "alarm-id"

    private const val TAG = "TimerUtils"
    const val ALARM_INTENT_ID = 76          //Arbitrary unique ID for the alarm intent
    private const val TIMER_ACTIVITY_INTENT_ID = 34 //Arbitrary unique ID for the TimerActivity intent

    /**
     * Returns whatever pending intent i am using at the moment
     */
    private fun getPI(context: Context, id: Long): PendingIntent {
        return getAlarmServicePendingIntent(context, id, ACTION_ACTIVATE)
    }

    fun getAlarmActionIntent(context : Context, action_id: String, alarm_id: Long): Intent {
        return Intent(context, AlarmIntentService::class.java).apply {
            action = action_id
            putExtra(EXTRA_ALARM_ID, alarm_id)
        }
    }

    fun doBackgroundWork(context: Context, action_id: String, alarm_id: Long) {
        val intent = getAlarmActionIntent(context, action_id, alarm_id)
        context.startService(intent)
    }

    /**
     * Checks if the AlarmManager has a alarm registered for the
     * supplied id.
     * @param id the id of the alarm to be checked.
     */
    fun alarmIsSet(context: Context, id: Long): Boolean {
        val alarmIntent = getAlarmServiceIntent(context, id, ACTION_ACTIVATE)
        val service = PendingIntent.getForegroundService(
                context,
                ALARM_INTENT_ID,
                alarmIntent,
                PendingIntent.FLAG_NO_CREATE)
        return service != null
    }

    fun getAlarmServiceIntent(context: Context, id: Long, action_id: String): Intent {
        val alarmIntent = Intent(context, AlarmService::class.java)
        alarmIntent.putExtra(EXTRA_ALARM_ID, id)
        alarmIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        alarmIntent.action = action_id
        return alarmIntent
    }

    /**
     * Returns the pending intent that starts [AlarmService]
     */
    private fun getAlarmServicePendingIntent(context: Context, id: Long, action_id: String): PendingIntent {
        val alarmIntent = getAlarmServiceIntent(context, id, action_id)
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
     * Creates a new alarm with the system [AlarmManager]
     */
    fun setNewAlarm(context: Context, id: Long, endTime: Date) {
        AlarmBroadcasts.broadcastStopAlarm(context) /* Stop any vibration */

        val pendingAlarmIntent = getPI(context, id) /* Set AlarmService as the intent to start when alarm goes off */

        context.getSystemService(AlarmManager::class.java).run {
            setExactAndAllowWhileIdle(RTC_WAKEUP, endTime.time, pendingAlarmIntent)
        }

        if (BuildConfig.DEBUG) {
            val df = DateFormat.getTimeInstance(DateFormat.SHORT)
            val debugString = String.format(Locale.getDefault(), "Set alarm with id %d due %s.", id, df.format(endTime))
            Log.d(TAG, debugString)
        }
    }

    /**
     * Cancel the AlarmManager alarm with the supplied id
     * @param id The id of the alarm to be cancelled
     */
    fun cancelAlarm(context: Context, id: Long) {
        if (alarmIsSet(context, id)) {
            AlarmNotificationsUtils.clearAllNotifications(context) /* Remove any currently visible notifications */
            AlarmBroadcasts.broadcastStopAlarm(context) /* Stop all vibrations */
            val pendingAlarmIntent = getPI(context, id) /*Create the same intent as the registered alarm in order to cancel it*/
            context.getSystemService(AlarmManager::class.java).run {
                cancel(pendingAlarmIntent)
            }
            pendingAlarmIntent.cancel()
            if (BuildConfig.DEBUG) {
                val debugString = String.format(Locale.getDefault(), "Cancelled alarm with id %d.", id)
                Log.d(TAG, debugString)
            }
        }
    }

    /**
     * Start an instance of the main timer, this will set an alarm with AlarmManager
     * with the length saved in SharedPreferences. A new alarm will also be created and saved to the DB.
     * A notification will also be shown.
     * @param context
     * @return The expiration time of the started alarm
     */
    suspend fun startMainTimer(context: Context): Date {
        val timerDuration: Long = if (PreferenceUtils.getCustomTimerEnabled(context)) {
            PreferenceUtils.getCustomTimerLength(context)
        } else {
            PreferenceUtils.getMainTimerLength(context)
        }
        PreferenceUtils.setCustomTimerEnabled(context, false)
        val startTime = Calendar.getInstance().time
        val endTime = Calendar.getInstance().apply { add(Calendar.MILLISECOND, timerDuration.toInt()) }.time
        val alarm = Alarm(AlarmState.STATE_WAITING, startTime, endTime)
        InjectorUtils.getAlarmRepository(context).run {
            safeInsert(alarm)?.let { id ->
                alarm.id = id
                setNewAlarm(context, id, alarm.endTime)
                AlarmNotificationsUtils.showWaitingAlarmNotification(context, alarm)
            }
        }

        return endTime
    }

    /**
     *  Start an instance of the snooze timer, this will set an alarm with AlarmManager
     *  with the length saved in SharedPreferences. The specified alarm will have its due time
     *  updated and a notification is shown.
     *  @param context
     *  @param alarm The alarm that is to be snoozed.
     *  @return The new due time of the snoozed alarm.
     */
    suspend fun startSnoozeTimer(context: Context, alarm: Alarm): Date {
        val snoozeDuration = PreferenceUtils.getSnoozeTimerLength(context)
        val newEndTime = Calendar.getInstance().apply { add(Calendar.MILLISECOND, snoozeDuration.toInt()) }.time
        setNewAlarm(context, alarm.id, newEndTime)
        InjectorUtils.getAlarmRepository(context).let { repository ->
            AlarmStateChanger.snooze(alarm, newEndTime, repository)
            AlarmNotificationsUtils.showSnoozingAlarmNotification(context, alarm)
        }
        return newEndTime
    }
}
