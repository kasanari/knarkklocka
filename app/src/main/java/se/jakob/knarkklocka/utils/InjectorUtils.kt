package se.jakob.knarkklocka.utils

import android.content.Context
import se.jakob.knarkklocka.data.AlarmDatabase
import se.jakob.knarkklocka.data.AlarmRepository
import se.jakob.knarkklocka.viewmodels.AlarmActivityViewModelFactory
import se.jakob.knarkklocka.viewmodels.AlarmHistoryViewModelFactory
import se.jakob.knarkklocka.viewmodels.MainActivityViewModelFactory

object InjectorUtils {

    @JvmStatic
    fun getAlarmRepository(context: Context): AlarmRepository {
        return AlarmRepository.getInstance(AlarmDatabase.getInstance(context).alarmDao())
    }

    @JvmStatic
    fun provideMainActivityViewModelFactory(
            context: Context
    ): MainActivityViewModelFactory {
        return MainActivityViewModelFactory(getAlarmRepository(context))
    }

    @JvmStatic
    fun provideAlarmHistoryViewModelFactory(
            context: Context
    ): AlarmHistoryViewModelFactory {
        return AlarmHistoryViewModelFactory(getAlarmRepository(context))
    }

    @JvmStatic
    fun provideAlarmActivityViewModelFactory(
            context: Context,
            alarmId: Long
    ): AlarmActivityViewModelFactory {
        return AlarmActivityViewModelFactory(getAlarmRepository(context), alarmId)
    }

}
