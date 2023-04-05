package com.masterproject.fittam.googleApis;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.masterproject.fittam.utilities.SharedPrefUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * This service contains Sensor Api method to read data from sensors. It lists available data source,
 * registers listeners to recieve data. Also, it has 2 methods using Recording API to upload data from sensors
 * to fitness store. DataTypes are steps and distance. If distance is not subscribed, it cannot be obtained from history.
 * In addition, the distance data requires manifest permission.
 * <p>
 * Sources:   https://developers.google.com/fit/android/record
 * https://developers.google.com/fit/android/sensors
 * https://github.com/googlesamples/android-fit/blob/master/BasicSensorsApi/app/src/main/java/com/google/android/gms/fit/samples/basicsensorsapi/MainActivity.java
 */
public class SensorApi extends Service {


    // Listener for steps datasource
    private OnDataPointListener stepsListener;

    private static final String TAG = "SensorActivity";
    private int steps;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // record data
        recordStepsCount();
        recordDistance();

        //Listen to steps
        identifyStepsDataSources();
        return Service.START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     *  Recording API starts
     *
     */


    /**
     * This method invokes Recording api and starts recording.
     */
    private void recordStepsCount() {
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_DELTA)

                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed Steps");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem subscribing Steps");
                    }
                });

    }

    private void recordDistance() {
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.AGGREGATE_DISTANCE_DELTA)

                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed Distance");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem subscribing Distance");
                    }
                });

    }


    /**
     *
     * Sensor API starts
     *
     *
     */


    /**
     * This method finds available datasource and subscripse to specific data type.
     * TYPE_STEP_COUNT_DELTA - each data point represents the number of steps since previous reading
     * TYPE_DERIVED - the data is derived from several data sources and transformed
     * <p>
     * Sources: https://developers.google.com/android/reference/com/google/android/gms/fitness/data/DataSource
     * https://developers.google.com/android/reference/com/google/android/gms/fitness/data/DataType.html#TYPE_DISTANCE_DELTA
     * https://github.com/googlesamples/android-fit/blob/master/BasicSensorsApi/app/src/main/java/com/google/android/gms/fit/samples/basicsensorsapi/MainActivity.java
     */
    private void identifyStepsDataSources() {
        Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .findDataSources(
                        new DataSourcesRequest.Builder()
                                .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                                .build())
                .addOnSuccessListener(
                        new OnSuccessListener<List<DataSource>>() {
                            @Override
                            public void onSuccess(List<DataSource> dataSources) {
                                for (DataSource dataSource : dataSources) {
                                    Log.i(TAG, "Data source found: " + dataSource.toString());
                                    Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());
                                    // Register listeners if there is requested datasource and listener is not
                                    // already registered
                                    if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)
                                            && stepsListener == null) {
                                        Log.i(TAG, "Data source for TYPE_STEP_COUNT found. Registering.");
                                        attachListenerToDataSource(dataSource, DataType.TYPE_STEP_COUNT_DELTA);
                                    }

                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "failed", e);
                            }
                        });

    }

    /**
     * This method attaches listeners to Sensor API datasource and data type to receive data.
     *
     * @param dataSource
     * @param dataType
     */
    public void attachListenerToDataSource(DataSource dataSource, DataType dataType) {
        stepsListener =
                new OnDataPointListener() {
                    @Override
                    public void onDataPoint(DataPoint dataPoint) {

                        final StringBuilder sb = new StringBuilder();
                        for (Field field : dataPoint.getDataType().getFields()) {
                            Value val = dataPoint.getValue(field);
                            steps = val.asInt();

                            // AsyncTask which updates SharedPref
                            new CalculateStepsTask().execute(steps);

                            Log.i(TAG, "Detected DataPoint field: " + field.getName());
                            Log.i(TAG, "Detected DataPoint value: " + val);
                        }

                    }
                };

        Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .add(
                        new SensorRequest.Builder()
                                .setDataSource(dataSource) // Optional
                                .setDataType(dataType) // Required
                                .setSamplingRate(1, TimeUnit.SECONDS) // how often
                                .build(),
                        stepsListener)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Listener registered");
                                } else {
                                    Log.e(TAG, "Listener not registered.", task.getException());
                                }
                            }
                        });
    }

    /**
     *
     * APIS END
     */

    /**
     * AsynTask to update sharedPref
     */

    private class CalculateStepsTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... integers) {
            Integer steps = integers[0];
            Integer newStepsData = null;
            try {
                SharedPrefUtils.updateStepsCount(SensorApi.this, steps);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


    }


}