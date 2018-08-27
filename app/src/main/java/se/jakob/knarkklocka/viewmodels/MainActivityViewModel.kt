package se.jakob.knarkklocka.viewmodels

import se.jakob.knarkklocka.data.AlarmRepository

class MainActivityViewModel(repository: AlarmRepository) : AlarmViewModel(repository) {

        override var alarm = repository.currentAlarm

}


