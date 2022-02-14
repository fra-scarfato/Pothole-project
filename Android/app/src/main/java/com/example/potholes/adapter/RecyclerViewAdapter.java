package com.example.potholes.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.potholes.R;
import com.example.potholes.entities.Hole;
import com.example.potholes.map.MapsActivity;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<Hole> holes;

    public RecyclerViewAdapter(Context context, ArrayList<Hole> holes) {
        this.context = context;
        this.holes = holes;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.adapter_item, parent, false);
        return new MyViewHolder(view);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupHolder(MyViewHolder holder, Hole hole) {
        holder.latitude.setText(String.valueOf(hole.getLatitude()));
        holder.longitude.setText(String.valueOf(hole.getLongitude()));
        holder.depth.setText(String.valueOf(hole.getDepth()));

    }



    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Hole hole = holes.get(position);
        setupHolder(holder, hole);

        holder.open_map.setOnClickListener(v -> {
            //TODO:Open map da finire
            Intent intent = new Intent(context, MapsActivity.class);
            intent.putExtra("latitude",holder.latitude.getText());
            intent.putExtra("longitude",holder.longitude.getText());
            context.startActivity(intent);

        });


    }


    public /*static */ class MyViewHolder extends RecyclerView.ViewHolder {

        TextView latitude, longitude, depth;
        Button open_map;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            latitude = itemView.findViewById(R.id.adapterItem_latitude_value);
            longitude = itemView.findViewById(R.id.adapterItem_longitude_value);
            depth = itemView.findViewById(R.id.adapterItem_hole_depth_value);
            open_map = itemView.findViewById(R.id.adapter_item_openMapBtn);


        }

    }

}
