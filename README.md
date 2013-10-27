# cs4247

## Setup
### Android SDK + Eclipse
Make sure to have 
* Android 4.3 API 18
* Android 2.2 API 8
* Extras->Google Play Services
Import existing Android project into Eclipse

### Google Play Services
Copy folder android-sdk-macosx/extras/google/google_play_services to location to store Android libraries.
Import into Eclipse.

### Hook up app with Google Play Services
With app project in Eclipse, right click on properties->android.
Under Library, click add and select google-play-services-lib project.

### Done

## Files of interest
### MainActivity.java
* Init Maps and set location
* Use GeofenceRequester.java and registers with Google Play Services to be notified of changes in Geofences.
* Define BroadcastReceiver.

### ContextUpdateService.java
* onHandleIntent is called when changes in Geofences occurs. All context awareness code can go here.

### Utilities.java
* Defines geofences. A geofence defines a point location and radius. Only contain a NUS geofence.
