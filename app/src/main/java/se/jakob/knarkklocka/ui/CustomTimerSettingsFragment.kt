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

class CustomTimerSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.custom_preferences)
        val switcher = (preferenceScreen.findPreference<SwitchPreferenceCompat>("custom_timer_enabled"))
        preferenceScreen.findPreference<TimerLengthPreference>("custom_timer")?.isVisible = switcher?.isChecked ?: false
    }


    override fun onDisplayPreferenceDialog(preference: Preference?) {
        // Try if the preference is one of our custom Preferences
        var dialogFragment: DialogFragment? = null

        if (preference is TimerLengthPreference) {
            // Create a new instance of TimePreferenceDialogFragment with the key of the related Preference
            dialogFragment = TimePreferenceDialogFragmentCompat.getInstance(preference.getKey())
        }

        // If it was one of our custom Preferences, show its dialog
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(this.fragmentManager!!,
                    "androidx.preference" + ".PreferenceFragment.DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference) // Could not be handled here. Try with the super method.
        }
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
            switcher?.isChecked = sharedPreferences?.getBoolean(key, false) ?: false
            preferenceScreen.findPreference<TimerLengthPreference>("custom_timer")?.isVisible = switcher?.isChecked ?: false
        }
    }
}