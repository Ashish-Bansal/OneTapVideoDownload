package com.phantom.onetapvideodownload.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.utils.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApplicationUpdateNotification {
    private static final String UPDATE_JSON_URL = "https://raw.githubusercontent.com/Ashish-Bansal/OneTapVideoDownload/master/version.json";
    private static final String TAG = "UpdateNotification";
    private Context mContext;

    /*
    JSON Format
    {
        latest_version_code : 17,
        latest_version_name : "2.6",
        download_url : "url"
    }
     */

    public ApplicationUpdateNotification(Context context) {
        mContext = context;
    }

    public void checkForUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Global.isPlaystoreAvailable(mContext)) {
                    return;
                }

                JSONObject jsonObject = getLatestJson();
                if (jsonObject == null) {
                    return;
                }

                int installedApplicationVersion = installedApplicationVersion();
                final int latestApplicationVersion = latestApplicationVersion(jsonObject);
                if (latestApplicationVersion <= installedApplicationVersion) {
                    return;
                }

                final String url = getDownloadUrl(jsonObject);
                Activity activity = (Activity) mContext;
                if (url != null && activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String dialogContent = "Latest version : " + latestApplicationVersion;
                            MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                                    .title(R.string.new_update_available_title)
                                    .content(dialogContent)
                                    .canceledOnTouchOutside(false)
                                    .positiveText(R.string.download)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                                            viewIntent.setData(Uri.parse(url));
                                            mContext.startActivity(viewIntent);
                                        }
                                    })
                                    .show();

                            dialog.getActionButton(DialogAction.POSITIVE).setAllCaps(false);
                        }
                    });
                }
            }
        }).start();
    }

    public String getDownloadUrl(@NonNull JSONObject jsonObject) {
        try {
            return jsonObject.getString("latest_version_url");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int latestApplicationVersion(@NonNull JSONObject jsonObject) {
        try {
            return jsonObject.getInt("latest_version_code");
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public JSONObject getLatestJson() {
        String json = getResultFromUrl(getUpdateJsonUrl());
        if (json == null || json.isEmpty()) {
            Log.e("HERE", "HERE");
            return null;
        }

        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getResultFromUrl(@NonNull String stringUrl) {
        StringBuilder result = new StringBuilder();
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(stringUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
        } catch( Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result.toString();
    }

    public int installedApplicationVersion() {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getUpdateJsonUrl() {
        return UPDATE_JSON_URL;
    }
}
