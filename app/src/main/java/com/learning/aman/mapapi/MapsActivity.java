package com.learning.aman.mapapi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.learning.aman.mapapi.PrefrenceManager.PrefManager;
import com.learning.aman.mapapi.service.TraceService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    //These are map related Instances
    private LocationRequest mLocationRequest;
    private static final int REQUEST_FINE_LOCATION = 100;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private GoogleMap mMap;
    private LatLng MyLocation, MyLastLocation, mUserLocation, lastLocation;
    private Marker MyLocationMarker, mUserMarker;

    private int distanceCount = 0, mRuntasticDistance = 0, j = 1, x = 500, k = 10;
    private int []z = new int[11];

    // Instance of  int and string value for pickUpExactTimeDistance()
    int   previousDistance = 0,
            afterwardDistance = 0,
            distanceDifference = 0,
            leftDistance = 0,
            timeForLeftDistance = 0;
    String previousTime, afterwardTime, timeAtZDistance;
    String lat = null, lng = null ;

    //DrawerLayout , ActionBar , Navigation & Toolbar Instance
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView DrawerNavigation;

    //Firebase & Geofire instance
    private DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference();
    private GeoFire mGeoFire;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Button mStartActivity, mEndActivity;
    private ImageView mGPSLocation;
    private TextView mDistance, mTime;
    private LinearLayout runtasticLayout, mStopwatch;
    private String userID, uid, distance, duration, mainTime;
    private boolean polyLine = false, runtastic = false, timer = false, setTimeDistanceMarker = true;

    //Instaces for Stopwatch
    private Chronometer chronometer;
    private Button startBtn, pauseBtn, resetBtn;
    private long stopTime = 0;

    //SharedPrefrence
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        prefManager = new PrefManager(this);
        //init() & setUpNavigationDrawer() use for make drawer and Listner on menu Select
        init();
        setUpNavigationDrawer();

        mGPSLocation = (ImageView) findViewById(R.id.locateMe);
        mDistance = findViewById(R.id.runtastic_distance);
        runtasticLayout = findViewById(R.id.runtastic);
        mStopwatch = findViewById(R.id.stopwatch);
        mStartActivity = findViewById(R.id.runtastic_startMainActivity);
        mEndActivity = findViewById(R.id.runtastic_endMainActivity);
        startBtn = findViewById(R.id.runtastic_startActivity);
        pauseBtn = findViewById(R.id.runtastic_pauseActivity);
        resetBtn = findViewById(R.id.runtastic_resetActivity);

        for(int i = 1; i <= k ; i++){
            z[i] = x * i;
            Log.e(TAG, i+" - Index | Value - "+z[i]);

        }

