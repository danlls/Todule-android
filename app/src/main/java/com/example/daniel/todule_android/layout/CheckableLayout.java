package com.example.daniel.todule_android.layout;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Checkable;
import android.widget.LinearLayout;


/**
 * Created by danieL on 10/24/2017.
 */

public class CheckableLayout extends LinearLayout implements Checkable {
    private boolean mChecked;

    public CheckableLayout (Context context) {
        super(context);
    }

    public CheckableLayout (Context context, AttributeSet attrs) {
        super(context, attrs);
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
        setBackground(b ? new ColorDrawable(android.graphics.Color.LTGRAY) : null);
    }


}