package se.jakob.knarkklocka.viewmodels

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

import se.jakob.knarkklocka.data.AlarmRepository

/**
 * Factory for creating a [AlarmHistoryViewModel] with a constructor that takes a
 * [AlarmRepository].
 */

class AlarmHistoryViewModelFactory(
        private val alarmRepository: AlarmRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlarmHistoryViewModel(alarmRepository) as T
    }
}
