package se.jakob.knarkklocka.utils

import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmRepository
import java.util.*

/** This object provides functions to change the states of Alarms in a consistent way,
 * ensuring that the database is updated. **/
object AlarmStateChanger {

    suspend fun sleep(alarm: Alarm, repository: AlarmRepository) : Boolean {
        return when {
            alarm.active or alarm.snoozing or alarm.missed -> {
                kill(alarm, repository)
            }
            alarm.waiting -> repository.safeDelete(alarm)
            else -> false
        }
    }

    suspend fun kill(alarm: Alarm, repository: AlarmRepository) : Boolean {
        alarm.kill()
        return repository.safeUpdate(alarm)
    }

    suspend fun miss(alarm: Alarm, repository: AlarmRepository) : Boolean {
        alarm.miss()
        return repository.safeUpdate(alarm)
    }

    suspend fun snooze(alarm: Alarm, endTime: Date, repository: AlarmRepository) : Boolean {
        alarm.snooze(endTime)
        return repository.safeUpdate(alarm)
    }

    suspend fun activate(alarm: Alarm, repository: AlarmRepository) : Boolean {
        alarm.activate()
        return repository.safeUpdate(alarm)
    }
}
