package se.jakob.knarkklocka.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import se.jakob.knarkklocka.R
import se.jakob.knarkklocka.settings.TimePreferenceDialogFragmentCompat
import se.jakob.knarkklocka.settings.TimerLengthPreference

/**
 This is a preference which allows the user the set an alarm with a custom length, overriding the length settings in SharedPreferences. After the custom alarm has fired
 the setting will be disabled automatically.
 **/
class CustomTimerSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.custom_preferences)
        val switcher = (preferenceScreen.findPreference<SwitchPreferenceCompat>("custom_timer_enabled"))
        preferenceScreen.findPreference<TimerLengthPreference>("custom_timer")?.isVisible = switcher?.isChecked ?: false // Hide the length selector if the checkbox is not checked
    }


    override fun onDisplayPreferenceDialog(preference: Preference?) {
            var dialogFragment: DialogFragment? = null

            if (preference is TimerLengthPreference) { // Try if the preference is one of our custom Preferences
                // Create a new instance of TimePreferenceDialogFragment with the key of the related Preference
                dialogFragment = TimePreferenceDialogFragmentCompat.getInstance(preference.getKey())
            }

            // If it was one of our custom Preferences, show its dialog
   dialogFragment?.let { fragment ->
                fragment.setTargetFragment(this, 0)
                fragment.show(requireFragmentManager(),
                        "androidx.preference" + ".PreferenceFragment.DIALOG")
            } ?: super.onDisplayPreferenceDialog(preference) // Could not be handled here. Try with the super method.

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        preferenceScreen.sharedPreferences
                .registerOnSharedPreferenceChangeListener(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        preferenceScreen.sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroyView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if ((key ?: false) == "custom_timer_enabled") {
            val switcher = preferenceScreen.findPreference<SwitchPreferenceCompat>("custom_timer_enabled")
            switcher?.isChecked = sharedPreferences?.getBoolean(key, false) ?: false // Change the checkbox to reflect current setting
            preferenceScreen.findPreference<TimerLengthPreference>("custom_timer")?.isVisible = switcher?.isChecked ?: false // Hide the length selector if the checkbox is not checked
        }
    }
}