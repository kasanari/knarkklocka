package se.jakob.knarkklocka.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

//Class to allow interaction of database

@Dao
public interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Alarm alarm);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAlarm(Alarm alarm);

    @Delete
    void deleteAlarm(Alarm alarm);

    @Query("DELETE FROM alarm_table")
    void deleteAll();

    @Query("SELECT * FROM alarm_table ORDER BY end_time")
    LiveData<List<Alarm>> loadAllAlarms();

    @Query("SELECT * FROM alarm_table WHERE id = :id")
    LiveData<Alarm> loadLiveAlarmById(long id);

    @Query("SELECT * FROM alarm_table WHERE id = :id")
    Alarm loadAlarmById(long id);

    @Query("SELECT * FROM alarm_table WHERE alarm_state = :state")
    LiveData<Alarm> loadAlarmsByState(int state);

    @Query("SELECT * FROM alarm_table WHERE alarm_state IN (:states) LIMIT 1")
    LiveData<Alarm> loadSingleAlarmByState(int... states);
}
