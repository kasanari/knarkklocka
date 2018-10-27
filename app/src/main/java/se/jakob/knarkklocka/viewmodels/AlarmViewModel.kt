package se.jakob.knarkklocka.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
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

    fun add(alarm: Alarm): Long {
        return repository.safeInsert(alarm)
    }

    fun sleep() {
        liveAlarm.value?.let { alarm ->
            if (alarm.active or alarm.snoozing) {
                alarm.kill()
                repository.safeUpdate(alarm)
            } else if (alarm.waiting) {
                repository.safeDelete(alarm)
            }
        }
    }

    fun kill() {
        liveAlarm.value?.let { alarm ->
            alarm.kill()
            repository.safeUpdate(alarm)
        }
    }

    fun snooze(endTime: Date) {
        liveAlarm.value?.let { alarm ->
            alarm.snooze(endTime)
            repository.safeUpdate(alarm)
        }
    }
}
