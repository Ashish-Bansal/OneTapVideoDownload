package com.phantom.onetapvideodownload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.phantom.utils.CheckPreferences;

public class ThemeManager {
    public static void applyTheme(AppCompatActivity activity) {
        activity.setTheme(ThemeManager.getTheme(activity));
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(ThemeManager.getPrimaryColor(activity));
        toolbar.setPopupTheme(getPopupMenuTheme(activity));
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(ThemeManager.getPrimaryDarkColor(activity));
            activity.getWindow().setNavigationBarColor(ThemeManager.getNavigationBarColor(activity));
        }
    }

    public static void onThemeChanged(Activity activity) {
        activity.finish();
        final Intent intent = activity.getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    public static int getTheme(Context context) {
        if (CheckPreferences.getDarkThemeEnabled(context)) {
            return R.style.AppTheme_Dark;
        } else {
            return R.style.AppTheme;
        }
    }

    public static int getPopupMenuTheme(Context context) {
        if (CheckPreferences.getDarkThemeEnabled(context)) {
            return R.style.AppTheme_ThemeOverlay_AppCompat_Dark;
        } else {
            return R.style.AppTheme_ThemeOverlay_AppCompat_Light;
        }
    }

    public static int getBackgroundColor(Context context) {
        if (CheckPreferences.getDarkThemeEnabled(context)) {
            return ContextCompat.getColor(context, R.color.dark_background);
        } else {
            return ContextCompat.getColor(context, R.color.white);
        }
    }

    public static int getPrimaryColor(Context context) {
        if (CheckPreferences.getDarkThemeEnabled(context)) {
            return ContextCompat.getColor(context, R.color.dark_background);
        } else {
            return ContextCompat.getColor(context, R.color.primary);
        }
    }

    public static int getLightBackgroundColor(Context context) {
        if (CheckPreferences.getDarkThemeEnabled(context)) {
            return ContextCompat.getColor(context, R.color.dark_ligher_background);
        } else {
            return ContextCompat.getColor(context, R.color.grey);
        }
    }

    public static int getPrimaryDarkColor(Context context) {
        if (CheckPreferences.getDarkThemeEnabled(context)) {
            return ContextCompat.getColor(context, R.color.dark_background);
        } else {
            return ContextCompat.getColor(context, R.color.dark_primary);
        }
    }

    public static int getNavigationBarColor(Context context) {
        if (CheckPreferences.getDarkThemeEnabled(context)) {
            return ContextCompat.getColor(context, R.color.black);
        } else {
            return ContextCompat.getColor(context, R.color.primary);
        }
    }

    public static int getHeadingTextColor(Context context) {
        if (CheckPreferences.getDarkThemeEnabled(context)) {
            return ContextCompat.getColor(context, R.color.heading_white);
        } else {
            return ContextCompat.getColor(context, R.color.heading_black);
        }
    }

    public static int getTextColor(Context context) {
        if (CheckPreferences.getDarkThemeEnabled(context)) {
            return ContextCompat.getColor(context, R.color.white);
        } else {
            return ContextCompat.getColor(context, R.color.black);
        }
    }
}
