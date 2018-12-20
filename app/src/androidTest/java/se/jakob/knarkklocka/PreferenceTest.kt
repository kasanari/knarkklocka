import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.jakob.knarkklocka.utilities.testTimerLength
import se.jakob.knarkklocka.utils.PreferenceUtils

@RunWith(AndroidJUnit4::class)
class TimerUtilsTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun setTimerLengthTest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        PreferenceUtils.shortMode = false
        PreferenceUtils.setMainTimerLength(context, testTimerLength)

        Assert.assertEquals(testTimerLength, PreferenceUtils.getMainTimerLength(context))

    }

}
