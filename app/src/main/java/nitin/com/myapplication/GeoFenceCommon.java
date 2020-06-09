package nitin.com.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class GeoFenceCommon {
    private Context context;

    public GeoFenceCommon(Context context) {
        this.context = context;
        getSharedPreference();
    }

    /**
     */
    public enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    /**
     * Provides access to the Geofencing API.
     */
    public GeofencingClient mGeofencingClient;
    /**
     * Used when requesting to add or remove geofences.
     */
    public PendingIntent mGeofencePendingIntent = null;


    public PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String PREFERENCE_KEY = "GEOFENCING_APP";
    private FusedLocationProviderClient fusedLocationClient;

    private ArrayList<POIBean> BAY_AREAS_ARRAYLIST = new ArrayList<>();

    private ArrayList<Geofence> mGeofenceList = new ArrayList<>();

    public SharedPreferences getSharedPreference() {
        sharedPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        return sharedPreferences;
    }

    public SharedPreferences.Editor getEditor() {
        return editor;
    }

    public void registerGeofencingClient() {
        mGeofencingClient = LocationServices.getGeofencingClient(context);
    }

    public DBManager getDB(){
        DBManager dbManager = new DBManager(context);
        dbManager.open();
        return dbManager;
    }

    public ArrayList<Geofence> getGeofenceList(){
        return mGeofenceList;
    }

    public void initLocationUpdate(final boolean isFromActivity){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            getEditor().putString("last_lat", String.valueOf(location.getLatitude()));
                            getEditor().putString("last_lng", String.valueOf(location.getLongitude()));
                            getEditor().commit();
                            BAY_AREAS_ARRAYLIST = getLocations(isFromActivity);
                            if(isFromActivity){
                                populateGeofenceList();
                            }
                            if(!isFromActivity){
                                addGeofences(isFromActivity);
                            }
                        }
                    }
                });

    }

   /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    public ArrayList<POIBean> getLocations(boolean isFromActivity){
        if(!isFromActivity) {
            getDB().deleteAll();
            getDB().insert("New place AGH", 16.415549, 81.621073);
            getDB().insert("New place Hotel", 16.41307, 81.6066007);
            getDB().insert("New place Market", 16.412576, 81.606477);
        }
        SharedPreferences sharedPreferences = getSharedPreference();
        Cursor cursor = getDB().fetchNearest(Double.parseDouble(sharedPreferences.getString("last_lat","16.409169")),Double.parseDouble(sharedPreferences.getString("last_lng","81.588186")));
        if ((cursor != null) && (cursor.getCount() > 0)){
            cursor.moveToFirst();
            do {
                int index0 = cursor.getColumnIndex(DatabaseHelper._ID);
                int index1 = cursor.getColumnIndex(DatabaseHelper.NAME);
                int index2 = cursor.getColumnIndex(DatabaseHelper.LAT);
                int index3 = cursor.getColumnIndex(DatabaseHelper.LNG);

                int ID = cursor.getInt(index0);
                String name = cursor.getString(index1);
                Double lat = cursor.getDouble(index2);
                Double lng = cursor.getDouble(index3);
                POIBean object = new POIBean(ID,name,lat,lng);
                BAY_AREAS_ARRAYLIST.add(object);
            }while (cursor.moveToNext());
            POIBean currentLocation = new POIBean(-1,"myplace",Double.parseDouble(sharedPreferences.getString("last_lat","16.40916")),Double.parseDouble(sharedPreferences.getString("last_lng","81.588186")));
            BAY_AREAS_ARRAYLIST.add(currentLocation);
            populateGeofenceList();

        }
        return BAY_AREAS_ARRAYLIST;
    }

    public void populateGeofenceList() {
        if(!BAY_AREAS_ARRAYLIST.isEmpty()) {
            double startLat = BAY_AREAS_ARRAYLIST.get(BAY_AREAS_ARRAYLIST.size() - 1).getLat();
            double startLng = BAY_AREAS_ARRAYLIST.get(BAY_AREAS_ARRAYLIST.size() - 1).getLng();
            double endLat = BAY_AREAS_ARRAYLIST.get(BAY_AREAS_ARRAYLIST.size() - 2).getLat();
            double endLng = BAY_AREAS_ARRAYLIST.get(BAY_AREAS_ARRAYLIST.size() - 2).getLng();
            Location currentLocation = new Location("locationA");
            currentLocation.setLatitude(startLat);
            currentLocation.setLongitude(startLng);
            Location destination = new Location("locationB");
            destination.setLatitude(endLat);
            destination.setLongitude(endLng);
            float circle_radius = currentLocation.distanceTo(destination) - 30.48f; //meters
            if(circle_radius<0){
             return;
            }
            for (POIBean bean :
                    BAY_AREAS_ARRAYLIST) {
                String name = bean.getName();
                Double lat = bean.getLat();
                Double lng = bean.getLng();
                mGeofenceList.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(name)
                        // Set the circular region of this geofence.
                        .setCircularRegion(
                                lat,
                                lng,
                                name.matches("myplace") ? circle_radius : Constants.GEOFENCE_RADIUS_IN_METERS
                        )
                        // Set the expiration duration of the geofence. This geofence gets automatically
                        // removed after this period of time.
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                        .setTransitionTypes(name.matches("myplace") ? Geofence.GEOFENCE_TRANSITION_EXIT : Geofence.GEOFENCE_TRANSITION_ENTER)
                        // Create the geofence.
                        .build());
            }
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    public PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    /**
     * Returns true if geofences were added, otherwise false.
     */
    /**
     * Returns true if geofences were added, otherwise false.
     */
    public boolean getGeofencesAdded() {
        return sharedPreferences.getBoolean(
                "isAdded", false);
    }

    /**
     * Stores whether geofences were added ore removed in {@link android.content.SharedPreferences};
     *
     * @param added Whether geofences were added or removed.
     */
    public void updateGeofencesAdded(boolean added) {
        editor.putBoolean("isAdded", added).commit();
    }


    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    public void performPendingGeofenceTask(boolean isFromActivity) {
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            addGeofences(isFromActivity);
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            removeGeofences(isFromActivity);
        }
    }

    public void setPendingGeofenceTask(PendingGeofenceTask pendingGeofenceTask){
        mPendingGeofenceTask = pendingGeofenceTask;
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    public void removeGeofences(boolean isFromActivity) {
        if(isFromActivity) {
            if (!checkPermissions()) {
                showSnackbar(context.getString(R.string.insufficient_permissions));
                return;
            }
        }
        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener((OnCompleteListener<Void>) context);
    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    public void addGeofences(boolean isFromActivity) {
        if(isFromActivity) {
            if (!checkPermissions()) {
                showSnackbar(context.getString(R.string.insufficient_permissions));
                return;
            }
            if(getGeofenceList().isEmpty()){
                showSnackbar(context.getString(R.string.no_geofence_added));
            }else {
                mGeofencingClient.addGeofences(getGeofencingRequest(),getGeofencePendingIntent())
                        .addOnCompleteListener((OnCompleteListener<Void>) context);
            }
        }else {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnCompleteListener((OnCompleteListener<Void>) context);
        }
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(getGeofenceList());

        // Return a GeofencingRequest.
        return builder.build();
    }
    /**
     * Return the current state of the permissions needed.
     */
    public boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    public void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                ((Activity)context).findViewById(android.R.id.content),
                context.getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(context.getString(actionStringId), listener).show();
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = ((Activity)context).findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

}
