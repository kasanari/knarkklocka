package se.jakob.knarkklocka

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.utils.AlarmNotificationsUtils
import se.jakob.knarkklocka.utils.InjectorUtils
import se.jakob.knarkklocka.utils.TimerUtils
import se.jakob.knarkklocka.utils.WakeLocker

class AlarmBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        Log.i(TAG, "AlarmBootReceiver $action")

        WakeLocker.acquire(context!!)

        if (ACTION_BOOT_COMPLETED == action) {
            GlobalScope.launch {
                val repository = InjectorUtils.getAlarmRepository(context)
                val alarm : Alarm = repository.currentAlarm
                if (alarm.waiting or alarm.snoozing) {
                    TimerUtils.setNewAlarm(context, alarm.id, alarm.endTime)
                    if (alarm.snoozing) {
                        AlarmNotificationsUtils.showSnoozingAlarmNotification(context, alarm)
                    } else {
                        AlarmNotificationsUtils.showWaitingAlarmNotification(context, alarm)
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "AlarmBootReceiver"
        const val ACTION_BOOT_COMPLETED = Intent.ACTION_LOCKED_BOOT_COMPLETED
    }

}