package se.jakob.knarkklocka

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.run{
            intent?.run {
                AlarmIntentService.enqueueWork(context, intent)
            }
        }
    }
}