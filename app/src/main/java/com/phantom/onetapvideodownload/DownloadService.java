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

import com.phantom.onetapvideodownload.Video.Video;
import com.phantom.onetapvideodownload.Video.YoutubeVideo;

import java.io.File;

public class DownloadService extends IntentService {
    private static final String PACKAGE_NAME = "com.phantom.onetapvideodownload";
    private static final String CLASS_NAME = "com.phantom.onetapvideodownload.DownloadService";
    private static final String ACTION_DOWNLOAD = "com.phantom.onetapvideodownload.action.download";
    private static final String EXTRA_VIDEO_ID = "com.phantom.onetapvideodownload.extra.video_id";
    private static final String EXTRA_VIDEO_ITAG = "com.phantom.onetapvideodownload.extra.itag";
    private static final int STORAGE_PERMISSION_NOTIFICATION_ID = 100;

    public static Intent getActionDownload(long videoId) {
        Intent intent = new Intent(ACTION_DOWNLOAD);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_VIDEO_ID, videoId);
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
                final long videoId = intent.getLongExtra(EXTRA_VIDEO_ID, -1);
                if (videoId == -1) {
                    return;
                }

                handleActionDownload(videoId);
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

    private void handleActionDownload(long id) {
        DatabaseHandler databaseHandler = DatabaseHandler.getDatabase(this);
        Video video = databaseHandler.getVideo(id);
        if (video == null) {
            return;
        }

        if (!checkPermissionGranted(AppPermissions.External_Storage_Permission)) {
            requestPermission(AppPermissions.External_Storage_Permission);
        }

        String filename = video.getTitle();
        if (filename.isEmpty()) {
            filename = "videoplayback.mp4";
        }

        if (video instanceof YoutubeVideo) {
            filename += ".mp4";
        }

        DownloadManager dm;
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Request request = new Request(Uri.parse(video.getUrl()));
        request.setTitle(filename);
        request.setDescription(video.getUrl());
        request.allowScanningByMediaScanner();

        File downloadDirectory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), filename);

        request.setDestinationUri(Uri.fromFile(downloadDirectory));
        dm.enqueue(request);
    }
}
