package se.jakob.knarkklocka.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import se.jakob.knarkklocka.BuildConfig
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository
import java.util.*

abstract class AlarmViewModel internal constructor(private val repository: AlarmRepository)
    : ViewModel() {

    abstract var liveAlarm: LiveData<Alarm>

    var hasAlarm: Boolean = false
        get() = liveAlarm.value != null


    fun getCurrentAlarm(): Alarm? {
        return liveAlarm.value
    }

    fun add(alarm: Alarm): Long? {
        return repository.safeInsert(alarm)
    }

    fun sleep() = GlobalScope.launch {
        var success: Boolean
        liveAlarm.value?.let { alarm ->
            success = when {
                alarm.active or alarm.snoozing -> {
                    alarm.kill()
                    repository.safeUpdate(alarm)
                }
                alarm.waiting -> repository.safeDelete(alarm)
                else -> false
            }
            if (!success) {
                if (BuildConfig.DEBUG) {
                    throw Exception("Failed to put alarm to sleep")
                }
            }
        }
    }

    fun kill() = GlobalScope.launch {
        liveAlarm.value?.let { alarm ->
            alarm.kill()
            repository.safeUpdate(alarm)
        }
    }

    fun snooze(endTime: Date) = GlobalScope.launch {
        liveAlarm.value?.let { alarm ->
            alarm.snooze(endTime)
            repository.safeUpdate(alarm)
        }
    }
}
