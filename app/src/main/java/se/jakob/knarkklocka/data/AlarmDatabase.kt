package se.jakob.knarkklocka.data

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import android.os.AsyncTask
import android.util.Log

import java.util.Date

import se.jakob.knarkklocka.BuildConfig

@TypeConverters(DateConverter::class)
@Database(entities = arrayOf(Alarm::class), version = 1, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao  //Getter for Dao, use to access the database

    /*For debug purposes, populates the database with alarms*/
    private class PopulateDbAsync internal constructor(db: AlarmDatabase) : AsyncTask<Void, Void, Void>() {
        private val mDao: AlarmDao

        init {
            mDao = db.alarmDao()
        }

        override fun doInBackground(vararg params: Void): Void? {
            mDao.deleteAll()
            var alarm: Alarm
            alarm = Alarm(Alarm.STATE_DEAD, Date(500), Date(500))
            mDao.insert(alarm)
            alarm = Alarm(Alarm.STATE_DEAD, Date(600), Date(600))
            mDao.insert(alarm)
            return null
        }
    }

    companion object {

        private val LOG_TAG = AlarmDatabase::class.java.simpleName
        private val LOCK = Any()
        @Volatile
        private var INSTANCE: AlarmDatabase? = null //Instance of the database

        /*For debug purposes, populates the database with alarms*/
        private val sRoomDatabaseCallback = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                PopulateDbAsync(INSTANCE!!).execute()
            }
        }

        fun getDatabase(context: Context): AlarmDatabase? {
            if (INSTANCE == null) {
                synchronized(LOCK) {
                    if (INSTANCE == null) {
                        Log.d(LOG_TAG, "Creating new database instance")
                        if (BuildConfig.DEBUG) {
                            INSTANCE = Room.databaseBuilder(context.applicationContext,
                                    AlarmDatabase::class.java, "alarm_database_debug")
                                    .addCallback(sRoomDatabaseCallback)
                                    .build()
                        } else {
                            INSTANCE = Room.databaseBuilder(context.applicationContext,
                                    AlarmDatabase::class.java, "alarm_database")
                                    .build()
                        }
                    }
                }
            }
            return INSTANCE
        }
    }
}
