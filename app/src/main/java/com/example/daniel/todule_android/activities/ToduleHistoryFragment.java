package com.example.daniel.todule_android.activities;

import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.daniel.todule_android.R;
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
        setHasOptionsMenu(true);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_history, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_clear_history:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.clear_history_dialog_msg) + "\n(This action is irreversible)");
                builder.setTitle(R.string.clear_history_dialog_title);
                builder.setPositiveButton(R.string.clear_history_dialog_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        String selectionClause = TodoEntry.COLUMN_NAME_ARCHIVED + " = ?";
                        String[] selectionArgs = {"1"};
                        getContext().getContentResolver().delete(TodoEntry.CONTENT_URI, selectionClause, selectionArgs);
                    }
                });
                builder.setNegativeButton(R.string.clear_history_dialog_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
