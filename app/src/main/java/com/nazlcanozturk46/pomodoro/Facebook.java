package com.nazlcanozturk46.pomodoro;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;


public class Facebook extends AppCompatActivity {

    ImageView imageView;
    TextView textViewName, textViewEmail;
    Button buttonOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);

        imageView = findViewById(R.id.imageView3);
        textViewName = findViewById(R.id.textView14);
        textViewEmail = findViewById(R.id.textView15);
        buttonOut = findViewById(R.id.button2);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Account");
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));

        // taking users information from Login.java
        String name = Login.fullName;
        String image = Login.imageUrl;
        String email = Login.email;
        Log.i("image", image);
        Log.i("email", email);

        //Showing users profile image
        showImageOne(imageView, image);

        //cheking info
        if (name != null && email != null) {
            textViewName.setText(name);
            textViewEmail.setText(email);
        } else {
            Toast.makeText(getApplicationContext(), "Data Not Found", Toast.LENGTH_LONG).show();
        }


        buttonOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //logout from Facebook
                LoginManager.getInstance().logOut();
                Intent i = new Intent(getApplicationContext(), Login.class);
                startActivity(i);
            }
        });

    }

    public static void showImageOne(ImageView v, String url) {
        new AsyncLoadImage(v).execute(url);
    }


}
