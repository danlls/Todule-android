package com.example.daniel.todule_android.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoEntry;

import java.text.Format;

import static java.text.DateFormat.SHORT;


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
        final Cursor cursor1 = cursor;
        TextView titleView = (TextView) view.findViewById(R.id.title_text);
        TextView descriptionView = (TextView) view.findViewById(R.id.description_text);
        TextView dueDateView = (TextView) view.findViewById(R.id.due_text);
        Button done_button = (Button) view.findViewById(R.id.done_button);

        String title = cursor.getString(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_DESCRIPTION));
        long dueDate = cursor.getLong(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_DUE_DATE));
        String dueDateString = DateUtils.formatSameDayTime(dueDate, System.currentTimeMillis(), SHORT, SHORT).toString();

        titleView.setText(title);
        descriptionView.setText(description);
        dueDateView.setText(dueDateString);
        done_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues cv = new ContentValues();
                cv.put(TodoEntry.COLUMN_NAME_TASK_DONE, 1);
                String id = cursor1.getString(cursor1.getColumnIndexOrThrow(TodoEntry._ID));
                Uri aUri = Uri.withAppendedPath(TodoEntry.CONTENT_ID_URI_BASE, id);
                view.getContext().getContentResolver().update(aUri , cv, null, null);
            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.fragment_list_item, parent, false);
    }
}
