package se.jakob.knarkklocka.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.util.Log;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import se.jakob.knarkklocka.AlarmActivity;
import se.jakob.knarkklocka.AlarmBroadcastReceiver;
import se.jakob.knarkklocka.AlarmService;
import se.jakob.knarkklocka.AppExecutors;
import se.jakob.knarkklocka.BuildConfig;
import se.jakob.knarkklocka.PreferenceUtils;
import se.jakob.knarkklocka.TimerActivity;
import se.jakob.knarkklocka.data.Alarm;
import se.jakob.knarkklocka.data.AlarmViewModel;

/**
 * Created by Jakob Nyberg on 2018-03-26.
 */

public class TimerUtils {

    public static final String ACTION_ACTIVATE_ALARM = "activate-alarm";
    public static final String ACTION_STOP_ALARM = "stop-alarm";

    public static final String EXTRA_END_TIME = "end-time";
    public static final String EXTRA_ALARM_ID = "alarm-id";

    private static final String TAG = "TimerUtils";
    private static final int ALARM_INTENT_ID = 76;          //Arbitrary unique ID for the alarm intent
    private static final int TIMER_ACTIVITY_INTENT_ID = 34; //Arbitrary unique ID for the TimerActivity intent




    /**
     * Returns whatever pending intent i am using at the moment
     **/
    private static PendingIntent getPI(Context context, long id) {
        //return getAlarmReceiverIntent(context, id);
        return getAlarmServiceIntent(context, id);
    }

    /**
     * Returns the pending intent that starts the Alarm Service
     **/
    private static PendingIntent getAlarmServiceIntent(Context context, long id) {
        Intent alarmIntent = new Intent(context, AlarmService.class);
        alarmIntent.putExtra(EXTRA_ALARM_ID, id);
        alarmIntent.setAction(ACTION_ACTIVATE_ALARM);
        return PendingIntent.getForegroundService(context, ALARM_INTENT_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Returns the pending intent that starts the Alarm Activity
     **/
    public static PendingIntent getAlarmActivityIntent(Context context, long id) {
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra(EXTRA_ALARM_ID, id);
        return PendingIntent.getActivity(context, ALARM_INTENT_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Returns the pending intent that starts the Alarm Activity
     **/
    private static PendingIntent getAlarmReceiverIntent(Context context, long id) {
        Intent alarmIntent = new Intent(context, AlarmBroadcastReceiver.class);
        alarmIntent.putExtra(EXTRA_ALARM_ID, id);
        return PendingIntent.getBroadcast(context, ALARM_INTENT_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Returns the pending intent that starts the main timer activity
     **/
    private static PendingIntent getShowAlarmIntent(Context context, long id) {
        Intent timerIntent = new Intent(context, TimerActivity.class);
        timerIntent.putExtra(EXTRA_ALARM_ID, id);
        return PendingIntent.getActivity(context, TIMER_ACTIVITY_INTENT_ID, timerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void setNewAlarm(Context context, long id, Date endTime) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);

        /*Set AlarmService to start when alarm goes off*/
        PendingIntent pendingAlarmIntent = getPI(context, id);

        /*Setup alarm clock info*/
        //long wakeup_time = System.currentTimeMillis() + length;
        PendingIntent showAlarmPI = getShowAlarmIntent(context, id);
        AlarmManager.AlarmClockInfo alarmInfo = new AlarmManager.AlarmClockInfo(endTime.getTime(), showAlarmPI);

        if (alarmManager != null) {
            alarmManager.setAlarmClock(alarmInfo, pendingAlarmIntent);
            if (BuildConfig.DEBUG) {
                DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
                String debugString = String.format(Locale.getDefault(), "Set alarm with id %d due %s", id, df.format(endTime));
                Log.d(TAG, debugString);
            }
        }
    }

    public static void cancelAlarm(Context context, long id) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        PendingIntent pendingAlarmIntent = getPI(context, id);
        if (alarmManager != null) {
            alarmManager.cancel(pendingAlarmIntent);
            if (BuildConfig.DEBUG) {
                String debugString = String.format(Locale.getDefault(), "Cancelled alarm with id %d", id);
                Log.d(TAG, debugString);
            }
        }
    }

    public static void startMainTimer(final Context context, final AlarmViewModel vm) {
        restartTimer(context, vm, false);
    }

    public static void startSnoozeTimer(final Context context, final AlarmViewModel vm) {
        restartTimer(context, vm, true);
    }

    private static void restartTimer(final Context context, final AlarmViewModel vm, final boolean isSnooze) {
        final long timer_duration;
        if (isSnooze) {
            timer_duration = PreferenceUtils.getSnoozeTimerLength(context);
        } else {
            timer_duration = PreferenceUtils.getMainTimerLength(context);
        }

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Calendar currentTime = Calendar.getInstance();
                Calendar endTime = Calendar.getInstance();
                endTime.add(Calendar.MILLISECOND, (int) timer_duration);
                if (isSnooze) {
                    Alarm currentAlarm = vm.getCurrentAlarm();
                    if (currentAlarm != null) {
                        vm.snooze(endTime.getTime());
                        TimerUtils.setNewAlarm(context, currentAlarm.getId(), endTime.getTime());
                    }
                } else {
                    Alarm alarm = new Alarm(Alarm.STATE_WAITING, currentTime.getTime(), endTime.getTime());
                    long id = vm.add(alarm);
                    setNewAlarm(context, id, alarm.getEndTime());
                }
            }
        });
    }
}
