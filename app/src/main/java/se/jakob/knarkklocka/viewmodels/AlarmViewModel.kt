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

    val endTimeString : LiveData<String>
        get() = Transformations.map(liveAlarm) { alarm ->
            alarm?.run {
                (df.format(alarm.endTime))
            } ?: "No alarm"

        }

    fun getCurrentAlarm(): Alarm? {
        return liveAlarm.value
    }

    internal fun deleteAll() {
        launchCoroutine {
            repository.deleteAll()
        }
    }

    fun sleep() {
        var success: Boolean
        getData { alarm: Alarm ->
            launchCoroutine {
                success = AlarmStateChanger.sleep(alarm, repository)
                if (!success) {
                    if (BuildConfig.DEBUG) {
                        throw Exception("Failed to put alarm to sleep")
                    }
                }
            }
        }
    }

    fun kill() {
        getData { alarm: Alarm ->
            launchCoroutine {
                if (!alarm.dead) {
                    AlarmStateChanger.kill(alarm, repository)
                }
            }
        }
    }

    fun miss() {
        getData { alarm: Alarm ->
            launchCoroutine {
                AlarmStateChanger.miss(alarm, repository)
            }
        }
    }

    fun snooze(endTime: Date) = run {
        getData { alarm: Alarm ->
            launchCoroutine {
                AlarmStateChanger.snooze(alarm, endTime, repository)
            }
        }
    }

    private fun launchCoroutine(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            block()
        }
    }

    private fun getData(block: (Alarm) -> Unit) {
        liveAlarm.value?.let { alarm: Alarm ->
            block(alarm)
        }
    }

}
