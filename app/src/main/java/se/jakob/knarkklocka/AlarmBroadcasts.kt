package se.jakob.knarkklocka

import android.content.Context
import android.content.Intent

import se.jakob.knarkklocka.utils.ACTION_STOP_ALARM


object AlarmBroadcasts {

    fun broadcastStopAlarm(context: Context) {
        val intent = Intent().also { it.action = ACTION_STOP_ALARM }
        context.sendBroadcast(intent)
    }
}
