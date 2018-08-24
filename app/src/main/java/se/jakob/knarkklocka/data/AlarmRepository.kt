package se.jakob.knarkklocka.data

import android.arch.lifecycle.LiveData
import android.os.AsyncTask

class AlarmRepository private constructor(private val alarmDao: AlarmDao) {

    val currentAlarm: LiveData<Alarm>
        get() = alarmDao.loadSingleAlarmByState(Alarm.STATE_WAITING, Alarm.STATE_SNOOZING, Alarm.STATE_ACTIVE)

    fun getLiveAlarmByID(id: Long): LiveData<Alarm> {
        return alarmDao.loadLiveAlarmById(id)
    }

    fun getAlarmByID(id: Long): Alarm {
        return alarmDao.loadAlarmById(id)
    }

    internal fun getAlarmsByState(state: Int): LiveData<Alarm> {
        return alarmDao.loadAlarmsByState(state)
    }

    fun changeAlarmState(id: Long, state: Int) {
        val alarm = getAlarmByID(id)
        alarm.state = state
        alarmDao.updateAlarm(alarm)
    }

    fun getAllAlarms(): LiveData<MutableList<Alarm>>? {
        return alarmDao.loadAllAlarms()
    }

    fun deleteAll() {
        ClearAsyncTask(alarmDao).execute()
    }

    fun insert(alarm: Alarm): Long {
        return alarmDao.insert(alarm)
    }

    fun delete(alarm: Alarm) {
        DeleteAsyncTask(alarmDao).execute(alarm)
    }

    fun update(alarm: Alarm) {
        UpdateAsyncTask(alarmDao).execute(alarm)
    }

    private class UpdateAsyncTask internal constructor(private val mAsyncTaskDao: AlarmDao) : AsyncTask<Alarm, Void, Void>() {

        override fun doInBackground(vararg params: Alarm): Void? {
            mAsyncTaskDao.updateAlarm(params[0])
            return null
        }
    }

    private class DeleteAsyncTask internal constructor(private val mAsyncTaskDao: AlarmDao) : AsyncTask<Alarm, Void, Void>() {

        override fun doInBackground(vararg params: Alarm): Void? {
            mAsyncTaskDao.deleteAlarm(params[0])
            return null
        }
    }

    private class ClearAsyncTask internal constructor(private val mAsyncTaskDao: AlarmDao) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg voids: Void): Void? {
            mAsyncTaskDao.deleteAll()
            return null
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
