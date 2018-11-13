package com.learning.aman.mapapi;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.MyViewHolder> {

    ArrayList personNames;
    Context context;
    public DetailsAdapter(Context context, ArrayList personNames) {
        this.context = context;
        this.personNames = personNames;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_time_distance, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.time.setText((CharSequence) personNames.get(position));
        holder.distance.setText((CharSequence) personNames.get(position));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, ""+personNames.get(position), Toast.LENGTH_SHORT).show();
            }
        });

    }
    @Override
    public int getItemCount() {
        return personNames.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView time, distance;

        public MyViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            time = (TextView) mView.findViewById(R.id.single_time);
            distance = (TextView) mView.findViewById(R.id.single_distance);


        }
    }

}
