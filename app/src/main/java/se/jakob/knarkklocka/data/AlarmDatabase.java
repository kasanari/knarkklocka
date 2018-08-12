package se.jakob.knarkklocka.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Date;

import se.jakob.knarkklocka.BuildConfig;

@TypeConverters(DateConverter.class)
@Database(entities = {Alarm.class}, version = 1, exportSchema = false)
public abstract class AlarmDatabase extends RoomDatabase {
    private static final String LOG_TAG = AlarmDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static volatile AlarmDatabase INSTANCE; //Instance of the database

    public abstract AlarmDao alarmDao(); //Getter for Dao, use to access the database

    /*For debug purposes, populates the database with alarms*/
    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    public static AlarmDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    Log.d(LOG_TAG, "Creating new database instance");
                    if (BuildConfig.DEBUG) {
                        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                AlarmDatabase.class, "alarm_database_debug")
                                .build();
                    } else {
                        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                AlarmDatabase.class, "alarm_database")
                                .build();
                    }
                }
            }
        }
        return INSTANCE;
    }

    /*For debug purposes, populates the database with alarms*/
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private final AlarmDao mDao;
        PopulateDbAsync(AlarmDatabase db) {
            mDao = db.alarmDao();
        }
        @Override
        protected Void doInBackground(final Void... params) {
            mDao.deleteAll();
            Alarm alarm;
            alarm = new Alarm(Alarm.STATE_DEAD, new Date(500), new Date(500));
            mDao.insert(alarm);
            alarm = new Alarm(Alarm.STATE_DEAD, new Date(600), new Date(600));
            mDao.insert(alarm);
            return null;
        }
    }
}
