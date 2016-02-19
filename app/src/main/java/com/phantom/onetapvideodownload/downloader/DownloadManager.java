package com.phantom.onetapvideodownload.downloader;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.phantom.onetapvideodownload.AppPermissions;
import com.phantom.onetapvideodownload.MainActivity;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.databasehandlers.DownloadDatabase;
import com.phantom.onetapvideodownload.downloader.downloadinfo.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

public class DownloadManager extends IntentService {
    private static final String PACKAGE_NAME = "com.phantom.onetapvideodownload";
    private static final String CLASS_NAME = "com.phantom.onetapvideodownload.downloader.DownloadManager";
    private static final String ACTION_DOWNLOAD = "com.phantom.onetapvideodownload.action.download";
    private static final String EXTRA_DOWNLOAD_ID = "com.phantom.onetapvideodownload.extra.download_id";
    private static final int STORAGE_PERMISSION_NOTIFICATION_ID = 100;
    private static List<Pair<Long, DownloadHandler>> mDownloadHandlers = new ArrayList<>();
    private final IBinder mBinder = new LocalBinder();
    private static List<ServiceCallbacks> serviceCallbacks = new ArrayList<>();
    private final String TAG = "DownloadManager";
    public interface ServiceCallbacks {
        void onDownloadAdded();
    }

    @Override
    public void onCreate () {
        DownloadDatabase downloadDatabase = DownloadDatabase.getDatabase(this);
        List<DownloadInfo> downloadInfos = downloadDatabase.getAllDownloads();
        for(DownloadInfo downloadInfo : downloadInfos) {
            DownloadHandler downloadHandler = new DownloadHandler(this, downloadInfo);
            mDownloadHandlers.add(Pair.create(downloadInfo.getDatabaseId(), downloadHandler));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public DownloadManager getServiceInstance() {
            return DownloadManager.this;
        }
    }

    public static Intent getActionVideoDownload(long videoId) {
        Intent intent = new Intent(ACTION_DOWNLOAD);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_DOWNLOAD_ID, videoId);
        return intent;
    }

    public DownloadManager() {
        super("DownloadService");
    }

    public Integer getDownloadCount() {
        return mDownloadHandlers.size();
    }

    public DownloadInfo getDownloadInfo(int position) {
        assert(position < mDownloadHandlers.size());
        return mDownloadHandlers.get(position).second.getDownloadInfo();
    }

    public void registerCallbacks(ServiceCallbacks object) {
        Log.e(TAG, "Registering Callback " + object.getClass().getName());
        serviceCallbacks.add(object);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            System.out.println(action);
            if (ACTION_DOWNLOAD.equals(action)) {
                final long videoId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1);
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
        DownloadDatabase downloadDatabase = DownloadDatabase.getDatabase(this);
        DownloadInfo downloadInfo = downloadDatabase.getDownload(id);
        DownloadHandler downloadHandler = new DownloadHandler(this, downloadInfo);
        mDownloadHandlers.add(Pair.create(id, downloadHandler));
        if (checkPermissionGranted(AppPermissions.External_Storage_Permission)) {
            downloadHandler.startDownload();
        } else {
            requestPermission(AppPermissions.External_Storage_Permission);
        }

        Log.e(TAG, "ServiceCallback Size : " + serviceCallbacks.size());
        for(ServiceCallbacks sc : serviceCallbacks) {
            if (sc == null) {
                continue;
            }

            Log.e(TAG, "Calling onDownloadAdded callback method " + sc.getClass().getName());
            sc.onDownloadAdded();
        }
    }

}
