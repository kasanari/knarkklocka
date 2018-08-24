package se.jakob.knarkklocka.viewmodels

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

import se.jakob.knarkklocka.data.AlarmRepository

/**
 * Factory for creating a [MainActivityViewModel] with a constructor that takes a
 * [AlarmRepository].
 */

class MainActivityViewModelFactory(
        private val alarmRepository: AlarmRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainActivityViewModel(alarmRepository) as T
    }
}
