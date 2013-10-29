package com.cs4247;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

public class ContextUpdateService extends IntentService {
	
	List<Event> events;

	public ContextUpdateService() {
		super("ContextUpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(Utilities.APPTAG, "geofence intent received.");
		
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
            
            /*
             * We ignore local broadcast for now
             * 
            // Set the action and error message for the broadcast intent
            broadcastIntent.setAction(Utilities.ACTION_GEOFENCE_ERROR)
                           .putExtra(Utilities.EXTRA_GEOFENCE_STATUS, errorMessage);

            // Broadcast the error *locally* to other components in this app
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
            */

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
    		
    		// print out events
    		for(int i = 0; i < events.size(); i++){
    			System.out.println(events.get(i).getAddress1());
    		}
    	}else{
    		// ajax error
    	}
    }

}
