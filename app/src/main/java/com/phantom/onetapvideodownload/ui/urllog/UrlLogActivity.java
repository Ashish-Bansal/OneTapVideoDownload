package com.phantom.onetapvideodownload.ui.urllog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.phantom.onetapvideodownload.AnalyticsApplication;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.Video.Video;

import java.util.ArrayList;

import co.dift.ui.SwipeToAction;

public class UrlLogActivity extends AppCompatActivity {
    private VideoList mVideoList;
    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private UrlAdapter mUrlAdapter;
    private SwipeToAction mSwipeToAction;
    private TextView mEmptyView;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_log);

        mVideoList = VideoList.getVideoListSingleton(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
                displaySnackbar("URL List updated", null, null);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (mToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mUrlAdapter = new UrlAdapter(mVideoList);
        mRecyclerView.setAdapter(mUrlAdapter);


        mEmptyView = (TextView) findViewById(R.id.empty_view);
        evaluateVisibility();
        mSwipeToAction = new SwipeToAction(mRecyclerView, new SwipeToAction.SwipeListener<Video>() {
            @Override
            public boolean swipeLeft(final Video video) {
                removeVideo(video);
                displaySnackbar(video.getUrl() + " removed", "Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addVideo(video);
                        mUrlAdapter.notifyDataSetChanged();
                    }
                });
                return true;
            }

            @Override
            public boolean swipeRight(Video itemData) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                Uri copyUri = Uri.parse(itemData.getUrl());
                ClipData clip = ClipData.newUri(getContentResolver(), "URI", copyUri);
                clipboard.setPrimaryClip(clip);
                displaySnackbar("URL copied", null, null);
                return true;
            }

            @Override
            public void onClick(final Video itemData) {
                displaySnackbar("Filename : " + itemData.getTitle(), "Open", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemData.getUrl()));
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onLongClick(Video itemData) {
            }
        });

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("Activity~" + getClass().getName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
        mTracker.setScreenName("Activity~" + getClass().getName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void displaySnackbar(String text, String actionName, View.OnClickListener action) {
        Snackbar snack = Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction(actionName, action);

        View v = snack.getView();
        v.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_primary));
        ((TextView) v.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        ((TextView) v.findViewById(android.support.design.R.id.snackbar_action)).setTextColor(Color.BLACK);

        snack.show();
    }

    private Video removeVideo(Video video) {
        VideoList.getVideoListSingleton(this).removeVideo(video);
        mUrlAdapter.notifyDataSetChanged();
        evaluateVisibility();
        return video;
    }

    private void reload() {
        mVideoList.reloadVideos();
        mUrlAdapter.notifyDataSetChanged();
        evaluateVisibility();
    }

    private void addVideo(Video video) {
        mVideoList.addVideo(video);
        mUrlAdapter.notifyDataSetChanged();
        evaluateVisibility();
    }

    private void evaluateVisibility() {
        if (mVideoList.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_clear_videos :
                mVideoList.clearLocalList();
                mVideoList.loadSavedVideos();

                final ArrayList<Video> videoList = mVideoList.getVideoList();

                mVideoList.clearLocalList();
                mVideoList.clearSavedVideos();

                mUrlAdapter.notifyDataSetChanged();
                displaySnackbar("URL List cleared", "Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for(Video video : videoList) {
                            mVideoList.addVideo(video);
                        }
                        mUrlAdapter.notifyDataSetChanged();
                    }
                });
                return true;
            case android.R.id.home :
                onBackPressed();
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }
}
