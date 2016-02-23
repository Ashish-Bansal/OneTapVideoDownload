package com.phantom.onetapvideodownload.Video;

import android.content.Context;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.DownloadOptionAdapter;
import com.phantom.onetapvideodownload.DownloadOptionIds;
import com.phantom.onetapvideodownload.Global;
import com.phantom.onetapvideodownload.MainActivity;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.downloader.DownloadOptionItem;

import java.util.ArrayList;
import java.util.List;

public class BrowserVideo implements Video {
    private String mTitle, mUrl;
    private long mDatabaseId = -1;
    private Context mContext;

    public BrowserVideo(Context context, String url) {
        mContext = context;
        mUrl = url;
        mTitle = Global.getFilenameFromUrl(url);
        if (mTitle.isEmpty()) {
            mTitle = "otv_unnamed_video.mp4";
        }
    }

    public BrowserVideo(Context context, String url, String title) {
        mContext = context;
        mUrl = url;
        mTitle = title;
        if (mTitle.isEmpty()) {
            mTitle = Global.getFilenameFromUrl(url);
            if (mTitle.isEmpty()) {
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
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public List<DownloadOptionItem> getOptions() {
        List<DownloadOptionItem> options = new ArrayList<>();
        options.add(new DownloadOptionItem(DownloadOptionIds.Filename,
                R.drawable.file,
                R.string.filename,
                Global.getValidatedFilename(getTitle()),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final DownloadOptionAdapter downloadOptionAdapter =
                                MainActivity.getDownloadOptionAdapter();
                        final DownloadOptionItem filenameOptionItem = downloadOptionAdapter.getOptionItem(DownloadOptionIds.Filename);
                        new MaterialDialog.Builder(mContext)
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
}
