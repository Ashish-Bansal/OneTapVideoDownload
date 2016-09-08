package com.phantom.onetapfacebookmodule;

import android.content.Context;
import android.content.pm.PackageManager;

import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.IpcService;
import com.phantom.utils.Global;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class FacebookLiteHook implements IXposedHookLoadPackage {
    private static final String ONE_TAP_FACEBOOK_MODULE_PACKAGE_NAME = "com.phantom.onetapfacebookmodule";
    private static final String FACEBOOK_PACKAGE_NAME = "com.facebook.lite";
    private static final String CLASS_NAME = "com.facebook.lite.r.e";
    private static final String CLASS_NAME_1 = "com.a.a.a.d.b";

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
            protected void afterHookedMethod(MethodHookParam hookParams) throws Throwable {
                try {
                    String uri = hookParams.args[1].toString();
                    IpcService.startSaveUrlAction(context, uri, FACEBOOK_PACKAGE_NAME);
                    ApplicationLogMaintainer.sendBroadcast(context, "Facebook Lite URL : " + uri);
                } catch (Exception e) {
                    ApplicationLogMaintainer.sendBroadcast(context, Global.getStackTrace(e));
                }
            }
        };

        String packageName = context.getPackageManager().getPackageInfo(FACEBOOK_PACKAGE_NAME,
                PackageManager.GET_META_DATA).packageName;

        if (!Global.isClassPresent(lpparam.classLoader, CLASS_NAME)
                || !Global.isClassPresent(lpparam.classLoader, CLASS_NAME)) {
            ApplicationLogMaintainer.sendBroadcast(context, "Facebook Lite Hook class not found. Package version : " + packageName);
            return;
        }

        Class mainClass = XposedHelpers.findClass(CLASS_NAME, lpparam.classLoader);
        Class subClass = XposedHelpers.findClass(CLASS_NAME_1, lpparam.classLoader);

        Object [] objects = new Object[] {
                String.class,
                String.class,
                int.class,
                int.class,
                int.class,
                subClass,
                long.class,
                boolean.class,
                long.class,
                String.class,
                methodHook
        };

        XposedHelpers.findAndHookConstructor(mainClass, objects);
        ApplicationLogMaintainer.sendBroadcast(context, "Facebook Lite hooking successful for version " + packageName);
    }

}
