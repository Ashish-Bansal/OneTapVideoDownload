package com.phantom.onetapvideodownload.ui.urllog;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.Video.Video;

public class UrlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private VideoList mVideoList;

    public UrlAdapter(VideoList urlList) {
        this.mVideoList = urlList;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mVideoList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_item, parent, false);

        return new UrlViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Video video = mVideoList.getVideo(position);
        UrlViewHolder vh = (UrlViewHolder) holder;
        vh.setUrlText(video.getUrl());
        vh.setMetadataText(video.getTitle());
        vh.data = video;
    }
}
