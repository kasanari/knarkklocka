package se.jakob.knarkklocka;

import android.arch.lifecycle.LifecycleService;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import se.jakob.knarkklocka.data.Alarm;
import se.jakob.knarkklocka.data.AlarmRepository;

import static se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID;

public class AlarmService extends LifecycleService {

    private static final String TAG = "AlarmService";

    private PowerManager.WakeLock wakeLock;
    private AlarmRepository mRepository;
    private LiveData<Alarm> currentAlarm;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Testing", "Service got created");
        this.wakeLock = AlarmAlertWakeLocker.createPartialWakeLock(this);
        mRepository = new AlarmRepository(getApplication());

    }

    public void startAlarmActivity() {
        Intent alarmActivityIntent = new Intent(this, AlarmActivity.class);
        alarmActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(alarmActivityIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long alarmID = intent.getLongExtra(EXTRA_ALARM_ID, -1);
        currentAlarm = mRepository.getAlarmByID(alarmID);
        currentAlarm.observe(this, new Observer<Alarm>() {
            @Override
            public void onChanged(@Nullable final Alarm alarm) {
                if (alarm != null) {
                    Log.d(TAG, alarm.toString());
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            alarm.setState(Alarm.STATE_ACTIVE);
                            mRepository.update(alarm);
                            AlarmAlertWakeLocker.releaseCpuLock();
                            stopSelf();
                        }
                    });
                    startAlarmActivity();
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
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
