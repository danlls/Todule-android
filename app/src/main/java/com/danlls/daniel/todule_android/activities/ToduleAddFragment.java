package com.danlls.daniel.todule_android.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
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

import com.danlls.daniel.todule_android.R;
import com.danlls.daniel.todule_android.provider.ToduleDBContract;
import com.danlls.daniel.todule_android.provider.ToduleDBContract.TodoEntry;
import com.danlls.daniel.todule_android.utilities.NotificationHelper;

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

        final TextInputEditText titleEdit = view.findViewById(R.id.edit_title);
        final TextInputLayout titleWrapper = view.findViewById(R.id.edit_title_wrapper);

        TextInputEditText descriptionEdit = view.findViewById(R.id.edit_description);
        TextInputLayout descriptionWrapper = view.findViewById(R.id.edit_description_wrapper);

        final TextInputEditText dateEdit = view.findViewById(R.id.edit_date);
        final TextInputLayout timeWrapper = view.findViewById(R.id.edit_time_wrapper);
        final TextInputEditText timeEdit = view.findViewById(R.id.edit_time);
        dateEdit.setInputType(InputType.TYPE_NULL);
        timeEdit.setInputType(InputType.TYPE_NULL);

        titleEdit.setText(title);
        descriptionEdit.setText(description);

        titleEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(titleEdit.getText().toString().isEmpty()){
                    titleWrapper.setErrorEnabled(true);
                    titleWrapper.setError("This field is required");
                } else {
                    titleWrapper.setError(null);
                    titleWrapper.setErrorEnabled(false);
                }
            }
        });

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
                long date_time = myCalendar.getTimeInMillis();//utilise ca
                long current_date_time = System.currentTimeMillis();
                if (date_time < current_date_time) {
                    timeWrapper.setErrorEnabled(true);
                    timeWrapper.setError("Please select a date later than now");
                } else {
                    timeWrapper.setError(null);
                    timeWrapper.setErrorEnabled(false);
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
                    ShowTos(MainActivity.Tdays);
                    getActivity().onBackPressed();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void ShowTos(int d) {
        String msg = "";
        if (d == 0){
            msg = "You will be notified "+MainActivity.Thours+" hours and "+MainActivity.Tmins+" mins before deadline";
        }else{
            msg = "You will be notified "+MainActivity.Tdays+" days and "+MainActivity.Thours+" hours and "+MainActivity.Tmins+" mins before deadline";
        }
        Toast.makeText(getActivity(),msg,
                Toast.LENGTH_LONG).show();
    }

    private void updateEntry(){
        TextInputEditText title = getView().findViewById(R.id.edit_title);
        String title_text = title.getText().toString();

        TextInputEditText description = getView().findViewById(R.id.edit_description);
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
        NotificationHelper.setReminder(getContext(), entryUri, due_date - MainActivity.timing);
    }

    private boolean validateInputs() {
        boolean valid = true;
        // Validates title (required field, ensure title is given.)
        TextInputLayout titleWrapper = getView().findViewById(R.id.edit_title_wrapper);
        TextInputEditText title = getView().findViewById(R.id.edit_title);

        if (title.getText().toString().isEmpty()) {
            titleWrapper.setErrorEnabled(true);
            titleWrapper.setError("This field is required.");
            title.requestFocus();
            myActivity.hideSoftKeyboard(false);
            valid = false;
        } else {
            titleWrapper.setError(null);
            titleWrapper.setErrorEnabled(false);
        }

        // Validates due_date (must be later than now.)
        TextInputLayout timeWrapper = getView().findViewById(R.id.edit_time_wrapper);
        TextInputEditText time = getView().findViewById(R.id.edit_time);
        long date_time = myCalendar.getTimeInMillis();
        long current_date_time = System.currentTimeMillis();
        if (date_time < current_date_time){
            timeWrapper.setErrorEnabled(true);
            timeWrapper.setError("Please select a time later than now");
            time.requestFocus();
            valid = false;
        } else {
            time.setError(null);
            timeWrapper.setErrorEnabled(false);
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
