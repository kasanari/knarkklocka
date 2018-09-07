package se.jakob.knarkklocka.settings

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.NavUtils
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.activity_settings.*
import android.view.MenuItem
import se.jakob.knarkklocka.R
import se.jakob.knarkklocka.utils.Utils


class SettingsActivity : FragmentActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        val actionBar = actionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_settings)

        super.onCreate(savedInstanceState)
        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
                .replace(R.id.preference_content, SettingsFragment())
                .commit()
                
        tv_credits.text = Utils.creditsString
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        // When the home button is pressed, take the user back to the TimerActivity
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
        }
        return super.onOptionsItemSelected(item)
    }


    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(p0: Bundle?, p1: String?) {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences)
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
                dialogFragment.show(this.fragmentManager,
                        "android.support.v7.preference" + ".PreferenceFragment.DIALOG")
            } else {
                super.onDisplayPreferenceDialog(preference) // Could not be handled here. Try with the super method.
            }
        }
    }

}
