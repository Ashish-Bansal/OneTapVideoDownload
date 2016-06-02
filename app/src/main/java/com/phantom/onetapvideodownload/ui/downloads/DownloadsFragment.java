package com.phantom.onetapvideodownload.ui.downloads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.downloader.DownloadManager;
import com.phantom.onetapvideodownload.ui.UsageInstruction;
import com.phantom.onetapvideodownload.utils.OnDownloadChangeListener;

public class DownloadsFragment extends Fragment
        implements OnDownloadChangeListener {
    private RecyclerView mRecyclerView;
    private DownloadAdapter mDownloadAdapter;
    private View mEmptyView;
    private Context mContext;

    public DownloadsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_downloads, container, false);
        mContext = getActivity();
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.downloadRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
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
                Intent usageInstructionIntent = new Intent(mContext, UsageInstruction.class);
                startActivity(usageInstructionIntent);
            }
        });

        DownloadManager.addOnDownloadChangeListener(DownloadsFragment.this);
        evaluateVisibility();
        return rootView;
    }

    private void evaluateVisibility() {
        DownloadManager downloadManager = DownloadManager.getDownloadManagerInstance();
        if (downloadManager == null || downloadManager.getDownloadCount() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDownloadAdapter != null) {
            mDownloadAdapter.onStop();
        }
    }

    @Override
    public void onDownloadAdded() {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDownloadAdapter.notifyDataSetChanged();
            }
        });
        evaluateVisibility();
    }

    @Override
    public void onDownloadRemoved() {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDownloadAdapter.notifyDataSetChanged();
            }
        });
        evaluateVisibility();
    }

    @Override
    public void onDownloadInfoUpdated() {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDownloadAdapter.notifyDataSetChanged();
            }
        });
        evaluateVisibility();
    }
}
