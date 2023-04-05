package com.masterproject.fittam;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.masterproject.fittam.QuestHelper.Quest;

import java.util.List;

/**
 * QuestAdapter
 * <p>
 * Adapter displays data questData, data inserted into adapter via ViewModel.
 * <p>
 * Adapter allows to create viewholders for the data in quest list to be displayed in recycler. These data is prepared
 * in nested QuestAdapater.QuestViewHolder.
 * <p>
 * ViewHolder - contains View's info to be displayed.
 * <p>
 * Sources:
 * https://codelabs.developers.google.com/codelabs/android-training-create-recycler-view/index.html?index=..%2F..android-training#3
 * https://codelabs.developers.google.com/codelabs/android-training-livedata-viewmodel/#12
 * https://code.tutsplus.com/tutorials/getting-started-with-recyclerview-and-cardview-on-android--cms-23465
 * https://medium.com/@droidbyme/android-cardview-with-recyclerview-90cfeda6a4d4
 * https://www.udacity.com/course/new-android-fundamentals--ud851 (the first free course, requires sign in, NAME:Developing Android Apps)
 * https://developer.android.com/guide/topics/ui/layout/recyclerview
 * Overall, Recycler  -> refers to adapter, adapter identify number of views, creates viewholders -> populates recycler
 */
public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> {
    // quest list
    private List<Quest> questDetailsArrayList;
    private static final String TAG = "Quest Adapter";
    Context context;
    private static int recViewHolderCount;


    public QuestAdapter(Context context) {
        this.context = context;

    }

    /**
     * onCreateViewHolder
     * <p>
     * RecyclerView is set -> this method is called for each new HistoryDataViewHolder
     * created.
     *
     * @param viewGroup - holders placed within this viewgroup
     * @param viewType  - can be used to provide different layouts if there several item sets
     * @return hisViewHolder - holds views
     */
    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context historyContext = viewGroup.getContext();
        int layoutForHistoryData = R.layout.quests_layout;

        LayoutInflater inflater = LayoutInflater.from(historyContext);
        boolean OnCreatAttachToParent = false;

        View historyView = inflater.inflate(layoutForHistoryData, viewGroup, OnCreatAttachToParent);
        QuestViewHolder hisViewHolder = new QuestViewHolder(historyView);

        // number of view holders created
        recViewHolderCount++;
        Log.d(TAG, recViewHolderCount + "were created");
        return hisViewHolder;


    }

    /**
     * RecyclerView calls this method to display data.
     *
     * @param holder   - holder shoulde be updated to display data items at certain position
     * @param position - item position in the weeklyData list
     */

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        // check whether is null
        if (questDetailsArrayList != null) {
            holder.setQuest(questDetailsArrayList.get(position).getQuestTitle());
            holder.setQuestName(questDetailsArrayList.get(position).getQuestName());

            // check what quest it is and add additional info, steps quest id ==1
            if (questDetailsArrayList.get(position).getQuestID() == 1) {
                holder.setQuestProgressAndAim("Progress: " + questDetailsArrayList.get(position).getProgress() +
                        "/" + String.valueOf(questDetailsArrayList.get(position).getAim()) + " Steps");

            } else {
                holder.setQuestProgressAndAim("Progress: " + questDetailsArrayList.get(position).getProgress() +
                        "/" + String.valueOf(questDetailsArrayList.get(position).getAim()) + " Active Min");

            }


            holder.setQuestDescription(questDetailsArrayList.get(position).getQuestDescription());
            holder.setQuestHappinesReward(questDetailsArrayList.get(position).getHappinesTitle() +
                    ":" + "+" + questDetailsArrayList.get(position).getReward());


        } else {

            holder.setQuest("Sorry, there is no quests yet");
        }
    }

    /**
     * setQuests
     * <p>
     * This method is called in QuestActivity by viewmodel to add quests to the adapter
     *
     * @param quests
     */
    public void setQuests(List<Quest> quests) {
        questDetailsArrayList = quests;
        notifyDataSetChanged();
    }

    /**
     * getItemCount
     * This method is called many times, initially 0, when job has been scheduled to inser quest, returns number of quests
     *
     * @return item count
     */
    @Override
    public int getItemCount() {
        if (questDetailsArrayList != null) {
            return questDetailsArrayList.size();
        } else {
            return 0;
        }

    }

    /**
     * QuestViewHolder
     * <p>
     * This is used to cache the data, set views
     */

    public class QuestViewHolder extends RecyclerView.ViewHolder {
        //CardView - container
        private CardView questCardView;
        //display date and day
        private TextView questTitle;
        // display info
        private TextView questDescription;
        private TextView questProgressAndAim;
        private TextView questName;
        private TextView happinesAndReward;


        public QuestViewHolder(View dataView) {
            super(dataView);
            questCardView = dataView.findViewById(R.id.questData_CardView);
            questTitle = dataView.findViewById(R.id.quest_name_view);
            questName = dataView.findViewById(R.id.quest_description_view);
            questProgressAndAim = dataView.findViewById(R.id.quest_progress);
            questDescription = dataView.findViewById(R.id.quest_aim);

            happinesAndReward = dataView.findViewById(R.id.quest_happiness);

        }

        public void setQuest(String s) {
            questTitle.setText(s);
        }

        public void setQuestName(String s) {
            questName.setText(s);
        }

        public void setQuestProgressAndAim(String s) {
            questProgressAndAim.setText(s);
        }

        public void setQuestDescription(String s) {
            questDescription.setText(s);
        }

        public void setQuestHappinesReward(String s) {
            happinesAndReward.setText(s);
        }


    }

}
