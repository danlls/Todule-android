package com.danlls.daniel.todule_android.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.danlls.daniel.todule_android.R;


public class MainActivity extends AppCompatActivity implements
        ToduleLabelFragment.OnLabelSelectedListener,
        NavigationView.OnNavigationItemSelectedListener{

    Context context;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                fabVisibility(false);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Fragment f = getSupportFragmentManager().findFragmentByTag("list_frag1");

                if (f != null){
                    fabVisibility(true);
                }
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {;
                ToduleAddFragment frag = new ToduleAddFragment();
                Bundle args = new Bundle();
                args.putString("mode", "create_entry");
                frag.setArguments(args);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom);
                ft.replace(R.id.fragment_container, frag, "add_frag");
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                updateFragmentPresentation();
            }
        });

        // Check if activity is launched with todule_id (From notification)
        final long entryId = getIntent().getLongExtra("todule_id", -1L);
        if(entryId != -1L){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ToduleDetailFragment frag = ToduleDetailFragment.newInstance(entryId);
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.fragment_container, frag)
                            .addToBackStack(null)
                            .commit();
                }
            }, 500);

        }

        // default fragment
        if (savedInstanceState == null){
            navigationView.getMenu().getItem(0).setChecked(true);
            ToduleListFragment frag = ToduleListFragment.newInstance(1);
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.fragment_container, frag, "list_frag1")
                    .commit();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void updateFragmentPresentation(){
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if(backStackEntryCount > 0){
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            fabVisibility(false);
        }else{
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }
    }

    public void fabVisibility(boolean show){
        FloatingActionButton fabBtn = (FloatingActionButton)findViewById(R.id.add_fab);
        if (show) fabBtn.show();
        else fabBtn.hide();
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0){
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
        hideSoftKeyboard(true);
    }

    public void hideSoftKeyboard (boolean hide)
    {
        if (this.getCurrentFocus() == null) return;
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(hide){
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getApplicationWindowToken(), 0);
        } else {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int loaderId = 0;
        switch(item.getGroupId()){
            case R.id.navgroup_list:
                switch(item.getItemId()){
                    case R.id.nav_incomplete:
                        loaderId = 1;
                        break;
                    case R.id.nav_expired:
                        loaderId = 2;
                        break;
                    case R.id.nav_completed:
                        loaderId = 3;
                        break;
                    case R.id.nav_archive:
                        loaderId = 4;
                        break;
                    case R.id.nav_deleted:
                        loaderId = 5;
                        break;
                }
                navigationView.setCheckedItem(item.getItemId());
                ToduleListFragment frag = ToduleListFragment.newInstance(loaderId);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.fragment_container, frag, "list_frag" + String.valueOf(loaderId))
                        .commit();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.navgroup_setting:
                switch(item.getItemId()){
                    case R.id.nav_label:
                        navigationView.setCheckedItem(item.getItemId());
                        ToduleLabelFragment labelFrag = ToduleLabelFragment.newInstance(false, null);
                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                .replace(R.id.fragment_container, labelFrag)
                                .commit();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        break;
                }
                return true;
        }
        return true;
    }





    @Override
    public void onLabelSelected(long id) {

        ToduleAddFragment toduleAddFragment = (ToduleAddFragment) getSupportFragmentManager().findFragmentByTag("add_frag");
        toduleAddFragment.setLabel(id);
    }


    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccentDark));

    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        Window window = getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }
}


