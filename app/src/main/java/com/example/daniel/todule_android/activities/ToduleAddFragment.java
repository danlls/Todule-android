package com.example.daniel.todule_android.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoEntry;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by danieL on 8/1/2017.
 */

public class ToduleAddFragment extends Fragment implements View.OnClickListener{
    Calendar myCalendar = Calendar.getInstance();


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        ((MainActivity)getActivity()).fabVisibility(false);
        getActivity().findViewById(R.id.toolbar).setVisibility(View.GONE);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button saveButton = (Button) view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);
        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(this);

        EditText dateEdit = (EditText) view.findViewById(R.id.edit_date);
        dateEdit.setOnClickListener(this);
        DateFormat df = DateFormat.getDateInstance();
        dateEdit.setText(df.format(myCalendar.getTime()));
        EditText timeEdit = (EditText) view.findViewById(R.id.edit_time);
        timeEdit.setOnClickListener(this);
        timeEdit.setText(String.format("%02d", Calendar.HOUR_OF_DAY) + ": " + String.format("%02d", Calendar.MINUTE));

        return view;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.save_button:
                // TODO : Validate before save
                addEntry();
                getActivity().onBackPressed();
                break;
            case R.id.cancel_button:
                getActivity().onBackPressed();
                break;
            case R.id.edit_date:
                final EditText editDate = (EditText) view;
                DatePickerDialog datePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        myCalendar.set(Calendar.YEAR, i);
                        myCalendar.set(Calendar.MONTH, i1);
                        myCalendar.set(Calendar.DAY_OF_MONTH, i2);
                        DateFormat df = DateFormat.getDateInstance();
                        editDate.setText(df.format(myCalendar.getTime()));
                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePicker.show();
                break;
            case R.id.edit_time:
                final EditText editTime = (EditText) view;
                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        myCalendar.set(Calendar.HOUR_OF_DAY, i);
                        myCalendar.set(Calendar.MINUTE, i1);
                        editTime.setText(String.format("%02d", i) + ":" + String.format("%02d", i1));
                    }
                }, myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true)
                        .show();
                break;
        }
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
        getContext().getContentResolver().insert(TodoEntry.CONTENT_URI, cv);
    }

    @Override
    public void onDestroyView() {
        ((MainActivity)getActivity()).fabVisibility(true);
        getActivity().findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        super.onDestroyView();
    }
}
