package com.example.daniel.todule_android.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.provider.ToduleDBContract;

/**
 * Created by danieL on 8/9/2017.
 */

public class HistoryAdapter extends CursorAdapter{
    private LayoutInflater cursorInflater;

    public HistoryAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);
        cursorInflater = LayoutInflater.from(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        String title = cursor.getString(cursor.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_DESCRIPTION));
        int task_status = cursor.getInt(cursor.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_TASK_DONE));

        switch(task_status){
            case ToduleDBContract.TodoEntry.TASK_COMPLETED:
                long completed_date = cursor.getLong(cursor.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_COMPLETED_DATE));
                holder.completed.setText(DateUtils.formatDateTime(context, completed_date, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_TIME));
                // Add green tick at the start of text
                Drawable tickDrawable = context.getDrawable(R.drawable.ic_done_white_18dp);
                tickDrawable.setTint(ContextCompat.getColor(context, R.color.normalGreen));
                holder.completed.setCompoundDrawablesRelativeWithIntrinsicBounds(tickDrawable, null, null, null);
                break;
            case ToduleDBContract.TodoEntry.TASK_NOT_COMPLETED:
                holder.completed.setText(R.string.not_done);
                // Add red cross at the start of text
                Drawable crossDrawable = context.getDrawable(R.drawable.ic_clear_black_18dp);
                crossDrawable.setTint(ContextCompat.getColor(context, R.color.red));
                holder.completed.setCompoundDrawablesRelativeWithIntrinsicBounds(crossDrawable, null, null, null);
                break;
            default:
                break;
        }

        holder.title.setText(title);
        if(!description.isEmpty()){
            description = description.trim();
            holder.description.setText(description);
        } else {
            holder.description.setText(R.string.no_descrption);
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View rowView = cursorInflater.inflate(R.layout.fragment_history_item, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.title = rowView.findViewById(R.id.title_text);
        holder.description = rowView.findViewById(R.id.description_text);
        holder.completed = rowView.findViewById(R.id.completed_date_text);
        rowView.setTag(holder);
        return rowView;
    }

    static class ViewHolder {
        TextView title;
        TextView description;
        TextView completed;
    }
}
