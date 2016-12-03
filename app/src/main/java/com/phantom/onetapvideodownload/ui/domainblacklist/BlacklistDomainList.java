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

    private static BlacklistDomainList mBlacklistDomainList;
    private List<Pair<Long, String>> mList = new ArrayList<>();
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

    void sortList() {
        Collections.sort(mList, new Comparator<Pair<Long, String>>() {
            @Override
            public int compare(Pair<Long, String> lhs, Pair<Long, String> rhs) {
                return lhs.first < rhs.first ? 1 : 0;
            }
        });
    }

    void addUrl(String url) {
        long urlId = mDomainBlacklistDatabase.addUrl(url);
        mList.add(new Pair<>(urlId, url));
        sortList();
    }

    void removeUrl(String url) {
        mDomainBlacklistDatabase.deleteUrl(url);
        for (int i = mList.size() - 1; i > -1; i--) {
            if (mList.get(i).second.equals(url) ) {
                mList.remove(i);
            }
        }
    }

    int size() {
        return mList.size();
    }

    String getUrl(int pos) {
        return mList.get(pos).second;
    }

    void clearLocalList() {
        mList.clear();
    }

    void clearSavedUrls() {
        mDomainBlacklistDatabase.clearDatabase();
    }

    void loadSavedUrls() {
        mList = mDomainBlacklistDatabase.getAllUrls();
    }

    List<Pair<Long, String>> getUrlList() {
        return new ArrayList<>(mList);
    }

    boolean isEmpty() {
        return size() <= 0;
    }

    void reloadUrls() {
        clearLocalList();
        loadSavedUrls();
    }

    void updateUrl(String oldUrl, String newUrl) {
        mDomainBlacklistDatabase.updateUrl(oldUrl, newUrl);
        updateUrlInMemory(oldUrl, newUrl);
    }

    private void updateUrlInMemory(String oldUrl, String newUrl) {
        for(int i = 0; i < mList.size(); i++) {
            Pair<Long, String> p = mList.get(i);
            if (p.second.equals(oldUrl)) {
                mList.add(i, new Pair<>(p.first, newUrl));
                mList.remove(i + 1);
            }
        }
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
