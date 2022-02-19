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
        private TextView indirizzo;
        private TextView valore_variazione;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            indirizzo = itemView.findViewById(R.id.indirizzo);
            valore_variazione = itemView.findViewById(R.id.variazione);

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
        String indirizzo = holeArrayList.get(position).getIndirizzo();
        String valore_variazione = String.valueOf(holeArrayList.get(position).getValore_variazione());

        holder.indirizzo.setText(indirizzo);
        holder.valore_variazione.setText(valore_variazione);
    }

    @Override
    public int getItemCount() {
        return holeArrayList.size();
    }



}
