package se.jakob.knarkklocka.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import se.jakob.knarkklocka.BuildConfig

class AlarmRepository private constructor(private val alarmDao: AlarmDao) {

    val currentLiveAlarm
        get() = alarmDao.getMostRecentLiveAlarm()

    val currentAlarm
        get() = alarmDao.getMostRecentAlarm()

     fun getLiveAlarmByID(id: Long): LiveData<Alarm> {
        return alarmDao.getLiveAlarm(id)
        }

    suspend fun getAlarmByID(id: Long): Alarm? {
        return withContext(IO) { alarmDao.getAlarm(id) }
    }

    internal fun getLiveAlarmsByStates(vararg states: AlarmState): LiveData<List<Alarm>> {
        return alarmDao.getLiveAlarmsByStates(*Converters().fromAlarmState(*states))
    }

    internal fun getAlarmsByStates(vararg states: AlarmState): List<Alarm> {
        return alarmDao.getAlarmsByStates(*Converters().fromAlarmState(*states))
    }

     fun getAllAlarms(): LiveData<List<Alarm>> {
        return alarmDao.getAllAlarms()
    }

    suspend fun deleteAll() {
        withContext(IO) {
            alarmDao.deleteAll()
        }
    }

    private suspend fun ensureUnique(state: AlarmState) {
        withContext(IO) {
            alarmDao.deleteAlarmsByStates(Converters().fromAlarmState(state))
        }
    }

    internal suspend fun insert(alarm: Alarm): Long {
        return withContext(IO) { alarmDao.insert(alarm) }
    }

    private suspend fun delete(alarm: Alarm) {
        withContext(IO) {
            alarmDao.deleteAlarm(alarm)
        }
    }

    private suspend fun update(alarm: Alarm) {
        withContext(IO) {
            alarmDao.updateAlarm(alarm)
        }
    }

    suspend fun safeInsert(alarm: Alarm): Long? {
        return if (alarm.state == AlarmState.STATE_WAITING) {
            ensureUnique(AlarmState.STATE_WAITING)
            insert(alarm)
        } else {
            if (BuildConfig.DEBUG) {
                throw Exception("Can only insert waiting Alarms")
            }
            null
        }
    }

    suspend fun safeDelete(alarm: Alarm): Boolean {
        return if (alarm.waiting) {
            delete(alarm)
            true
        } else {
            false
        }
    }

    /**
     * Update an alarm in the DB, and check for inconsistencies
     * @param alarm The alarm that is to be updated.
     * **/
    suspend fun safeUpdate(alarm: Alarm): Boolean {
        when (alarm.state) {
            AlarmState.STATE_WAITING -> {
                if (BuildConfig.DEBUG) {
                    throw Alarm.InvalidStateChangeException("Can not update an Alarm to be waiting.")
                }
                return false
            }
            AlarmState.STATE_DEAD, AlarmState.STATE_MISSED -> {
                update(alarm)
                return true
            }

            AlarmState.STATE_ACTIVE, AlarmState.STATE_SNOOZING -> {
                ensureUnique(alarm.state)
                update(alarm)
                return true
            }
        }
    }

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: AlarmRepository? = null

        fun getInstance(alarmDao: AlarmDao) =
                instance ?: synchronized(this) {
                    instance ?: AlarmRepository(alarmDao).also { instance = it }
                }
    }

}
