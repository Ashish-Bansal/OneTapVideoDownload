package com.phantom.onetapvideodownload;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

public class XposedChecker {
    private static Context mContext;

    public static void showXposedNotFound(Context context) {
        mContext = context;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(mContext.getResources().getString(R.string.xposed_not_found_title));
        builder.setMessage(mContext.getResources().getString(R.string.xposed_not_found_description));

        builder.setPositiveButton("Read More", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String url = "http://repo.xposed.info/module/de.robv.android.xposed.installer";
                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setData(Uri.parse(url));
                mContext.startActivity(viewIntent);
            }
        });

        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exit(dialog);
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                exit(dialog);
            }
        });

        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    public static void exit(DialogInterface dialog) {
        dialog.dismiss();
        ((Activity)mContext).finish();
    }

    public static boolean isXposedInstalled(Context context) {
        String packageName = "de.robv.android.xposed.installer";
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
