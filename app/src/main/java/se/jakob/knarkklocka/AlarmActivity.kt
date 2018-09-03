/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.jakob.knarkklocka

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.text.format.DateUtils.SECOND_IN_MILLIS
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Chronometer
import kotlinx.android.synthetic.main.activity_alarm.*
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState
import se.jakob.knarkklocka.utils.ACTION_STOP_ALARM
import se.jakob.knarkklocka.utils.InjectorUtils
import se.jakob.knarkklocka.utils.TimerUtils
import se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID
import se.jakob.knarkklocka.utils.WakeLocker
import se.jakob.knarkklocka.viewmodels.AlarmActivityViewModel

class AlarmActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {

    private val TAG = "AlarmActivity"
    private lateinit var alarmActivityViewModel: AlarmActivityViewModel

    private var alarmIsActive = false

    private var currentAlarm: Alarm? = null

    private val alarmCallback = AlarmManager.OnAlarmListener {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Timeout reached. Snoozing...")
        }
        snooze()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "AlarmActivity started")
        setContentView(R.layout.activity_alarm)

        WakeLocker.acquire(this)

        /*Ensure that screen turns on*/
        val win = window
        if (Build.VERSION.SDK_INT >= 27) {
            setTurnScreenOn(true)      //Replaces FLAG_TURN_SCREEN_ON
            setShowWhenLocked(true)    //Replaces FLAG_SHOW_WHEN_LOCKED
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        } else {
            win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        }

        /*Hide navigation bar*/
        hideNavigationBar()

        /* Close dialogs and window shade, so this is fully visible */
        sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))


        button_snooze_alarm.setOnClickListener(this)
        button_dismiss_alarm.setOnLongClickListener(this)

        /*Start timeout timer, so that alarm is not going off forever */
        startTimeoutClock()

        /*Get the ID of the Alarm that is going off*/
        val intent = intent
        val id = intent.getLongExtra(EXTRA_ALARM_ID, -1)

        /*Setup ViewModel and Observer*/
        val factory = InjectorUtils.provideAlarmActivityViewModelFactory(this, id)
        alarmActivityViewModel = ViewModelProviders.of(this, factory).get(AlarmActivityViewModel::class.java)

        alarmActivityViewModel.liveAlarm.observe(this, Observer { alarm ->
            if (alarm != null) {
                val state = alarm.state
                when (state) {
                    AlarmState.STATE_ACTIVE -> {
                        currentAlarm = alarm
                        startAlarm(alarm)
                    }
                    AlarmState.STATE_DEAD -> finish()
                    AlarmState.STATE_SNOOZING -> finish()
                    AlarmState.STATE_WAITING -> finish()
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
        if (alarmIsActive) {
            snooze()
        }
    }

    private fun snooze() {
        if (alarmIsActive) {
            TimerUtils.startSnoozeTimer(this, alarmActivityViewModel)

            alarmIsActive = false
            alarmActivityViewModel.liveAlarm.removeObservers(this)

            stopAlarm()
        }
        finish()
    }

    private fun dismiss() {
        if (alarmIsActive) {
            alarmActivityViewModel.kill()
            TimerUtils.startMainTimer(this, alarmActivityViewModel)
            stopAlarm()
        }
        finish()
    }

    private fun hideNavigationBar() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onClick(view: View) {
        if (view === button_snooze_alarm) {
            snooze()
        } else if (view === button_dismiss_alarm) {
            dismiss()
        }

    }

    private fun startAlarm(alarm: Alarm?) {
        setupChronometer(alarm!!)
        alarmIsActive = true
    }

    private fun stopAlarm() {
        stopTimeoutClock()
        alarmIsActive = false
        alarmActivityViewModel.liveAlarm.removeObservers(this)
        val alarmIntent = Intent(this, AlarmService::class.java)
        alarmIntent.putExtra(EXTRA_ALARM_ID, currentAlarm!!.id)
        alarmIntent.action = ACTION_STOP_ALARM
        startService(alarmIntent)
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

    override fun onLongClick(v: View): Boolean {
        if (v === button_dismiss_alarm) {
            dismiss()
        }
        return true
    }
}
