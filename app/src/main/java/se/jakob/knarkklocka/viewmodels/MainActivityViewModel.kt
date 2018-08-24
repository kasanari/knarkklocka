package se.jakob.knarkklocka.viewmodels

import android.app.Application

class MainActivityViewModel(application: Application) : AlarmViewModel(application) {
    init {
        super.mAlarm = mRepository.currentAlarm
    }
}


