package com.phantom.onetapvideodownload;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.util.Pair;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import at.huber.youtubeExtractor.YouTubeUriExtractor;
import at.huber.youtubeExtractor.YtFile;

public class IpcService extends IntentService {
    public static final String PREFS_NAME = "SavedUrls";
    private static final String PACKAGE_NAME = "com.phantom.onetapvideodownload";
    private static final String CLASS_NAME = "com.phantom.onetapvideodownload.IpcService";
    private static final String ACTION_SAVE_URI = "com.phantom.onetapvideodownload.action.saveurl";
    private static final String ACTION_SAVE_YOUTUBE_URI = "com.phantom.onetapvideodownload.action.saveyoutubeurl";
    private static final String EXTRA_URL = "com.phantom.onetapvideodownload.extra.url";
    private static final String EXTRA_PARAM_STRING = "com.phantom.onetapvideodownload.extra.url";
    private static final String EXTRA_METADATA = "com.phantom.onetapvideodownload.extra.metadata";
    private static final String YOUTUBE_URL_PREFIX = "http://youtube.com/watch?v=";
    private static final String LOG_TAG = "IpcService";
    private static final AtomicInteger notificationId = new AtomicInteger();
    private Handler mHandler = new Handler();

    public static void startSaveUrlAction(Context context, Uri uri) {
        Intent intent = new Intent(ACTION_SAVE_URI);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_URL, uri.toString());

        Calendar cal = Calendar.getInstance();
        intent.putExtra(EXTRA_METADATA, DateFormat.getDateTimeInstance().format(cal.getTime()));

        context.startService(intent);
    }

    public static void startSaveYoutubeVideoAction(Context context, String paramString) {
        Intent intent = new Intent(ACTION_SAVE_YOUTUBE_URI);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_PARAM_STRING, paramString);
        context.startService(intent);
    }

    public static void startSaveUrlAction(Context context, String url) {
        Intent intent = new Intent(ACTION_SAVE_URI);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_URL, url);

        Calendar cal = Calendar.getInstance();
        intent.putExtra(EXTRA_METADATA, DateFormat.getDateTimeInstance().format(cal.getTime()));

        context.startService(intent);
    }

    public IpcService() {
        super("IpcService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.e("IpcService", action);
            if (ACTION_SAVE_URI.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_URL);
                final String metadata = intent.getStringExtra(EXTRA_METADATA);
                handleActionSaveUrl(url, metadata);
            } else if (ACTION_SAVE_YOUTUBE_URI.equals(action)) {
                final String paramString = intent.getStringExtra(EXTRA_PARAM_STRING);
                handleActionSaveYoutubeVideo(paramString);
            }
        }
    }

    private void showNotification(String url) {
        showNotification(url, Url.getFilename(url));
    }

    private void showNotification(String url, String title) {
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

        if (CheckPreferences.headsUpEnabled(this)) {
            mBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        int possibleId = notificationId.getAndIncrement();
        if (possibleId >= CheckPreferences.notificationCountAllowed(this)) {
            possibleId = 0;
            notificationId.set(possibleId);
        }

        Intent downloadIntent = DownloadService.getActionDownload(url);
        PendingIntent downloadPendingIntent = PendingIntent.getService(this,
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

        Intent urlLogIntent = new Intent(this, UrlLogActivity.class);
        PendingIntent urlLogPendingIntent = PendingIntent.getActivity(this,
                possibleId,
                urlLogIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(urlLogPendingIntent);

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

    private void logUrl(String url, String metadata) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        int urlSavedCount = settings.getInt("count", 0);
        for(int i = 0; i < urlSavedCount*2; i+=2) {
            String savedUrl = settings.getString(Integer.toString(i + 1), "");
            if (savedUrl.equals(url)) {
                editor.putString(Integer.toString(i + 2), metadata);
                return;
            }
        }

        editor.putString(Integer.toString(urlSavedCount * 2 + 1), url);
        editor.putString(Integer.toString(urlSavedCount*2+2), metadata);
        editor.putInt("count", urlSavedCount + 1);
        editor.apply();
    }

    private void handleActionSaveUrl(String url, String metadata) {
        if (CheckPreferences.notificationsEnabled(this)) {
            showNotification(url);
        }

        if (CheckPreferences.loggingEnabled(this)) {
            logUrl(url, metadata);
        }
    }

    private void handleActionSaveYoutubeVideo(String paramString) {
        final Context context = this;
        if (!CheckPreferences.notificationsEnabled(context) && !CheckPreferences.loggingEnabled(context)) {
            Log.e(LOG_TAG, "Notifications and Logging is disabled");
            return;
        }

        YouTubeUriExtractor youtubeExtractor = new YouTubeUriExtractor(this) {
            @Override
            public void onUrisAvailable(String videoId, String videoTitle, SparseArray<YtFile> ytFiles) {
                if (ytFiles != null) {
                    YoutubeVideo video = new YoutubeVideo(videoTitle);
                    for(Pair p : YoutubeVideo.itagMapping) {
                        YtFile videoFormat = ytFiles.get(Integer.parseInt(p.first.toString()));
                        if (videoFormat == null) {
                            continue;
                        }
                        video.addFormat(videoFormat.getUrl(), Integer.parseInt(p.first.toString()));
                    }

                    if (CheckPreferences.notificationsEnabled(context)) {
                        showNotification(video.getBestVideoFormat().url, videoTitle);
                    }

                    Calendar cal = Calendar.getInstance();
                    String metadata = DateFormat.getDateTimeInstance().format(cal.getTime());

                    if (CheckPreferences.loggingEnabled(context)) {
                        logUrl(video.getBestVideoFormat().url, metadata);
                    }

                    Log.e(LOG_TAG, video.getBestAudioFormat().url);
                    Log.e(LOG_TAG, video.getBestVideoFormat().url);
                } else {
                    Log.e(LOG_TAG, "URLs are empty");
                }
            }
        };

        Log.e(LOG_TAG, YOUTUBE_URL_PREFIX + paramString);
        youtubeExtractor.setParseDashManifest(true);
        youtubeExtractor.execute(YOUTUBE_URL_PREFIX + paramString);
    }
}
