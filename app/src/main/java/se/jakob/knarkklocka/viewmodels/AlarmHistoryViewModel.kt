package se.jakob.knarkklocka.viewmodels

import android.app.Application
import android.arch.lifecycle.LiveData

import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository

class AlarmHistoryViewModel(application: Application) : ViewModel(application) {
    val allAlarms: LiveData<List<Alarm>>

    init {
        mRepository = AlarmRepository(application)
        super.mAlarm = mRepository.getCurrentAlarm()
        allAlarms = mRepository.getAllAlarms()
    }

    fun clearHistory() {
        mRepository.deleteAll()
    }

}
