package com.learning.aman.mapapi;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class UserListActivity extends AppCompatActivity {

    private RecyclerView mUserList;
    private DatabaseReference mDatabase;

    private static final String TAG = "UserListActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        mUserList = (RecyclerView) findViewById(R.id.user_recyclerView);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onStart() {
        super.onStart();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        Query query = mDatabase.orderByChild("name");

        FirebaseRecyclerOptions<UserModel> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class)
                .build();

        FirebaseRecyclerAdapter<UserModel, UserModelViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<UserModel, UserModelViewHolder>(firebaseRecyclerOptions) {

                    @NonNull
                    @Override
                    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(getApplicationContext())
                                .inflate(R.layout.single_item, null, false);
                        return new UserModelViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, final int position, @NonNull final UserModel model) {

                        holder.setName(model.getName());

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String uid = getRef(position).getKey().toString();
                                Log.e(TAG,"UID - "+uid);

                                Toast.makeText(UserListActivity.this, "Namr = "+model.getName(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(UserListActivity.this, MapsActivity.class);
                                intent.putExtra("UID", uid);
                                    startActivity(intent);
                                finish();
                            }
                        });
                    }
                };

        mUserList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private class UserModelViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {

            TextView userName = (TextView) mView.findViewById(R.id.user_name);
            userName.setText(name);

        }
    }
}
