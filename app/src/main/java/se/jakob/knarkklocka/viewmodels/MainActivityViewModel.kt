package se.jakob.knarkklocka.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import se.jakob.knarkklocka.data.AlarmRepository

class MainActivityViewModel(repository: AlarmRepository) : AlarmViewModel(repository) {

    override var liveAlarm = repository.currentLiveAlarm

    var stringCache : String = " "

    fun getStateText(): LiveData<String> {
        return Transformations.map(liveAlarm) { alarm ->
            stringCache = alarm?.stateToString ?: stringCache // If there is no string to get, use the previous one
            (stringCache)
        }
    }
}


