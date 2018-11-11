package se.jakob.knarkklocka.data
import androidx.lifecycle.LiveData
import se.jakob.knarkklocka.utils.runOnIoThread

class AlarmRepository private constructor(private val alarmDao: AlarmDao) {

    val currentAlarm
        get() = alarmDao.getMostRecentAlarm()

    fun getLiveAlarmByID(id: Long): LiveData<Alarm> {
        return alarmDao.getLiveAlarm(id)
    }

    fun getAlarmByID(id: Long): Alarm? {
        return alarmDao.getAlarm(id)
    }

    internal fun getAlarmsByStates(vararg states: AlarmState): LiveData<List<Alarm>> {
        return alarmDao.getAlarmsByStates(*Converters().fromAlarmState(*states))
    }

    fun changeAlarmState(alarm: Alarm, state: AlarmState) {
        alarm.state = state
        runOnIoThread {
            alarmDao.updateAlarm(alarm)
        }
    }

    fun getAllAlarms(): LiveData<List<Alarm>> {
        return alarmDao.getAllAlarms()
    }

    fun deleteAll() {
        runOnIoThread {
            alarmDao.deleteAll()
        }
    
    private fun ensureUnique(state: AlarmState) {
        alarmDao.deleteAlarmsByStates(Converters().fromAlarmState(state))
    }

    fun insert(alarm: Alarm): Long {
        return alarmDao.insert(alarm)
    }

    fun delete(alarm: Alarm) {
        runOnIoThread {
            alarmDao.deleteAlarm(alarm)
        }
    }

    fun safeInsert(alarm: Alarm): Long? {
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

    fun safeDelete(alarm: Alarm): Boolean {
        return if (alarm.waiting) {
            delete(alarm)
            true
        } else {
            false
        }
    }

    /**
     * Update an alarm in the DB, and check for inconsistencies
     * **/
    fun safeUpdate(alarm: Alarm): Boolean {
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
