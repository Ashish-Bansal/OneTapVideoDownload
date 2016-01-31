package com.phantom.videoplayerselect;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CheckPreferences {

    public static boolean loggingDisabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return !prefs.getBoolean("pref_url_logging", true);
    }
}
