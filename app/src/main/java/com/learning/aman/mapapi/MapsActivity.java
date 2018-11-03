package com.learning.aman.mapapi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final String TAG = "MapsActivity";

    private LocationRequest mLocationRequest;
    private static final int REQUEST_FINE_LOCATION = 100;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private GoogleMap mMap;
    private LatLng MyLocation, MyLastLocation, mUserLocation, lastLocation;
    private Marker MyLocationMarker, mUserMarker;
    private int distanceCount = 0;

    //DrawerLayout , ActionBar , Navigation & Toolbar Instance
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView DrawerNavigation;

    //Firebase & Geofire instance
    private DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference();
    private GeoFire mGeoFire;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private ImageView mGPSLocation;
    private TextView mDistance, mTime;
    private LinearLayout time_distance;
    private String userID, uid, distance, duration;
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //init() & setUpNavigationDrawer() use for make drawer and Listner on menu Select
        init();
        setUpNavigationDrawer();

        mGPSLocation = (ImageView) findViewById(R.id.locateMe);
        mDistance = findViewById(R.id.setDistance);
        mTime = findViewById(R.id.setTime);
        time_distance = findViewById(R.id.time_distance);

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
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MyLocation, 18.5f));
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

    private void getUserLocation() {
        if(userID != null && !userID.equals(uid)){

            Log.e(TAG,uid+" - getUserLocation - "+userID);
            myDatabase.child("Locations").child(userID).child("You").child("l").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double lat = 0 , lng = 0;

                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        String key = child.getKey().toString();  // Key Fatch User Lat & Long from Firebase Structure
                        String value = child.getValue().toString();

                        Log.e("Data2",key+" = "+value);
                        if(key.equals("0")) {
//                                        Log.e("Data3","Lat = "+value);
                            lat = Double.parseDouble(value);
                        }
                        if(key.equals("1")) {
//                                        Log.e("Data3","Long = "+value);
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
            drawMyPolylines(MyLocation, MyLastLocation);
            Toast.makeText(this, "Distcane in meter = "+distanceCount+" m", Toast.LENGTH_SHORT).show();
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
                    .title("YOU"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MyLocation,14.5f));

            // Log.i(Tag,"MyLocationMarker Added");
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
                    .title("Your Friends")
                    .snippet("Distance "+new DecimalFormat("#.#").format((locationA.distanceTo(locationB) / 1000))+ " KM"));
        }
        else {
            MarkerAnimation.animateMarkerToICS(mUserMarker, mUserLocation, new LatLngInterpolator.Spherical());
        }

        Log.e("addUserLocationMarker","User Location = "+mUserLocation);

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




    public void drawMyPolylines(LatLng myLocation, LatLng myLastLocation){

        if(myLocation != null && myLastLocation != null){
            if(flag){
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
                flag = true;
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
                        Toast.makeText(MapsActivity.this, "Logout", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                        finish();
                        break;

                    case R.id.tracking:
                        startActivity(new Intent(MapsActivity.this, UserListActivity.class));
                        finish();
                        break;

                    default: break;
                }
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

}
