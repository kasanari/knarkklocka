package se.jakob.knarkklocka

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ServiceTestRule
import androidx.test.runner.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.jakob.knarkklocka.data.AlarmRepository
import se.jakob.knarkklocka.data.AlarmState
import se.jakob.knarkklocka.utilities.createTestAlarm
import se.jakob.knarkklocka.utilities.getValue
import se.jakob.knarkklocka.utilities.testSnoozeLength
import se.jakob.knarkklocka.utilities.testTimerLength
import se.jakob.knarkklocka.utils.InjectorUtils
import se.jakob.knarkklocka.utils.PreferenceUtils
import se.jakob.knarkklocka.utils.TimerUtils
import java.util.*

@RunWith(AndroidJUnit4::class)
class AlarmServiceTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val serviceRule = ServiceTestRule()

    lateinit var repository: AlarmRepository

    @Before
    fun waitForABit() { // To let the system settle
        Thread.sleep(1000)
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = InjectorUtils.getAlarmRepository(context)
        runBlocking {
            repository.deleteAll()
        }
        PreferenceUtils.setMainTimerLength(context, testTimerLength)
        PreferenceUtils.setSnoozeTimerLength(context, testSnoozeLength)
    }

    @Test
    fun startServiceTest() {
        var alarm = createTestAlarm(Calendar.getInstance(), testTimerLength)
        val context = ApplicationProvider.getApplicationContext<Context>()
        var id : Long = 0
        runBlocking {
            id = repository.insert(alarm).await()
            alarm = getValue(repository.getLiveAlarmByID(id))
        }
        val intent = TimerUtils.getAlarmServiceIntent(context, id)
        serviceRule.startService(intent)
        Thread.sleep(2000)
        assertEquals(AlarmState.STATE_ACTIVE, alarm.state)
    }
    
}