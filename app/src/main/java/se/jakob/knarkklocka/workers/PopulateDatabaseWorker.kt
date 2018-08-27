package se.jakob.knarkklocka.workers

import androidx.work.Worker
import se.jakob.knarkklocka.data.AlarmDatabase
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState
import java.util.*


class PopulateDatabaseWorker : Worker() {

    override fun doWork(): Result {
        val database = AlarmDatabase.getInstance(applicationContext)
        database.alarmDao().deleteAll()
        var alarm = Alarm(AlarmState.STATE_DEAD, Date(500), Date(500))
        database.alarmDao().insert(alarm)
        alarm = Alarm(AlarmState.STATE_DEAD, Date(600), Date(600))
        database.alarmDao().insert(alarm)
        return Worker.Result.SUCCESS
    }
}