//        try
//        {
//            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
//            Date Date1 = format.parse("00:11:24");
//            Date Date2 = format.parse("00:01:47");
//
//            long millse = Date1.getTime() - Date2.getTime();
//            long mills = Math.abs(millse);
//
//
//            int h = (int) (mills/(1000 * 60 * 60));
//            int m = (int) (mills/(1000*60)) % 60;
//            long s = (int) (mills / 1000) % 60;
//
//            String hh = h < 10 ? "0"+h: h+"";
//            String mm = m < 10 ? "0"+m: m+"";
//            String ss = s < 10 ? "0"+s: s+"";
//
//
//
//            String diff = hh+":"+mm+":"+ss;
//            Log.e(TAG,"mills - "+mills/1000+"\nmillse - "+millse/1000+"\nDiff - "+diff);
//
//            try{
//                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
//                Date d = df.parse(diff);
//                Calendar cal = Calendar.getInstance();
//                cal.set(Calendar.HOUR_OF_DAY, d.getHours());
//                cal.setTime(d);
//                cal.add(Calendar.SECOND, 10);
//                String newTime = df.format(cal.getTime());
//
//                Log.e(TAG,"addTimeToPrevious TEST - "+newTime);
//
//
//            }catch (Exception e){
//
//            }
//        }
//        catch (Exception e)
//        {
//
//        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.i(TAG,"onMapReady");

        if (checkPermissions()) {
            googleMap.setMyLocationEnabled(false);
            startLocationUpdates();
            getLastLocation();
            getUserLocation();
        }

        mGPSLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MyLocation, 18.5f));
                }else{
                    showGPSDisabledAlertToUser();
                }
            }
        });


    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        Log.e(TAG,"startLocationUpdates");

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                        MyLastLocation = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());

                    }
                },
                Looper.myLooper());
    }

    public void getLastLocation() {
        Log.e(TAG,"getLastLocation");
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            Log.i(TAG,"getLastLocation");
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    private void getUserLocation() {   // getting error her in geting UserLocation - showing lat/lng: (0.0,0.0)
        if(userID != null && !userID.equals(uid)){

            Log.e(TAG,uid+" - getUserLocation - "+userID);
            myDatabase.child("Locations").child(userID).child("You").child("l").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double lat = 0 , lng = 0;

                    String latitude = dataSnapshot.child("0").getValue().toString();
                    String longitude = dataSnapshot.child("1").getValue().toString();
                    Log.e(TAG,latitude+" --- "+longitude);

                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        String key = child.getKey().toString();  // Key Fatch User Lat & Long from Firebase Structure
                        String value = child.getValue().toString();

                        Log.e("Data2",key+" = "+value);
                        if(key.equals("0")) {
                                        Log.e("Data3","Lat = "+value);
                            lat = Double.parseDouble(value);
                        }
                        if(key.equals("1")) {
                                        Log.e("Data3","Long = "+value);
                            lng = Double.parseDouble(value);
                        }
                    }
                    mUserLocation = new LatLng(lat,lng);
                    addUserLocationMarker(MyLocation, mUserLocation);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void onLocationChanged(Location location) {
//        int x = 10;
        Log.i(TAG,"onLocationChanged");
        MyLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (MyLocation != null) {
            addMyLocationMarker();
            mGeoFire.setLocation("You", new GeoLocation(MyLocation.latitude, MyLocation.longitude),
                    new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            myQueries();
                        }
                    });
            if(runtastic){
                drawMyPolylines(MyLocation, MyLastLocation);
                mRuntasticDistance = distanceCount / 1000;
                mDistance.setText(String.valueOf(distanceCount));

                pickUpApproxPreviousTimeDistance(z[j]);
                if(pickUpExactTimeDistance(z[j])){
                    pickUpApproxAfterTimeDistance(z[j]);
                }
//                Toast.makeText(this, j+" - J \n"
//                        +previousDistance +" - previousDistance \n"
//                        +previousTime+" - previousTime \n"
//                        +afterwardDistance+" - afterwardDistance \n"
//                        +afterwardTime+" - afterwardTime \n"
//                        +leftDistance+" - leftDistance", Toast.LENGTH_SHORT).show();

                if(afterwardDistance != 0 && afterwardTime != null){
                    distanceDifference = afterwardDistance - previousDistance;

                    timeForLeftDistance = ( findTimeDifference(previousTime, afterwardTime) * leftDistance) / distanceDifference;
                    timeAtZDistance = addTimeToPrevious(previousTime, timeForLeftDistance);

                    if(timeAtZDistance != null){
//                        Toast.makeText(this, "Time Required For  Exact Point - "+timeAtZDistance, Toast.LENGTH_SHORT).show();
                        setTimeDistanceMarker(timeAtZDistance, z[j-1], lat, lng);

                    }

                }
            }

        } else {
            // Log.i(Tag,"MyLocation is null");
        }


    }

    private void addMyLocationMarker() {
        Log.e(TAG,"addMyLocationMarker");

        if (MyLocationMarker == null) {
            MyLocationMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.c))
                    .position(MyLocation)
                    .title("You are here"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MyLocation,14.5f));

