package se.jakob.knarkklocka.data

import android.app.Application
import android.arch.lifecycle.LiveData
import android.os.AsyncTask

class AlarmRepository(application: Application) {
    private val mAlarmDao: AlarmDao
    val allAlarms: LiveData<List<Alarm>>

    val currentAlarm: LiveData<Alarm>
        get() = mAlarmDao.loadSingleAlarmByState(Alarm.STATE_WAITING, Alarm.STATE_SNOOZING, Alarm.STATE_ACTIVE)

    internal val activeAlarm: LiveData<Alarm>
        get() = mAlarmDao.loadSingleAlarmByState(Alarm.STATE_ACTIVE)

    init {
        val db = AlarmDatabase.getDatabase(application)
        mAlarmDao = db.alarmDao()
        allAlarms = mAlarmDao.loadAllAlarms()
    }

    fun getLiveAlarmByID(id: Long): LiveData<Alarm> {
        return mAlarmDao.loadLiveAlarmById(id)
    }

    fun getAlarmByID(id: Long): Alarm {
        return mAlarmDao.loadAlarmById(id)
    }

    internal fun getAlarmsByState(state: Int): LiveData<Alarm> {
        return mAlarmDao.loadAlarmsByState(state)
    }

    fun changeAlarmState(id: Long, state: Int) {
        val alarm = getAlarmByID(id)
        alarm.state = state
        mAlarmDao.updateAlarm(alarm)
    }

    fun deleteAll() {
        clearAsyncTask(mAlarmDao).execute()
    }

    fun insert(alarm: Alarm): Long {
        return mAlarmDao.insert(alarm)
        //new insertAsyncTask(mAlarmDao).execute(alarm);
    }

    fun delete(alarm: Alarm) {
        deleteAsyncTask(mAlarmDao).execute(alarm)
    }

    fun update(alarm: Alarm) {
        updateAsyncTask(mAlarmDao).execute(alarm)
    }

    private class updateAsyncTask internal constructor(private val mAsyncTaskDao: AlarmDao) : AsyncTask<Alarm, Void, Void>() {

        override fun doInBackground(vararg params: Alarm): Void? {
            mAsyncTaskDao.updateAlarm(params[0])
            return null
        }
    }

    private class insertAsyncTask internal constructor(private val mAsyncTaskDao: AlarmDao) : AsyncTask<Alarm, Void, Void>() {

        override fun doInBackground(vararg params: Alarm): Void? {
            mAsyncTaskDao.insert(params[0])
            return null
        }
    }

    private class deleteAsyncTask internal constructor(private val mAsyncTaskDao: AlarmDao) : AsyncTask<Alarm, Void, Void>() {

        override fun doInBackground(vararg params: Alarm): Void? {
            mAsyncTaskDao.deleteAlarm(params[0])
            return null
        }
    }

    private class clearAsyncTask internal constructor(private val mAsyncTaskDao: AlarmDao) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg voids: Void): Void? {
            mAsyncTaskDao.deleteAll()
            return null
        }
    }
}
