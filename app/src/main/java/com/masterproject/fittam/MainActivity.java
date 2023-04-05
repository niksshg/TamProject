package com.masterproject.fittam;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.masterproject.fittam.googleApis.FitnessOptionsBuilder;
import com.masterproject.fittam.googleApis.HistoryApi;
import com.masterproject.fittam.googleApis.SensorApi;
import com.masterproject.fittam.utilities.SharedPrefUtils;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * MainActivity
 * <p>
 * This is a home page of the application. It get manifest and gooogle fit permission. Then it
 * starts sensor and history api services. Next, it display information about current steps, edittext for goal,
 * calories, distance and move min values. It implements onSharedPreferenceChangeListener interface, which is registered
 * in the onCreate. It is used to listen to changes of values in SharedPreference and update UI.
 * <p>
 * Source: https://developer.android.com/reference/android/content/SharedPreferences.OnSharedPreferenceChangeListener
 */


public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    //views
    private TextView stepsTextView;
    private TextView caloriesTextView;
    private TextView distanceTextView;
    private TextView moveMinutesTextView;
    private TextView dateView;
    private EditText goalEditTextView;
    private CircularProgressBar circularProgressBar;


    private BottomNavigationView navView;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;

    // code used for auth in google
    private final int GOOGLE_FIT_ACCESS_CODE_REQUEST = System.identityHashCode(this) & 0xFFFF;
    private final String TAG = " MainActivity";

    private int stepsCount;


    private final int DEFAULT_GOAL = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // views references
        navView = findViewById(R.id.nav_view);
        stepsTextView = findViewById(R.id.steps_text_view);
        caloriesTextView = findViewById(R.id.calories_view);
        distanceTextView = findViewById(R.id.distance_view);
        moveMinutesTextView = findViewById(R.id.move_minutes_view);
        dateView = findViewById(R.id.dateView);

        goalEditTextView = findViewById(R.id.goal_view);
        goalEditTextView.setFocusable(true);

        // set up progress bar
        circularProgressBar = findViewById(R.id.circularProgressBar);
        setUpProgressBar();

        // set up nav bar
        setUpBottomNavBar();
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        // edittext listener. It allows to change goal and save in sharedpref.
        goalEditTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                // set default 10000 goal if empty
                if (s == null || s.isEmpty()) {
                    SharedPrefUtils.setGoal(getApplicationContext(), DEFAULT_GOAL);

                    // goal cannot be less than a thousand
                } else if (editable.length() >= 4) {
                    SharedPrefUtils.setGoal(getApplicationContext(), Integer.parseInt(editable.toString()));
                    Log.e("MainActivity", String.valueOf(SharedPrefUtils.getGoal(getApplicationContext())));

                } else if (editable.length() < 3) {

                }


            }

        });


        // Update methods for the ui
        updateGoal();
        updateStepsCount();
        updateCalories();
        updateDistance();
        updateActiveMinuntes();

        // getPermissionFromManifest();
        getFitnessDataPermission();

        // register listener
        String sharedPreferenceFile = getString(R.string.step_preferences_file_key);
        SharedPreferences mPreferences = getApplicationContext().getSharedPreferences(sharedPreferenceFile, MODE_PRIVATE);
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        // get date to display
        getDate();

        // check if access_fine_location permission is granted, needed for distance and steps
        getPermissionFromManifestLocation();
        // permission since api29
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            getPermissionFromManifestActivityRec();
        }

        // for testing
       // SharedPrefUtils.scheduleAlarmtoClearSteps(getApplicationContext());


    }

    /**
     * Method set up progress bar
     * <p>
     * Source: https://github.com/lopspower/CircularProgressBar
     */

    private void setUpProgressBar() {
        long duration = 1000;
        int goal = SharedPrefUtils.getGoal(getApplicationContext());
        //set progress and max=goal
        circularProgressBar.setProgressWithAnimation(SharedPrefUtils.getStepsCount(getApplicationContext()), duration);
        circularProgressBar.setProgressMax(goal);

        // Set ProgressBar Color
        circularProgressBar.setProgressBarColor(Color.RED);
        // gradient
        circularProgressBar.setProgressBarColorStart(Color.YELLOW);
        circularProgressBar.setProgressBarColorEnd(Color.GRAY);
        circularProgressBar.setProgressBarColorDirection(CircularProgressBar.GradientDirection.TOP_TO_BOTTOM);
        //Set Width
        circularProgressBar.setProgressBarWidth(15f); // in DP
        circularProgressBar.setBackgroundProgressBarWidth(25f); // in DP
        circularProgressBar.setRoundBorder(true);
        circularProgressBar.setStartAngle(180f);
        circularProgressBar.setProgressDirection(CircularProgressBar.ProgressDirection.TO_RIGHT);

        // Set background ProgressBar Color
        circularProgressBar.setBackgroundProgressBarColor(Color.GRAY);
        circularProgressBar.setBackgroundProgressBarColorStart(Color.WHITE);
        circularProgressBar.setBackgroundProgressBarColorEnd(Color.LTGRAY);
        circularProgressBar.setBackgroundProgressBarColorDirection(CircularProgressBar.GradientDirection.TOP_TO_BOTTOM);
    }

    /**
     * Standard method for nav bar
     */
    private void setUpBottomNavBar() {
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        break;
                    case R.id.navigation_dashboard:
                        Intent toStat = new Intent(MainActivity.this, HistoryDataActivity.class);
                        startActivity(toStat);
                        break;
                    case R.id.navigation_notifications:
                        Intent toQ = new Intent(MainActivity.this, QuestActivity.class);
                        startActivity(toQ);
                        break;
                    case R.id.navigation_tamhouse:
                        Intent toTamHouse = new Intent(MainActivity.this, TamHomePageActivity.class);
                        startActivity(toTamHouse);
                        break;
                }
                return false;
            }
        });

    }

    /**
     * Series of methods to update ui
     */
    public void updateGoal() {
        int goal = SharedPrefUtils.getGoal(getApplicationContext());
        goalEditTextView.setText(String.valueOf(goal));
    }

    public void updateStepsCount() {
        long duration = 10000;
        stepsCount = SharedPrefUtils.getStepsCount(getApplicationContext());
        circularProgressBar.setProgressWithAnimation(stepsCount, duration);
        stepsTextView.setText(String.valueOf(stepsCount) + " /");

    }

    public void updateCalories() {
        int calories = (int) SharedPrefUtils.getCaloriesCount(getApplicationContext());
        caloriesTextView.setText(calories + " cal");

    }

    public void updateDistance() {
        int distance = (int) SharedPrefUtils.getDistanceCount(getApplicationContext());
        distanceTextView.setText(String.valueOf(distance) + " m");

    }

    public void updateActiveMinuntes() {
        int actMin = (int) SharedPrefUtils.getMoveMinutes(getApplicationContext());
        moveMinutesTextView.setText(String.valueOf(actMin) + " min");
    }


    /**
     * Interface method, updates ui/
     * <p>
     * Takes key string from strings.xml, checks whether it is equal to the key changes and applies
     * relevant method to update UI.
     */

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getString(R.string.saved_step_count).equals(key)) {
            updateStepsCount();

        } else if (getString(R.string.saved_calories_count).equals(key)) {
            updateCalories();

        } else if (getString(R.string.saved_distance_count).equals(key)) {
            updateDistance();

        } else if (getString(R.string.saved_active_minutes_count).equals(key)) {
            updateActiveMinuntes();

        } else if (getString(R.string.fitness_goal).equals(key)) {
            updateGoal();
        }
    }

    /**
     * Checks whether data access permissions were granted. If yes starts services.
     * https://developers.google.com/fit/android/get-started
     */
    public void getFitnessDataPermission() {
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), FitnessOptionsBuilder.getFitnessOptions())) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_ACCESS_CODE_REQUEST,
                    GoogleSignIn.getLastSignedInAccount(this),
                    FitnessOptionsBuilder.getFitnessOptions()); // get data needed from builder class
        } else {
            // if yes start services
            startService();
            // alarm to clear steps - not used, view is reset via HistoryApi.readDailyTotal
           // SharedPrefUtils.scheduleAlarmtoClearSteps(getApplicationContext());


        }

    }


    /**
     * get access_fine_location permission for steps and distance (manifest)
     * https://developer.android.com/training/permissions/requesting
     */
    public void getPermissionFromManifestLocation() {
        final int MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 03;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // provide explanation
            } else {
                //  request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
            }
        } else {
            // Permission has been already given
            Log.e(TAG, "Permission was granted already");
        }
    }

    /**
     * get access_fine_location permission for steps and distance (manifest). For api 29+
     * https://developer.android.com/training/permissions/requesting
     */

    public void getPermissionFromManifestActivityRec() {
        final int MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 02;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // If permission was not granted, explanation window method can be here
            } else {
                // Request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
            }
        } else {
            // Permission has been already given
            Log.e(TAG, "Permission was granted already");


        }
    }


    /**
     * Method to start services after fitness permission were granted
     */
    public void startService() {
        Intent sensorIntent = new Intent(this, SensorApi.class);
        startService(sensorIntent);


        Intent historyIntent = new Intent(this, HistoryApi.class);
        startService(historyIntent);


    }

    /**
     * Handles user response if there is need for auth
     * https://developers.google.com/fit/android/get-started
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_ACCESS_CODE_REQUEST) {
                Intent intent = new Intent(this, SensorApi.class);
                startService(intent);

                //History
                Intent historyIntent = new Intent(this, HistoryApi.class);
                startService(historyIntent);


            }
        }
    }

    /**
     * Just in case, update goal if state is onresume
     */
    @Override
    public void onResume() {
        super.onResume();
        updateGoal();
    }

    /**
     * Displays today date above progressbar
     * https://stackoverflow.com/questions/5369682/get-current-time-and-date-on-android
     */

    private void getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE" + " " + "dd/MM/yyyy", Locale.getDefault());
        String today = sdf.format(new Date());
        Log.e("DATE", String.valueOf(today));
        dateView.setText(today);

    }


}
