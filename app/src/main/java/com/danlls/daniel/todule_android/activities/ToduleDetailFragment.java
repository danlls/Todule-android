package com.danlls.daniel.todule_android.activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.danlls.daniel.todule_android.R;
import com.danlls.daniel.todule_android.provider.ToduleDBContract;
import com.danlls.daniel.todule_android.utilities.DateTimeUtils;
import com.danlls.daniel.todule_android.utilities.NotificationHelper;


/**
 * Created by danieL on 11/2/2017.
 */

public class ToduleDetailFragment extends Fragment {
    MainActivity myActivity;
    private Long entryId;
    int taskDone;
    int taskArchived;
    int taskDeleted;
    String title;
    Context context;

    public static ToduleDetailFragment newInstance(long id) {
        ToduleDetailFragment f= new ToduleDetailFragment();

        Bundle args = new Bundle();
        args.putLong("entry_id", id);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity = (MainActivity) getActivity();
        entryId = getArguments().getLong("entry_id");
        setHasOptionsMenu(true);
        context = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        TextView titleView = view.findViewById(R.id.detail_title);
        TextView descriptionView = view.findViewById(R.id.detail_description);
        TextView labelView = view.findViewById(R.id.detail_label);
        TextView createdDateView = view.findViewById(R.id.detail_created_date);
        TextView dueDateView = view.findViewById(R.id.detail_due_date);
        TextView countdownView = view.findViewById(R.id.detail_countdown);
        Button doneButton = view.findViewById(R.id.detail_done);

        Uri entryUri = ContentUris.withAppendedId(ToduleDBContract.TodoEntry.CONTENT_ID_URI_BASE, entryId);
        Cursor cr = getContext().getContentResolver().query(entryUri, ToduleDBContract.TodoEntry.PROJECTION_ALL, null, null, null);
        cr.moveToFirst();
        title = cr.getString(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_TITLE));
        String description = cr.getString(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_DESCRIPTION));
        Long dueDate = cr.getLong(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_DUE_DATE));
        Long createdDate = cr.getLong(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_CREATED_DATE));
        Long completeDate = cr.getLong(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_COMPLETED_DATE));
        Long labelId;
        if(cr.isNull(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_LABEL))){
            labelId = null;
        } else{
            labelId = cr.getLong(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_LABEL));
        }
        taskDone = cr.getInt(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_TASK_DONE));
        taskArchived = cr.getInt(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_ARCHIVED));
        taskDeleted = cr.getInt(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_DELETED));
        cr.close();

        titleView.setText(title);
        if(description.isEmpty()){
            descriptionView.setText(R.string.no_descrption);
        } else {
            descriptionView.setText(description);
        }

        String createDateString = DateUtils.formatDateTime(getContext(), createdDate, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_TIME);
        String dueDateString = DateUtils.formatDateTime(getContext(), dueDate, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_TIME);
        String countdownString =  DateTimeUtils.dateTimeDiff(System.currentTimeMillis() ,dueDate);
        String completeDateString = DateTimeUtils.dateTimeDiff(System.currentTimeMillis(), completeDate);

        if(taskDone == ToduleDBContract.TodoEntry.TASK_NOT_COMPLETED){
            if(dueDate < System.currentTimeMillis()) {
                countdownView.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                countdownView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_clear_black_18dp, 0, 0, 0);
                countdownView.setText("Expired: " + countdownString);
            } else {
                countdownView.setTextColor(ContextCompat.getColor(getContext(), R.color.normalGreen));
                countdownView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_black_18dp, 0, 0, 0);
                countdownView.setText(countdownString);
            }

            // Show mark as done button
            doneButton.setVisibility(View.VISIBLE);
        } else {
            Drawable draw = getContext().getDrawable(R.drawable.ic_done_white_18dp).mutate();
            draw.setColorFilter(ContextCompat.getColor(getContext(), R.color.normalGreen), PorterDuff.Mode.SRC_IN);
            countdownView.setTextColor(ContextCompat.getColor(getContext(), R.color.normalGreen));
            countdownView.setCompoundDrawablesWithIntrinsicBounds(draw, null, null, null);
            countdownView.setText("Completed: " + completeDateString);

            // Hide mark as done button
            doneButton.setVisibility(View.GONE);
        }

        createdDateView.setText("Created: " + createDateString);
        dueDateView.setText(dueDateString);


        if(labelId != null){
            Uri labelUri = ContentUris.withAppendedId(ToduleDBContract.TodoLabel.CONTENT_ID_URI_BASE, labelId);
            Cursor labelCr = getContext().getContentResolver().query(labelUri, ToduleDBContract.TodoLabel.PROJECTION_ALL, null, null, null);
            labelCr.moveToFirst();
            String labelTitle = labelCr.getString(labelCr.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_TAG));
            int labelColor = labelCr.getInt(labelCr.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_COLOR));
            int labelTextColor = labelCr.getInt(labelCr.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_TEXT_COLOR));

            labelView.setText(labelTitle);
            labelView.setBackgroundColor(labelColor);
            labelView.setTextColor(labelTextColor);
            labelView.setVisibility(View.VISIBLE);

            labelCr.close();
        } else {
            labelView.setVisibility(View.GONE);
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Uri entryUri = ContentUris.withAppendedId(ToduleDBContract.TodoEntry.CONTENT_ID_URI_BASE, entryId);
                ContentValues cv = new ContentValues();
                cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_TASK_DONE, ToduleDBContract.TodoEntry.TASK_COMPLETED);
                getContext().getContentResolver().update(entryUri, cv, null, null);
                Snackbar mySnackbar = Snackbar.make(view, R.string.entry_done, Snackbar.LENGTH_LONG);
                mySnackbar.setAction(R.string.undo_string, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContentValues cv = new ContentValues();
                        cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_TASK_DONE, ToduleDBContract.TodoEntry.TASK_NOT_COMPLETED);
                        view.getContext().getContentResolver().update(entryUri, cv, null, null);
                    }
                });
                mySnackbar.show();
                myActivity.onBackPressed();
            }
        });

        myActivity.getSupportActionBar().setTitle(title);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(taskDeleted == ToduleDBContract.TodoEntry.TASK_DELETED){
            menu.findItem(R.id.action_delete_forever).setVisible(true);
            menu.findItem(R.id.action_restore).setVisible(true);
        } else {
            menu.findItem(R.id.action_soft_delete).setVisible(true);
            if (taskDone == ToduleDBContract.TodoEntry.TASK_NOT_COMPLETED){
                menu.findItem(R.id.action_edit).setVisible(true);
            } else {
                if(taskArchived == ToduleDBContract.TodoEntry.TASK_ARCHIVED){
                    menu.findItem(R.id.action_unarchive).setVisible(true);
                } else {
                    menu.findItem(R.id.action_archive).setVisible(true);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Uri entryUri = ContentUris.withAppendedId(ToduleDBContract.TodoEntry.CONTENT_ID_URI_BASE, entryId);
        final ContentResolver resolver = context.getContentResolver();
        ContentValues cv = new ContentValues();
        Snackbar mySnackbar = null;
        switch(item.getItemId()){
            case R.id.action_edit:
                ToduleAddFragment frag = new ToduleAddFragment();
                Bundle args = new Bundle();
                args.putString("mode", "edit_entry");
                args.putLong("entry_id", entryId);
                frag.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.fragment_container, frag, "add_frag")
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.action_soft_delete:
                cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_DELETED, ToduleDBContract.TodoEntry.TASK_DELETED);
                cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_DELETION_DATE, System.currentTimeMillis());
                resolver.update(entryUri, cv, null, null);
                NotificationHelper.cancelReminder(getContext(), entryUri);
                mySnackbar = Snackbar.make(getView(), getString(R.string.entry_deleted) + ": " + title, Snackbar.LENGTH_LONG);
                mySnackbar.setAction(R.string.undo_string, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContentValues cv = new ContentValues();
                        cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_DELETED, ToduleDBContract.TodoEntry.TASK_NOT_DELETED);
                        cv.putNull(ToduleDBContract.TodoEntry.COLUMN_NAME_DELETION_DATE);
                        resolver.update(entryUri, cv, null, null);
                        NotificationHelper.setReminder(context, entryUri, NotificationHelper.getReminderTime(context, entryUri));
                    }
                });
                mySnackbar.show();
                myActivity.onBackPressed();
                return true;
            case R.id.action_delete_forever:
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry permanently?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Uri entryUri = ContentUris.withAppendedId(ToduleDBContract.TodoEntry.CONTENT_ID_URI_BASE, entryId);
                                getContext().getContentResolver().delete(entryUri, null, null);
                                Toast.makeText(getContext(), getString(R.string.entry_deleted) + ": " + title, Toast.LENGTH_SHORT).show();
                                myActivity.onBackPressed();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                return true;
            case R.id.action_restore:
                cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_DELETED, ToduleDBContract.TodoEntry.TASK_NOT_DELETED);
                cv.putNull(ToduleDBContract.TodoEntry.COLUMN_NAME_DELETION_DATE);
                resolver.update(entryUri, cv, null, null);
                // Set reminder if todule is not completed or expired
                Cursor cr = getContext().getContentResolver().query(entryUri, null, null, null ,null);
                cr.moveToNext();
                int completed = cr.getInt(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_TASK_DONE));
                long due_date = cr.getLong(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_DUE_DATE));
                cr.close();
                if(!(completed == ToduleDBContract.TodoEntry.TASK_COMPLETED || due_date < System.currentTimeMillis())){
                    NotificationHelper.setReminder(context, entryUri, NotificationHelper.getReminderTime(context, entryUri));
                }
                mySnackbar = Snackbar.make(getView(), getString(R.string.entry_restored) + ": " + title, Snackbar.LENGTH_LONG);
                mySnackbar.setAction(R.string.undo_string, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContentValues cv = new ContentValues();
                        cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_DELETED, ToduleDBContract.TodoEntry.COLUMN_NAME_DELETED);
                        cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_DELETION_DATE, System.currentTimeMillis());
                        resolver.update(entryUri, cv, null, null);
                        NotificationHelper.cancelReminder(context, entryUri);
                    }
                });
                mySnackbar.show();
                myActivity.onBackPressed();
                return true;
            case R.id.action_archive:
                cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_ARCHIVED, ToduleDBContract.TodoEntry.TASK_ARCHIVED);
                resolver.update(entryUri, cv, null, null);
                mySnackbar = Snackbar.make(getView(), getString(R.string.entry_archived) + ": " + title, Snackbar.LENGTH_LONG);
                mySnackbar.setAction(R.string.undo_string, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContentValues cv = new ContentValues();
                        cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_ARCHIVED, ToduleDBContract.TodoEntry.TASK_NOT_ARCHIVED);
                        resolver.update(entryUri, cv, null, null);
                    }
                });
                mySnackbar.show();
                myActivity.onBackPressed();
                return true;
            case R.id.action_unarchive:
                cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_ARCHIVED, ToduleDBContract.TodoEntry.TASK_NOT_ARCHIVED);
                resolver.update(entryUri, cv, null, null);
                mySnackbar = Snackbar.make(getView(), getString(R.string.entry_unarchived) + ": " + title, Snackbar.LENGTH_LONG);
                mySnackbar.setAction(R.string.undo_string, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContentValues cv = new ContentValues();
                        cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_ARCHIVED, ToduleDBContract.TodoEntry.TASK_ARCHIVED);
                        resolver.update(entryUri, cv, null, null);
                    }
                });
                mySnackbar.show();
                myActivity.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
