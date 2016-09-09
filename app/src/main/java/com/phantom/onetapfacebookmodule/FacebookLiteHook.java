package com.phantom.onetapfacebookmodule;

import android.content.Context;
import android.support.v4.util.Pair;

import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.IpcService;
import com.phantom.utils.Global;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class FacebookLiteHook implements IXposedHookLoadPackage {
    private static final String ONE_TAP_FACEBOOK_MODULE_PACKAGE_NAME = "com.phantom.onetapfacebookmodule";
    private static final String FACEBOOK_PACKAGE_NAME = "com.facebook.lite";
    private static final HashMap<Integer, Pair<String, String>> classNamesMap = new HashMap<>();

    static {
        classNamesMap.put(36, new Pair<>("com.facebook.lite.r.e", "com.a.a.a.d.b"));
    }

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(FACEBOOK_PACKAGE_NAME)) {
            return;
        }

        final Context context = Global.getContext();
        ApplicationLogMaintainer.sendBroadcast(context, "One Tap Facebook Lite Hook Initialized");
        if (!Global.isPackageInstalled(context, ONE_TAP_FACEBOOK_MODULE_PACKAGE_NAME)) {
            ApplicationLogMaintainer.sendBroadcast(context, "One Tap Facebook Lite Module not installed");
            return;
        }

        File hookFile = new File(Global.getHooksFilePath(context));
        try {
            if (!hookFile.exists())  {
                ApplicationLogMaintainer.sendBroadcast(context, "Hook file doesn't exist");
            }

            if (!Global.isFileReadable(hookFile))  {
                ApplicationLogMaintainer.sendBroadcast(context, "Unable to open file for reading");
                ApplicationLogMaintainer.sendBroadcast(context, hookFile.getAbsolutePath());
            }

            String jsonString = Global.readFileToString(hookFile);
            JSONObject jsonObject = Global.isValidJSONObject(jsonString);
            if (jsonObject != null) {
                classNamesMap.clear();
                ApplicationLogMaintainer.sendBroadcast(Global.getContext(), "Cleared Class Names Map");
                Global.loadJSONToMap(classNamesMap, jsonObject.getJSONObject("FacebookLite"));
            }
        } catch (Exception e) {
            ApplicationLogMaintainer.sendBroadcast(context, Global.getStackTrace(e));
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

        int packageVersion = context.getPackageManager()
                .getPackageInfo(lpparam.packageName, 0).versionCode;

        ApplicationLogMaintainer.sendBroadcast(context, "Facebook Lite Package Version : " + packageVersion);

        Pair<String, String> classPair = classNamesMap.get(Global.getXSignificantDigits(packageVersion, 2));
        if (classPair == null) {
            ApplicationLogMaintainer.sendBroadcast(context, "ClassNamePair is null. Todo : Update Hooks");
            return;
        }

        if (!Global.isClassPresent(lpparam.classLoader, classPair.first)
                || !Global.isClassPresent(lpparam.classLoader, classPair.second)) {
            ApplicationLogMaintainer.sendBroadcast(context, "Facebook Lite Hooking failed even when ClassPair is not null." );
            return;
        }

        Class mainClass = XposedHelpers.findClass(classPair.first, lpparam.classLoader);
        Class subClass = XposedHelpers.findClass(classPair.second, lpparam.classLoader);

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
                boolean.class,
                methodHook
        };

        try {
            XposedHelpers.findAndHookConstructor(mainClass, objects);
            ApplicationLogMaintainer.sendBroadcast(context, "Facebook Lite hooking successful");
        } catch (XposedHelpers.ClassNotFoundError | NoSuchMethodError e) {
            ApplicationLogMaintainer.sendBroadcast(context, Global.getStackTrace(e));
        }
    }

}
