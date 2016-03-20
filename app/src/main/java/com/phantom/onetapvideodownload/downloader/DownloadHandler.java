package com.phantom.onetapvideodownload.downloader;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.phantom.onetapvideodownload.MainActivity;
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
        setStatus(DownloadInfo.Status.Downloading);
        mContext.startService(DownloadManager.getActionUpdateUi());
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
                    setStatus(DownloadInfo.Status.NetworkProblem);
                    writeToDatabase();
                }

                @Override
                public void onResponse(Call call, Response response) {
                    BufferedOutputStream bufferedOutputStream = null;

                    try {
                        InputStream inputStream = response.body().byteStream();
                        if (response.isSuccessful()) {
                            if (!started()) {
                                mDownloadInfo.setContentLength(response.body().contentLength());
                            }

                            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file, started()));
                            byte data[] = new byte[1024 * 4];
                            int count;
                            while ((count = inputStream.read(data)) != -1) {
                                bufferedOutputStream.write(data, 0, count);
                                mDownloadInfo.addDownloadedLength(count);
                                long currentTime = System.currentTimeMillis();
                                if (currentTime - lastWriteTime > 8000L) {
                                    writeToDatabase();
                                    lastWriteTime = currentTime;
                                }
                            }

                            bufferedOutputStream.close();
                            inputStream.close();
                            setStatus(DownloadInfo.Status.Completed);
                            writeToDatabase();
                            DownloadManager downloadManager = (DownloadManager) mContext;
                            downloadManager.updateUi();
                            showNotification();
                        } else {
                            Log.v(TAG, "Status Code : " + response.code());
                        }
                    } catch (IOException e) {
                        Log.e("DownloadService", "Exception : ", e);
                        try {
                            if (bufferedOutputStream != null) {
                                bufferedOutputStream.close();
                            }
                        } catch (IOException ioException) {
                                ioException.printStackTrace();
                        }
                    } finally {
                        writeToDatabase();
                    }
                }
            });
        } else {
            setStatus(DownloadInfo.Status.NetworkNotAvailable);
            writeToDatabase();
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
        mBuilder.setAutoCancel(true);
        mBuilder.setOnlyAlertOnce(false);

        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                mNotificationId.get(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
        mNotifyManager.notify(mNotificationId.getAndIncrement(), mBuilder.build());
    }

    private String getNotificationContent() {
        return "Download Finished";
    }

    public Collection<String> getOptions() {
        return mDownloadInfo.getOptions();
    }

    public int findIdByString(Context context, String string) {
        return mDownloadInfo.findIdByString(context, string);
    }

    public boolean handleOptionClicks(Context context, int resId) {
        // Used Activity context instead of ApplicationContext
        boolean success = mDownloadInfo.handleOptionClicks(context, resId);
        if (!success) {
            success = true;
            switch (resId) {
                case R.string.resume:
                    startDownload();
                    break;
                case R.string.pause:
                    stopDownload();
                    break;
                // ToDo: Implement the resume and pause functionality
                // case R.string.details:
                default:
                    success = false;
            }
        }

        return success;
    }

    public long getDownloadedLength() {
        return mDownloadInfo.getDownloadedLength();
    }

    public boolean started() {
        return getDownloadedLength() != 0;
    }

    public void stopDownload() {
        setStatus(DownloadInfo.Status.Stopped);
        writeToDatabase();
        if (mCall != null) {
            mCall.cancel();
        }
    }

    public void writeToDatabase() {
        mDownloadInfo.writeToDatabase();
    }
}
