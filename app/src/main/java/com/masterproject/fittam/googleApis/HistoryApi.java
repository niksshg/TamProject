package com.masterproject.fittam.googleApis;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.masterproject.fittam.utilities.SharedPrefUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

public class HistoryApi extends Service {

    /**
     * This class contains Google Fit History Api methods. Firstly, it reads data in readWeeklyData() method (DataTypes: Steps and Calories), data is added to 4 arrays, then it
     * addedd as weeklyData object to the list. The problem here if some type of data is absent in these arrays -> it caanot be added to the weeklyData array (Exception: IndexOutOfBound). Next, data is used by
     * HistoryDataAdapter and HistoryDataBarChart for statistics display.
     * <p>
     * Finally, there are readDaily total methods, which receives current data for the display in MainActivity.  The data is saved in SharedPrefrences. There is no need to clean the data
     * via alarms and broadcast as those method returns 0 automatically on the next day.
     * <p>
     * The rational for using additional steps method to receive data is that when the user downloads the app for the first time his data is inherited by this application.
     * <p>
     * Sources: https://github.com/googlesamples/android-fit/tree/master/BasicHistoryApi
     * https://developers.google.com/fit/faq
     * https://developers.google.com/fit/android/history
     * <p>
     * Drawback: there are restrictions since api 26+ on services work when the app
     * is not in the foreground.
     * Source: https://developer.android.com/about/versions/oreo/background.html
     */
    // logging tag
    private final static String HisTag = "History API";

    // list with weeklyData objects, passed to adapter and chart for display
    private static List<WeeklyData> historyDataArray = new ArrayList<>();

    // lists where the data added first -> to weeklydata
    // calories
    ArrayList<String> calories = new ArrayList<>();
    // steps
    ArrayList<String> step = new ArrayList<>();
    // date today (Mon, Tu, Wed)
    ArrayList<String> date = new ArrayList<>();
    // data in the format dd/mm/yyyy
    ArrayList<String> datOfweek = new ArrayList<>();

    // custom data source for steps like in the google fit/sensor api
    // Source:https://developers.google.com/fit/faq

    private static DataSource ESTIMATED_STEPS_LIKE_GOOGLE_SOURCE = new DataSource.Builder()
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .setAppPackageName("com.google.android.gms")
            .build();


    /**
     * The system invokes this method when another component calls it via
     * startService(). After the call, the service can run indefinitely.
     * I am stopping the service if the weeklyData array is not empty.
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // the method which stops service if weekly data is not empty
        stopService();

        // if the service is killed -> restart
        return Service.START_STICKY;
    }

    /**
     * This method checks whether the weeklyData array is empty and if it
     * is not empty, it stops the service. Otherwise, start AsyncTask
     * <p>
     * Source: https://developer.android.com/guide/components/services
     */

    private void stopService() {
        if (!historyDataArray.isEmpty()) {
            stopSelf();
        } else {

            new readHistoryDataAsyncTask().execute();


        }

    }

    /**
     * AsyncTask - runs history api tasks on the background thread
     * <p>
     * Source: https://developer.android.com/reference/android/os/AsyncTask
     */

