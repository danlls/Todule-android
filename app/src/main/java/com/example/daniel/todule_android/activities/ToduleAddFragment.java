package com.example.daniel.todule_android.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoEntry;
import com.example.daniel.todule_android.services.ExpiryUpdateService;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by danieL on 8/1/2017.
 */

public class ToduleAddFragment extends Fragment{
    Calendar myCalendar = Calendar.getInstance();
    MainActivity myActivity;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myActivity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        myActivity.fabVisibility(false);
        getActivity().findViewById(R.id.toolbar).setVisibility(View.GONE);
        myActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);

        EditText titleEdit = view.findViewById(R.id.edit_title);
        final EditText dateEdit = view.findViewById(R.id.edit_date);
        final EditText timeEdit = view.findViewById(R.id.edit_time);
        dateEdit.setInputType(InputType.TYPE_NULL);
        timeEdit.setInputType(InputType.TYPE_NULL);

        dateEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View view, boolean b) {
                if (b) {
                    myActivity.hideSoftKeyboard(true);
                    DatePickerDialog datePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                            myCalendar.set(Calendar.YEAR, i);
                            myCalendar.set(Calendar.MONTH, i1);
                            myCalendar.set(Calendar.DAY_OF_MONTH, i2);
                            DateFormat df = DateFormat.getDateInstance();
                            EditText ed = (EditText) view;
                            ed.setText(df.format(myCalendar.getTime()));
                        }
                    }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));
                    datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                    datePicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            dateEdit.clearFocus();
                        }
                    });
                    datePicker.show();
                }
            }
        });
        timeEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View view, boolean b) {
                if (b) {
                    myActivity.hideSoftKeyboard(true);
                    TimePickerDialog timePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int i, int i1) {
                            myCalendar.set(Calendar.HOUR_OF_DAY, i);
                            myCalendar.set(Calendar.MINUTE, i1);
                            DateFormat dft = DateFormat.getTimeInstance(DateFormat.SHORT);
                            EditText ed = (EditText) view;
                            ed.setText(dft.format(myCalendar.getTime()));
                        }
                    }, myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true);
                    timePicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            timeEdit.clearFocus();
                        }
                    });
                    timePicker.show();
                }
            }
        });

        timeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                long date_time = myCalendar.getTimeInMillis();
                long current_date_time = System.currentTimeMillis();
                if (date_time < current_date_time){
                    timeEdit.setError("Please select a time later than now");
                } else {
                    timeEdit.setError(null);
                }
            }
        });

        // Set default
        myCalendar.set(Calendar.HOUR_OF_DAY, 23);
        myCalendar.set(Calendar.MINUTE, 59);
        myCalendar.set(Calendar.SECOND, 0);
        DateFormat df = DateFormat.getDateInstance();
        dateEdit.setText(df.format(myCalendar.getTime()));
        DateFormat df2 = DateFormat.getTimeInstance(DateFormat.SHORT);
        timeEdit.setText(df2.format(myCalendar.getTime()));

        titleEdit.requestFocus();
        titleEdit.postDelayed(new Runnable() {
            @Override
            public void run() {
                myActivity.hideSoftKeyboard(false);
            }
        }, 200);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_add, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_save:
                if (validateInputs()){
                    addEntry();
                    Toast.makeText(myActivity, R.string.entry_created, Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void addEntry(){
        EditText title = (EditText) getView().findViewById(R.id.edit_title);
        String title_text = title.getText().toString();

        EditText description = (EditText) getView().findViewById(R.id.edit_description);
        String description_text = description.getText().toString();

        long due_date = myCalendar.getTimeInMillis();
        long created_date = System.currentTimeMillis();

        ContentValues cv = new ContentValues();
        cv.put(TodoEntry.COLUMN_NAME_TITLE, title_text);
        cv.put(TodoEntry.COLUMN_NAME_DESCRIPTION, description_text);
        cv.put(TodoEntry.COLUMN_NAME_DUE_DATE, due_date);
        cv.put(TodoEntry.COLUMN_NAME_CREATED_DATE, created_date);
        Uri itemUri = getContext().getContentResolver().insert(TodoEntry.CONTENT_URI, cv);
        // Reminder set at one minute before due_date
        myActivity.setReminder(itemUri, due_date - 60 * 60 * 1000);
        setExpiry(itemUri, due_date);
    }

    @Override
    public void onDestroyView() {
        myActivity.fabVisibility(true);
        getActivity().findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
        myActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        super.onDestroyView();
    }

    private boolean validateInputs() {
        boolean valid;
        // Validates title (required field, ensure title is given.)
        EditText title = (EditText) getView().findViewById(R.id.edit_title);
        if (!title.getText().toString().isEmpty()) {
            valid = true;
        } else {
            title.setError("This field is required.");
            title.requestFocus();
            myActivity.hideSoftKeyboard(false);
            valid = false;
        }

        // Validates due_date (must be later than now.)
        EditText time = (EditText) getView().findViewById(R.id.edit_time);
        if (time.getError() != null){
            time.requestFocus();
            valid = false;
        }

        return valid;
    }

    private void setExpiry(Uri itemUri, long datetimeInMillis) {
        Intent intent = new Intent(getContext(), ExpiryUpdateService.class);
        intent.setData(itemUri);

        PendingIntent sender = PendingIntent.getService(getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) myActivity.getSystemService(Context.ALARM_SERVICE);
        if(Build.VERSION.SDK_INT >= 23){
            am.setExactAndAllowWhileIdle(AlarmManager.RTC, datetimeInMillis, sender);
        } else if (Build.VERSION.SDK_INT >= 19) {
            am.setExact(AlarmManager.RTC, datetimeInMillis, sender);
        } else {
            am.set(AlarmManager.RTC, datetimeInMillis, sender);
        }
    }
}
