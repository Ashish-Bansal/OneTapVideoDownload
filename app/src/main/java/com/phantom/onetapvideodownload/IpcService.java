package com.phantom.onetapvideodownload;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.phantom.onetapvideodownload.Video.BrowserVideo;
import com.phantom.onetapvideodownload.Video.Video;
import com.phantom.onetapvideodownload.Video.YoutubeVideo;
import com.phantom.onetapvideodownload.databasehandlers.VideoDatabase;
import com.phantom.onetapvideodownload.utils.Global;
import com.phantom.onetapvideodownload.utils.Invokable;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class IpcService extends Service implements Invokable<Video, Integer> {
    private static final String PACKAGE_NAME = "com.phantom.onetapvideodownload";
    private static final String CLASS_NAME = "com.phantom.onetapvideodownload.IpcService";
    private static final String ACTION_SAVE_BROWSER_VIDEO = "com.phantom.onetapvideodownload.action.saveurl";
    private static final String ACTION_SAVE_YOUTUBE_VIDEO = "com.phantom.onetapvideodownload.action.saveyoutubeurl";
    private static final String EXTRA_URL = "com.phantom.onetapvideodownload.extra.url";
    private static final String EXTRA_PARAM_STRING = "com.phantom.onetapvideodownload.extra.url";
    private static final String EXTRA_PACKAGE_NAME = "com.phantom.onetapvideodownload.extra.package_name";
    private static final String EXTRA_METADATA = "com.phantom.onetapvideodownload.extra.metadata";
    private static final String TAG = "IpcService";
    private static final AtomicInteger notificationId = new AtomicInteger();
    private Handler mHandler = new Handler();
    private final IBinder mBinder = new LocalBinder();

    public static void startSaveUrlAction(Context context, Uri uri, String packageName) {
        Intent intent = new Intent(ACTION_SAVE_BROWSER_VIDEO);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_URL, uri.toString());
        intent.putExtra(EXTRA_PACKAGE_NAME, packageName);

        Calendar cal = Calendar.getInstance();
        intent.putExtra(EXTRA_METADATA, DateFormat.getDateTimeInstance().format(cal.getTime()));

        context.startService(intent);
    }

    public static void startSaveYoutubeVideoAction(Context context, String paramString) {
        Intent intent = new Intent(ACTION_SAVE_YOUTUBE_VIDEO);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_PARAM_STRING, paramString);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public IpcService getServiceInstance() {
            return IpcService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.e("IpcService", action);
            if (ACTION_SAVE_BROWSER_VIDEO.equals(action)) {
                String url = intent.getStringExtra(EXTRA_URL);
                String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
                if (url == null || url.isEmpty() || Global.getFilenameFromUrl(url) == null) {
                    return START_STICKY;
                }

                Video video = new BrowserVideo(this, url);
                if (packageName != null) {
                    video.setPackageName(packageName);
                } else {
                    Log.e(TAG, "Package name is invalid");
                }
                handleActionSaveBrowserVideo(video);
            } else if (ACTION_SAVE_YOUTUBE_VIDEO.equals(action)) {
                final String paramString = intent.getStringExtra(EXTRA_PARAM_STRING);
                handleActionSaveYoutubeVideo(paramString);
            }
        }

        return START_STICKY;
    }

    private void showNotification(String url, String title, long videoId) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.one_tap_small);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.one_tap_large));
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(url);
        mBuilder.setAutoCancel(true);
        mBuilder.setOnlyAlertOnce(false);

        // 0 if vibration is disabled.
        long vibrationAmount = CheckPreferences.vibrationAmount(this);
        mBuilder.setVibrate(new long[] {0, vibrationAmount});

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (CheckPreferences.headsUpEnabled(this) && currentApiVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            mBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        int possibleId = notificationId.getAndIncrement();
        if (possibleId >= CheckPreferences.notificationCountAllowed(this)) {
            possibleId = 0;
            notificationId.set(possibleId);
        }

        Intent downloadIntent = new Intent(this, MainActivity.class);
        downloadIntent.putExtra("videoId", videoId);

        PendingIntent downloadPendingIntent = PendingIntent.getActivity(this,
                possibleId,
                downloadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.download, "Download", downloadPendingIntent);

        Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        viewIntent.setDataAndType(Uri.parse(url), "video/mp4");
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this,
                possibleId,
                viewIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.play, "Play", viewPendingIntent);

        Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        PendingIntent openPendingIntent = PendingIntent.getActivity(this,
                possibleId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.browser, "Open", openPendingIntent);

        mBuilder.setContentIntent(downloadPendingIntent);

        final NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        final int id = possibleId;
        notificationmanager.notify(id, mBuilder.build());
        int delayInSeconds = CheckPreferences.notificationDismissTime(this);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                notificationmanager.cancel(id);
            }
        }, delayInSeconds*1000);
    }

    private long saveUrlToDatabase(Video video) {
        VideoDatabase videoDatabase = VideoDatabase.getDatabase(this);
        return videoDatabase.addOrUpdateVideo(video);
    }

    private void handleActionSaveBrowserVideo(Video video) {
        long id = saveUrlToDatabase(video);
        if (CheckPreferences.notificationsEnabled(this)) {
            showNotification(video.getUrl(), Global.getFilenameFromUrl(video.getUrl()), id);
        }
    }

    private void handleActionSaveYoutubeVideo(String paramString) {
        final Context context = this;
        if (!CheckPreferences.notificationsEnabled(context) && !CheckPreferences.loggingEnabled(context)) {
            Log.e(TAG, "Notifications and Logging is disabled");
            return;
        }
        YoutubeParserProxy.startParsing(this, paramString, this);
    }

    @Override
    public Integer invoke(Video video) {
        if (video != null) {
            YoutubeVideo youtubeVideo = (YoutubeVideo)video;
            youtubeVideo.setPackageName("com.google.android.youtube");
            long id = saveUrlToDatabase(youtubeVideo);
            if (CheckPreferences.notificationsEnabled(this)) {
                showNotification(youtubeVideo.getBestVideoFormat().url, youtubeVideo.getTitle(), id);
            }
        }
        return 0;
    }
}
