package com.phantom.onetapvideodownload.ui;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.XpPreferenceFragment;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.phantom.onetapvideodownload.utils.enums.MaterialDialogIds;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.utils.CheckPreferences;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.SwitchPreference;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends XpPreferenceFragment {
    private static List<Preference> preferenceList = new ArrayList<>();

    @Override
    public void onCreatePreferences2(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.fragment_preferences);
        preferenceList.add(findPreference("pref_notification_count"));
        preferenceList.add(findPreference("pref_notification_dismiss_time"));
        preferenceList.add(findPreference("pref_vibrate_amount"));
        preferenceList.add(findPreference("pref_download_location"));

        for (Preference p : preferenceList) {
            updatePreferenceSummary(p);
            p.setOnPreferenceChangeListener(bindPreferenceSummaryToValueListener);
        }

        findPreference("pref_download_location").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MainActivity mainActivity = ((MainActivity)getActivity());
                mainActivity.setFolderChooserDialogId(MaterialDialogIds.DefaultDownloadLocation);
                new FolderChooserDialog.Builder(mainActivity)
                        .chooseButton(R.string.md_choose_label)
                        .initialPath(Environment.getExternalStorageDirectory().getPath())
                        .show();
                return true;
            }
        });
    }

    private static Preference.OnPreferenceChangeListener bindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference instanceof ListPreference) {
                String stringValue = value.toString();
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                CharSequence summary = index >= 0 ? listPreference.getEntries()[index] : null;
                preference.setSummary(summary);
            } else if (preference instanceof SwitchPreference) {
                //Do something
            } else {
                String stringValue = value.toString();
                preference.setSummary(stringValue);
            }
            return true;
        }

    };

    public static void updatePreferenceSummary(Preference preference) {
        if (preference.getKey().equals("pref_download_location")) {
            bindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    CheckPreferences.getDownloadLocation(preference.getContext()));
        } else if (preference instanceof SwitchPreference) {
            bindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    CheckPreferences.getBooleanPreferenceValue(preference));
        } else {
            bindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    CheckPreferences.getStringPreferenceValue(preference));
        }
    }

    public static void updatePreferenceSummary() {
        for (Preference preference : preferenceList) {
            updatePreferenceSummary(preference);
        }
    }
}
