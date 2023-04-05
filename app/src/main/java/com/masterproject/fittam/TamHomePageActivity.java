package com.masterproject.fittam;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.akexorcist.roundcornerprogressbar.TextRoundCornerProgressBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.masterproject.fittam.FitBud.FitBudModel;
import com.masterproject.fittam.utilities.GoalCompletionBroadcast;
import com.masterproject.fittam.utilities.SharedPrefUtils;
import com.masterproject.fittam.utilities.TamWorkerUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * TamHomePageActivity
 * <p>
 * This view-controller class is responsible for displaying information about the fitness buddy. It schedules jobs to
 * check the user's progress towards his goal, to send notification when the device was idle and schedules alarm to check whether the
 * steps goal was completed.
 * <p>
 * It implements onsharedpreferenceeslistenr to update the ui depending on changes in state, happiness and name values.
 * If state has changed, it displays appropriate to the state gif image of the fitness buddy and message.
 * If happiness has changed, it changes progress in happiness bar and its color if needed.
 */


public class TamHomePageActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    private BottomNavigationView navView;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;
    private TextView increaseInHappines;


    private TextRoundCornerProgressBar happinesBar;
    private TextView happinesTitleView;
    private TextView budSpeaking;
    private EditText budName;
    private GifImageView budLook;


    // model .. not really needed, everything is stored in sharedPref
    private FitBudModel fitBudModel;
    private int state;
    private int happiness;

    // gifs of the fitness buddy, changes depending on state
    private GifDrawable stitchBored;
    private GifDrawable stitchSad;
    private GifDrawable stitchProgress;
    private GifDrawable stitchQuiteHappy;
    private GifDrawable stitchHappy;
    private GifDrawable stitchSleepy;
    private GifDrawable stitch;

    // different progress bar colors, color is changing depending on happiness level
    private int progressColor1;
    private int progressColor2;
    private int progressColor3;
    private int progressColor4;

    private final static String TAG = "BudHome";
    private final String DEFAULT_NAME = "Your Buddy";

    // job values
    public static final int REGULAR_JOB_NOTIFICATION_AND_STATE = 1;
    public static final int IDLE_JOB = 2;
    // key of the int to put in job
    public static final String SCHEDULE_JOB = "JOB";
    // tags for jobs
    private final static String WORK_CHECK_STATE = "Check state work";
    private final static String IDLE_NOTIFICATION = "Idle notification";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tam_home_page);

        //set up progress bar
        navView = findViewById(R.id.nav_view);
        setUpBottomNavBar();
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // ref to views
        increaseInHappines = findViewById(R.id.increaseInHappines);
        happinesTitleView = findViewById(R.id.happy_title_view);
        happinesBar = findViewById(R.id.happiness_level_view);
        budSpeaking = findViewById(R.id.bud_speaking);
        budName = findViewById(R.id.bud_name);

        // edittext listener, allows to enter name of the fitness buddy
        budName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                // if no name, set default
                if (s == null || s.isEmpty()) {
                    SharedPrefUtils.setName(getApplicationContext(), DEFAULT_NAME);


                } else {
                    // otherwise save in sharedpref
                    SharedPrefUtils.setName(getApplicationContext(), editable.toString());
                    Log.e(TAG, String.valueOf(SharedPrefUtils.getBudName(getApplicationContext())));

                }

            }
        });

        // set up look of the fitness bud
        budLook = (GifImageView) findViewById(R.id.fitBud_View);
        // add listener, allows to touch him and get +1 in happiness
        budLook.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // change pic when touched
                        budLook.setImageDrawable(stitchHappy);
                        // show +1 in happiness
                        increaseHappinessIfTouched();
                        // display message
                        Toast.makeText(TamHomePageActivity.this, "Your Buddy like that!",
                                Toast.LENGTH_LONG).show();
                        break;
                    case MotionEvent.ACTION_UP:
                        // display message
                        Toast.makeText(TamHomePageActivity.this, "Your Buddy like that!",
                                Toast.LENGTH_LONG).show();
                        // add +1 happiness
                        increaseHappinessIfTouched();
                        break;


                }

                return false;
            }


        });


        // different images for state
        fitBudModel = new FitBudModel(budLook, happiness, state, this);
        try {
            stitchBored = new GifDrawable(getResources(), R.drawable.stich_bored);
            stitchSad = new GifDrawable(getResources(), R.drawable.stich_sad);
            stitchProgress = new GifDrawable(getResources(), R.drawable.stich_in_progress);
            stitchQuiteHappy = new GifDrawable(getResources(), R.drawable.stitch_fit_and_tired);
            stitchHappy = new GifDrawable(getResources(), R.drawable.stich_happy);
            stitchSleepy = new GifDrawable(getResources(), R.drawable.stich_pixel);
            stitch = new GifDrawable(getResources(), R.drawable.stitc_quite_happy_2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // different colors for happiness
        progressColor1 = Color.RED;
        progressColor2 = Color.YELLOW;
        progressColor3 = Color.GREEN;
        progressColor4 = Color.MAGENTA;

        //max value is always 100
        happinesBar.setMax(100);
        happinesBar.setProgressBackgroundColor(Color.parseColor("#808080"));


        // different update methods for views
        updateProgressBar();
        reflectStateChanges();
        budMessage();
        updateName();

        // schedule job to reassess state, send idle notification, sets alarm to check whether goal was achieved
        scheduleRegularJob();
        schedulIdleJob();
        scheduleAlramToSeeGoalStatus();

        // add listener
        String sharedPreferenceFile = getString(R.string.step_preferences_file_key);
        SharedPreferences mPreferences = getApplicationContext().getSharedPreferences(sharedPreferenceFile, MODE_PRIVATE);
        mPreferences.registerOnSharedPreferenceChangeListener(this);


    }

    /**
     * updateName
     * <p>
     * takes name from sharePref and sets in the nameview
     */
    private void updateName() {
        String name = SharedPrefUtils.getBudName(getApplicationContext());
        budName.setText(name);
    }

    /**
     * update progress bar
     * <p>
     * get happiness. set progress value and text, change color depending on happiness
     */
    private void updateProgressBar() {
        happiness = SharedPrefUtils.getHappiness(getApplicationContext());
        happinesBar.setProgress((float) happiness);
        // show users happiness stat
        happinesBar.setProgressText(String.valueOf(happiness) + "/" + "100");
        // set appropriate color
        if (happiness <= 33) {
            happinesBar.setProgressColor(progressColor1);
        } else if (happiness > 34 && happiness <= 70) {
            happinesBar.setProgressColor(progressColor2);

        } else if (happiness >= 71) {
            happinesBar.setProgressColor(progressColor3);
        } else {
            happinesBar.setProgressColor(progressColor4);
        }

    }

    /**
     * scheduleRegularJob
     * <p>
     * schedules repeating job (every 2h) to check steps and set appropriate state value
     */

    private void scheduleRegularJob() {
        //value = 1
        Data regularJobStateAndNotif = new Data.Builder()
                .putInt(SCHEDULE_JOB, REGULAR_JOB_NOTIFICATION_AND_STATE)
                .build();

        PeriodicWorkRequest checkState = new PeriodicWorkRequest.Builder(TamWorkerUtil.class, 2, TimeUnit.HOURS)
                .addTag(WORK_CHECK_STATE)
                .setInputData(regularJobStateAndNotif)
                .build();
        WorkManager workInstance = WorkManager.getInstance();
        workInstance.enqueueUniquePeriodicWork(WORK_CHECK_STATE, ExistingPeriodicWorkPolicy.KEEP, checkState);


    }

    /**
     * scheduleIdleJob
     * <p>
     * sent notification if the the device was idle for approximately 30 min
     */
    private void schedulIdleJob() {
        Data idleJob = new Data.Builder()
                .putInt(SCHEDULE_JOB, IDLE_JOB)
                .build();
        Constraints deviceIdleConstarint = new Constraints.Builder().setRequiresDeviceIdle(true).build();
        PeriodicWorkRequest idleNotificaition = new PeriodicWorkRequest.Builder(TamWorkerUtil.class, 30, TimeUnit.MINUTES)
                .addTag(IDLE_NOTIFICATION)
                .setConstraints(deviceIdleConstarint)
                .setInputData(idleJob)
                .build();
        WorkManager workInstance = WorkManager.getInstance();
        workInstance.enqueueUniquePeriodicWork(IDLE_NOTIFICATION, ExistingPeriodicWorkPolicy.KEEP, idleNotificaition);
    }

    /**
     * reflectStateChanges
     * <p>
     * set appropriate to state picture
     */
    private void reflectStateChanges() {
        state = SharedPrefUtils.getBudState(getApplicationContext());
        if (state == 1) {
            budLook.setImageDrawable(stitchSad);

        } else if (state == 2) {
            budLook.setImageDrawable(stitchBored);

        } else if (state == 3) {
            budLook.setImageDrawable(stitchProgress);

        } else if (state == 4) {
            budLook.setImageDrawable(stitchQuiteHappy);

        } else if (state == 5) {
            budLook.setImageDrawable(stitch);

        }

    }

    /**
     * budMessage
     * <p>
     * display appropriate message to state
     * <p>
     * Again, such string concatenation prevents addition to static strings.xml -> easier translation
     */
    private void budMessage() {
        state = SharedPrefUtils.getBudState(getApplicationContext());
        if (state == 1) {
            budSpeaking.setText(" It seems " + SharedPrefUtils.getBudName(getApplicationContext()) + " wants to walk more ");


        } else if (state == 2) {
            budSpeaking.setText(" It looks like " + SharedPrefUtils.getBudName(getApplicationContext()) + " is getting healthier ");


        } else if (state == 3) {
            budSpeaking.setText(SharedPrefUtils.getBudName(getApplicationContext()) + " feels like to walk a little bit more ");


        } else if (state == 4) {
            budSpeaking.setText(SharedPrefUtils.getBudName(getApplicationContext()) + " has almost achieved his aims! ");


        } else if (state == 5) {
            budSpeaking.setText(SharedPrefUtils.getBudName(getApplicationContext()) + " looks totaly happy!!! ");


        }

    }

    /**
     * SetUpNavigationBar
     */
    private void setUpBottomNavBar() {
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);

        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        Intent toHome = new Intent(TamHomePageActivity.this, MainActivity.class);
                        startActivity(toHome);
                        break;
                    case R.id.navigation_dashboard:
                        Intent toStat = new Intent(TamHomePageActivity.this, HistoryDataActivity.class);
                        startActivity(toStat);
                        break;
                    case R.id.navigation_notifications:
                        Intent toQ = new Intent(TamHomePageActivity.this, QuestActivity.class);
                        startActivity(toQ);
                        break;
                    case R.id.navigation_tamhouse:
                        break;
                }
                return false;
            }
        });

    }

    /**
     * onSharedPreferenceChange
     * <p>
     * listens to changes of values in sharedpref. Invokes appropriate
     * to the key methods if there were changes
     */

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getString(R.string.bud_state).equals(key)) {
            reflectStateChanges();
            budMessage();

        } else if (getString(R.string.bud_happiness).equals(key)) {
            updateProgressBar();

        } else if (getString(R.string.bud_name).equals(key)) {
            updateName();
        }

    }

    /**
     * scheduleAlarmToSeeGoalStatus
     * <p>
     * set alarm to be received by goal completion broadcast
     * to check whether steps goal was completed and award or deduct happiness
     * https://stackoverflow.com/questions/4562757/alarmmanager-android-every-day
     */
    public void scheduleAlramToSeeGoalStatus() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 55);
        calendar.set(Calendar.SECOND, 33);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 0);
        PendingIntent pendingIntentAlarmForGoal = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), GoalCompletionBroadcast.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntentAlarmForGoal);

    }

    /**
     * increaseHappinessIfTouched
     * <p>
     * method invoked in the budLook listener to show otherwise invisible +1 message when
     * the fitness buddy is touched
     */
    private void increaseHappinessIfTouched() {
        int happines = SharedPrefUtils.getHappiness(getApplicationContext());
        int newHappines = happines + 1;
        SharedPrefUtils.setHappiness(getApplicationContext(), newHappines);
        increaseInHappines.setVisibility(View.VISIBLE);

    }


}
