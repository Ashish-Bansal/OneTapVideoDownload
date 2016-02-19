package com.phantom.onetapvideodownload.downloader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.phantom.onetapvideodownload.downloader.downloadinfo.DownloadInfo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadHandler {
    private Context mContext;
    private DownloadInfo mDownloadInfo;

    DownloadHandler(Context context, DownloadInfo downloadInfo) {
        mContext = context;
        mDownloadInfo = downloadInfo;
    }

    public void startDownload() {
        File filePath = new File(mDownloadInfo.getDownloadLocation());
        downloadFile(mDownloadInfo.getUrl(), filePath);
    }

    public DownloadInfo getDownloadInfo() {
        return mDownloadInfo;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void downloadFile(String url, final File file) {
        if(isNetworkAvailable()) {
            OkHttpClient Client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Call call = Client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
//                    "There are network problems"
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        InputStream in = response.body().byteStream();
                        if (response.isSuccessful()) {
                            Log.v("DownloadService", file.getAbsolutePath());
                            BufferedOutputStream bw  = new BufferedOutputStream(new FileOutputStream(file));
                            byte data[] = new byte[1024 * 4];
                            int count;
                            while ((count = in.read(data)) != -1) {
                                bw.write(data, 0, count);
                            }
                            bw.close();
                            in.close();
                        } else {
//                            alertUser();
                        }
                    }
                    catch (IOException e) {
                        Log.e("DownloadService", "expection is ", e);
                    }
                }
            });
        } else{
//            Toast.makeText(this,"The network is unavailable",Toast.LENGTH_LONG).show();
        }
    }
}
