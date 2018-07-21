package se.jakob.knarkklocka;

import android.arch.lifecycle.LifecycleService;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.DateFormat;
import java.util.Locale;

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
        mRepository = new AlarmRepository(getApplication());
    }

    public void startAlarmActivity() {
        Intent alarmActivityIntent = new Intent(this, AlarmActivity.class);
        alarmActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(alarmActivityIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, TAG);
        //wakeLock.acquire(30000);
        WakeLocker.acquire(this);
        long alarmID = intent.getLongExtra(EXTRA_ALARM_ID, -1);
        currentAlarm = mRepository.getAlarmByID(alarmID);
        currentAlarm.observe(this, new Observer<Alarm>() {
            @Override
            public void onChanged(@Nullable final Alarm alarm) {
                if (alarm != null) {
                    if (BuildConfig.DEBUG) {
                        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
                        String debugString = String.format(Locale.getDefault(), "Activating alarm with id %d due %s", alarm.getId(), df.format(alarm.getEndTime()));
                        Log.d(TAG, debugString);
                    }

                    if (alarm.getSnoozes() > 10) {
                        alarm.setState(Alarm.STATE_DEAD);
                        mRepository.update(alarm);
                        WakeLocker.release();
                        stopSelf();
                    } else {
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                alarm.setState(Alarm.STATE_ACTIVE);
                                mRepository.update(alarm);
                                WakeLocker.release();
                                stopSelf();
                            }
                        });
                        startAlarmActivity();
                    }
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
}
