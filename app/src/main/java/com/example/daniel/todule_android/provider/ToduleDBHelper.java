package com.example.daniel.todule_android.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoEntry;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoLabel;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoNotification;


/**
 * Created by danieL on 7/31/2017.
 */

public class ToduleDBHelper extends SQLiteOpenHelper{
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TodoEntry.TABLE_NAME + " (" +
                    TodoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TodoEntry.COLUMN_NAME_TITLE + " TEXT," +
                    TodoEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    TodoEntry.COLUMN_NAME_CREATED_DATE + " INTEGER," +
                    TodoEntry.COLUMN_NAME_DUE_DATE + " INTEGER," +
                    TodoEntry.COLUMN_NAME_TASK_DONE + " INTEGER DEFAULT 0," +
                    TodoEntry.COLUMN_NAME_COMPLETED_DATE + " INTEGER DEFAULT NULL," +
                    TodoEntry.COLUMN_NAME_ARCHIVED + " INTEGER DEFAULT 0," +
                    TodoEntry.COLUMN_NAME_LABEL + " INTEGER DEFAULT NULL," +
                    TodoEntry.COLUMN_NAME_DELETED + " INTEGER DEFAULT 0," +
                    TodoEntry.COLUMN_NAME_DELETION_DATE + " INTEGER DEFAULT NULL" +
                    ");";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TodoEntry.TABLE_NAME;

    private static final String SQL_CREATE_LABELS =
            "CREATE TABLE " + TodoLabel.TABLE_NAME + " (" +
                    TodoLabel._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TodoLabel.COLUMN_NAME_TAG + " TEXT," +
                    TodoLabel.COLUMN_NAME_COLOR + " INTEGER," +
                    TodoLabel.COLUMN_NAME_TEXT_COLOR + " INTEGER" +
                    ");";

    private static final String SQL_DELETE_LABELS =
            "DROP TABLE IF EXISTS " + TodoLabel.TABLE_NAME;

    private static final String SQL_CREATE_NOTIFICATIONS =
            "CREATE TABLE " + TodoNotification.TABLE_NAME + " (" +
                    TodoNotification._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TodoNotification.COLUMN_NAME_TODULE_ID + " INTEGER," +
                    TodoNotification.COLUMN_NAME_REMINDER_TIME + " INTEGER" +
                    ");";

    private static final String SQL_DELETE_NOTIFICATIONS =
            "DROP TABLE IF EXISTS " + TodoNotification.TABLE_NAME;


    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 13;
    public static final String DATABASE_NAME = "Todule.db";

    public ToduleDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_LABELS);
        db.execSQL(SQL_CREATE_NOTIFICATIONS);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + TodoEntry.TABLE_NAME + " ADD COLUMN " + TodoEntry.COLUMN_NAME_COMPLETED_DATE + " INTEGER");
        }
        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE " + TodoEntry.TABLE_NAME + " ADD COLUMN " + TodoEntry.COLUMN_NAME_ARCHIVED + " INTEGER DEFAULT 0 ");
        }
        if (oldVersion < 8) {
            db.execSQL("ALTER TABLE " + TodoEntry.TABLE_NAME + " ADD COLUMN " + TodoEntry.COLUMN_NAME_LABEL + " INTEGER DEFAULT 0 ");
            db.execSQL(SQL_CREATE_LABELS);
        }
        if (oldVersion < 9) {
            db.execSQL("ALTER TABLE " + TodoLabel.TABLE_NAME + " ADD COLUMN " + TodoLabel.COLUMN_NAME_TEXT_COLOR + " INTEGER");
        }
        if(oldVersion <12) {
            db.execSQL("ALTER TABLE " + TodoEntry.TABLE_NAME + " ADD COLUMN " + TodoEntry.COLUMN_NAME_DELETED + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TodoEntry.TABLE_NAME + " ADD COLUMN " + TodoEntry.COLUMN_NAME_DELETION_DATE + " INTEGER DEFAULT NULL");
        }
        if (oldVersion < 13){
            db.execSQL(SQL_CREATE_NOTIFICATIONS);
        }
        else {
            db.execSQL(SQL_DELETE_ENTRIES);
            db.execSQL(SQL_DELETE_LABELS);
            db.execSQL(SQL_DELETE_NOTIFICATIONS);

            db.execSQL(SQL_CREATE_ENTRIES);
            db.execSQL(SQL_CREATE_LABELS);
            db.execSQL(SQL_CREATE_NOTIFICATIONS);
        }
    }

}
