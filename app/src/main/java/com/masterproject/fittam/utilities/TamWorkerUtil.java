package com.masterproject.fittam.utilities;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.masterproject.fittam.QuestActivity;
import com.masterproject.fittam.TamHomePageActivity;

/**
 * TamWorkerUtil
 * <p>
 * WorkManager - schedules job depending on input data int. This class is used by
 * QuestActivity and TamHomePage to schedule state changes, notifications and need to create quests.
 * Source: https://developer.android.com/topic/libraries/architecture/workmanager
 */

public class TamWorkerUtil extends Worker {
    Context context;
    private static final String TAG = "Worker";

    public TamWorkerUtil(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;

    }

    @NonNull
    @Override
    public Result doWork() {

        int whatWork = getInputData().getInt(TamHomePageActivity.SCHEDULE_JOB, 0);
        switch (whatWork) {
            // case 1 - change state, sent by TamHomePage
            case TamHomePageActivity.REGULAR_JOB_NOTIFICATION_AND_STATE:
                recalculateState();
                Log.e(TAG, "Started");
                break;
            // case 2 - device idle notification, sent by TamHomePage
            case TamHomePageActivity.IDLE_JOB:
                NotificationUtils.deviceIdleNotificaiont(context);
                Log.e(TAG, "Started");
                //placed also here for the faster refresh rate
                createStepsQuest();
                createActiveQuest();
                break;
            // case 3 - steps quest should be created, regular job once in 2-3h.
            case QuestActivity.STEPS_QUEST_CODE:
                createStepsQuest();
                Log.e(TAG, "Work is Scheduled");
                break;
            // case 4 - activity quest, This one is almost never scheduled ...
            case QuestActivity.ACTIVE_QUEST_CODE:
                createActiveQuest();
                Log.e(TAG, "Work is sheduled");
                break;

        }

        return Result.success();
    }

    /**
     * Main method, initialises the others. Checks state, recalculates happiness. Sends notification
     */

    private void recalculateState() {
        // steps and goal
        int steps = SharedPrefUtils.getStepsCount(context);

        int goal = SharedPrefUtils.getGoal(context);
        //state
        int previousState = SharedPrefUtils.getBudState(context);
        int state = 0;
        //happiness
        int previousHappiness = SharedPrefUtils.getHappiness(context);
        int happiness = 0;
        // find out what state it is relative to goal and previous state
        calculateState(state, steps, goal);

        // get new state after reculculation
        int newState = SharedPrefUtils.getBudState(context);
        // calculate happiness
        calculateHappiness(previousState, newState, happiness, previousHappiness);

        // send regular notification
        NotificationUtils.hourlyStateNotification(context);

        Log.v(TAG, "Workdone" + String.valueOf(previousHappiness));
        Log.v(TAG, String.valueOf(steps) + "/" + String.valueOf(previousState) + "/" + String.valueOf(previousHappiness));

    }

    /**
     * Chane happiness relative to state changes
     *
     * @param previousState     - state before evaluation
     * @param state             - state after state check
     * @param happiness         - new happiness
     * @param previousHappiness - previous
     * @return
     */
    private int calculateHappiness(int previousState, int state, int happiness, int previousHappiness) {
        // if previous state the same as current and app was not just downloaded (happiness !=0)/was not changed at all.
        // Do not add extra happiness. Else add happiness depending on state progress
        if (previousState == state && previousHappiness != 0) {
            happiness = previousHappiness;
            SharedPrefUtils.setHappiness(context, happiness);
        } else if (state == 1) {
            happiness = previousHappiness + 1;
            SharedPrefUtils.setHappiness(context, happiness);
        } else if (state == 2) {
            happiness = previousHappiness + 2;
            SharedPrefUtils.setHappiness(context, happiness);
            Log.e("Activeremindes", "Is scheduled");

        } else if (state == 3) {
            happiness = previousHappiness + 3;
            SharedPrefUtils.setHappiness(context, happiness);
        } else if (state == 4) {
            happiness = previousHappiness + 4;
            SharedPrefUtils.setHappiness(context, happiness);
            Log.e("ACTIVEq", "Active q is scheduled");
        } else if (state == 5) {
            happiness = previousHappiness + 5;
            SharedPrefUtils.setHappiness(context, happiness);

        }
        Log.e("HapM", String.valueOf(happiness));
        return happiness;
    }

    /**
     * Change state relative to goal
     *
     * @param state - tamagotchi state
     * @param steps - steps
     * @param goal  - goal
     * @return
     */
    private int calculateState(int state, int steps, int goal) {
        if (steps <= goal / 5) {
            state = 1;
            SharedPrefUtils.setState(context, state);


        } else if (steps > goal / 5 && steps <= goal / 2) {
            state = 2;
            SharedPrefUtils.setState(context, state);

        } else if (steps > goal / 2 && steps <= goal / 1.33) {
            state = 3;
            SharedPrefUtils.setState(context, state);

        } else if (steps > goal / 1.33 && steps < goal) {
            state = 4;
            SharedPrefUtils.setState(context, state);

        } else if (steps >= goal) {
            state = 5;
            SharedPrefUtils.setState(context, state);

        }

        return state;
    }

    /**
     * When work request is sent, set boolean to true so the quest is added to db
     */
    private void createStepsQuest() {
        boolean creatStepQuest = true;
        SharedPrefUtils.setStepQuestShouldBeCreated(context, creatStepQuest);

    }

    private void createActiveQuest() {
        boolean createActiveQuest = true;
        SharedPrefUtils.setActivityQuestShouldBeCreated(context, createActiveQuest);
    }
}
