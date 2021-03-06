package se.jakob.knarkklocka.data


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import se.jakob.knarkklocka.BuildConfig
import java.lang.Exception
import java.util.*

@Entity(tableName = "alarm_table")
data class Alarm constructor(
        @PrimaryKey(autoGenerate = true) var id: Long,
        @ColumnInfo(name = "alarm_state") var state: AlarmState = AlarmState.STATE_WAITING,
        @ColumnInfo(name = "start_time") var startTime: Date,
        @ColumnInfo(name = "end_time") var endTime: Date,
        @ColumnInfo(name = "number_of_snoozes") var snoozes: Int = 0
) {

    val waiting : Boolean
    get () {
        return state == AlarmState.STATE_WAITING
    }
    val active : Boolean
        get () {
            return state == AlarmState.STATE_ACTIVE
        }
    val snoozing : Boolean
        get () {
            return state == AlarmState.STATE_SNOOZING
        }
    val dead : Boolean
        get () {
            return state == AlarmState.STATE_DEAD
        }
    val missed : Boolean
        get() {
            return state == AlarmState.STATE_MISSED
        }

    @Ignore
    constructor(state: AlarmState, startTime: Date, endTime: Date) : this(id = 0, state = state, startTime = startTime, endTime = endTime)

    @Ignore
    constructor(startTime: Date, endTime: Date) : this(id = 0, startTime=startTime, endTime=endTime)

    val stateToString: String
        get() {
            return when (this.state) {
                AlarmState.STATE_ACTIVE -> "Active"
                AlarmState.STATE_WAITING -> "Waiting"
                AlarmState.STATE_DEAD -> "Dead"
                AlarmState.STATE_SNOOZING -> "Snoozed"
                AlarmState.STATE_MISSED -> "Missed"
            }
        }

    fun activate() {
        if (dead or active) {
            if (BuildConfig.DEBUG) {
                throw InvalidStateChangeException("Activating a dead or active Alarm is not allowed.")
            }
        } else {
            this.state = AlarmState.STATE_ACTIVE
        }
    }

    fun snooze(newEndTime: Date) {
        if (!(active or missed)) {
            if (BuildConfig.DEBUG) {
                throw InvalidStateChangeException("Only an active or missed Alarm can be snoozed.")
            }
        } else {
            state = AlarmState.STATE_SNOOZING
            incrementSnoozeCount()
            endTime = newEndTime
        }
    }

    fun kill() {
        this.endTime = Calendar.getInstance().time
        this.state = AlarmState.STATE_DEAD
    }

    fun miss() {
        if (!(active or snoozing)) {
            if (BuildConfig.DEBUG) {
                throw InvalidStateChangeException("Only an active Alarm can be missed.")
            }
        } else {
            this.state = AlarmState.STATE_MISSED
        }
    }

    private fun incrementSnoozeCount() {
        this.snoozes += 1
    }

    override fun toString(): String {
        return String.format(Locale.getDefault(), "Alarm \n id: %d \n startTime: %s \n endTime: %s \n state: %s",
                id, startTime.toString(), endTime.toString(), stateToString)
    }

    class InvalidStateChangeException(message:String) : Exception(message)
}

