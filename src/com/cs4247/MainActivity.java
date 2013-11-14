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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
    
    /*
     * An instance of an inner class that receives broadcasts from listeners and from the
     * IntentService that receives geofence transition events
     */
    private ContextUpdateReceiver mBroadcastReceiver;
    
 // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;

    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    
    private GeofenceRequester mGeofenceRequester;
    
    private ActivityRecognitionRequester mActivityRequester;
    
    ArrayList<Event> events;
    
    HashMap<String, Event> markerMap;
    
    EventFilter eventFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mBroadcastReceiver = new ContextUpdateReceiver();
        
        mIntentFilter = new IntentFilter();
        
        // Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(Utilities.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(Utilities.ACTION_GEOFENCES_REMOVED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(Utilities.ACTION_GEOFENCE_ERROR);
        
        // Action for broadcast Intents for transitions
        mIntentFilter.addAction(Utilities.ACTION_GEOFENCE_TRANSITION);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(Utilities.CATEGORY_LOCATION_SERVICES);
        
        mGeofenceRequester = new GeofenceRequester(this);
        mActivityRequester = new ActivityRecognitionRequester(this);
        
        events = new ArrayList<Event>();
        
        markerMap = new HashMap<String, Event>();
        
        try {
            // Try to add geofences
            mGeofenceRequester.addGeofences(Utilities.getGeofences());
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, "Error: geofence already requested.",
                        Toast.LENGTH_LONG).show();
        }
       
        mActivityRequester.requestUpdates();
        
        eventFilter = new EventFilter(this);
        eventFilter.extractAndIndexSMS();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setUpLocationClientIfNeeded();
        mLocationClient.connect();
        centerMapOnMyLocation();
        
        // update map markers
        updateMapMarkers();
        
        // Register the broadcast receiver to receive status updates
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
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
            
            mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(1.352083,103.819836) , 10.0f) );
            
            mMap.setOnInfoWindowClickListener(new InfoWindowClickListener());
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
    
    private void updateMapMarkers(){
    	// make connection to retrieve relevant events
    	//List<SimpleGeofence> appGeofences = Utilities.getSimpleGeofences();
    	
    	Location location = mMap.getMyLocation();
    	
    	if (location != null){
    		AQuery aq = new AQuery(this);
    		aq.ajax(Utilities.getServerURL(location.getLatitude(), location.getLongitude(), Utilities.getSmartRadius(this) ), 
    				JSONArray.class, this, "serverCallback");
    	}
    }
    
    public void serverCallback(String url, JSONArray json, AjaxStatus status){
    	System.out.println("main: servercallback()");
    	if(json != null){         
    		System.out.println("in main: " + json.toString());
    		
    		events.clear();
    		markerMap.clear();
    		mMap.clear();
    		
    		for(int i = 0; i < json.length(); i++){
    			try {
					events.add(new Event(json.getJSONObject(i)) );
				} catch (JSONException e) {
					e.printStackTrace();
				}
    		}
    		
    		// add markers
        	for(int i = 0; i < events.size(); i++){
        		Marker currMarker = mMap.addMarker(new MarkerOptions().
        				position(new LatLng(events.get(i).getLa(), events.get(i).getLo())).
        				title(events.get(i).getEventname()).
        				snippet(events.get(i).getDescription())
        				);
        		
        		if(eventFilter.scoreEvent(events.get(i)) > Utilities.FILTER_SCORE){
        			markerMap.put(currMarker.getId(), events.get(i));
        		}
        		
        		System.out.println("No. " + i + " " + events.get(i).getEventname() + " " + eventFilter.scoreEvent(events.get(i)));
        	}
    	}else{
    		// ajax error
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
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(1.297081, 103.773615))
                .title("Hello world"));
        return false;
    }
    
    public class InfoWindowClickListener implements GoogleMap.OnInfoWindowClickListener {

		@Override
		public void onInfoWindowClick(Marker marker) {
			// TODO Auto-generated method stub
			
			//System.out.println(markerMap.get(marker.getId()).getEventname() );
		}
    	
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
        	
        	System.out.println(intent.getAction());
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
            	System.out.println("here");
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
        	ArrayList<Event> events = (ArrayList<Event>) intent.getSerializableExtra(Utilities.EXTRA_EVENT);
        	
        	// add markers
        	for(int i = 0; i < events.size(); i++){
        		mMap.addMarker(new MarkerOptions().
        				position(new LatLng(events.get(i).getLa(), events.get(i).getLo())).
        				title(events.get(i).getEventname()).
        				snippet(events.get(i).getDescription())
        				);
        	}
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
