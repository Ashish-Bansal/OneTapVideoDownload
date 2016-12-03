package com.phantom.onetapvideodownload.ui.domainblacklist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.phantom.onetapvideodownload.R;

class BlacklistDomainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private BlacklistDomainList mBlacklistDomainList;
    BlacklistDomainAdapter(BlacklistDomainList blacklistDomainList) {
        this.mBlacklistDomainList = blacklistDomainList;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mBlacklistDomainList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.blacklisted_url_view_item, parent, false);

        return new BlacklistDomainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String url = mBlacklistDomainList.getUrl(position);
        BlacklistDomainViewHolder vh = (BlacklistDomainViewHolder) holder;
        vh.setUrlText(url);
        vh.data = url;
    }
}
