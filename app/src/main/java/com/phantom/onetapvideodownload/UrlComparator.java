package com.phantom.onetapvideodownload;

import android.util.Log;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;

public class UrlComparator implements Comparator<Url> {

    public UrlComparator() {

    }

    /*
        Comparator for Reverse Sorting List<Url> according to time/date
     */
    @Override
    public int compare(Url firstUrlObj, Url secondUrlObj) {
        try {
            Date firstUrlDate = DateFormat.getDateTimeInstance().parse(firstUrlObj.getMetadata());
            Date secondUrlDate = DateFormat.getDateTimeInstance().parse(secondUrlObj.getMetadata());
            return secondUrlDate.compareTo(firstUrlDate);
        } catch(java.text.ParseException e) {
            Log.e("URL", e.getStackTrace().toString());
        }
        return 0;
    }
}