    private class readHistoryDataAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // main request to get data for the list and bar chart
            readWeeklyData();
            fetchData(); // method that put data from 4 arraylists to the weeklyData list and reverses it
            readCalories(); // readDaily method for calories
            readDistance(); // readDaily method for distance
            readStepCountDelta1(); // readDaily method for steps
            readActive(); // readDaily method for active minutes/move minutes
            return null;
        }
    }


    /**
     *
     *
     * API - start of the API methods.
     *
     *
     */


    /**
     * readWeeklyData() method reads data to be display in recycler and barchart.
     * More importantly, it create subscription to the datatypes I would like to recieve.
     * <p>
     *  Probably, the accessing google account in such a way as below via( GoogleSignIn.getLastSignedInAccoutn)
     *  was not the best option as it sometimes may return null while switching activities. It can be visible by
     *  putting the breakpoint on the first line of the method below.
     *  It is likely that the better option is to pass instance of the google api client.
     * Soource : https://github.com/googlesamples/android-fit/blob/master/BasicHistoryApi/app/src/main/java/com/google/android/gms/fit/samples/basichistoryapi/MainActivity.java
     * https://developers.google.com/fit/android/history
     */
    private void readWeeklyData() {
        // calls History API to get the data with query = getFitnessHistoryData()
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(getFitnessHistoryData()) // query
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                // if success - start to read data to arrays
                                fetchData(dataReadResponse);


                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(HisTag, "There is no data available");
                            }
                        });


    }

    /**
     * This is used to query the data. Creates DataReadRequest instance.
     * Specifies multiple data queries I want to get and
     * specifies that the data should be aggregated by datapoints rather
     * than time-series (  DataType.AGGREGATE .. ).
     * <p>
     * So each datapoint represents the number of calories/steps per day.
     *
     * @return data read request
     */

    private DataReadRequest getFitnessHistoryData() {
        // get data for the last seven daya
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        Log.i(HisTag, "Range Start: " + dateFormat.format(startTime));
        Log.i(HisTag, "Range End: " + dateFormat.format(endTime));

        // add datatypes I want to recieve. Pack by bucket = day
        DataReadRequest readRequest = new DataReadRequest.Builder()
                //steps by day
                .aggregate(ESTIMATED_STEPS_LIKE_GOOGLE_SOURCE, DataType.AGGREGATE_STEP_COUNT_DELTA) //DataType.TYPE_STEP_COUNT_DELTA
                //calories by day
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                // similar to Group By in SQL
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        return readRequest;
    }

    /**
     * This method gets datasets from buckets
     *
     * @param dataReadResult - result of HistoryApi.readData method. contains 1 data set
     *                       for each data source requested. Since the data specified is aggreagted,
     *                       it will return buckets containing several datasets.
     *                       Sources: https://developers.google.com/android/reference/com/google/android/gms/fitness/result/DataReadResponse
     *                       https://developers.google.com/fit/android/history
     *                       https://github.com/googlesamples/android-fit/blob/master/BasicHistoryApi/app/src/main/java/com/google/android/gms/fit/samples/basichistoryapi/MainActivity.java
     */
    private void fetchData(DataReadResponse dataReadResult) {

        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(HisTag, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    retrieveData(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(HisTag, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                retrieveData(dataSet);
            }
        }

    }

    /**
     * Datasets are passed to this method, then it obtains instances of data points
     * Sources: https://developers.google.com/fit/android/history
     * https://github.com/googlesamples/android-fit/blob/master/BasicHistoryApi/app/src/main/java/com/google/android/gms/fit/samples/basichistoryapi/MainActivity.java
     * https://code.tutsplus.com/tutorials/google-fit-for-android-history-api--cms-25856
     * https://stackoverflow.com/questions/41285508/calories-for-session-google-fit/41789675#41789675 - if fields
     *
     * @param dataSet
     */
    private void retrieveData(DataSet dataSet) {
        Log.i(HisTag, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getDateInstance();
        DateFormat timeformat = getTimeInstance();
        // used to get day of the week from the date
        SimpleDateFormat getDayOfTheWeek = new SimpleDateFormat("EEEE");


        for (DataPoint dp : dataSet.getDataPoints()) {

            Log.e(HisTag, "Data point:");
            Log.e(HisTag, "\tType: " + dp.getDataType().getName());


            // get date , convert to week data
            String day = String.valueOf(dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            String formattedDay = getDayOfTheWeek.format(new Date(day));


            Log.i(HisTag, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS))
                    + timeformat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(HisTag, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));

            // get field in the data point
            for (Field field : dp.getDataType().getFields()) {
                String cal = null;

                // Get calories field
                if (field.getName().equalsIgnoreCase(Field.FIELD_CALORIES.getName())) {
                    Log.i(HisTag, "Name of the field:" + field.getName() + " Field's value: " + dp.getValue(field));

                    Value val = dp.getValue(field); // get value
                    cal = String.valueOf(val); // convert
                    String cl = cal.split("\\.", 2)[0]; // round
                    calories.add(cl); // add to array

                }
                // get steps field
                if (field.getName().equalsIgnoreCase(Field.FIELD_STEPS.getName())) {
                    String steps = String.valueOf(dp.getValue(field).asInt()); // get value
                    Log.i(HisTag, "Field Name : " + field.getName() + " Field value: " + dp.getValue(field));

                    step.add(steps); // add steps to steps array
                    datOfweek.add(formattedDay); // add day of week to the array
                    date.add(day); // add date

                }

            }


        }


    }

    /**
     * Adds data from arraylists to List with weeklyData Objects and reverses it,
     * so the first day in the recycler -> yesterday
     *
     * @return historyDataArray
     */
    private List<WeeklyData> fetchData() {
        for (int i = 0; i < calories.size(); i++) {
            historyDataArray.add(new WeeklyData(datOfweek.get(i), date.get(i), step.get(i), calories.get(i)));
            Log.i(HisTag, datOfweek.get(i));
        }

        return reverseList(historyDataArray);

    }

    /**
     * This method reverse array with weekly data.
     *
     * @param dataList - gets array with WeeklyData
     * @return reversed list of weeklydata
     */
    private static List<WeeklyData> reverseList(List<WeeklyData> dataList) {
        Collections.reverse(dataList);
        return dataList;

    }

    /**
     * This method is not used, but should be implemented. An activity can bind to the service
     * by invoking bindService() method, clients should implement the interface which
     * facilitates communication by  returning IBinder.
     * <p>
     * In such a case, the service will be alive until onUnbind is called.
     *
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        // retunr null as there is no binding
        return null;
    }


    /**
     * getter for the list
     *
     * @return historyDataArray
     */
    public static List<WeeklyData> getHistoryDataArray() {
        return historyDataArray;
    }

    // [END] of methods related to readWeeklyData

    /**
     *
     * readDailyTotal methods start
     *
     * reads data for the current day, computed from the midnight.
     *
     * Read data for the current day  DATATYPEs - CALORIES,STEPS,MOVE MINTETS, DISTANCE . Displayed in the Main Activity and saved in SharedPref.
     * It display current result by itself - no need to clear SharedPref.
     *
     *
     * Source: https://developers.google.com/fit/scenarios/read-daily-step-total
     * Source for the request used: https://github.com/googlesamples/android-fit/blob/master/StepCounter/app/src/main/java/com/google/android/gms/fit/samples/stepcounter/MainActivity.java
     * The latter allows to avoid using and build GooglApiClient
     */

    /**
     * Read data for the current day  DATATYPE - CALORIES,STEPS,DISTANCE,MOVE_MINUTES. Displayed in the Main Activity and saved in SharedPref.
     * Since it returns the result for the day - become 0 at midnight -> no need to clear SharedPreference via alarm.
     */

    // get calories
    private void readCalories() {

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(final DataSet dataSet) {

                                float totalCal =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();

                                // set calories value in SharedPref
                                SharedPrefUtils.setCalories(getApplicationContext(), totalCal);

                                Log.i(HisTag, "Daily Calories" + (int) totalCal);


                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(HisTag, "There was a problem getting the today's calories.", e);
                            }
                        });

    }

    // get steps
    private void readStepCountDelta1() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(final DataSet dataSet) {
                                long totalSteps =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                                Log.i(HisTag, "Daily Steps " + totalSteps);

                                // save value in sharedpref
                                SharedPrefUtils.setStepsCount(getApplicationContext(), (int) totalSteps);


                            }
                        })

                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(HisTag, "There was a problem getting the today's steps.", e);
                            }
                        });


    }

    // get Distance
    private void readDistance() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.AGGREGATE_DISTANCE_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(final DataSet dataSet) {
                                float totalDistance =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_DISTANCE).asFloat();

                                //save value in sharedpref
                                SharedPrefUtils.setDistance(getApplicationContext(), (int) totalDistance);
                                Log.i(HisTag, "Daily Distance" + totalDistance);


                            }
                        })

                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(HisTag, "There was a problem getting today's distance.", e);
                            }
                        });


    }

    // get active min/ move minutes
    private void readActive() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_MOVE_MINUTES)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(final DataSet dataSet) {
                                long totalActiveMin =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_DURATION).asInt();

                                //save value in shared pref
                                SharedPrefUtils.setMoveMinutes(getApplicationContext(), (int) totalActiveMin);
                                Log.i(HisTag, "Move minutes" + totalActiveMin);


                            }
                        })

                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(HisTag, "There was a problem getting today's active time.", e);
                            }
                        });


    }


}

