package se.jakob.knarkklocka.viewmodels
import se.jakob.knarkklocka.data.AlarmRepository

class AlarmActivityViewModel(repository: AlarmRepository, id : Long) : AlarmViewModel(repository) {

        override var alarm = repository.getLiveAlarmByID(id)

}
