package com.phantom.onetapvideodownload.downloader;

import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.downloader.downloadinfo.DownloadInfo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadHandler {
    private Context mContext;
    private DownloadInfo mDownloadInfo;
    private final static String TAG = "DownloadHandler";
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyManager;
    private final static AtomicInteger mNotificationId = new AtomicInteger(150);
    private static long lastWriteTime = System.currentTimeMillis();
    private Call mCall;

    DownloadHandler(Context context, DownloadInfo downloadInfo) {
        mContext = context;
        mDownloadInfo = downloadInfo;
        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext);
    }

    public void startDownload() {
        File filePath = new File(mDownloadInfo.getDownloadLocation());
        downloadFile(mDownloadInfo.getUrl(), filePath);
        mDownloadInfo.setStatus(DownloadInfo.Status.Downloading);
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
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Range", "bytes=" + getDownloadedLength() + "-")
                    .build();
            mCall = Client.newCall(request);
            mCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mDownloadInfo.setStatus(DownloadInfo.Status.NetworkProblem);
                    mDownloadInfo.writeToDatabase();
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        InputStream in = response.body().byteStream();
                        mDownloadInfo.setContentLength(response.body().contentLength());
                        if (response.isSuccessful()) {
                            Log.v(TAG, file.getAbsolutePath());
                            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(file, started()));
                            byte data[] = new byte[1024 * 4];
                            int count;
                            while ((count = in.read(data)) != -1) {
                                bw.write(data, 0, count);
                                mDownloadInfo.addDownloadedLength(count);
                                long currentTime = System.currentTimeMillis();
                                if (currentTime - lastWriteTime > 8000L) {
                                    mDownloadInfo.writeToDatabase();
                                    lastWriteTime = currentTime;
                                }
                            }
                            bw.close();
                            in.close();
                            mDownloadInfo.setStatus(DownloadInfo.Status.Completed);
                            mDownloadInfo.writeToDatabase();
                            DownloadManager downloadManager = (DownloadManager) mContext;
                            downloadManager.updateUi();
                            showNotification();
                        } else {
                            Log.v(TAG, "Status Code : " + response.code());
                        }
                    } catch (IOException e) {
                        Log.e("DownloadService", "Exception : ", e);
                        mDownloadInfo.setStatus(DownloadInfo.Status.WriteFailed);
                        mDownloadInfo.writeToDatabase();
                    }
                }
            });
        } else {
            mDownloadInfo.setStatus(DownloadInfo.Status.NetworkNotAvailable);
            mDownloadInfo.writeToDatabase();
        }
    }

    public Integer getProgress() {
        return mDownloadInfo.getProgress();
    }

    public DownloadInfo.Status getStatus() {
        return mDownloadInfo.getStatus();
    }

    public void setStatus(DownloadInfo.Status status) {
        mDownloadInfo.setStatus(status);
    }

    public long getContentLength() {
        return mDownloadInfo.getContentLength();
    }

    public String getFilename() {
        return mDownloadInfo.getFilename();
    }

    public String getUrl() {
        return mDownloadInfo.getUrl();
    }

    public Drawable getPackageDrawable() {
        try {
            Drawable d = mContext.getPackageManager().getApplicationIcon(mDownloadInfo.getPackageName());
            return d;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private void showNotification() {
        mBuilder.setSmallIcon(R.drawable.download);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));
        mBuilder.setContentTitle(getFilename());
        mBuilder.setContentText(getNotificationContent());
        mBuilder.setAutoCancel(false);
        mBuilder.setOnlyAlertOnce(false);
        mNotifyManager.notify(mNotificationId.getAndIncrement(), mBuilder.build());
    }

    private String getNotificationContent() {
        return "Download Finished";
    }

    public Collection<String> getOptions() {
        return mDownloadInfo.getOptions();
    }

    public MaterialDialog.ListCallback getOptionCallback() {
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                Context context = dialog.getContext();
                int resId = mDownloadInfo.findIdByString(context, (String) text);
                if (resId == -1) {
                    return;
                }

                // Used Activity context instead of ApplicationContext
                if (!mDownloadInfo.handleOptionClicks(context, resId)) {
                    switch (resId) {
                        case R.string.resume:
                            startDownload();
                            break;
                        case R.string.pause:
                            stopDownload();
                            break;
                        // ToDo: Implement the resume and pause functionality
                        // case R.string.details:
                    }
                }
            }
        };
    }

    public long getDownloadedLength() {
        return mDownloadInfo.getDownloadedLength();
    }

    public boolean started() {
        return getDownloadedLength() != 0;
    }

    public void stopDownload() {
        setStatus(DownloadInfo.Status.Stopped);
        if (mCall != null) {
            mCall.cancel();
        }
    }
}
