package com.example.potholes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.potholes.R;
import com.example.potholes.entities.Hole;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private ArrayList<Hole> holeArrayList;

    public RecyclerAdapter(ArrayList<Hole> holeArrayList) {
        this.holeArrayList = holeArrayList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView address;
        private TextView variation;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.address_value);
            variation = itemView.findViewById(R.id.variation_value);

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
        String address = holeArrayList.get(position).getAddress();
        Double variation = holeArrayList.get(position).getVariation();

        holder.address.setText(address);
        holder.variation.setText(new DecimalFormat("#.####").format(variation));
    }

    @Override
    public int getItemCount() {
        return holeArrayList.size();
    }



}