//             Log.i(TAG,MyLocation.latitude+" - MyLocation - "+MyLocation.longitude);
        } else {
            MarkerAnimation.animateMarkerToICS(MyLocationMarker, MyLocation, new LatLngInterpolator.Spherical());
            // Log.i(Tag,"MyLocationMarker updated");
        }
    }

    private void addUserLocationMarker(LatLng myLocation, LatLng mUserLocation) {

        Location locationA = new Location("Point A");
        locationA.setLatitude(myLocation.latitude);
        locationA.setLongitude(myLocation.longitude);

        Location locationB = new Location("Point B");
        locationB.setLatitude(mUserLocation.latitude);
        locationB.setLongitude(mUserLocation.longitude);

        if(mUserMarker == null){
            mUserMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .position(mUserLocation)
                    .title("Your Friend")
                    .snippet("Distance "+new DecimalFormat("#.#").format((locationA.distanceTo(locationB) / 1000))+ " KM"));
        }
        else {
            MarkerAnimation.animateMarkerToICS(mUserMarker, mUserLocation, new LatLngInterpolator.Spherical());
        }

        Log.e("addUserLocationMarker","User Location = "+mUserLocation);
//        mMap.clear();   //Clear and set up map again
//        MyLocationMarker = null;
//        addMyLocationMarker();
        drawPolylines(myLocation, mUserLocation);  //whenever need to draw line between nodes Just use this methohd
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_FINE_LOCATION);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startLocationUpdates();

            } else {

            }
        }


    }

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void stopWatch() {

        chronometer = (Chronometer)findViewById(R.id.chronometer);
        pauseBtn.setVisibility(View.GONE);
        mStartActivity.setVisibility(View.GONE);
        mEndActivity.setVisibility(View.VISIBLE);
        mStopwatch.setVisibility(View.VISIBLE);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.setBase(SystemClock.elapsedRealtime() + stopTime);
                chronometer.start();
                timer = true;
                runtastic = true;

                startService();

                startBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.VISIBLE);

            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTime = chronometer.getBase() - SystemClock.elapsedRealtime();
                chronometer.stop();
                startBtn.setVisibility(View.VISIBLE);
                pauseBtn.setVisibility(View.GONE);
                timer = false;
                runtastic = false;

                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                chronometer.stop();
                int h   = (int)(time /3600000);
                int m = (int)(time - h*3600000)/60000;
                int s= (int)(time - h*3600000- m*60000)/1000 ;
                String hh = h < 10 ? "0"+h: h+"";
                String mm = m < 10 ? "0"+m: m+"";
                String ss = s < 10 ? "0"+s: s+"";
                mainTime = hh+":"+mm+":"+ss;
//                if(chronometer.getText().toString().equals("00:05")){
//
//                    Toast.makeText(MapsActivity.this, "chronometer", Toast.LENGTH_SHORT).show();
//
//                }
                if(distanceCount > 1) {

                    Toast.makeText(MapsActivity.this, distanceCount + " - Distance | pauseBtn | Time - " + mainTime, Toast.LENGTH_SHORT).show();
                }
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();

                j = 1;
                stopTime = 0;
                distanceCount = 0;
                timer = true;
                runtastic = true;

                mMap.clear();   //Clear and set up map again
                MyLocationMarker = null;
                addMyLocationMarker();


                mDistance.setText(String.valueOf(distanceCount));
                startBtn.setVisibility(View.VISIBLE);
                pauseBtn.setVisibility(View.GONE);
            }
        });
        mEndActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                j = 1;
                timePicker();
//                Log.e(TAG, "totalTime - "+mainTime);

                Intent mEndAcitvityIntent = new Intent(MapsActivity.this, DetailsActivity.class);
                mEndAcitvityIntent.putExtra("Distacne", String.valueOf(distanceCount));
                mEndAcitvityIntent.putExtra("Time", mainTime);
                startActivity(mEndAcitvityIntent);

                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();

                stopService();

                timer = false;
                runtastic = false;

                mMap.clear();   //Clear and set up map again
                MyLocationMarker = null;
                addMyLocationMarker();

                startBtn.setVisibility(View.VISIBLE);
                pauseBtn.setVisibility(View.GONE);
                stopTime = 0;
                distanceCount = 0;
