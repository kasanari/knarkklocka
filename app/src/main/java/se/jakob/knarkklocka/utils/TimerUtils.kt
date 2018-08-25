package se.jakob.knarkklocka.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log

import java.text.DateFormat
import java.util.Date
import java.util.Locale

import se.jakob.knarkklocka.AlarmActivity
import se.jakob.knarkklocka.AlarmBroadcastReceiver
import se.jakob.knarkklocka.AlarmNotificationsBuilder
import se.jakob.knarkklocka.AlarmService
import se.jakob.knarkklocka.BuildConfig
import se.jakob.knarkklocka.PreferenceUtils
import se.jakob.knarkklocka.TimerActivity
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.viewmodels.AlarmViewModel

/**
 * Created by Jakob Nyberg on 2018-03-26.
 */

object TimerUtils {

    const val ACTION_ACTIVATE_ALARM = "activate-alarm"
    const val ACTION_STOP_ALARM = "stop-alarm"
    const val ACTION_WAITING_ALARM = "wating-alarm"

    const val EXTRA_ALARM_ID = "alarm-id"

    private const val TAG = "TimerUtils"
    private const val ALARM_INTENT_ID = 76          //Arbitrary unique ID for the alarm intent
    private const val TIMER_ACTIVITY_INTENT_ID = 34 //Arbitrary unique ID for the TimerActivity intent


    /**
     * Returns whatever pending intent i am using at the moment
     */
    private fun getPI(context: Context, id: Long): PendingIntent {
        //return getAlarmReceiverIntent(context, id);
        return getAlarmServiceIntent(context, id)
    }

    /**
     * Returns the pending intent that starts the Alarm Service
     */
    private fun getAlarmServiceIntent(context: Context, id: Long): PendingIntent {
        val alarmIntent = Intent(context, AlarmService::class.java)
        alarmIntent.putExtra(EXTRA_ALARM_ID, id)
        alarmIntent.action = ACTION_ACTIVATE_ALARM
        return PendingIntent.getForegroundService(context, ALARM_INTENT_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Returns the pending intent that starts the Alarm Activity
     */
    fun getAlarmActivityIntent(context: Context, id: Long): PendingIntent {
        val alarmIntent = Intent(context, AlarmActivity::class.java)
        alarmIntent.putExtra(EXTRA_ALARM_ID, id)
        return PendingIntent.getActivity(context, ALARM_INTENT_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Returns the pending intent that starts the Alarm Activity
     */
    private fun getAlarmReceiverIntent(context: Context, id: Long): PendingIntent {
        val alarmIntent = Intent(context, AlarmBroadcastReceiver::class.java)
        alarmIntent.putExtra(EXTRA_ALARM_ID, id)
        return PendingIntent.getBroadcast(context, ALARM_INTENT_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Returns the pending intent that starts the main timer activity
     */
    fun getTimerActivityIntent(context: Context, id: Long): PendingIntent {
        val timerIntent = Intent(context, TimerActivity::class.java)
        timerIntent.putExtra(EXTRA_ALARM_ID, id)
        return PendingIntent.getActivity(context, TIMER_ACTIVITY_INTENT_ID, timerIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun setNewAlarmClock(context: Context, id: Long, endTime: Date) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        /*Set AlarmService to start when alarm goes off*/
        val pendingAlarmIntent = getPI(context, id)

        /*Setup alarm clock info*/
        //long wakeup_time = System.currentTimeMillis() + length;
        val showAlarmPI = getTimerActivityIntent(context, id)
        val alarmInfo = AlarmManager.AlarmClockInfo(endTime.time, showAlarmPI)

        if (alarmManager != null) {
            alarmManager.setAlarmClock(alarmInfo, pendingAlarmIntent)
            if (BuildConfig.DEBUG) {
                val df = DateFormat.getTimeInstance(DateFormat.SHORT)
                val debugString = String.format(Locale.getDefault(), "Set alarm with id %d due %s", id, df.format(endTime))
                Log.d(TAG, debugString)
            }
        }
    }

    fun cancelAlarm(context: Context, id: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingAlarmIntent = getPI(context, id)
        if (alarmManager != null) {
            alarmManager.cancel(pendingAlarmIntent)
            if (BuildConfig.DEBUG) {
                val debugString = String.format(Locale.getDefault(), "Cancelled alarm with id %d", id)
                Log.d(TAG, debugString)
            }
        }
    }

    fun startMainTimer(context: Context, vm: AlarmViewModel) {
        restartTimer(context, vm, false)
    }

    fun startSnoozeTimer(context: Context, vm: AlarmViewModel) {
        restartTimer(context, vm, true)
    }

    private fun restartTimer(context: Context, vm: AlarmViewModel, isSnooze: Boolean) {

        val timerDuration = if (isSnooze) {
            PreferenceUtils.getSnoozeTimerLength(context)
        } else {
            PreferenceUtils.getMainTimerLength(context)
        }

        runOnIoThread {
            val currentTime = Calendar.getInstance()
            val endTime = Calendar.getInstance().also { it.add(Calendar.MILLISECOND, timerDuration.toInt()) }
            if (isSnooze) {
                val currentAlarm = vm.getCurrentAlarm()
                if (currentAlarm != null) {
                    vm.snooze(endTime.time)
                    AlarmNotificationsBuilder.showSnoozingAlarmNotification(context, currentAlarm)
                    setNewAlarmClock(context, currentAlarm.id, endTime.time)
                }
            } else {
                val alarm = Alarm(Alarm.STATE_WAITING, currentTime.time, endTime.time)
                val id = vm.add(alarm)
                setNewAlarmClock(context, id, alarm.endTime)
                AlarmNotificationsBuilder.showWaitingAlarmNotification(context, alarm)
            }
        }
    }
}
