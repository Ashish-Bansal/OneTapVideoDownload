package com.phantom.onetapvideodownload.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.samples.apps.iosched.ui.widget.SlidingTabLayout;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.ui.downloads.DownloadsFragment;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerFragmentParent extends Fragment {
    public static final String FRAGMENT_TAG = "main_fragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_pager_parent, container, false);

        PagerAdapter pagerAdapter = new NavigationAdapter(view.getContext(), getChildFragmentManager());
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(getActivity(), R.color.highlight));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(viewPager);
        return view;
    }

    private class NavigationAdapter extends FragmentPagerAdapter {
        private Context mContext;
        private List<String> mTitles;

        List<Fragment> fragmentList = new ArrayList<>();
        NavigationAdapter(Context context, FragmentManager fm) {
            super(fm);
            mContext = context;
            mTitles = new ArrayList<>();
            mTitles.add(mContext.getString(R.string.view_pager_title_downloads));
            mTitles.add(mContext.getString(R.string.view_pager_title_settings));
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f;
            if (fragmentList.size() <= position) {
                switch (position) {
                    case 0:
                        f = new DownloadsFragment();
                        break;
                    case 1:
                        f = new SettingsFragment();
                        break;
                    default:
                        f = new DownloadsFragment();
                }
                fragmentList.add(position, f);
            }
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mTitles.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles.get(position);
        }
    }
}
