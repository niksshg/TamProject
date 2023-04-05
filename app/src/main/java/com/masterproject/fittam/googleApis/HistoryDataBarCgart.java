package com.masterproject.fittam.googleApis;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.masterproject.fittam.R;
import com.masterproject.fittam.utilities.SharedPrefUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This is fragment class that contains bar chart, that receives data from History Api.
 * The chart is built using MPAndroidChar library
 * Source: https://github.com/PhilJay/MPAndroidChart
 * Source Fragments :https://developer.android.com/guide/components/fragments
 * https://code.tutsplus.com/tutorials/add-charts-to-your-android-app-using-mpandroidchart--cms-23335
 */
public class HistoryDataBarCgart extends Fragment {


    // list with weeklyData
    private List<WeeklyData> weeklyDate = HistoryApi.getHistoryDataArray();
    private static final String BAR_TAG = "HistoryBar";


    public HistoryDataBarCgart() {
        // Required empty public constructor
    }

    /**
     * This is factory method to create new instace of the fragment
     *
     * @return A new instance of fragment HistoryDataBarCgart.
     */

    public static HistoryDataBarCgart newInstance() {
        HistoryDataBarCgart fragment = new HistoryDataBarCgart();
        return fragment;
    }

    /**
     * The system calls this when it creates new fragment.
     *
     * @param savedInstanceState
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    /**
     * This method is called when
     * system draws interface for the first time. It inflates from xml layout fragment_history_data_bar_cgart.
     * There is defiantly a problem with my implementation. Probably, variables should be only initiated ( reference made) to the views and
     * the logic should go in onViewCreated or by passing some parameters ..
     * Frequent problems : data duplicates, limitline disappears very often..
     *
     * @param inflater           - inflates the layout takes 3 arguments: resource ID, ViewGroup, boolean which indicates whether layout
     *                           should be attached to the parent.
     * @param container          - indicates parent ViewGroup,
     * @param savedInstanceState - bundle provide information about previous instances of the fragment, when the fragment
     *                           is resumed
     * @return historyView - returns view, root of the layout
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Context historyContext = container.getContext();
        // xml
        int layoutForHistoryBarChart = R.layout.fragment_history_data_bar_cgart;
        // whether shoud be attach
        boolean OnCreatAttachToParent = false;
        // from where to inflate
        inflater = LayoutInflater.from(historyContext);
        // inflate layout for this fragment
        View historyView = inflater.inflate(layoutForHistoryBarChart, container, OnCreatAttachToParent);

        // init barchart
        BarChart historyChartView = historyView.findViewById(R.id.barchart);

        // check whether list is empty and
        // fetch data into 2 arrays
        if (!weeklyDate.isEmpty()) {
            ArrayList<BarEntry> data = new ArrayList();
            ArrayList<String> day = new ArrayList();

            for (int i = 0; i < weeklyDate.size(); i++) {

                // get day of the week
                String s1 = weeklyDate.get(i).getDay();
                String s2 = weeklyDate.get(i).getValue(); // get value

                //requires to be float for chart
                float j = Float.parseFloat(s2);

                // create bar entries
                data.add(new BarEntry(i, j));
                day.add(i, s1);

            }
            //create dataset from entries
            BarDataSet historyBardataSet = new BarDataSet(data, "Steps");
            historyChartView.animateY(5000);  // animate on creation


            BarData historyData = new BarData(historyBardataSet); //init bar form dataset
            historyBardataSet.setColors(ColorTemplate.PASTEL_COLORS); // set up color scheme
            historyChartView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT; // set height to match parent

            int goal = SharedPrefUtils.getGoal(historyContext.getApplicationContext());
            LimitLine line = new LimitLine(goal, "10000"); // red limit line with goal value
            YAxis yAxis = historyChartView.getAxisLeft();
            yAxis.addLimitLine(line); // add line to y axis

            // changes of viability of diff axis
            historyChartView.getXAxis().setEnabled(true);
            historyChartView.getAxisLeft().setEnabled(false);
            historyChartView.getAxisRight().setEnabled(false);

            // hide grid lines in the background
            historyChartView.getXAxis().setDrawGridLines(false);
            historyChartView.getAxisLeft().setDrawGridLines(false);
            historyChartView.getAxisRight().setDrawGridLines(true);

            historyChartView.getLegend().setEnabled(true);

            // no description is needed
            historyChartView.setDescription(null);

            // sets days below bars
            historyChartView.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            historyChartView.setFitBars(true); // position right below the bar

            // add day names to the bar
            historyChartView.getXAxis().setValueFormatter(new IndexAxisValueFormatter(day));


            historyChartView.setData(historyData);
            historyChartView.invalidate();
        }


        return historyView;
    }


}
