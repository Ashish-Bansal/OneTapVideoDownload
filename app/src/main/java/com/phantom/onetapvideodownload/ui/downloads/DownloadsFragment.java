package com.phantom.onetapvideodownload.ui.downloads;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.downloader.DownloadManager;
import com.phantom.onetapvideodownload.ui.UsageInstruction;
import com.phantom.utils.OnDownloadChangeListener;

public class DownloadsFragment extends Fragment implements OnDownloadChangeListener {
    public static final String TAG = "DownloadsFragment";
    private RecyclerView mRecyclerView;
    private DownloadAdapter mDownloadAdapter;
    private View mEmptyView;
    private Boolean mBounded;
    private DownloadManager mDownloadManager;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mDownloadManager = null;
            evaluateVisibility();
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            DownloadManager.LocalBinder mLocalBinder = (DownloadManager.LocalBinder)service;
            mDownloadManager = mLocalBinder.getServiceInstance();
            mDownloadManager.addOnDownloadChangeListener(DownloadsFragment.this);
            evaluateVisibility();
            onReset();
        }
    };

    public DownloadsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_downloads, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.downloadRecyclerView);
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDownloadAdapter = new DownloadAdapter(getActivity());
        mRecyclerView.setAdapter(mDownloadAdapter);
        mEmptyView = rootView.findViewById(R.id.empty_view);
        FloatingActionButton usageInstructionButton = (FloatingActionButton)rootView.findViewById(R.id.usage_instruction_button);
        usageInstructionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent usageInstructionIntent = new Intent(getActivity(), UsageInstruction.class);
                startActivity(usageInstructionIntent);
            }
        });

        return rootView;
    }

    private void evaluateVisibility() {
        if (mDownloadManager == null) {
            Log.e(TAG, "DownloadManagerInstance is null");
            return;
        }

        if (mDownloadManager.getDownloadCount() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBounded == null || !mBounded) {
            getActivity().startService(DownloadManager.getActionStartService());
            Intent mIntent = new Intent(getActivity(), DownloadManager.class);
            getActivity().bindService(mIntent, mConnection, Context.BIND_ABOVE_CLIENT);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mBounded != null && mBounded) {
            mDownloadManager.removeOnDownloadChangeListener(this);
            getActivity().unbindService(mConnection);
            mBounded = false;
        }

        if (mDownloadAdapter != null) {
            mDownloadAdapter.onStop();
        }
    }

    @Override
    public void onDownloadAdded(final int position) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDownloadAdapter.notifyItemInserted(position);
                evaluateVisibility();
            }
        });
    }

    @Override
    public void onDownloadRemoved(final int position) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDownloadAdapter.notifyItemRemoved(position);
                evaluateVisibility();
            }
        });
    }

    @Override
    public void onDownloadInfoUpdated(final int position) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDownloadAdapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    public void onReset() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDownloadAdapter.notifyDataSetChanged();
                evaluateVisibility();
            }
        });
    }
}
