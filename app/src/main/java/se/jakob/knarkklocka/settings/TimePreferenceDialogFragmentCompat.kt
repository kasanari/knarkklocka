package se.jakob.knarkklocka.settings

import android.os.Bundle
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.text.format.DateUtils
import android.view.View
import android.widget.NumberPicker
import se.jakob.knarkklocka.R


class TimePreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    private lateinit var hourPicker: NumberPicker
    private lateinit var minutePicker: NumberPicker

    private fun setupPickers(view: View) {
        val preference = preference
        if (preference is TimerLengthPreference) {
            hourPicker = view.findViewById(R.id.np_hours)
            minutePicker = view.findViewById(R.id.np_minutes)
            hourPicker.maxValue = 100
            hourPicker.minValue = 0
            minutePicker.maxValue = 59
            minutePicker.minValue = 0
            hourPicker.wrapSelectorWheel = false
            minutePicker.wrapSelectorWheel = false
            hourPicker.value = preference.wholeHours
            minutePicker.value = preference.wholeMinutes
        }

    }

    override fun onBindDialogView(view: View) {
        setupPickers(view)
        super.onBindDialogView(view)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {

            val hour = hourPicker.value
            val minute = minutePicker.value
            val newLength = hour * DateUtils.HOUR_IN_MILLIS + minute * DateUtils.MINUTE_IN_MILLIS

            // Get the related Preference and save the value
            val preference = preference
            if (preference is TimerLengthPreference) {
                // This allows the client to ignore the user value.
                if (preference.callChangeListener(newLength)) {
                    // Save the value
                    preference.time = newLength
                }
            }
        }
    }

    companion object {
        fun getInstance(
                key: String
        ): TimePreferenceDialogFragmentCompat {
            val fragment = TimePreferenceDialogFragmentCompat()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = b

            return fragment
        }
    }

}