package com.example.daniel.todule_android.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.provider.ToduleDBContract;
import com.example.daniel.todule_android.utilities.NotificationHelper;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by danieL on 10/10/2017.
 */

public class NotificationReceiver extends BroadcastReceiver {

    String CHANNEL_ID = "todule_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.BOOT_COMPLETED") || action.equals("android.intent.action.REBOOT")) {
                    Log.d("BroadCast Received", "ON BOOT COMPLETE");
                    Cursor cr = context.getContentResolver().query(ToduleDBContract.TodoNotification.CONTENT_URI, null, null, null, null);
                    try {
                        while (cr.moveToNext()) {
                            long toduleId = cr.getLong(cr.getColumnIndexOrThrow(ToduleDBContract.TodoNotification.COLUMN_NAME_TODULE_ID));
                            int reminderTime = cr.getInt(cr.getColumnIndexOrThrow(ToduleDBContract.TodoNotification.COLUMN_NAME_REMINDER_TIME));
                            Uri toduleUri = ContentUris.withAppendedId(ToduleDBContract.TodoNotification.CONTENT_ID_URI_BASE, toduleId);
                            NotificationHelper.setReminder(context, toduleUri, reminderTime);
                        }
                    } finally {
                        cr.close();
                    }
                } else if (action.equals("com.example.daniel.todule_android.VIEW_ENTRY")) {
                    long entryId = intent.getLongExtra("todule_id", -1L);
                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    notificationIntent.putExtra("todule_id", entryId);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    PendingIntent resultPendingIntent =
                            PendingIntent.getActivity(
                                    context,
                                    0,
                                    notificationIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_stat_todule)
                                    .setContentTitle("Reminder: " + intent.getStringExtra("todule_title"))
                                    .setContentText(intent.getStringExtra("todule_due_date"))
                                    .setContentIntent(resultPendingIntent)
                                    .setAutoCancel(true);

                    mBuilder.setDefaults(Notification.DEFAULT_ALL);

                    // Gets an instance of the NotificationManager service
                    NotificationManager mNotifyMgr =
                            (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                    // Builds the notification and issues it.
                    mNotifyMgr.notify((int) entryId, mBuilder.build());
                }
            }

        }
    }
}
