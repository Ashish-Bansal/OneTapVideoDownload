// How to create Xposed plugin for One Tap Video Download

private static final String ACTION_SAVE_URI = "com.phantom.onetapvideodownload.action.saveurl";
private static final String ONE_TAP_PACKAGE_NAME = "com.phantom.onetapvideodownload";
private static final String IPC_SERVICE_CLASS_NAME = ONE_TAP_PACKAGE_NAME + ".IpcService";
private static final String EXTRA_URL = ONE_TAP_PACKAGE_NAME + ".extra.url";
private static final String EXTRA_PACKAGE_NAME = ONE_TAP_PACKAGE_NAME + ".extra.package_name";
private static final String EXTRA_TITLE = PACKAGE_NAME + ".extra.title";

private boolean isOTVDInstalled(Context context) {
    PackageManager packageManager = context.getPackageManager();
    try {
        packageManager.getPackageInfo(ONE_TAP_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
        return true;
    } catch (PackageManager.NameNotFoundException e) {
        return false;
    }
}

private Context getContext() {
    Class activityThreadClass = XposedHelpers.findClass("android.app.ActivityThread", null);
    Object activityThread = XposedHelpers.callStaticMethod(activityThreadClass, "currentActivityThread");
    return (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
}

//Code
try {
    Context context = getContext();
    if (isOTVDInstalled(context)) {
        Intent intent = new Intent(ACTION_SAVE_URI);
        intent.setClassName(ONE_TAP_PACKAGE_NAME, IPC_SERVICE_CLASS_NAME);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_TITLE, filename);
        intent.putExtra(EXTRA_PACKAGE_NAME, lpparam.packageName);
        context.startService(intent);
    }
} catch (Exception e) {
    e.printStackTrace();
}
