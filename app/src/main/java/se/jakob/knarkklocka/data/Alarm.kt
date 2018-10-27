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

    var waiting : Boolean = true
    get () {
        return state == AlarmState.STATE_WAITING
    }
    var active : Boolean = false
        get () {
            return state == AlarmState.STATE_ACTIVE
        }
    var snoozing : Boolean = false
        get () {
            return state == AlarmState.STATE_SNOOZING
        }
    var dead : Boolean = false
        get () {
            return state == AlarmState.STATE_DEAD
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
            }
        }

    fun activate() {
        if (dead) {
            if (BuildConfig.DEBUG) {
                throw InvalidStateChangeException("Activating a dead Alarm is not allowed.")
            }
        } else {
            this.state = AlarmState.STATE_ACTIVE
        }
    }

    fun snooze(newEndTime: Date) {
        if (!active) {
            if (BuildConfig.DEBUG) {
                throw InvalidStateChangeException("Only an active Alarm can be snoozed.")
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

    private fun incrementSnoozeCount() {
        this.snoozes += 1
    }

    override fun toString(): String {
        return String.format(Locale.getDefault(), "Alarm \n id: %d \n startTime: %s \n endTime: %s \n state: %s",
                id, startTime.toString(), endTime.toString(), stateToString)
    }

    class InvalidStateChangeException(message:String) : Exception(message)
}

