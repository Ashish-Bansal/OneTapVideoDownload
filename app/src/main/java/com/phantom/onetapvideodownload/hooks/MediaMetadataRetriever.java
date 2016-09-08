package com.phantom.onetapvideodownload.hooks;

import android.content.Context;
import android.net.Uri;

import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.IpcService;
import com.phantom.utils.Global;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MediaMetadataRetriever implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        XC_MethodHook methodHook = new XC_MethodHook() {
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam hookParams) throws Throwable {
                Context context = Global.getContext();
                String packageName = context.getPackageName();
                Uri uri = (Uri) hookParams.args[1];
                if (uri.toString().startsWith("http") || uri.toString().startsWith("ftp")) {
                    ApplicationLogMaintainer.sendBroadcast(Global.getContext(), uri.toString());
                    IpcService.startSaveUrlAction(context, uri,  packageName);
                }
            }
        };

        // void setDataSource (Context context, Uri uri)
        Class mediaMetadataRetriever = XposedHelpers.findClass("android.media.MediaMetadataRetriever", lpparam.classLoader);
        Object[] objects = new Object[] { Context.class, Uri.class, methodHook };
        XposedHelpers.findAndHookMethod(mediaMetadataRetriever, "setDataSource", objects);
    }
}
