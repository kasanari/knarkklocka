package se.jakob.knarkklocka.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel

import java.util.Date

import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository
import se.jakob.knarkklocka.data.AlarmState

abstract class AlarmViewModel internal constructor(private val repository: AlarmRepository)
    : ViewModel() {

    abstract var alarm : LiveData<Alarm>

    private var hasAlarm: Boolean = false
        get() = alarm.value != null


    fun getCurrentAlarm() : Alarm? {
        return alarm.value
    }

    fun add(alarm: Alarm): Long {
        return repository.insert(alarm)
    }

    fun delete() {
        if (alarm.value != null) {
            repository.delete(alarm.value!!)
        }
    }

    fun kill() {
        if (hasAlarm) {
            val alarm = this.alarm.value
            alarm!!.state = AlarmState.STATE_DEAD
            repository.update(alarm)
        }
    }

    fun snooze(endTime: Date) {
        if (hasAlarm) {
            val alarm : Alarm = this.alarm.value!!
            alarm.state = AlarmState.STATE_SNOOZING
            alarm.incrementSnoozes()
            alarm.endTime = endTime
            repository.update(alarm)
        }
    }



}
