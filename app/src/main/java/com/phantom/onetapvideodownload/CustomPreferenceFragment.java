package com.phantom.onetapvideodownload;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.XpPreferenceFragment;

import net.xpece.android.support.preference.ListPreference;

public class CustomPreferenceFragment extends XpPreferenceFragment {
    @Override
    public void onCreatePreferences2(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        bindPreferenceSummaryToValue(findPreference("pref_notification_count"));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                CharSequence summary = index >= 0 ? listPreference.getEntries()[index] : null;
                preference.setSummary(summary);
            } else {
                preference.setSummary(stringValue);
            }

            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                CheckPreferences.getPreferenceValue(preference));
    }
}
