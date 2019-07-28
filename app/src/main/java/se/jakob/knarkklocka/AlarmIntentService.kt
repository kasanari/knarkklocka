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
                    runBlocking {
                        repository.getAlarmByID(id)?.let { alarm ->
                            TimerUtils.cancelAlarm(applicationContext, alarm.id)
                            AlarmStateChanger.sleep(alarm, repository)
                            Log.d(TAG, "Received action to cancel alarm.")
                            AlarmBroadcasts.broadcastAlarmHandled(this@AlarmIntentService)
                        }
                    }
                }
                ACTION_SNOOZE -> {
                    runBlocking {
                        repository.getAlarmByID(id)?.let { alarm ->
                            TimerUtils.startSnoozeTimer(applicationContext, alarm)
                            Log.d(TAG, "Received action to snooze alarm.")
                            AlarmBroadcasts.broadcastAlarmHandled(this@AlarmIntentService)
                        }
                    }
                }
                ACTION_RESTART -> {
                    runBlocking {
                        repository.getAlarmByID(id)?.let { alarm ->
                            TimerUtils.cancelAlarm(applicationContext, alarm.id)
                            AlarmStateChanger.sleep(alarm, repository)
                        }
                        TimerUtils.startMainTimer(applicationContext)
                        Log.d(TAG, "Received action to restart alarm.")
                        AlarmBroadcasts.broadcastAlarmHandled(this@AlarmIntentService)
                    }
                }
                else -> {

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