//                Toast.makeText(MapsActivity.this, "Wait", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(MapsActivity.this, DetailsActivity.class));
            }
        });
    }

    private boolean pickUpExactTimeDistance(int i){

        if(distanceCount % 500 == 0 && distanceCount != 0){
            timePicker();

            lat = String.valueOf(MyLocation.latitude);
            lng = String.valueOf(MyLocation.longitude);

            if(setTimeDistanceMarker){
                setTimeDistanceMarker(mainTime, distanceCount, lat, lng);
                setTimeDistanceMarker = false;
            }
            j++;
            return false;
        }
        else {
            setTimeDistanceMarker = true;
        }
        return true;
    }

    private void pickUpApproxPreviousTimeDistance(int i){

        int fivePercentOfZ , tenPercentOfZ , twelvePercentOfZ ;
        fivePercentOfZ = (x * 10) /100;
        tenPercentOfZ = (x * 25) /100;
        twelvePercentOfZ = (x * 50) /100;

        if((i - twelvePercentOfZ) < distanceCount && i > distanceCount){
            previousDistance = distanceCount;
            timePicker();
            previousTime = mainTime;
            leftDistance = i - distanceCount;

            if((i - tenPercentOfZ) <= distanceCount && i > distanceCount){
                previousDistance = distanceCount;
                timePicker();
                previousTime = mainTime;
                leftDistance = i - distanceCount;

                if((i - fivePercentOfZ) <= distanceCount && i > distanceCount){

                    previousDistance = distanceCount;
                    timePicker();
                    previousTime = mainTime;
                    leftDistance = i - distanceCount;
                }
            }
        }
    }

    private void pickUpApproxAfterTimeDistance(int i){
        int fivePercentOfZ , tenPercentOfZ , twelvePercentOfZ ;
        fivePercentOfZ = (x * 10) /100;
        tenPercentOfZ = (x * 25) /100;
        twelvePercentOfZ = (x * 50) /100;

        if(((i + fivePercentOfZ) >= distanceCount && i < distanceCount)){
            afterwardDistance = distanceCount;
            timePicker();
            afterwardTime = mainTime;

            lat = String.valueOf(MyLocation.latitude);
            lng = String.valueOf(MyLocation.longitude);

            j++;
        }
        else if(((i + tenPercentOfZ) >= distanceCount && i < distanceCount)){
            afterwardDistance = distanceCount;
            timePicker();
            afterwardTime = mainTime;

            lat = String.valueOf(MyLocation.latitude);
            lng = String.valueOf(MyLocation.longitude);
            j++;
        }
        else if(((i + twelvePercentOfZ) > distanceCount && i < distanceCount)){
            afterwardDistance = distanceCount;
            timePicker();
            afterwardTime = mainTime;

            lat = String.valueOf(MyLocation.latitude);
            lng = String.valueOf(MyLocation.longitude);

            j++;
        }else{

        }

    }

    private void timePicker() {
        if(timer){
            long time = SystemClock.elapsedRealtime() - chronometer.getBase();
            int h   = (int)(time /3600000);
            int m = (int)(time - h*3600000)/60000;
            int s= (int)(time - h*3600000 - m*60000)/1000 ;
            String hh = h < 10 ? "0"+h: h+"";
            String mm = m < 10 ? "0"+m: m+"";
            String ss = s < 10 ? "0"+s: s+"";
            mainTime = hh+":"+mm+":"+ss;

            Toast.makeText(MapsActivity.this, distanceCount + " - Distance | Time Picker | Time - " + mainTime, Toast.LENGTH_LONG).show();
//            arrayList = new ArrayList<HashMap<String,String>>();
//
//            HashMap<String, String> h1 = new HashMap<String, String>();
//
//
//            h1.put("h1_key_1", String.valueOf(distanceCount));
//            h1.put("h1_key_2", "h1_value_2");
//            arrayList.add(h1);
//
//            HashMap<String, String> h2 = new HashMap<String, String>();
//            h2.put("h2_key_1", "h2_value_1");
//            h2.put("h2_key_2", "h2_value_2");
//            arrayList.add(h2);
        }
    }

    private int findTimeDifference(String previousTime, String afterwardTime){

        try
        {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            Date Date1 = format.parse(previousTime);
            Date Date2 = format.parse(afterwardTime);

            long millse = Date1.getTime() - Date2.getTime();
            long mills = Math.abs(millse);


            int h = (int) (mills/(1000 * 60 * 60));
            int m = (int) (mills/(1000*60)) % 60;
            long s = (int) (mills / 1000) % 60;

            String hh = h < 10 ? "0"+h: h+"";
            String mm = m < 10 ? "0"+m: m+"";
            String ss = s < 10 ? "0"+s: s+"";



            String diff = hh+":"+mm+":"+ss;
//            Log.e(TAG,"mills - "+mills/1000+"\nmillse - "+millse/1000+"\nDiff - "+diff);
            return (int) mills/1000;
        }
        catch (Exception e)
        {

        }
     return  0;
    }

    private String addTimeToPrevious(String previousTime, int timeForLeftDistance){
        try{
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            Date d = df.parse(previousTime);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, d.getHours());
            cal.setTime(d);
            cal.add(Calendar.SECOND, timeForLeftDistance);
            String newTime = df.format(cal.getTime());
            return newTime;

        }catch (Exception e){

        }
        return null;
    }

    private void setTimeDistanceMarker(final String mTime, final int distanceCount, String lat, String lng){

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("distance", String.valueOf(distanceCount));
        hashMap.put("time", mainTime);
        hashMap.put("lat", lat);
        hashMap.put("lng", lng);
        myDatabase.child("Runtastic").child(uid).push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MapsActivity.this, "Distance - "+distanceCount+"\nTime - "+mainTime+"\nMyLocation -" +MyLocation, Toast.LENGTH_SHORT).show();
                    previousDistance = 0;
                    previousTime = null;
                    afterwardDistance = 0;
                    afterwardTime = null;
                    leftDistance = 0;
                }
            }
        });

        Double lattitude = Double.parseDouble(lat);
        Double longitude = Double.parseDouble(lng);
        LatLng mRunstaticLocation = new LatLng(lattitude, longitude);
        Marker mRunstaticMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(mRunstaticLocation)
                .title(distanceCount+"m Completed")
                .snippet("Distance - "+distanceCount
                            +" Time - "+mTime));


    }

    public void drawMyPolylines(LatLng myLocation, LatLng myLastLocation){

        if(myLocation != null && myLastLocation != null){
            if(polyLine){
                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(myLocation.latitude, myLocation.longitude), new LatLng(myLastLocation.latitude, myLastLocation.longitude))
                        .width(12)
                        .color(Color.RED));

                Location locationA = new Location("Point A");
                locationA.setLatitude(myLocation.latitude);
                locationA.setLongitude(myLocation.longitude);

                Location locationB = new Location("Point B");
                locationB.setLatitude(MyLastLocation.latitude);
                locationB.setLongitude(MyLastLocation.longitude);
                polyLine = true;
                lastLocation = new LatLng(myLocation.latitude, myLocation.longitude);
                distanceCount = (int) (distanceCount + locationA.distanceTo(locationB));

            }else{
                if(myLocation != null && lastLocation != null){
                    Polyline line = mMap.addPolyline(new PolylineOptions()
                            .add(new LatLng(myLocation.latitude, myLocation.longitude), new LatLng(lastLocation.latitude, lastLocation.longitude))
                            .width(12)
                            .color(Color.RED));

                    Location locationA = new Location("Point A");
                    locationA.setLatitude(myLocation.latitude);
                    locationA.setLongitude(myLocation.longitude);

                    Location locationB = new Location("Point B");
                    locationB.setLatitude(lastLocation.latitude);
                    locationB.setLongitude(lastLocation.longitude);

                    distanceCount = (int) (distanceCount + locationA.distanceTo(locationB));
//                    Log.e(TAG,"drawMyPolylines Distance = "+locationA.distanceTo(locationB));
                }
                lastLocation = new LatLng(myLocation.latitude, myLocation.longitude);

            }
            Log.e(TAG,myLocation+" = MyLocation | MyLastLocation = "+lastLocation);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private void myQueries() {
//        mMap.addCircle(new CircleOptions()
//                .center(MyLocation)
//                .radius(2 * 1000) //2000 m
//                .strokeColor(Color.LTGRAY)
////                .fillColor(0x40808080)
//                .strokeWidth(5.0f));

        GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(MyLocation.latitude, MyLocation.longitude), 0.5f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.e("onKeyEntered",location + " - "+key);
            }

            @Override
            public void onKeyExited(String key) {
                Log.e("onKeyExited",""+key);

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.e("onKeyMoved","-"+key+"--"+location.latitude+"--"+location.latitude);

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e("onGeoQueryError",""+error);

            }
        });

    }

    private void drawPolylines(LatLng myLocation, LatLng mUserLocation) {

        // Getting URL to the Google Directions API
        Log.i(TAG, "drawPolylines: "+myLocation.toString()+" = "+mUserLocation.toString());

        String url = getDirectionsUrl(myLocation, mUserLocation);
        Log.e(TAG, "URL - "+url);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }



    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
                distance = parser.distance;
                duration = parser.duration;

                Log.e(TAG,distance+" = Distance || Duration = "+duration);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

// Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";
        String key = getResources().getString(R.string.key);

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key="+key;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }



    @Override
    protected void onStart() {
        super.onStart();

        uid = mAuth.getCurrentUser().getUid();
        mGeoFire = new GeoFire(myDatabase.child("Locations").child(uid));

        prefManager.setCurrentUser(uid);

        userID = getIntent().getStringExtra("UID");
        Log.e(TAG,"USER ID = "+userID);

        if(uid == null){
            Log.e(TAG,"Not Logged In");
        }else {
            Log.e(TAG,"Already Login - "+uid);

        }
    }

    private void init() {
        drawerLayout = findViewById(R.id.drower);
        toolbar = findViewById(R.id.toolbar_base);
        setSupportActionBar(toolbar);
    }

    private void setUpNavigationDrawer() {
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        DrawerNavigation = findViewById(R.id.drawer_navigation);
        DrawerNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.logout:
                        setUpLogOut();
                        break;

                    case R.id.tracking:
                        setUpTracking();
                        break;

                    case R.id.runtastic:
                        setUpRuntastic();
                        break;

                    case R.id.locate_in_a_radius:
                        setUpLocateInRadius();
                        break;

                    default: break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    // Navigation Drawer ONCLICKED
    private void setUpLogOut() {
        runtasticLayout.setVisibility(View.GONE);
        mStartActivity.setVisibility(View.GONE);
        mStopwatch.setVisibility(View.GONE);
        mEndActivity.setVisibility(View.GONE);

        Toast.makeText(MapsActivity.this, "Logout", Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MapsActivity.this, LoginActivity.class));
        finish();
    }

    private void setUpTracking() {
        runtasticLayout.setVisibility(View.GONE);
        mStartActivity.setVisibility(View.GONE);
        mEndActivity.setVisibility(View.GONE);
        mStopwatch.setVisibility(View.GONE);

        runtastic = false;

        mMap.clear();   //Clear and set up map again
        MyLocationMarker = null;
        addMyLocationMarker();
        startActivity(new Intent(MapsActivity.this, UserListActivity.class));

    }

    private void setUpRuntastic() {
        runtasticLayout.setVisibility(View.VISIBLE);
        mStartActivity.setVisibility(View.VISIBLE);
        mStartActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runtastic = true;
                stopWatch();
            }
        });

        mMap.clear();   //Clear and set up map again
        MyLocationMarker = null;
        addMyLocationMarker();
        Toast.makeText(MapsActivity.this, "Runtastic is ON", Toast.LENGTH_SHORT).show();

    }

    private void setUpLocateInRadius() {
        runtasticLayout.setVisibility(View.GONE);
        mStartActivity.setVisibility(View.GONE);
        mStopwatch.setVisibility(View.GONE);
        mEndActivity.setVisibility(View.GONE);
        runtastic = false;

        mMap.clear();    //Clear and set up map again
        MyLocationMarker = null;
        addMyLocationMarker();
        Toast.makeText(MapsActivity.this, "We are working on it", Toast.LENGTH_SHORT).show();
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private void startService(){
        //startService(new Intent(getApplicationContext(),TraceService.class));
        Intent intent=new Intent(MapsActivity.this, TraceService.class);
        intent.putExtra("UID",uid);
        startService(intent);
    }

    private void stopService(){

        stopService(new Intent(MapsActivity.this, TraceService.class));
    }
}
