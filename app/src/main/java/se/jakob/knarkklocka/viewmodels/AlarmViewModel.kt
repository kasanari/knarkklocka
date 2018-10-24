package se.jakob.knarkklocka.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository
import se.jakob.knarkklocka.data.AlarmState
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
        return repository.insert(alarm)
    }

    fun delete() {
        liveAlarm.value?.let {
            repository.delete(it)
        }
    }

    fun kill() {
        liveAlarm.value?.let {
            repository.changeAlarmState(it, AlarmState.STATE_DEAD)
        }
    }

    fun snooze(endTime: Date) {
        liveAlarm.value?.let {
            it.snooze(endTime)
            repository.update(it)
        }
    }
}
