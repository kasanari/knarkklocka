package se.jakob.knarkklocka.viewmodels

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

import se.jakob.knarkklocka.data.AlarmRepository

/**
 * Factory for creating a [AlarmViewModel] with a constructor that takes a
 * [AlarmRepository].
 */

class AlarmActivityViewModelFactory(
        private val alarmRepository: AlarmRepository,
        private val alarmID : Long
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlarmActivityViewModel(alarmRepository, alarmID) as T
    }
}
