package com.example.potholes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.potholes.R;
import com.example.potholes.entities.Hole;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private ArrayList<Hole> holeArrayList;

    public RecyclerAdapter(ArrayList<Hole> holeArrayList) {
        this.holeArrayList = holeArrayList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView lat;
        private TextView longitude;
        private TextView username;
        private TextView variation;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            lat = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);
            username = itemView.findViewById(R.id.username);
            variation = itemView.findViewById(R.id.variation);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_items,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String username = holeArrayList.get(position).getUsername();
        String latitude = String.valueOf(holeArrayList.get(position).getLat());
        String longitude = String.valueOf(holeArrayList.get(position).getLon());
        String variation = String.valueOf(holeArrayList.get(position).getVar());

        holder.username.setText(username);
        holder.lat.setText(latitude);
        holder.longitude.setText(longitude);
        holder.variation.setText(variation);
    }

    @Override
    public int getItemCount() {
        return holeArrayList.size();
    }



}
