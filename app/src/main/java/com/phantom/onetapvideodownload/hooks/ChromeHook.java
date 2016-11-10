package com.phantom.onetapvideodownload.hooks;

import android.net.Uri;

import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.IpcService;
import com.phantom.utils.Global;

import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ChromeHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        ArrayList<String> chromiumBasedPackages = new ArrayList<>();
        chromiumBasedPackages.add("com.android.chrome");
        chromiumBasedPackages.add("com.chrome.dev");
        chromiumBasedPackages.add("com.chrome.beta");
        chromiumBasedPackages.add("com.chrome.canary");

        final String packageName = lpparam.packageName;
        Boolean chromiumBasedApplication = false;
        for(String application : chromiumBasedPackages) {
            if (application.equals(packageName)) {
                chromiumBasedApplication = true;
                break;
            }
        }

        if (!chromiumBasedApplication) {
            return;
        }

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
        Class remoteMediaPlayerBridge = XposedHelpers.findClass("org.chromium.chrome.browser.media.remote.RemoteMediaPlayerBridge", lpparam.classLoader);
        Object[] objects = new Object[] { long.class, String.class, String.class, String.class, methodHook };
        XposedHelpers.findAndHookConstructor(remoteMediaPlayerBridge, objects);
    }
}
