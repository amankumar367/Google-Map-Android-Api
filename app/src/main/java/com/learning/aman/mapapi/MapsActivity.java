package com.learning.aman.mapapi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.learning.aman.mapapi.Activity.LoginActivity;
import com.learning.aman.mapapi.Activity.UserListActivity;
import com.learning.aman.mapapi.Helper.Runtastic;
import com.learning.aman.mapapi.Interfaces.LatLngInterpolator;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private static final int REQUEST_FINE_LOCATION = 100;
    private static final long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private static final long FASTEST_INTERVAL = 2000; /* 2 sec */
    private static final String PROVIDER_A ="Point A";
    private static final String PROVIDER_B ="Point B";

    public GoogleMap mMap;
    public static Marker MyLocationMarker, mUserMarker;
    public static LatLng MyLocation, MyLastLocation, mUserLocation, lastLocation;

    public static int mDistanceTravell = 0, mRuntasticDistance = 0, j = 1, x = 1000, k = 20;
    private int []z = new int[k + 2];


    //DrawerLayout , ActionBar , Navigation & Toolbar Instance
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView DrawerNavigation;

    //Firebase & Geofire instance
    private DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference();
    private GeoFire mGeoFire;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String userID, uid, distance, duration, mainTime;
    private boolean enablePolyline = true, timer = false, setTimeDistanceMarker = true;

    //SharedPrefrence
    private PrefManager prefManager;

    Polyline polyline = null;
    private PolylineOptions polylineOptions = new PolylineOptions().color(Color.RED).width(10);
    private View mRuntacticView;
    private Runtastic runtastic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();
        setUpNavigationDrawer();


        for(int i = 1; i <= k ; i++){
            z[i] = x * i;
            Log.e(TAG, i+" - Index | Value - "+z[i]);
        }

    }

    private void init() {
        mRuntacticView = findViewById(R.id.runtastic);
        drawerLayout = findViewById(R.id.drower);
        toolbar = findViewById(R.id.toolbar_base);
        setSupportActionBar(toolbar);
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
    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        Log.e(TAG,"startLocationUpdates");


        // Create the location request to start receiving updates
        LocationRequest mLocationRequest = new LocationRequest();
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

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

                    lat = Double.parseDouble(latitude);
                    lng = Double.parseDouble(longitude);

                    mUserLocation = new LatLng(lat,lng);
                    if(MyLocation != null){
                        addUserLocationMarker(MyLocation, mUserLocation);
                    }
                    else {
                        myDatabase.child("Locations").child(uid).child("You").child("l").addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                double lat = 0 , lng = 0;

                                String latitude = dataSnapshot.child("0").getValue().toString();
                                String longitude = dataSnapshot.child("1").getValue().toString();
                                Log.e(TAG,latitude+" -OWN- "+longitude);

                                lat = Double.parseDouble(latitude);
                                lng = Double.parseDouble(longitude);
                                MyLocation = new LatLng(lat, lng);
                                addUserLocationMarker(MyLocation, mUserLocation);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void onLocationChanged(Location location) {
        MyLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (MyLocation != null) {
            addMyLocationMarker(mMap);

            // Insert MyLocation in Firebase RealTime Dataease Using GeoFire
            mGeoFire.setLocation("You", new GeoLocation(MyLocation.latitude, MyLocation.longitude),
                    new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            myQueries();
                        }
                    });


            if(runtastic != null && Runtastic.isRuntasticEnabled)
                runtastic.startRuntasticProcess();
        }
    }


    public void addMyLocationMarker(GoogleMap googleMap) {
        Log.e(TAG,"addMyLocationMarker");

        if (MyLocationMarker == null) {
            MyLocationMarker = googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.c))
                    .position(MyLocation)
                    .title("You are here"));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MyLocation,14.5f));

        } else {
            MarkerAnimation.animateMarkerToICS(MyLocationMarker, MyLocation, new LatLngInterpolator.Spherical());
        }
    }

    private void addUserLocationMarker(LatLng myLocation, LatLng mUserLocation) {

        Location locationA = getLocationFromLatLng(myLocation, PROVIDER_A);
        Location locationB = getLocationFromLatLng(mUserLocation, PROVIDER_B);

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

        Log.i("addUserLocationMarker","User Location = "+mUserLocation);
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
                                enableGPS();
                            }

                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void enableGPS() {
        GoogleApiClient googleApiClient = null;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(MapsActivity.this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(
                                        MapsActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        }
    }

    public int drawMyPolylines(LatLng myLocation, LatLng myLastLocation, GoogleMap map){
        if(myLocation != null && myLastLocation != null){
            if(enablePolyline){
                polylineOptions.add(myLastLocation);
                Location locationA = getLocationFromLatLng(myLocation, PROVIDER_A);
                Location locationB = getLocationFromLatLng(myLastLocation, PROVIDER_B);
                lastLocation = new LatLng(myLocation.latitude, myLocation.longitude);
                mDistanceTravell = getDistance(locationA, locationB);

                enablePolyline = false;
            }else{
                if(lastLocation != null){
                    polylineOptions.add(lastLocation);
                    Location locationA = getLocationFromLatLng(myLocation, PROVIDER_A);
                    Location locationB = getLocationFromLatLng(lastLocation, PROVIDER_B);
                    mDistanceTravell = getDistance(locationA, locationB);
                    lastLocation = new LatLng(myLocation.latitude, myLocation.longitude);
                }
            }
        }
        if(polylineOptions != null && map != null)
            polyline = map.addPolyline(polylineOptions);
        return mDistanceTravell;
    }

    private Location getLocationFromLatLng(LatLng myLocation, String provider) {
        Location location = new Location(provider);
        location.setLatitude(myLocation.latitude);
        location.setLongitude(myLocation.longitude);
        return location;
    }

    private int getDistance(Location locationA, Location locationB) {
        return (int) (mDistanceTravell + locationA.distanceTo(locationB));
    }

    private void myQueries() {

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
                Log.e("onKeyMoved","-"+key+"--"+location.latitude+"--"+location.longitude);

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
        isGPSEnabled();

        uid = mAuth.getCurrentUser().getUid();
        mGeoFire = new GeoFire(myDatabase.child("Locations").child(uid));

        prefManager = new PrefManager(MapsActivity.this);
        prefManager.setCurrentUser(uid);

        userID = getIntent().getStringExtra("UID");
        Log.i(TAG,"USER ID = "+userID);

        if(uid == null){
            Log.e(TAG,"Not Logged In");
        }else {
            Log.i(TAG,"Already Login - "+uid);

        }
    }

    private void isGPSEnabled() {
        //Checking whether GPS is enabled or not
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        }
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
        clearMap();
        mRuntacticView.setVisibility(View.GONE);

        showMessage(MapsActivity.this,"Logout");
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MapsActivity.this, LoginActivity.class));
        finish();
    }

    private void setUpTracking() {
        clearMap();
        deleteRuntasticObject();
        mRuntacticView.setVisibility(View.GONE);

        startActivity(new Intent(MapsActivity.this, UserListActivity.class));
        finish();
    }

    private void setUpRuntastic() {
        mRuntacticView.setVisibility(View.VISIBLE);
        createRuntasticObject();

        clearMap();
        showMessage(getApplicationContext(),"Runtastic Service");
    }

    private void setUpLocateInRadius() {
        clearMap();
        deleteRuntasticObject();
        mRuntacticView.setVisibility(View.GONE);

        showMessage(getApplicationContext(),"We are working on it");
    }

    private void clearMap() {
        userID = null;
        MyLocationMarker = null;
        addMyLocationMarker(mMap);
        mMap.clear();
    }

    private void createRuntasticObject() {
        runtastic = new Runtastic(MapsActivity.this, mMap);
        runtastic.onClick();
    }

    private void deleteRuntasticObject() {
        runtastic = null;
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click Back again to exit", Toast.LENGTH_SHORT).show();

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

    public static void showMessage(Context context,String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
