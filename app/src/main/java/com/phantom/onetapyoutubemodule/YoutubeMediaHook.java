package com.phantom.onetapyoutubemodule;

import android.content.Context;

import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.IpcService;
import com.phantom.onetapvideodownload.utils.Global;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class YoutubeMediaHook implements IXposedHookLoadPackage {
    private static final String ONE_TAP_YOUTUBE_MODULE_PACKAGE_NAME = "com.phantom.onetapyoutubemodule";
    private static final String PACKAGE_NAME = "com.google.android.youtube";
    private static final String ORIGINAL_MAIN_CLASS_NAME = "com.google.android.libraries.youtube.innertube.model.media.FormatStreamModel";
    private static final String ORIGINAL_METHOD_CLASS_NAME = "com.google.android.libraries.youtube.proto.nano.InnerTubeApi.FormatStream";
    private static final HashMap<Integer, YouTubePackage> applicationMap = new HashMap<>();
    private static long lastVideoTime = System.currentTimeMillis();

    static {
        applicationMap.put(0, new YouTubePackage(ORIGINAL_MAIN_CLASS_NAME, ORIGINAL_METHOD_CLASS_NAME));
        applicationMap.put(108358, new YouTubePackage("jci", "noo"));
        applicationMap.put(107756, new YouTubePackage("ipz", "myv"));
        applicationMap.put(108754, new YouTubePackage("knr", "pkl"));
        applicationMap.put(108959, new YouTubePackage("kqo", "pqh"));
        applicationMap.put(107858, new YouTubePackage("irs", "nax"));
        applicationMap.put(102857, new YouTubePackage("hwt", "lqk"));
        applicationMap.put(108553, new YouTubePackage("jmv", "oae"));
        applicationMap.put(102555, new YouTubePackage("hux", "lkq"));
        applicationMap.put(103457, new YouTubePackage("idv", "mpj"));
        applicationMap.put(108058, new YouTubePackage("jcw", "noc"));
        applicationMap.put(102952, new YouTubePackage("hxz", "lsy"));
        applicationMap.put(103351, new YouTubePackage("ift", "mkx"));
        applicationMap.put(110354, new YouTubePackage("lkn", "qcf"));
        applicationMap.put(101855, new YouTubePackage("gmg", "jlb"));
        applicationMap.put(103155, new YouTubePackage("ibx", "map"));
        applicationMap.put(101653, new YouTubePackage("gin", "jda"));
        applicationMap.put(101253, new YouTubePackage("gfl", "iur"));
        applicationMap.put(100203, new YouTubePackage("fmo", "hoi"));
        applicationMap.put(110153, new YouTubePackage("kts", "pgj"));
        applicationMap.put(102455, new YouTubePackage("hfz", "ktr"));
        applicationMap.put(100506, new YouTubePackage("fwc", "ibn"));
        applicationMap.put(100305, new YouTubePackage("foe", "hrf"));
        applicationMap.put(100405, new YouTubePackage("ftp", "hxs"));
        applicationMap.put(103553, new YouTubePackage("lkn", "qcf"));
        applicationMap.put(100852, new YouTubePackage("geg", "iny"));
        applicationMap.put(108957, new YouTubePackage("kqo", "pqh"));
        applicationMap.put(110759, new YouTubePackage("lxg", "qub"));
        applicationMap.put(111060, new YouTubePackage("mdn", "rgd"));
        applicationMap.put(111356, new YouTubePackage("mnv", "rtv"));
        applicationMap.put(111555, new YouTubePackage("mzy", "sir"));
        applicationMap.put(111662, new YouTubePackage("nhl", "srt"));
        applicationMap.put(111752, new YouTubePackage("nif", "suk"));
        applicationMap.put(111852, new YouTubePackage("nji", "sxd"));
        applicationMap.put(111956, new YouTubePackage("naw", "sru"));
        applicationMap.put(112054, new YouTubePackage("nau", "suv"));
        applicationMap.put(112153, new YouTubePackage("ngq", "tae"));
        applicationMap.put(112254, new YouTubePackage("niz", "tbz"));
        applicationMap.put(112356, new YouTubePackage("niv", "tcp"));
        applicationMap.put(112555, new YouTubePackage("nlk", "tht"));
        applicationMap.put(112559, new YouTubePackage("nlk", "tht"));
        applicationMap.put(112753, new YouTubePackage("nms", "tlb"));
        applicationMap.put(112953, new YouTubePackage("nmj", "tyt"));
        applicationMap.put(112955, new YouTubePackage("nkn", "tww"));
        applicationMap.put(113253, new YouTubePackage("nsz", "uik"));
        applicationMap.put(113355, new YouTubePackage("ntm", "ukl"));
        applicationMap.put(113358, new YouTubePackage("ntm", "ukl"));
    }

    public boolean findClass(ClassLoader loader, String className) {
        try {
            loader.loadClass(className);
            return true;
        } catch( ClassNotFoundException e ) {
            return false;
        }
    }

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        final Context context = Global.getContext();
        if (!lpparam.packageName.equals(PACKAGE_NAME)) {
            return;
        }

        if (!Global.isPackageInstalled(Global.getContext(), ONE_TAP_YOUTUBE_MODULE_PACKAGE_NAME)) {
            ApplicationLogMaintainer.sendBroadcast(context, "One Tap Youtube Hook Initialized");
            ApplicationLogMaintainer.sendBroadcast(context, "One Tap Youtube Module not installed");
            return;
        }

        // Method Signature
        // public MainClass(MethodParameterClass paramJlb, String paramString, long paramLong)

        final ClassLoader loader = lpparam.classLoader;
        boolean isNotObfuscated = findClass(loader, ORIGINAL_MAIN_CLASS_NAME);

        final XC_MethodHook methodHook = new XC_MethodHook() {
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam hookParams) throws Throwable {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastVideoTime < 1200L) {
                    return;
                }

                lastVideoTime = currentTime;
                String paramString = (String)hookParams.args[1];
                XposedBridge.log(paramString);

                IpcService.startSaveYoutubeVideoAction(context, paramString);
            }
        };

        int packageVersion = context.getPackageManager()
                .getPackageInfo(lpparam.packageName, 0).versionCode;

        XposedBridge.log("OneTapVideoDownload : Youtube Package version : " + packageVersion);

        YouTubePackage currentPackage;
        if (isNotObfuscated) {
            currentPackage = applicationMap.get(0);
        } else {
            currentPackage = applicationMap.get(getSignificantDigits(packageVersion));
        }

        if (currentPackage == null) {
            XposedBridge.log("One Tap Video Download : Trying bruteforcing");
            boolean successful;
            for (Map.Entry<Integer, YouTubePackage> pair : applicationMap.entrySet()) {
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
            hookYoutube(lpparam.classLoader, methodHook, mainClassName, parameterClassName);
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
        XposedBridge.log("OneTapVideoDownload : Successful Hooking : " + mainClassName);
        return true;
    }

    private int getSignificantDigits(int version) {
        return version / (int)Math.pow(10, (int)Math.log10(version) - 5);
    }
}
