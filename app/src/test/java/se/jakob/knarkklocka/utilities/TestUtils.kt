package se.jakob.knarkklocka.utilities

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

fun createTestAlarm(id : Long, state: AlarmState, startTime: Calendar, length: Long) : Alarm {
    val endTime : Calendar = testCalendar.apply { add(Calendar.MILLISECOND, length.toInt()) }
    return Alarm(id, state, startTime.time, endTime.time)
}