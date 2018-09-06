package se.jakob.knarkklocka.viewmodels

import android.arch.lifecycle.LiveData
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository

class AlarmHistoryViewModel internal constructor(private val repository: AlarmRepository) : AlarmViewModel(repository) {

    val allAlarms = repository.getAllAlarms()
    override var liveAlarm = repository.currentAlarm

    fun clearHistory() {
        repository.deleteAll()
    }

    fun getAlarms() = allAlarms
}
