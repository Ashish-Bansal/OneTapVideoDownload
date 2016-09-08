package com.phantom.onetapvideodownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Environment;

import com.phantom.onetapvideodownload.utils.Global;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ApplicationLogMaintainer extends BroadcastReceiver {
    private static final String PACKAGE_NAME = "com.phantom.onetapvideodownload";
    private static final String CLASS_NAME = PACKAGE_NAME + ".ApplicationLogMaintainer";
    public static final String EXTRA_MESSAGE = PACKAGE_NAME + ".extra.message";

    public ApplicationLogMaintainer() {
    }

    public static void sendBroadcast(Context context, String message) {
        Intent intent = new Intent(context, ApplicationLogMaintainer.class);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }

    public static String getLogFilePath() {
        return new File(Environment.getExternalStorageDirectory(), ".OneTapVideoDownload/Error.txt").getPath();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        String logMessage = intent.getStringExtra(EXTRA_MESSAGE);
        if (logMessage.equals("One Tap Initialized")) {
            try {
                if (Global.getDirectorySize(new File(Environment.getExternalStorageDirectory(), ".OneTapVideoDownload/Error.txt")) > 10000) {
                    startErrorLog(logMessage);
                } else {
                    setError("---------------------------");
                    setError(logMessage);
                }
            } catch (Exception e) {
            }

            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                setError("OneTapVideoDownload Version " + pInfo.versionName);
            } catch (Exception e) {
            }
        } else {
            setError(logMessage);
        }
    }

    void setError(String status) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
            String time = sdf.format(new Date());

            status = time + " - " + status;

            File root = new File(Environment.getExternalStorageDirectory(), ".OneTapVideoDownload");
            if (!root.exists()) {
                root.mkdirs();
            }
            File file = new File(root, "Error.txt");
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            buf.newLine();
            buf.append(status);
            buf.close();
        } catch (IOException e) {

        }
    }

    void startErrorLog(String status) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
            String time = sdf.format(new Date());

            status = time + " - " + status;

            File root = new File(Environment.getExternalStorageDirectory(), ".OneTapVideoDownload");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "Error.txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(status);
            writer.flush();
            writer.close();
        } catch (IOException e) {

        }
    }
}
