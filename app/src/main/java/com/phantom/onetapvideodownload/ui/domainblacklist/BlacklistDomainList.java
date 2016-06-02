package com.phantom.onetapvideodownload.ui.domainblacklist;

import android.content.Context;
import android.support.v4.util.Pair;

import com.phantom.onetapvideodownload.databasehandlers.DomainBlacklistDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BlacklistDomainList {
    private BlacklistDomainList() {
    }

    public static BlacklistDomainList mBlacklistDomainList;
    List<Pair<Long, String>> mList = new ArrayList<>();
    private static DomainBlacklistDatabase mDomainBlacklistDatabase;

    public static BlacklistDomainList getUrlListSingleTon(Context context) {
        if (mBlacklistDomainList != null) {
            return mBlacklistDomainList;
        }

        mBlacklistDomainList = new BlacklistDomainList();
        mDomainBlacklistDatabase = DomainBlacklistDatabase.getDatabase(context);
        mBlacklistDomainList.loadSavedUrls();
        return mBlacklistDomainList;
    }

    public void sortList() {
        Collections.sort(mList, new Comparator<Pair<Long, String>>() {
            @Override
            public int compare(Pair<Long, String> lhs, Pair<Long, String> rhs) {
                return lhs.first < rhs.first ? 1 : 0;
            }
        });
    }

    public void addUrl(String url) {
        mDomainBlacklistDatabase.addUrl(url);
        sortList();
    }

    public void removeUrl(String url) {
        mDomainBlacklistDatabase.deleteUrl(url);
        for (int i = mList.size() - 1; i > -1; i--) {
            if (mList.get(i).second.equals(url) ) {
                mList.remove(i);
            }
        }
    }

    public int size() {
        return mList.size();
    }

    public String getUrl(int pos) {
        return mList.get(pos).second;
    }

    public void clearLocalList() {
        mList.clear();
    }

    public void clearSavedUrls() {
        mDomainBlacklistDatabase.clearDatabase();
    }

    public void loadSavedUrls() {
        mList = mDomainBlacklistDatabase.getAllUrls();
    }

    public List<Pair<Long, String>> getUrlList() {
        return new ArrayList<>(mList);
    }

    public boolean isEmpty() {
        return size() <= 0;
    }

    public void reloadUrls() {
        clearLocalList();
        loadSavedUrls();
    }

    public void updateUrl(String oldUrl, String newUrl) {
        mDomainBlacklistDatabase.updateUrl(oldUrl, newUrl);
    }

    public boolean exists(String url) {
        for(Pair<Long, String> p : mList) {
            if (p.second.equals(url) ) {
                return true;
            }
        }
        return false;
    }
}
