package se.jakob.knarkklocka.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.activity_settings.*
import se.jakob.knarkklocka.R
import se.jakob.knarkklocka.utils.Utils


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
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
                dialogFragment.show(this.fragmentManager!!,
                        "androidx.preference" + ".PreferenceFragment.DIALOG")
            } else {
                super.onDisplayPreferenceDialog(preference) // Could not be handled here. Try with the super method.
            }
        }
    }

}
