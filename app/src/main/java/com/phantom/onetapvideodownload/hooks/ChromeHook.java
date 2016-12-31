package com.phantom.onetapvideodownload.hooks;

import android.net.Uri;

import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.IpcService;
import com.phantom.utils.Global;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ChromeHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        String mediaPlayerBridgeClassName = "org.chromium.chrome.browser.media.remote.RemoteMediaPlayerBridge";
        if (!usesChromeBrowserComponent(lpparam.classLoader, mediaPlayerBridgeClassName)) {
            return;
        }

        final String packageName = lpparam.packageName;
        XC_MethodHook methodHook = new XC_MethodHook() {
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam hookParams) throws Throwable {
                try {
                    Uri uri = Uri.parse((String) hookParams.args[1]);
                    ApplicationLogMaintainer.sendBroadcast(Global.getContext(), "Chrome Hook : URL" + uri.toString());
                    if (uri.toString().startsWith("http")) {
                        IpcService.startSaveUrlAction(Global.getContext(), uri, packageName);
                    }
                } catch (Exception e) {
                    ApplicationLogMaintainer.sendBroadcast(Global.getContext(), Global.getStackTrace(e));
                }
            }
        };

        // RemoteMediaPlayerBridge(long nativeRemoteMediaPlayerBridge, String sourceUrl, String frameUrl, String userAgent)
        Class remoteMediaPlayerBridge = XposedHelpers.findClass(mediaPlayerBridgeClassName, lpparam.classLoader);
        Object[] objects = new Object[] { long.class, String.class, String.class, String.class, methodHook };
        XposedHelpers.findAndHookConstructor(remoteMediaPlayerBridge, objects);
    }

    private boolean usesChromeBrowserComponent(ClassLoader classLoader, String className) {
        try {
            Class mediaPlayerBridge = XposedHelpers.findClass(className, classLoader);
            mediaPlayerBridge.getName();
        } catch (Exception | NoSuchMethodError | XposedHelpers.ClassNotFoundError e) {
            return false;
        }
        return true;
    }
}
