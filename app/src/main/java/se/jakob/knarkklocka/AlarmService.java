package se.jakob.knarkklocka;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

public class AlarmService extends Service {

    private static final String TAG = "AlarmService";

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /** Whether the service is currently bound to AlarmActivity */
    private boolean mIsBound = false;

    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Testing", "Service got created");
        Intent intent = new Intent(this, AlarmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        startAlarm();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mIsBound = false;
        return super.onUnbind(intent);
    }
/*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand initiated");
        //Intent alarm_activity_intent = new Intent(this, AlarmActivity.class);
        //startActivity(alarm_activity_intent);
        return super.onStartCommand(intent, flags, startId);
    }
*/
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        AlarmService getService() {
            // Return this instance of AlarmService so clients can call public methods
            return AlarmService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        mIsBound = true;
        return mBinder;
    }

    public void startAlarm() {
        wakeLock = AlarmAlertWakeLocker.createPartialWakeLock(this);
        wakeLock.acquire(1000);
    }

    public void stopAlarm() {
        wakeLock.release();
        stopSelf();
    }
}
