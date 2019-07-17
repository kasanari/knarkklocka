package se.jakob.knarkklocka.utils

import android.app.AlarmManager
import android.content.Context
import android.text.format.DateUtils
import se.jakob.knarkklocka.BuildConfig

/**
 * This class is used to automatically finish [AlarmActivity] and [AlarmService]. It is a countdown timer which will execute a callback function
 * once the time has expired.
 */
class TimeOutClock(private val timeoutLength: Long, private val alarmCallback : AlarmManager.OnAlarmListener) {

    private var running = false

     fun start(context: Context) {
         if (!running) {
             val alarmManager = context.getSystemService(AlarmManager::class.java)
             val timeout = if (BuildConfig.DEBUG) {
                 10 * DateUtils.SECOND_IN_MILLIS
             } else {
                 timeoutLength
             }
             alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeout, "tag", alarmCallback, null)
             running = true
         }
    }

     fun stop(context: Context) {
         if (running) {
             val alarmManager = context.getSystemService(AlarmManager::class.java)
             alarmManager.cancel(alarmCallback)
             running = false
         }
    }
}