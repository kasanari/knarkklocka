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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import se.jakob.knarkklocka.LocalService.LocalBinder;
import se.jakob.knarkklocka.utils.TimerUtils;

public class AlarmActivity extends Activity implements View.OnClickListener {

    /**
     * Bound AlarmService
     */
    LocalService mService;

    /** Whether the AlarmService is currently bound */
    private boolean mBound = false;

    private static final String TAG = "AlarmActivity";

    private Button mSnoozeButton;
    private Button mDismissButton;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /**
     * Bind AlarmService if not yet bound.
     */
    private void bindAlarmService() {
        final Intent intent = new Intent(this, AlarmService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbind AlarmService if bound.
     */
    private void unbindAlarmService() {
        unbindService(mConnection);
    }

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

    }

    @Override
    protected void onStart() {
        super.onStart();
        final Intent intent = new Intent(this, LocalService.class);
        boolean success = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //mService.startAlarm();
        dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mService.stopAlarm();
        unbindAlarmService();
        mBound = false;
    }

    /*
    @Override
    protected void onResume() {
        super.onResume();
        bindAlarmService();
    }*/

    /*@Override
    protected void onPause() {
        super.onPause();
        //unbindAlarmService();
    }*/

    public void snooze() {
        Intent startNewTimerIntent = new Intent(this, TimerIntentService.class);
        startNewTimerIntent.setAction(TimerUtils.ACTION_SNOOZE_TIMER);
        startService(startNewTimerIntent);
        //mService.stopAlarm();
        unbindAlarmService();
        finish();
    }

    public void dismiss() {
        //mService.stopAlarm();
        unbindAlarmService();
        finish();
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
