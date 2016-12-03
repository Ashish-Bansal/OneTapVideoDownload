package com.phantom.onetapvideodownload.Video;

import android.content.Context;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.ui.downloadoptions.DownloadOptionItem;
import com.phantom.onetapvideodownload.ui.downloadoptions.DownloadOptionAdapter;
import com.phantom.onetapvideodownload.ui.downloadoptions.DownloadOptionIds;
import com.phantom.utils.Global;

import java.util.ArrayList;
import java.util.List;

public class BrowserVideo implements Video {
    private String mTitle, mUrl, mPackageName;
    private long mDatabaseId = -1;

    public BrowserVideo(String url) {
        mUrl = url;
        mTitle = Global.getFilenameFromUrl(url);
        if (mTitle.isEmpty()) {
            mTitle = "otv_unnamed_video.mp4";
        }
    }

    public BrowserVideo(String url, String title) {
        mUrl = url;
        mTitle = title;
        if (mTitle == null || mTitle.isEmpty()) {
            mTitle = Global.getFilenameFromUrl(url);
            if (mTitle == null || mTitle.isEmpty()) {
                mTitle = "otv_unnamed_video.mp4";
            }
        }

    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public long getDatabaseId() {
        return mDatabaseId;
    }

    @Override
    public void setDatabaseId(long databaseId) {
        mDatabaseId = databaseId;
    }

    @Override
    public List<DownloadOptionItem> getOptions(final Context context, final DownloadOptionAdapter downloadOptionAdapter) {
        List<DownloadOptionItem> options = new ArrayList<>();
        options.add(new DownloadOptionItem(DownloadOptionIds.Filename,
                R.drawable.file,
                R.string.filename,
                Global.getValidatedFilename(getTitle()),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final DownloadOptionItem filenameOptionItem = downloadOptionAdapter.getOptionItem(DownloadOptionIds.Filename);
                        new MaterialDialog.Builder(context)
                                .title(R.string.enter_filename)
                                .inputType(InputType.TYPE_CLASS_TEXT)
                                .input("", filenameOptionItem.getOptionValue(), new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        filenameOptionItem.setOptionValue(Global.getValidatedFilename(input.toString()));
                                        downloadOptionAdapter.setOptionItem(filenameOptionItem);
                                    }
                                }).show();
                    }
                }
        ));

        return options;
    }

    public boolean isResourceAvailable() {
        return Global.isResourceAvailable(mUrl);
    }

    @Override
    public String getPackageName() {
        return mPackageName;
    }

    @Override
    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }
}
