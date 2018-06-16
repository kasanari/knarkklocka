package se.jakob.knarkklocka.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class AlarmRepository {
    private AlarmDao mAlarmDao;
    private LiveData<List<Alarm>> mAllAlarms;

    AlarmRepository(Application application) {
        AlarmRoomDatabase db = AlarmRoomDatabase.getDatabase(application);
        mAlarmDao = db.alarmDao();
        mAllAlarms = mAlarmDao.loadAllAlarms();
    }

    LiveData<List<Alarm>> getAllAlarms() {
        return mAllAlarms;
    }

    public void insert(Alarm alarm) {
        new insertAsyncTask(mAlarmDao).execute(alarm);
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
}
