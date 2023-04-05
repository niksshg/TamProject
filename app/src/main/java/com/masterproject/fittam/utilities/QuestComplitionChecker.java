package com.masterproject.fittam.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.masterproject.fittam.QuestActivity;
import com.masterproject.fittam.QuestHelper.Quest;

import java.util.List;

/**
 * QuestCompletionChecker - broadcastreciever
 * <p>
 * Receives intent from alarm in quests. Get copy of quests. Check if there are any quests.
 * ->  check whether they were completed, adjust happiness , sets boolean for quest deletion.
 */

public class QuestComplitionChecker extends BroadcastReceiver {

    // copy of the quest list
    public static List<Quest> qList;

    @Override
    public void onReceive(Context context, Intent intent) {
        checkWhetheToGiveReward(context.getApplicationContext());



    }

    /**
     * Evaluates quests completion status.
     * <p>
     * 1.Check if there are any quests
     * 2. Check what quest it is by fitness buddy name. Only the quest for steps contains it.
     * Should change it to id rather than name.
     * 3. get steps, get aim
     * 4. check completion
     * 5. alter Happines and set
     * 6. set boolean to delete quest
     *
     * @param context
     */
    private void checkWhetheToGiveReward(Context context) {
        boolean result = true;
        // check if there is any
        if (!qList.isEmpty()) {
            // get them
            for (int i = 0; i < qList.size(); i++) {
                // this should be changed to id , check what quest
                String questName = qList.get(i).getQuestName();
                if (questName.contains(SharedPrefUtils.getBudName(context))) {
                    // get steps relative to aim(goal)
                    int steps = qList.get(i).getProgress();
                    int aim = qList.get(i).getAim();
                    // alter happiness
                    if (steps >= aim) {
                        int happines = SharedPrefUtils.getHappiness(context);
                        int reward = qList.get(i).getReward();
                        int newHappines = happines + reward;
                        SharedPrefUtils.setHappiness(context, newHappines);
                    } else {
                        int sadness = 2;
                        int happines = SharedPrefUtils.getHappiness(context);
                        int newHappines = happines - sadness;
                        SharedPrefUtils.setHappiness(context, newHappines);

                    }
                } else {
                    // for active quest
                    int activeTime = qList.get(i).getProgress();
                    int activeGoal = qList.get(i).getAim();
                    if (activeTime >= activeGoal) {
                        int happines = SharedPrefUtils.getHappiness(context);
                        int reward = qList.get(i).getReward();
                        int newHappines = happines + reward;
                        SharedPrefUtils.setHappiness(context, newHappines);
                    } else {
                        int sadness = 2;
                        int happines = SharedPrefUtils.getHappiness(context);
                        int newHappines = happines - sadness;
                        SharedPrefUtils.setHappiness(context, newHappines);

                    }
                }

            }
            // set that job is done if there are any quests
            SharedPrefUtils.setQuestEvaluationCompleted(context, result);
        } else {
            // false otherwise
            SharedPrefUtils.setQuestEvaluationCompleted(context, false);
        }


    }

    /**
     * Method to copy quests
     *
     * @param quests
     */
    public static void setQ(List<Quest> quests) {
        qList = quests;

    }
}
