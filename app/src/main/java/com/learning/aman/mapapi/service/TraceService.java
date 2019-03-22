package com.learning.aman.mapapi.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.learning.aman.mapapi.PrefrenceManager.PrefManager;

import java.util.HashMap;

public class TraceService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // @androidx.annotation.Nullable
    private GoogleApiClient apiClient;
    private LocationRequest locationRequest ;
    private Location location;

    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";

    private static final String TAG = "TraceService";

    private String uid;
    private PrefManager prefManager;

   // private LocationRequest mLocationRequest;
    private static final int REQUEST_FINE_LOCATION = 100;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private GeoFire mGeoFire;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "onCreate");
        prefManager = new PrefManager(getApplicationContext());


                apiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        apiClient.connect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

       // String data = intent.getStringExtra("data");

        Log.e(TAG, "onStartCommand");
        uid = prefManager.getCurrentUser();
        mGeoFire = new GeoFire(mDatabase.child("Locations").child(uid));

//        Toast.makeText(this, "CurrentUser - "+uid, Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(apiClient);

        if (location != null) {
            Log.e(TAG, "LAt" + location.getLatitude() + " Long : " + location.getLongitude());
        }

        startLocationUpdates();

    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.e(TAG,"onConnectionSuspended"+i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG,"onConnectionFailed"+connectionResult.getErrorMessage());

    }

    @Override
    public void onLocationChanged(final Location location) {

        Log.e(TAG,"Lat : "+location.getLatitude()+" Longitude :"+location.getLongitude());

        mGeoFire.setLocation("You", new GeoLocation(location.getLatitude(), location.getLongitude()),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
//                        Toast.makeText(getApplicationContext(), "Lat : "+location.getLatitude()+"\nLongitude :"
//                                +location.getLongitude()
//                                +"\nID - "+uid, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
    }
}
