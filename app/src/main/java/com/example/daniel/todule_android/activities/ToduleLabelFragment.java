package com.example.daniel.todule_android.activities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.adapter.LabelAdapter;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoLabel;


/**
 * Created by danieL on 10/20/2017.
 */

public class ToduleLabelFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 3;
    LabelAdapter lAdapter;
    MainActivity myActivity;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        lAdapter = new LabelAdapter(getActivity(), null, 0);
        setListAdapter(lAdapter);

        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myActivity = (MainActivity) getActivity();
        myActivity.getSupportActionBar().setTitle("Labels");

        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri LABEL_URI = TodoLabel.CONTENT_URI;
        String select = "(" + TodoLabel.COLUMN_NAME_TAG + " NOTNULL)";
        CursorLoader cursorLoader = new CursorLoader(getActivity(), LABEL_URI,
                TodoLabel.PROJECTION_ALL, select, null, TodoLabel.SORT_ORDER_DEFAULT);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        lAdapter.swapCursor(data);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        lAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_label, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.label_new:
                myActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ToduleLabelAddFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
