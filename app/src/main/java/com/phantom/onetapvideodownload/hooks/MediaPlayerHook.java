package com.phantom.onetapvideodownload.hooks;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.phantom.onetapvideodownload.IpcService;

import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MediaPlayerHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        XC_MethodHook methodHook = new XC_MethodHook() {
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam hookParams) throws Throwable {
                Context context = (Context) hookParams.args[0];
                Intent intent = new Intent(context, IpcService.class);
                context.startService(intent);

                String packageName = context.getPackageName();
                Uri uri = (Uri) hookParams.args[1];
                if (uri.toString().startsWith("http") || uri.toString().startsWith("ftp")) {
                    XposedBridge.log(uri.toString());
                    IpcService.startSaveUrlAction(context, uri, packageName);
                }
            }
        };

        Class mediaPlayerClass = XposedHelpers.findClass("android.media.MediaPlayer", lpparam.classLoader);

        //void setDataSource(Context context, Uri uri, Map<String, String> headers);
        Object[] objects = new Object[] { Context.class, Uri.class, Map.class, methodHook};
        XposedHelpers.findAndHookMethod(mediaPlayerClass, "setDataSource", objects);
    }
}
