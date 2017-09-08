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
        classNamesMap.put(108358, new Pair<>("jci", "noo"));
        classNamesMap.put(107756, new Pair<>("ipz", "myv"));
        classNamesMap.put(108754, new Pair<>("knr", "pkl"));
        classNamesMap.put(108959, new Pair<>("kqo", "pqh"));
        classNamesMap.put(107858, new Pair<>("irs", "nax"));
        classNamesMap.put(102857, new Pair<>("hwt", "lqk"));
        classNamesMap.put(108553, new Pair<>("jmv", "oae"));
        classNamesMap.put(102555, new Pair<>("hux", "lkq"));
        classNamesMap.put(103457, new Pair<>("idv", "mpj"));
        classNamesMap.put(108058, new Pair<>("jcw", "noc"));
        classNamesMap.put(102952, new Pair<>("hxz", "lsy"));
        classNamesMap.put(103351, new Pair<>("ift", "mkx"));
        classNamesMap.put(110354, new Pair<>("lkn", "qcf"));
        classNamesMap.put(101855, new Pair<>("gmg", "jlb"));
        classNamesMap.put(103155, new Pair<>("ibx", "map"));
        classNamesMap.put(101653, new Pair<>("gin", "jda"));
        classNamesMap.put(101253, new Pair<>("gfl", "iur"));
        classNamesMap.put(100203, new Pair<>("fmo", "hoi"));
        classNamesMap.put(110153, new Pair<>("kts", "pgj"));
        classNamesMap.put(102455, new Pair<>("hfz", "ktr"));
        classNamesMap.put(100506, new Pair<>("fwc", "ibn"));
        classNamesMap.put(100305, new Pair<>("foe", "hrf"));
        classNamesMap.put(100405, new Pair<>("ftp", "hxs"));
        classNamesMap.put(103553, new Pair<>("lkn", "qcf"));
        classNamesMap.put(100852, new Pair<>("geg", "iny"));
        classNamesMap.put(108957, new Pair<>("kqo", "pqh"));
        classNamesMap.put(110759, new Pair<>("lxg", "qub"));
        classNamesMap.put(111060, new Pair<>("mdn", "rgd"));
        classNamesMap.put(111356, new Pair<>("mnv", "rtv"));
        classNamesMap.put(111555, new Pair<>("mzy", "sir"));
        classNamesMap.put(111662, new Pair<>("nhl", "srt"));
        classNamesMap.put(111752, new Pair<>("nif", "suk"));
        classNamesMap.put(111852, new Pair<>("nji", "sxd"));
        classNamesMap.put(111956, new Pair<>("naw", "sru"));
        classNamesMap.put(112054, new Pair<>("nau", "suv"));
        classNamesMap.put(112153, new Pair<>("ngq", "tae"));
        classNamesMap.put(112254, new Pair<>("niz", "tbz"));
        classNamesMap.put(112356, new Pair<>("niv", "tcp"));
        classNamesMap.put(112555, new Pair<>("nlk", "tht"));
        classNamesMap.put(112559, new Pair<>("nlk", "tht"));
        classNamesMap.put(112753, new Pair<>("nms", "tlb"));
        classNamesMap.put(112953, new Pair<>("nmj", "tyt"));
        classNamesMap.put(112955, new Pair<>("nkn", "tww"));
        classNamesMap.put(113253, new Pair<>("nsz", "uik"));
        classNamesMap.put(113355, new Pair<>("ntm", "ukl"));
        classNamesMap.put(113358, new Pair<>("ntm", "ukl"));
        classNamesMap.put(113560, new Pair<>("nzj", "uti"));
        classNamesMap.put(113854, new Pair<>("ocr", "ury"));
        classNamesMap.put(113956, new Pair<>("odr", "utk"));
        classNamesMap.put(114156, new Pair<>("ofb", "vas"));
        classNamesMap.put(114352, new Pair<>("oit", "vay"));
        classNamesMap.put(114354, new Pair<>("oit", "vay"));
    }
}
