package com.phantom.onetapyoutubemodule;

import android.content.Context;
import android.support.v4.util.Pair;

import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.IpcService;
import com.phantom.utils.Global;
import com.phantom.utils.HookClassNamesFetcher;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class YoutubeMediaHook implements IXposedHookLoadPackage {
    private static final String ONE_TAP_PACKAGE_NAME = "com.phantom.onetapvideodownload";
    private static final String ONE_TAP_YOUTUBE_MODULE_PACKAGE_NAME = "com.phantom.onetapyoutubemodule";
    private static final String PACKAGE_NAME = "com.google.android.youtube";
    private static final String ORIGINAL_MAIN_CLASS_NAME = "com.google.android.libraries.youtube.innertube.model.media.FormatStreamModel";
    private static final String ORIGINAL_METHOD_CLASS_NAME = "com.google.android.libraries.youtube.proto.nano.InnerTubeApi.FormatStream";
    private static final HashMap<Integer, Pair<String, String>> classNamesMap = new HashMap<>();
    private static long lastVideoTime = System.currentTimeMillis();

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(PACKAGE_NAME)) {
            return;
        }

        final Context context = Global.getContext();
        ApplicationLogMaintainer.sendBroadcast(context, "One Tap Youtube Hook Initialized");
        if (!Global.isPackageInstalled(Global.getContext(), ONE_TAP_YOUTUBE_MODULE_PACKAGE_NAME)) {
            ApplicationLogMaintainer.sendBroadcast(context, "One Tap Youtube Module not installed");
            return;
        }

        File hookFile = new File(HookClassNamesFetcher.getHooksFilePath(context));
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
                Global.loadJSONToMap(classNamesMap, jsonObject.getJSONObject("Youtube"));
            }
        } catch (Exception e) {
            ApplicationLogMaintainer.sendBroadcast(context, Global.getStackTrace(e));
        }

        // Method Signature
        // public MainClass(MethodParameterClass paramJlb, String paramString, long paramLong)

        final ClassLoader loader = lpparam.classLoader;
        boolean isNotObfuscated = Global.isClassPresent(loader, ORIGINAL_MAIN_CLASS_NAME);

        final XC_MethodHook methodHook = new XC_MethodHook() {
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam hookParams) throws Throwable {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastVideoTime < 1200L) {
                    return;
                }

                if (!(hookParams.args[1] instanceof String)) {
                    ApplicationLogMaintainer.sendBroadcast(context, "Non-string object found in the hooked method");
                    return;
                }

                lastVideoTime = currentTime;
                String paramString = (String)hookParams.args[1];
                ApplicationLogMaintainer.sendBroadcast(context, "Youtube Video Id : " + paramString);
                IpcService.startSaveYoutubeVideoAction(context, paramString);
            }
        };

        int packageVersion = context.getPackageManager()
                .getPackageInfo(lpparam.packageName, 0).versionCode;

        ApplicationLogMaintainer.sendBroadcast(context, "Youtube Package version Code : " + packageVersion);

        Pair<String, String> currentPackage;
        if (isNotObfuscated) {
            currentPackage = classNamesMap.get(0);
        } else {
            currentPackage = classNamesMap.get(Global.getXSignificantDigits(packageVersion, 6));
        }

        boolean successful = false;
        if (currentPackage == null) {
            ApplicationLogMaintainer.sendBroadcast(context, "Trying bruteforcing using Map");
            for (Map.Entry<Integer, Pair<String, String>> pair : classNamesMap.entrySet()) {
                String className = pair.getValue().first;
                successful = hookYoutube(lpparam.classLoader, methodHook, className);
                if (successful) {
                    break;
                }
            }
        } else {
            String className = currentPackage.first;
            successful = hookYoutube(lpparam.classLoader, methodHook, className);
        }
        if (successful) {
            ApplicationLogMaintainer.sendBroadcast(context, "Youtube Hooking Successful");
        } else {
            ApplicationLogMaintainer.sendBroadcast(context, "Youtube hooking failed. To Do : Update Hooks");
        }
    }

    private boolean hookYoutube(ClassLoader classLoader, XC_MethodHook methodHook, String className) {
        try {
            Class mainClass = XposedHelpers.findClass(className, classLoader);
            XposedBridge.hookAllConstructors(mainClass, methodHook);
        } catch (Exception | NoSuchMethodError | XposedHelpers.ClassNotFoundError e) {
            return false;
        }
        return true;
    }

    static {
        classNamesMap.put(0, new Pair<>(ORIGINAL_MAIN_CLASS_NAME, ORIGINAL_METHOD_CLASS_NAME));
    }
}
