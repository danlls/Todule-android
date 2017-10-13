package com.example.daniel.todule_android.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.daniel.todule_android.provider.ToduleDBContract;

/**
 * Created by danieL on 10/13/2017.
 */

public class ExpiryUpdateService extends IntentService {

    public ExpiryUpdateService() {
        super("ExpiryUpdateService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Uri aUri = intent.getData();
        ContentValues cv = new ContentValues();
        cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_TASK_DONE, ToduleDBContract.TodoEntry.TASK_EXPIRED);
        getContentResolver().update(aUri, cv, null, null);
    }
}
