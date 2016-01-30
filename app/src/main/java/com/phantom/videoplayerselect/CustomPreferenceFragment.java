package com.phantom.videoplayerselect;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class CustomPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}