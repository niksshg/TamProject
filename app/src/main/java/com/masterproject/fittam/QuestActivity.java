package com.masterproject.fittam;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.masterproject.fittam.QuestHelper.Quest;
import com.masterproject.fittam.QuestHelper.QuestViewModel;
import com.masterproject.fittam.utilities.QuestComplitionChecker;
import com.masterproject.fittam.utilities.SharedPrefUtils;
import com.masterproject.fittam.utilities.TamWorkerUtil;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * QuestActivity
 * <p>
 * The implementation of the quests bloc is the closest to the android recommended MVVM (model-view-viewmodel) pattern.
 * Model - quest db
 * Activity - view/ui controller
 * ViewModel - notified by activity about user interaction.
 * <p>
 * This class also responsible for issuing the quests along with workmanager, so it is more view-controller, while workmanager is also can be perceived as controller.
 * Potential enhancement - move quest creation logic to tamagotchi. Meanwhile, tamagotchi model should be entity in db.
 * <p>
 * This class responsible for initiating views, schedules quest creation via workmanager and schedules alarm to evaluate quests and then clears quests.
 * <p>
 * There is lack of query which can update quest progress. Quest progress is updated when the quests is reissued and replaced.
 * <p>
 * Sources:
 * https://developer.android.com/jetpack/docs/guide
 * https://codelabs.developers.google.com/codelabs/android-training-livedata-viewmodel/#0
 * https://developer.android.com/topic/libraries/architecture/workmanager
 * https://developer.android.com/training/scheduling/alarms
 * https://stackoverflow.com/questions/4562757/alarmmanager-android-every-day
 */

public class QuestActivity extends AppCompatActivity {

    private BottomNavigationView navView;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;


    private RecyclerView questRecyclerView;
    private QuestViewModel questViewModel;

