package se.jakob.knarkklocka

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.runner.AndroidJUnit4
import org.junit.runner.RunWith
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking


import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository
import se.jakob.knarkklocka.data.AlarmState
import se.jakob.knarkklocka.utilities.*
import se.jakob.knarkklocka.utils.InjectorUtils
import se.jakob.knarkklocka.utils.PreferenceUtils
import se.jakob.knarkklocka.utils.TimerUtils
import java.util.*

@RunWith(AndroidJUnit4::class)
class TimerUtilsTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: AlarmRepository

    @Before
    fun waitForABit() { // To let the system settle
        Thread.sleep(1000)
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = InjectorUtils.getAlarmRepository(context)
        runBlocking {
            repository.deleteAll()
        }
        PreferenceUtils.shortMode = false
        PreferenceUtils.setMainTimerLength(context, testTimerLength)
        PreferenceUtils.setSnoozeTimerLength(context, testSnoozeLength)
    }

    @Test
    fun startAlarmTest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val endTime = Calendar.getInstance().apply {add(Calendar.MILLISECOND, testTimerLength.toInt())}.time
        TimerUtils.startMainTimer(context)
        Thread.sleep(2000) //Wait a bit for db operation to finish
        val alarm : Alarm = getValue(repository.currentLiveAlarm)
        val isSet = TimerUtils.alarmIsSet(context, alarm.id)
        assertTrue(isSet)
        assertEquals(alarm.endTime.time/1000, endTime.time/1000)
    }

    @Test
    fun startAndCancelAlarmTest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TimerUtils.startMainTimer(context)
        Thread.sleep(2000) //Wait a bit for db operation to finish
        val alarm : Alarm = getValue(repository.currentLiveAlarm)
        var isSet = TimerUtils.alarmIsSet(context, alarm.id)
        assertTrue(isSet)

        // Cancel the alarm
        TimerUtils.cancelAlarm(context, alarm.id)
        isSet = TimerUtils.alarmIsSet(context, alarm.id)
        assertFalse(isSet)
    }

    @Test
    fun snoozeAlarmTest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository: AlarmRepository = InjectorUtils.getAlarmRepository(context)
        var alarm = createTestAlarm(Calendar.getInstance(), testTimerLength)
        val newEndTime = Calendar.getInstance().apply { add(Calendar.MILLISECOND, testSnoozeLength.toInt()) }.time
        alarm.activate()
        runBlocking {
            val id = repository.insert(alarm).await()
            alarm.id = id
            TimerUtils.startSnoozeTimer(context, alarm)
            Thread.sleep(2000) //Wait a bit for db operation to finish
            alarm = getValue(repository.getLiveAlarmByID(id))
            assertEquals(alarm.state, AlarmState.STATE_SNOOZING)
            assertEquals(alarm.endTime.time/1000, newEndTime.time/1000)
        }
    }

    @After
    fun cancelAlarms() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TimerUtils.cancelAlarm(context, -1)
    }
}