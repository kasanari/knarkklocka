package se.jakob.knarkklocka.data

import android.arch.lifecycle.LiveData
import se.jakob.knarkklocka.utils.runOnIoThread

class AlarmRepository private constructor(private val alarmDao: AlarmDao) {

    val currentAlarm: LiveData<Alarm>
        get() = alarmDao.loadMostRecentAlarm()

    fun getLiveAlarmByID(id: Long): LiveData<Alarm> {
        return alarmDao.loadLiveAlarmById(id)
    }

    fun getAlarmByID(id: Long): Alarm {
        return alarmDao.loadAlarmById(id)
    }

    internal fun getAlarmsByState(state: AlarmState): LiveData<Alarm> {
        return alarmDao.loadAlarmsByState(Converters().fromAlarmState(state))
    }

    fun changeAlarmState(id: Long, state: AlarmState) {
        val alarm = getAlarmByID(id)
        alarm.state = state
        alarmDao.updateAlarm(alarm)
    }

    fun getAllAlarms(): LiveData<List<Alarm>>? {
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
