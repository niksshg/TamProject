package com.masterproject.fittam;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.masterproject.fittam.QuestHelper.Quest;
import com.masterproject.fittam.QuestHelper.QuestDao;
import com.masterproject.fittam.QuestHelper.QuestDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The functioning of the tests is not checked. Required lang lvl for robolectric - 9, current 8
 * https://github.com/google-developer-training/android-fundamentals-apps-v2/blob/master/RoomWordsSample/app/src/androidTest/java/com/example/android/roomwordssample/WordDaoTest.java
 */

@RunWith(AndroidJUnit4.class)
public class QuestDaoT {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private QuestDao questDao;
    private QuestDatabase localQb;

    @Before
    public void createQuestDb() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        localQb = Room.inMemoryDatabaseBuilder(context, QuestDatabase.class)
                .allowMainThreadQueries()
                .build();
        questDao = localQb.questDao();

    }

    @After
    public void closeQb() throws IOException {
        localQb.close();
    }

    @Test
    public void addAndGetQuest() throws Exception {
        Quest q = new Quest(3, "Test", "Test Quest", "Add and get it", 1, 5, "hap", 22);
        questDao.insert(q);
        List<Quest> testQuests = LiveDataHelper.getValue(questDao.getAllQuest());
        assertEquals(testQuests.get(0).getQuestID(),q.getQuestID());
    }

    @Test
    public void getQuests() throws Exception{
        Quest q = new Quest(4, "Test", "Test Quest", "Add and get it", 1, 5, "hap", 22);
        questDao.insert(q);
        Quest q2 = new Quest(5, "Test", "Test Quest", "Add and get it", 1, 5, "hap", 22);
        questDao.insert(q2);
        List<Quest> testQuest = LiveDataHelper.getValue(questDao.getAllQuest());
        assertEquals(testQuest.get(0).getQuestID(),q.getQuestID());
        assertEquals(testQuest.get(1).getQuestID(),q2.getQuestID());
    }

    @Test
    public void deleteQuests() throws Exception{
        Quest q = new Quest(4, "Test", "Test Quest", "Add and get it", 1, 5, "hap", 22);
        questDao.insert(q);
        Quest q2 = new Quest(5, "Test", "Test Quest", "Add and get it", 1, 5, "hap", 22);
        questDao.insert(q2);
        questDao.deleteAll();
        List<Quest> testQuest = LiveDataHelper.getValue(questDao.getAllQuest());
        assertTrue(testQuest.isEmpty());

    }
}