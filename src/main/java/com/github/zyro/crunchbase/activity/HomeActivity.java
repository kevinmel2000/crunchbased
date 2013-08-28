package com.github.zyro.crunchbase.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;
import com.github.zyro.crunchbase.fragment.HomeBiggestFragment_;
import com.github.zyro.crunchbase.fragment.HomeRecentFragment_;
import com.github.zyro.crunchbase.fragment.HomeTrendingFragment_;
import com.github.zyro.crunchbase.R;
import com.github.zyro.crunchbase.fragment.HomeFragment;
import com.github.zyro.crunchbase.service.ClientException;
import com.github.zyro.crunchbase.util.FormatUtils;
import com.github.zyro.crunchbase.util.HomeData;
import com.googlecode.androidannotations.annotations.*;

import java.util.ArrayList;
import java.util.List;

/** Activity to handle the application home area. */
@EActivity(R.layout.home)
public class HomeActivity extends BaseActivity {

    /** Pager adapter for home page tabs. */
    protected HomePagerAdapter adapter;

    /** Initialize tabs and associated view pager. */
    @AfterViews
    public void initState() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        adapter = new HomePagerAdapter(getSupportFragmentManager());

        final ViewPager pager = (ViewPager) findViewById(R.id.homeContainer);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(adapter.getCount());
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int index) {
                getActionBar().setSelectedNavigationItem(index);
            }
        });

        final ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                pager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}
            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}
        };

        final ActionBar.Tab trendingTab = actionBar.newTab()
                .setText(R.string.trending_header)
                .setTabListener(tabListener);
        actionBar.addTab(trendingTab);

        final ActionBar.Tab recentTab = actionBar.newTab()
                .setText(R.string.recent_header)
                .setTabListener(tabListener);
        actionBar.addTab(recentTab);

        final ActionBar.Tab biggestTab = actionBar.newTab()
                .setText(R.string.biggest_header)
                .setTabListener(tabListener);
        actionBar.addTab(biggestTab);

        refreshContents();
    }

    /** Begin a data refresh attempt. */
    @Background
    public void refreshContents() {
        refreshContentsStarted();
        try {
            final HomeData data = client.getHomeData();
            refreshContentsDone(data);
        }
        catch(final ClientException e) {
            Log.e("", "FAIL", e);
            refreshContentsFailed();
        }
    }

    /** Refresh started action. */
    @UiThread
    public void refreshContentsStarted() {
        invalidateOptionsMenu();
        menu.findItem(R.id.refreshButton).setVisible(false);
        setProgressBarIndeterminateVisibility(true);

        for(final HomeFragment fragment : adapter.getAll()) {
            fragment.refreshStarted();
        }
    }

    /** Refresh complete action. */
    @UiThread
    public void refreshContentsDone(final HomeData data) {
        for(final HomeFragment fragment : adapter.getAll()) {
            fragment.refreshContents(data);
            fragment.refreshDone();
        }

        setProgressBarIndeterminateVisibility(false);
        invalidateOptionsMenu();

        Toast.makeText(this, getString(R.string.refreshed) +
                FormatUtils.formatTimestamp(System.currentTimeMillis()),
                Toast.LENGTH_SHORT).show();
    }

    /** Refresh failed action. */
    @UiThread
    public void refreshContentsFailed() {
        setProgressBarIndeterminateVisibility(false);
        invalidateOptionsMenu();

        Toast.makeText(this, getString(R.string.refresh_failed),
                Toast.LENGTH_SHORT).show();
    }

    /** Refresh button handler. */
    @OptionsItem(R.id.refreshButton)
    public void refreshButton() {
        refreshContents();
    }

    /** Home button handler. */
    /*@OptionsItem(android.R.id.home)
    public void homeButton() {
        slidingMenu.toggle();
    }*/

    /** Adapter for tab fragments on the application home screen. */
    private class HomePagerAdapter extends FragmentPagerAdapter {

        /** Internal list of fragments. */
        private List<HomeFragment> fragments;

        /** Initialise with appropriate fragment manager instance. */
        public HomePagerAdapter(final FragmentManager fragmentManager) {
            super(fragmentManager);

            fragments = new ArrayList<HomeFragment>();
            fragments.add(new HomeTrendingFragment_());
            fragments.add(new HomeRecentFragment_());
            fragments.add(new HomeBiggestFragment_());

            this.notifyDataSetChanged();
        }

        /** Get the full list of fragments this adapter is holding. */
        public List<HomeFragment> getAll() {
            return fragments;
        }

        /** Get the fragment at a specific page/tab index. */
        @Override
        public HomeFragment getItem(final int index) {
            return fragments.get(index);
        }

        /** Get the total number of pages/tabs. */
        @Override
        public int getCount() {
            return fragments.size();
        }

    }

}