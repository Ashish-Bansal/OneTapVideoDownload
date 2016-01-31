package com.phantom.onetapvideodownload;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class UrlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private UrlList mUrlList;

    public UrlAdapter(UrlList urlList) {
        this.mUrlList = urlList;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mUrlList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_item, parent, false);

        return new UrlViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Url item = mUrlList.getUrl(position);
        UrlViewHolder vh = (UrlViewHolder) holder;
        vh.setUrlText(item.getUrl());
        vh.setMetadataText(item.getMetadata());
        vh.data = item;
    }
}
