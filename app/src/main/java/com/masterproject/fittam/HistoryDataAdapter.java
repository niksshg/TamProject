package com.masterproject.fittam;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.masterproject.fittam.googleApis.WeeklyData;

import java.util.List;

/**
 *
 *HistoryDataAdapter
 *
 * Adapter displays data from HistoryAPI about the users performance
 * during the period of 1 week in the recyclerView.
 *
 * RecyclerView- efficient way to display scrollable list, contains list of item.
 *
 * Adapter allows to create viewholders for the data in weeklyData list to be displayed in recycler. These data is prepared
 * in nested HistoryDataAdapter.HistoryDataViewHolder.
 *
 * ViewHolder - contains View's info to be displayed.
 *
 * Sources:
 *          https://codelabs.developers.google.com/codelabs/android-training-create-recycler-view/index.html?index=..%2F..android-training#3
 *          https://code.tutsplus.com/tutorials/getting-started-with-recyclerview-and-cardview-on-android--cms-23465
 *          https://medium.com/@droidbyme/android-cardview-with-recyclerview-90cfeda6a4d4
 *          https://www.udacity.com/course/new-android-fundamentals--ud851 (the first free course, requires sign in, NAME:Developing Android Apps)
 *          https://developer.android.com/guide/topics/ui/layout/recyclerview
 * Overall, Recycler  -> refers to adapter, adapter identify number of views, creates viewholders
 */

public class HistoryDataAdapter extends RecyclerView.Adapter<HistoryDataAdapter.HistoryDataViewHolder> {

    private static final String TAG = "HistoryAdapater";
    private static int recViewHolderCount;
    private List<WeeklyData> weeklyData;
    Context context;

    /**
     * Constructor for HistoryDataAdapter.
     * Receives data ;ist to be displayed.
     */
    public HistoryDataAdapter(List<WeeklyData> data, boolean stableid, Context context) {
        this.context = context;

        weeklyData = data;
        recViewHolderCount = 0;
        setHasStableIds(stableid);

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
    @Override
    public HistoryDataViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context historyContext = viewGroup.getContext();
        //xml
        int layoutForHistoryData = R.layout.weekly_data;
        // use aforementioned layout to inflate from
        LayoutInflater inflater = LayoutInflater.from(historyContext);
        //not immediately attach to the parent viewgroup
        boolean OnCreatAttachToParent = false;

        View historyView = inflater.inflate(layoutForHistoryData, viewGroup, OnCreatAttachToParent);
        HistoryDataViewHolder hisViewHolder = new HistoryDataViewHolder(historyView);

        // number of history holders created
        recViewHolderCount++;
        Log.i(TAG, recViewHolderCount + "were created");
        return hisViewHolder;
    }

    /**
     * RecyclerView calls this method to display data.
     *
     * @param holder   - holder shoulde be updated to display data items at certain position
     * @param position - item position in the weeklyData list
     */
    @Override
    public void onBindViewHolder(HistoryDataViewHolder holder, int position) {
        // check if there are data, -> set views
        if (weeklyData != null) {
            holder.setDay(weeklyData.get(position).getDay());
            holder.setSteps(weeklyData.get(position).getValue());
            holder.setCalories(weeklyData.get(position).getCalories());
            holder.setDate(weeklyData.get(position).getDate());

            Log.d(TAG, "");
            // message to display if there is no data
        } else {
            holder.setDate("Sorry, there is no data yet");
        }
    }

    /**
     * getItemCount
     * <p>
     * This method helps to set up views, returns the number of items
     * in the list
     *
     * @return number of items
     */
    @Override
    public int getItemCount() {
        if (weeklyData != null) {
            return weeklyData.size();
        } else {
            return 0;
        }
    }

    /**
     * I was trying to modify methods below to control incoming data. ( remove duplicates)
     *
     */

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    /**
     *
     * HistoryDataViewHolder
     *
     * This is used to cache the data
     *
     */

    public class HistoryDataViewHolder extends RecyclerView.ViewHolder {

        // container fo the view
        private CardView histroyCardView;

        private TextView weeklyDayView;

        private TextView weeklyDayStepsView;
        private TextView weeklyDayCaloriesView;
        private TextView weeklyDateView;

        // init views
        public HistoryDataViewHolder(View dataView) {
            super(dataView);
            histroyCardView = dataView.findViewById(R.id.historyData_CardView);
            weeklyDayView = dataView.findViewById(R.id.history_day_view);
            weeklyDayStepsView = dataView.findViewById(R.id.history_steps_view);
            weeklyDayCaloriesView = dataView.findViewById(R.id.history_calories_view);
            weeklyDateView = dataView.findViewById(R.id.history_day_date);

        }

        /**
         * Series of setters for display
         *
         * @param s - data got from weeklyDataList in onBindViewHolder
         */
        public void setDay(String s) {
            weeklyDayView.setText(s);
        }

        public void setSteps(String s) {
            weeklyDayStepsView.setText("Steps:" + s);
        }

        public void setCalories(String s) {
            weeklyDayCaloriesView.setText("cal:" + s);
        }

        public void setDate(String s) {
            weeklyDateView.setText(s);
        }

    }
}
