package com.phantom.onetapvideodownload;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import java.io.File;

public class DownloadService extends IntentService {
    private static final String PACKAGE_NAME = "com.phantom.onetapvideodownload";
    private static final String CLASS_NAME = "com.phantom.onetapvideodownload.DownloadService";
    private static final String ACTION_DOWNLOAD = "com.phantom.onetapvideodownload.action.download";
    private static final String EXTRA_URL = "com.phantom.onetapvideodownload.extra.url";
    private static final int STORAGE_PERMISSION_NOTIFICATION_ID = 100;

    public static Intent getActionDownload(String url) {
        Intent intent = new Intent(ACTION_DOWNLOAD);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_URL, url);
        return intent;
    }

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            System.out.println(action);
            if (ACTION_DOWNLOAD.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_URL);
                handleActionDownload(url);
            }
        }
    }

    @TargetApi(23)
    public boolean checkPermissionGranted(AppPermissions permission) {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion > android.os.Build.VERSION_CODES.LOLLIPOP){
            if (ContextCompat.checkSelfPermission(this, permission.getPermissionName())
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void requestPermission(AppPermissions permission) {
        String title = "Storage permission required";
        String description = "Please enable this permission and restart your download.";
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.one_tap_small);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.one_tap_large));
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(description);
        mBuilder.setAutoCancel(true);
        mBuilder.setOnlyAlertOnce(true);

        Intent permissionIntent;

        switch (permission) {
            case External_Storage_Permission:
                permissionIntent = new Intent(this, MainActivity.class);
                PendingIntent permissionPendingIntent = PendingIntent.getActivity(this, 0, permissionIntent, 0);
                mBuilder.setContentIntent(permissionPendingIntent);
                mBuilder.addAction(R.drawable.transparent, "Enable", permissionPendingIntent);
                break;
            default:
                return;
        }

        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationmanager.notify(STORAGE_PERMISSION_NOTIFICATION_ID, mBuilder.build());
    }

    private void handleActionDownload(String url) {
        DownloadManager dm;
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Request request = new Request(Uri.parse(url));
        File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        request.setDestinationUri(Uri.fromFile(downloadDirectory));
        if (checkPermissionGranted(AppPermissions.External_Storage_Permission)) {
            dm.enqueue(request);
        } else {
            requestPermission(AppPermissions.External_Storage_Permission);
        }
    }
}
