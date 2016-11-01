package com.phantom.onetapvideodownload.ui.domainblacklist;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.phantom.onetapvideodownload.AnalyticsApplication;
import com.phantom.onetapvideodownload.R;
import com.phantom.utils.Global;

import java.util.List;

import co.dift.ui.SwipeToAction;

public class BlacklistDomainActivity extends AppCompatActivity {
    private BlacklistDomainList mBlacklistDomainList;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private BlacklistDomainAdapter mBlacklistDomainAdapter;
    private TextView mEmptyView;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist_url);

        mBlacklistDomainList = BlacklistDomainList.getUrlListSingleTon(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
                displaySnackbar("Blacklist URL List updated", null, null);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mBlacklistDomainAdapter = new BlacklistDomainAdapter(mBlacklistDomainList);
        mRecyclerView.setAdapter(mBlacklistDomainAdapter);


        mEmptyView = (TextView) findViewById(R.id.empty_view);
        evaluateVisibility();
        SwipeToAction swipeToAction = new SwipeToAction(mRecyclerView, new SwipeToAction.SwipeListener<String>() {
            @Override
            public boolean swipeLeft(final String url) {
                removeUrl(url);
                displaySnackbar(url + " removed", "Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addUrl(url);
                    }
                });
                return true;
            }

            @Override
            public boolean swipeRight(String url) {
                Global.copyUrlToClipboard(getApplicationContext(), url);
                displaySnackbar(getResources().getString(R.string.blacklist_domain_copied), null, null);
                return true;
            }

            @Override
            public void onClick(final String domain) {
                displaySnackbar(getResources().getString(R.string.edit_blacklisted_domain),
                        getResources().getString(R.string.edit),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new MaterialDialog.Builder(BlacklistDomainActivity.this)
                                        .title(R.string.edit_blacklisted_domain)
                                        .inputType(InputType.TYPE_CLASS_TEXT)
                                        .input(getResources().getString(R.string.blacklist_domain_hint), domain, false, new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog, CharSequence url) {
                                                String newUrl = getHostNameFromUrl(url.toString());
                                                if (newUrl == null || newUrl.isEmpty()) {
                                                    Toast.makeText(BlacklistDomainActivity.this, R.string.invalid_domain, Toast.LENGTH_LONG).show();
                                                } else {
                                                    editUrl(domain, newUrl);
                                                }
                                            }
                                        }).show();
                            }
                        }
                );
            }

            @Override
            public void onLongClick(String url) {
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

    private String removeUrl(String url) {
        mBlacklistDomainList.removeUrl(url);
        mBlacklistDomainAdapter.notifyDataSetChanged();
        evaluateVisibility();
        return url;
    }

    private void editUrl(String originalUrl, String editedUrl) {
        mBlacklistDomainList.updateUrl(originalUrl, editedUrl);
        mBlacklistDomainAdapter.notifyDataSetChanged();
    }

    private void reload() {
        mBlacklistDomainList.reloadUrls();
        mBlacklistDomainAdapter.notifyDataSetChanged();
        evaluateVisibility();
    }

    private void addUrl(String url) {
        mBlacklistDomainList.addUrl(url);
        mBlacklistDomainAdapter.notifyDataSetChanged();
        evaluateVisibility();
    }

    private void evaluateVisibility() {
        if (mBlacklistDomainList.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_blacklist_url, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_clear_videos :
                mBlacklistDomainList.clearLocalList();
                mBlacklistDomainList.loadSavedUrls();

                final List<Pair<Long, String>> urlList = mBlacklistDomainList.getUrlList();

                mBlacklistDomainList.clearLocalList();
                mBlacklistDomainList.clearSavedUrls();

                mBlacklistDomainAdapter.notifyDataSetChanged();
                displaySnackbar("URL List cleared", "Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for(Pair<Long, String> p : urlList) {
                            mBlacklistDomainList.addUrl(p.second);
                        }
                        mBlacklistDomainAdapter.notifyDataSetChanged();
                    }
                });
                return true;
            case android.R.id.home :
                onBackPressed();
                return true;
            case R.id.add_url:
                new MaterialDialog.Builder(this)
                    .title(R.string.add_new_domain)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input(R.string.blacklist_domain_hint, R.string.empty, false, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence url) {
                            String hostname = getHostNameFromUrl(url.toString());
                            if (hostname == null || hostname.isEmpty()) {
                                Toast.makeText(BlacklistDomainActivity.this, R.string.invalid_domain, Toast.LENGTH_LONG).show();
                            } else {
                                addUrl(hostname);
                            }
                        }
                    }).show();
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    // Used custom logic instead of Global.getDomain() to allow URL invalidity tolerance
    String getHostNameFromUrl(String url) {
        try {
            int doubleSlash = url.indexOf("//");
            if (doubleSlash == -1) {
                doubleSlash = 0;
            } else {
                doubleSlash += 2;
            }

            int hostNameEnd = url.indexOf('/', doubleSlash);
            if (hostNameEnd == -1) {
                hostNameEnd = url.length();
            }

            return url.substring(doubleSlash, hostNameEnd);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }
}
