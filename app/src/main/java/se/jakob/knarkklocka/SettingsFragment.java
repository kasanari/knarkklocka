package se.jakob.knarkklocka;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Fragment for general settings
 * Created by Jakob on 2018-03-26.
 */

public class SettingsFragment extends PreferenceFragment{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);
    }


}
