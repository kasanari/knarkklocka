package se.jakob.knarkklocka;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver{
    private static final String TAG = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Starting Alarm Receiver...");
        //WakeLocker.acquire(context);
        Bundle bundle = intent.getExtras();
        Intent newIntent;
        newIntent = new Intent(context, AlarmActivity.class);

        //newIntent.putExtra("alarm", alarm); //Will probably want to add extra information in future
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(newIntent);

    }
}
