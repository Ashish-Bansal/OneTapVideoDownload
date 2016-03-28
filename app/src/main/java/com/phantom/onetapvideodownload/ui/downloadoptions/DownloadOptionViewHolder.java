package com.phantom.onetapvideodownload.ui.downloadoptions;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.phantom.onetapvideodownload.R;

public class DownloadOptionViewHolder extends RecyclerView.ViewHolder {
    private TextView mOptionLabel, mOptionValue;
    private ImageView mOptionImage;
    private Context mContext;

    public DownloadOptionViewHolder(View v) {
        super(v);
        mContext = v.getContext();
        mOptionLabel = (TextView) v.findViewById(R.id.option_label);
        mOptionValue = (TextView) v.findViewById(R.id.option_value);
        mOptionImage = (ImageView) v.findViewById(R.id.option_image);
    }

    public void setOptionLabel(Integer resourceId) {
        mOptionLabel.setText(mContext.getString(resourceId));
    }

    public void setOptionValue(String optionValue) {
        mOptionValue.setText(optionValue);
    }

    public void setOptionImage(int resourceId) {
        mOptionImage.setImageDrawable(ContextCompat.getDrawable(mContext, resourceId));
    }

    public void setOptionOnClickListener(View.OnClickListener onClickListener) {
        itemView.setOnClickListener(onClickListener);
    }
}