package se.jakob.knarkklocka.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class AlarmRepository {
    private AlarmDao mAlarmDao;
    private LiveData<List<Alarm>> mAllAlarms;

    public AlarmRepository(Application application) {
        AlarmDatabase db = AlarmDatabase.getDatabase(application);
        mAlarmDao = db.alarmDao();
        mAllAlarms = mAlarmDao.loadAllAlarms();
    }

    public LiveData<Alarm> getLiveAlarmByID(long id){return mAlarmDao.loadLiveAlarmById(id);}

    public Alarm getAlarmByID(long id){
        return mAlarmDao.loadAlarmById(id);
    }

    public LiveData<List<Alarm>> getAllAlarms() {
        return mAllAlarms;
    }

    LiveData<Alarm> getAlarmsByState(int state) {
        return mAlarmDao.loadAlarmsByState(state);
    }

    public LiveData<Alarm> getWaitingAlarm() {
        return mAlarmDao.loadSingleAlarmByState(Alarm.STATE_WAITING, Alarm.STATE_SNOOZING);
    }

    LiveData<Alarm> getActiveAlarm() {
        return mAlarmDao.loadSingleAlarmByState(Alarm.STATE_ACTIVE);
    }

    public long insert(Alarm alarm) {
        return mAlarmDao.insert(alarm);
        //new insertAsyncTask(mAlarmDao).execute(alarm);
    }

    public void delete(Alarm alarm) {
        new deleteAsyncTask(mAlarmDao).execute(alarm);
    }

    public void update(Alarm alarm) {
        new updateAsyncTask(mAlarmDao).execute(alarm);
    }

    private static class updateAsyncTask extends AsyncTask<Alarm, Void, Void> {
        private AlarmDao mAsyncTaskDao;

        updateAsyncTask(AlarmDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Alarm... params) {
            mAsyncTaskDao.updateAlarm(params[0]);
            return null;
        }
    }

    private static class insertAsyncTask extends AsyncTask<Alarm, Void, Void> {
        private AlarmDao mAsyncTaskDao;

        insertAsyncTask(AlarmDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Alarm... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Alarm, Void, Void> {
        private AlarmDao mAsyncTaskDao;

        deleteAsyncTask(AlarmDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Alarm... params) {
            mAsyncTaskDao.deleteAlarm(params[0]);
            return null;
        }
    }
}
