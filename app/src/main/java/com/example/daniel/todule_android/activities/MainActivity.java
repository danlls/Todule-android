package com.example.daniel.todule_android.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.provider.ToduleDBContract;
import com.example.daniel.todule_android.utilities.DateTimeUtils;


public class MainActivity extends AppCompatActivity implements ToduleLabelFragment.OnLabelSelectedListener{

    MyPagerAdapter myPagerAdapter;
    ViewPager mViewPager;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(myPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
            @Override
            public void onPageSelected(int position) {
                switch(position){
                    case 0:
                        fabVisibility(true);
                        break;
                    case 1:
                        fabVisibility(false);
                        break;
                    default:
                        fabVisibility(false);
                        break;
                }
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {;
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new ToduleAddFragment(), "add_frag");
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
                if(backStackEntryCount > 0){
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    fabVisibility(false);
                    findViewById(R.id.toolbar).setVisibility(View.GONE);
                }else{
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setTitle("Todule");
                    fabVisibility(true);
                    findViewById(R.id.toolbar).setVisibility(View.VISIBLE);

                }
            }
        });

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
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment=null;
            switch(position) {
                case 0:
                    fragment = Fragment.instantiate(context, ToduleListFragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, ToduleHistoryFragment.class.getName());
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case 0:
                    return getString(R.string.todule_list_page_title);
                case 1:
                    return getString(R.string.todule_history_page_title);
                default:
                    return null;
            }
        }
    }

    public void setReminder(Uri itemUri, long datetimeInMillis){

        Cursor cr = getContentResolver().query(itemUri, ToduleDBContract.TodoEntry.PROJECTION_ALL, null, null, ToduleDBContract.TodoEntry.SORT_ORDER_DEFAULT);
        cr.moveToFirst();
        long itemId = Long.valueOf(itemUri.getLastPathSegment());
        String title = cr.getString(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_TITLE));
        long dueDate = cr.getLong(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_DUE_DATE));
        String dueDateString = DateTimeUtils.dateTimeDiff(dueDate);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setData(Uri.parse(R.string.reminder_intent_scheme + String.valueOf(itemId)));
        intent.putExtra("todule_title", title);
        intent.putExtra("todule_due_date", dueDateString);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, datetimeInMillis, sender);

        cr.close();
    }

    @Override
    public void onLabelSelected(long id) {

        ToduleAddFragment toduleAddFragment = (ToduleAddFragment) getSupportFragmentManager().findFragmentByTag("add_frag");
        toduleAddFragment.setLabel(id);
    }
}


