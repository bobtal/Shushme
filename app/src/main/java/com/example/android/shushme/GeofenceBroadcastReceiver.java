package com.example.android.shushme;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    /***
     * Handles the Broadcast message sent when the Geofence Transition is triggered
     * Careful here though, this is running on the main thread so make sure you start an AsyncTask for
     * anything that takes longer than say 10 second to run
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive called");
        // COMPLETED (4) Use GeofencingEvent.fromIntent to retrieve the GeofencingEvent that caused the transition
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            Log.e(TAG, String.format("Error code : %d", event.getErrorCode()));
            return;
        }

        // COMPLETED (5) Call getGeofenceTransition to get the transition type and use AudioManager to set the
        // phone ringer mode based on the transition type. Feel free to create a helper method (setRingerMode)
        int geofenceTransition = event.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            setRingerMode(context, AudioManager.RINGER_MODE_SILENT);
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            setRingerMode(context, AudioManager.RINGER_MODE_NORMAL);
        } else {
            // Log the error
            Log.e(TAG, String.format("Unknown transition : %d", geofenceTransition));
            // no need to do anything else
            return;
        }

        // COMPLETED (6) Show a notification to alert the user that the ringer mode has changed.
        // Feel free to create a helper method (sendNotification)
        sendNotification(context, geofenceTransition);

    }

    private void setRingerMode(Context context, int mode) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Check for Do Not Disturb permissions for API 24+
        if (Build.VERSION.SDK_INT < 24 ||
                Build.VERSION.SDK_INT >= 24 && !nm.isNotificationPolicyAccessGranted()) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(mode);
        }
    }

    private void sendNotification(Context context, int transitionType) {
        // Create an explicit content intent that start the main activity
        Intent notificationIntent = new Intent(context, MainActivity.class);

        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main activity to the task stack as the parent
        stackBuilder.addParentStack(MainActivity.class);

        // Puch the content intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Get a pending intent containing the entire back stack
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Check the transition type to display the relevant icon image
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            builder
                    .setSmallIcon(R.drawable.ic_volume_off_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(
                            context.getResources(), R.drawable.ic_volume_off_white_24dp))
                    .setContentTitle(context.getString(R.string.silent_mode_activated));
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            builder
                    .setSmallIcon(R.drawable.ic_volume_up_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(
                            context.getResources(), R.drawable.ic_volume_up_white_24dp))
                    .setContentTitle(context.getString(R.string.back_to_normal));
        }

        // Continue building the notification
        builder
                .setContentText(context.getString(R.string.touch_to_relaunch))
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);

        // Get an instance of the notification manager
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        nm.notify(0, builder.build());
    }
}
