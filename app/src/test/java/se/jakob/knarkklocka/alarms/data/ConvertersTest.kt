package se.jakob.knarkklocka.alarms.data


import org.junit.Assert.assertEquals
import org.junit.Test
import se.jakob.knarkklocka.data.AlarmState
import se.jakob.knarkklocka.data.Converters
import java.util.Calendar
import java.util.Calendar.*

class ConvertersTest {

    private val cal = Calendar.getInstance().apply {
        set(YEAR, 1995)
        set(MONTH, MARCH)
        set(DAY_OF_MONTH, 7)
    }

    @Test
    fun dateToTimestamp() {
        assertEquals(cal.timeInMillis, Converters().toTimestamp(cal.time))
    }

    @Test
    fun timeStampToDate() {
        assertEquals(Converters().toDate(cal.timeInMillis), cal.time)
    }

    @Test
    fun alarmStateToInt() {
        assertEquals(Converters().toAlarmState(0), AlarmState.STATE_WAITING)
        assertEquals(Converters().toAlarmState(1), AlarmState.STATE_DEAD)
        assertEquals(Converters().toAlarmState(2), AlarmState.STATE_ACTIVE)
        assertEquals(Converters().toAlarmState(3), AlarmState.STATE_SNOOZING)
    }

    @Test
    fun intToAlarmState() {
        assertEquals(Converters().fromAlarmState(AlarmState.STATE_WAITING), 0)
        assertEquals(Converters().fromAlarmState(AlarmState.STATE_DEAD), 1)
        assertEquals(Converters().fromAlarmState(AlarmState.STATE_ACTIVE), 2)
        assertEquals(Converters().fromAlarmState(AlarmState.STATE_SNOOZING), 3)
    }
}