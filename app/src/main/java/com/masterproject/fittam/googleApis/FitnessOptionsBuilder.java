package com.masterproject.fittam.googleApis;

import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

public class FitnessOptionsBuilder {
    /**
     * This class contains fitness options builder. This is used in authentication process
     * in the MainActivity to receive permissions from the user and access his fitness data.
     * Source:https://developers.google.com/android/reference/com/google/android/gms/fitness/FitnessOptions.Builder
     */

    private static FitnessOptions fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE) // get total steps
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA) // get current steps
            //History
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA) // totalsteps, used in History request
            .addDataType(DataType.TYPE_CALORIES_EXPENDED) // calories
            .addDataType(DataType.TYPE_LOCATION_SAMPLE) // not used, saw in the example. Source: https://github.com/googlesamples/android-fit/blob/master/BasicSensorsApi/app/src/main/java/com/google/android/gms/fit/samples/basicsensorsapi/MainActivity.java
            // the following used in readDailyTotal
            .addDataType(DataType.AGGREGATE_DISTANCE_DELTA) // used to get distance
            .addDataType(DataType.TYPE_DISTANCE_DELTA) //  distance, not used
            .addDataType(DataType.AGGREGATE_ACTIVITY_SUMMARY) // it is possible to get distance or activity type via this, not used
            .addDataType(DataType.TYPE_MOVE_MINUTES) // active minutes, used in HistoryApi, readDailyTotal
            .build();

    /**
     * Returns the builder for request of datatypes to access
     *
     * @return fitnessOptions - builder to get access to datatypes
     */
    public static FitnessOptions getFitnessOptions() {
        return fitnessOptions;
    }
}
