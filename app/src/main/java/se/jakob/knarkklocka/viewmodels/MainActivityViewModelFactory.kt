package se.jakob.knarkklocka.viewmodels

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

import se.jakob.knarkklocka.data.AlarmRepository

/**
 * Factory for creating a [AlarmViewModel] with a constructor that takes a
 * [AlarmRepository].
 */

class AlarmViewModelFactory(
        private val alarmRepository: AlarmRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlarmViewModel(alarmRepository) as T
    }
}
