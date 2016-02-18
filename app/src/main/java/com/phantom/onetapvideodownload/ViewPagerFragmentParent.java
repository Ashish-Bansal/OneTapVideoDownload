package com.phantom.onetapvideodownload;

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

import java.util.ArrayList;
import java.util.List;

public class ViewPagerFragmentParent extends Fragment {
    public static final String FRAGMENT_TAG = "main_fragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_pager_parent, container, false);

        PagerAdapter pagerAdapter = new NavigationAdapter(getChildFragmentManager());
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(getActivity(), R.color.amber));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(viewPager);
        return view;
    }

    private class NavigationAdapter extends FragmentPagerAdapter {

        private final String[] mTitles = new String[] { "Downloads", "Settings" };

        List<Fragment> fragmentList = new ArrayList<>();
        public NavigationAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f;
            if (fragmentList.size() <= position) {
                switch (position) {
                    case 0:
                        f = new SettingsFragment();
                    case 1:
                        f = new SettingsFragment();
                    default:
                        f = new SettingsFragment();
                }
                fragmentList.add(position, f);
            }
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }
}
