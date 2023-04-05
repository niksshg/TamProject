package com.masterproject.fittam;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.masterproject.fittam.googleApis.HistoryApi;
import com.masterproject.fittam.googleApis.HistoryDataBarCgart;

/**
 * HistoryDataActivity
 *
 * This activity sets ref to xml layout and displays data
 *   to the users from HistoryAPI. The data is display by inflating
 *  the activity_history_data layout by recycler view and bar chart fragment.
 *
 */

public class HistoryDataActivity extends AppCompatActivity {


    private BottomNavigationView navView;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;
   // private final static String HisTag = "History API Activity";

    // ref to recycler for the data from History API, view
    private RecyclerView histDataList;
    // ref tp adapter for the data from History, connect recycler and adapter
    private HistoryDataAdapter hisDataAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_data);

        //use findViewById to get ref from xml.
        navView = findViewById(R.id.nav_view);

        // sets up recycler+adapter and inserts fragment with chart
        layoutSetup();


        setUpBottomNavBar();
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


    }

    /**
     * LayoutSetup
     *
     * sets up recycler+adaoter and adds fragment
     */

    private void layoutSetup() {

        histDataList = findViewById(R.id.history_data);

        // LayoutManager positions history data recycler view items
        // into the linear list. Orientation - vertical, as horizontal flag is not passed.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        histDataList.setLayoutManager(linearLayoutManager);

        // improves perfomance, the size is always 7
        histDataList.setHasFixedSize(true);

        //  adapter which displays items in the list
        // stablid - attempt to avoid having duplicates
        hisDataAdapter = new HistoryDataAdapter(HistoryApi.getHistoryDataArray(), true, getApplicationContext());
        histDataList.setAdapter(hisDataAdapter);

        // add fragment ot the activity layout
        // layout_for_fragment - the place where fragment will be added
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.layout_for_fragment, HistoryDataBarCgart.newInstance());
        transaction.commit();


    }

    /**
     *
     * Method to set up the bottom navigation bar,
     * each activity has its own bar.
     *
     */

    private void setUpBottomNavBar() {
        Menu menu = navView.getMenu();
        // the number of the item
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home: // if the user clicks home, he navigates to home page
                        Intent toHome = new Intent(HistoryDataActivity.this, MainActivity.class);
                        startActivity(toHome);
                        break;
                    case R.id.navigation_dashboard:
                        break;
                    case R.id.navigation_notifications:
                        Intent toQ = new Intent(HistoryDataActivity.this, QuestActivity.class);
                        startActivity(toQ);
                        break;
                    case R.id.navigation_tamhouse:
                        Intent toTamHouse = new Intent(HistoryDataActivity.this, TamHomePageActivity.class);
                        startActivity(toTamHouse);
                        break;
                }
                return false;
            }
        });

    }



    /**
     * These are overridden lifecycle methods, I used them to make a call to the
     * HistoryAPI service, to keep it alive. There is only 1 instance of the
     * service at the time. Not completely sure if this is correct way to achieve what I wanted
     * (keep service working, recall it if needed as HisData is not save anywhere)
     */


    @Override
    public void onPause() {
        super.onPause();

        Intent historyIntent = new Intent(this, HistoryApi.class);
        startService(historyIntent);


    }



    @Override
    public void onRestart() {
        super.onRestart();

        Intent historyIntent = new Intent(this, HistoryApi.class);
        startService(historyIntent);
    }

    public void onDestroy() {
        super.onDestroy();

    }


}
