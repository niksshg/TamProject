package com.masterproject.fittam.QuestHelper;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

/**
 * Quest entity
 * <p>
 * Source: https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#3
 */

@Entity(tableName = "quest_table")
public class Quest {

    @PrimaryKey
    @NotNull
    @ColumnInfo(name = "quest_id")
    private int questID;


    @NotNull
    @ColumnInfo(name = "quest_name")
    private String questName;

    @NotNull
    @ColumnInfo(name = "quest_description")
    private String questDescription;

    @NotNull
    @ColumnInfo(name = "quest_progress")
    private int progress;

    @NotNull
    @ColumnInfo(name = "quest_aim")
    private int aim;

    @NotNull
    @ColumnInfo(name = "quest_title")
    private String questTitle;

    @NotNull
    @ColumnInfo(name = "happiness_reward")
    private int reward;

    @NotNull
    @ColumnInfo(name = "happiness_title")
    private String happinesTitle;


    public Quest(int questID, String questTitle, String questName, String questDescription, int progress, int aim, String happinesTitle, int reward
    ) {
        this.questTitle = questTitle;
        this.questName = questName;
        this.questDescription = questDescription;
        this.progress = progress;
        this.aim = aim;
        this.happinesTitle = happinesTitle;
        this.reward = reward;
        this.questID = questID;

    }

    public String getQuestTitle() {
        return questTitle;
    }

    public String getQuestName() {
        return questName;
    }

    public String getQuestDescription() {
        return questDescription;
    }

    public int getProgress() {
        return progress;
    }

    public int getAim() {
        return aim;
    }

    public String getHappinesTitle() {
        return happinesTitle;
    }

    public int getReward() {
        return reward;
    }

    public int getQuestID() {
        return questID;
    }



}
