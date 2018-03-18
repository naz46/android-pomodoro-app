package com.nazlcanozturk46.pomodoro;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

import static com.nazlcanozturk46.pomodoro.R.drawable.actionbar_background;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private long timeCountInMilliSeconds = 1 * 60000;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadSettings();
        setTimer();
    }

    private enum TimerStatus {
        STARTED,
        STOPPED,
    }

    int breakCount = 0;
    boolean breakOrWork = false;
    private TimerStatus timerStatus = TimerStatus.STOPPED;
    private ProgressBar progressBarCircle;
    private TextView textViewTime;
    private ImageView imageViewReset;
    private ImageView imageViewStartStop;
    private ImageView imageViewTomato, imageViewWork, imageViewBreak;
   // private ImageView imageViewPomodora1, imageViewPomodora2, imageViewPomodora3, imageViewPomodora4;
    private CountDownTimer countDownTimer;
    private boolean  vibration;
    private SharedPreferences settings;
    private Vibrator vibrator;

    //hamburger menu
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    ImageView imageViewPic;
    TextView textViewName,textViewEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // method call to initialize the views
        initViews();

        //Set vibrate feature
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Toggle menu
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Taking Navigation menu settings
        navigationClick();

        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //Taking to feature from the setting menu
        loadSettings();

        //ActionBar set
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Pomodoro");
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));

        // method call to initialize the listeners
        initListeners();
        //method call to initialize the settings menu item
        setTimer();

    }

    /**
     * method to Navigation menu settings
     */
    public void navigationClick() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                if (item.getItemId() == R.id.settings) {
                    Intent settingsIntent = new Intent(getApplicationContext(), Settings.class);
                    startActivity(settingsIntent);
                } else if (item.getItemId() == R.id.about) {
                    Intent aboutIntent = new Intent(getApplicationContext(), AboutPomodoro.class);
                    startActivity(aboutIntent);

                } else if (item.getItemId() == R.id.share) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("Text/Plain");
                    String shareBody = "There are many improvements you can experience from successfully implementing the pomodoro technique into your life, making it one of your good habits. Here are some of the improvements you will see;\n" + "\n" +
                            "Increased productivity.\n" +
                            "Improved quality and quantity of work.\n" +
                            "Better time management.\n" +
                            "Strengthened focus and motivation.\n" +
                            "The ability to stay fresh throughout the work day.";
                    String shareSub = "Focus with Pomodoro";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
                    startActivity(Intent.createChooser(shareIntent, "Share Using"));
                } else if (item.getItemId() == R.id.account) {
                    Intent accountIntent = new Intent(getApplicationContext(), Facebook.class);
                    startActivity(accountIntent);
                }
                return false;
            }
        });
    }

    /**
     * method to initialize the settings menu item
     */
    private void setTimer() {
        Long work = Long.valueOf(Integer.parseInt(settings.getString("work_duration", "1")) * 60000);
        textViewTime.setText(hmsTimeFormatter(work));
    }

    /**
     * Method to take settings from the setting menu
     */
    private void loadSettings() {
        vibration = settings.getBoolean("vibration", false);
        settings.registerOnSharedPreferenceChangeListener(MainActivity.this);
    }

    /**
     * method to initialize the views
     */
    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view);
        progressBarCircle = findViewById(R.id.progressBarCircle);
        textViewTime = findViewById(R.id.textViewTime);
        imageViewReset = findViewById(R.id.imageViewReset);
        imageViewStartStop = findViewById(R.id.imageViewStartStop);
        imageViewTomato = findViewById(R.id.imageViewTomato);
        imageViewBreak = findViewById(R.id.imageViewBreak);
        imageViewWork = findViewById(R.id.imageViewWork);
    }

    /**
     * method to initialize the click listeners
     */
    private void initListeners() {
        imageViewReset.setOnClickListener(this);
        imageViewStartStop.setOnClickListener(this);
        imageViewTomato.setOnClickListener(this);
    }

    /**
     * implemented method to listen clicks
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageViewReset:
                reset();
                break;
            case R.id.imageViewStartStop:
                workstartStop();
                break;
            case R.id.imageViewTomato:
                visibleButton();
                workstartStop();
                break;
        }
    }

    /**
     * method to visible start stop icon
     */
    public void visibleButton() {
        imageViewStartStop.setVisibility(View.VISIBLE);
        imageViewTomato.setVisibility(View.GONE);
    }

    /**
     * method to reset count down timer
     */
    private void reset() {
        breakCount = 0;
        stopCountDownTimer();
        //startCountDownTimer();
        textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
        // call to initialize the progress bar values
        setProgressBarValues();
        //hiding break and work icon
        imageViewBreak.setVisibility(View.GONE);
        imageViewWork.setVisibility(View.GONE);
        // changing stop icon to start icon
        imageViewStartStop.setImageResource(R.mipmap.icon_start);
        // changing the timer status to stopped
        timerStatus = TimerStatus.STOPPED;
    }


    /**
     * method to start and stop count down timer
     */
    private void workstartStop() {

        breakOrWork = true;
        if (timerStatus == TimerStatus.STOPPED) {

            // call to initialize the timer values
            WorkSetTimerValues();
            // call to initialize the progress bar values
            setProgressBarValues();
            // showing the work icon
            imageViewWork.setVisibility(View.VISIBLE);
            // showing the reset icon
            imageViewReset.setVisibility(View.VISIBLE);
            // changing play icon to stop icon
            imageViewStartStop.setImageResource(R.mipmap.icon_pause);
            // changing the timer status to started
            timerStatus = TimerStatus.STARTED;
            // call to start the count down timer
            startCountDownTimer();


        } else {
            // changing stop icon to start icon
            imageViewStartStop.setImageResource(R.mipmap.icon_start);
            // changing the timer status to stopped
            timerStatus = TimerStatus.STOPPED;
            stopCountDownTimer();

        }

    }

    /**
     * method to initialize the values for count down timer work
     */
    private void WorkSetTimerValues() {
        int time;
        time = Integer.parseInt(settings.getString("work_duration", "1"));
        // assigning values after converting to milliseconds
        timeCountInMilliSeconds = time * 60 * 1000;
    }

    /**
     * method to initialize the values for count down timer
     */
    private void BreakSetTimerValues() {
        int time;

        // fetching value from edit text and type cast to integer
        time = Integer.parseInt(settings.getString("break_duration", "1"));

        // assigning values after converting to milliseconds
        timeCountInMilliSeconds = time * 60 * 1000;
    }

    /**
     * method to start count down timer
     */
    private void startCountDownTimer() {

        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));

            }

            @Override
            public void onFinish() {
                //The count check end of the task
                breakCount++;
                textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
                // call to initialize the progress bar values
                setProgressBarValues();
                // hiding the work icon
                imageViewWork.setVisibility(View.GONE);
                // hiding the break icon
                imageViewBreak.setVisibility(View.GONE);
                // changing stop icon to start icon
                imageViewStartStop.setImageResource(R.mipmap.icon_start);
                // changing the timer status to stopped
                timerStatus = TimerStatus.STOPPED;
                //Vibration
                vibration = settings.getBoolean("vibration", true);

                if (vibration) vibrator.vibrate(1000);

                //checking work and break times
                checkBreakOrWork();
            }
        }.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            scheduleNotification(getNotification("Time is over!!"), timeCountInMilliSeconds);
        }
    }

    /**
     * method to set the alarm
     *
     * @param notification
     * @param delay
     */
    private void scheduleNotification(Notification notification, long delay) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = System.currentTimeMillis() + delay;

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent);

    }


    /**
     * method to creating notification
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification(String content) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification builder = new Notification.Builder(this, "default")
                .setContentTitle("Pomodoro")
                .setContentText(content)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setVibrate(new long[]{500, 500, 500, 500, 500})
                .setSmallIcon(R.mipmap.icontomato).getNotification();
        return builder;
    }


    /**
     * method check break and work duration
     */
    public void checkBreakOrWork() {
        if (breakCount != 8) {
            if (breakOrWork) {
                breakAlert();
                // System.out.println("************************ break");
            } else if (!breakOrWork) {
                workAlert();
                //  System.out.println("************************ work");
            } else {
                Toast.makeText(getApplicationContext(), "Finish", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Pomodoro is over", Toast.LENGTH_LONG).show();
            reset();

        }

    }

    /**
     * method to show alert take a break
     */
    public void breakAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setMessage("Good job! Would you like  to take a break");

        alertDialogBuilder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                workstartStop();
            }
        });

        alertDialogBuilder.setNegativeButton("Take a break", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                breakStartStop();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * method to show alert take a break
     */
    public void workAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setMessage("The break is over! Now working time");
        alertDialogBuilder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                workstartStop();
            }
        });

        alertDialogBuilder.setNegativeButton("Finish", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reset();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * method to start and stop count down timer break
     */
    private void breakStartStop() {
        breakOrWork = false;
        if (timerStatus == TimerStatus.STOPPED) {

            // call to initialize the timer values
            BreakSetTimerValues();
            // call to initialize the progress bar values
            setProgressBarValues();
            // showing the break icon
            imageViewBreak.setVisibility(View.VISIBLE);
            // showing the reset icon
            imageViewReset.setVisibility(View.VISIBLE);
            // changing play icon to stop icon
            imageViewStartStop.setImageResource(R.mipmap.icon_pause);
            // making edit text not editable
            // changing the timer status to started
            timerStatus = TimerStatus.STARTED;
            // call to start the count down timer
            startCountDownTimer();

        } else {
            breakCount = 0;
            // changing stop icon to start icon
            imageViewStartStop.setImageResource(R.mipmap.icon_start);
            // changing the timer status to stopped
            timerStatus = TimerStatus.STOPPED;
            stopCountDownTimer();

        }

    }

    /**
     * method to stop count down timer
     */
    private void stopCountDownTimer() {
        countDownTimer.cancel();
    }

    /**
     * method to set circular progress bar values
     */
    private void setProgressBarValues() {

        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);
    }


    /**
     * method to convert millisecond to time format
     *
     * @param milliSeconds
     * @return HH:mm:ss time formatted string
     */
    private String hmsTimeFormatter(long milliSeconds) {

        String hms = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hms;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
       loadInformation();
        return true;
    }

    /**
     * method to get profile pic. and name
     */
    public void loadInformation(){
        imageViewPic = findViewById(R.id.imageViewPic);
        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);


        String name = Login.fullName;
        String image = Login.imageUrl;
        String email = Login.email;
        Log.i("image", image);
        Log.i("email", email);
        Facebook.showImageOne(imageViewPic, image);

        if (name != null && email != null) {
            textViewName.setText(name);
            textViewEmail.setText(email);
        } else {
            Toast.makeText(getApplicationContext(), "Data Not Found", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            Intent settingsIntent = new Intent(getApplicationContext(), Settings.class);
            startActivity(settingsIntent);
            return true;
        }
        if (item.getItemId() == R.id.question) {
            Intent questionIntent = new Intent(getApplicationContext(), Questions.class);
            startActivity(questionIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
