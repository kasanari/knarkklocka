package se.jakob.knarkklocka.viewmodels
import se.jakob.knarkklocka.data.AlarmRepository

class AlarmHistoryViewModel internal constructor(repository: AlarmRepository) : AlarmViewModel(repository) {

    val allAlarms = repository.getAllAlarms()
    override var liveAlarm = repository.currentLiveAlarm

    fun clearHistory() {
        deleteAll()
    }

}
