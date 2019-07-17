package se.jakob.knarkklocka.utils

import android.content.Context
import android.content.Intent


/**
 * Functions for sending broadcasts.
 */
object AlarmBroadcasts {

    fun broadcastStopAlarm(context: Context) {
        val intent = Intent().also { it.action = ACTION_STOP }
        context.sendBroadcast(intent)
    }

    fun broadcastAlarmHandled(context: Context) {
        val intent = Intent().also { it.action = SIGNAL_ALARM_HANDLED }
        context.sendBroadcast(intent)
    }
}
