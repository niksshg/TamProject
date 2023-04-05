package com.masterproject.fittam.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.masterproject.fittam.MainActivity;
import com.masterproject.fittam.R;
import com.masterproject.fittam.TamHomePageActivity;

import org.jetbrains.annotations.NotNull;

/**
 * NotificationUtils
 * <p>
 * This class is responsible for notifications. Contains 3 channels, 3 notifications, 3 pending intents (
 * when the user taps notification, he gets redirected to certain app screen.
 * And 2 methods that identifies message to be sent.
 * 1 method for big picture.
 * <p>
 * There are 3 channels intentionally, used in evaluation to find out which notifications are better:
 * conditional ( goal evaluation in the evening, idle device) or unconditional every 2h.
 * <p>
 * Otherwise, it can be grouped in 1-2 channels.
 * <p>
 * <p>
 * Sources: https://codelabs.developers.google.com/codelabs/android-training-notifications/index.html?index=..%2F..android-training#0
 * , NAME: "Developing Android Apps".
 * https://developer.android.com/training/notify-user/build-notification.html
 */

public class NotificationUtils {

    // Can be used after notification was displayed.
    private static final int STITCH_PROGRESS_NOTIFICATION_ID = 1010;
    private static final int STITCH_IDLE_NOTIFICATION_ID = 1011;
    private static final int STITCH_GOAL_NOTIFICATION_ID = 1111;

    // unique intetn reference
    private static final int STATE_PROGRESS_REMINDER_PENDING_INTENT = 2020;
    private static final int IDLE_REMINDER_PENDING_INTENT = 2022;
    private static final int GOAL_COMPLETION_PENDING_INTENT = 2222;


    // unique channel id, used to link channel and notification
    private static final String STITCH_NOTIFICATION_CHANNEL_ID = "stitch_progress_notification";
    private static final String STITCH_NOTIFICATION_CHANNEL_ID2 = "stitch_idle_notification";
    private static final String STITCH_NOTIFICATION_CHANNEL_ID3 = "Goal complition channel";

    private static String translatedState;

