package com.phantom.onetapvideodownload.downloader;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.phantom.onetapvideodownload.Video.Video;
import com.phantom.onetapvideodownload.Video.YoutubeVideo;
import com.phantom.onetapvideodownload.databasehandlers.DownloadDatabase;
import com.phantom.onetapvideodownload.databasehandlers.VideoDatabase;
import com.phantom.onetapvideodownload.downloader.downloadinfo.BrowserDownloadInfo;
import com.phantom.onetapvideodownload.downloader.downloadinfo.YoutubeDownloadInfo;

import java.io.File;

public class ProxyDownloadManager extends IntentService {
    private static final String TAG = "ProxyDownloadManager";
    private static final String ACTION_INSERT_DOWNLOAD = "com.phantom.onetapvideodownload.downloader.action.insert_download";
    private static final String ACTION_START_DOWNLOAD = "com.phantom.onetapvideodownload.downloader.action.start_download";

    private static final String EXTRA_VIDEO_ID = "com.phantom.onetapvideodownload.downloader.extra.video_id";
    private static final String EXTRA_FILENAME = "com.phantom.onetapvideodownload.downloader.extra.filename";
    private static final String EXTRA_VIDEO_ITAG = "com.phantom.onetapvideodownload.downloader.extra.video_itag";
    private static final String EXTRA_DOWNLOAD_LOCATION = "com.phantom.onetapvideodownload.downloader.extra.video_download_location";

    public ProxyDownloadManager() {
        super("ProxyDownloadManager");
    }

    public static void startActionYoutubeDownload(Context context, long videoId, String filename,
                                                  String downloadLocation, int itag) {
        Intent intent = getActionYoutubeDownload(context, videoId, filename, downloadLocation, itag);
        context.startService(intent);
    }

    public static Intent getActionYoutubeDownload(Context context, long videoId, String filename,
                                                  String downloadLocation, int itag) {
        Intent intent = new Intent(context, ProxyDownloadManager.class);
        intent.setAction(ACTION_START_DOWNLOAD);
        intent.putExtra(EXTRA_VIDEO_ID, videoId);
        intent.putExtra(EXTRA_FILENAME, filename);
        intent.putExtra(EXTRA_DOWNLOAD_LOCATION, downloadLocation);
        intent.putExtra(EXTRA_VIDEO_ITAG, itag);
        return intent;
    }
    public static void startActionBrowserDownload(Context context, long videoId, String filename,
                                                  String downloadLocation) {
        Intent intent = getActionBrowserDownload(context, videoId, filename, downloadLocation);
        context.startService(intent);
    }

    public static Intent getActionBrowserDownload(Context context, long videoId, String filename,
                                                  String downloadLocation) {
        Intent intent = new Intent(context, ProxyDownloadManager.class);
        intent.setAction(ACTION_START_DOWNLOAD);
        intent.putExtra(EXTRA_VIDEO_ID, videoId);
        intent.putExtra(EXTRA_FILENAME, filename);
        intent.putExtra(EXTRA_DOWNLOAD_LOCATION, downloadLocation);
        return intent;
    }

    public static void startActionYoutubeInserted(Context context, long videoId, String filename,
                                                  String downloadLocation, int itag) {
        Intent intent = new Intent(context, ProxyDownloadManager.class);
        intent.setAction(ACTION_INSERT_DOWNLOAD);
        intent.putExtra(EXTRA_VIDEO_ID, videoId);
        intent.putExtra(EXTRA_FILENAME, filename);
        intent.putExtra(EXTRA_DOWNLOAD_LOCATION, downloadLocation);
        intent.putExtra(EXTRA_VIDEO_ITAG, itag);
        context.startService(intent);
    }

    public static void startActionBrowserInserted(Context context, long videoId, String filename,
                                                  String downloadLocation) {
        Intent intent = new Intent(context, ProxyDownloadManager.class);
        intent.setAction(ACTION_INSERT_DOWNLOAD);
        intent.putExtra(EXTRA_VIDEO_ID, videoId);
        intent.putExtra(EXTRA_FILENAME, filename);
        intent.putExtra(EXTRA_DOWNLOAD_LOCATION, downloadLocation);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INSERT_DOWNLOAD.equals(action) || ACTION_START_DOWNLOAD.equals(action)) {
                long videoId = intent.getLongExtra(EXTRA_VIDEO_ID, -1);
                int itag = intent.getIntExtra(EXTRA_VIDEO_ITAG, -1);
                String downloadLocation = intent.getStringExtra(EXTRA_DOWNLOAD_LOCATION);
                String filename = intent.getStringExtra(EXTRA_FILENAME);
                long downloadId = -1;
                if (itag == -1) {
                    downloadId = insertBrowserVideo(videoId, filename, downloadLocation);
                } else {
                    downloadId = insertYoutubeVideo(videoId, filename, downloadLocation, itag);
                }

                if (downloadId == -1) {
                    return;
                }

                if (ACTION_INSERT_DOWNLOAD.equals(action)) {
                    Intent downloadManagerService = DownloadManager.getActionVideoInserted(downloadId);
                    startService(downloadManagerService);
                } else if (ACTION_START_DOWNLOAD.equals(action)) {
                    Intent downloadManagerService = DownloadManager.getActionVideoDownload(downloadId);
                    startService(downloadManagerService);
                }
            }
        }
    }

    private long insertBrowserVideo(long videoId, String filename, String downloadLocation) {
        VideoDatabase videoDatabase = VideoDatabase.getDatabase(this);
        Video video = videoDatabase.getVideo(videoId);
        if(video == null) {
            Log.e(TAG, "Video not found in database. Video ID: " + videoId);
            return -1;
        }

        BrowserDownloadInfo browserDownloadInfo = new BrowserDownloadInfo(this,
                filename,
                video.getUrl(),
                new File(downloadLocation, filename).getAbsolutePath());

        browserDownloadInfo.setPackageName(video.getPackageName());
        DownloadDatabase downloadDatabase = DownloadDatabase.getDatabase(this);
        return downloadDatabase.addDownload(browserDownloadInfo);
    }

    private long insertYoutubeVideo(long videoId, String filename, String downloadLocation, int itag) {
        VideoDatabase videoDatabase = VideoDatabase.getDatabase(this);
        YoutubeVideo video = (YoutubeVideo)videoDatabase.getVideo(videoId);
        if(video == null) {
            Log.e(TAG, "Video not found in database. Video ID: " + videoId);
            return -1;
        }

        filename += '.' + YoutubeVideo.getExtensionForItag(itag);
        YoutubeDownloadInfo youtubeDownloadInfo = new YoutubeDownloadInfo(this,
                filename,
                video.getVideoUrl(itag),
                new File(downloadLocation, filename).getAbsolutePath(),
                video.getParam(),
                itag);

        youtubeDownloadInfo.setPackageName(video.getPackageName());
        DownloadDatabase downloadDatabase = DownloadDatabase.getDatabase(this);
        long downloadId = downloadDatabase.addDownload(youtubeDownloadInfo);
        return downloadId;
    }
}
