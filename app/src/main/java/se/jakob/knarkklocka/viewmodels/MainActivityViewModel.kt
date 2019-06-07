package se.jakob.knarkklocka.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import se.jakob.knarkklocka.data.AlarmRepository

class MainActivityViewModel(repository: AlarmRepository) : AlarmViewModel(repository) {

    override var liveAlarm = repository.currentLiveAlarm

    fun getStateText(): LiveData<String> {
        return Transformations.map(liveAlarm) { alarm ->
            (alarm?.stateToString ?: "No alarm")
        }
    }
}


