package com.phantom.onetapvideodownload.downloader;

import android.view.View;

import com.phantom.onetapvideodownload.DownloadOptionIds;

public class DownloadOptionItem {
    private Integer mIconId, mLabelStringId;
    private String mValueString;
    private View.OnClickListener mOnClickListener;
    private DownloadOptionIds mDownloadOptionId;

    public DownloadOptionItem(DownloadOptionIds downloadOptionId, Integer iconId,
                              Integer labelStringId, String valueString,
                              View.OnClickListener onClickListener) {
        mDownloadOptionId = downloadOptionId;
        mIconId = iconId;
        mLabelStringId = labelStringId;
        mValueString = valueString;
        mOnClickListener = onClickListener;
    }

    public Integer getIcon() {
        return mIconId;
    }

    public Integer getOptionLabel() {
        return mLabelStringId;
    }

    public String getOptionValue() {
        return mValueString;
    }

    public DownloadOptionIds getDownloadOptionId() {
        return mDownloadOptionId;
    }

    public void setOptionValue(String value) {
        mValueString = value;
    }

    public View.OnClickListener getOnClickListener() {
        return mOnClickListener;
    }
}
