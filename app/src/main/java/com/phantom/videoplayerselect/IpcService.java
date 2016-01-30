package com.phantom.videoplayerselect;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.text.DateFormat;
import java.util.Calendar;

public class IpcService extends IntentService {
    public static final String PREFS_NAME = "SavedUrls";
    private static final String PACKAGE_NAME = "com.phantom.videoplayerselect";
    private static final String CLASS_NAME = "com.phantom.videoplayerselect.IpcService";
    private static final String ACTION_SAVE_URI = "com.phantom.videoplayerselect.action.saveurl";
    private static final String EXTRA_URL = "com.phantom.videoplayerselect.extra.url";
    private static final String EXTRA_METADATA = "com.phantom.videoplayerselect.extra.metadata";

    public static void startSaveUrlAction(Context context, Uri uri) {
        Intent intent = new Intent(ACTION_SAVE_URI);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(EXTRA_URL, uri.toString());

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
            }
        }
    }

    private void handleActionSaveUrl(String url, String metadata) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.copy);
        mBuilder.setContentTitle("Any Video Downloader");
        mBuilder.setContentText(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setData(Uri.parse(url));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, Intent.FILL_IN_ACTION);
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationmanager.notify(0, mBuilder.build());

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

        editor.putString(Integer.toString(urlSavedCount*2+1), url);
        editor.putString(Integer.toString(urlSavedCount*2+2), metadata);
        editor.putInt("count", urlSavedCount+1);
        editor.apply();
    }

}
