package com.phantom.onetapvideodownload;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class IpcService extends IntentService {
    public static final String PREFS_NAME = "SavedUrls";
    private static final String PACKAGE_NAME = "com.phantom.onetapvideodownload";
    private static final String CLASS_NAME = "com.phantom.onetapvideodownload.IpcService";
    private static final String ACTION_SAVE_URI = "com.phantom.onetapvideodownload.action.saveurl";
    private static final String ACTION_SAVE_YOUTUBE_URI = "com.phantom.onetapvideodownload.action.saveyoutubeurl";
    private static final String EXTRA_URL = "com.phantom.onetapvideodownload.extra.url";
    private static final String EXTRA_PARAM_STRING = "com.phantom.onetapvideodownload.extra.url";
    private static final String EXTRA_METADATA = "com.phantom.onetapvideodownload.extra.metadata";
    private static final String API_URL = "http://xposed-youtube.herokuapp.com/api/info?url=http://youtube.com/watch?v=";
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
        Log.e(LOG_TAG, ACTION_SAVE_YOUTUBE_URI);
        Intent intent = new Intent(ACTION_SAVE_YOUTUBE_URI);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_PARAM_STRING, paramString);

        Calendar cal = Calendar.getInstance();
        intent.putExtra(EXTRA_METADATA, DateFormat.getDateTimeInstance().format(cal.getTime()));

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
                final String metadata = intent.getStringExtra(EXTRA_METADATA);
                handleActionSaveYoutubeVideo(paramString, metadata);
            }
        }
    }

    private void showNotification(String url) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.one_tap_small);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.one_tap_large));
        mBuilder.setContentTitle(Url.getFilename(url));
        mBuilder.setContentText(url);
        mBuilder.setAutoCancel(true);
        mBuilder.setOnlyAlertOnce(true);

        Intent downloadIntent = DownloadService.getActionDownload(url);
        PendingIntent downloadPendingIntent = PendingIntent.getService(this, 0, downloadIntent, 0);
        mBuilder.addAction(R.drawable.download, "Download", downloadPendingIntent);

        Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        viewIntent.setDataAndType(Uri.parse(url), "video/mp4");
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);
        mBuilder.addAction(R.drawable.play, "Play", viewPendingIntent);

        Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        PendingIntent openPendingIntent = PendingIntent.getActivity(this, 0, openIntent, 0);
        mBuilder.addAction(R.drawable.browser, "Open", openPendingIntent);

        Intent urlLogIntent = new Intent(this, UrlLogActivity.class);
        PendingIntent urlLogPendingIntent = PendingIntent.getActivity(this, 0, urlLogIntent, 0);
        mBuilder.setContentIntent(urlLogPendingIntent);

        final NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int possibleId = notificationId.getAndIncrement();
        if (possibleId >= CheckPreferences.notificationCountAllowed(this)) {
            possibleId = 0;
            notificationId.set(possibleId);
        }

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

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private void handleActionSaveYoutubeVideo(String paramString, String metadata) {
        URL url;
        HttpURLConnection urlConnection;
        try {
            url = new URL(API_URL + paramString);
            urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode != 200) {
                return;
            }

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String json = convertStreamToString(in);
            urlConnection.disconnect();

            // Parsing JSON
            // JSON Format : https://youtube-dl-api-server.readthedocs.org/en/latest/api.html
            JSONObject jsonObject = new JSONObject(json);
            JSONObject info = jsonObject.getJSONObject("info");
            String videoTitle = info.getString("title");

            YoutubeVideo video = new YoutubeVideo(videoTitle);
            Log.e(LOG_TAG, videoTitle);
            JSONArray formats = info.getJSONArray("formats");
            if (formats.length() == 0) {
                return;
            }

            for(int i=0; i < formats.length(); i++) {
                JSONObject format = formats.getJSONObject(i);
                try {
                    String extension = format.getString("ext");
                    String videoUrl = format.getString("url");
                    String formatDescription = format.getString("format");
                    video.addFormat(extension, videoUrl, formatDescription);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (video.urlsForbidden()) {
                Log.e(LOG_TAG, "URL forbidden");
                return;
            }

            if (CheckPreferences.notificationsEnabled(this)) {
                showNotification(video.getVideoUrl(0));
            }

            if (CheckPreferences.loggingEnabled(this)) {
                logUrl(video.getVideoUrl(0), metadata);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
