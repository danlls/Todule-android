package com.example.daniel.todule_android.activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.adapter.HistoryAdapter;
import com.example.daniel.todule_android.adapter.MainCursorAdapter;
import com.example.daniel.todule_android.parcelable.LongSparseArrayBooleanParcelable;
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
    private LongSparseArray<Boolean> selectedIds;

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
        if(savedInstanceState != null){
            selectedIds = savedInstanceState.getParcelable("myLongSparseBooleanArray");
        } else{
            selectedIds = new LongSparseArray<>();
        }
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

        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(myMultiChoiceModeListener);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ToduleDetailFragment frag = ToduleDetailFragment.newInstance(l);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.fragment_container, frag, "detail_frag")
                        .addToBackStack(null)
                        .commit();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(myActivity, "test", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("myLongSparseBooleanArray", new LongSparseArrayBooleanParcelable(selectedIds));
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

    private AbsListView.MultiChoiceModeListener myMultiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
            if(b){
                selectedIds.put(l, b);
            } else {
                selectedIds.remove(l);
            }
            actionMode.setTitle(selectedIds.size() + " selected");
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(getMenuRes(loaderId), menu);

            // Show checkboxes
            if(mAdapter instanceof HistoryAdapter){
                ((HistoryAdapter) mAdapter).setShowCheckbox(true);
            } else if (mAdapter instanceof MainCursorAdapter){
                ((MainCursorAdapter) mAdapter).setShowCheckbox(true);
            }

            actionMode.setTitle(selectedIds.size() + " selected");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            if(loaderId == INCOMPLETE_LOADER_ID){
                // remove edit action
                menu.removeItem(R.id.action_edit);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_delete:
                    deleteSelectedItems();
                    actionMode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.action_archive:
                    archiveSelectedItems();
                    actionMode.finish();
                    return true;
                case R.id.action_unarchive:
                    unarchiveSelectedItems();
                    actionMode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            selectedIds.clear();
            if(mAdapter instanceof HistoryAdapter){
                ((HistoryAdapter) mAdapter).setShowCheckbox(false);
            } else if (mAdapter instanceof MainCursorAdapter){
                ((MainCursorAdapter) mAdapter).setShowCheckbox(false);
            }
        }
    };

    private void deleteSelectedItems(){
        ContentResolver resolver = getContext().getContentResolver();
        int size = selectedIds.size();
        for (int i = 0; i < size; i++){
            long id = selectedIds.keyAt(i);
            Uri entryUri = ContentUris.withAppendedId(TodoEntry.CONTENT_ID_URI_BASE, id);
            resolver.delete(entryUri, null, null);
        }
        Toast.makeText(myActivity, size + getString(R.string.entry_deleted), Toast.LENGTH_SHORT).show();
    }

    private void archiveSelectedItems(){
        ContentResolver resolver = getContext().getContentResolver();
        int size = selectedIds.size();
        for (int i = 0; i < size; i++){
            long id = selectedIds.keyAt(i);
            Uri entryUri = ContentUris.withAppendedId(TodoEntry.CONTENT_ID_URI_BASE, id);
            ContentValues cv = new ContentValues();
            cv.put(TodoEntry.COLUMN_NAME_ARCHIVED, TodoEntry.TASK_ARCHIVED);
            resolver.update(entryUri, cv, null, null);
        }
        Toast.makeText(myActivity, size + getString(R.string.entry_archived), Toast.LENGTH_SHORT).show();
    }

    private void unarchiveSelectedItems(){
        ContentResolver resolver = getContext().getContentResolver();
        int size = selectedIds.size();
        for (int i = 0; i < size; i++){
            long id = selectedIds.keyAt(i);
            Uri entryUri = ContentUris.withAppendedId(TodoEntry.CONTENT_ID_URI_BASE, id);
            ContentValues cv = new ContentValues();
            cv.put(TodoEntry.COLUMN_NAME_ARCHIVED, TodoEntry.TASK_NOT_ARCHIVED);
            resolver.update(entryUri, cv, null, null);
        }
        Toast.makeText(myActivity, size + getString(R.string.entry_archived), Toast.LENGTH_SHORT).show();
    }

    private int getMenuRes(int id){
        switch(id){
            case INCOMPLETE_LOADER_ID:
                return R.menu.menu_entry_incomplete;
            case COMPLETED_LOADER_ID:
                return R.menu.menu_entry_completed;
            case ARCHIVE_LOADER_ID:
                return R.menu.menu_entry_archived;
        }
        return -1;
    }
}
