package com.example.daniel.todule_android.adapter;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoEntry;
import com.example.daniel.todule_android.utilities.DateTimeUtils;


/**
 * Created by danieL on 8/3/2017.
 */

public class MainCursorAdapter extends CursorAdapter {
    private LayoutInflater cursorInflater;

    public MainCursorAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);
        cursorInflater = LayoutInflater.from(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(TodoEntry._ID));

        TextView titleView = (TextView) view.findViewById(R.id.title_text);
        final TextView descriptionView = (TextView) view.findViewById(R.id.description_text);
        TextView dueDateView = (TextView) view.findViewById(R.id.due_text);
        TextView countdownView = (TextView) view.findViewById(R.id.countdown_text);
        Button done_button = (Button) view.findViewById(R.id.done_button);

        String title = cursor.getString(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_DESCRIPTION));
        long dueDate = cursor.getLong(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_DUE_DATE));
        String dueDateString = DateUtils.formatDateTime(context, dueDate, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_TIME);
        String countdownString =  DateTimeUtils.dateTimeDiff(dueDate);

        titleView.setText(title);
        if(!description.isEmpty()){
            description = description.trim();
            descriptionView.setText(description);
        } else {
            descriptionView.setText(R.string.no_descrption);
        }
        dueDateView.setText(dueDateString);
        if(dueDate < System.currentTimeMillis()) {
            countdownView.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            countdownView.setTextColor(ContextCompat.getColor(context, R.color.normalGreen));
        }
        countdownView.setText(countdownString);

        done_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ContentValues cv = new ContentValues();
                cv.put(TodoEntry.COLUMN_NAME_TASK_DONE, 1);
                cv.put(TodoEntry.COLUMN_NAME_COMPLETED_DATE, System.currentTimeMillis());
                Uri aUri = ContentUris.withAppendedId(TodoEntry.CONTENT_ID_URI_BASE, id);
                view.getContext().getContentResolver().update(aUri, cv, null, null);
                Snackbar mySnackbar = Snackbar.make(view, R.string.entry_done, Snackbar.LENGTH_LONG);
                mySnackbar.setAction(R.string.undo_string, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cv.put(TodoEntry.COLUMN_NAME_TASK_DONE, 0);
                        Uri aUri = ContentUris.withAppendedId(TodoEntry.CONTENT_ID_URI_BASE, id);
                        view.getContext().getContentResolver().update(aUri, cv, null, null);
                    }
                });
                mySnackbar.show();
            }
        });

        view.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(descriptionView.getMaxLines() == 1) {
                    descriptionView.setMaxLines(Integer.MAX_VALUE);
                    descriptionView.setEllipsize(null);
                } else {
                    descriptionView.setMaxLines(1);
                    descriptionView.setEllipsize(TextUtils.TruncateAt.END);
                }
            }
        });

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.fragment_list_item, parent, false);
    }

}
