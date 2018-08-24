package se.jakob.knarkklocka.viewmodels

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

import se.jakob.knarkklocka.data.AlarmRepository

/**
 * Factory for creating a [AlarmViewModel] with a constructor that takes a
 * [AlarmRepository].
 */

internal class AlarmActivityViewModelFactory
