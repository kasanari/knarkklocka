package se.jakob.knarkklocka.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import se.jakob.knarkklocka.AlarmActivity;
import se.jakob.knarkklocka.AlarmService;
import se.jakob.knarkklocka.LogUtils;
import se.jakob.knarkklocka.PreferenceUtils;
import se.jakob.knarkklocka.TimerActivity;

/**
 * Created by Jakob Nyberg on 2018-03-26.
 */

public class TimerUtils {

    public static final String ACTION_SET_NEW_TIMER = "set-new-timer";
    public static final String ACTION_REMOVE_TIMER = "remove-timer";
    public static final String ACTION_SNOOZE_TIMER = "snooze-timer";
    private static final String TAG = "TimerUtils";
    private static final int ALARM_INTENT_ID = 76;          //Arbitrary unique ID for the alarm intent
    private static final int TIMER_ACTIVITY_INTENT_ID = 34; //Arbitrary unique ID for the TimerActivity intent

    //private static int testing_time = 5000;

    public static void executeTask(Context context, String action) {
        switch (action) {
            case ACTION_SET_NEW_TIMER:
                setNewAlarm(context);
                break;
            case ACTION_REMOVE_TIMER:
                cancelAlarm(context);
                break;
            case ACTION_SNOOZE_TIMER:
                setSnooze(context);
                break;
        }
    }

    /**
     * Returns the pending intent that starts the Alarm service
     **/
    private static PendingIntent getAlarmServiceIntent(Context context) {
        Intent alarmIntent = new Intent(context, AlarmService.class);
        return PendingIntent.getService(context, ALARM_INTENT_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Returns the pending intent that starts the Alarm service
     **/
    private static PendingIntent getAlarmActivityIntent(Context context) {
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        return PendingIntent.getActivity(context, ALARM_INTENT_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Returns the pending intent that starts the main timer activity
     **/
    private static PendingIntent getShowAlarmIntent(Context context) {
        Intent timerIntent = new Intent(context, TimerActivity.class);
        return PendingIntent.getActivity(context, TIMER_ACTIVITY_INTENT_ID, timerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Fetches the upcoming alarm if there is one
     **/
    private static AlarmManager.AlarmClockInfo getNextAlarm(Context context) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager != null) {                                                          //Check that we found the Alarm Manager
            AlarmManager.AlarmClockInfo nextAlarmClock = alarmManager.getNextAlarmClock();   //Fetch the next upcoming alarm
            if (nextAlarmClock != null) {                                                    //Check that we found an upcoming alarm
                if (nextAlarmClock.getShowIntent().equals(getShowAlarmIntent(context))) {    //Check that the upcoming alarm is ours
                    LogUtils.d("Found upcoming drug alarm!");
                    return nextAlarmClock;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    public static long getRemainingTime(Context context) {
        AlarmManager.AlarmClockInfo nextAlarm = getNextAlarm(context);
        if (nextAlarm != null) {
            return nextAlarm.getTriggerTime() - System.currentTimeMillis();
        } else {
            return -1;
        }
    }

    public static boolean alarmIsRunning(Context context) {
        return getNextAlarm(context) != null;
    }


    public static void setNewAlarm(Context context) {
        long timer_duration = PreferenceUtils.getTimerLength(context);
        setNewAlarm(context, timer_duration);
    }

    public static void setNewAlarm(Context context, long length) {
        AlarmManager alarm = context.getSystemService(AlarmManager.class);

        /*Setup AlarmService to start when alarm goes off*/
        PendingIntent pendingAlarmIntent = getAlarmActivityIntent(context);//getAlarmServiceIntent(context);

        /*Setup alarm clock info*/
        long wakeup_time = System.currentTimeMillis() + length;
        PendingIntent pi = getShowAlarmIntent(context);
        AlarmManager.AlarmClockInfo alarmInfo = new AlarmManager.AlarmClockInfo(wakeup_time, pi);

        if (alarm != null) {
            alarm.setAlarmClock(alarmInfo, pendingAlarmIntent);
            Log.d(TAG, "Set an alarm");
        }
    }

    public static void setSnooze(Context context) {
        long length = PreferenceUtils.getSnoozeLength(context);
        if (!alarmIsRunning(context)) {
            setNewAlarm(context, length);
        }
    }

    public static void cancelAlarm(Context context) {
        AlarmManager alarm = context.getSystemService(AlarmManager.class);
        PendingIntent pendingAlarmIntent = getAlarmServiceIntent(context);
        if (alarm != null) {
            alarm.cancel(pendingAlarmIntent);
            LogUtils.d("Cancelled alarm");
        }
    }

}
