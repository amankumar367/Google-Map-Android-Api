package com.learning.aman.mapapi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Arrays;

public class DetailsActivity extends AppCompatActivity {

    private RecyclerView mUserDetails;
    private DatabaseReference mDatabase;

    private static final String TAG = "DetailsActivity";
       @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

           mUserDetails = (RecyclerView) findViewById(R.id.user_recyclerView);
           mUserDetails.setHasFixedSize(true);
           mUserDetails.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Runtastic");

        Query query = mDatabase.orderByChild("distance");

        FirebaseRecyclerOptions<DetailsModel> firebaseRecyclerOptions =
                new FirebaseRecyclerOptions.Builder<DetailsModel>()
                .setQuery(query, DetailsModel.class)
                .build();

//        FirebaseRecyclerAdapter<DetailsModel, DetailsModelVieHolder> firebaseRecyclerAdapter =
//                FirebaseRecyclerAdapter<>
    }
}
