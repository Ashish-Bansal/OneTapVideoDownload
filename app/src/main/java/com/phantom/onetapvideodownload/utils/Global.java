package com.phantom.onetapvideodownload.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.phantom.onetapvideodownload.R;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

public class Global {
    public static String VIDEO_MIME = "video/*";
    public static String getFilenameFromUrl(String url) {
        Uri uri = Uri.parse(url);
        return uri.getLastPathSegment();
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
        Intent openIntent = new Intent();
        openIntent.setAction(android.content.Intent.ACTION_VIEW);
        openIntent.setDataAndType(Uri.parse(fileLocation), "video/*");
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(openIntent);
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
}
