package com.phantom.utils;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Toast;

import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.onetapvideodownload.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.robv.android.xposed.XposedHelpers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Global {
    public static final String TAG = "Global";
    public static final String VIDEO_MIME = "video/*";
    public static final String DEVELOPER_EMAIL = "onetapvideodownload@gmail.com";
    public static final String MP4_FILE_EXTENSION = ".mp4";

    //List taken from Wikipedia
    public static final String[] VIDEO_FILE_EXTENSION = {"webm", "mkv", "flv", "vob", "ogv", "ogg",
            "drc", "gif", "gifv", "mng", "avi", "mov", "qt", "wmv", "yuv", "rm", "rmvb", "asf", "amv",
            "mp4", "m4p", "m4v", "mpg", "mp2", "mpeg", "mpe", "mpv", "m2v", "svi", "3gp", "sg2", "mxf",
            "roq", "nsv", "flb", "f4v", "f4p", "f4a", "f4b" };

    public static String getDeveloperEmail() {
        return DEVELOPER_EMAIL;
    }

    public static String getFilenameFromUrl(String url) {
        Uri uri = Uri.parse(url);
        return uri.getLastPathSegment();
    }

    public static String getDomain(String url) {
        Uri uri = Uri.parse(url);
        return uri.getHost();
    }

    public static String getNewFilename(String filename) {
        int dotPos = filename.lastIndexOf('.');
        if (dotPos == -1) {
            dotPos = filename.length();
        }

        int openingBracketPos = filename.lastIndexOf('(');
        int closingBracketPos = filename.lastIndexOf(')');
        if (openingBracketPos != -1 && closingBracketPos != -1) {
            String numberString = filename.substring(openingBracketPos + 1, closingBracketPos);
            try {
                Integer number = Integer.parseInt(numberString);
                number = number + 1;
                filename = filename.substring(0, openingBracketPos + 1) + number.toString()
                        + filename.substring(closingBracketPos);
            } catch (Exception e) {
                filename = filename.substring(0, dotPos) + " (1)" + filename.substring(dotPos);
            }
        } else {
            filename = filename.substring(0, dotPos) + " (1)" + filename.substring(dotPos);
        }
        return filename;
    }

    public static String suggestName(String location, String filename) {
        boolean validVideoExtension = false;
        for(String extension : VIDEO_FILE_EXTENSION) {
            if (filename.endsWith(extension)) {
                validVideoExtension = true;
            }
        }

        if (!validVideoExtension) {
            filename += MP4_FILE_EXTENSION;
        }

        File downloadFile = new File(location, filename);
        if (!downloadFile.exists()) {
            return downloadFile.getName();
        }

        filename = getNewFilename(filename);
        return suggestName(location, filename);
    }

    public static boolean isResourceAvailable(String urlString) {
        URL url;
        HttpURLConnection urlConnection;
        try {
            url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();

            // HttpURLConnection will follow up to five HTTP redirects.
            if (responseCode/100 == 2) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getResourceMime(String urlString) {
        URL url;
        HttpURLConnection urlConnection;
        try {
            url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();

            // HttpURLConnection will follow up to five HTTP redirects.
            if (responseCode/100 == 2) {
                return urlConnection.getHeaderField("Content-Type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getValidatedFilename(String filename) {
        StringBuilder filenameBuilder = new StringBuilder(filename);
        for(int i = 0; i < filename.length(); i++) {
            char j = filename.charAt(i);
            String reservedChars = "?:\"*|/\\<>";
            if(reservedChars.indexOf(j) != -1) {
                filenameBuilder.setCharAt(i, ' ');
            }
        }
        return filenameBuilder.toString().trim();
    }

    public static void startOpenIntent(Context context, String fileLocation) {
        try {
            Intent openIntent = new Intent();
            openIntent.setAction(android.content.Intent.ACTION_VIEW);
            openIntent.setDataAndType(Uri.parse(fileLocation), "video/*");
            openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(openIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context,
                    context.getResources().getString(R.string.play_video_activity_not_found),
                    Toast.LENGTH_LONG).show();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(context,
                    context.getResources().getString(R.string.unable_to_file_file) + fileLocation,
                    Toast.LENGTH_LONG).show();
        }
    }

    public static void startFileShareIntent(Context context, String fileLocation) {
        try {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);

            Uri fileUri = FileProvider.getUriForFile(context, "com.phantom.fileprovider",
                    new File(fileLocation));
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.setType(Global.VIDEO_MIME);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(shareIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context,
                    context.getResources().getString(R.string.share_video_activity_not_found),
                    Toast.LENGTH_LONG).show();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(context,
                    context.getResources().getString(R.string.unable_to_file_file) + fileLocation,
                    Toast.LENGTH_LONG).show();
        }
    }

    public static void deleteFile(Context context, String fileLocation) {
        File file = new File(fileLocation);
        boolean result = file.delete();
        if (result) {
            Toast.makeText(context, R.string.file_deleted_successfully,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.unable_to_delete_file,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static String getHumanReadableSize(long bytes) {
        if (bytes < 1000) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(1000));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(1000, exp), pre);
    }

    public static String getResponseBody(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            if (response.body().contentLength() < 3*1000*1000L) {
                return response.body().string();
            } else {
                throw new IllegalArgumentException("Body content size is very large");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendEmail(Context context, String to, String subject, String body) {
        sendEmail(context, to, subject, body, null);
    }

    public static void sendEmail(Context context, String to, String subject, String body, String fileLocation) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);

        if (fileLocation != null) {
            Uri fileUri = FileProvider.getUriForFile(context, "com.phantom.fileprovider", new File(fileLocation));
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        }

        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(emailIntent, "Send via Email"));
    }

    public static boolean isLocalFile(String path) {
        return path.startsWith("file://") || path.startsWith("/");
    }

    public static void copyUrlToClipboard(Context context, String url) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        Uri copyUri = Uri.parse(url);
        ClipData clip = ClipData.newUri(context.getContentResolver(), "URI", copyUri);
        clipboard.setPrimaryClip(clip);
    }

    public static boolean isPlaystoreAvailable(@NonNull Context context) {
        List<String> packages = new ArrayList<>();
        packages.add("com.google.market");
        packages.add("com.android.vending");

        PackageManager packageManager = context.getPackageManager();
        for (String packageName : packages) {
            try {
                packageManager.getPackageInfo(packageName, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        Log.v(TAG, "Playstore not available on the device!");
        return false;
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static Context getContext() {
        Class activityThreadClass = XposedHelpers.findClass("android.app.ActivityThread", null);
        Object activityThread = XposedHelpers.callStaticMethod(activityThreadClass, "currentActivityThread");
        return (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
    }

    public static long getDirectorySize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                size += getDirectorySize(childFile);
                }
            } else {
            size = file.length();
            }
        return size;
     }

    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isClassPresent(ClassLoader loader, String className) {
        try {
            loader.loadClass(className);
            return true;
        } catch( ClassNotFoundException e ) {
            return false;
        }
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    public static boolean writeStringToFile(File file, String content) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean isFileReadable(File file) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            bufferedReader.readLine();
            bufferedReader.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String readFileToString(File file) {
        String result = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            result = new String(data, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONObject isValidJSONObject(String jsonString) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static int getXSignificantDigits(int version, int x) {
        return version / (int)Math.pow(10, (int)Math.log10(version) - x + 1);
    }

    public static void loadJSONToMap(Map<Integer, Pair<String, String>> map, JSONObject jsonObject) throws JSONException{
        Iterator<String> jsonKeys = jsonObject.keys();
        while (jsonKeys.hasNext()) {
            String key = jsonKeys.next();
            JSONObject value = jsonObject.getJSONObject(key);
            String mainClass = value.keys().next();
            String methodClass = value.getString(mainClass);
            map.put(Integer.parseInt(key), new Pair<>(mainClass, methodClass));
        }
        ApplicationLogMaintainer.sendBroadcast(Global.getContext(), "Parsed JSON and class names added to map.");
    }
}
