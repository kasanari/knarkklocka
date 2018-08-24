package se.jakob.knarkklocka.viewmodels

import android.app.Application

class AlarmActivityViewModel(application: Application) : AlarmViewModel(application) {

    fun setAlarm(id: Long) {
        super.mAlarm = mRepository.getLiveAlarmByID(id)
    }
}
