package com.phantom.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HookClassNamesFetcher {
    private Context mContext;
    private String mHookUrl;
    private File mHookFile;
    private MaterialDialog mProgressDialog;

    public HookClassNamesFetcher(Context context, String url, File destinationPath) {
        mContext = context;
        mHookUrl = url;
        mHookFile = destinationPath;
    }

    public void updateHooks() {
        boolean internetAccess = Global.isInternetAvailable(mContext);
        if (!internetAccess) {
            new MaterialDialog.Builder(mContext)
                    .title(R.string.internet_not_available)
                    .content(R.string.internet_not_available_summary)
                    .positiveText(R.string.okay)
                    .neutralText(R.string.open_wifi_settings)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mContext.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            dialog.dismiss();
                        }
                    })
                    .show();
            return;
        }

        mProgressDialog = new MaterialDialog.Builder(mContext)
                .title(R.string.fetching_hook_classes)
                .content(R.string.please_wait)
                .progress(true, 0)
                .show();
        fetchFile();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void createToastOnUiThread(final Integer stringId) {
        final Activity activity = (Activity) mContext;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, stringId, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchFile() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(mHookUrl)
                .build();

        final Call mCall = client.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ApplicationLogMaintainer.sendBroadcast(mContext, "Error fetching file : onFailure()" + mHookUrl);
                createToastOnUiThread(R.string.error_fetching_file);
                dismissProgressDialog();
            }

            @Override
            public void onResponse(Call call, Response response) {
                String result = null;
                try {
                    if (response.isSuccessful()) {
                        InputStream inputStream = response.body().byteStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        String line = bufferedReader.readLine();
                        result = line;
                        while((line = bufferedReader.readLine()) != null) {
                            result += line;
                        }
                        response.body().close();
                    } else {
                        ApplicationLogMaintainer.sendBroadcast(mContext, "Error fetching file : " + mHookUrl);
                        ApplicationLogMaintainer.sendBroadcast(mContext, "Status Code : " + response.code());
                    }
                } catch (IOException e) {
                    Log.e("DownloadService", "Exception : ", e);
                }

                dismissProgressDialog();
                if (result != null && Global.writeStringToFile(mHookFile, result)) {
                    mHookFile.setReadable(true, false);
                    createToastOnUiThread(R.string.hooks_successfully_fetched);
                    ApplicationLogMaintainer.sendBroadcast(mContext, "Hooks Successfully updated");
                } else {
                    ApplicationLogMaintainer.sendBroadcast(mContext, "Error writing fetched file to : " + mHookFile.getAbsolutePath());
                }
            }
        });
    }


}
