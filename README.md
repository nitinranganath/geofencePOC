
## Database
GeoFencePOC is having a SQLite DB table named STORES with rows
 > ID (Auto Increment)
 > Name 
 > Latitude
 > Longitude


## Permissions
Access Location Permission will be asked on opening the app, which "Allow all the Time" to be selected to make the app run in background as well.

Some devices doing battery optimisations by default and need to select Don't optimise in the Settings -> Apps & Notifications -> App info -> Battery optimisation 

Location should be enabled to work with GeoFence.

## Adding locations

On opening the app, we have four buttons "Add Geofence", "Remove Geofence", "Add Rows", "Delete All Rows"

We can add Locations(POI) by using menu "+" button  to enter location manually. We can add more locations with "Add Rows" button which will add the locations which are given in onClickListener of that button.
```
 mAddRowsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbManager = geoFenceCommon.getDB();
                dbManager.deleteAll();
                LOCATION_ARRAYLIST.clear();
                dbManager.insert("Home", 16.409169, 81.588186);
                dbManager.insert("factory", 16.408272, 81.584704);
                dbManager.insert("market", 16.412576, 81.606477);
                dbManager.insert("Hotel", 16.41307, 81.606007);
                dbManager.insert("AGH", 16.415549, 81.621073);
                LOCATION_ARRAYLIST.addAll(geoFenceCommon.getLocations(true));
                adapter.notifyDataSetChanged();
            }
        });
```
Once the user reaches outer circle, we will clear the old geofences and add new set of POIs. This can be added in getLocations() in GeoFenceCommon.class.

```
public ArrayList<POIBean> getLocations(boolean isFromActivity){
        if(!isFromActivity) {
            getDB().deleteAll();
            getDB().insert("New place AGH", 16.415549, 81.621073);
            getDB().insert("New place Hotel", 16.41307, 81.6066007);
            getDB().insert("New place Market", 16.412576, 81.606477);
        }
  .....
  .....
  }
  ```
  ## Changing Radius of POI
  
  We can change the radius of POI in Constants file.
 
  public static final float GEOFENCE_RADIUS_IN_METERS = 984.252f; //300mts in  ft
  
  ## How it works?
  
  On Adding a set of POI rows in database table, they will be displayed on Homescreen sorted by distance from current location to all POIs and max of 19 will be shown.
  ```
  database.rawQuery( "SELECT * FROM "+DatabaseHelper.TABLE_NAME+" ORDER BY (("+location_lat+" - "+DatabaseHelper.LAT+")*("+location_lat+" - "+DatabaseHelper.LAT+")) + (("+location_lng+" - "+DatabaseHelper.LNG+")*("+location_lng+" - "+DatabaseHelper.LNG+")) ASC LIMIT 19", null );
  ```
  where location_lat and location_lng are cordiantes of current location.
  
  
Along with the 19 POIs obtained from DB, we are adding a new GEOFENCE with id = -1 and name = "myplace", latitude and logitude will be current location's coordinates in getLocations() method in GeoFenceCommon.class while registering geofences. This row is registered with Geofence.GEOFENCE_TRANSITION_EXIT, radius = distance between current location and last row(sorted by distance) location, and remaining 19 will be registered with Geofence.GEOFENCE_TRANSITION_ENTER, radius = radius mentioned in Constants file. This will be done in populateGeofenceList() method in GeofenceCommon class.


Whenever the user enters any POI, we will get a notification that he entered that place. But when he exit the outer circle, we will get a notification that he exited myplace, and deletes all geofences and database tables and adds a new set of DB rows and geofences.



## Some check points
[Points to check](https://simpleinout.helpscoutdocs.com/article/232-my-geofences-arent-working-android#:~:text=Make%20sure%20that%20the%20Geofence,doing%20it%20on%20the%20device.)
  
  
  
  
