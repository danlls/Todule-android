package com.example.daniel.todule_android.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.adapter.LabelAdapter;
import com.example.daniel.todule_android.provider.ToduleDBContract;
import com.example.daniel.todule_android.provider.ToduleDBContract.TodoEntry;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by danieL on 8/1/2017.
 */

public class ToduleAddFragment extends Fragment{
    Calendar myCalendar;
    MainActivity myActivity;
    Long chosenLabelId = -1L;
    String mode;
    Long entryId;
    String title;
    String description;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if(bundle != null){
            mode = bundle.getString("mode");
            if(bundle.containsKey("entry_id")){
                entryId = bundle.getLong("entry_id");
            } else {
                entryId = null;
            }
        }

        if(savedInstanceState != null){
            myCalendar = (Calendar) savedInstanceState.getSerializable("calendar");
            chosenLabelId = savedInstanceState.getLong("chosen_label_id", -1L);
            title = savedInstanceState.getString("title");
        } else {
            myCalendar = Calendar.getInstance();
            // Default for new entry
            myCalendar.set(Calendar.HOUR_OF_DAY, 23);
            myCalendar.set(Calendar.MINUTE, 59);
            myCalendar.set(Calendar.SECOND, 0);

            if (mode.equals("edit_entry")){
                Uri entryUri = ContentUris.withAppendedId(ToduleDBContract.TodoEntry.CONTENT_ID_URI_BASE, entryId);
                Cursor cr = getContext().getContentResolver().query(entryUri, TodoEntry.PROJECTION_ALL, null, null, TodoEntry.SORT_ORDER_DEFAULT);
                cr.moveToFirst();

                long dueDateInMillis = cr.getLong(cr.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_DUE_DATE));
                title = cr.getString(cr.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_TITLE));
                description = cr.getString(cr.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_DESCRIPTION));
                if(cr.isNull(cr.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_LABEL))){
                    chosenLabelId = -1L;
                } else{
                    chosenLabelId = cr.getLong(cr.getColumnIndexOrThrow(TodoEntry.COLUMN_NAME_LABEL));
                }

                myCalendar.setTimeInMillis(dueDateInMillis);

                cr.close();
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myActivity = (MainActivity) getActivity();
        setHasOptionsMenu(true);
        if(mode.equals("edit_entry")){
            myActivity.getSupportActionBar().setTitle(title);
        } else {
            myActivity.getSupportActionBar().setTitle("New entry");
        }
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        EditText titleEdit = view.findViewById(R.id.edit_title);
        EditText descriptionEdit = view.findViewById(R.id.edit_description);
        final EditText dateEdit = view.findViewById(R.id.edit_date);
        final EditText timeEdit = view.findViewById(R.id.edit_time);
        dateEdit.setInputType(InputType.TYPE_NULL);
        timeEdit.setInputType(InputType.TYPE_NULL);

        titleEdit.setText(title);
        descriptionEdit.setText(description);

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

        Button selectLabel = view.findViewById(R.id.select_label);
        selectLabel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ToduleLabelFragment labelFrag = ToduleLabelFragment.newInstance(true, chosenLabelId);
                myActivity.hideSoftKeyboard(true);
                myActivity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.fragment_container, labelFrag)
                        .addToBackStack(null)
                        .commit();
            }
        });


        TextView chosenLabel = view.findViewById(R.id.chosen_label);
        if(chosenLabelId != -1L) {
            Uri labelUri = ContentUris.withAppendedId(ToduleDBContract.TodoLabel.CONTENT_ID_URI_BASE, chosenLabelId);
            Cursor cr = getContext().getContentResolver().query(labelUri, ToduleDBContract.TodoLabel.PROJECTION_ALL, null, null, ToduleDBContract.TodoLabel.SORT_ORDER_DEFAULT);

            cr.moveToFirst();
            String labelText = cr.getString(cr.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_TAG));
            int textColor = cr.getInt(cr.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_TEXT_COLOR));
            int color = cr.getInt(cr.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_COLOR));
            chosenLabel.setText(labelText);
            chosenLabel.setTextColor(textColor);
            chosenLabel.setBackgroundColor(color);
            cr.close();
        } else {
            chosenLabel.setText(R.string.no_label_selected);
        }



        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("chosen_label_id", chosenLabelId);
        outState.putSerializable("calendar", myCalendar);
        outState.putString("title", title);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_add, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_save:
                if (validateInputs()){
                    updateEntry();
                    getActivity().onBackPressed();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void updateEntry(){
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
        if(chosenLabelId == -1L){
            cv.putNull(TodoEntry.COLUMN_NAME_LABEL);
        } else {
            cv.put(TodoEntry.COLUMN_NAME_LABEL, chosenLabelId);
        }

        Uri entryUri;
        if(entryId == null) {
            entryUri = getContext().getContentResolver().insert(TodoEntry.CONTENT_URI, cv);
            Toast.makeText(myActivity, getString(R.string.entry_created) + ": " + title_text, Toast.LENGTH_SHORT).show();
        } else {
            entryUri = ContentUris.withAppendedId(TodoEntry.CONTENT_ID_URI_BASE, entryId);
            getContext().getContentResolver().update(entryUri, cv, null, null);
            Toast.makeText(myActivity, getString(R.string.entry_updated) + ": " + title_text, Toast.LENGTH_SHORT).show();
        }
        // Reminder set at one hour before due_date
        myActivity.setReminder(entryUri, due_date - 60 * 60 * 1000);
    }

    private boolean validateInputs() {
        boolean valid = true;
        // Validates title (required field, ensure title is given.)
        EditText title = (EditText) getView().findViewById(R.id.edit_title);
        if (title.getText().toString().isEmpty()) {
            title.setError("This field is required.");
            title.requestFocus();
            myActivity.hideSoftKeyboard(false);
            valid = false;
        }

        // Validates due_date (must be later than now.)
        EditText time = (EditText) getView().findViewById(R.id.edit_time);
        long date_time = myCalendar.getTimeInMillis();
        long current_date_time = System.currentTimeMillis();
        if (date_time < current_date_time){
            time.setError("Please select a time later than now");
            time.requestFocus();
            valid = false;
        } else {
            time.setError(null);
        }

        return valid;
    }

    public void setLabel(long id){
        if( id < 0){
            chosenLabelId = -1L;
        } else {
            chosenLabelId = id;
        }
    }

}
