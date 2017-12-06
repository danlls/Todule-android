package com.example.daniel.todule_android.layout;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;

import com.example.daniel.todule_android.R;


/**
 * Created by danieL on 10/24/2017.
 */

public class CheckableLinearLayout extends LinearLayout implements Checkable {
    private boolean mChecked;
    private Context mContext;

    public CheckableLinearLayout(Context context) {
        super(context);
        mContext = context;
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public void setChecked(boolean b) {
        mChecked = b;
        CheckBox cb = findViewById(R.id.checkbox);
        if(b){
            setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccentLight));
            cb.setChecked(true);

        } else {
            setBackground(null);
            cb.setChecked(false);
        }

    }


}