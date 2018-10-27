package se.jakob.knarkklocka.utils

import android.content.Context
import android.text.format.DateUtils.*
import androidx.preference.PreferenceManager
import se.jakob.knarkklocka.BuildConfig

object PreferenceUtils {

    private const val KEY_MAIN_TIMER_LENGTH = "timer_length"
    private const val KEY_SNOOZE_TIMER_LENGTH = "snooze_length"

    private const val DEFAULT_MAIN_TIMER_LENGTH = 4 * HOUR_IN_MILLIS
    private const val DEFAULT_SNOOZE_TIMER_LENGTH = 5 * MINUTE_IN_MILLIS

    private fun getDefaultLength(key: String): Long {
        return when (key) {
            KEY_SNOOZE_TIMER_LENGTH -> DEFAULT_SNOOZE_TIMER_LENGTH
            KEY_MAIN_TIMER_LENGTH -> DEFAULT_MAIN_TIMER_LENGTH
            else -> {
                -1
            }
        }
    }

    private fun getShortDefault(key: String): Long {
        return when (key) {
            KEY_SNOOZE_TIMER_LENGTH -> (10 * SECOND_IN_MILLIS.toInt()).toLong()
            KEY_MAIN_TIMER_LENGTH -> (10 * SECOND_IN_MILLIS.toInt()).toLong()
            else -> {
                -1
            }
        }
    }

    private fun getTimerLength(context: Context, key: String): Long {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val quickTimerMode =
                if (BuildConfig.DEBUG) {
                    false /*This can be set to true for testing purposes, otherwise it should be set to false*/
                } else {
                    false /*Should not be changed*/
                }

        return if (quickTimerMode) {
            getShortDefault(key)
        } else {
            sharedPreferences.getLong(key, getDefaultLength(key))
        }
    }

    fun getMainTimerLength(context: Context): Long {
        return getTimerLength(context, KEY_MAIN_TIMER_LENGTH)
    }

    fun getSnoozeTimerLength(context: Context): Long {
        return getTimerLength(context, KEY_SNOOZE_TIMER_LENGTH)
    }

    private fun setTimerLength(context: Context, key: String, length: Long) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs!!.edit()
        editor.putLong(key, length)
        editor.apply()
    }

    fun setMainTimerLength(context: Context, length: Long) {
        setTimerLength(context, KEY_MAIN_TIMER_LENGTH, length)
    }

    fun setSnoozeTimerLength(context: Context, length: Long) {
        setTimerLength(context, KEY_SNOOZE_TIMER_LENGTH, length)
    }

}
