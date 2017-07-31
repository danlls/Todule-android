package com.example.daniel.todule_android.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Created by danieL on 7/31/2017.
 */

public final class ToduleDBContract {
    public static final String AUTHORITY = "com.example.todule.provider";
    public static final String SCHEME = "content://";
    public static final String SLASH = "/";
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ToduleDBContract() {}

    /* Inner class that defines the table contents */
    public static final class TodoEntry implements BaseColumns {

        public static final String TABLE_NAME = "todo_entry";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_CREATED_DATE = "created_date";
        public static final String COLUMN_NAME_DUE_DATE = "due_date";


        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME);
        /**
         * The content URI base for a single row. An ID must be appended.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME + SLASH);

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.example.todule.todo_entry";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.example.todule.todo_entry";

    }
}