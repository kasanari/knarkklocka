package se.jakob.knarkklocka.workers




import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import se.jakob.knarkklocka.data.AlarmDatabase
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState
import java.util.*


class PopulateDatabaseWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams)
{

    override fun doWork(): Result {
        val database = AlarmDatabase.getInstance(applicationContext)
        database.alarmDao().deleteAll()
        var alarm = Alarm(AlarmState.STATE_DEAD, Date(500), Date(500))
        database.alarmDao().insert(alarm)
        alarm = Alarm(AlarmState.STATE_DEAD, Date(600), Date(600))
        database.alarmDao().insert(alarm)
        return Result.SUCCESS
    }
}