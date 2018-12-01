package se.jakob.knarkklocka

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.text.format.DateUtils.SECOND_IN_MILLIS
import android.util.Log
import android.view.View
import android.view.WindowManager.LayoutParams.*
import android.widget.Chronometer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_alarm.*
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState
import se.jakob.knarkklocka.utils.InjectorUtils
import se.jakob.knarkklocka.utils.TimerUtils
import se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID
import se.jakob.knarkklocka.utils.WakeLocker
import se.jakob.knarkklocka.viewmodels.AlarmActivityViewModel


class AlarmActivity : AppCompatActivity() {

    private lateinit var viewModel: AlarmActivityViewModel

    private var alarmIsActive = false
    private var alarmIsHandled = false
    private var mServiceBound: Boolean = false

    private val alarmCallback = AlarmManager.OnAlarmListener {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Timeout reached. Snoozing...")
        }
        finish()
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Finished binding to AlarmService")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Disconnected from AlarmService")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "AlarmActivity started")
        setContentView(R.layout.activity_alarm)

        bindAlarmService()

        WakeLocker.acquire(this)

        /*Ensure that screen turns on*/
        val win = window
        if (Build.VERSION.SDK_INT >= 27) {
            setTurnScreenOn(true)      //Replaces FLAG_TURN_SCREEN_ON
            setShowWhenLocked(true)    //Replaces FLAG_SHOW_WHEN_LOCKED
            win.addFlags(
                    FLAG_KEEP_SCREEN_ON or
                            FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        } else {
            win.addFlags(
                    FLAG_SHOW_WHEN_LOCKED or
                            FLAG_TURN_SCREEN_ON or
                            FLAG_KEEP_SCREEN_ON or
                            FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        }

        /*Hide navigation and status bar*/
        hideUIElements()

        setContentView(R.layout.activity_alarm)

        /* Close dialogs and window shade, so this is fully visible */
        sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))

        if (BuildConfig.DEBUG) {
            button_miss_alarm.visibility = View.VISIBLE
            button_miss_alarm.setOnClickListener {
                finish()
            }
        } else {
            button_miss_alarm.visibility = View.INVISIBLE
        }

        button_snooze_alarm.setOnClickListener {
            alarmIsHandled = true
            if (alarmIsActive) {
                snooze()
            } else {
                finish()
            }
        }
        button_dismiss_alarm.setOnLongClickListener {
            alarmIsHandled = true
            if (alarmIsActive) {
                dismiss()
            } else {
                finish()
            }
            true
        }

        /*Start timeout timer, so that alarm is not going off forever */
        startTimeoutClock()

        /*Get the ID of the Alarm that is going off*/
        val id = intent.getLongExtra(EXTRA_ALARM_ID, -1)

        /*Setup ViewModel and Observer*/
        val factory = InjectorUtils.provideAlarmActivityViewModelFactory(this, id)
        viewModel = ViewModelProviders.of(this, factory).get(AlarmActivityViewModel::class.java)

        viewModel.liveAlarm.observe(this, Observer {alarm : Alarm? ->
            if (alarm != null) {
                if (alarm.state == AlarmState.STATE_ACTIVE) {
                    startAlarm(alarm)
                } else {
                    finish()
                }
            } else {
                alarm_chronometer.visibility = View.INVISIBLE
                tv_alarm_text.visibility = View.INVISIBLE
            }
        })

    }

    private fun setupChronometer(alarm: Alarm) {
        alarm_chronometer.visibility = View.VISIBLE
        tv_alarm_text.visibility = View.VISIBLE
        val endTime = alarm.endTime
        val timeDelta = endTime.time - System.currentTimeMillis()
        alarm_chronometer.base = SystemClock.elapsedRealtime() + timeDelta
        alarm_chronometer.onChronometerTickListener = Chronometer.OnChronometerTickListener {
            val onColor = ContextCompat.getColor(application, R.color.colorAccent)
            val offColor = ContextCompat.getColor(application, android.R.color.darker_gray)
            val currentColor = tv_alarm_text.currentTextColor
            if (currentColor == onColor) {
                tv_alarm_text.setTextColor(offColor)
            } else {
                tv_alarm_text.setTextColor(onColor)
            }
        }
        alarm_chronometer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (alarmIsHandled) {
            AlarmBroadcasts.broadcastAlarmHandled(this)
            stopAlarm()
            WakeLocker.release()
        } else {
            miss()
        }
        unbindAlarmService()
    }

    private fun miss() {
        viewModel.miss()
    }

    private fun snooze() {
        viewModel.getCurrentAlarm()?.let { alarm ->
            TimerUtils.startSnoozeTimer(this, alarm)
        }
        finish()
    }

    private fun dismiss() {
        viewModel.kill()
        TimerUtils.startMainTimer(this)
        finish()
    }

    private fun hideUIElements() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        supportActionBar?.hide()
    }

    override fun onBackPressed() {
        snooze()
    }

    private fun startAlarm(alarm: Alarm) {
        setupChronometer(alarm)
        alarmIsActive = true
    }

    private fun stopAlarm() {
        stopTimeoutClock()
        alarmIsActive = false
        viewModel.liveAlarm.removeObservers(this)
        AlarmBroadcasts.broadcastStopAlarm(this)
    }

    private fun startTimeoutClock() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        val timeout = if (BuildConfig.DEBUG) {
            10 * SECOND_IN_MILLIS
        } else {
            2 * MINUTE_IN_MILLIS
        }

        alarmManager.setExact(RTC_WAKEUP, System.currentTimeMillis() + timeout, "tag", alarmCallback, null)
    }

    private fun stopTimeoutClock() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        alarmManager.cancel(alarmCallback)
    }

    /**
     * Bind AlarmService if not yet bound.
     */
    private fun bindAlarmService() {
        if (!mServiceBound) {
            val intent = Intent(this, AlarmService::class.java)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
            mServiceBound = true
        }
    }

    /**
     * Unbind AlarmService if bound.
     */
    private fun unbindAlarmService() {
        if (mServiceBound) {
            unbindService(connection)
            mServiceBound = false
        }
    }

    companion object {
        private const val TAG = "AlarmActivity"
    }
}
