package com.masterproject.fittam.QuestHelper;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room db - additional layer on top of sql.  Uses dao to query. Singleton - only allows to open 1 instance of db.
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#6
 *
 *
 */

@Database(entities = {Quest.class}, version = 2)
public abstract class QuestDatabase extends RoomDatabase {
    public abstract QuestDao questDao();

    private static volatile QuestDatabase INSTANCE;

    static QuestDatabase getQuestDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (QuestDatabase.class) {
                if (INSTANCE == null) {
                    // Create db
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            QuestDatabase.class, "quest_database")
                            .fallbackToDestructiveMigration() // added as there is no need in migration strategy, if db is updated -> deleter previous version
                            .build();
                }
            }
        }
        return INSTANCE;
    }


}
