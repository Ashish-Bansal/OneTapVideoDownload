package com.phantom.onetapvideodownload.downloader;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.downloader.downloadinfo.DownloadInfo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadHandler {
    private Context mContext;
    private DownloadInfo mDownloadInfo;
    NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyManager;
    private Integer mNotificationId, mProgress = 0;
    private long mContentLength = 0, mDownloadedLength = 0;
    private static AtomicInteger newNotificationId = new AtomicInteger();
    private final static String TAG = "DownloadHandler";
    private static long mLastNotifcationUpdateTime = System.currentTimeMillis();

    DownloadHandler(Context context, DownloadInfo downloadInfo) {
        mContext = context;
        mDownloadInfo = downloadInfo;
        mNotificationId = newNotificationId.getAndIncrement();
    }

    public void startDownload() {
        showNotification();
        File filePath = new File(mDownloadInfo.getDownloadLocation());
        downloadFile(mDownloadInfo.getUrl(), filePath);
        mDownloadInfo.setStatus(DownloadInfo.Status.Downloading);
    }

    public DownloadInfo getDownloadInfo() {
        return mDownloadInfo;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void downloadFile(String url, final File file) {
        if (isNetworkAvailable()) {
            OkHttpClient Client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Call call = Client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mDownloadInfo.setStatus(DownloadInfo.Status.NetworkProblem);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        InputStream in = response.body().byteStream();
                        mContentLength = response.body().contentLength();
                        if (response.isSuccessful()) {
                            Log.v(TAG, file.getAbsolutePath());
                            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(file));
                            byte data[] = new byte[1024 * 4];
                            int count;
                            while ((count = in.read(data)) != -1) {
                                mDownloadedLength += count;
                                bw.write(data, 0, count);
                                long currentTime = System.currentTimeMillis();
                                if (currentTime - mLastNotifcationUpdateTime > 2500L) {
                                    mProgress = (int) (mDownloadedLength * 100 / mContentLength);
                                    updateNotification(mProgress);
                                }
                            }
                            bw.close();
                            in.close();
                            mDownloadInfo.setStatus(DownloadInfo.Status.Completed);
                        } else {
                            mDownloadInfo.setStatus(DownloadInfo.Status.WriteFailed);
                        }
                    } catch (IOException e) {
                        Log.e("DownloadService", "expection is ", e);
                    }
                }
            });
        } else {
            mDownloadInfo.setStatus(DownloadInfo.Status.NetworkNotAvailable);
        }
    }

    public void showNotification() {
        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.download);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));
        mBuilder.setContentTitle(mContext.getResources().getString(R.string.app_name));
        mBuilder.setContentText("CONTENT");
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);
        mBuilder.setOnlyAlertOnce(false);
        mBuilder.setProgress(100, 0, false);
        mNotifyManager.notify(mNotificationId, mBuilder.build());
    }

    public void updateNotification(int progress) {
        mLastNotifcationUpdateTime = System.currentTimeMillis();
        if (progress == 100) {
            mBuilder.setContentText("Download complete").setProgress(0, 0, false);
        } else {
            mBuilder.setProgress(100, progress, false);
        }

        mNotifyManager.notify(mNotificationId, mBuilder.build());
    }
}
