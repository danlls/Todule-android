package com.example.daniel.todule_android.activities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.adapter.LabelAdapter;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoLabel;


/**
 * Created by danieL on 10/20/2017.
 */

public class ToduleLabelFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 99;
    LabelAdapter lAdapter;
    MainActivity myActivity;
    OnLabelSelectedListener mCallback;
    Long selectedLabelId = null;
    boolean selecting;

    public static ToduleLabelFragment newInstance(boolean select, Long selected_label_id) {

        Bundle args = new Bundle();
        args.putBoolean("select", select);
        if (selected_label_id != null){
            args.putLong("selected_label_id", selected_label_id);
        }
        ToduleLabelFragment fragment = new ToduleLabelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            selecting = bundle.getBoolean("select", false);
            selectedLabelId = bundle.getLong("selected_label_id", -1L);
        } else {
            selecting = false;
        }
        if(savedInstanceState != null) {
            selectedLabelId = savedInstanceState.getLong("selected_label_id", -1L);
        }

        setHasOptionsMenu(true);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lAdapter = new LabelAdapter(getActivity(), null, 0);
        setListAdapter(lAdapter);
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        myActivity = (MainActivity) getActivity();
        myActivity.getSupportActionBar().setTitle("Labels");
        myActivity.hideSoftKeyboard(true);

        if(selecting){
            setActivateOnItemClick(true);
            myActivity.getSupportActionBar().setTitle("Select label");
            // Add "no label" to list
            View noLabel =  View.inflate(getContext(), R.layout.fragment_label_item, null);
            TextView labelTag = noLabel.findViewById(R.id.label_tag);
            labelTag.setText(R.string.none);
            getListView().addHeaderView(noLabel);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(selecting) {
            outState.putLong("selected_label_id", selectedLabelId);
        }
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(selecting) {
            menu.findItem(R.id.label_new).setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        } else {
            menu.findItem(R.id.label_confirm).setVisible(false);
        }
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
        getListView().postDelayed(new Runnable() {
            @Override
            public void run() {
                ListView lv = getListView();
                if(selecting) {
                    if (selectedLabelId == -1L){
                        // Set headerview as checked
                        lv.setItemChecked(0, true);
                    } else {
                        lv.setItemChecked(getAdapterItemPosition(selectedLabelId) + lv.getHeaderViewsCount(), true);
                    }
                }
            }
        }, 100);

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
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.fragment_container, new ToduleLabelAddFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.label_confirm:
                mCallback.onLabelSelected(selectedLabelId);
                myActivity.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0){
                    // If user choose the headerview "no label"
                    selectedLabelId = -1L;
                } else {
                    Cursor cr = (Cursor) adapterView.getItemAtPosition(i);
                    selectedLabelId = cr.getLong(cr.getColumnIndexOrThrow(TodoLabel._ID));
                }
            }
        });
        lAdapter.setShowCheckbox(true);
    }

    public interface OnLabelSelectedListener {
        public void onLabelSelected(long id);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        if (context instanceof OnLabelSelectedListener){
            try {
                mCallback = (OnLabelSelectedListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString()
                        + " must implement OnHeadlineSelectedListener");
            }
        }

    }

    private int getAdapterItemPosition(long id){
        for (int position=0; position < getListView().getCount(); position++){
            if(lAdapter.getItemId(position) == id){
                return position;
            }
        }
        return -1;
    }


}
