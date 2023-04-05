package com.masterproject.fittam.QuestHelper;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Quest DAO interface- specifies queries associted with method calls.
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#4
 * https://codelabs.developers.google.com/codelabs/android-training-room-delete-data/#0
 */

@Dao
public interface QuestDao {

    // Replace strategy if there is conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Quest quest);

    @Query("DELETE FROM quest_table")
    void deleteAll();

    // LiveData - to observe data changes and update
    @Query("SELECT * from quest_table ORDER BY quest_id ASC")
    LiveData<List<Quest>> getAllQuest();



}
