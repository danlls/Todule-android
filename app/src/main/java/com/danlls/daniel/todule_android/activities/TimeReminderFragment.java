package com.danlls.daniel.todule_android.activities;


import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import com.danlls.daniel.todule_android.R;


public class TimeReminderFragment extends Fragment {
    SharedPreferences sf;
    Calendar myCalendar;
    MainActivity myActivity;
    TextInputEditText timeViewHours;
    TextInputEditText timeViewDays;


    public static TimeReminderFragment newInstance() {

        TimeReminderFragment fragment = new TimeReminderFragment();
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sf = PreferenceManager.getDefaultSharedPreferences(getContext());
        myCalendar = Calendar.getInstance();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myActivity = (MainActivity) getActivity();
        myActivity.getSupportActionBar().setTitle("Edit Reminder");
        myActivity.hideSoftKeyboard(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_time_reminder, container, false);
        timeViewDays = view.findViewById(R.id.timeViewDays);
        timeViewHours = view.findViewById(R.id.timeViewHours);
        //timeViewDays.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        timeViewHours.setInputType(InputType.TYPE_NULL);

        // Default for new entry
        if (sf.contains("timing")){
            myCalendar.set(Calendar.HOUR_OF_DAY, sf.getInt("hours",1));
            myCalendar.set(Calendar.MINUTE, sf.getInt("minutes",0));
            myCalendar.set(Calendar.SECOND, 0);
            timeViewDays.setText(String.valueOf(sf.getInt("days",0)));
        }else {
            myCalendar.set(Calendar.HOUR_OF_DAY, 1);
            myCalendar.set(Calendar.MINUTE, 0);
            myCalendar.set(Calendar.SECOND, 0);
            timeViewDays.setText("0");
        }

        timeViewHours.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View view, boolean b) {
                if (b) {
                    myActivity.hideSoftKeyboard(true);
                    TimePickerDialog timePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int i, int i1) {
                            myCalendar.set(Calendar.HOUR_OF_DAY, i);
                            myCalendar.set(Calendar.MINUTE, i1);
                            EditText ed = (EditText) view;
                            ed.setText(myCalendar.get(Calendar.HOUR_OF_DAY) +":"+myCalendar.get(Calendar.MINUTE));
                        }
                    }, myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true);
                    timePicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            timeViewHours.clearFocus();
                        }
                    });
                    timePicker.show();
                }
            }
        });

        timeViewHours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                long date_time = myCalendar.getTimeInMillis();
            }
        });

        timeViewHours.setText(myCalendar.get(Calendar.HOUR_OF_DAY) +":"+myCalendar.get(Calendar.MINUTE));



        return view;
    }

    private void ShowTos(String msg) {
        Toast.makeText(getActivity(), msg,
                Toast.LENGTH_LONG).show();
    }

    public long getOnlyHoursAndMins(int dayHourMin,int t){
        if (t == 1) return dayHourMin * 60 * 60 * 1000; // convert hours to millis
        if (t == 2) return dayHourMin * 24 * 60 * 60 *1000; // convert days to millis
        else return dayHourMin * 60 * 1000; // convert minutes to millis
    }




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_time_save, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_save:
                if (!timeViewDays.getText().toString().equals("") && timeViewDays.getText().toString().matches("[0-9]+")) {
                    long timingInMillis = getOnlyHoursAndMins(Integer.parseInt(timeViewDays.getText().toString()), 2)
                            + getOnlyHoursAndMins(myCalendar.get(Calendar.HOUR_OF_DAY), 1)
                            + getOnlyHoursAndMins(myCalendar.get(Calendar.MINUTE), 3);
                    MainActivity.timing = timingInMillis;
                    MainActivity.Tdays = Integer.parseInt(timeViewDays.getText().toString());
                    MainActivity.Thours = myCalendar.get(Calendar.HOUR_OF_DAY);
                    MainActivity.Tmins = myCalendar.get(Calendar.MINUTE);
                    SharedPreferences.Editor editor = sf.edit();
                    editor.putLong("timing", timingInMillis);
                    editor.putInt("hours", myCalendar.get(Calendar.HOUR_OF_DAY));
                    editor.putInt("minutes", myCalendar.get(Calendar.MINUTE));
                    editor.putInt("days", Integer.parseInt(timeViewDays.getText().toString()));
                    editor.apply();
                    ShowTos("Reminder updated "+timeViewDays.getText().toString() + " Days and "
                            + myCalendar.get(Calendar.HOUR_OF_DAY)+":"+myCalendar.get(Calendar.MINUTE));
                    return true;
                }else{
                    ShowTos("Error in input of Days !");
                }
        }
        return super.onOptionsItemSelected(item);
    }

}
