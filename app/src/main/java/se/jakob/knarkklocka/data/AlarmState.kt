package se.jakob.knarkklocka.data

enum class AlarmState {
    /**
     * The default state of an [Alarm], in which it is waiting for the end time.
     */
    STATE_WAITING,
    /**
     * A [Alarm] which has been active or snoozed and then dismissed.
     */
    STATE_DEAD,
    /**
     * The state in which an [Alarm] has reached its end time and is firing.
     */
    STATE_ACTIVE,
    /**
     * The state of an [Alarm] which has been active and was delayed for a short snooze time.
     */
    STATE_SNOOZING,
    /**
     * The erroneous state of an [Alarm] which has been active but was not handled properly.
     */
    STATE_MISSED
}