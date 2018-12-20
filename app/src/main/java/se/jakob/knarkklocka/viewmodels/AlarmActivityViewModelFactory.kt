package se.jakob.knarkklocka.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import se.jakob.knarkklocka.data.AlarmRepository

/**
 * Factory for creating a [AlarmActivityViewModel] with a constructor that takes a
 * [AlarmRepository].
 */

class AlarmActivityViewModelFactory(
        private val alarmRepository: AlarmRepository,
        private val alarmID: Long
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlarmActivityViewModel(alarmRepository, alarmID) as T
    }
}
