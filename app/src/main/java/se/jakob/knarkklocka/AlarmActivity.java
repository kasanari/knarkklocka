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

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;

import java.util.Date;

import se.jakob.knarkklocka.data.Alarm;
import se.jakob.knarkklocka.data.MainAlarmViewModel;

public class AlarmActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AlarmActivity";

    private Button mSnoozeButton;
    private Button mDismissButton;

    private MainAlarmViewModel mainAlarmViewModel;

    private Chronometer mAlarmChronometer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        /* Turn on vibration */
        Klaxon.vibrateOnce(this);

        /* Keep screen turned on */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        /*Hide navigation bar*/
        hideNavigationBar();

        // Close dialogs and window shade, so this is fully visible
        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        mDismissButton = findViewById(R.id.dismiss);
        mSnoozeButton =  findViewById(R.id.snooze);

        mSnoozeButton.setOnClickListener(this);
        mDismissButton.setOnClickListener(this);

        mAlarmChronometer = findViewById(R.id.alarm_chronometer);

        mainAlarmViewModel = ViewModelProviders.of(this).get(MainAlarmViewModel.class);
        mainAlarmViewModel.getAlarm().observe(this, new Observer<Alarm>() {
            @Override
            public void onChanged(@Nullable final Alarm alarm) {
                if (alarm != null) {
                    //DateFormat dateFormat = DateFormat.getTimeInstance();
                    Date endTime = alarm.getEndTime();
                    //String dateString = dateFormat.format(endTime);
                    //due_time_view.setText(dateString);
                    //due_time_view.setVisibility(View.VISIBLE);
                    mAlarmChronometer.setVisibility(View.VISIBLE);
                    long timeDelta = endTime.getTime() - System.currentTimeMillis();
                    mAlarmChronometer.setBase(SystemClock.elapsedRealtime() + timeDelta);
                    mAlarmChronometer.start();
                } else {
                    //due_time_view.setVisibility(View.INVISIBLE);
                    mAlarmChronometer.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void snooze() {
        finish();
    }

    public void dismiss() {
        if (isAlarmRunning()) {
            mainAlarmViewModel.delete();
        }
        finish();
    }

    public boolean isAlarmRunning() {
        return mainAlarmViewModel.getAlarm().getValue() != null;
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
}
