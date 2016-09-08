package com.phantom.onetapvideodownload.hooks;

import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.utils.Global;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SelfHook implements IXposedHookLoadPackage {
    private String packageName = "com.phantom.onetapvideodownload";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(packageName)) {
            ApplicationLogMaintainer.sendBroadcast(Global.getContext(), "One Tap Initialized");
            ApplicationLogMaintainer.sendBroadcast(Global.getContext(), "OneTapVideoDownload : Self hooking.");
            XposedHelpers.findAndHookMethod(packageName + ".ui.MainActivity", lpparam.classLoader, "isModuleEnabled", XC_MethodReplacement.returnConstant(true));
        }
    }
}