    /**
     * Method that creates regular notifications. Notification sent every 2h, when by
     * workmanager.
     *
     * @param context
     */
    public static void hourlyStateNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // create channel for android O devices. Users can turn off channels. Used in Evaluation.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    STITCH_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.notification_channel_regular), // name
                    NotificationManager.IMPORTANCE_HIGH); // for 0+, otherwise channels are not supported
            notificationManager.createNotificationChannel(mChannel);
        }

        // build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, STITCH_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary)) //set color specified in colors.xml
                .setSmallIcon(R.drawable.ic__small_stich_icon) // icon
                .setLargeIcon(largeIcon(context)) // defines icon image in notification
                // title of the notificaion
                .setContentTitle(SharedPrefUtils.getBudName(context.getApplicationContext()) + " " + context.getString(R.string.progress_notification_title))
                // stateTranslater - returns message to display
                .setContentText(stateTranslater(context.getApplicationContext()))
                //  make text bigger, display style
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        stateTranslater(context.getApplicationContext())))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                // add pending intent, grantees that application will be opened when clicked
                .setContentIntent(intentToRedirectToStich(context))
                // notification will disappear when clicked
                .setAutoCancel(true);
        // high priority for versions lower than Oreo, make notification to show
        // The min version for application to work is M, specified in gradle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(STITCH_PROGRESS_NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Notification send when device is idle.
     *
     * @param context
     */
    public static void deviceIdleNotificaiont(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    STITCH_NOTIFICATION_CHANNEL_ID2,
                    context.getString(R.string.notification_channel_idle),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, STITCH_NOTIFICATION_CHANNEL_ID2)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic__small_stich_icon)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.idle_notification_title))
                // It is not best idea to concatenate strings like that, as it seems I cannot add it
                // to string xml directly, which disables translation options.
                // Therefore, it works only if system lang is english. It is possible
                //
                .setContentText("It looks like that " + SharedPrefUtils.getBudName(context.getApplicationContext()) + " wants to go for a walk ! " + " Current steps "
                        + String.valueOf(SharedPrefUtils.getGoal(context.getApplicationContext())))

                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        "It looks like that " + SharedPrefUtils.getBudName(context.getApplicationContext()) + " wants to go for a walk ! " + " Current steps " +
                                String.valueOf(SharedPrefUtils.getStepsCount(context.getApplicationContext()))))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(redirectToMainIdle(context))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(STITCH_IDLE_NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Notification is sent in the evening. Notifies about goal complities
     *
     * @param context
     */
    public static void setGoalCompletionNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    STITCH_NOTIFICATION_CHANNEL_ID3,
                    context.getString(R.string.notification_channel_goal),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, STITCH_NOTIFICATION_CHANNEL_ID3)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic__small_stich_icon)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.goal_notification_title))
                // method that returns text depending on goal completion
                .setContentText(whetherGoalWasAchieved(context.getApplicationContext()))

                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        whetherGoalWasAchieved(context.getApplicationContext())))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(redirectToMainGoal(context))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(STITCH_GOAL_NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Method that decodes picture for the display
     *
     * @param context
     * @return
     */
    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        Bitmap bigStitchIcon = BitmapFactory.decodeResource(res, R.drawable.stich_small_icon);
        return bigStitchIcon;
    }

    /**
     * When the user clicks notification -> opens tamagotchi page
     *
     * @param context
     * @return
     */
    private static PendingIntent intentToRedirectToStich(Context context) {
        Intent startActivityIntent = new Intent(context, TamHomePageActivity.class);
        return PendingIntent.getActivity(
                context,
                STATE_PROGRESS_REMINDER_PENDING_INTENT,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * When the user clicks notification -> opens Main  page
     *
     * @param context
     * @return
     */
    private static PendingIntent redirectToMainIdle(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                IDLE_REMINDER_PENDING_INTENT,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * When the use clicks notification -> opens main
     *
     * @param context
     * @return
     */
    private static PendingIntent redirectToMainGoal(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                GOAL_COMPLETION_PENDING_INTENT,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Get state, display appropriate message
     *
     * @param context
     * @return translatedState - message to be displayed in regular notifications
     */
    private static String stateTranslater(@NotNull Context context) {
        int state = SharedPrefUtils.getBudState(context.getApplicationContext());
        switch (state) {
            case 1:
                translatedState = " Your " + SharedPrefUtils.getBudName(context.getApplicationContext()) + " looks sad ";
                break;
            case 2:
                translatedState = " Your " + SharedPrefUtils.getBudName(context.getApplicationContext()) + " is feeling like walking ";
                break;
            case 3:
                translatedState = " Your " + SharedPrefUtils.getBudName(context.getApplicationContext()) + " is getting happier! " +
                        " You made " + String.valueOf(SharedPrefUtils.getStepsCount(context.getApplicationContext()) + " today ");
                break;
            case 4:
                translatedState = " Your " + SharedPrefUtils.getBudName(context.getApplicationContext()) + " wants to walk a little bit more. " +
                        " Just " + String.valueOf(SharedPrefUtils.getGoal(context.getApplicationContext()) - SharedPrefUtils.getStepsCount(context.getApplicationContext()) +
                        " to your goal ");
                break;
            case 5:
                translatedState = " You and  " + SharedPrefUtils.getBudName(context.getApplicationContext()) + " achived your goal today! " + " Goal " + SharedPrefUtils.getGoal(context.getApplicationContext());
                break;
        }
        return translatedState;
    }

    /**
     * Get goals status, display appropriate message in the goal notification
     *
     * @param context
     * @return
     */
    private static String whetherGoalWasAchieved(@NotNull Context context) {
        String goalStatus = null;
        int goal = SharedPrefUtils.getGoal(context.getApplicationContext());
        int steps = SharedPrefUtils.getStepsCount(context.getApplicationContext());
        String budName = SharedPrefUtils.getBudName(context.getApplicationContext());
        boolean goalAchieved = SharedPrefUtils.getGoalAchievment(context.getApplicationContext());

        if (goalAchieved == true) {
            goalStatus = ".. was achieved! " + budName + " is really Happy Today " + "\n"
                    + " Steps : " + steps + "/ " + goal;

        } else {
            goalStatus = ".. was't achieved. " + budName + " might get sad ." + "\n" +
                    " Steps : " + steps + "/ " + goal;

        }


        return goalStatus;
    }

}
