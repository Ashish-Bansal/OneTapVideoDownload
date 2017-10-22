package com.phantom.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.BuildConfig;
import com.phantom.onetapvideodownload.R;

import java.io.File;

public class HookClassNamesFetcher extends AsyncTask<String, Integer, String> {
    private Context mContext;
    private String mHookUrl;
    private File mHookFile;
    private MaterialDialog mProgressDialog;

    private static final String HOOKS_FILE_NAME = "Hooks.json";
    private static final String HOOKS_URL = "https://raw.githubusercontent.com/Ashish-Bansal/OneTapVideoDownload/master/app/src/main/assets/HookClassnames.json";

    public static void startHookFileUpdateOnMainThread(Context context) {
        File hookFile = new File(getHooksFilePath(context));
        HookClassNamesFetcher hookClassNamesFetcher = new HookClassNamesFetcher(context, getHooksUrl(), hookFile);
        hookClassNamesFetcher.updateHooksOnMainThread();
    }

    private void updateHooksOnMainThread() {
        String json = Global.getResponseBody(mHookUrl);
        if (json == null) {
            ApplicationLogMaintainer.sendBroadcast(mContext, "Error fetching Hook file " + mHookUrl);
            return;
        }

        if (Global.writeStringToFile(mHookFile, json)) {
            mHookFile.setReadable(true, false);
            ApplicationLogMaintainer.sendBroadcast(mContext, "Hooks Successfully updated");

            try {
                File applicationDataDirectory = new File(getDataDirectory(mContext));
                applicationDataDirectory.setExecutable(true, false);
                ApplicationLogMaintainer.sendBroadcast(mContext, "Changed application data directory permissions.");
            } catch (Exception e) {
                ApplicationLogMaintainer.sendBroadcast(mContext, "Updating package data directory permission failed.");
            }
        } else {
            ApplicationLogMaintainer.sendBroadcast(mContext, "Error writing fetched file to : " + mHookFile.getAbsolutePath());
        }
    }

    public static void startHookFileUpdateAsync(Context context) {
        File hookFile = new File(getHooksFilePath(context));
        new HookClassNamesFetcher(context, getHooksUrl(), hookFile).execute();
    }

    private HookClassNamesFetcher(Context context, String url, File destinationPath) {
        mContext = context;
        mHookUrl = url;
        mHookFile = destinationPath;
    }

    protected String doInBackground(String... urls) {
        try {
            return Global.getResponseBody(mHookUrl);
        } catch (Exception e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }
        return null;
    }

    protected void onPreExecute() {
        boolean internetAccess = Global.isInternetAvailable(mContext);
        if (!internetAccess) {
            new MaterialDialog.Builder(mContext)
                    .title(R.string.internet_not_available)
                    .content(R.string.internet_not_available_summary)
                    .positiveText(R.string.okay)
                    .neutralText(R.string.open_wifi_settings)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mContext.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            dialog.dismiss();
                        }
                    })
                    .show();
            cancel(true);
            return;
        }

        mProgressDialog = new MaterialDialog.Builder(mContext)
                .title(R.string.fetching_hook_classes)
                .content(R.string.please_wait)
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

    protected void onPostExecute(String json) {
        mProgressDialog.dismiss();
        if (json == null) {
            ApplicationLogMaintainer.sendBroadcast(mContext, "Error fetching Hook file " + mHookUrl);
            Toast.makeText(mContext, R.string.error_fetching_file, Toast.LENGTH_LONG).show();
            return;
        }

        if (Global.writeStringToFile(mHookFile, json)) {
            mHookFile.setReadable(true, false);
            Toast.makeText(mContext, R.string.hooks_successfully_updated, Toast.LENGTH_LONG).show();
            ApplicationLogMaintainer.sendBroadcast(mContext, "Hooks Successfully updated");
        } else {
            Toast.makeText(mContext, R.string.unable_to_write_file, Toast.LENGTH_LONG).show();
            ApplicationLogMaintainer.sendBroadcast(mContext, "Error writing fetched file to : " + mHookFile.getAbsolutePath());
        }
    }

    static String getHooksUrl() {
        return HOOKS_URL;
    }

    static String getHooksFileName() {
        return HOOKS_FILE_NAME;
    }

    static String getDataDirectory(Context context) throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0);
        return packageInfo.applicationInfo.dataDir;
    }

    static String getHooksDirectoryPath(Context context) throws PackageManager.NameNotFoundException {
        File hookDirectory = new File(getDataDirectory(context), "files/");
        return hookDirectory.getAbsolutePath();
    }

    public static String getHooksFilePath(Context context) {
        try {
            File hookFile = new File(getHooksDirectoryPath(context), getHooksFileName());
            return hookFile.getAbsolutePath();
        } catch (Exception e) {
            ApplicationLogMaintainer.sendBroadcast(context, Global.getStackTrace(e));
        }
        return new File(context.getFilesDir().getAbsolutePath(), getHooksFileName()).getAbsolutePath();
    }

}
