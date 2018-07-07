package se.jakob.knarkklocka.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

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
    public static final String EXTRA_END_TIME = "end-time";
    public static final String EXTRA_ALARM_ID = "alarm-id";
    private static final String TAG = "TimerUtils";
    private static final int ALARM_INTENT_ID = 76;          //Arbitrary unique ID for the alarm intent
    private static final int TIMER_ACTIVITY_INTENT_ID = 34; //Arbitrary unique ID for the TimerActivity intent

    //private static int testing_time = 5000;

    public static void executeTask(Context context, String action, int id) {
        switch (action) {
            case ACTION_SET_NEW_TIMER:
                setNewAlarm(context, id);
                break;
            case ACTION_REMOVE_TIMER:
                cancelAlarm(context, id);
                break;
            case ACTION_SNOOZE_TIMER:
                setSnooze(context, id);
                break;
        }
    }

    /**
     * Returns the pending intent that starts the Alarm service
     **/
    private static PendingIntent getAlarmServiceIntent(Context context, long id) {
        Intent alarmIntent = new Intent(context, AlarmService.class);
        alarmIntent.putExtra(EXTRA_ALARM_ID, id);
        return PendingIntent.getService(context, ALARM_INTENT_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Returns the pending intent that starts the Alarm service
     **/
    private static PendingIntent getAlarmActivityIntent(Context context, long id) {
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra(EXTRA_ALARM_ID, id);
        return PendingIntent.getActivity(context, ALARM_INTENT_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Returns the pending intent that starts the main timer activity
     **/
    private static PendingIntent getShowAlarmIntent(Context context, long id) {
        Intent timerIntent = new Intent(context, TimerActivity.class);
        timerIntent.putExtra(EXTRA_ALARM_ID, id);
        return PendingIntent.getActivity(context, TIMER_ACTIVITY_INTENT_ID, timerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void setNewAlarm(Context context, long id) {
        int timer_duration = PreferenceUtils.getMainTimerLength(context);
        setNewAlarm(context, id, new Date(timer_duration));
    }

    public static void setNewAlarm(Context context, long id, Date endTime) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);

        /*Setup AlarmService to start when alarm goes off*/
        PendingIntent pendingAlarmIntent = getAlarmActivityIntent(context, id);//getAlarmServiceIntent(context);

        /*Setup alarm clock info*/
        //long wakeup_time = System.currentTimeMillis() + length;
        PendingIntent showAlarmPI = getShowAlarmIntent(context, id);
        AlarmManager.AlarmClockInfo alarmInfo = new AlarmManager.AlarmClockInfo(endTime.getTime(), showAlarmPI);

        if (alarmManager != null) {
            alarmManager.setAlarmClock(alarmInfo, pendingAlarmIntent);
            Log.d(TAG, "Set an alarm");
        }
    }

    public static void setSnooze(Context context, long id) {
        int length = PreferenceUtils.getSnoozeTimerLength(context);
        setNewAlarm(context, id, new Date(length));
    }


    public static void cancelAlarm(Context context, long id) {
        AlarmManager alarm = context.getSystemService(AlarmManager.class);
        PendingIntent pendingAlarmIntent = getAlarmActivityIntent(context, id);
        if (alarm != null) {
            alarm.cancel(pendingAlarmIntent);
            LogUtils.d("Cancelled alarm");
        }
    }

}
