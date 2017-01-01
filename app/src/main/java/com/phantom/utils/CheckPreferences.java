package com.phantom.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.preference.Preference;

import java.io.File;

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

    public static Boolean getBooleanPreferenceValue(Preference preference) {
        return android.support.v7.preference.PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getBoolean(preference.getKey(), true);
    }

    public static Integer getIntegerPreferenceValue(Preference preference) {
        return android.support.v7.preference.PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getInt(preference.getKey(), -1);
    }

    public static String getStringPreferenceValue(Preference preference) {
        return android.support.v7.preference.PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), "");
    }

    public static Integer notificationDismissTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(prefs.getString("pref_notification_dismiss_time", "15"));
    }

    public static boolean headsUpEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("pref_headsup", false);
    }

    public static boolean vibrationEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("pref_vibrate", false);
    }

    public static Integer vibrationAmount(Context context) {
        if (!vibrationEnabled(context)) {
            return 0;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(prefs.getString("pref_vibrate_amount", "200"));
    }

    public static void setDownloadLocation(Context context, String location) {
        SharedPreferences settings = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("pref_download_location", location);
        editor.apply();
    }

    public static String getDownloadLocation(Context context) {
        File downloadDirectory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);

        SharedPreferences settings = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString("pref_download_location", downloadDirectory.toString());
    }

    public static Boolean getDonationStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean("donation_status", false);
    }

    public static void setDonationStatus(Context context, Boolean donationStatus) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("donation_status", donationStatus);
        editor.apply();
    }

    public static Boolean getAdEnabled(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean("ad_enabled", true);
    }

    public static void setAdEnabled(Context context, Boolean enabled) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("ad_enabled", enabled);
        editor.apply();
    }

    public static void toggleAdEnabled(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setAdEnabled(context, !sharedPreferences.getBoolean("ad_enabled", true));
    }

    public static boolean xposedErrorsEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("prefs_enable_startup_errors", true);
    }

    public static boolean getModuleDisabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("pref_module_disabled", false);
    }


    public static Boolean getDarkThemeEnabled(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean("pref_dark_theme_enabled", false);
    }

    public static void setDarkThemeEnabled(Context context, Boolean enabled) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("pref_dark_theme_enabled", enabled);
        editor.apply();
    }

}
