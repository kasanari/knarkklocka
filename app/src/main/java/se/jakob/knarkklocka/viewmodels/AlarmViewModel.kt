package se.jakob.knarkklocka.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import se.jakob.knarkklocka.BuildConfig
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository
import se.jakob.knarkklocka.data.AlarmState
import se.jakob.knarkklocka.utils.AlarmStateChanger
import java.text.SimpleDateFormat
import java.util.*

abstract class AlarmViewModel internal constructor(private val repository: AlarmRepository)
    : ViewModel() {

    abstract var liveAlarm: LiveData<Alarm>

    /**
     * This is the job for all coroutines started by this ViewModel.
     *
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = Job()
    private val df = SimpleDateFormat("HH:mm", Locale.getDefault())

    /**
     * This is the main scope for all coroutines launched by MainViewModel.
     *
     * Since we pass viewModelJob, you can cancel all coroutines launched by uiScope by calling
     * viewModelJob.cancel()
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

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
        launchDataLoad {
            repository.deleteAll()
        }
    }

    fun sleep() {
        var success: Boolean
        getData { alarm: Alarm ->
            launchDataLoad {
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
            launchDataLoad {
                if (!alarm.dead) {
                    AlarmStateChanger.kill(alarm, repository)
                }
            }
        }
    }

    fun miss() {
        getData { alarm: Alarm ->
            launchDataLoad {
                AlarmStateChanger.miss(alarm, repository)
            }
        }
    }

    fun snooze(endTime: Date) = {
        getData { alarm: Alarm ->
            launchDataLoad {
                AlarmStateChanger.snooze(alarm, endTime, repository)
            }
        }
    }

    /**
     * Cancel all coroutines when the ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * Helper function to call a data load function with a loading spinner, errors will trigger a
     * snackbar.
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually load data. It is called in the uiScope.
     */
    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return uiScope.launch {
            block()
        }
    }

    private fun getData(block: (Alarm) -> Unit) {
        liveAlarm.value?.let { alarm: Alarm ->
            block(alarm)
        }
    }

}
