package se.jakob.knarkklocka

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import kotlinx.coroutines.runBlocking
import se.jakob.knarkklocka.utils.*

class AlarmIntentService : JobIntentService() {

    /**
     * Convenience method for enqueuing work in to this service.
     */

    override fun onDestroy() {
        Log.d(TAG, "All work complete.")
        super.onDestroy()
    }

    override fun onHandleWork(intent: Intent) {
        val id = intent.getLongExtra(TimerUtils.EXTRA_ALARM_ID, -1)
        val repository = InjectorUtils.getAlarmRepository(this)
        intent.action?.let { action ->
            when (action) {
                ACTION_SLEEP -> {
                    Log.d(TAG, "Received action to cancel alarm.")
                    runBlocking {
                        repository.getAlarmByID(id)?.let { alarm ->
                            TimerUtils.cancelAlarm(applicationContext, alarm.id)
                            AlarmStateChanger.sleep(alarm, repository)
                            AlarmBroadcasts.broadcastAlarmHandled(this@AlarmIntentService)
                        }
                    }
                }
                ACTION_SNOOZE -> {
                    Log.d(TAG, "Received action to snooze alarm.")
                    runBlocking {
                        repository.getAlarmByID(id)?.let { alarm ->
                            TimerUtils.startSnoozeTimer(applicationContext, alarm)
                            AlarmBroadcasts.broadcastAlarmHandled(this@AlarmIntentService)
                        }
                    }
                }
                ACTION_RESTART -> {
                    Log.d(TAG, "Received action to restart alarm.")
                    runBlocking {
                        repository.getAlarmByID(id)?.let { alarm ->
                            TimerUtils.cancelAlarm(applicationContext, alarm.id)
                            AlarmStateChanger.sleep(alarm, repository)
                        }
                        TimerUtils.startMainTimer(applicationContext)
                        AlarmBroadcasts.broadcastAlarmHandled(this@AlarmIntentService)
                    }
                }
                else -> {
                    Log.e(TAG, "Received invalid action.")
                }
            }
        }
    }

    companion object {
        private const val TAG = "AlarmIntentService"
        private const val JOB_ID = 1000

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, AlarmIntentService::class.java, JOB_ID, work)
        }

    }
}