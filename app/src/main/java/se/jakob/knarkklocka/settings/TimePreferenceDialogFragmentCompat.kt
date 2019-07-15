package se.jakob.knarkklocka.settings
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.widget.NumberPicker
import androidx.preference.PreferenceDialogFragmentCompat
import se.jakob.knarkklocka.R

/**
 * Dialog fragment which shows two spinners. Used for setting the number of hours and minutes an alarm should
 * run for.
 */
class TimePreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    private lateinit var hourPicker: NumberPicker
    private lateinit var minutePicker: NumberPicker

    private fun setupPickers(view: View) {
        val preference = preference
        if (preference is TimerLengthPreference) {
            hourPicker = view.findViewById(R.id.np_hours)
            minutePicker = view.findViewById(R.id.np_minutes)
            hourPicker.maxValue = 24
            hourPicker.minValue = 0
            minutePicker.maxValue = 59
            minutePicker.minValue = 0
            hourPicker.wrapSelectorWheel = true
            minutePicker.wrapSelectorWheel = true
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
            val args: Bundle = Bundle().apply {
                putString(ARG_KEY, key) // Add key of the preference this dialog belongs to.
            }

            return TimePreferenceDialogFragmentCompat().apply {
                arguments = args
            }
        }
    }

}