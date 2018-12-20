package se.jakob.knarkklocka.alarms.data

import org.junit.Assert.assertEquals
import android.text.format.DateUtils.HOUR_IN_MILLIS
import org.junit.Assert
import org.junit.Test
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState
import java.util.*


class AlarmTest {

    private val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, 1995)
        set(Calendar.MONTH, Calendar.MARCH)
        set(Calendar.DAY_OF_MONTH, 7)
    }

    private fun createTestAlarm(startTime: Calendar, lengthInMillis: Long) : Alarm {
        val endTime : Calendar = startTime.apply { add(Calendar.MILLISECOND, lengthInMillis.toInt()) }
        return Alarm(startTime.time, endTime.time)
    }

    @Test
    fun testDefaultValues() {
        val alarm = createTestAlarm(cal, 4*HOUR_IN_MILLIS)
        assertEquals(alarm.state, AlarmState.STATE_WAITING)
        assertEquals(alarm.snoozes, 0)
    }

    @Test
    fun testSnoozing() {
        val alarm = createTestAlarm(cal, 4*HOUR_IN_MILLIS)
        val endTime : Calendar = cal.apply { add(Calendar.MILLISECOND, 1000)}
        alarm.activate()
        alarm.snooze(endTime.time)
        assertEquals(alarm.endTime, endTime.time)
        assertEquals(alarm.snoozes, 1)
    }

    @Test
    fun testInvalidTransitions() {
        val alarm = createTestAlarm(cal, 4*HOUR_IN_MILLIS)
        val endTime : Calendar = cal.apply { add(Calendar.MILLISECOND, 1000)}
        assertEquals(alarm.state, AlarmState.STATE_WAITING)
        try {
            alarm.snooze(endTime.time)
            Assert.fail()
        } catch (e : Alarm.InvalidStateChangeException) {
            // success
        }
        alarm.kill()
        assertEquals(alarm.state, AlarmState.STATE_DEAD)
        try {
            alarm.activate()
            Assert.fail()
        } catch (e : Alarm.InvalidStateChangeException) {
            // success
        }
    }
}