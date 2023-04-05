package com.masterproject.fittam.QuestHelper;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * Repository class that abstarct access to data sources. This along with db can be extended to use for history API...
 * Implementation of repository allows to use several backends.
 * <p>
 * Source: https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#7
 * https://codelabs.developers.google.com/codelabs/android-training-room-delete-data/#0
 */
public class QuestRepository {
    // DAO
    private QuestDao questDao;
    private LiveData<List<Quest>> questLiveList; // quests

    // constructor, operates db and initialises vars
    public QuestRepository(Application application) {
        QuestDatabase questDatabase = QuestDatabase.getQuestDatabase(application);
        questDao = questDatabase.questDao();
        questLiveList = questDao.getAllQuest();
    }

    // wrapper
    LiveData<List<Quest>> getAllQuest() {
        return questLiveList;
    }

    //wrapper
    public void insert(Quest quest) {
        new insertQuestAsyncTask(questDao).execute(quest);
    }

    //wrapper
    public void deleteAllQuests() {
        new deleteQuestAsyncTask(questDao).execute();
    }

    // async task to insert data in the background
    private static class insertQuestAsyncTask extends AsyncTask<Quest, Void, Void> {
        private QuestDao questDaoInTask;

        insertQuestAsyncTask(QuestDao qDao) {
            questDaoInTask = qDao;
        }

        @Override
        protected Void doInBackground(final Quest... quests) {
            questDaoInTask.insert(quests[0]);

            return null;
        }
    }

    // asynctask to clear data, implemented in QuestActivity when broadcast indicates that quests
    // should be cleared
    private static class deleteQuestAsyncTask extends AsyncTask<Void, Void, Void> {
        private QuestDao questDaoInTask;

        deleteQuestAsyncTask(QuestDao qDao) {
            questDaoInTask = qDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            questDaoInTask.deleteAll();
            return null;
        }
    }



}
