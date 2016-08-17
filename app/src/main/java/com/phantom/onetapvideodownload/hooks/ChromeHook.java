package com.phantom.onetapvideodownload.hooks;

import android.net.Uri;

import com.phantom.onetapvideodownload.IpcService;
import com.phantom.onetapvideodownload.utils.Global;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ChromeHook implements IXposedHookLoadPackage {
    private String packageName = "com.android.chrome";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(packageName)) {
            return;
        }

        XC_MethodHook methodHook = new XC_MethodHook() {
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam hookParams) throws Throwable {
                try {
                    Uri uri = Uri.parse((String) hookParams.args[1]);
                    XposedBridge.log("One Tap Video Download : Chrome Hook : URL " + uri.toString());
                    if (uri.toString().startsWith("http")) {
                        IpcService.startSaveUrlAction(Global.getContext(), uri, packageName);
                    }
                } catch (Exception e) {
                    XposedBridge.log(e);
                }
            }
        };

        // RemoteMediaPlayerBridge(long nativeRemoteMediaPlayerBridge, String sourceUrl, String frameUrl, String userAgent)
        Class remoteMediaPlayerBridge = XposedHelpers.findClass("org.chromium.chrome.browser.media.remote.RemoteMediaPlayerBridge", lpparam.classLoader);
        Object[] objects = new Object[] { long.class, String.class, String.class, String.class, methodHook };
        XposedHelpers.findAndHookConstructor(remoteMediaPlayerBridge, objects);
    }
}
