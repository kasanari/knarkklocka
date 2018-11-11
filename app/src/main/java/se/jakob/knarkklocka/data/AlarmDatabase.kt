package se.jakob.knarkklocka.data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import se.jakob.knarkklocka.BuildConfig
import se.jakob.knarkklocka.workers.PopulateDatabaseWorker

@TypeConverters(Converters::class)
@Database(entities = [Alarm::class], version = 4, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao  //Getter for Dao, use to access the database

    companion object {

        @Volatile private var instance: AlarmDatabase? = null //Instance of the database

        fun getInstance(context: Context): AlarmDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        /*For debug purposes, populates the database with alarms*/
        private val databaseDebugCallback = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                val request = OneTimeWorkRequestBuilder<PopulateDatabaseWorker>().build()
                WorkManager.getInstance().enqueue(request)
            }
        }

        private fun buildDatabase(context: Context): AlarmDatabase {
            if (BuildConfig.DEBUG) {
                return Room.databaseBuilder(context, AlarmDatabase::class.java,
                        "alarm_database_debug")
                        .addCallback(databaseDebugCallback)
                        .fallbackToDestructiveMigration()
                        .build()
            } else {
                return Room.databaseBuilder(context,
                        AlarmDatabase::class.java, "alarm_database")
                        .fallbackToDestructiveMigration()
                        .build()
            }
        }

    }
}