    // key of the int put in job
    public static final String SCHEDULE_JOB = "JOB";
    // unique job identifier
    public static final String ACTIVE_QUEST_TAG = "ACTIVE_QUEST";
    public static final String STEPS_QUEST_TAG = "STEPS_QUEST";
    // code of the quest to be checked in workmanager
    public static final int ACTIVE_QUEST_CODE = 4;
    public static final int STEPS_QUEST_CODE = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest);

        // set up bottom navigation bar
        navView = findViewById(R.id.nav_view);
        setUpBottomNavBar();
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        // LayoutManager positions quests within recycler view
        // into the linear list. Orientation - vertical, as horizontal flag is not passed.
        questRecyclerView = findViewById(R.id.quest_data_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        questRecyclerView.setLayoutManager(layoutManager);

        // create adapter
        final QuestAdapter questAdapter = new QuestAdapter(this);
        questRecyclerView.setAdapter(questAdapter);

        //Associate viewmodel with activity (ui controller)
        questViewModel = ViewModelProviders.of(this).get(QuestViewModel.class);
        // add livedata observer
        questViewModel.getAllQuest().observe(this, new Observer<List<Quest>>() {
            @Override
            public void onChanged(List<Quest> quests) {
                // populate recycler
                questAdapter.setQuests(quests);
                // create copy of quests for QuestComplitionChecker to evaluate
                QuestComplitionChecker.setQ(quests);


            }
        });

        // shcedule job and alarm
        scheduleStepsQuest();
        scheduleActiveQuest();
        scheduleAlramToEvaluateQuest();

        // create quest or delete if booleans have appropriate value
        createStepsQuest();
        createActiveTimeQuest();
        clearData();


    }

    /**
     * clearData
     * <p>
     * Check status of quest completion boolean and clears list. The quest status is switched by alarm.
     */
    private void clearData() {
        boolean questEvaluation = SharedPrefUtils.getQuestEvaluationCompleted(getApplicationContext());
        if (questEvaluation == true) {
            questViewModel.deletAllQuest();
        }
        SharedPrefUtils.setQuestEvaluationCompleted(getApplicationContext(), false);
    }

    /**
     * scheduleStepsQuest
     * <p>
     * Schedules job to set boolean to true and create/update quest data. Quest is replaced with new one on every request.
     */
    private void scheduleStepsQuest() {
        // put int = 3, to enable workmanager set to do appropriate job
        Data regularStepQ = new Data.Builder()
                .putInt(SCHEDULE_JOB, STEPS_QUEST_CODE)
                .build();

        // do it once in 2 h, add tag to ensure that there is 1 job at the time
        PeriodicWorkRequest checkState = new PeriodicWorkRequest.Builder(TamWorkerUtil.class, 2, TimeUnit.HOURS)
                .addTag(STEPS_QUEST_TAG)
                .setInputData(regularStepQ)
                .build();
        WorkManager workInstance = WorkManager.getInstance();
        workInstance.enqueueUniquePeriodicWork(STEPS_QUEST_TAG, ExistingPeriodicWorkPolicy.KEEP, checkState);


    }

    /**
     * scheduleAciveQuest
     * <p>
     * The idea is that this quest is scheduled when the device is idle, but it's never/rarely scheduled...
     * <p>
     * It can be tested with uncondtional one time request.. then it works, below
     * OneTimeWorkRequest checkState = new OneTimeWorkRequest.Builder(TamWorkerUtil.class)
     * .setInitialDelay(5, TimeUnit.MINUTES)
     * .addTag(ACTIVE_QUEST_TAG)
     * .setInputData(regularActiveQ)
     * .build();
     * WorkManager workInstance = WorkManager.getInstance();
     * workInstance.enqueueUniqueWork(ACTIVE_QUEST_TAG, ExistingWorkPolicy.KEEP, checkState);
     */

    private void scheduleActiveQuest() {
        Data regularActiveQ = new Data.Builder()
                .putInt(SCHEDULE_JOB, ACTIVE_QUEST_CODE)
                .build();

        Constraints deviceIdleConstarint = new Constraints.Builder().setRequiresDeviceIdle(true).build();
        // minimum time for periodic request is 15
        PeriodicWorkRequest checkState = new PeriodicWorkRequest.Builder(TamWorkerUtil.class, 15, TimeUnit.MINUTES)
                .addTag(ACTIVE_QUEST_TAG)
                .setInputData(regularActiveQ)
                .setConstraints(deviceIdleConstarint)
                .build();
        WorkManager workInstance = WorkManager.getInstance();
        workInstance.enqueueUniquePeriodicWork(ACTIVE_QUEST_TAG, ExistingPeriodicWorkPolicy.KEEP, checkState);


    }

    /**
     * Method to set up nav bar and assign item number
     */
    private void setUpBottomNavBar() {
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        Intent toHome = new Intent(QuestActivity.this, MainActivity.class);
                        startActivity(toHome);
                        break;

                    case R.id.navigation_dashboard:
                        Intent toStat = new Intent(QuestActivity.this, HistoryDataActivity.class);
                        startActivity(toStat);
                        break;
                    case R.id.navigation_notifications:
                        break;
                    case R.id.navigation_tamhouse:
                        Intent toTamHouse = new Intent(QuestActivity.this, TamHomePageActivity.class);
                        startActivity(toTamHouse);
                        break;
                }
                return false;
            }
        });

    }

    /**
     * createStepsQuest
     * <p>
     * WorkManager switches status of boolean, this method creates/updates quest once in approximately 2h
     */
    private void createStepsQuest() {
        boolean shouldBeCreated = SharedPrefUtils.getWhetherStepQuestShouldBeCreated(getApplicationContext());
        // check if true
        if (shouldBeCreated == true) {
            String questTitel = "Quest";
            String questName = SharedPrefUtils.getBudName(getApplicationContext()) + ": An Unexpected Journey";
            String questDescription = "Your reluctant Bud is feeling adventurous today! Its " +
                    "time to overcome your limits.";

            int steps = SharedPrefUtils.getStepsCount(getApplicationContext());

            int goal = SharedPrefUtils.getGoal(getApplicationContext());
            // extra steps on top of the goal
            int goalExtra = goal + (int) (goal * 0.20);
            String happinessTitle = "Happiness";
            int rewardForQuest = 5;
            int questID = 1;

            Quest stepsQuest = new Quest(questID, questTitel, questName, questDescription, steps, goalExtra, happinessTitle, rewardForQuest);
            questViewModel.insert(stepsQuest);
        }
        // switch boolean status back
        SharedPrefUtils.setStepQuestShouldBeCreated(getApplicationContext(), false);

    }

    /**
     * createActiveTimeQuest
     * <p>
     * This method should create quest when the device is idle ( never happens).
     */
    private void createActiveTimeQuest() {
        boolean shouldBeCreated = SharedPrefUtils.getWhetherActivityQuestShouldBeCreated(getApplicationContext());
        if (shouldBeCreated == true) {
            String questTitel = "Quest";
            String questName = " I see a mountain at my gates..";
            String questDescription = SharedPrefUtils.getBudName(getApplicationContext()) + " wants to " +
                    " be more active today  ";

            int activeTime = (int) SharedPrefUtils.getMoveMinutes(getApplicationContext());

            // active quest aim
            int goalMin = 60;
            String happinessTitle = "Happiness";
            int rewardForQuest = 8;
            int questID = 2;

            Quest activeQuest = new Quest(questID, questTitel, questName, questDescription, activeTime, goalMin, happinessTitle, rewardForQuest);
            questViewModel.insert(activeQuest);

        }
        SharedPrefUtils.setActivityQuestShouldBeCreated(getApplicationContext(), false);

    }

    /**
     * scheduleAlarmToEvaluateQuest
     * <p>
     * This method schedules alarm to check whether quests were completed (received by QuestCompletionBroadcast)
     */
    private void scheduleAlramToEvaluateQuest() {
        // time of quest
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 50);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 0);
        // send intent
        PendingIntent pendingIntentCheckQuestComplition = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), QuestComplitionChecker.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        // set up interval
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntentCheckQuestComplition);

    }


}
