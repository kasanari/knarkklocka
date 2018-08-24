package se.jakob.knarkklocka.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel

import java.util.Date

import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository

class AlarmViewModel internal constructor(internal var mRepository: AlarmRepository)//this.mAlarm = mRepository.getActiveAlarm();
    : ViewModel() {
    var alarm: LiveData<Alarm>? = null
        internal set

    val currentAlarm: Alarm?
        get() = alarm!!.value

    fun add(alarm: Alarm): Long {
        return mRepository.insert(alarm)
    }

    fun delete() {
        if (alarm!!.value != null) {
            mRepository.delete(alarm!!.value!!)
        }
    }

    fun kill() {
        if (alarm!!.value != null) {
            val alarm = this.alarm!!.value
            alarm!!.state = Alarm.STATE_DEAD
            mRepository.update(alarm)
        }
    }

    fun snooze(endTime: Date) {
        if (alarm!!.value != null) {
            val alarm = this.alarm!!.value
            alarm!!.state = Alarm.STATE_SNOOZING
            alarm.incrementSnoozes()
            alarm.endTime = endTime
            mRepository.update(alarm)
        }
    }

}
