package se.jakob.knarkklocka.viewmodels

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel

import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository

class AlarmHistoryViewModel internal constructor(private val repository: AlarmRepository) : AlarmViewModel(repository) {

    val allAlarms = repository.getAllAlarms()
    override var alarm = repository.currentAlarm

    fun clearHistory() {
        repository.deleteAll()
    }

}
