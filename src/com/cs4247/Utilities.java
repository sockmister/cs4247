package com.cs4247;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.cs4247.R;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;

/**
 * Defines app-wide constants and utilities
 */
public final class Utilities {

    // Debugging tag for the application
    public static final String APPTAG = "LocationSample";

    // Name of shared preferences repository that stores persistent state
    public static final String SHARED_PREFERENCES =
            "com.example.android.location.SHARED_PREFERENCES";

    // Key for storing the "updates requested" flag in shared preferences
    public static final String KEY_UPDATES_REQUESTED =
            "com.example.android.location.KEY_UPDATES_REQUESTED";

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    public static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;
    
    public static final long DETECTION_INTERVAL_SECONDS = 0;
    
    public static final long DETECTION_INTERVAL_MILLISECONDS = 
    		MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;

    // Create an empty string for initializing strings
    public static final String EMPTY_STRING = new String();
    
    // Intent actions
    public static final String ACTION_CONNECTION_ERROR =
            "com.example.android.geofence.ACTION_CONNECTION_ERROR";

    public static final String ACTION_CONNECTION_SUCCESS =
            "com.example.android.geofence.ACTION_CONNECTION_SUCCESS";

    public static final String ACTION_GEOFENCES_ADDED =
            "com.example.android.geofence.ACTION_GEOFENCES_ADDED";

    public static final String ACTION_GEOFENCES_REMOVED =
            "com.example.android.geofence.ACTION_GEOFENCES_DELETED";

    public static final String ACTION_GEOFENCE_ERROR =
            "com.example.android.geofence.ACTION_GEOFENCES_ERROR";

    public static final String ACTION_GEOFENCE_TRANSITION =
            "com.example.android.geofence.ACTION_GEOFENCE_TRANSITION";

    public static final String ACTION_GEOFENCE_TRANSITION_ERROR =
                    "com.example.android.geofence.ACTION_GEOFENCE_TRANSITION_ERROR";
    
    // The Intent category used by all Location Services sample apps
    public static final String CATEGORY_LOCATION_SERVICES =
                    "com.example.android.geofence.CATEGORY_LOCATION_SERVICES";

    // Keys for extended data in Intents
    public static final String EXTRA_CONNECTION_CODE =
                    "com.example.android.EXTRA_CONNECTION_CODE";

    public static final String EXTRA_CONNECTION_ERROR_CODE =
            "com.example.android.geofence.EXTRA_CONNECTION_ERROR_CODE";

    public static final String EXTRA_CONNECTION_ERROR_MESSAGE =
            "com.example.android.geofence.EXTRA_CONNECTION_ERROR_MESSAGE";

    public static final String EXTRA_GEOFENCE_STATUS =
            "com.example.android.geofence.EXTRA_GEOFENCE_STATUS";
    
    public static final String EXTRA_EVENT = 
    		"EVENTS";
    
    // Key in the repository for the previous activity
    public static final String KEY_PREVIOUS_ACTIVITY_TYPE =
            "com.example.android.activityrecognition.KEY_PREVIOUS_ACTIVITY_TYPE";
    
    public static final float FILTER_SCORE = 0.002f;
    
    /*
     * Server URL
     */
    public static final String SERVER_URL = 
    		"http://penbites.info.tm";
    
    public static String getServerURL(Double lat, Double lon, Double ra){
    	return SERVER_URL + "/get?la=" + lat.toString() + "&lo=" + lon.toString() + "&ra=" + ra.toString();
    }
    
    public static Double getSmartRadius(Context context){
    	
    	SharedPreferences mPrefs = context.getApplicationContext().getSharedPreferences(
                Utilities.SHARED_PREFERENCES, Context.MODE_PRIVATE);
    	
    	int activityType = mPrefs.getInt(Utilities.KEY_PREVIOUS_ACTIVITY_TYPE, -1);
    	
    	switch(activityType) {
    	case -1:
    		return 4000.0;
        case DetectedActivity.IN_VEHICLE:
            return 15000.0;
        case DetectedActivity.ON_BICYCLE:
            return 6000.0;
        case DetectedActivity.ON_FOOT:
            return 4000.0;
        case DetectedActivity.STILL:
            return 1000.0;
        case DetectedActivity.UNKNOWN:
            return 4000.0;
        case DetectedActivity.TILTING:
            return 2000.0;
            
        default:
        	return 4000.0;
    	}
    	
    }


    /**
     * Get the latitude and longitude from the Location object returned by
     * Location Services.
     *
     * @param currentLocation A Location object containing the current location
     * @return The latitude and longitude of the current location, or null if no
     * location is available.
     */
    public static String getLatLng(Context context, Location currentLocation) {
        // If the location is valid
        if (currentLocation != null) {

            // Return the latitude and longitude as strings
            return context.getString(
                    R.string.latitude_longitude,
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude());
        } else {

            // Otherwise, return the empty string
            return EMPTY_STRING;
        }
    }
    
    public static List<SimpleGeofence> getSimpleGeofences(){
    	List<SimpleGeofence> geofences = new ArrayList<SimpleGeofence>();
    	
    	// NUS
		SimpleGeofence geofence1 = new SimpleGeofence(
                "1",
                1.29619002,
                103.77616882,
                5000.0f,
                Geofence.NEVER_EXPIRE,
                Geofence.GEOFENCE_TRANSITION_ENTER);
    	geofences.add(geofence1);
    	
    	// bukit panjang
		SimpleGeofence geofence2 = new SimpleGeofence(
                "2",
                1.37744999,
                103.76732635,
                5000.0f,
                Geofence.NEVER_EXPIRE,
                Geofence.GEOFENCE_TRANSITION_ENTER);
    	geofences.add(geofence2);
    	
    	return geofences;
    }
    
    public static List<Geofence> getGeofences(){
    	List<SimpleGeofence> simpleGeofences = Utilities.getSimpleGeofences();
    	List<Geofence> geofences = new ArrayList<Geofence>();
    	for(SimpleGeofence simpleGeofence : simpleGeofences){
    		geofences.add(simpleGeofence.toGeofence());
    	}
    	
    	return geofences;
    }
}
