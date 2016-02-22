package com.phantom.onetapvideodownload.downloader;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.AppPermissions;
import com.phantom.onetapvideodownload.MainActivity;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.databasehandlers.DownloadDatabase;
import com.phantom.onetapvideodownload.downloader.downloadinfo.DownloadInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DownloadManager extends Service {
    private static final String PACKAGE_NAME = "com.phantom.onetapvideodownload";
    private static final String CLASS_NAME = "com.phantom.onetapvideodownload.downloader.DownloadManager";
    private static final String ACTION_DOWNLOAD = "com.phantom.onetapvideodownload.action.download";
    private static final String ACTION_START = "com.phantom.onetapvideodownload.action.start";
    private static final String EXTRA_DOWNLOAD_ID = "com.phantom.onetapvideodownload.extra.download_id";
    private static final int STORAGE_PERMISSION_NOTIFICATION_ID = 100;
    private static List<Pair<Long, DownloadHandler>> mDownloadHandlers = new ArrayList<>();
    private final IBinder mBinder = new LocalBinder();
    private static List<ServiceCallbacks> serviceCallbacks = new ArrayList<>();
    private final String TAG = "DownloadManager";
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyManager;
    private final static Integer mNotificationId = 20;
    private final static Long NOTIFICATION_UPDATE_WAIT_TIME = 2500L;

    public interface ServiceCallbacks {
        void onDownloadAdded();
    }

    @Override
    public void onCreate() {
        DownloadDatabase downloadDatabase = DownloadDatabase.getDatabase(this);
        List<DownloadInfo> downloadInfos = downloadDatabase.getAllDownloads();
        for (DownloadInfo downloadInfo : downloadInfos) {
            DownloadHandler downloadHandler = new DownloadHandler(this, downloadInfo);
            mDownloadHandlers.add(Pair.create(downloadInfo.getDatabaseId(), downloadHandler));
        }

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
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

    public static Intent getActionVideoDownload(long downloadId) {
        Intent intent = new Intent(ACTION_DOWNLOAD);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
        return intent;
    }

    public static Intent getActionStartService() {
        Intent intent = new Intent(ACTION_START);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        return intent;
    }

    public Integer getDownloadCount() {
        return mDownloadHandlers.size();
    }

    public void registerCallbacks(ServiceCallbacks object) {
        Log.e(TAG, "Registering Callback " + object.getClass().getName());
        serviceCallbacks.add(object);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            System.out.println(action);
            if (ACTION_DOWNLOAD.equals(action)) {
                final long downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == -1) {
                    return START_REDELIVER_INTENT;
                }

                handleActionDownload(downloadId);
            }
        }

        return START_REDELIVER_INTENT;
    }

    @TargetApi(23)
    public boolean checkPermissionGranted(AppPermissions permission) {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion > android.os.Build.VERSION_CODES.LOLLIPOP) {
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
        for (ServiceCallbacks sc : serviceCallbacks) {
            if (sc == null) {
                continue;
            }

            Log.e(TAG, "Calling onDownloadAdded callback method " + sc.getClass().getName());
            sc.onDownloadAdded();
        }

        showNotification();
        startNotificationUpdateThread();
    }

    public void startNotificationUpdateThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (getDownloadCountByStatus(DownloadInfo.Status.Downloading) != 0) {
                    updateNotification();
                    try {
                        Thread.sleep(NOTIFICATION_UPDATE_WAIT_TIME);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Notification Update Thread Interrupted Exception");
                        e.printStackTrace();
                    }
                }
                updateNotification();
            }
        }).start();
    }

    public void showNotification() {
        mBuilder.setSmallIcon(R.drawable.download);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mBuilder.setContentTitle(getResources().getString(R.string.app_name));
        mBuilder.setContentText(getNotificationContent());
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);
        mBuilder.setOnlyAlertOnce(false);
        mBuilder.setProgress(100, 0, false);
        mNotifyManager.notify(mNotificationId, mBuilder.build());
    }

    public synchronized void updateNotification() {
        int progress = getDownloadsAverageProgress();
        if (progress == 100) {
            mNotifyManager.cancel(mNotificationId);
        } else {
            mBuilder.setContentText(getNotificationContent());
            mBuilder.setProgress(100, progress, false);
            mNotifyManager.notify(mNotificationId, mBuilder.build());
        }
    }

    public int getDownloadCountByStatus(DownloadInfo.Status status) {
        Integer count = 0;
        for (Pair<Long, DownloadHandler> p : mDownloadHandlers) {
            if (p.second.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    public String getNotificationContent() {
        return "Downloading " + getDownloadCountByStatus(DownloadInfo.Status.Downloading) + " Files"
                + " : " + getDownloadsAverageProgress() + "%";
    }

    public int getDownloadsAverageProgress() {
        int downloadCount = 0, progressSum = 0;
        for (Pair<Long, DownloadHandler> p : mDownloadHandlers) {
            if (p.second.getStatus() == DownloadInfo.Status.Downloading) {
                progressSum += p.second.getProgress();
                downloadCount++;
            }
        }

        if (downloadCount == 0) {
            return 100;
        }

        return progressSum / downloadCount;

    }

    public int getDownloadProgress(long id) {
        DownloadHandler downloadHandler;
        for (Pair<Long, DownloadHandler> p : mDownloadHandlers) {
            if (p.first == id) {
                downloadHandler = p.second;
                return downloadHandler.getProgress();
            }
        }

        return -1;
    }

    public long getContentLength(long id) {
        DownloadHandler downloadHandler;
        for (Pair<Long, DownloadHandler> p : mDownloadHandlers) {
            if (p.first == id) {
                downloadHandler = p.second;
                return downloadHandler.getContentLength();
            }
        }

        return -1;
    }

    public String getFilename(int index) {
        if (index >= mDownloadHandlers.size()) {
            Log.e(TAG, "Requested index is larger that available downloads size.");
        }

        return mDownloadHandlers.get(index).second.getFilename();
    }

    public String getUrl(int index) {
        if (index >= mDownloadHandlers.size()) {
            Log.e(TAG, "Requested index is larger that available downloads size.");
        }

        return mDownloadHandlers.get(index).second.getUrl();
    }

    public Collection<String> getOptions(int index) {
        return mDownloadHandlers.get(index).second.getOptions();
    }

    public MaterialDialog.ListCallback getOptionCallback(int index) {
        return mDownloadHandlers.get(index).second.getOptionCallback();
    }
}
