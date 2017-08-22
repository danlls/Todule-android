package com.example.daniel.todule_android.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

        final EditText dateEdit = view.findViewById(R.id.edit_date);
        final EditText timeEdit = view.findViewById(R.id.edit_time);
        dateEdit.setInputType(InputType.TYPE_NULL);
        timeEdit.setInputType(InputType.TYPE_NULL);

        dateEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View view, boolean b) {
                if (b) {
                    ((MainActivity) getActivity()).hideSoftKeyboard(true);
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
                    ((MainActivity) getActivity()).hideSoftKeyboard(true);
                    TimePickerDialog timePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int i, int i1) {
                            myCalendar.set(Calendar.HOUR_OF_DAY, i);
                            myCalendar.set(Calendar.MINUTE, i1);
                            DateFormat dft = DateFormat.getTimeInstance();
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
        DateFormat df2 = DateFormat.getTimeInstance();
        timeEdit.setText(df2.format(myCalendar.getTime()));

        return view;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.save_button:
                if (validateInputs()){
                    addEntry();
                    getActivity().onBackPressed();
                }
                break;
            case R.id.cancel_button:
                getActivity().onBackPressed();
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

    private boolean validateInputs() {
        boolean valid;
        // Validates title (required field, ensure title is given.)
        EditText title = (EditText) getView().findViewById(R.id.edit_title);
        if (!title.getText().toString().isEmpty()) {
            valid = true;
        } else {
            title.setError("This field is required.");
            title.requestFocus();
            ((MainActivity) getActivity()).hideSoftKeyboard(false);
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
}
