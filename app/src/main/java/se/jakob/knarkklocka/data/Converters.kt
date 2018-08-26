package se.jakob.knarkklocka.data

import android.arch.persistence.room.TypeConverter

import java.util.Date

class Converters {

    @TypeConverter
     fun toDate(timestamp: Long?): Date? {
        return if (timestamp == null) null else Date(timestamp)
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromAlarmState(state : AlarmState) : Int {
        return when (state) {
            AlarmState.STATE_WAITING -> 0
            AlarmState.STATE_DEAD -> 1
            AlarmState.STATE_ACTIVE -> 2
            AlarmState.STATE_SNOOZING -> 3
        }
    }

    @TypeConverter
    fun toAlarmState(state : Int) : AlarmState {
        return when (state) {
            0 -> AlarmState.STATE_WAITING
            1 -> AlarmState.STATE_DEAD
            2 -> AlarmState.STATE_ACTIVE
            3 -> AlarmState.STATE_SNOOZING
            else -> {
                throw Exception("Invalid AlarmState")
            }
        }
    }
}