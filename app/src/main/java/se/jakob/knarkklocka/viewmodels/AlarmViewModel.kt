package se.jakob.knarkklocka.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import se.jakob.knarkklocka.BuildConfig
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository
import se.jakob.knarkklocka.utils.AlarmStateChanger
import java.text.SimpleDateFormat
import java.util.*

/**
 * Abstract base class for the viewmodels used in various places thorough the app.
 */
abstract class AlarmViewModel internal constructor(private val repository: AlarmRepository)
    : ViewModel() {

    abstract var liveAlarm: LiveData<Alarm>

    private val df = SimpleDateFormat("HH:mm", Locale.getDefault())

   val hasAlarm: Boolean
        get() = liveAlarm.value != null

    val isDead: LiveData<Boolean>
        get() = Transformations.map(liveAlarm) { alarm : Alarm? ->
            alarm?.run {
                dead
            } ?: true
        }

    val isFiring: LiveData<Boolean>
        get() = Transformations.map(liveAlarm) { alarm : Alarm? ->
            alarm?.run {
                active or missed
            } ?: false
        }

    val buttonText : LiveData<String>
        get() = Transformations.map(liveAlarm) { alarm : Alarm? ->
            alarm?.run {
                if(dead) "Start" else "Restart"
            } ?: "Start"
        }

    var endTimeStringCache = " "

    val endTimeString : LiveData<String>
        get() = Transformations.map(liveAlarm) { alarm ->
            alarm?.run {
                endTimeStringCache = df.format(alarm.endTime)
                (df.format(alarm.endTime))
            } ?: endTimeStringCache

        }

    internal fun deleteAll() {
        launchCoroutine {
            repository.deleteAll()
        }
    }

    private fun launchCoroutine(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            block()
        }
    }
}
