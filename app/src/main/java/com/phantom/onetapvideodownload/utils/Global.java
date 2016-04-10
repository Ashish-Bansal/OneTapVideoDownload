package com.phantom.onetapvideodownload.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.phantom.onetapvideodownload.R;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Global {
    public static String VIDEO_MIME = "video/*";
    public static String DEVELOPER_EMAIL = "onetapvideodownload@gmail.com";

    public static String getDeveloperEmail() {
        return DEVELOPER_EMAIL;
    }

    public static String getFilenameFromUrl(String url) {
        Uri uri = Uri.parse(url);
        return uri.getLastPathSegment();
    }

    public static String suggestName(String location, String filename) {
        File downloadFile = new File(location, filename);
        if (!downloadFile.exists()) {
            return downloadFile.getName();
        }

        int dotPos = filename.lastIndexOf('.');
        int openingBracketPos = filename.lastIndexOf('(');
        int closingBracketPos = filename.lastIndexOf(')');
        if (dotPos == closingBracketPos + 1 && closingBracketPos - openingBracketPos == 2) {
            String numberString = filename.substring(openingBracketPos + 1, closingBracketPos);
            Integer number = Integer.parseInt(numberString);
            number = number  + 1;
            filename = filename.substring(0, openingBracketPos + 1) + number.toString()
                    + filename.charAt(closingBracketPos) + filename.substring(dotPos);
        } else {
            filename = filename.substring(0, dotPos) + "(1)" + filename.substring(dotPos);
        }

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
        }
    }

    public static void startFileShareIntent(Context context, String fileLocation) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);

        Uri fileUri = FileProvider.getUriForFile(context, "com.phantom.fileprovider",
                new File(fileLocation));
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setType(Global.VIDEO_MIME);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(shareIntent);
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
            return null;
        }
    }

    public static void sendEmail(Context context, String to, String subject, String body) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", to, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
        context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }
}
