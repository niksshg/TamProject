package com.masterproject.fittam.QuestHelper;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * QuestViewModel
 * <p>
 * Provides data to the QuestActivity, which in turn displays it, communicates with Repository.
 * The main benefit of using it as it holds data being aware about android
 * lifecycles - adapts to configuration changes.
 * <p>
 * Source
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#8
 *
 * Quests are inserted in QuestActivity when WorkManager initiates quest creation.
 */

public class QuestViewModel extends AndroidViewModel {
    // ref to repository
    private QuestRepository questRepository;
    // cache quests
    private LiveData<List<Quest>> questLiveList;

    // constructor, get quests from repository
    public QuestViewModel(@NonNull Application application) {
        super(application);
        questRepository = new QuestRepository(application);
        questLiveList = questRepository.getAllQuest();
    }

    // getter
    public LiveData<List<Quest>> getAllQuest() {
        return questLiveList;
    }

    //wrapper
    public void insert(Quest quest) {
        questRepository.insert(quest);
    }

    //wrapper
    public void deletAllQuest() {
        questRepository.deleteAllQuests();
    }
}
