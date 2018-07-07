package se.jakob.knarkklocka;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;

public final class PreferenceUtils {

    private static final String KEY_MAIN_TIMER_LENGTH = "timer-length";
    private static final String KEY_CUSTOM_TIMER_LENGTH = "custom-timer-length";
    private static final String KEY_SNOOZE_TIMER_LENGTH = "snooze-length";

    private static final int DEFAULT_MAIN_TIMER_LENGTH = 4 * (int) HOUR_IN_MILLIS;
    private static final int DEFAULT_SNOOZE_TIMER_LENGTH = 5 * (int) MINUTE_IN_MILLIS;

    private static boolean shortMode = true;

    synchronized private static void setTimerLength(Context context, String key, int length) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, length);
        editor.apply();
    }

    private static int getDefaultLength(String key) {
        switch (key) {
            case KEY_CUSTOM_TIMER_LENGTH:
                return DEFAULT_MAIN_TIMER_LENGTH;
            case KEY_SNOOZE_TIMER_LENGTH:
                return DEFAULT_SNOOZE_TIMER_LENGTH;
            case KEY_MAIN_TIMER_LENGTH:
                return DEFAULT_MAIN_TIMER_LENGTH;
        }
        return -1;
    }

    private static int getShortDefault(String key) {
        switch (key) {
            case KEY_CUSTOM_TIMER_LENGTH:
                return 10 * (int) SECOND_IN_MILLIS;
            case KEY_SNOOZE_TIMER_LENGTH:
                return 5 * (int) SECOND_IN_MILLIS;
            case KEY_MAIN_TIMER_LENGTH:
                return 10 * (int) SECOND_IN_MILLIS;
        }
        return -1;
    }

    private static int getTimerLength(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int length;
        if (shortMode) {
            length = getShortDefault(key);
        } else {
            length = prefs.getInt(key, getDefaultLength(key));
        }
        return length;
    }

    synchronized public static void setSnoozeTimerLength(Context context, int length) {
        setTimerLength(context, KEY_SNOOZE_TIMER_LENGTH, length);
    }

    synchronized public static void setCustomTimerLength(Context context, int length) {
        setTimerLength(context, KEY_CUSTOM_TIMER_LENGTH, length);
    }

    public static int getMainTimerLength(Context context) {
        return getTimerLength(context, KEY_MAIN_TIMER_LENGTH);
    }

    public static int getCustomTimerLength(Context context) {
        return getTimerLength(context, KEY_CUSTOM_TIMER_LENGTH);
    }

    public static int getSnoozeTimerLength(Context context) {
        return getTimerLength(context, KEY_SNOOZE_TIMER_LENGTH);
    }
}
