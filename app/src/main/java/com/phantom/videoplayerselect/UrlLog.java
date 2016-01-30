package com.phantom.videoplayerselect;

import android.content.ClipData;
import android.content.ClipboardManager;
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

import co.dift.ui.SwipeToAction;

public class UrlLog extends AppCompatActivity {
    private UrlList mUrlList;
    private Toolbar toolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView recyclerView;
    UrlAdapter adapter;
    SwipeToAction swipeToAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_log);

        mUrlList = UrlList.getUrlListSingleton(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mUrlList.clearLocalList();
                mUrlList.loadSavedUrls();
                displaySnackbar("URL List updated", null, null);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new UrlAdapter(mUrlList);
        recyclerView.setAdapter(adapter);

        swipeToAction = new SwipeToAction(recyclerView, new SwipeToAction.SwipeListener<Url>() {
            @Override
            public boolean swipeLeft(final Url url) {
                removeUrl(url);
                displaySnackbar(url.getUrl() + " removed", "Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addUrl(url);
                        adapter.notifyDataSetChanged();
                    }
                });
                return true;
            }

            @Override
            public boolean swipeRight(Url itemData) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                Uri copyUri = Uri.parse(itemData.getUrl());
                ClipData clip = ClipData.newUri(getContentResolver(), "URI", copyUri);
                clipboard.setPrimaryClip(clip);
                displaySnackbar("URL copied", null, null);
                return true;
            }

            @Override
            public void onClick(Url itemData) {
                displaySnackbar("Filename : " + itemData.getFilename(), null, null);
            }

            @Override
            public void onLongClick(Url itemData) {
            }
        });
    }

    private void displaySnackbar(String text, String actionName, View.OnClickListener action) {
        Snackbar snack = Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction(actionName, action);

        View v = snack.getView();
        v.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar));
        ((TextView) v.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        ((TextView) v.findViewById(android.support.design.R.id.snackbar_action)).setTextColor(Color.BLACK);

        snack.show();
    }

    private int removeUrl(Url url) {
        int position = UrlList.getUrlListSingleton(this).removeUrl(url);
        adapter.notifyItemRemoved(position);
        return position;
    }

    private void addUrl(Url url) {
        mUrlList.addUrl(url);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear_urls) {
            mUrlList.clearLocalList();
            mUrlList.clearSavedList();
            adapter.notifyDataSetChanged();
            displaySnackbar("URL List cleared", null, null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
