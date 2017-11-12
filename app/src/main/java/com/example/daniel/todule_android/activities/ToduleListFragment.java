package com.example.daniel.todule_android.activities;

import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.transition.Fade;
import android.support.transition.TransitionInflater;
import android.support.transition.TransitionSet;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.adapter.HistoryAdapter;
import com.example.daniel.todule_android.adapter.MainCursorAdapter;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoEntry;

/**
 * Created by danieL on 8/1/2017.
 */

public class ToduleListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {
    private static final int INCOMPLETE_LOADER_ID = 1;
    private static final int COMPLETED_LOADER_ID = 2;
    private static final int ARCHIVE_LOADER_ID = 3;
    private CursorAdapter mAdapter;
    private MainActivity myActivity;
    private ListView listView;
    private ScrollView emptyView;
    private SwipeRefreshLayout swipeContainer;
    private int loaderId;

    public static ToduleListFragment newInstance(int id){
        ToduleListFragment f = new ToduleListFragment();

        Bundle args = new Bundle();
        args.putInt("loader_id", id);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loaderId = getArguments().getInt("loader_id");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myActivity = (MainActivity) getActivity();
        switch(loaderId){
            case INCOMPLETE_LOADER_ID:
                mAdapter = new MainCursorAdapter(getActivity(), null, 0);
                myActivity.fabVisibility(true);
                break;
            case COMPLETED_LOADER_ID:
                mAdapter = new HistoryAdapter(getActivity(), null, 0);
                myActivity.fabVisibility(false);
                break;
            case ARCHIVE_LOADER_ID:
                mAdapter = new HistoryAdapter(getActivity(), null, 0);
                myActivity.fabVisibility(false);
                break;
            default:
                break;
        }

        listView.setAdapter(mAdapter);
        listView.setEmptyView(emptyView);

        switch(loaderId){
            case INCOMPLETE_LOADER_ID:
                getActivity().getSupportLoaderManager().initLoader(INCOMPLETE_LOADER_ID, null, this);
                break;
            case COMPLETED_LOADER_ID:
                getActivity().getSupportLoaderManager().initLoader(COMPLETED_LOADER_ID, null, this);
                break;
            case ARCHIVE_LOADER_ID:
                getActivity().getSupportLoaderManager().initLoader(ARCHIVE_LOADER_ID, null, this);
            default:
                break;
        }

        myActivity.getSupportActionBar().setTitle(getTitle(loaderId));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);
        listView = view.findViewById(android.R.id.list);
        emptyView = view.findViewById(android.R.id.empty);
        setHasOptionsMenu(true);
        swipeContainer  = view.findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(this);
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ToduleDetailFragment frag = ToduleDetailFragment.newInstance(id);
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.fragment_container, frag, "detail_frag")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;
        Uri ENTRY_URI = TodoEntry.CONTENT_URI;
        String select = "";
        String[] selectionArgs;

        switch(loaderId){
            case INCOMPLETE_LOADER_ID:
                select = "(" + TodoEntry.COLUMN_NAME_TITLE + " NOTNULL) AND ("
                        + TodoEntry.COLUMN_NAME_TASK_DONE + " == ?)";
                selectionArgs = new String []{
                        String.valueOf(TodoEntry.TASK_NOT_COMPLETED)
                };
                cursorLoader = new CursorLoader(getActivity(), ENTRY_URI,
                        TodoEntry.PROJECTION_ALL, select, selectionArgs, TodoEntry.SORT_ORDER_DEFAULT);
                break;
            case COMPLETED_LOADER_ID:
                select = "(" + TodoEntry.COLUMN_NAME_TITLE + " NOTNULL) AND ("
                        + TodoEntry.COLUMN_NAME_TASK_DONE + " == ?) AND ("
                        + TodoEntry.COLUMN_NAME_ARCHIVED + " == ?)";
                selectionArgs = new String[] {
                        String.valueOf(TodoEntry.TASK_COMPLETED),
                        String.valueOf(TodoEntry.TASK_NOT_ARCHIVED)
                };
                cursorLoader = new CursorLoader(getActivity(), ENTRY_URI,
                        TodoEntry.PROJECTION_ALL, select, selectionArgs, TodoEntry.SORT_ORDER_DEFAULT);
                break;
            case ARCHIVE_LOADER_ID:
                select = "(" + TodoEntry.COLUMN_NAME_TITLE + " NOTNULL) AND ("
                        + TodoEntry.COLUMN_NAME_ARCHIVED + " == ?)";
                selectionArgs = new String []{
                        String.valueOf(TodoEntry.TASK_ARCHIVED)
                };
                cursorLoader = new CursorLoader(getActivity(), ENTRY_URI,
                        TodoEntry.PROJECTION_ALL, select, selectionArgs, TodoEntry.SORT_ORDER_DEFAULT);
                break;
            default:
                break;
        }
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
        getActivity().getSupportLoaderManager().restartLoader(loaderId, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        switch(loaderId){
            case ARCHIVE_LOADER_ID:
                inflater.inflate(R.menu.menu_fragment_archive, menu);
                break;
            default:
                break;
        }

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_clear_archive:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.clear_archive_dialog_msg) + "\n(This action is irreversible)");
                builder.setTitle(R.string.clear_archive_dialog_title);
                builder.setPositiveButton(R.string.clear_archive_dialog_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        String selectionClause = TodoEntry.COLUMN_NAME_ARCHIVED + " = ?";
                        String[] selectionArgs = {"1"};
                        int count = getContext().getContentResolver().delete(TodoEntry.CONTENT_URI, selectionClause, selectionArgs);
                        if (count > 0){
                            Toast.makeText(getContext(), String.valueOf(count) + " " + getString(R.string.entry_deleted), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton(R.string.clear_archive_dialog_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getTitle(int id){
        switch(id){
            case INCOMPLETE_LOADER_ID:
                return getContext().getString(R.string.todule_title);
            case COMPLETED_LOADER_ID:
                return getContext().getString(R.string.completed_title);
            case ARCHIVE_LOADER_ID:
                return getContext().getString(R.string.archive_title);
            default:
                return "";
        }
    }
}
