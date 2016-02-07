package com.phantom.onetapvideodownload;

import android.content.Context;
import android.net.Uri;

import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MediaPlayerHook implements IXposedHookLoadPackage {
    private String packageName = "com.phantom.onetapvideodownload";
    private static boolean moduleEnabled = false;

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(packageName)) {
            XposedBridge.log("Self hooking.");
            XposedHelpers.findAndHookMethod(packageName + ".MediaPlayerHook", lpparam.classLoader, "isModuleEnabled", XC_MethodReplacement.returnConstant(true));
        }

        XC_MethodHook methodHook = new XC_MethodHook() {
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam hookParams) throws Throwable {
                Context context = (Context) hookParams.args[0];
                Uri uri = (Uri) hookParams.args[1];
                if (uri.toString().startsWith("http")) {
                    XposedBridge.log(uri.toString());
                    IpcService.startSaveUrlAction(context, uri);
                }
            }
        };

        Class mediaPlayerClass = XposedHelpers.findClass("android.media.MediaPlayer", lpparam.classLoader);

        //void setDataSource(Context context, Uri uri, Map<String, String> headers);
        Object[] objects = new Object[] { Context.class, Uri.class, Map.class, methodHook};
        XposedHelpers.findAndHookMethod(mediaPlayerClass, "setDataSource", objects);
    }

    //If self hooking is successful, it will return true.
    public static boolean isModuleEnabled() {
        return false;
    }
}
