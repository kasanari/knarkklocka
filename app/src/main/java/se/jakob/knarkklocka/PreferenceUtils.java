package se.jakob.knarkklocka;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

public final class PreferenceUtils {

    private static final String KEY_TIMER_LENGTH = "timer-length";
    private static final String KEY_CUSTOM_TIMER_LENGTH = "custom-timer-length";
    private static final String KEY_SNOOZE_LENGTH = "snooze-length";

    private static final long DEFAULT_TIMER_LENGTH = 4 * DateUtils.HOUR_IN_MILLIS;
    private static final long DEFAULT_SNOOZE_LENGTH = 5 * DateUtils.MINUTE_IN_MILLIS;

    synchronized private static void setTimerLength(Context context, int hours) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_TIMER_LENGTH, hours);
        editor.apply();
    }

    public static long getTimerLength(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
         //long length = prefs.getLong(KEY_TIMER_LENGTH, DEFAULT_TIMER_LENGTH);
         double hours = 0.001;
         return (long) (hours * DateUtils.HOUR_IN_MILLIS);
    }

    synchronized public static void setCustomTimerLength(Context context, long length) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_CUSTOM_TIMER_LENGTH, length);
        editor.apply();
    }

    public static long getCustomTimerLength(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(KEY_CUSTOM_TIMER_LENGTH, DEFAULT_TIMER_LENGTH);
    }

    public static long getSnoozeLength(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(KEY_SNOOZE_LENGTH, DEFAULT_SNOOZE_LENGTH);
    }
}
