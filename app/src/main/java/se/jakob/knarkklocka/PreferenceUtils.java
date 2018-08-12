package se.jakob.knarkklocka;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;

public final class PreferenceUtils {

    private static final String KEY_MAIN_TIMER_LENGTH = "timer_length";
    private static final String KEY_SNOOZE_TIMER_LENGTH = "snooze_length";

    private static final long DEFAULT_MAIN_TIMER_LENGTH = 4 * HOUR_IN_MILLIS;
    private static final long DEFAULT_SNOOZE_TIMER_LENGTH = 5 * MINUTE_IN_MILLIS;

    private static long getDefaultLength(String key) {
        switch (key) {
            case KEY_SNOOZE_TIMER_LENGTH:
                return DEFAULT_SNOOZE_TIMER_LENGTH;
            case KEY_MAIN_TIMER_LENGTH:
                return DEFAULT_MAIN_TIMER_LENGTH;
        }
        return -1;
    }

    private static long getShortDefault(String key) {
        switch (key) {
            case KEY_SNOOZE_TIMER_LENGTH:
                return 10 * (int) SECOND_IN_MILLIS;
            case KEY_MAIN_TIMER_LENGTH:
                return 10 * (int) SECOND_IN_MILLIS;
        }
        return -1;
    }

    private static long getTimerLength(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long length;
        boolean quickTimerMode = true; /*This can be set to true for testing purposes, otherwise it should be set to false*/
        if (quickTimerMode) {
            length = getShortDefault(key);
        } else {
            length = prefs.getLong(key, getDefaultLength(key));
        }
        return length;
    }

    public static long getMainTimerLength(Context context) {
        return getTimerLength(context, KEY_MAIN_TIMER_LENGTH);
    }

    public static long getSnoozeTimerLength(Context context) {
        return getTimerLength(context, KEY_SNOOZE_TIMER_LENGTH);
    }
}
