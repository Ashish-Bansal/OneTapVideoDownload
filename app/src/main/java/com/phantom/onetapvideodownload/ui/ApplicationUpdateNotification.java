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
import com.google.firebase.crash.FirebaseCrash;
import com.phantom.onetapvideodownload.R;
import com.phantom.utils.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class ApplicationUpdateNotification {
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

    ApplicationUpdateNotification(Context context) {
        mContext = context;
    }

    void checkForUpdate() {
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

    private String getDownloadUrl(@NonNull JSONObject jsonObject) {
        try {
            return jsonObject.getString("latest_version_url");
        } catch (JSONException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
            return null;
        }
    }

    private int latestApplicationVersion(@NonNull JSONObject jsonObject) {
        try {
            return jsonObject.getInt("latest_version_code");
        } catch (JSONException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
            return 0;
        }
    }

    private JSONObject getLatestJson() {
        String json = getResultFromUrl(getUpdateJsonUrl());
        try {
            return new JSONObject(json);
        } catch (Exception e) {
            Log.e(TAG, "Exception while creating JSON Object", e);
            return null;
        }
    }

    private String getResultFromUrl(@NonNull String stringUrl) {
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
            FirebaseCrash.report(e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result.toString();
    }

    private int installedApplicationVersion() {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
            return -1;
        }
    }

    private String getUpdateJsonUrl() {
        return UPDATE_JSON_URL;
    }
}
