package com.masterproject.fittam.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * ClearSharedPrefBoadcast
 * <p>
 * This class is used but not really needed anymore as the value of steps is
 * reset via HistoryAPI readDaily total.
 * <p>
 * Alarm Manager in SharedPref sends intent to this broadcast.
 */
public class ClearSharedPrefBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPrefUtils.setDefaultSteps(context);
        Log.d("Boradcast", "Broadcast recieved");

    }

}
