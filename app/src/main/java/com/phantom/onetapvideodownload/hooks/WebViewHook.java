package com.phantom.onetapvideodownload.hooks;

import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.IpcService;
import com.phantom.onetapvideodownload.utils.Global;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class WebViewHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // Chromium Android WebView Hooking
        try {
            String webViewBackgroundThreadClientImpClassName =
                    "org.chromium.android_webview.AwContents$BackgroundThreadClientImpl";
            String webViewAwWebResourceRequestClassName =
                    "org.chromium.android_webview.AwContentsClient$AwWebResourceRequest";
            final Class<?> webViewBackgroundThreadClientImpl =
                    findClass(webViewBackgroundThreadClientImpClassName, lpparam.classLoader);
            final Class<?> webViewAwWebResourceRequest =
                    findClass(webViewAwWebResourceRequestClassName, lpparam.classLoader);

            XC_MethodHook methodHook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam hookParams) throws Throwable {
                    Field urlField = webViewAwWebResourceRequest.getField("url");
                    String url = (String) urlField.get(hookParams.args[0]);
                    log(lpparam.packageName + " URL : " + url);
                    IpcService.startInspectMediaUriAction(Global.getContext(), url, lpparam.packageName);
                }
            };

            Object[] objects = new Object[] { webViewAwWebResourceRequest, methodHook};

            findAndHookMethod(webViewBackgroundThreadClientImpl, "shouldInterceptRequest", objects);
        } catch (Exception e) {
            ApplicationLogMaintainer.sendBroadcast(Global.getContext(), Global.getStackTrace(e));
        } catch (XposedHelpers.ClassNotFoundError | NoSuchMethodError _) {
        }

//        Android WebView (Hooking at higher level of WebView Implementation)
//        try {
//            final Class<?> webViewClientRequest = findClass("com.android.webview.chromium.WebViewContentsClientAdapter", lpparam.classLoader);
//            findAndHookMethod(webViewClientRequest, "onLoadResource", String.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    String url = (String) param.args[0];
//                    String packageName = lpparam.packageName;
//                    log(lpparam.packageName + " URL : " + url);
//                    IpcService.inspectMediaUri(url, lpparam.packageName);
//                }
//            });
//        } catch (Exception e) {
//        }
    }
}
