package com.phantom.onetapfacebookmodule;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.IpcService;
import com.phantom.utils.Global;

import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class FacebookHook implements IXposedHookLoadPackage {
    private static final String FACEBOOK_PACKAGE_NAME = "com.facebook.katana";
    private static final String VIDEO_PLAY_REQUEST = "com.facebook.exoplayer.ipc.VideoPlayRequest";
    private static final String VIDEO_URI_SOURCE_TYPE = "com.facebook.exoplayer.ipc.VideoPlayRequest$VideoUriSourceType";
    private static final String ONE_TAP_FACEBOOK_MODULE_PACKAGE_NAME = "com.phantom.onetapfacebookmodule";

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(FACEBOOK_PACKAGE_NAME)) {
            return;
        }

        final Context context = Global.getContext();
        ApplicationLogMaintainer.sendBroadcast(context, "One Tap Facebook Hook Initialized");
        if (!Global.isPackageInstalled(context, ONE_TAP_FACEBOOK_MODULE_PACKAGE_NAME)) {
            ApplicationLogMaintainer.sendBroadcast(context, "One Tap Facebook Module not installed");
            return;
        }

        final XC_MethodHook methodHook = new XC_MethodHook() {
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam hookParams) throws Throwable {
                try {
                    Uri uri = (Uri) hookParams.args[0];
                    ApplicationLogMaintainer.sendBroadcast(context, "Facebook Main URL : " + uri.toString());
                    String stringUri = "";
                    Set<String> keys = uri.getQueryParameterNames();
                    for (String key : keys) {
                        String value = uri.getQueryParameter(key);
                        if (Uri.parse(value).getScheme() != null) {
                            stringUri = value;
                        }
                    }

                    ApplicationLogMaintainer.sendBroadcast(context, "Facebook Query Parameter URL : " + stringUri);
                    IpcService.startSaveUrlAction(context, stringUri, FACEBOOK_PACKAGE_NAME);
                } catch (Exception e) {
                    ApplicationLogMaintainer.sendBroadcast(context, Global.getStackTrace(e));
                }
            }
        };

        String versionName = context.getPackageManager().getPackageInfo(FACEBOOK_PACKAGE_NAME,
                PackageManager.GET_META_DATA).versionName;

        ApplicationLogMaintainer.sendBroadcast(context, "Facebook package version : " + versionName);

        if (!Global.isClassPresent(lpparam.classLoader, VIDEO_PLAY_REQUEST)
                || !Global.isClassPresent(lpparam.classLoader, VIDEO_URI_SOURCE_TYPE)) {
            ApplicationLogMaintainer.sendBroadcast(context, "Facebook Hook class not found. ToDo: Update hooks.");
            return;
        }

        Class mainClass = XposedHelpers.findClass(VIDEO_PLAY_REQUEST, lpparam.classLoader);
        Class subClass = XposedHelpers.findClass(VIDEO_URI_SOURCE_TYPE, lpparam.classLoader);

        Object [] objects = new Object[] {
                Uri.class,
                String.class,
                String.class,
                Uri.class,
                String.class,
                ParcelFileDescriptor.class,
                subClass,
                methodHook
        };

        XposedHelpers.findAndHookConstructor(mainClass,  objects);
        ApplicationLogMaintainer.sendBroadcast(context, "Facebook Hook successful");
    }

}
