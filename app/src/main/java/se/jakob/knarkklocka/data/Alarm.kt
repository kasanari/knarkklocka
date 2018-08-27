package se.jakob.knarkklocka.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "alarm_table")
data class Alarm constructor(
        @PrimaryKey(autoGenerate = true) val id: Long,
        @ColumnInfo(name = "alarm_state") var state: AlarmState,
        @ColumnInfo(name = "start_time") var startTime: Date,
        @ColumnInfo(name = "end_time") var endTime: Date,
        @ColumnInfo(name = "number_of_snoozes") var snoozes: Int = 0
) {

    @Ignore
    constructor(state: AlarmState, startTime: Date, endTime: Date) : this(0, state, startTime, endTime)

    val stateToString: String
        get() {
            return when (this.state) {
                AlarmState.STATE_ACTIVE -> "Active"
                AlarmState.STATE_WAITING -> "Waiting"
                AlarmState.STATE_DEAD -> "Dead"
                AlarmState.STATE_SNOOZING -> "Snoozed"
            }
        }

    fun incrementSnoozes() {
        this.snoozes += 1
    }

    override fun toString(): String {
        return String.format(Locale.getDefault(), "Alarm \n id: %d \n startTime: %s \n endTime: %s \n state: %s",
                id, startTime.toString(), endTime.toString(), stateToString)
    }


}
