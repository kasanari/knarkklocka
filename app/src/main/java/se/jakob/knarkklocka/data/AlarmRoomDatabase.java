package se.jakob.knarkklocka.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.util.Log;

@TypeConverters(DateConverter.class)
@Database(entities = {Alarm.class}, version = 1)
public abstract class AlarmRoomDatabase extends RoomDatabase {
    private static final String LOG_TAG = AlarmRoomDatabase.class.getSimpleName();
    private static AlarmRoomDatabase INSTANCE;

    public static AlarmRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AlarmRoomDatabase.class) {
                if (INSTANCE == null) {
                    Log.d(LOG_TAG, "Creating new database instance");
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AlarmRoomDatabase.class, "alarm_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract AlarmDao alarmDao();

}
