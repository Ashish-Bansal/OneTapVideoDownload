package com.phantom.onetapvideodownload;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

public class DownloadService extends IntentService {
    private static final String PACKAGE_NAME = "com.phantom.onetapvideodownload";
    private static final String CLASS_NAME = "com.phantom.onetapvideodownload.DownloadService";
    private static final String ACTION_DOWNLOAD = "com.phantom.onetapvideodownload.action.download";
    private static final String EXTRA_URL = "com.phantom.onetapvideodownload.extra.url";

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

    private void handleActionDownload(String url) {
        DownloadManager dm;
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Request request = new Request(Uri.parse(url));
        File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        request.setDestinationUri(Uri.fromFile(downloadDirectory));
        dm.enqueue(request);
    }

}
