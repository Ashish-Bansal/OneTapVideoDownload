package com.phantom.videoplayerselect;

import android.util.Log;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;

public class UrlComparator implements Comparator<Url> {

    public UrlComparator() {

    }

    @Override
    public int compare(Url firstUrlObj, Url secondUrlObj) {
        try {
            Date thisDate = DateFormat.getDateTimeInstance().parse(firstUrlObj.getMetadata());
            Date urlObjDate = DateFormat.getDateTimeInstance().parse(secondUrlObj.getMetadata());
            return thisDate.compareTo(urlObjDate);
        } catch(java.text.ParseException e) {
            Log.e("URL", e.getStackTrace().toString());
        }
        return 0;
    }
}
