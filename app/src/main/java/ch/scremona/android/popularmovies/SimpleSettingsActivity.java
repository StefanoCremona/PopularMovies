package ch.scremona.android.popularmovies;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by stefanocremona on 09/10/15.
 */
public class SimpleSettingsActivity extends Activity {

    private final String LOG_CAT = SimpleSettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsFragment sFragment = new SettingsFragment();
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, sFragment)
                .commit();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        private final String LOG_CAT = SettingsFragment.class.getSimpleName();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            //Update the summary for the SortMethodKey only
            //TO_DO for general behavior
            Preference pref = findPreference(getString(R.string.pref_sortmethods_key));
            ListPreference listPreference = (ListPreference) pref;
            pref.setSummary(listPreference.getEntry());

        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d(LOG_CAT, "onSharedPreferenceChanged input key: "+ key);

            //Update the summary of the SettingsView
            if (key.equals(getString(R.string.pref_sortmethods_key))) {
                Preference pref = findPreference(key);
                Log.d(LOG_CAT, "onSharedPreferenceChanged Summary was: " + pref.getSummary());
                if(pref instanceof ListPreference){
                    ListPreference listPreference = (ListPreference) pref;
                    pref.setSummary(listPreference.getEntry());
                } else {
                    pref.setSummary(sharedPreferences.getString(key, ""));
                }
                Log.d(LOG_CAT, "onSharedPreferenceChanged Summary now is: " + pref.getSummary());
            }

        }
    }


}
