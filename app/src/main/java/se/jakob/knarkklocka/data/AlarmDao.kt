package se.jakob.knarkklocka.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

//Class to allow interaction of database

@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(alarm: Alarm): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(plants: List<Alarm>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAlarm(alarm: Alarm)

    @Delete
    fun deleteAlarm(alarm: Alarm)

    @Query("DELETE FROM alarm_table")
    fun deleteAll()

    @Query("SELECT * FROM alarm_table ORDER BY start_time DESC, end_time DESC")
    fun getAllAlarms(): LiveData<List<Alarm>>

    @Query("SELECT * FROM alarm_table ORDER BY start_time DESC, end_time DESC LIMIT 1")
    fun getMostRecentAlarm(): LiveData<Alarm>

    @Query("SELECT * FROM alarm_table WHERE id = :id")
    fun getLiveAlarm(id: Long): LiveData<Alarm>

    @Query("SELECT * FROM alarm_table WHERE id = :id")
    fun getAlarm(id: Long): Alarm

    @Query("SELECT * FROM alarm_table  WHERE alarm_state IN (:states) ORDER BY start_time DESC, end_time DESC")
    fun getAlarmsByStates(vararg states: Int): LiveData<List<Alarm>>

    @Query("SELECT * FROM alarm_table WHERE alarm_state IN (:states) LIMIT 1")
    fun getAlarmByState(vararg states: Int): LiveData<Alarm>
}
