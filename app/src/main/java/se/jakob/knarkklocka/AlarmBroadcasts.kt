package se.jakob.knarkklocka

import android.content.Context
import android.content.Intent
import se.jakob.knarkklocka.utils.ACTION_ALARM_HANDLED

import se.jakob.knarkklocka.utils.ACTION_STOP_ALARM


object AlarmBroadcasts {

    fun broadcastStopAlarm(context: Context) {
        val intent = Intent().also { it.action = ACTION_STOP_ALARM }
        context.sendBroadcast(intent)
    }

    fun broadcastAlarmHandled(context: Context) {
        val intent = Intent().also { it.action = ACTION_ALARM_HANDLED }
        context.sendBroadcast(intent)
    }
}
