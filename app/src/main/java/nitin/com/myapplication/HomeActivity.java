/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nitin.com.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

/**
 * Demonstrates how to create and remove geofences using the GeofencingApi. Uses an IntentService
 * to monitor geofence transitions and creates notifications whenever a device enters or exits
 * a geofence.
 * <p>
 * This sample requires a device's Location settings to be turned on. It also requires
 * the ACCESS_FINE_LOCATION permission, as specified in AndroidManifest.xml.
 * <p>
 */
public class HomeActivity extends AppCompatActivity implements OnCompleteListener<Void> {

    private static final String TAG = HomeActivity.class.getSimpleName();

    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private DBManager dbManager;

    private RecyclerView recyclerView;

    private MyAdapter adapter;

    // Buttons for kicking off the process of adding or removing geofences.
    private Button mAddGeofencesButton;
    private Button mRemoveGeofencesButton,mAddRowsButton,mDeleteRowsButton;

    private ArrayList<POIBean> LOCATION_ARRAYLIST = new ArrayList<>();


    private GeoFenceCommon geoFenceCommon;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_emp_list);

        geoFenceCommon = new GeoFenceCommon(this);
        // Get the UI widgets.
        recyclerView = (RecyclerView) findViewById(R.id.list_view);
        mAddGeofencesButton = (Button) findViewById(R.id.add_geofences_button);
        mRemoveGeofencesButton = (Button) findViewById(R.id.remove_geofences_button);
        mAddRowsButton = (Button) findViewById(R.id.add_rows);
        mDeleteRowsButton = (Button) findViewById(R.id.delete_rows);

        mAddGeofencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addGeofencesButtonHandler();
            }
        });
        mRemoveGeofencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeGeofencesButtonHandler();
            }
        });
        mDeleteRowsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                geoFenceCommon.getDB().deleteAll();
                LOCATION_ARRAYLIST.clear();
                adapter.notifyDataSetChanged();
            }
        });
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


        setButtonsEnabledState();

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        LOCATION_ARRAYLIST.addAll(geoFenceCommon.getLocations(true));
        adapter = new MyAdapter(this,LOCATION_ARRAYLIST);
        recyclerView.setAdapter(adapter);
        geoFenceCommon.initLocationUpdate(true);
        geoFenceCommon.registerGeofencingClient();
    }
    @Override
    public void onStart() {
        super.onStart();

        if (!geoFenceCommon.checkPermissions()) {
            requestPermissions();
        } else {
            geoFenceCommon.performPendingGeofenceTask(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.add_record) {

            Intent add_mem = new Intent(this, AddStoreActivity.class);
            startActivity(add_mem);

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void addGeofencesButtonHandler() {
        if (!geoFenceCommon.checkPermissions()) {
            geoFenceCommon.setPendingGeofenceTask(GeoFenceCommon.PendingGeofenceTask.ADD);
            requestPermissions();
            return;
        }
        geoFenceCommon.addGeofences(true);
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofencesButtonHandler() {
        if (!geoFenceCommon.checkPermissions()) {
            geoFenceCommon.setPendingGeofenceTask(GeoFenceCommon.PendingGeofenceTask.REMOVE);
            requestPermissions();
            return;
        }
        geoFenceCommon.removeGeofences(true);
    }

    /**
     * Runs when the result of calling {#addGeofences()} and/or {#removeGeofences()}
     * is available.
     * @param task the resulting Task, containing either a result or error.
     */
    @Override
    public void onComplete(@NonNull Task<Void> task) {
       geoFenceCommon.setPendingGeofenceTask(GeoFenceCommon.PendingGeofenceTask.NONE);
        if (task.isSuccessful()) {
            geoFenceCommon.updateGeofencesAdded(!geoFenceCommon.getGeofencesAdded());
            setButtonsEnabledState();

            int messageId = geoFenceCommon.getGeofencesAdded() ? R.string.geofences_added :
                    R.string.geofences_removed;
            Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);
            Toast.makeText(this, "Error Occurred. Check Whether Location is enabled.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Ensures that only one button is enabled at any time. The Add Geofences button is enabled
     * if the user hasn't yet added geofences. The Remove Geofences button is enabled if the
     * user has added geofences.
     */
    private void setButtonsEnabledState() {
        if (geoFenceCommon.getGeofencesAdded()) {
            mAddGeofencesButton.setEnabled(false);
            mRemoveGeofencesButton.setEnabled(true);
        } else {
            mAddGeofencesButton.setEnabled(true);
            mRemoveGeofencesButton.setEnabled(false);
        }
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            geoFenceCommon.showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(HomeActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.");
                geoFenceCommon.performPendingGeofenceTask(true);
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                geoFenceCommon.showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                geoFenceCommon.setPendingGeofenceTask(GeoFenceCommon.PendingGeofenceTask.NONE);
            }
        }
    }
}
