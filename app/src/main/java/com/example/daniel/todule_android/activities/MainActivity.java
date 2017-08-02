package com.example.daniel.todule_android.activities;


import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.daniel.todule_android.R;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, new ToduleListFragment())
                    .commit();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, new ToduleAddFragment());
                ft.addToBackStack(null);
                ft.commit();
            }
        });

    }

    public void fabVisibility(boolean show){
        FloatingActionButton fabBtn = (FloatingActionButton)findViewById(R.id.add_fab);
        if (show) fabBtn.show();
        else fabBtn.hide();
    }


}
