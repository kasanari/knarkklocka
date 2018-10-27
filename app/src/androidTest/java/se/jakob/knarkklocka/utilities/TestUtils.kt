package se.jakob.knarkklocka.utilities

import android.text.format.DateUtils
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState
import java.util.*

/**
 * [Calendar] object used for tests.
 */
val testCalendar: Calendar = Calendar.getInstance().apply {
    set(Calendar.YEAR, 2018)
    set(Calendar.MONTH, Calendar.JANUARY)
    set(Calendar.DAY_OF_MONTH, 16)
}

const val testTimerLength = 4* DateUtils.HOUR_IN_MILLIS
const val testSnoozeLength = 5 * DateUtils.MINUTE_IN_MILLIS

fun createTestAlarm(id : Long, state: AlarmState, startTime: Calendar, lengthInMillis: Long) : Alarm {
    val endTime : Calendar = testCalendar.apply { add(Calendar.MILLISECOND, lengthInMillis.toInt()) }
    return Alarm(id, state, startTime.time, endTime.time)
}

fun createTestAlarm(startTime: Calendar, lengthInMillis: Long) : Alarm {
    val endTime : Calendar = testCalendar.apply { add(Calendar.MILLISECOND, lengthInMillis.toInt()) }
    return Alarm(startTime.time, endTime.time)
}