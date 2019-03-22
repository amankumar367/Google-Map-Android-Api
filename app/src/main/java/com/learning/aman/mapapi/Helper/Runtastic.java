package com.learning.aman.mapapi.Helper;

import android.app.Activity;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.learning.aman.mapapi.MapsActivity;
import com.learning.aman.mapapi.R;

public class Runtastic {

    private static final String TAG = Runtastic.class.getSimpleName();
    private Activity activity;
    private GoogleMap googleMap;

    private Chronometer chronometer;
    private Button mStart, mPause, mResume, mRest, mStop;
    private TextView mRuntasticDistance;
    private LinearLayout mRuntasticLayoutBtn;

    public static boolean isRuntasticEnabled = false;

    private String uniqueID;

    private long stopTime = 0;

    private MapsActivity mMapActivity;


    public Runtastic(MapsActivity activity, GoogleMap mMap) {
        this.activity = activity;
        this.mMapActivity = new MapsActivity();
        this.googleMap = mMap;
        init();
        reInitRuntastic();
    }

    private void init() {
        chronometer = activity.findViewById(R.id.chronometer);
        mRuntasticDistance = activity.findViewById(R.id.runtastic_distance);

        mStart = activity.findViewById(R.id.runtastic_start_live_running);
        mPause = activity.findViewById(R.id.runtastic_pause);
        mResume = activity.findViewById(R.id.runtastic_resume);
        mRest = activity.findViewById(R.id.runtastic_reset);
        mStop = activity.findViewById(R.id.runtastic_stop);
        mRuntasticLayoutBtn = activity.findViewById(R.id.runtastic_layout_buttons);

    }

    public void onClick(){
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
                isRuntasticEnabled = true;
                mStart.setVisibility(View.GONE);
                mStop.setVisibility(View.VISIBLE);
                mRuntasticLayoutBtn.setVisibility(View.VISIBLE);
            }
        });
        mPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseTimer();
                isRuntasticEnabled = false;
                mPause.setVisibility(View.GONE);
                mResume.setVisibility(View.VISIBLE);
            }
        });
        mResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
                isRuntasticEnabled = true;
                mResume.setVisibility(View.GONE);
                mPause.setVisibility(View.VISIBLE);
            }
        });
        mRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
                reInitRuntastic();
                isRuntasticEnabled = false;

                mResume.setVisibility(View.GONE);
                mPause.setVisibility(View.VISIBLE);
                mStart.setVisibility(View.VISIBLE);
                mStop.setVisibility(View.GONE);
                mRuntasticLayoutBtn.setVisibility(View.GONE);
            }
        });
        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
                reInitRuntastic();
                isRuntasticEnabled = false;

                mResume.setVisibility(View.GONE);
                mPause.setVisibility(View.VISIBLE);
                mStart.setVisibility(View.VISIBLE);
                mStop.setVisibility(View.GONE);
                mRuntasticLayoutBtn.setVisibility(View.GONE);
            }
        });
    }

    private void startTimer() {
        chronometer.setBase(SystemClock.elapsedRealtime() + stopTime);
        chronometer.start();
    }

    private void pauseTimer() {
        stopTime = chronometer.getBase() - SystemClock.elapsedRealtime();
        chronometer.stop();
    }

    private void resetTimer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.stop();
    }

    public void startRuntasticProcess() {
        if(isRuntasticEnabled){
            mRuntasticDistance.setText(String.valueOf(
                    mMapActivity.drawMyPolylines(MapsActivity.MyLocation,
                            MapsActivity.MyLastLocation,
                            googleMap)));

//            pickUpApproxPreviousTimeDistance(z[j]);
//            if(pickUpExactTimeDistance(z[j])){
//                pickUpApproxAfterTimeDistance(z[j]);
//            }
//
//            Log.e(TAG,j+" - J \n"
//                    +previousDistance +" - previousDistance \n"
//                    +previousTime+" - previousTime \n"
//                    +afterwardDistance+" - afterwardDistance \n"
//                    +afterwardTime+" - afterwardTime \n"
//                    +leftDistance+" - leftDistance\n"
//                    +mDistanceTravell+" - mDistanceTravell");
//
//            if(afterwardDistance != 0 && afterwardTime != null){
//                distanceDifference = afterwardDistance - previousDistance;
//
//                if(previousDistance != 0 && previousTime != null){
//                    timeForLeftDistance = ( findTimeDifference(previousTime, afterwardTime) * leftDistance) / distanceDifference;
//                    timeAtZDistance = addTimeToPrevious(previousTime, timeForLeftDistance);
//                }
//                else {
//                    timeAtZDistance = afterwardTime;
//                }
//
//                if(timeAtZDistance != null){
////                        Toast.makeText(this, "Time Required For  Exact Point - "+timeAtZDistance, Toast.LENGTH_SHORT).show();
//                    setTimeDistanceMarker(timeAtZDistance, z[j - 1], lat, lng);
//
//                }
//
//            }
        }
    }


