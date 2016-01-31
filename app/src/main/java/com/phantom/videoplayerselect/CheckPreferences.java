package com.phantom.videoplayerselect;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.preference.Preference;

public class CheckPreferences {
    public static boolean loggingEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("pref_url_logging", true);
    }

    public static boolean notificationsEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("pref_notification", true);
    }

    public static int notificationCountAllowed(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(prefs.getString("pref_notification_count", "1"));
    }

    public static String getPreferenceValue(Preference preference) {
        return android.support.v7.preference.PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "");
    }
}
