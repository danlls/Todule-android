package com.example.daniel.todule_android.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.provider.ToduleDBContract;

/**
 * Created by danieL on 10/20/2017.
 */

public class LabelAdapter extends CursorAdapter {
    private LayoutInflater cursorInflater;

    public LabelAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);
        cursorInflater = LayoutInflater.from(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String tag = cursor.getString(cursor.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_TAG));
        int color = cursor.getInt(cursor.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_COLOR));
        int text_color = cursor.getInt(cursor.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_TEXT_COLOR));

        holder.label.setText(tag);
        holder.label.setBackgroundColor(color);
        holder.label.setTextColor(text_color);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View rowView = cursorInflater.inflate(R.layout.fragment_label_item, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.label = rowView.findViewById(R.id.label_tag);
        rowView.setTag(holder);
        return rowView;
    }

    static class ViewHolder {
        TextView label;
    }
}
