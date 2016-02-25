package com.phantom.onetapvideodownload;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.phantom.onetapvideodownload.downloader.DownloadManager;

public class DownloadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements DownloadManager.ServiceCallbacks {
    private DownloadManager mDownloadManager;
    private boolean mBounded;
    private Context mContext;

    public DownloadAdapter(Context context) {
        mContext = context;
        context.startService(DownloadManager.getActionStartService());
        Intent mIntent = new Intent(context, DownloadManager.class);
        context.bindService(mIntent, mConnection, context.BIND_AUTO_CREATE);
    }

    ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(mContext, "Download Service is disconnected", Toast.LENGTH_LONG).show();
            mBounded = false;
            mDownloadManager = null;
            notifyDataSetChanged();
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(mContext, "Download Service is connected", Toast.LENGTH_LONG).show();
            mBounded = true;
            DownloadManager.LocalBinder mLocalBinder = (DownloadManager.LocalBinder)service;
            mDownloadManager = mLocalBinder.getServiceInstance();
            mDownloadManager.registerCallbacks(DownloadAdapter.this);
            notifyDataSetChanged();
        }
    };

    public void onStop() {
        if(mBounded) {
            mContext.unbindService(mConnection);
            mBounded = false;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        if (mDownloadManager == null) {
            return 0;
        }

        return mDownloadManager.getDownloadCount();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.download_item, parent, false);

        return new DownloadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DownloadViewHolder vh = (DownloadViewHolder) holder;
        vh.setDownloadTitle(mDownloadManager.getFilename(position));
        vh.setDownloadUrl(mDownloadManager.getUrl(position));
        vh.setOnClickListener(mDownloadManager.getOptions(position), mDownloadManager.getOptionCallback(position));
        vh.setImageForView(mDownloadManager.getPackageDrawable(position));
    }

    @Override
    public void onDownloadAdded() {
        ((MainActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDownloadInfoUpdated() {
        ((MainActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }
}
