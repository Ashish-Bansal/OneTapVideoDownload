package com.phantom.onetapvideodownload.UriMediaChecker;

import android.content.Context;
import android.net.Uri;

import com.phantom.onetapvideodownload.IpcService;
import com.phantom.onetapvideodownload.Video.BrowserVideo;
import com.phantom.onetapvideodownload.Video.Video;
import com.phantom.onetapvideodownload.Video.YoutubeVideo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaChecker {
    private final Integer THREAD_POOL_SIZE = 4;
    private final String TAG = "MediaChecker";
    private ExecutorService mExecutorService;
    private static List<String> nonMediaSuffixList =  new ArrayList<>();
    private Context mContext;

    static {
        nonMediaSuffixList.add("html");
        nonMediaSuffixList.add("css");
        nonMediaSuffixList.add("js");
    }

    public List<AbstractUriChecker> getWebsiteSpecificUriCheckers() {
        List<AbstractUriChecker> websiteSpecificUriCheckers = new ArrayList<>();
        websiteSpecificUriCheckers.add(new VimeoUriChecker(mContext));
        websiteSpecificUriCheckers.add(new YoutubeUriChecker(mContext));
        return websiteSpecificUriCheckers;
    }

    class UriMediaCheckThread implements Runnable {
        private String mUrl, mPackageName;
        public UriMediaCheckThread(String url, String packageName){
            mUrl = url;
            mPackageName = packageName;
        }

        public void run() {
            for (AbstractUriChecker uriChecker : getWebsiteSpecificUriCheckers()) {
                Video video = uriChecker.checkUrl(mUrl);
                if (video != null) {
                    video.setPackageName(mPackageName);
                    startSaveUrlAction(video);
                    return;
                }
            }
        }
    }

    public MediaChecker(Context context) {
        mExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mContext = context;
    }

    public void addUri(String url, String packageName) {
        UriMediaCheckThread uriMediaCheckThread = new UriMediaCheckThread(url, packageName);
        mExecutorService.execute(uriMediaCheckThread);
    }

    public void startSaveUrlAction(Video video) {
        if (video instanceof BrowserVideo) {
            IpcService.startSaveUrlAction(mContext, Uri.parse(video.getUrl()), video.getPackageName());
        } else if (video instanceof YoutubeVideo){
            YoutubeVideo youtubeVideo = (YoutubeVideo) video;
            IpcService.startSaveYoutubeVideoAction(mContext, youtubeVideo.getParam());
        }
    }
}
