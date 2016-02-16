package com.phantom.onetapvideodownload;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.XpPreferenceFragment;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;

import net.xpece.android.support.preference.ListPreference;

public class CustomPreferenceFragment extends XpPreferenceFragment {

    @Override
    public void onCreatePreferences2(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        bindPreferenceSummaryToValue(findPreference("pref_notification_count"));
        bindPreferenceSummaryToValue(findPreference("pref_notification_dismiss_time"));
        bindPreferenceSummaryToValue(findPreference("pref_vibrate_amount"));
        bindPreferenceSummaryToValue(findPreference("pref_download_location"));

        findPreference("pref_url_logging").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                DatabaseHandler databaseHandler = DatabaseHandler.getDatabase(getContext());
                databaseHandler.clearDatabase();
                return true;
            }
        });

        findPreference("pref_download_location").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new FolderChooserDialog.Builder((MainActivity)getActivity())
                        .chooseButton(R.string.md_choose_label)
                        .initialPath(Environment.getExternalStorageDirectory().getPath())
                        .show();
                return true;
            }
        });

        updateDownloadLocationPreference();
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

    public void updateDownloadLocationPreference() {
        String downloadLocation = CheckPreferences.getDownloadLocation(getContext());
        findPreference("pref_download_location").setSummary(downloadLocation);
    }
}
