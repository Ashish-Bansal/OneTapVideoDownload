package com.phantom.onetapvideodownload.hooks;

import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.IpcService;
import com.phantom.utils.Global;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SoundCloudHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        final String packageName = "com.soundcloud.android";
        if (!lpparam.packageName.equals(packageName)) {
            return;
        }

        String skippyPlayerClassName = "com.soundcloud.android.playback.skippy.SkippyAdapter";
        Class skippyClass = XposedHelpers.findClass(skippyPlayerClassName, lpparam.classLoader);

        final XC_MethodHook methodHook = new XC_MethodHook() {
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam hookParams) throws Throwable {
                try  {
                    String url = hookParams.getResult().toString();
                    ApplicationLogMaintainer.sendBroadcast(Global.getContext(), "Sound Cloud URL : " + url);
                    IpcService.startSaveUrlAction(Global.getContext(), url, packageName);
                } catch (Exception e) {
                    ApplicationLogMaintainer.sendBroadcast(Global.getContext(), Global.getStackTrace(e));
                } catch (XposedHelpers.ClassNotFoundError | NoSuchMethodError _) {
                    ApplicationLogMaintainer.sendBroadcast(Global.getContext(), "SoundCloud Hook Class not found");
                }
            }
        };

        // private String buildStreamUrl(PlaybackItem paramPlaybackItem)
        XposedBridge.hookAllMethods(skippyClass, "buildStreamUrl", methodHook);
    }
}
