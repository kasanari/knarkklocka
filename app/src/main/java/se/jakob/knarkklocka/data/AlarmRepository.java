package se.jakob.knarkklocka.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class AlarmRepository {
    private AlarmDao mAlarmDao;
    private LiveData<List<Alarm>> mAllAlarms;

    AlarmRepository(Application application) {
        AlarmDatabase db = AlarmDatabase.getDatabase(application);
        mAlarmDao = db.alarmDao();
        mAllAlarms = mAlarmDao.loadAllAlarms();
    }

    LiveData<List<Alarm>> getAllAlarms() {
        return mAllAlarms;
    }

    LiveData<Alarm> getAlarmsByState(int state) {
        return mAlarmDao.loadAlarmsByState(state);
    }

    public long insert(Alarm alarm) {
        return mAlarmDao.insert(alarm);
        //new insertAsyncTask(mAlarmDao).execute(alarm);
    }

    public void delete(Alarm alarm) {
        new deleteAsyncTask(mAlarmDao).execute(alarm);
    }

    public void update(Alarm alarm) {
        mAlarmDao.updateAlarm(alarm);
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
