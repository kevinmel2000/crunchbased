package com.github.zyro.crunchbased.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewConfiguration;

import com.github.zyro.crunchbased.R;
import com.github.zyro.crunchbased.service.CrunchbaseClient;
import com.github.zyro.crunchbased.util.HeaderTransformer;
import com.github.zyro.crunchbased.util.RefreshMessage;
import com.github.zyro.crunchbased.util.SearchDialog;
import com.googlecode.androidannotations.annotations.*;
import com.koushikdutta.async.future.FutureCallback;

import java.lang.reflect.Field;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

/** Common behaviour, encapsulated in an abstract Activity. */
@EActivity
@OptionsMenu(R.menu.menu)
public abstract class BaseActivity<T> extends FragmentActivity
        implements PullToRefreshAttacher.OnRefreshListener, FutureCallback<T> {

    /** Access to remote data. */
    @Bean
    protected CrunchbaseClient client;

    protected PullToRefreshAttacher attacher;

    /** Perform additional custom configuration. */
    @Override
    public void onConfigurationChanged(final Configuration config) {
        super.onConfigurationChanged(config);
        attacher.getHeaderTransformer().onViewCreated(this, attacher.getHeaderView());
    }

    /** Request common window features, BaseActivity has no view of its own. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Force the overflow menu even if there is a physical menu button.
        try {
            final ViewConfiguration config = ViewConfiguration.get(this);
            final Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch(final Exception e) {} // Don't care if it doesn't work.

        final PullToRefreshAttacher.Options options =
                new PullToRefreshAttacher.Options();
        options.headerTransformer = new HeaderTransformer();
        attacher = PullToRefreshAttacher.get(this, options);
    }

    /** Initialize common state elements. */
    @AfterViews
    public void initBaseState() {
        //getActionBar().setDisplayShowTitleEnabled(false);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        /*((Switch) findViewById(R.id.loadImagesSwitch))
                .setChecked(preferences.loadImages().get());*/
    }

    public void addRefreshableView(final View view) {
        attacher.addRefreshableView(view, this);
    }

    @Override
    public void onRefreshStarted(final View view) {
        refreshButton();
    }

    public void onRefreshCompleted() {
        attacher.setRefreshComplete();
    }

    @UiThread
    public void refreshStarted() {
        RefreshMessage.hideRefreshFailed(this);
    }

    public abstract void refresh();

    public abstract void refreshDone(final T entity);

    @UiThread
    public void refreshFailed() {
        onRefreshCompleted();
        RefreshMessage.showRefreshFailed(this);
    }

    @Override
    public void onCompleted(final Exception e, final T entity) {
        if(e == null) {
            refreshDone(entity);
        }
        else {
            refreshFailed();
        }
    }

    @OptionsItem(android.R.id.home)
    public void homeButton() {
        super.onBackPressed();
    }

    @OptionsItem(R.id.refreshButton)
    public void refreshButton() {
        attacher.setRefreshing(true);
        refresh();
    }

    /** Listener for the action bar search button. */
    @OptionsItem(R.id.searchButton)
    public void searchButton() {
        SearchDialog.showSearchDialog(this);
    }

}