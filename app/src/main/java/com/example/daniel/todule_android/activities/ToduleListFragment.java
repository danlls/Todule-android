package com.example.daniel.todule_android.activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.adapter.HistoryAdapter;
import com.example.daniel.todule_android.adapter.MainCursorAdapter;
import com.example.daniel.todule_android.parcelable.LongSparseArrayBooleanParcelable;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoEntry;
import com.example.daniel.todule_android.utilities.NotificationHelper;

import java.util.ArrayList;

/**
 * Created by danieL on 8/1/2017.
 */

public class ToduleListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener{
    private static final int INCOMPLETE_LOADER_ID = 1;
    private static final int EXPIRED_LOADER_ID = 2;
    private static final int COMPLETED_LOADER_ID = 3;
    private static final int ARCHIVE_LOADER_ID = 4;
    private static final int DELETED_LOADER_ID = 5;
    private CursorAdapter mAdapter;
    private MainActivity myActivity;
    private ListView listView;
    private SwipeRefreshLayout swipeContainer;
    private SwipeRefreshLayout swipeContainerEmpty;
    private int loaderId;
    private LongSparseArray<Boolean> selectedIds;

    ActionMode mActionMode;
    View expiredHeader;
    SearchView searchView;
    String mCurFilter;

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
                myActivity.fabVisibility(true);
            case EXPIRED_LOADER_ID:
                mAdapter = new MainCursorAdapter(getActivity(), null, 0);
                break;
            case COMPLETED_LOADER_ID:
            case ARCHIVE_LOADER_ID:
            case DELETED_LOADER_ID:
                mAdapter = new HistoryAdapter(getActivity(), null, 0);
                myActivity.fabVisibility(false);
                break;
            default:
                break;
        }

        listView.setAdapter(mAdapter);
        listView.setEmptyView(swipeContainerEmpty);

        myActivity.getSupportLoaderManager().initLoader(loaderId, null, this);
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



        if(loaderId == INCOMPLETE_LOADER_ID){
            // Check if there is any expired todule
            String select = "(" + TodoEntry.COLUMN_NAME_TITLE + " NOTNULL) AND ("
                    + TodoEntry.COLUMN_NAME_DUE_DATE + " < ?) AND ("
                    + TodoEntry.COLUMN_NAME_TASK_DONE + " == ?) AND ("
                    + TodoEntry.COLUMN_NAME_DELETED + " == ?)";
            String[] selectionArgs = {
                    String.valueOf(System.currentTimeMillis()),
                    String.valueOf(TodoEntry.TASK_NOT_COMPLETED),
                    String.valueOf(TodoEntry.TASK_NOT_DELETED)
            };
            Cursor cr = getContext().getContentResolver().query(TodoEntry.CONTENT_URI, null, select, selectionArgs, null);
            if(cr.getCount() > 0){
                expiredHeader = View.inflate(getContext(), R.layout.expired_header, null);
                TextView expiredCount = expiredHeader.findViewById(R.id.expired_count);
                expiredCount.setText(String.valueOf(cr.getCount()));
                expiredHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mActionMode !=  null){
                            mActionMode.finish();
                        }
                        ToduleListFragment f = newInstance(EXPIRED_LOADER_ID);
                        myActivity.getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                .replace(R.id.fragment_container, f)
                                .addToBackStack(null)
                                .commit();
                    }
                });
                listView.addHeaderView(expiredHeader, null, true);
            }
            cr.close();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);
        listView = view.findViewById(android.R.id.list);

        setHasOptionsMenu(true);

        swipeContainer  = view.findViewById(R.id.swipe_container);
        swipeContainerEmpty = view.findViewById(R.id.swipe_container_empty);

        swipeContainer.setOnRefreshListener(this);
        swipeContainerEmpty.setOnRefreshListener(this);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("myLongSparseBooleanArray", new LongSparseArrayBooleanParcelable(selectedIds));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader;
        Uri ENTRY_URI = TodoEntry.CONTENT_URI;
        String select = "";
        ArrayList<String> selectionArgs = new ArrayList<String>();
        String sortOrder = "";

        switch(loaderId){
            case INCOMPLETE_LOADER_ID:
                select = "(" + TodoEntry.COLUMN_NAME_TITLE + " NOTNULL) AND ("
                        + TodoEntry.COLUMN_NAME_TASK_DONE + " == ?) AND ("
                        + TodoEntry.COLUMN_NAME_DELETED + " == ?) AND ("
                        + TodoEntry.COLUMN_NAME_DUE_DATE + " > ?)";
                selectionArgs.add(String.valueOf(TodoEntry.TASK_NOT_COMPLETED));
                selectionArgs.add(String.valueOf(TodoEntry.TASK_NOT_DELETED));
                selectionArgs.add(String.valueOf(System.currentTimeMillis()));
                break;
            case EXPIRED_LOADER_ID:
                select = "(" + TodoEntry.COLUMN_NAME_TITLE + " NOTNULL) AND ("
                        + TodoEntry.COLUMN_NAME_DUE_DATE + " < ?) AND ("
                        + TodoEntry.COLUMN_NAME_TASK_DONE + " == ?) AND ("
                        + TodoEntry.COLUMN_NAME_DELETED + " == ?)";
                selectionArgs.add(String.valueOf(System.currentTimeMillis()));
                selectionArgs.add(String.valueOf(TodoEntry.TASK_NOT_COMPLETED));
                selectionArgs.add(String.valueOf(TodoEntry.TASK_NOT_DELETED));
                break;
            case COMPLETED_LOADER_ID:
                select = "(" + TodoEntry.COLUMN_NAME_TITLE + " NOTNULL) AND ("
                        + TodoEntry.COLUMN_NAME_TASK_DONE + " == ?) AND ("
                        + TodoEntry.COLUMN_NAME_ARCHIVED + " == ?) AND ("
                        + TodoEntry.COLUMN_NAME_DELETED + " == ?)";
                selectionArgs.add(String.valueOf(TodoEntry.TASK_COMPLETED));
                selectionArgs.add(String.valueOf(TodoEntry.TASK_NOT_ARCHIVED));
                selectionArgs.add(String.valueOf(TodoEntry.TASK_NOT_DELETED));
                sortOrder = TodoEntry.COLUMN_NAME_COMPLETED_DATE + " DESC";
                break;
            case ARCHIVE_LOADER_ID:
                select = "(" + TodoEntry.COLUMN_NAME_TITLE + " NOTNULL) AND ("
                        + TodoEntry.COLUMN_NAME_ARCHIVED + " == ?) AND ("
                        + TodoEntry.COLUMN_NAME_DELETED + " == ?)";
                selectionArgs.add(String.valueOf(TodoEntry.TASK_ARCHIVED));
                selectionArgs.add(String.valueOf(TodoEntry.TASK_NOT_DELETED));
                sortOrder = TodoEntry.COLUMN_NAME_COMPLETED_DATE + " DESC";
                break;
            case DELETED_LOADER_ID:
                select = "(" + TodoEntry.COLUMN_NAME_TITLE + " NOTNULL) AND ("
                    + TodoEntry.COLUMN_NAME_DELETED + " == ?)";
                selectionArgs.add(String.valueOf(TodoEntry.TASK_DELETED));
                sortOrder = TodoEntry.COLUMN_NAME_DELETION_DATE + " DESC";
                break;
            default:
                break;
        }
        if (mCurFilter != null){
            select += " AND (" + TodoEntry.COLUMN_NAME_TITLE + " LIKE ?)";
            selectionArgs.add("%" + mCurFilter + "%");
        }
        String [] selectionArgsArray = new String[selectionArgs.size()];
        selectionArgs.toArray(selectionArgsArray);
        cursorLoader = new CursorLoader(getActivity(), ENTRY_URI,
                TodoEntry.PROJECTION_ALL, select, selectionArgsArray, sortOrder);
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
        swipeContainerEmpty.post(new Runnable() {
            @Override
            public void run() {
                swipeContainerEmpty.setRefreshing(false);
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
            case EXPIRED_LOADER_ID:
                return getContext().getString(R.string.expired_title);
            case COMPLETED_LOADER_ID:
                return getContext().getString(R.string.completed_title);
            case ARCHIVE_LOADER_ID:
                return getContext().getString(R.string.archive_title);
            case DELETED_LOADER_ID:
                return getContext().getString(R.string.deleted_title);
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
            mActionMode = actionMode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            if(loaderId == INCOMPLETE_LOADER_ID || loaderId == EXPIRED_LOADER_ID){
                // remove edit action
                menu.removeItem(R.id.action_edit);
            }
            if (loaderId == DELETED_LOADER_ID){
                menu.removeItem(R.id.action_empty_bin);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_soft_delete:
                    softDeleteSelectedItems();
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
                case R.id.action_restore:
                    restoreSelectedItems();
                    actionMode.finish();
                    return true;
                case R.id.action_delete_forever:
                    deleteSelectedItems();
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
            actionMode = null;
        }
    };

    private String constructPlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    private void softDeleteSelectedItems(){
        final ContentResolver resolver = getContext().getContentResolver();
        int size = selectedIds.size();
        final Long[] mArray = new Long[size];
        for (int i = 0; i < size; i++) {
            long id = selectedIds.keyAt(i);
            mArray[i] = id;
        }
        ContentValues cv = new ContentValues();
        cv.put(TodoEntry.COLUMN_NAME_DELETED, TodoEntry.TASK_DELETED);
        cv.put(TodoEntry.COLUMN_NAME_DELETION_DATE, System.currentTimeMillis());
        final String select = TodoEntry._ID + " IN(" + constructPlaceholders(mArray.length)+ ")";
        final String[] selectionArgs = new String[mArray.length];
        for (int i =0; i< mArray.length; i++){
            selectionArgs[i] = String.valueOf(mArray[i]);
            Uri entryUri = ContentUris.withAppendedId(TodoEntry.CONTENT_ID_URI_BASE, mArray[i]);
            NotificationHelper.cancelReminder(getContext(), entryUri);
        }
        int count = resolver.update(TodoEntry.CONTENT_URI, cv, select, selectionArgs);
        final Snackbar mySnackbar = Snackbar.make(getView(), String.valueOf(count) + " " + getString(R.string.entry_deleted), Snackbar.LENGTH_LONG);
        mySnackbar.setAction(R.string.undo_string, new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ContentValues cv = new ContentValues();
                cv.put(TodoEntry.COLUMN_NAME_DELETED, TodoEntry.TASK_NOT_DELETED);
                cv.putNull(TodoEntry.COLUMN_NAME_DELETION_DATE);
                resolver.update(TodoEntry.CONTENT_URI, cv, select, selectionArgs);
                for (int i =0; i< mArray.length; i++){
                    Uri entryUri = ContentUris.withAppendedId(TodoEntry.CONTENT_ID_URI_BASE, mArray[i]);
                    NotificationHelper.setReminder(getContext(), entryUri, NotificationHelper.getReminderTime(getContext(), entryUri));
                }
            }
        });
        mySnackbar.show();
    }

    private void restoreSelectedItems(){
        final ContentResolver resolver = getContext().getContentResolver();
        int size = selectedIds.size();
        final Long[] mArray = new Long[size];
        for (int i = 0; i < size; i++) {
            long id = selectedIds.keyAt(i);
            mArray[i] = id;
        }
        ContentValues cv = new ContentValues();
        cv.put(TodoEntry.COLUMN_NAME_DELETED, TodoEntry.TASK_NOT_DELETED);
        cv.putNull(TodoEntry.COLUMN_NAME_DELETION_DATE);
        final String select = TodoEntry._ID + " IN(" + constructPlaceholders(mArray.length)+ ")";
        final String[] selectionArgs = new String[mArray.length];
        for (int i =0; i< mArray.length; i++){
            selectionArgs[i] = String.valueOf(mArray[i]);
            Uri entryUri = ContentUris.withAppendedId(TodoEntry.CONTENT_ID_URI_BASE, mArray[i]);
            // Set reminder if todule is not completed or expired
            Cursor cr = getContext().getContentResolver().query(entryUri, null, null, null ,null);
            cr.moveToNext();
            int completed = cr.getInt(cr.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_TASK_DONE));
            long due_date = cr.getLong(cr.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_DUE_DATE));
            cr.close();
            if(!(completed == TodoEntry.TASK_COMPLETED || due_date < System.currentTimeMillis())){
                NotificationHelper.setReminder(getContext(), entryUri, NotificationHelper.getReminderTime(getContext(), entryUri));
            }
        }
        int count = resolver.update(TodoEntry.CONTENT_URI, cv, select ,selectionArgs);
        Snackbar mySnackbar = Snackbar.make(getView(), String.valueOf(count)+ " " + getString(R.string.entry_restored), Snackbar.LENGTH_LONG);
        mySnackbar.setAction(R.string.undo_string, new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ContentValues cv = new ContentValues();
                cv.put(TodoEntry.COLUMN_NAME_DELETED, TodoEntry.TASK_DELETED);
                cv.put(TodoEntry.COLUMN_NAME_DELETION_DATE, System.currentTimeMillis());
                resolver.update(TodoEntry.CONTENT_URI, cv, select, selectionArgs);
                for (int i =0; i< mArray.length; i++){
                    Uri entryUri = ContentUris.withAppendedId(TodoEntry.CONTENT_ID_URI_BASE, mArray[i]);
                    NotificationHelper.cancelReminder(getContext(), entryUri);
                }
            }
        });
        mySnackbar.show();
    }

    private void deleteSelectedItems(){
        ContentResolver resolver = getContext().getContentResolver();
        int size = selectedIds.size();
        Long[] mArray = new Long[size];
        for (int i = 0; i < size; i++) {
            long id = selectedIds.keyAt(i);
            mArray[i] = id;
        }
        final String select = TodoEntry._ID + " IN(" + constructPlaceholders(mArray.length)+ ")";
        final String[] selectionArgs = new String[mArray.length];
        for (int i =0; i< mArray.length; i++){
            selectionArgs[i] = String.valueOf(mArray[i]);
        }
        int count = resolver.delete(TodoEntry.CONTENT_URI , select, selectionArgs);
        Toast.makeText(myActivity, count + " " + getString(R.string.entry_deleted), Toast.LENGTH_SHORT).show();
    }

    private void archiveSelectedItems(){
        final ContentResolver resolver = getContext().getContentResolver();
        int size = selectedIds.size();
        Long[] mArray = new Long[size];
        for (int i = 0; i < size; i++) {
            long id = selectedIds.keyAt(i);
            mArray[i] = id;
        }
        ContentValues cv = new ContentValues();
        cv.put(TodoEntry.COLUMN_NAME_ARCHIVED, TodoEntry.TASK_ARCHIVED);
        final String select = TodoEntry._ID + " IN(" + constructPlaceholders(mArray.length)+ ")";
        final String[] selectionArgs = new String[mArray.length];
        for (int i =0; i< mArray.length; i++){
            selectionArgs[i] = String.valueOf(mArray[i]);
        }
        int count = resolver.update(TodoEntry.CONTENT_URI, cv, select, selectionArgs);
        Snackbar mySnackbar = Snackbar.make(getView(), String.valueOf(count)+ " " + getString(R.string.entry_archived), Snackbar.LENGTH_LONG);
        mySnackbar.setAction(R.string.undo_string, new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ContentValues cv = new ContentValues();
                cv.put(TodoEntry.COLUMN_NAME_ARCHIVED, TodoEntry.TASK_NOT_ARCHIVED);
                resolver.update(TodoEntry.CONTENT_URI, cv, select, selectionArgs);
            }
        });
        mySnackbar.show();
    }

    private void unarchiveSelectedItems(){
        final ContentResolver resolver = getContext().getContentResolver();
        int size = selectedIds.size();
        Long[] mArray = new Long[size];
        for (int i = 0; i < size; i++) {
            long id = selectedIds.keyAt(i);
            mArray[i] = id;
        }
        ContentValues cv = new ContentValues();
        cv.put(TodoEntry.COLUMN_NAME_ARCHIVED, TodoEntry.TASK_NOT_ARCHIVED);
        final String select = TodoEntry._ID + " IN(" + constructPlaceholders(mArray.length)+ ")";
        final String[] selectionArgs = new String[mArray.length];
        for (int i =0; i< mArray.length; i++){
            selectionArgs[i] = String.valueOf(mArray[i]);
        }
        int count = resolver.update(TodoEntry.CONTENT_URI, cv, select, selectionArgs);
        Snackbar mySnackbar = Snackbar.make(getView(), String.valueOf(count) + " " + getString(R.string.entry_unarchived), Snackbar.LENGTH_LONG);
        mySnackbar.setAction(R.string.undo_string, new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ContentValues cv = new ContentValues();
                cv.put(TodoEntry.COLUMN_NAME_ARCHIVED, TodoEntry.TASK_ARCHIVED);
                resolver.update(TodoEntry.CONTENT_URI, cv, select, selectionArgs);
            }
        });
        mySnackbar.show();
    }

    private void emptyBin(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Empty bin")
            .setMessage("All entries removed from the bin cannot be recovered!")
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String select = TodoEntry.COLUMN_NAME_DELETED + " == ?" + TodoEntry.TASK_DELETED;
                    String[] selectionArgs = new String[]{
                            String.valueOf(TodoEntry.TASK_DELETED)
                    };
                    getContext().getContentResolver().delete(TodoEntry.CONTENT_URI, select, selectionArgs);
                    Toast.makeText(myActivity, getString(R.string.bin_emptied) , Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            })
            .show();
    }

    public static int getMenuRes(int id){
        switch(id){
            case INCOMPLETE_LOADER_ID:
                return R.menu.menu_entry_incomplete;
            case EXPIRED_LOADER_ID:
                return R.menu.menu_entry_expired;
            case COMPLETED_LOADER_ID:
                return R.menu.menu_entry_completed;
            case ARCHIVE_LOADER_ID:
                return R.menu.menu_entry_archived;
            case DELETED_LOADER_ID:
                return R.menu.menu_entry_deleted;
        }
        return -1;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(loaderId == DELETED_LOADER_ID){
            inflater.inflate(R.menu.menu_fragment_bin, menu);
        }

        // Place an action bar item for searching.
        MenuItem item = menu.add("Search");
        item.setIcon(R.drawable.ic_search_white_24dp);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        searchView = new SearchView(myActivity);
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // Set searchbox text to white
        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        searchEditText.setHintTextColor(ContextCompat.getColor(getContext(), R.color.white));

        searchView.setOnQueryTextListener(this);
        item.setActionView(searchView);
        super.onCreateOptionsMenu(menu, inflater);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_empty_bin:
                emptyBin();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(loaderId, null, this);
        return true;
    }

    @Override public boolean onQueryTextSubmit(String query) {
        myActivity.hideSoftKeyboard(true);
        return true;
    }
}
