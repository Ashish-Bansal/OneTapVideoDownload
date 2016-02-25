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

    private static final String EXTRA_VIDEO_ID = "com.phantom.onetapvideodownload.downloader.extra.video_id";
    private static final String EXTRA_FILENAME = "com.phantom.onetapvideodownload.downloader.extra.filename";
    private static final String EXTRA_VIDEO_ITAG = "com.phantom.onetapvideodownload.downloader.extra.video_itag";
    private static final String EXTRA_DOWNLOAD_LOCATION = "com.phantom.onetapvideodownload.downloader.extra.video_download_location";

    public ProxyDownloadManager() {
        super("ProxyDownloadManager");
    }

    public static void startActionYoutubeDownload(Context context, long videoId, String filename,
                                                  String downloadLocation, int itag) {
        Intent intent = new Intent(context, ProxyDownloadManager.class);
        intent.setAction(ACTION_INSERT_DOWNLOAD);
        intent.putExtra(EXTRA_VIDEO_ID, videoId);
        intent.putExtra(EXTRA_FILENAME, filename);
        intent.putExtra(EXTRA_DOWNLOAD_LOCATION, downloadLocation);
        intent.putExtra(EXTRA_VIDEO_ITAG, itag);
        context.startService(intent);
    }

    public static void startActionDownload(Context context, long videoId, String filename,
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
            if (ACTION_INSERT_DOWNLOAD.equals(action)) {
                long videoId = intent.getLongExtra(EXTRA_VIDEO_ID, -1);
                int itag = intent.getIntExtra(EXTRA_VIDEO_ITAG, -1);
                String downloadLocation = intent.getStringExtra(EXTRA_DOWNLOAD_LOCATION);
                String filename = intent.getStringExtra(EXTRA_FILENAME);
                if (itag == -1) {
                    downloadBrowserVideo(videoId, filename, downloadLocation);
                } else {
                    downloadYoutubeVideo(videoId, filename, downloadLocation, itag);
                }
            }
        }
    }

    private void downloadBrowserVideo(long videoId, String filename, String downloadLocation) {
        VideoDatabase videoDatabase = VideoDatabase.getDatabase(this);
        Video video = videoDatabase.getVideo(videoId);
        if(video == null) {
            Log.e(TAG, "Video not found in database. Video ID: " + videoId);
            return;
        }

        BrowserDownloadInfo browserDownloadInfo = new BrowserDownloadInfo(this,
                filename,
                video.getUrl(),
                new File(downloadLocation, filename).getAbsolutePath());

        browserDownloadInfo.setPackageName(video.getPackageName());
        DownloadDatabase downloadDatabase = DownloadDatabase.getDatabase(this);
        long downloadId = downloadDatabase.addDownload(browserDownloadInfo);
        Intent downloadManagerService = DownloadManager.getActionVideoDownload(downloadId);
        startService(downloadManagerService);
    }

    private void downloadYoutubeVideo(long videoId, String filename, String downloadLocation, int itag) {
        VideoDatabase videoDatabase = VideoDatabase.getDatabase(this);
        YoutubeVideo video = (YoutubeVideo)videoDatabase.getVideo(videoId);
        if(video == null) {
            Log.e(TAG, "Video not found in database. Video ID: " + videoId);
            return;
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
        Intent downloadManagerService = DownloadManager.getActionVideoDownload(downloadId);
        startService(downloadManagerService);
    }
}
