package com.masterproject.fittam.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

/**
 * GoalCompletionBroadcast
 * <p>
 * Receives intent from TamagotchiHomePageActivity sent by Alarm at 23.55 to check whether
 * the user completed the goal or not. Happiness is changed based on goal completion. Then,
 * Notification is sent
 */

public class GoalCompletionBroadcast extends BroadcastReceiver {

    // default value of goal
    private static final boolean DEFAULT_GOAL_ACHIEVMENT_STATUS = false;

    /**
     * Receives broadcast
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        // check whether the goal is achieved
        whetherGoalWasAchieved(context.getApplicationContext());
        // change happiness
        correctHappinesIfGoalwasAchieved(context.getApplicationContext());

        // send notification
        NotificationUtils.setGoalCompletionNotification(context.getApplicationContext());

    }

    /**
     * Method that changes happiness
     *
     * @param context
     */
    private static void correctHappinesIfGoalwasAchieved(Context context) {
        // get happiness from  shared pref
        int previousHappines = SharedPrefUtils.getHappiness(context.getApplicationContext());
        int newHappines = 0;

        // get boolean whether goal was achieved
        boolean getGoalAchievmentStatus = SharedPrefUtils.getGoalAchievment(context.getApplicationContext());


        // if yes +10, no -4. Set value
        if (getGoalAchievmentStatus == true) {

            newHappines = previousHappines + 10;
            SharedPrefUtils.setHappiness(context.getApplicationContext(), newHappines);
        } else {
            newHappines = previousHappines - 4;
            SharedPrefUtils.setHappiness(context.getApplicationContext(), newHappines);
        }

    }

    /**
     * Evaluate whether goal was achieved
     *
     * @param context
     */
    private static void whetherGoalWasAchieved(@NotNull Context context) {
        // get goal and steps from shared pref
        int goal = SharedPrefUtils.getGoal(context.getApplicationContext());
        int steps = SharedPrefUtils.getStepsCount(context.getApplicationContext());
        boolean goalAchieved;

        // check whether was achieved, set default value
        if (steps >= goal) {
            goalAchieved = true;
            SharedPrefUtils.setGoalAchievment(context.getApplicationContext(), goalAchieved);

        } else {
            goalAchieved = DEFAULT_GOAL_ACHIEVMENT_STATUS;
            SharedPrefUtils.setGoalAchievment(context.getApplicationContext(), goalAchieved);


        }
        Log.e("GOAL STATUS", String.valueOf(goalAchieved));

    }


}
