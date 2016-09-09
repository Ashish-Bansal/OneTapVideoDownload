package com.phantom.onetapyoutubemodule;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.IpcService;
import com.phantom.utils.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class YoutubeMediaHook implements IXposedHookLoadPackage {
    private static final String ONE_TAP_PACKAGE_NAME = "com.phantom.onetapvideodownload";
    private static final String ONE_TAP_YOUTUBE_MODULE_PACKAGE_NAME = "com.phantom.onetapyoutubemodule";
    private static final String PACKAGE_NAME = "com.google.android.youtube";
    private static final String ORIGINAL_MAIN_CLASS_NAME = "com.google.android.libraries.youtube.innertube.model.media.FormatStreamModel";
    private static final String ORIGINAL_METHOD_CLASS_NAME = "com.google.android.libraries.youtube.proto.nano.InnerTubeApi.FormatStream";
    private static final HashMap<Integer, YouTubePackage> classNamesMap = new HashMap<>();
    private static long lastVideoTime = System.currentTimeMillis();

    void loadJSONToMap(JSONObject jsonObject) throws JSONException{
            Iterator<String> jsonKeys = jsonObject.keys();
            while (jsonKeys.hasNext()) {
                String key = jsonKeys.next();
                JSONObject value = jsonObject.getJSONObject(key);
                String mainClass = value.keys().next();
                String methodClass = value.getString(mainClass);
                classNamesMap.put(Integer.parseInt(key), new YouTubePackage(mainClass, methodClass));
            }
        ApplicationLogMaintainer.sendBroadcast(Global.getContext(), "Parsed JSON and class names added to map.");
    }

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

        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(ONE_TAP_PACKAGE_NAME, 0);
        String applicationDirectoryPath = packageInfo.applicationInfo.dataDir;
        ApplicationLogMaintainer.sendBroadcast(context, "Hook File Path : " + applicationDirectoryPath);

        File hookDirectory = new File(applicationDirectoryPath, "files/");
        File hookFile = new File(hookDirectory, Global.getHooksFileName());
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
                loadJSONToMap(jsonObject.getJSONObject("Youtube"));
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

                lastVideoTime = currentTime;
                String paramString = (String)hookParams.args[1];
                ApplicationLogMaintainer.sendBroadcast(context, "Youtube Video Id : " + paramString);
                IpcService.startSaveYoutubeVideoAction(context, paramString);
            }
        };

        int packageVersion = context.getPackageManager()
                .getPackageInfo(lpparam.packageName, 0).versionCode;

        ApplicationLogMaintainer.sendBroadcast(context, "Youtube Package version Code : " + packageVersion);

        YouTubePackage currentPackage;
        if (isNotObfuscated) {
            currentPackage = classNamesMap.get(0);
        } else {
            currentPackage = classNamesMap.get(getSignificantDigits(packageVersion));
        }

        boolean successful = false;
        if (currentPackage == null) {
            ApplicationLogMaintainer.sendBroadcast(context, "Trying bruteforcing using Map");
            for (Map.Entry<Integer, YouTubePackage> pair : classNamesMap.entrySet()) {
                String mainClassName = pair.getValue().getMainClass();
                String parameterClassName = pair.getValue().getMethodParameterClass();
                successful = hookYoutube(lpparam.classLoader, methodHook, mainClassName,
                        parameterClassName);
                if (successful) {
                    break;
                }
            }
        } else {
            String mainClassName = currentPackage.getMainClass();
            String parameterClassName = currentPackage.getMethodParameterClass();
            successful = hookYoutube(lpparam.classLoader, methodHook, mainClassName, parameterClassName);
        }
        if (successful) {
            ApplicationLogMaintainer.sendBroadcast(context, "Youtube Hooking Successful");
        } else {
            ApplicationLogMaintainer.sendBroadcast(context, "Youtube hooking failed");
        }
    }

    private boolean hookYoutube(ClassLoader classLoader, XC_MethodHook methodHook,
                                String mainClassName, String parameterClassName) {
        try {
            Class mainClass = XposedHelpers.findClass(mainClassName, classLoader);
            Object[] objects = new Object[]{
                    XposedHelpers.findClass(parameterClassName, classLoader),
                    String.class,
                    Long.TYPE,
                    methodHook
            };
            XposedHelpers.findAndHookConstructor(mainClass, objects);
        } catch (Exception | NoSuchMethodError e) {
            return false;
        }
        return true;
    }

    private int getSignificantDigits(int version) {
        return version / (int)Math.pow(10, (int)Math.log10(version) - 5);
    }

    static {
        classNamesMap.put(0, new YouTubePackage(ORIGINAL_MAIN_CLASS_NAME, ORIGINAL_METHOD_CLASS_NAME));
        classNamesMap.put(108358, new YouTubePackage("jci", "noo"));
        classNamesMap.put(107756, new YouTubePackage("ipz", "myv"));
        classNamesMap.put(108754, new YouTubePackage("knr", "pkl"));
        classNamesMap.put(108959, new YouTubePackage("kqo", "pqh"));
        classNamesMap.put(107858, new YouTubePackage("irs", "nax"));
        classNamesMap.put(102857, new YouTubePackage("hwt", "lqk"));
        classNamesMap.put(108553, new YouTubePackage("jmv", "oae"));
        classNamesMap.put(102555, new YouTubePackage("hux", "lkq"));
        classNamesMap.put(103457, new YouTubePackage("idv", "mpj"));
        classNamesMap.put(108058, new YouTubePackage("jcw", "noc"));
        classNamesMap.put(102952, new YouTubePackage("hxz", "lsy"));
        classNamesMap.put(103351, new YouTubePackage("ift", "mkx"));
        classNamesMap.put(110354, new YouTubePackage("lkn", "qcf"));
        classNamesMap.put(101855, new YouTubePackage("gmg", "jlb"));
        classNamesMap.put(103155, new YouTubePackage("ibx", "map"));
        classNamesMap.put(101653, new YouTubePackage("gin", "jda"));
        classNamesMap.put(101253, new YouTubePackage("gfl", "iur"));
        classNamesMap.put(100203, new YouTubePackage("fmo", "hoi"));
        classNamesMap.put(110153, new YouTubePackage("kts", "pgj"));
        classNamesMap.put(102455, new YouTubePackage("hfz", "ktr"));
        classNamesMap.put(100506, new YouTubePackage("fwc", "ibn"));
        classNamesMap.put(100305, new YouTubePackage("foe", "hrf"));
        classNamesMap.put(100405, new YouTubePackage("ftp", "hxs"));
        classNamesMap.put(103553, new YouTubePackage("lkn", "qcf"));
        classNamesMap.put(100852, new YouTubePackage("geg", "iny"));
        classNamesMap.put(108957, new YouTubePackage("kqo", "pqh"));
        classNamesMap.put(110759, new YouTubePackage("lxg", "qub"));
        classNamesMap.put(111060, new YouTubePackage("mdn", "rgd"));
        classNamesMap.put(111356, new YouTubePackage("mnv", "rtv"));
        classNamesMap.put(111555, new YouTubePackage("mzy", "sir"));
        classNamesMap.put(111662, new YouTubePackage("nhl", "srt"));
        classNamesMap.put(111752, new YouTubePackage("nif", "suk"));
        classNamesMap.put(111852, new YouTubePackage("nji", "sxd"));
        classNamesMap.put(111956, new YouTubePackage("naw", "sru"));
        classNamesMap.put(112054, new YouTubePackage("nau", "suv"));
        classNamesMap.put(112153, new YouTubePackage("ngq", "tae"));
        classNamesMap.put(112254, new YouTubePackage("niz", "tbz"));
        classNamesMap.put(112356, new YouTubePackage("niv", "tcp"));
        classNamesMap.put(112555, new YouTubePackage("nlk", "tht"));
        classNamesMap.put(112559, new YouTubePackage("nlk", "tht"));
        classNamesMap.put(112753, new YouTubePackage("nms", "tlb"));
        classNamesMap.put(112953, new YouTubePackage("nmj", "tyt"));
        classNamesMap.put(112955, new YouTubePackage("nkn", "tww"));
        classNamesMap.put(113253, new YouTubePackage("nsz", "uik"));
        classNamesMap.put(113355, new YouTubePackage("ntm", "ukl"));
        classNamesMap.put(113358, new YouTubePackage("ntm", "ukl"));
    }

}
