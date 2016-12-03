package com.phantom.onetapvideodownload.ui.downloadoptions;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.phantom.onetapvideodownload.R;

class DownloadOptionViewHolder extends RecyclerView.ViewHolder {
    private TextView mOptionLabel, mOptionValue;
    private ImageView mOptionImage;
    private Context mContext;

    DownloadOptionViewHolder(View v) {
        super(v);
        mContext = v.getContext();
        mOptionLabel = (TextView) v.findViewById(R.id.option_label);
        mOptionValue = (TextView) v.findViewById(R.id.option_value);
        mOptionImage = (ImageView) v.findViewById(R.id.option_image);
    }

    void setOptionLabel(Integer resourceId) {
        mOptionLabel.setText(mContext.getString(resourceId));
    }

    void setOptionValue(String optionValue) {
        mOptionValue.setText(optionValue);
    }

    void setOptionImage(int resourceId) {
        mOptionImage.setImageDrawable(ContextCompat.getDrawable(mContext, resourceId));
    }

    void setOptionOnClickListener(View.OnClickListener onClickListener) {
        itemView.setOnClickListener(onClickListener);
    }
}