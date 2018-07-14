package se.jakob.knarkklocka;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;


public class AlarmBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Broadcast Receiver started");
        // We're creating a new intent that's going to start the AlarmActivity
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, TAG);
        wakeLock.acquire(30000);
        Intent in = new Intent(context, AlarmActivity.class);
        // You need to add this to your intent if you want to start an Activity fromm a class
        // that is not an Activity itself
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Now we just start the Activity
        context.startActivity(in);
        wakeLock.release();
    }
}