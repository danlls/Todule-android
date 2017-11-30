package com.example.daniel.todule_android.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoEntry;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoLabel;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoNotification;

import org.w3c.dom.Text;

/**
 * Created by danieL on 7/31/2017.
 */

public class ToduleProvider extends ContentProvider{

    // UriMatcher constants
    private static final int ENTRY_LIST = 1;
    private static final int ENTRY_ID = 2;
    private static final int LABEL_LIST = 3;
    private static final int LABEL_ID = 4;
    private static final int NOTIFICATION_LIST = 5;
    private static final int NOTIFICATION_ID = 6;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI("com.example.todule.provider", "todo_entry", ENTRY_LIST);
        sUriMatcher.addURI("com.example.todule.provider", "todo_entry/#", ENTRY_ID);
        sUriMatcher.addURI("com.example.todule.provider", "todo_label", LABEL_LIST);
        sUriMatcher.addURI("com.example.todule.provider", "todo_label/#", LABEL_ID);
        sUriMatcher.addURI("com.example.todule.provider", "todo_notification", NOTIFICATION_LIST);
        sUriMatcher.addURI("com.example.todule.provider", "todo_notification/#", NOTIFICATION_ID);
    }

    private ToduleDBHelper tOpenHelper;
    private static final String DBNAME = "todule.db";
    private SQLiteDatabase db;

    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        db = tOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            // All rows
            case ENTRY_LIST:
                builder.setTables(TodoEntry.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = TodoEntry.SORT_ORDER_DEFAULT;
                }
                break;
            // Single row
            case ENTRY_ID:
                builder.setTables(TodoEntry.TABLE_NAME);
                builder.appendWhere("_ID = " + uri.getLastPathSegment());
                break;
            case LABEL_LIST:
                builder.setTables(TodoLabel.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = TodoEntry.SORT_ORDER_DEFAULT;
                }
                break;
            case LABEL_ID:
                builder.setTables(TodoLabel.TABLE_NAME);
                builder.appendWhere("_ID = " + uri.getLastPathSegment());
                break;
            case NOTIFICATION_LIST:
                builder.setTables(TodoNotification.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = TodoNotification.SORT_ORDER_DEFAULT;
                }
                break;
            case NOTIFICATION_ID:
                builder.setTables(TodoNotification.TABLE_NAME);
                builder.appendWhere("_ID = " + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Cursor cursor = builder.query(db, projection, selection, selectionArgs,
                null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    public boolean onCreate() {
        tOpenHelper = new ToduleDBHelper(
                getContext()
        );
        return true;
    }

    public String getType(Uri uri){
        switch (sUriMatcher.match(uri)){
            case ENTRY_LIST:
                return ToduleDBContract.TodoEntry.CONTENT_TYPE;
            case ENTRY_ID:
                return ToduleDBContract.TodoEntry.CONTENT_ITEM_TYPE;
            case LABEL_LIST:
                return TodoLabel.CONTENT_TYPE;
            case LABEL_ID:
                return TodoLabel.CONTENT_ITEM_TYPE;
            case NOTIFICATION_LIST:
                return TodoNotification.CONTENT_TYPE;
            case NOTIFICATION_ID:
                return TodoNotification.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues values){
        db = tOpenHelper.getWritableDatabase();
        long id;
        switch(sUriMatcher.match(uri)){
            case ENTRY_LIST:
                id = db.insert(TodoEntry.TABLE_NAME, null, values);
                break;
            case LABEL_LIST:
                id = db.insert(TodoLabel.TABLE_NAME, null, values);
                break;
            case NOTIFICATION_LIST:
                id = db.insert(TodoNotification.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }
        Uri itemUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(itemUri, null);
        return itemUri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs){
        db = tOpenHelper.getWritableDatabase();
        int count = 0;
        String idStr, where;
        switch(sUriMatcher.match(uri)) {
            case ENTRY_LIST:
                count = db.delete(TodoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ENTRY_ID:
                idStr = uri.getLastPathSegment();
                where = TodoEntry._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                count = db.delete(TodoEntry.TABLE_NAME, where, selectionArgs);
                break;
            case LABEL_LIST:
                count = db.delete(TodoLabel.TABLE_NAME, selection, selectionArgs);
                break;
            case LABEL_ID:
                idStr = uri.getLastPathSegment();
                where = TodoLabel._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                count = db.delete(TodoLabel.TABLE_NAME, where, selectionArgs);
                break;
            case NOTIFICATION_LIST:
                count = db.delete(TodoNotification.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTIFICATION_ID:
                idStr = uri.getLastPathSegment();
                where = TodoNotification._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                count = db.delete(TodoNotification.TABLE_NAME, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (count > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        db = tOpenHelper.getWritableDatabase();
        int count = 0;
        String idStr, where;
        switch(sUriMatcher.match(uri)) {
            case ENTRY_LIST:
                count = db.update(TodoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ENTRY_ID:
                idStr = uri.getLastPathSegment();
                where = TodoEntry._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                count = db.update(TodoEntry.TABLE_NAME, values, where,
                        selectionArgs);
                break;
            case LABEL_LIST:
                count = db.update(TodoLabel.TABLE_NAME, values, selection, selectionArgs);
                break;
            case LABEL_ID:
                idStr = uri.getLastPathSegment();
                where = TodoLabel._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                count = db.update(TodoLabel.TABLE_NAME, values, where,
                        selectionArgs);
                break;
            case NOTIFICATION_LIST:
                count = db.update(TodoNotification.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTIFICATION_ID:
                idStr = uri.getLastPathSegment();
                where = TodoNotification._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                count = db.update(TodoNotification.TABLE_NAME, values, where,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (count > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

}
