package se.jakob.knarkklocka

import android.app.AlarmManager
import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.util.Log
import android.view.View
import android.view.WindowManager.LayoutParams.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_alarm.*
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState
import se.jakob.knarkklocka.ui.ControllerFragment
import se.jakob.knarkklocka.utils.*
import se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID
import se.jakob.knarkklocka.utils.TimerUtils.getAlarmActionIntent
import se.jakob.knarkklocka.viewmodels.AlarmActivityViewModel


class AlarmActivity : AppCompatActivity(), ControllerFragment.OnControllerEventListener {

    private lateinit var viewModel: AlarmActivityViewModel

    private var alarmIsActive = false
    private var mServiceBound: Boolean = false

    private lateinit var timeOutClock: TimeOutClock

    private var currentAlarm: Alarm? = null

    // Animation
    private lateinit var animBlink : Animation

    private val timeoutLength = 5 * MINUTE_IN_MILLIS

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

        bindAlarmService()

        WakeLocker.acquire(this)

        keepScreenOn()

        /*Hide navigation and status bar*/
        hideUIElements()

        /*Get the ID of the Alarm that is going off*/
        val id = intent.getLongExtra(EXTRA_ALARM_ID, -1)

        /*Setup ViewModel and Observer*/
        val factory = InjectorUtils.provideAlarmActivityViewModelFactory(this, id)
        viewModel = ViewModelProviders.of(this, factory).get(AlarmActivityViewModel::class.java)

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


        /*Start timeout timer, so that alarm is not going off forever */
        timeOutClock = TimeOutClock(timeoutLength, alarmCallback)
        timeOutClock.start(this)



        viewModel.liveAlarm.observe(this, Observer {alarm : Alarm? ->
            if (alarm != null) {
                currentAlarm = alarm
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

        // load the animation
        animBlink = AnimationUtils.loadAnimation(this,
                R.anim.blink)

        // set animation listener
        //animBlink.setAnimationListener(this)

        tv_alarm_text.startAnimation(animBlink)

    }

    override fun onControllerEvent(v: View, event: String) {
        when (event) {
            ACTION_RESTART -> {
                AlarmBroadcasts.broadcastAlarmHandled(this)
                if (alarmIsActive) {
                    currentAlarm?.run {
                        dismiss(this)
                    }
                } else {
                    finish()
                }
            }
            ACTION_SNOOZE -> {
                AlarmBroadcasts.broadcastAlarmHandled(this)
                if (alarmIsActive) {
                    currentAlarm?.run {
                        snooze(this)
                    }
                } else {
                    finish()
                }
            }
            ACTION_SLEEP -> {
                AlarmBroadcasts.broadcastAlarmHandled(this)
                if (alarmIsActive) {
                    currentAlarm?.run {
                        sleep(this)
                    }
                } else {
                    finish()
                }
            }
        }
    }

    private fun setupChronometer(alarm: Alarm) {
        alarm_chronometer.visibility = View.VISIBLE
        val endTime = alarm.endTime
        val timeDelta = endTime.time - System.currentTimeMillis()
        alarm_chronometer.base = SystemClock.elapsedRealtime() + timeDelta
        alarm_chronometer.start()
    }

    /* This code has to be in onDestroy and not in onStop, otherwise the activity will end prematurely */
    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
        WakeLocker.release()
        unbindAlarmService()
    }


    private fun snooze(alarm: Alarm) {
        val intent = getAlarmActionIntent(this, ACTION_SNOOZE, alarm)
        AlarmIntentService.enqueueWork(this, intent)
        finish()
    }

    private fun dismiss(alarm: Alarm) {
        val intent = getAlarmActionIntent(this, ACTION_RESTART, alarm)
        AlarmIntentService.enqueueWork(this, intent)
        finish()
    }

    private fun sleep(alarm : Alarm) {
        val intent = getAlarmActionIntent(this, ACTION_SLEEP, alarm)
        AlarmIntentService.enqueueWork(this, intent)
        finish()
    }

    private fun keepScreenOn() {
        /*Ensure that screen turns on*/
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)    //Replaces FLAG_SHOW_WHEN_LOCKED
            setTurnScreenOn(true)      //Replaces FLAG_TURN_SCREEN_ON
            val keyguardManager = (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager)
            keyguardManager.requestDismissKeyguard(this, null)
            window.addFlags(
                    FLAG_KEEP_SCREEN_ON
                            or FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        } else {
            window.addFlags(
                    FLAG_SHOW_WHEN_LOCKED
                            or FLAG_TURN_SCREEN_ON
                            or FLAG_KEEP_SCREEN_ON
                            or FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                            or FLAG_DISMISS_KEYGUARD)
        }
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
        currentAlarm?.run {
            snooze(this)
        }
    }

    private fun startAlarm(alarm: Alarm) {
        setupChronometer(alarm)
        alarmIsActive = true
    }

    private fun stopAlarm() {
        timeOutClock.stop(this)
        alarmIsActive = false
        viewModel.liveAlarm.removeObservers(this)
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
