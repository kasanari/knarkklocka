package se.jakob.knarkklocka.data

import android.arch.lifecycle.LiveData
import se.jakob.knarkklocka.utils.runOnIoThread

class AlarmRepository private constructor(private val alarmDao: AlarmDao) {

    val currentAlarm
        get() = alarmDao.loadMostRecentAlarm()

    fun getLiveAlarmByID(id: Long): LiveData<Alarm> {
        return alarmDao.loadLiveAlarmById(id)
    }

    fun getAlarmByID(id: Long): Alarm {
        return alarmDao.loadAlarmById(id)
    }

    internal fun getAlarmsByStates(vararg states: AlarmState): LiveData<List<Alarm>> {
        return alarmDao.loadAlarmsByStates(*Converters().fromAlarmState(*states))
    }

    fun changeAlarmState(alarm: Alarm, state: AlarmState) {
        alarm.state = state
        runOnIoThread {
            alarmDao.updateAlarm(alarm)
        }
    }

    fun getAllAlarms(): LiveData<List<Alarm>> {
        return alarmDao.loadAllAlarms()
    }

    fun deleteAll() {
        runOnIoThread {
            alarmDao.deleteAll()
        }
    }

    fun insert(alarm: Alarm): Long {
        return alarmDao.insert(alarm)
    }

    fun delete(alarm: Alarm) {
        runOnIoThread {
            alarmDao.deleteAlarm(alarm)
        }
    }

    fun update(alarm: Alarm) {
        runOnIoThread {
            alarmDao.updateAlarm(alarm)
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
