package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;

// COMPLETED (1) Create a Geofencing class with a Context and GoogleApiClient constructor that
// initializes a private member ArrayList of Geofences called mGeofenceList
public class Geofencing implements ResultCallback<Status> {
    private static final String TAG = Geofencing.class.getSimpleName();

    private static final int GEOFENCE_RADIUS_IN_METERS = 50;

    private static final int GEOFENCE_EXPIRATION_IN_HOURS = 72;
    private static final int GEOFENCE_EXPIRATION_IN_MINUTES = GEOFENCE_EXPIRATION_IN_HOURS * 24;
    private static final int GEOFENCE_EXPIRATION_IN_SECONDS = GEOFENCE_EXPIRATION_IN_MINUTES * 60;
    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_SECONDS * 1000;
    private Context context;
    private GoogleApiClient googleApiClient;
    private PendingIntent geofencePendingIntent;
    private ArrayList<Geofence> geofenceList;


    public Geofencing(Context context, GoogleApiClient client) {
        this.context = context;
        this.googleApiClient = client;
        geofencePendingIntent = null;
        geofenceList = new ArrayList<>();
    }

    // COMPLETED (2) Inside Geofencing, implement a public method called updateGeofencesList that
    // given a PlaceBuffer will create a Geofence object for each Place using Geofence.Builder
    // and add that Geofence to mGeofenceList
    public void updateGeofencesList(PlaceBuffer places) {
        geofenceList = new ArrayList<>();
        if (places == null || places.getCount() == 0) {
            return;
        }
        for (Place place : places){
            String placeId = place.getId();
            double placeLatitiude = place.getLatLng().latitude;
            double placeLongitude = place.getLatLng().longitude;
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeId)
                    .setCircularRegion(
                            placeLatitiude,
                            placeLongitude,
                            GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            geofenceList.add(geofence);
        }
    }

    // COMPLETED (3) Inside Geofencing, implement a private helper method called getGeofencingRequest that
    // uses GeofencingRequest.Builder to return a GeofencingRequest object from the Geofence list
    private GeofencingRequest getGeofencingRequest() {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build();
    }

    // COMPLETED (5) Inside Geofencing, implement a private helper method called getGeofencePendingIntent that
    // returns a PendingIntent for the GeofenceBroadcastReceiver class
    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    // COMPLETED (6) Inside Geofencing, implement a public method called registerAllGeofences that
    // registers the GeofencingRequest by calling LocationServices.GeofencingApi.addGeofences
    // using the helper functions getGeofencingRequest() and getGeofencePendingIntent()
    public void registerAllGeofences() {
        if (googleApiClient == null || !googleApiClient.isConnected() ||
                geofenceList == null || geofenceList.size() == 0) {
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            Log.e(TAG, securityException.getMessage());
        }
    }

    // COMPLETED (7) Inside Geofencing, implement a public method called unRegisterAllGeofences that
    // unregisters all geofences by calling LocationServices.GeofencingApi.removeGeofences
    // using the helper function getGeofencePendingIntent()
    public void unRegisterAllGeofences() {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(
                    googleApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            Log.e(TAG, securityException.getMessage());
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.e(TAG, String.format(
                "Error adding/removing geofence : %s", status.getStatus().toString()));
    }
}
