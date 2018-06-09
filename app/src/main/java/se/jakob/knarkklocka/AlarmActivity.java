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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import se.jakob.knarkklocka.utils.TimerUtils;

public class AlarmActivity extends Activity implements View.OnClickListener {

    /** Whether the AlarmService is currently bound */
    private boolean mServiceBound;

    private static final String TAG = "AlarmActivity";

    private Button mSnoozeButton;
    private Button mDismissButton;
//
//    /**
//     * Bind AlarmService if not yet bound.
//     */
//    private void bindAlarmService() {
//        if (!mServiceBound) {
//            final Intent intent = new Intent(this, AlarmService.class);
//            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//            mServiceBound = true;
//        }
//    }
///*
//    /**
//     * Unbind AlarmService if bound.
//     */
//    private void unbindAlarmService() {
//        if (mServiceBound) {
//            unbindService(mConnection);
//            mServiceBound = false;
//        }
//    }

//    private final ServiceConnection mConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            Log.i(TAG,"Finished binding to AlarmService");
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            Log.i(TAG,"Disconnected from AlarmService");
//        }
//    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        //Klaxon.vibrateOnce(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        hideNavigationBar();

        // Close dialogs and window shade, so this is fully visible
        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        mDismissButton = findViewById(R.id.dismiss);
        mSnoozeButton =  findViewById(R.id.snooze);

        mSnoozeButton.setOnClickListener(this);
        mDismissButton.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //bindAlarmService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        WakeLocker.releaseCpuLock();
        //unbindAlarmService();
    }

    public void snooze() {
        //unbindAlarmService();
        WakeLocker.releaseCpuLock();
        Intent startNewTimerIntent = new Intent(this, TimerIntentService.class);
        startNewTimerIntent.setAction(TimerUtils.ACTION_SNOOZE_TIMER);
        startService(startNewTimerIntent);
        finish();
    }

    public void dismiss() {
        //unbindAlarmService();
        WakeLocker.releaseCpuLock();
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
