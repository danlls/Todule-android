package com.example.daniel.todule_android.activities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.daniel.todule_android.adapter.HistoryAdapter;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoEntry;

/**
 * Created by danieL on 8/9/2017.
 */

public class ToduleHistoryFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 2;
    HistoryAdapter mAdapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText("No entry found");
        mAdapter = new HistoryAdapter(getActivity(), null, 0);
        setListAdapter(mAdapter);
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri ENTRY_URI = TodoEntry.CONTENT_URI;

        String select = "(" + TodoEntry.COLUMN_NAME_TITLE + " NOTNULL) AND ("
                + TodoEntry.COLUMN_NAME_ARCHIVED + " == 1 " + " )";
        CursorLoader cursorLoader = new CursorLoader(getActivity(), ENTRY_URI,
                TodoEntry.PROJECTION_ALL, select, null, TodoEntry.SORT_ORDER_DEFAULT);
        return cursorLoader;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }
}
