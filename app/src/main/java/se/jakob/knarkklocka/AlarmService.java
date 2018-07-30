package se.jakob.knarkklocka;

import android.arch.lifecycle.LifecycleService;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import se.jakob.knarkklocka.data.Alarm;
import se.jakob.knarkklocka.data.AlarmRepository;

import static se.jakob.knarkklocka.data.Alarm.STATE_ACTIVE;
import static se.jakob.knarkklocka.utils.TimerUtils.ACTION_ACTIVATE_ALARM;
import static se.jakob.knarkklocka.utils.TimerUtils.ACTION_STOP_ALARM;
import static se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID;

public class AlarmService extends LifecycleService {

    private static final String TAG = "AlarmService";
    private AlarmRepository mRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service got created");
        mRepository = new AlarmRepository(getApplication());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final long id = intent.getLongExtra(EXTRA_ALARM_ID, -1);
        final String action = intent.getAction();
        if (action != null ) {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    handleIntent(action, id);
                }
            });
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /*Intent handler meant to be run on separate thread*/
    public void handleIntent(String action, long id) {
        Alarm alarm = mRepository.getAlarmByID(id);
            switch (action) {
                case ACTION_ACTIVATE_ALARM:
                    if (BuildConfig.DEBUG) {
                        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
                        String debugString = String.format(Locale.getDefault(), "Activating alarm with id %d due %s", id, df.format(alarm.getEndTime()));
                        Log.d(TAG, debugString);
                    }

                    if (alarm.getSnoozes() < 10) {
                        mRepository.changeAlarmState(id, STATE_ACTIVE);
                        startAlarm(alarm);
                    } else {
                        alarm.setState(Alarm.STATE_DEAD);
                        mRepository.update(alarm);
                        stopAlarm();
                        stopSelf();
                    }
                    break;

                case ACTION_STOP_ALARM:
                    stopAlarm();
                    stopSelf();
                    break;
            }
    }

    private void startAlarm(Alarm alarm) {
        WakeLocker.acquire(this);
        Klaxon.vibrateAlarm(this);
        AlarmNotificationsBuilder.showNotification(this, alarm);
    }

    private void stopAlarm() {
        Klaxon.stopVibrate(this);
        stopForeground(true);
        WakeLocker.release();
    }

    @Override
    public void onDestroy() {
        LogUtils.v("AlarmService.onDestroy() called");
        super.onDestroy();
        stopAlarm();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }
}
