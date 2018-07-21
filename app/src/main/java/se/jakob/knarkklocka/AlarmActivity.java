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
package se.jakob.knarkklocka;

import android.app.AlarmManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import se.jakob.knarkklocka.data.Alarm;
import se.jakob.knarkklocka.data.AlarmActivityViewModel;
import se.jakob.knarkklocka.utils.TimerUtils;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static se.jakob.knarkklocka.data.Alarm.STATE_ACTIVE;
import static se.jakob.knarkklocka.data.Alarm.STATE_DEAD;
import static se.jakob.knarkklocka.data.Alarm.STATE_SNOOZING;
import static se.jakob.knarkklocka.data.Alarm.STATE_WAITING;

public class AlarmActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "AlarmActivity";

    private Button mSnoozeButton;
    private Button mDismissButton;
    private TextView tv_alarm_text;

    private AlarmActivityViewModel alarmActivityViewModel;

    private Chronometer mAlarmChronometer;

    private boolean alarmIsActive = false;

    private AlarmManager.OnAlarmListener alarmCallback = new AlarmManager.OnAlarmListener() {
        @Override
        public void onAlarm() {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Timeout reached. Snoozing...");
            }
            snooze();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AlarmActivity started");
        setContentView(R.layout.activity_alarm);

        /* Turn on vibration */
        Klaxon.vibrateAlarm(this);

        /*Ensure screen turns on*/
        final Window win = getWindow();
        int flags;
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true); //Replaces FLAG_SHOW_WHEN_LOCKED
            setTurnScreenOn(true); //Replaces FLAG_TURN_SCREEN_ON
            flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
        } else {
            flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
        }
        win.addFlags(flags);

        /*Hide navigation bar*/
        hideNavigationBar();

        // Close dialogs and window shade, so this is fully visible
        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        mDismissButton = findViewById(R.id.dismiss);
        mSnoozeButton = findViewById(R.id.snooze);
        tv_alarm_text = findViewById(R.id.tv_alarm_text);

        mSnoozeButton.setOnClickListener(this);
        mDismissButton.setOnLongClickListener(this);

        mAlarmChronometer = findViewById(R.id.alarm_chronometer);
        AlarmManager alarmManager = getSystemService(AlarmManager.class);
        long timeout;
        if (BuildConfig.DEBUG) {
            timeout = 10 * SECOND_IN_MILLIS;
        } else {
            timeout = 2 * MINUTE_IN_MILLIS;
        }

        if (alarmManager != null) {
            alarmManager.setExact(RTC_WAKEUP, System.currentTimeMillis() + timeout, "tag", alarmCallback, null);
        }
        mAlarmChronometer.setVisibility(View.VISIBLE);
        /*Setup ViewModel and Observer*/
        alarmActivityViewModel = ViewModelProviders.of(this).get(AlarmActivityViewModel.class);
        alarmActivityViewModel.getAlarm().observe(this, new Observer<Alarm>() {
            @Override
            public void onChanged(@Nullable final Alarm alarm) {
                if (alarm != null) {
                    int state = alarm.getState();
                    switch (state) {
                        case STATE_ACTIVE:
                            setupChronometer(alarm);
                            alarmIsActive = true;
                            break;
                        case STATE_DEAD:
                            finish();
                            break;
                        case STATE_SNOOZING:
                            finish();
                            break;
                        case STATE_WAITING:
                            finish();
                            break;
                    }
                } else {
                    mAlarmChronometer.setVisibility(View.INVISIBLE);
                    tv_alarm_text.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void setupChronometer(Alarm alarm) {
        mAlarmChronometer.setVisibility(View.VISIBLE);
        tv_alarm_text.setVisibility(View.VISIBLE);
        Date endTime = alarm.getEndTime();
        long timeDelta = endTime.getTime() - System.currentTimeMillis();
        mAlarmChronometer.setBase(SystemClock.elapsedRealtime() + timeDelta);
        mAlarmChronometer.setOnChronometerTickListener(
                new Chronometer.OnChronometerTickListener() {
                    public void onChronometerTick(Chronometer chronometer) {
                        int onColor = ContextCompat.getColor(getApplication(), R.color.colorAccent);
                        int offColor = ContextCompat.getColor(getApplication(), android.R.color.darker_gray);
                        int currentColor = tv_alarm_text.getCurrentTextColor();
                        if (currentColor == onColor) {
                            tv_alarm_text.setTextColor(offColor);
                        } else {
                            tv_alarm_text.setTextColor(onColor);
                        }
                    }
                }
        );
        mAlarmChronometer.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (alarmIsActive) {
            snooze();
        }
    }

    public void snooze() {
        if (isAlarmRunning()) {
            Klaxon.stopVibrate(this);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    Alarm currentAlarm = alarmActivityViewModel.getAlarm().getValue();
                    Calendar snoozeTime = Calendar.getInstance();
                    snoozeTime.add(Calendar.SECOND, 5);
                    if (currentAlarm != null) {
                        alarmActivityViewModel.snooze(snoozeTime.getTime());
                        TimerUtils.setNewAlarm(getApplicationContext(), currentAlarm.getId(), snoozeTime.getTime());
                        alarmIsActive = false;
                        finish();
                    }
                }
            });
        } else {
            finish();
        }
    }

    private void dismiss() {
        if (isAlarmRunning()) {
            Klaxon.stopVibrate(this);
            long timer_duration = PreferenceUtils.getMainTimerLength(this);
            Calendar currentTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            endTime.add(Calendar.MILLISECOND, (int)timer_duration);
            final Alarm alarm = new Alarm(Alarm.STATE_WAITING, currentTime.getTime(), endTime.getTime());
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    alarmActivityViewModel.kill();
                    long id = alarmActivityViewModel.insert(alarm);
                    TimerUtils.setNewAlarm(getApplicationContext(), id, alarm.getEndTime());
                    alarmIsActive = false;
                    finish();
                }
            });
        } else {
            alarmIsActive = false;
            finish();
        }
    }

    public boolean isAlarmRunning() {
        return alarmActivityViewModel.isAlarmRunning();
    }

    private void hideNavigationBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onClick(View view) {
        if (view == mSnoozeButton) {
            snooze();
        } else if (view == mDismissButton) {
            dismiss();
        }

    }

    @Override
    public boolean onLongClick(View v) {
        if (v == mDismissButton) {
            dismiss();
        }
        return true;
    }
}
