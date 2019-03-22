package com.learning.aman.mapapi.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.learning.aman.mapapi.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailsActivity extends AppCompatActivity {

    private RecyclerView mUserDetails;
    private DatabaseReference mDatabase;

    private TextView mTotalDistance, mTotalTime, mAverageSpeed;
    private String totalDistance, totalTime;
    private double mAvgSpeed = 0, totaldistance = 0;

    private static DecimalFormat decimalFormat = new DecimalFormat("##.##");

    private static final String TAG = "DetailsActivity";
       @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

           mTotalDistance = findViewById(R.id.totalDistance);
           mTotalTime = findViewById(R.id.totalTime);
           mAverageSpeed = findViewById(R.id.avgSpeed);

           totalDistance = getIntent().getStringExtra("Distacne");
           totalTime = getIntent().getStringExtra("Time");
           Log.e(TAG, totalDistance+" - totalDistance | totalTime - "+totalTime);
           totaldistance = Double.parseDouble(totalDistance) / 1000;
           mTotalDistance.setText(String.valueOf(decimalFormat.format(totaldistance)+" Km"));
           mTotalTime.setText(totalTime);

           try
           {
               SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
               Date Date1 = format.parse(totalTime);
               Date Date2 = format.parse("00:00:00");

               long millse = Date1.getTime() - Date2.getTime();
               long mills = Math.abs(millse);

               mAvgSpeed = Double.parseDouble(totalDistance) / ((double) mills/1000); // mAvgSpeed is in meter / sec
               mAvgSpeed = (mAvgSpeed * 60 * 60) / 1000;  // mAvgSpeed is in km / h
               Log.e(TAG, mAvgSpeed+" - AvgSpeed");


               mAverageSpeed.setText(String.valueOf(decimalFormat.format(mAvgSpeed)+" Km/h"));

               Log.e(TAG,"Time In SEC - "+mills/1000);
           }
           catch (Exception e)
           {

           }

       }
}
