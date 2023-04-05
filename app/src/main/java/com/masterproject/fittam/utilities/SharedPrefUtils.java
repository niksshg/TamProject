package com.masterproject.fittam.utilities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.masterproject.fittam.R;

import java.util.Calendar;

/**
 * SharedPref class
 * <p>
 * Contains group of static method to save and get different data. Schedules alarm for steps(not needed)
 * 1.Steps
 * 2. Calories, Distance , Move Minutes
 * 3. Tamagotchi happiness, state, name
 * 4. Booleans : for quests and goal
 * <p>
 * The majority of them should not be synchronised. I kept it if  I want several methods to operations
 * on the same var.
 * <p>
 * Source: https://www.udacity.com/course/new-android-fundamentals--ud851 (the first free course, requires sign in)
 */

public class SharedPrefUtils {

    // default value set for steps, distance ...
    private static final int DEFAULT_INT_VALUE = 0;
    // default value for quest and goal's booleans
    private static final boolean DEFAULT_BOOLEAN_VALUE = false;
    private static final String DEFAULT_NAME = "Your Buddy";
    private static final int DEFAULT_GOAL = 10000;


    /**
     * Steps methods
     */
    synchronized public static void setStepsCount(Context context, int steps) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(context.getString(R.string.saved_step_count), steps);
        editor.apply();
    }

    synchronized static public void updateStepsCount(Context context, int newSteps) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        int steps = SharedPrefUtils.getStepsCount(context);
        newSteps = steps + newSteps;
        SharedPrefUtils.setStepsCount(context, newSteps);

    }

    synchronized static public void setDefaultSteps(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(context.getString(R.string.saved_step_count), DEFAULT_INT_VALUE);
        editor.apply();
    }

    public static int getStepsCount(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        int stepsCount = mPreferences.getInt(context.getString(R.string.saved_step_count), DEFAULT_INT_VALUE);
        return stepsCount;
    }

    // not needed, can be used for testing
    public static void scheduleAlarmtoClearSteps(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 30);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        PendingIntent pendingIntentClearSteps = PendingIntent.getBroadcast(context, 0, new Intent(context, ClearSharedPrefBroadcast.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 60 * 24, pendingIntentClearSteps);
        Log.e("TEST","Clear steps alarm is scheduled");
    }

    /**
     * Calories,Distance, Move Minutes
     */
    synchronized public static void setCalories(Context context, float calories) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(context.getString(R.string.saved_calories_count), (int) calories);
        editor.apply();
    }

    public static float getCaloriesCount(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        int stepsCount = mPreferences.getInt(context.getString(R.string.saved_calories_count), DEFAULT_INT_VALUE);
        return stepsCount;
    }

    synchronized public static void setDistance(Context context, int distance) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(context.getString(R.string.saved_distance_count), distance);
        editor.apply();
    }

    public static float getDistanceCount(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        int stepsCount = mPreferences.getInt(context.getString(R.string.saved_distance_count), DEFAULT_INT_VALUE);
        return stepsCount;
    }

    synchronized public static void setMoveMinutes(Context context, int moveMin) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(context.getString(R.string.saved_active_minutes_count), moveMin);
        editor.apply();
    }

    public static float getMoveMinutes(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        int moveMin = mPreferences.getInt(context.getString(R.string.saved_active_minutes_count), DEFAULT_INT_VALUE);
        return moveMin;
    }

    /**
     * Goal
     */
    synchronized public static void setGoal(Context context, int goal) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(context.getString(R.string.fitness_goal), goal);
        editor.apply();
    }

    public static int getGoal(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        int goal = mPreferences.getInt(context.getString(R.string.fitness_goal), DEFAULT_GOAL);
        return goal;
    }

    /**
     * Tamagotchi methods, state and happiness, name
     */
    synchronized public static void setState(Context context, int state) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(context.getString(R.string.bud_state), state);
        editor.apply();
    }

    public static int getBudState(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        int state = mPreferences.getInt(context.getString(R.string.bud_state), DEFAULT_INT_VALUE);
        return state;
    }

    synchronized public static void setHappiness(Context context, int happiness) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(context.getString(R.string.bud_happiness), happiness);
        editor.apply();
    }

    public static int getHappiness(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        int happiness = mPreferences.getInt(context.getString(R.string.bud_happiness), DEFAULT_INT_VALUE);
        return happiness;
    }

    synchronized public static void setName(Context context, String budName) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(context.getString(R.string.bud_name), budName);
        editor.apply();
    }

    public static String getBudName(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        String budName = mPreferences.getString(context.getString(R.string.bud_name), DEFAULT_NAME);
        return budName;
    }

    /**
     * Booleans for goal, quest creation, quest evaluation
     */

    synchronized public static void setGoalAchievment(Context context, boolean goalAchieved) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(context.getString(R.string.goal_achieved), goalAchieved);
        editor.apply();
    }

    public static boolean getGoalAchievment(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        boolean goalAchieved = mPreferences.getBoolean(context.getString(R.string.goal_achieved), DEFAULT_BOOLEAN_VALUE);
        return goalAchieved;
    }

    synchronized public static void setStepQuestShouldBeCreated(Context context, boolean stepQuest) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(context.getString(R.string.whether_step_quest_should_be_created), stepQuest);
        editor.apply();
    }

    public static boolean getWhetherStepQuestShouldBeCreated(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        boolean stepQuest = mPreferences.getBoolean(context.getString(R.string.whether_step_quest_should_be_created), DEFAULT_BOOLEAN_VALUE);
        return stepQuest;
    }

    synchronized public static void setActivityQuestShouldBeCreated(Context context, boolean activityQuest) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(context.getString(R.string.whether_activity_quest_should_be_created), activityQuest);
        editor.apply();
    }

    public static boolean getWhetherActivityQuestShouldBeCreated(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        boolean activityQuest = mPreferences.getBoolean(context.getString(R.string.whether_activity_quest_should_be_created), DEFAULT_BOOLEAN_VALUE);
        return activityQuest;
    }

    synchronized public static void setQuestEvaluationCompleted(Context context, boolean questEvaluation) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(context.getString(R.string.quest_perfomance_evaluated), questEvaluation);
        editor.apply();
    }

    public static boolean getQuestEvaluationCompleted(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
        boolean questEvaluated = mPreferences.getBoolean(context.getString(R.string.quest_perfomance_evaluated), DEFAULT_BOOLEAN_VALUE);
        return questEvaluated;
    }


}
