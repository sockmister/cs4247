/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cs4247;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The
 * "My Location" button uses GMS Location to set the blue dot representing the users location. To
 * track changes to the users location on the map, we request updates from the
 * {@link LocationClient}.
 */
public class MainActivity extends FragmentActivity
        implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener,
        OnMyLocationButtonClickListener {

    private GoogleMap mMap;
    
    private LocationClient mLocationClient;

    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    
    private GeofenceRequester mGeofenceRequester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mGeofenceRequester = new GeofenceRequester(this);
        
        // sync geofences with web service? or use predetermined geofences
        try {
            // Try to add geofences
            mGeofenceRequester.addGeofences(Utilities.getGeofences());
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, "Error: geofence already requested.",
                        Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setUpLocationClientIfNeeded();
        mLocationClient.connect();
        centerMapOnMyLocation();
        
        // Register the broadcast receiver to receive status updates
        //LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);
            }
        }
    }

    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(
                    getApplicationContext(),
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
    }
    
    private void centerMapOnMyLocation() {

        mMap.setMyLocationEnabled(true);

        Location location = mMap.getMyLocation();

        if (location != null) {
            LatLng myLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                    14));
        }        
    }

    /**
     * Button to get current Location. This demonstrates how to get the current Location as required
     * without needing to register a LocationListener.
     */
    public void showMyLocation(View view) {
        if (mLocationClient != null && mLocationClient.isConnected()) {
            String msg = "Location = " + mLocationClient.getLastLocation();
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Implementation of {@link LocationListener}.
     */
    @Override
    public void onLocationChanged(Location location) {
        //mMessageView.setText("Location = " + location);
    }

    /**
     * Callback called when connected to GCore. Implementation of {@link ConnectionCallbacks}.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationClient.requestLocationUpdates(
                REQUEST,
                this);  // LocationListener
    }

    /**
     * Callback called when disconnected from GCore. Implementation of {@link ConnectionCallbacks}.
     */
    @Override
    public void onDisconnected() {
        // Do nothing
    }

    /**
     * Implementation of {@link OnConnectionFailedListener}.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Do nothing
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
    
    /**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
     * 
     * Use this to update UI of app when location changes
     * TODO remember to
     * register receiver to local broadcast manager in onResume()
     * setup intent filter
     * update ContextUpdateService to broadcast changes
     */
    public class ContextUpdateReceiver extends BroadcastReceiver {
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {
        	// Check the action code and determine what to do
            String action = intent.getAction();

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, Utilities.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);

            // Intent contains information about successful addition or removal of geofences
            } else if ( TextUtils.equals(action, Utilities.ACTION_GEOFENCES_ADDED) ||
                    TextUtils.equals(action, Utilities.ACTION_GEOFENCES_REMOVED)) {

                handleGeofenceStatus(context, intent);

            // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, Utilities.ACTION_GEOFENCE_TRANSITION)) {

                handleGeofenceTransition(context, intent);

            // The Intent contained an invalid action
            } else {
            	//TODO
            }
        }

        /**
         * Update is on adding or removing of geofence
         *
         * @param context A Context for this component
         * @param intent The received broadcast Intent
         */
        private void handleGeofenceStatus(Context context, Intent intent) {

        }

        /**
         * Update is on geofence transition
         *
         * @param context A Context for this component
         * @param intent The Intent containing the transition
         */
        private void handleGeofenceTransition(Context context, Intent intent) {

        }

        /**
         * Update is on adding or removing errors
         *
         * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
         */
        private void handleGeofenceError(Context context, Intent intent) {
        	
        }
    }
}
