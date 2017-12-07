package com.danlls.daniel.todule_android.adapter;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.danlls.daniel.todule_android.R;
import com.danlls.daniel.todule_android.provider.ToduleDBContract;
import com.danlls.daniel.todule_android.provider.ToduleDBContract.TodoEntry;
import com.danlls.daniel.todule_android.utilities.DateTimeUtils;


/**
 * Created by danieL on 8/3/2017.
 */

public class MainCursorAdapter extends CursorAdapter {
    private LayoutInflater cursorInflater;
    private boolean showCheckbox = false;

    public MainCursorAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);
        cursorInflater = LayoutInflater.from(context);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(TodoEntry._ID));
        final ViewHolder holder = (ViewHolder) view.getTag();

        final String title = cursor.getString(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_DESCRIPTION));
        long dueDate = cursor.getLong(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_DUE_DATE));
        String dueDateString = DateUtils.formatDateTime(context, dueDate, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_TIME);
        String countdownString =  DateTimeUtils.dateTimeDiff(dueDate);
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_LABEL))){
            Long labelId = cursor.getLong(cursor.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_LABEL));
            Uri labelUri = ContentUris.withAppendedId(ToduleDBContract.TodoLabel.CONTENT_ID_URI_BASE, labelId);
            Cursor cr = context.getContentResolver().query(labelUri, ToduleDBContract.TodoLabel.PROJECTION_ALL, null, null, ToduleDBContract.TodoLabel.SORT_ORDER_DEFAULT);

            cr.moveToFirst();
            String labelText = cr.getString(cr.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_TAG));
            int textColor = cr.getInt(cr.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_TEXT_COLOR));
            int color = cr.getInt(cr.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_COLOR));
            holder.label.setText(labelText);
            holder.label.setTextColor(textColor);
            holder.label.setBackgroundColor(color);
            holder.label.setVisibility(View.VISIBLE);
            cr.close();
        } else {
            holder.label.setVisibility(View.GONE);
        }


        holder.title.setText(title);
        if(!description.isEmpty()){
            description = description.trim();
            holder.description.setText(description);
        } else {
            holder.description.setText(R.string.no_descrption);
        }
        holder.dueDate.setText(dueDateString);
        if(dueDate < System.currentTimeMillis()) {
            holder.countdown.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.countdown.setTextColor(ContextCompat.getColor(context, R.color.normalGreen));
        }
        holder.countdown.setText(countdownString);

        holder.doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ContentValues cv = new ContentValues();
                cv.put(TodoEntry.COLUMN_NAME_TASK_DONE, TodoEntry.TASK_COMPLETED);
                cv.put(TodoEntry.COLUMN_NAME_COMPLETED_DATE, System.currentTimeMillis());
                Uri aUri = ContentUris.withAppendedId(TodoEntry.CONTENT_ID_URI_BASE, id);
                view.getContext().getContentResolver().update(aUri, cv, null, null);
                Snackbar mySnackbar = Snackbar.make(view, context.getString(R.string.entry_done) + ": " + title, Snackbar.LENGTH_LONG);
                mySnackbar.setAction(R.string.undo_string, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cv.put(TodoEntry.COLUMN_NAME_TASK_DONE, TodoEntry.TASK_NOT_COMPLETED);
                        cv.putNull(TodoEntry.COLUMN_NAME_COMPLETED_DATE);
                        Uri aUri = ContentUris.withAppendedId(TodoEntry.CONTENT_ID_URI_BASE, id);
                        view.getContext().getContentResolver().update(aUri, cv, null, null);
                    }
                });
                mySnackbar.show();
            }
        });


        if(showCheckbox){
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View rowView = cursorInflater.inflate(R.layout.fragment_list_item, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.title = rowView.findViewById(R.id.title_text);
        holder.description = rowView.findViewById(R.id.description_text);
        holder.dueDate = rowView.findViewById(R.id.due_text);
        holder.countdown = rowView.findViewById(R.id.countdown_text);
        holder.label = rowView.findViewById(R.id.entry_label);
        holder.doneButton =  rowView.findViewById(R.id.done_button);
        holder.checkBox = rowView.findViewById(R.id.checkbox);
        rowView.setTag(holder);
        return rowView;
    }

    public void setShowCheckbox(boolean b){
        showCheckbox = b;
    }

    static class ViewHolder {
        TextView title;
        TextView description;
        TextView dueDate;
        TextView countdown;
        TextView label;
        Button doneButton;
        CheckBox checkBox;
    }

}
