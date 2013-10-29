package com.cs4247;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

public class ContextUpdateService extends IntentService {
	
	ArrayList<Event> events;
	Intent broadcastIntent;

	public ContextUpdateService() {
		super("ContextUpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(Utilities.APPTAG, "geofence intent received.");
		
        // Create a local broadcast Intent
        broadcastIntent = new Intent();

        // Give it the category for all intents sent by the Intent Service
        broadcastIntent.addCategory(Utilities.CATEGORY_LOCATION_SERVICES);
		
		// First check for errors
        if (LocationClient.hasError(intent)) {

            // Get the error code
            int errorCode = LocationClient.getErrorCode(intent);

            // Get the error message
            String errorMessage = LocationServiceErrorMessages.getErrorString(this, errorCode);

            // Log the error
            Log.e(Utilities.APPTAG,
                    getString(R.string.geofence_transition_error_detail, errorMessage)
            );
            
            // Set the action and error message for the broadcast intent
            broadcastIntent.setAction(Utilities.ACTION_GEOFENCE_ERROR)
                           .putExtra(Utilities.EXTRA_GEOFENCE_STATUS, errorMessage);

            // Broadcast the error *locally* to other components in this app
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

        } else {

            // Get the type of transition (entry or exit)
            int transition = LocationClient.getGeofenceTransition(intent);

            // Test that a valid transition was reported
            if ( (transition == Geofence.GEOFENCE_TRANSITION_ENTER) || 
                    (transition == Geofence.GEOFENCE_TRANSITION_EXIT) ) {
            	
            	// make connection to retrieve relevant events
            	List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
            	List<SimpleGeofence> appGeofences = Utilities.getSimpleGeofences();
            	if(events == null) events = new ArrayList<Event>();
            	
            	System.out.println("received geofences of size: " + geofences.size());
            	
            	AQuery aq = new AQuery(this);
            	for(Geofence currGeofence : geofences){	
            		int index = Integer.valueOf(currGeofence.getRequestId()) - 1;
            		Double lat = appGeofences.get(index).getLatitude();
            		Double lon = appGeofences.get(index).getLongitude();
            		Double ra = (double) appGeofences.get(index).getRadius();
            		
            		System.out.println(Utilities.getServerURL(lat, lon, ra));
            		
            		aq.ajax(Utilities.getServerURL(lat, lon, ra), JSONArray.class, this, "serverCallback"); 
            	}
            	
            	

            // An invalid transition was reported
            } else {
                // Always log as an error
                Log.e(Utilities.APPTAG,
                        getString(R.string.geofence_transition_invalid_type, transition));
            }
        }
	}
	
	/**
     * Maps geofence transition types to their human-readable equivalents.
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);

            default:
                return getString(R.string.geofence_transition_unknown);
        }
    }
    
    /*
     * Sends notification to notification bar only if app is not running in foreground.
     */
    public Notification buildNotification(String arg0, Map<String, String> arg1) {

        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> services = activityManager
                .getRunningTasks(Integer.MAX_VALUE);
        boolean isActivityFound = false;

        if (services.get(0).topActivity.getPackageName().toString()
                .equalsIgnoreCase(this.getPackageName().toString())) {
            isActivityFound = true;
        }

        if (isActivityFound) {
            return null;
        } else {
        	// TODO create notification with events.
        	
        	return new Notification();
        }

    }
    
    public void serverCallback(String url, JSONArray json, AjaxStatus status){
    	if(json != null){         
    		System.out.println(json.toString());
    		for(int i = 0; i < json.length(); i++){
    			try {
					events.add(new Event(json.getJSONObject(i)) );
				} catch (JSONException e) {
					e.printStackTrace();
				}
    		}
    		
    		// broadcast information to app
    		broadcastIntent.setAction(Utilities.ACTION_GEOFENCE_TRANSITION)
    			.putExtra(Utilities.EXTRA_GEOFENCE_STATUS, "");
    	
	    	// Set the action and error message for the broadcast intent
	        broadcastIntent.putExtra(Utilities.EXTRA_EVENT, events);
	
	        // Broadcast the error *locally* to other components in this app
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    	}else{
    		// ajax error
    	}
    }

}
