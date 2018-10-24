package se.jakob.knarkklocka.data


import androidx.room.Room
import androidx.test.InstrumentationRegistry

import org.hamcrest.Matchers.equalTo

import org.junit.Assert.assertThat

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import se.jakob.knarkklocka.utilities.getValue

import java.util.*

class AlarmDaoTest {
    private lateinit var database: AlarmDatabase
    private lateinit var alarmDao: AlarmDao
    private val alarmA = Alarm(1, AlarmState.STATE_DEAD, Calendar.getInstance().apply { add(Calendar.MINUTE, 4) }.time, Calendar.getInstance().apply { add(Calendar.HOUR, 4) }.time)
    private val alarmB = Alarm(2, AlarmState.STATE_WAITING, Calendar.getInstance().apply { add(Calendar.MINUTE, 5) }.time, Calendar.getInstance().apply { add(Calendar.HOUR, 4) }.time)
    private val alarmC = Alarm(3, AlarmState.STATE_SNOOZING, Calendar.getInstance().apply { add(Calendar.MINUTE, 6) }.time, Calendar.getInstance().apply { add(Calendar.HOUR, 4) }.time)

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        database = Room.inMemoryDatabaseBuilder(context, AlarmDatabase::class.java).build()
        alarmDao = database.alarmDao()

        alarmDao.insertAll(listOf(alarmB, alarmC, alarmA))
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testGetAllAlarms() {
        val alarmList = getValue(alarmDao.getAllAlarms())
        Assert.assertThat(alarmList.size, equalTo(3))

        // Ensure alarm list is sorted by endTime and startTime
        assertThat(alarmList[0], equalTo(alarmC))
        assertThat(alarmList[1], equalTo(alarmB))
        assertThat(alarmList[2], equalTo(alarmA))
    }

    @Test
    fun testGetMostRecentAlarm() {
        val alarm = getValue(alarmDao.getMostRecentAlarm())

        assertThat(alarm, equalTo(alarmC))
    }

    @Test
    fun testGetPlant() {
        assertThat(alarmDao.getAlarm(alarmA.id), equalTo(alarmA))
    }
}