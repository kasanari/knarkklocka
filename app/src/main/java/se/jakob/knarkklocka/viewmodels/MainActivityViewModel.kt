package se.jakob.knarkklocka.viewmodels

import android.arch.lifecycle.ViewModel
import se.jakob.knarkklocka.data.AlarmRepository

class MainActivityViewModel(repository: AlarmRepository) : AlarmViewModel(repository) {

        override var alarm = repository.currentAlarm

}