//    private void stopWatch() {
//
//        startBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                chronometer.setBase(SystemClock.elapsedRealtime() + stopTime);
//                chronometer.start();
//                timer = true;
//                isRuntasticEnabled = true;
//
//                uniqueID = UUID.randomUUID().toString();
//
////                setTimeDistanceMarker(previousTime, z[0], String.valueOf(MyLocation.latitude), String.valueOf(MyLocation.longitude));
//
//                startBtn.setVisibility(View.GONE);
//                pauseBtn.setVisibility(View.VISIBLE);
//
//            }
//        });
//
//        pauseBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stopTime = chronometer.getBase() - SystemClock.elapsedRealtime();
//                chronometer.stop();
//                startBtn.setVisibility(View.VISIBLE);
//                pauseBtn.setVisibility(View.GONE);
//                timer = false;
//                isRuntasticEnabled = false;
//
//            }
//        });
//
//        resetBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                chronometer.setBase(SystemClock.elapsedRealtime());
//                chronometer.stop();
//
//                j = 1;
//                stopTime = 0;
//                mDistanceTravell = 0;
//                isRuntasticEnabled = false;
//
//                mMap.clear();   //Clear and set up map again
//                MyLocationMarker = null;
//                addMyLocationMarker();
//
//                mDistance.setText(String.valueOf(mDistanceTravell));
//                startBtn.setVisibility(View.VISIBLE);
//                pauseBtn.setVisibility(View.GONE);
//            }
//        });
//        mEndActivity.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                j = 1;
//                getTime();
//                if(isRuntasticEnabled){
//                    setTimeDistanceMarker(mainTime, mDistanceTravell, String.valueOf(MyLocation.latitude), String.valueOf(MyLocation.longitude));
//                }
//
//                Intent mEndAcitvityIntent = new Intent(MapsActivity.this, DetailsActivity.class);
//                mEndAcitvityIntent.putExtra("Distacne", String.valueOf(mDistanceTravell));
//                mEndAcitvityIntent.putExtra("Time", mainTime);
//                startActivity(mEndAcitvityIntent);
//
//                chronometer.setBase(SystemClock.elapsedRealtime());
//                chronometer.stop();
//
//                timer = false;
//                isRuntasticEnabled = false;
//
//                mMap.clear();   //Clear and set up map again
//                MyLocationMarker = null;
//                addMyLocationMarker();
//
//                startBtn.setVisibility(View.VISIBLE);
//                pauseBtn.setVisibility(View.GONE);
//                stopTime = 0;
//                mDistanceTravell = 0;
//                mDistance.setText(String.valueOf(mDistanceTravell));
//            }
//        });
//    }
//
//    private boolean pickUpExactTimeDistance(int i){
//
//        if(mDistanceTravell % x  == 0 && mDistanceTravell != 0){
//            getTime();
//
//            lat = String.valueOf(MyLocation.latitude);
//            lng = String.valueOf(MyLocation.longitude);
//
//            if(setTimeDistanceMarker){
//                setTimeDistanceMarker(mainTime, mDistanceTravell, lat, lng);
//                setTimeDistanceMarker = false;
//            }
//            j++;
//            return false;
//        }
//        else {
//            setTimeDistanceMarker = true;
//        }
//        return true;
//    }
//
//    private void pickUpApproxPreviousTimeDistance(int i){
//
//        int fivePercentOfZ , tenPercentOfZ , twelvePercentOfZ ;
//        fivePercentOfZ = (x * 10) /100;
//        tenPercentOfZ = (x * 25) /100;
//        twelvePercentOfZ = (x * 50) /100;
//
//        if((i - twelvePercentOfZ) < mDistanceTravell && i > mDistanceTravell){
//            previousDistance = mDistanceTravell;
//            getTime();
//            previousTime = mainTime;
//            leftDistance = i - mDistanceTravell;
//
//            if((i - tenPercentOfZ) <= mDistanceTravell && i > mDistanceTravell){
//                previousDistance = mDistanceTravell;
//                getTime();
//                previousTime = mainTime;
//                leftDistance = i - mDistanceTravell;
//
//                if((i - fivePercentOfZ) <= mDistanceTravell && i > mDistanceTravell){
//
//                    previousDistance = mDistanceTravell;
//                    getTime();
//                    previousTime = mainTime;
//                    leftDistance = i - mDistanceTravell;
//                }
//            }
//        }
//    }
//
//    private void pickUpApproxAfterTimeDistance(int i){
//        int fivePercentOfZ , tenPercentOfZ , twelvePercentOfZ ;
//        fivePercentOfZ = (x * 10) /100;
//        tenPercentOfZ = (x * 25) /100;
//        twelvePercentOfZ = (x * 50) /100;
//
//        if(((i + fivePercentOfZ) >= mDistanceTravell && i < mDistanceTravell)){
//            afterwardDistance = mDistanceTravell;
//            getTime();
//            afterwardTime = mainTime;
//
//            lat = String.valueOf(MyLocation.latitude);
//            lng = String.valueOf(MyLocation.longitude);
//
//            j++;
//        }
//        else if(((i + tenPercentOfZ) >= mDistanceTravell && i < mDistanceTravell)){
//            afterwardDistance = mDistanceTravell;
//            getTime();
//            afterwardTime = mainTime;
//
//            lat = String.valueOf(MyLocation.latitude);
//            lng = String.valueOf(MyLocation.longitude);
//            j++;
//        }
//        else if(((i + twelvePercentOfZ) > mDistanceTravell && i < mDistanceTravell)){
//            afterwardDistance = mDistanceTravell;
//            getTime();
//            afterwardTime = mainTime;
//
//            lat = String.valueOf(MyLocation.latitude);
//            lng = String.valueOf(MyLocation.longitude);
//
//            j++;
//        }
//    }
//
//    private String getTime() {
//        long time = SystemClock.elapsedRealtime() - chronometer.getBase();
//        int h   = (int)(time /3600000);
//        int m = (int)(time - h*3600000)/60000;
//        int s= (int)(time - h*3600000 - m*60000)/1000 ;
//        String hh = h < 10 ? "0"+h: h+"";
//        String mm = m < 10 ? "0"+m: m+"";
//        String ss = s < 10 ? "0"+s: s+"";
//
//        return hh+":"+mm+":"+ss;
//    }
//
//    private int findTimeDifference(String previousTime, String afterwardTime){
//
//        try
//        {
//            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
//            Date Date1 = format.parse(previousTime);
//            Date Date2 = format.parse(afterwardTime);
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
//            String diff = hh+":"+mm+":"+ss;
////            Log.e(TAG,"mills - "+mills/1000+"\nmillse - "+millse/1000+"\nDiff - "+diff);
//            return (int) mills/1000;
//        }
//        catch (Exception e)
//        {
//
//        }
//        return  0;
//    }
//
//    private String addTimeToPrevious(String previousTime, int timeForLeftDistance){
//        try{
//            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
//            Date d = df.parse(previousTime);
//            Calendar cal = Calendar.getInstance();
//            cal.set(Calendar.HOUR_OF_DAY, d.getHours());
//            cal.setTime(d);
//            cal.add(Calendar.SECOND, timeForLeftDistance);
//            String newTime = df.format(cal.getTime());
//            return newTime;
//
//        }catch (Exception e){
//
//        }
//        return null;
//    }
//
//    private void setTimeDistanceMarker(final String mTime, final int mDistanceTravell, final String lat, final String lng){
//
//        HashMap<String, String> hashMap = new HashMap<>();
//        hashMap.put("distance", String.valueOf(mDistanceTravell));
//        hashMap.put("time", mTime);
//        hashMap.put("lat", lat);
//        hashMap.put("lng", lng);
//        myDatabase.child("Runtastic").child(uid).child(uniqueID).push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(task.isSuccessful()){
//
//                    Double lattitude = Double.parseDouble(lat);
//                    Double longitude = Double.parseDouble(lng);
//                    LatLng mRunstaticLocation = new LatLng(lattitude, longitude);
//                    Marker mRunstaticMarker = mMap.addMarker(new MarkerOptions()
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                            .position(mRunstaticLocation)
//                            .title(mDistanceTravell+"m Completed")
//                            .snippet("Distance - "+mDistanceTravell
//                                    +" Time - "+mTime));
//
//                    Toast.makeText(MapsActivity.this, "Distance - "+mDistanceTravell+"\nTime - "+mainTime+"\nMyLocation -" +MyLocation, Toast.LENGTH_SHORT).show();
//                    previousDistance = 0;
//                    previousTime = null;
//                    afterwardDistance = 0;
//                    afterwardTime = null;
//                    leftDistance = 0;
//                }
//            }
//        });
//
//    }

    private void reInitRuntastic() {
        stopTime = 0;
        mRuntasticDistance.setText(activity.getString(R.string.distance_value));
        MapsActivity.mDistanceTravell = 0;
        MapsActivity.MyLocationMarker = null;
        mMapActivity.addMyLocationMarker(googleMap);
        clearMap();
    }

    private void clearMap() {
        googleMap.clear();
    }
}
