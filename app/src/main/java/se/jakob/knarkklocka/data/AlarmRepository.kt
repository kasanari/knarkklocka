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
