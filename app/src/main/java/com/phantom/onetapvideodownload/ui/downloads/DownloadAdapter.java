package com.phantom.onetapvideodownload.ui.downloads;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.downloader.DownloadManager;
import com.phantom.onetapvideodownload.downloader.downloadinfo.DownloadInfo;

import java.util.ArrayList;
import java.util.Collections;

class DownloadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ActionMode.Callback  {
    private DownloadManager mDownloadManager;
    private boolean mBounded;
    private Context mContext;
    private SparseArray<View> mSelectedItems;
    private ActionMode mActionMode;

    DownloadAdapter(Context context) {
        mContext = context;
        mSelectedItems = new SparseArray<>();
        context.startService(DownloadManager.getActionStartService());
        Intent mIntent = new Intent(context, DownloadManager.class);
        context.bindService(mIntent, mConnection, Context.BIND_ABOVE_CLIENT);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mDownloadManager = null;
            notifyDataSetChanged();
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            DownloadManager.LocalBinder mLocalBinder = (DownloadManager.LocalBinder)service;
            mDownloadManager = mLocalBinder.getServiceInstance();
            notifyDataSetChanged();
        }
    };

    void onStop() {
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

        final DownloadViewHolder vh = new DownloadViewHolder(view);
        vh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mActionMode == null) {
                    new MaterialDialog.Builder(mContext)
                            .items(mDownloadManager.getOptions(vh.getLayoutPosition()))
                            .itemsCallback(mDownloadManager.getOptionCallback(vh.getLayoutPosition()))
                            .show();
                } else {
                    itemClicked(vh.getLayoutPosition(), view);
                }
            }
        });

        vh.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mActionMode != null) {
                    return false;
                }

                mActionMode = ((AppCompatActivity)mContext).startSupportActionMode(DownloadAdapter.this);
                int index = vh.getLayoutPosition();
                itemClicked(index, view);
                return true;
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        DownloadViewHolder vh = (DownloadViewHolder) holder;
        vh.setDownloadTitle(mDownloadManager.getFilename(position));
        vh.setDownloadUrl(mDownloadManager.getUrl(position));
        vh.setImageForView(mDownloadManager.getPackageDrawable(position));
        vh.setStatus(mDownloadManager.getStatus(position));
        if (mDownloadManager.getStatus(position) == DownloadInfo.Status.Downloading) {
            if (mDownloadManager.getDownloadProgress(position) == 0) {
                vh.setProgressBarState(true, true);
            } else {
                vh.setProgressBarState(true, false);
            }
        } else {
            vh.setProgressBarState(false, false);
        }

        vh.setProgress(mDownloadManager.getDownloadProgress(position));
        if (isSelected(position)) {
            vh.setSelectedTickVisibility(true);
        } else {
            vh.setSelectedTickVisibility(false);
        }
    }

    private void itemClicked(int index, View view) {
        toggleSelection(index, view);
        String title = String.valueOf(selectedItemsCount());
        mActionMode.setTitle(title);
        if (selectedItemsCount() == 0) {
            mActionMode.finish();
        }
    }

    private boolean isSelected(int pos) {
        return mSelectedItems.get(pos) != null;
    }

    private void toggleSelection(int pos, @Nullable View view) {
        if (mSelectedItems.get(pos) != null) {
            mSelectedItems.remove(pos);
        } else {
            mSelectedItems.put(pos, view);
        }

        ImageView tick = (ImageView) view.findViewById(R.id.tick);
        if (isSelected(pos)) {
            tick.setVisibility(View.VISIBLE);
        } else {
            tick.setVisibility(View.GONE);
        }
    }

    private void clearSelections() {
        for(Integer pos : getSelectedItems()) {
            if (mSelectedItems.get(pos) == null) {
                continue;
            }

            ImageView tick = (ImageView) mSelectedItems.get(pos).findViewById(R.id.tick);
            tick.setVisibility(View.GONE);
            notifyItemChanged(pos);
        }
        mSelectedItems.clear();
    }

    private int selectedItemsCount() {
        return mSelectedItems.size();
    }

    private ArrayList<Integer> getSelectedItems() {
        ArrayList<Integer> keySet = new ArrayList<>();
        for(int i = 0; i < mSelectedItems.size(); i++) {
            keySet.add(mSelectedItems.keyAt(i));
        }
        return keySet;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_downloads_selected, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_delete:
                ArrayList<Integer> keys = getSelectedItems();
                Collections.sort(keys);
                Collections.reverse(keys);
                for (Integer pos : keys) {
                    mDownloadManager.removeDownloadByIndex(pos);
                }
                actionMode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mActionMode = null;
        clearSelections();
    }
}
