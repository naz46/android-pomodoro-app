package com.nazlcanozturk46.pomodoro;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AboutPomodoro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_pomodoro);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("About Pomodoro");
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));

    }
}
