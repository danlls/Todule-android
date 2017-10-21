package com.example.daniel.todule_android.activities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ScrollView;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.adapter.MainCursorAdapter;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoEntry;

/**
 * Created by danieL on 8/1/2017.
 */

public class ToduleListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {
    private static final int LOADER_ID = 1;
    MainCursorAdapter mAdapter;
    private ListView listView;
    private ScrollView emptyView;
    private SwipeRefreshLayout swipeContainer;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new MainCursorAdapter(getActivity(), null, 0);

        listView.setAdapter(mAdapter);
        listView.setEmptyView(emptyView);

        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        swipeContainer  = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);
        listView = view.findViewById(android.R.id.list);
        emptyView = view.findViewById(android.R.id.empty);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri ENTRY_URI = TodoEntry.CONTENT_URI;

        String select = "(" + TodoEntry.COLUMN_NAME_TITLE + " NOTNULL) AND ("
                + TodoEntry.COLUMN_NAME_ARCHIVED + " == 0 " + " )";
        CursorLoader cursorLoader = new CursorLoader(getActivity(), ENTRY_URI,
                TodoEntry.PROJECTION_ALL, select, null, TodoEntry.SORT_ORDER_DEFAULT);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(false);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    @Override
    public void onRefresh() {
        getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main_activity, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.label_setting:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ToduleLabelFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
