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

/**
 * Created by danieL on 7/31/2017.
 */

public class ToduleProvider extends ContentProvider{

    // UriMatcher constants
    private static final int ENTRY_LIST = 1;
    private static final int ENTRY_ID = 2;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI("com.example.todule.provider", "todo_entry", ENTRY_LIST);
        sUriMatcher.addURI("com.example.todule.provider", "todo_entry/#", ENTRY_ID);
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
                    sortOrder = "_ID ASC";
                }
                break;
            // Single row
            case ENTRY_ID:
                builder.setTables(TodoEntry.TABLE_NAME);
                selection = selection + "_ID = " + uri.getLastPathSegment();
                builder.appendWhere(selection);
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
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues values){
        if (sUriMatcher.match(uri) != ENTRY_LIST){
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }
        db = tOpenHelper.getWritableDatabase();
        long id = db.insert(TodoEntry.TABLE_NAME, null, values);
        Uri itemUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(itemUri, null);
        return itemUri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs){
        db = tOpenHelper.getWritableDatabase();
        int count = 0;
        switch(sUriMatcher.match(uri)) {
            case ENTRY_LIST:
                count = db.delete(TodoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ENTRY_ID:
                String idStr = uri.getLastPathSegment();
                String where = TodoEntry._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                count = db.delete(TodoEntry.TABLE_NAME, where, selectionArgs);
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
        switch(sUriMatcher.match(uri)) {
            case ENTRY_LIST:
                count = db.update(TodoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ENTRY_ID:
                String idStr = uri.getLastPathSegment();
                String where = TodoEntry._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                count = db.update(TodoEntry.TABLE_NAME, values, where,
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
