package com.phantom.onetapvideodownload;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.net.HttpURLConnection;
import java.net.URL;

public class Global {
    // Get MP4 mime for video because it's supported by most of the players
    public static String VIDEO_MIME = MimeTypeMap.getSingleton().getMimeTypeFromExtension(".MP4");
    public static String getFilenameFromUrl(String url) {
        Uri uri = Uri.parse(url);
        return uri.getLastPathSegment();
    }

    public static boolean isValidUrl(String urlString) {
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
}
