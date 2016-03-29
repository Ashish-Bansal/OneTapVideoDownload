package com.phantom.onetapvideodownload.UriMediaChecker;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import com.phantom.onetapvideodownload.IpcService;
import com.phantom.onetapvideodownload.utils.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaChecker {
    private final Integer THREAD_POOL_SIZE = 4;
    private final String TAG = "MediaChecker";
    private ExecutorService mExecutorService;
    private static List<String> nonMediaSuffixList =  new ArrayList<>();
    private String mServerSocketAddress;
    private LocalSocket mLocalSocket;

    static {
        nonMediaSuffixList.add("html");
        nonMediaSuffixList.add("css");
        nonMediaSuffixList.add("js");
    }

    private class UriInfo implements Serializable {
        private String mUrl;
        private String mPackageName;

        UriInfo(String url, String packageName) {
            mUrl = url;
            mPackageName = packageName;
        }

        public String getUrl() {
            return mUrl;
        }

        public void setUrl(String url) {
            mUrl = url;
        }

        public String getPackageName() {
            return mPackageName;
        }
    }

    public static synchronized List<AbstractUriChecker> getWebsiteSpecificUriCheckers() {
        List<AbstractUriChecker> websiteSpecificUriCheckers = new ArrayList<>();
        websiteSpecificUriCheckers.add(new VimeoUriChecker());
        return websiteSpecificUriCheckers;
    }

    class UriMediaCheckThread implements Runnable {
        private UriInfo mUriInfo;
        public UriMediaCheckThread(UriInfo uriMediaInfo){
            mUriInfo = uriMediaInfo;
        }

        public void run() {
            String url = mUriInfo.getUrl();
            for (AbstractUriChecker uriChecker : getWebsiteSpecificUriCheckers()) {
                String directUrl = uriChecker.checkUrl(url);
                if (directUrl != null) {
                    mUriInfo.setUrl(directUrl);
                    writeUriInfoToSocket(mUriInfo);
                    return;
                }
            }

            for (String suffix : nonMediaSuffixList) {
                String filename = Global.getFilenameFromUrl(url);
                if (filename != null && filename.endsWith(suffix)) {
                    return;
                }
            }

            String contentType = Global.getResourceMime(url);
            if (contentType != null && contentType.startsWith("video")) {
                writeUriInfoToSocket(mUriInfo);
            }
        }
    }

    public MediaChecker(String serverSocketAddress) {
        mExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mServerSocketAddress = serverSocketAddress;
        mLocalSocket = new LocalSocket();
    }

    public void addUri(String url, String packageName) {
        UriInfo uriInfo = new UriInfo(url, packageName);
        UriMediaCheckThread uriMediaCheckThread = new UriMediaCheckThread(uriInfo);
        mExecutorService.execute(uriMediaCheckThread);
    }

    public synchronized void writeUriInfoToSocket(UriInfo uriInfo) {
        try {
            String json = getJson(uriInfo.getUrl(), uriInfo.getPackageName());
            mLocalSocket.connect(new LocalSocketAddress(mServerSocketAddress));
            OutputStream outputStream = mLocalSocket.getOutputStream();
            outputStream.write(json.getBytes(Charset.forName("UTF-8")));
            outputStream.close();
            mLocalSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getJson(String url, String packageName) {
        JSONObject json = new JSONObject();
        try {
            json.put(IpcService.EXTRA_URL, url);
            json.put(IpcService.EXTRA_PACKAGE_NAME, packageName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }
}
