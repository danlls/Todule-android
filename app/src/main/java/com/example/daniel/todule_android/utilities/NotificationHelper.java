package com.example.daniel.todule_android.utilities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.activities.NotificationReceiver;
import com.example.daniel.todule_android.provider.ToduleDBContract;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by danieL on 11/26/2017.
 */

public class NotificationHelper {

    public static PendingIntent initReminderPendingIntent(Context context, Uri toduleUri){
        Cursor cr = context.getContentResolver().query(toduleUri, ToduleDBContract.TodoEntry.PROJECTION_ALL, null, null, ToduleDBContract.TodoEntry.SORT_ORDER_DEFAULT);
        cr.moveToFirst();
        long itemId = Long.valueOf(toduleUri.getLastPathSegment());
        String title = cr.getString(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_TITLE));
        long dueDate = cr.getLong(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_DUE_DATE));
        String dueDateString = DateTimeUtils.dateTimeDiff(dueDate);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setData(Uri.parse(R.string.reminder_intent_scheme + String.valueOf(itemId)));
        intent.setAction("com.example.daniel.todule_android.REMINDER_NOTIFICATION");
        intent.putExtra("todule_id", itemId);
        intent.putExtra("todule_title", title);
        intent.putExtra("todule_due_date", dueDateString);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        cr.close();
        return sender;
    }

    public static void setReminder(Context context, Uri toduleUri, long datetimeInMillis){
        PendingIntent pIntent = initReminderPendingIntent(context, toduleUri);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, datetimeInMillis, pIntent);

        // Only update columns if notification instance was created previously
        long toduleId = ContentUris.parseId(toduleUri);
        String select = ToduleDBContract.TodoNotification.COLUMN_NAME_TODULE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(toduleId)};
        ContentValues cv = new ContentValues();
        cv.put(ToduleDBContract.TodoNotification.COLUMN_NAME_REMINDER_TIME, datetimeInMillis);
        cv.put(ToduleDBContract.TodoNotification.COLUMN_NAME_REMINDER_CANCELED, ToduleDBContract.TodoNotification.REMINDER_NOT_CANCELED);
        int nRowsEffected = context.getContentResolver().update(ToduleDBContract.TodoNotification.CONTENT_URI, cv, select, selectionArgs);

        // Insert db as no instance was created before
        if(nRowsEffected == 0){
            cv = new ContentValues();
            cv.put(ToduleDBContract.TodoNotification.COLUMN_NAME_TODULE_ID, ContentUris.parseId(toduleUri));
            cv.put(ToduleDBContract.TodoNotification.COLUMN_NAME_REMINDER_TIME, datetimeInMillis);
            cv.put(ToduleDBContract.TodoNotification.COLUMN_NAME_REMINDER_CANCELED, ToduleDBContract.TodoNotification.REMINDER_NOT_CANCELED);
            context.getContentResolver().insert(ToduleDBContract.TodoNotification.CONTENT_URI, cv);
        }
    }

    public static void cancelReminder(Context context, Uri toduleUri){
        PendingIntent pIntent = initReminderPendingIntent(context, toduleUri);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(pIntent);

        long toduleId = ContentUris.parseId(toduleUri);
        String select = ToduleDBContract.TodoNotification.COLUMN_NAME_TODULE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(toduleId)};

        ContentValues cv = new ContentValues();
        cv.put(ToduleDBContract.TodoNotification.COLUMN_NAME_REMINDER_CANCELED, ToduleDBContract.TodoNotification.REMINDER_CANCELED);
        context.getContentResolver().update(ToduleDBContract.TodoNotification.CONTENT_URI, cv, select, selectionArgs);
    }

    public static void cancelReminder(Context context, long toduleId){
        Uri toduleUri = ContentUris.withAppendedId(ToduleDBContract.TodoEntry.CONTENT_ID_URI_BASE, toduleId);
        PendingIntent pIntent = initReminderPendingIntent(context, toduleUri);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(pIntent);

        String select = ToduleDBContract.TodoNotification.COLUMN_NAME_TODULE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(toduleId)};

        ContentValues cv = new ContentValues();
        cv.put(ToduleDBContract.TodoNotification.COLUMN_NAME_REMINDER_CANCELED, ToduleDBContract.TodoNotification.REMINDER_CANCELED);
        context.getContentResolver().update(ToduleDBContract.TodoNotification.CONTENT_URI, cv, select, selectionArgs);
    }

    public static long getReminderTime(Context context, Uri toduleUri){
        long toduleId = ContentUris.parseId(toduleUri);
        String select = ToduleDBContract.TodoNotification.COLUMN_NAME_TODULE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(toduleId)};

        Cursor cr = context.getContentResolver().query(ToduleDBContract.TodoNotification.CONTENT_URI, null, select, selectionArgs, null);
        cr.moveToNext();
        long reminderTime = cr.getLong(cr.getColumnIndexOrThrow(ToduleDBContract.TodoNotification.COLUMN_NAME_REMINDER_TIME));
        cr.close();
        return reminderTime;
    }

}
