package com.example.daniel.todule_android.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        TextView titleView = (TextView) view.findViewById(R.id.title_text);
        TextView descriptionView = (TextView) view.findViewById(R.id.description_text);
        TextView dueDateView = (TextView) view.findViewById(R.id.due_text);

        String title = cursor.getString(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_DESCRIPTION));
        long dueDate = cursor.getLong(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_DUE_DATE));
        String dueDateString = DateUtils.formatSameDayTime(dueDate, System.currentTimeMillis(), SHORT, SHORT).toString();

        titleView.setText(title);
        descriptionView.setText(description);
        dueDateView.setText(dueDateString);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.fragment_list_item, parent, false);
    }
}
