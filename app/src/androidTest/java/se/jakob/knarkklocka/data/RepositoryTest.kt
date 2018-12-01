package se.jakob.knarkklocka.data

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import se.jakob.knarkklocka.utilities.createTestAlarm
import se.jakob.knarkklocka.utilities.getValue
import se.jakob.knarkklocka.utilities.testCalendar
import se.jakob.knarkklocka.utilities.testTimerLength
import java.lang.Exception

class RepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AlarmDatabase
    private lateinit var alarmDao: AlarmDao
    private lateinit var repository: AlarmRepository

    @Before
    fun createDB() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AlarmDatabase::class.java).build()
        alarmDao = database.alarmDao()
        repository = AlarmRepository.getInstance(alarmDao)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertConsistencyTest() {
        var alarm1 = createTestAlarm(testCalendar, testTimerLength)
        var alarm2 = createTestAlarm(testCalendar, testTimerLength)
        runBlocking {
            val id1 = repository.safeInsert(alarm1)
            val id2 = repository.safeInsert(alarm2)
            Thread.sleep(2000)
            alarm1 = getValue(repository.getLiveAlarmByID(id1!!))
            alarm2 = getValue(repository.getLiveAlarmByID(id2!!))
            assertNull(alarm1)
            assertEquals(AlarmState.STATE_WAITING, alarm2.state)
            alarm2.activate()
            try {
                repository.safeInsert(alarm2)
                fail()
            } catch (e : Exception) {
                // Success
            }
        }
    }

    @Test
    fun updateConsistencyTest() {
        var alarm1 = createTestAlarm(testCalendar, testTimerLength)
        var alarm2 = createTestAlarm(testCalendar, testTimerLength)
        runBlocking {
            alarm1.id = repository.insert(alarm1).await()
            alarm2.id = repository.insert(alarm2).await()
            Thread.sleep(2000)
            alarm1.activate()
            alarm2.activate()
            repository.safeUpdate(alarm1)
            repository.safeUpdate(alarm2)
            Thread.sleep(4000)
            alarm1 = getValue(repository.getLiveAlarmByID(alarm1.id))
            alarm2 = getValue(repository.getLiveAlarmByID(alarm2.id))
            assertNull(alarm1)
            assertEquals(AlarmState.STATE_ACTIVE, alarm2.state)
        }
    }

    @Test
    fun deleteConsistencyTest() {
        var alarm1 = createTestAlarm(testCalendar, testTimerLength)
        alarm1.activate()
        runBlocking {
            val id1 = repository.safeInsert(alarm1)
            Thread.sleep(2000)
            alarm1 = getValue(repository.getLiveAlarmByID(id1!!))
            repository.safeDelete(alarm1)

            alarm1 = getValue(repository.getLiveAlarmByID(id1))
        }
    }
}