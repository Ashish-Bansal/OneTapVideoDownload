package com.phantom.onetapvideodownload;


import android.content.Context;

import java.util.HashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class YoutubeMediaHook implements IXposedHookLoadPackage {

    public static final String PACKAGE_NAME = "com.google.android.youtube";
    public static final HashMap<Integer, YouTubePackage> applicationMap = new HashMap<>();

    static {
        applicationMap.put(108358, new YouTubePackage("jci", "noo"));
        applicationMap.put(107756, new YouTubePackage("ipz", "myv"));
        applicationMap.put(108754, new YouTubePackage("knr", "pkl"));
        applicationMap.put(108959, new YouTubePackage("kqo", "pqh"));
        applicationMap.put(107858, new YouTubePackage("irs", "nax"));
        applicationMap.put(102857, new YouTubePackage("hwt", "lqk"));
        applicationMap.put(108553, new YouTubePackage("jmv", "oae"));
        applicationMap.put(102555, new YouTubePackage("hux", "lkq"));
        applicationMap.put(103457, new YouTubePackage("idv", "mpj"));
        applicationMap.put(110156, new YouTubePackage("ktt", "pgk"));
        applicationMap.put(108058, new YouTubePackage("jcw", "noc"));
        applicationMap.put(102952, new YouTubePackage("hxz", "lsy"));
        applicationMap.put(103351, new YouTubePackage("ift", "mkx"));
        applicationMap.put(110354, new YouTubePackage("lkn", "qcf"));
        applicationMap.put(101855, new YouTubePackage("gmg", "jlb"));
        applicationMap.put(103155, new YouTubePackage("ibx", "map"));
    }

    public Context getContext() {
        Class activityThreadClass = XposedHelpers.findClass("android.app.ActivityThread", null);
        Object activityThread = XposedHelpers.callStaticMethod(activityThreadClass, "currentActivityThread");
        return (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
    }

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam paramLoadPackageParam) throws Throwable {
        if (!paramLoadPackageParam.packageName.equals(PACKAGE_NAME)) {
            return;
        }

        // Method Signature
        // public MainClass(MethodParameterClass paramJlb, String paramString, long paramLong)

        final XC_MethodHook methodHook = new XC_MethodHook() {
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam paramAnonymousMethodHookParam)
                    throws Throwable {
                String paramString = (String)paramAnonymousMethodHookParam.args[1];
                XposedBridge.log(paramString);
            }
        };

        Context context = getContext();
        int packageVersion = context.getPackageManager()
                .getPackageInfo(paramLoadPackageParam.packageName, 0).versionCode;

        XposedBridge.log("Youtube Package version : " + packageVersion);

        YouTubePackage currentPackage = applicationMap.get(getSignificantDigits(packageVersion));
        if (currentPackage == null) {
            XposedBridge.log("Version Class names not found. Please contact developer to get support for this package");
            return;
        }

        Class mainClass = XposedHelpers.findClass(currentPackage.getMainClass(), paramLoadPackageParam.classLoader);

        XposedBridge.log("Hooking constructor");

        XposedBridge.log(currentPackage.getMethodParameterClass());
        Object [] objects = new Object[] {
                XposedHelpers.findClass(currentPackage.getMethodParameterClass(), paramLoadPackageParam.classLoader),
                String.class,
                Long.TYPE,
                methodHook
        };

        XposedHelpers.findAndHookConstructor(mainClass,  objects);
    }

    private int getSignificantDigits(int version) {
        return version / (int)Math.pow(10, (int)Math.log10(version) - 5);
    }
}
