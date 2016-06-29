package com.phantom.onetapvideodownload.downloader;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.utils.enums.AppPermissions;
import com.phantom.onetapvideodownload.ui.MainActivity;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.databasehandlers.DownloadDatabase;
import com.phantom.onetapvideodownload.downloader.downloadinfo.DownloadInfo;
import com.phantom.onetapvideodownload.utils.OnDownloadChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DownloadManager extends Service {
    private static final String PACKAGE_NAME = "com.phantom.onetapvideodownload";
    private static final String CLASS_NAME = "com.phantom.onetapvideodownload.downloader.DownloadManager";
    private static final String ACTION_START_DOWNLOAD = "com.phantom.onetapvideodownload.action.start_download";
    private static final String ACTION_DOWNLOAD_INSERTED = "com.phantom.onetapvideodownload.action.download_inserted";
    private static final String ACTION_UPDATE_UI = "com.phantom.onetapvideodownload.action.update_ui";
    private static final String ACTION_START_SERVICE = "com.phantom.onetapvideodownload.action.start";
    private static final String ACTION_REMOVE_DOWNLOAD = "com.phantom.onetapvideodownload.action.remove";
    private static final String ACTION_RESUME_DOWNLOAD = "com.phantom.onetapvideodownload.action.resume";
    private static final String ACTION_STOP_DOWNLOAD = "com.phantom.onetapvideodownload.action.stop";
    private static final String ACTION_DELETE_DOWNLOAD = "com.phantom.onetapvideodownload.action.delete";
    private static final String EXTRA_DOWNLOAD_ID = "com.phantom.onetapvideodownload.extra.download_id";
    private static final String TAG = "DownloadManager";
    private final int STORAGE_PERMISSION_NOTIFICATION_ID = 100;
    private List<Pair<Long, DownloadHandler>> mDownloadHandlers = new ArrayList<>();
    private final IBinder mBinder = new LocalBinder();
    private List<OnDownloadChangeListener> onDownloadChangeListeners = new ArrayList<>();
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyManager;
    private Notification mNotification;
    private final Integer mNotificationId = 20;
    private final Long NOTIFICATION_UPDATE_WAIT_TIME = 2500L;
    private Thread mUiUpdateThread;

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
        Intent intent = new Intent(ACTION_START_DOWNLOAD);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
        return intent;
    }

    public static Intent getActionVideoInserted(long downloadId) {
        Intent intent = new Intent(ACTION_DOWNLOAD_INSERTED);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
        return intent;
    }

    public static Intent getActionStartService() {
        Intent intent = new Intent(ACTION_START_SERVICE);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        return intent;
    }

    public static Intent getActionUpdateUi() {
        Intent intent = new Intent(ACTION_UPDATE_UI);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        return intent;
    }

    public static Intent getActionResumeDownload(long downloadId) {
        Intent intent = new Intent(ACTION_RESUME_DOWNLOAD);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
        return intent;
    }

    public static Intent getActionStopDownload(long downloadId) {
        Intent intent = new Intent(ACTION_STOP_DOWNLOAD);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
        return intent;
    }

    public static Intent getActionRemoveDownload(long downloadId) {
        Intent intent = new Intent(ACTION_REMOVE_DOWNLOAD);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
        return intent;
    }

    public static Intent getActionDeleteDownload(long downloadId) {
        Intent intent = new Intent(ACTION_DELETE_DOWNLOAD);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
        return intent;
    }

    public Integer getDownloadCount() {
        return mDownloadHandlers.size();
    }

    public void addOnDownloadChangeListener(OnDownloadChangeListener object) {
        Log.e(TAG, "Registering DownloadChangeListener " + object.getClass().getName());
        onDownloadChangeListeners.add(object);
    }

    public void removeOnDownloadChangeListener(OnDownloadChangeListener object) {
        Log.e(TAG, "UnRegistering DownloadChangeListener " + object.getClass().getName());
        onDownloadChangeListeners.remove(object);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            System.out.println(action);
            if (ACTION_START_DOWNLOAD.equals(action)) {
                final long downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == -1) {
                    return START_STICKY;
                }

                handleActionDownloadInserted(downloadId);
                if (checkPermissionGranted(AppPermissions.External_Storage_Permission)) {
                    int index = getDownloadByDatabaseId(downloadId);
                    if (index == -1) throw new AssertionError("Index should not be -1");
                    DownloadHandler downloadHandler = mDownloadHandlers.get(index).second;
                    downloadHandler.startDownload();
                }

                showNotification();
                startUiUpdateThread();
            } else if (ACTION_DOWNLOAD_INSERTED.equals(action)) {
                final long downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == -1) {
                    return START_STICKY;
                }

                handleActionDownloadInserted(downloadId);
            } else if (ACTION_UPDATE_UI.equals(action)) {
                showNotification();
                startUiUpdateThread();
            } else if(ACTION_RESUME_DOWNLOAD.equals(action)) {
                final long downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == -1) {
                    return START_STICKY;
                }

                handleActionResumeDownload(downloadId);
            } else if(ACTION_STOP_DOWNLOAD.equals(action)) {
                final long downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == -1) {
                    return START_STICKY;
                }

                handleActionStopDownload(downloadId);
            } else if(ACTION_DELETE_DOWNLOAD.equals(action)) {
                final long downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == -1) {
                    return START_STICKY;
                }

                handleActionDeleteDownload(downloadId);
            } else if(ACTION_REMOVE_DOWNLOAD.equals(action)) {
                final long downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == -1) {
                    return START_STICKY;
                }

                handleActionRemoveDownload(downloadId);
            }
        }

        return START_STICKY;
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

    private void handleActionDownloadInserted(long id) {
        DownloadDatabase downloadDatabase = DownloadDatabase.getDatabase(this);
        DownloadInfo downloadInfo = downloadDatabase.getDownload(id);
        if (downloadInfo == null) {
            Log.e(TAG, "Download Info null in handleActionDownload for id : " + id);
            return;
        }

        DownloadHandler downloadHandler = new DownloadHandler(this, downloadInfo);
        mDownloadHandlers.add(Pair.create(id, downloadHandler));
        if (!checkPermissionGranted(AppPermissions.External_Storage_Permission)) {
            requestPermission(AppPermissions.External_Storage_Permission);
        }

        Log.e(TAG, "onDownloadChangeListeners Size : " + onDownloadChangeListeners.size());

        onDownloadChangeListeners.removeAll(Collections.singleton(null));
        for (OnDownloadChangeListener onDownloadChangeListener : onDownloadChangeListeners) {
            Log.e(TAG, "Calling onDownloadAdded callback method " + onDownloadChangeListener.getClass().getName());
            onDownloadChangeListener.onDownloadAdded();
        }
    }

    private void handleActionRemoveDownload(long id) {
        int index = getDownloadByDatabaseId(id);
        if (index == -1) {
            return;
        }

        DownloadHandler downloadHandler = mDownloadHandlers.get(index).second;
        downloadHandler.removeDownloadFromList();
        removeDownloadHandler(index);
    }

    private void handleActionDeleteDownload(long id) {
        int index = getDownloadByDatabaseId(id);
        if (index == -1) {
            return;
        }

        DownloadHandler downloadHandler = mDownloadHandlers.get(index).second;
        downloadHandler.deleteDownloadFromStorage();
        removeDownloadHandler(index);
    }

    private void handleActionResumeDownload(long id) {
        int index = getDownloadByDatabaseId(id);
        if (index == -1) {
            return;
        }

        DownloadHandler downloadHandler = mDownloadHandlers.get(index).second;
        downloadHandler.startDownload();
    }

    private void handleActionStopDownload(long id) {
        int index = getDownloadByDatabaseId(id);
        if (index == -1) {
            return;
        }

        DownloadHandler downloadHandler = mDownloadHandlers.get(index).second;
        downloadHandler.stopDownload();
    }

    public int getDownloadByDatabaseId(long databaseId) {
        for(int i = 0; i < mDownloadHandlers.size(); i++) {
            DownloadHandler downloadHandler = mDownloadHandlers.get(i).second;
            if (downloadHandler.getDatabaseId() == databaseId) {
                return i;
            }
        }

        return -1;
    }

    public void startUiUpdateThread() {
        if (mUiUpdateThread == null || !mUiUpdateThread.isAlive()) {
            mUiUpdateThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (getDownloadCountByStatus(DownloadInfo.Status.Downloading) != 0) {
                        updateNotification();
                        emitOnDownloadInfoUpdated();
                        try {
                            Thread.sleep(NOTIFICATION_UPDATE_WAIT_TIME);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Notification Update Thread Interrupted Exception");
                            e.printStackTrace();
                        }
                    }

                    updateUi();
                }
            });
            mUiUpdateThread.start();
        }
    }

    public void updateUi() {
        updateNotification();
        emitOnDownloadInfoUpdated();
    }

    private synchronized void emitOnDownloadInfoUpdated() {
        onDownloadChangeListeners.removeAll(Collections.singleton(null));
        for (OnDownloadChangeListener onDownloadChangeListener : onDownloadChangeListeners) {
            Log.e(TAG, "Calling onDownloadInfoUpdated callback method " + onDownloadChangeListener.getClass().getName());
            onDownloadChangeListener.onDownloadInfoUpdated();
        }
    }

    private synchronized void showNotification() {
        mBuilder.setSmallIcon(R.drawable.download);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mBuilder.setContentTitle(getResources().getString(R.string.app_name));
        mBuilder.setContentText(getNotificationContent());
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);
        mBuilder.setOnlyAlertOnce(false);
        mBuilder.setProgress(100, 0, false);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                mNotificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
        mNotification = mBuilder.build();
        mNotifyManager.notify(mNotificationId, mNotification);
        startForeground(mNotificationId, mNotification);
    }

    private synchronized void updateNotification() {
        int progress = getDownloadsAverageProgress();
        if (progress == 100) {
            mNotifyManager.cancel(mNotificationId);
            stopForeground(true);
        } else {
            mBuilder.setContentText(getNotificationContent());
            mBuilder.setProgress(100, progress, false);
            mNotification = mBuilder.build();
            mNotifyManager.notify(mNotificationId, mNotification);
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

    public int getDownloadProgress(int index) {
        if (index >= mDownloadHandlers.size()) {
            Log.e(TAG, "Requested index is larger that available downloads size.");
        }

        return mDownloadHandlers.get(index).second.getProgress();
    }

    public DownloadInfo.Status getStatus(int index) {
        if (index >= mDownloadHandlers.size()) {
            Log.e(TAG, "Requested index is larger that available downloads size.");
        }

        return mDownloadHandlers.get(index).second.getStatus();
    }

    public long getContentLength(int index) {
        if (index >= mDownloadHandlers.size()) {
            Log.e(TAG, "Requested index is larger that available downloads size.");
        }

        return mDownloadHandlers.get(index).second.getContentLength();
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

    public MaterialDialog.ListCallback getOptionCallback(final int index) {
        final DownloadHandler downloadHandler = mDownloadHandlers.get(index).second;
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                Context context = dialog.getContext();
                int resId = downloadHandler.findIdByString(context, (String) text);
                if (resId == -1) {
                    return;
                }

                downloadHandler.handleOptionClicks(context, resId);
            }
        };
    }

    public Drawable getPackageDrawable(int index) {
        if (index >= mDownloadHandlers.size()) {
            Log.e(TAG, "Requested index is larger that available downloads size.");
        }

        return mDownloadHandlers.get(index).second.getPackageDrawable();
    }

    public void removeDownloadHandler(int index) {
        mDownloadHandlers.remove(index);
        emitOnDownloadInfoUpdated();
    }

    public void removeDownloadByIndex(int index) {
        if (mDownloadHandlers.size() > index) {
            Log.e(TAG,"RemovingDownloadByIndex " + mDownloadHandlers.get(index).second.getFilename());
            DownloadHandler downloadHandler = mDownloadHandlers.get(index).second;
            downloadHandler.removeDownloadFromList();
            removeDownloadHandler(index);
        }
    }
}
