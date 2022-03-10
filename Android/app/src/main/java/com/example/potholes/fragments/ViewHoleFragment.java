package com.example.potholes.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.potholes.R;
import com.example.potholes.adapter.RecyclerAdapter;
import com.example.potholes.entities.Hole;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//TODO:Interfaccia grafica e logica
public class ViewHoleFragment extends Fragment {

    private ArrayList<Hole> holeArrayList;
    private ArrayList<Hole> holeArrayList_withAddress;
    private Context mContext;
    private RecyclerView recyclerView;
    private Geocoder geocoder;
    private List<Address> addressList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        holeArrayList_withAddress = new ArrayList<>();
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        return inflater.inflate(R.layout.fragment_view_hole, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpViewComponents(view);

        Bundle bundle = getArguments();
        holeArrayList = bundle.getParcelableArrayList("hole");

        for (int i = 0;i<holeArrayList.size();i++){
            try {
                addressList = geocoder.getFromLocation(holeArrayList.get(i).getLat(),holeArrayList.get(i).getLon(),1);
                Hole hole1 = new Hole(addressList.get(0).getAddressLine(0),holeArrayList.get(i).getVar());
                holeArrayList_withAddress.add(hole1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(holeArrayList_withAddress);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);




    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    private void setUpViewComponents(View view) {
        recyclerView = view.findViewById(R.id.recyclerview_ViewHole);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }



}