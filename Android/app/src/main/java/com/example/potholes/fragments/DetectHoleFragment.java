package com.example.potholes.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.potholes.adapter.RecyclerAdapter;
import com.example.potholes.entities.Hole;
import com.example.potholes.R;
import com.example.potholes.utils.SendHoleThread;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DetectHoleFragment extends Fragment implements SensorEventListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    private ArrayList<Hole> holeArrayList;
    private Location currentLocation;
    private final String IP = "20.126.138.164";
    private final int PORT = 80;
    private SensorManager sensorManager;
    private float limit;
    private String buffer = new String();
    private boolean checkConnection = false;
    private Hole hole;

    private RecyclerView recyclerView;
    private Geocoder geocoder;
    private List<Address> addresses;
    private Context mContext;


    // Inizializza il context dal onAttach
    //Se si andava indietro e si ritornare nella rilevazione il context diventava null
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        geocoder = new Geocoder(mContext, Locale.ITALY);
        return inflater.inflate(R.layout.fragment_detect_hole, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponents();
        setUpViewComponents(view);


    }

    private void setupComponents() {
        holeArrayList = new ArrayList<>();
        limit = getArguments().getFloat("limit");
        sensorManager = (SensorManager) mContext.getSystemService(getActivity().SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }


    private void setUpViewComponents(View view) {
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float z = sensorEvent.values[2];
        if (z > limit) {
            try {

                Task<Location> taskLocation = LocationServices.getFusedLocationProviderClient(mContext).getLastLocation();
                taskLocation.addOnCompleteListener(task -> {
                    //sensorManager.unregisterListener(this);
                    currentLocation = task.getResult();
                    if (currentLocation != null) {
                        sendHolePosition((z - limit), currentLocation);

                        try {
                            addresses = geocoder.getFromLocation(currentLocation.getLatitude(),currentLocation.getLongitude(),1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        hole = new Hole(addresses.get(0).getAddressLine(0), z - limit);
                        holeArrayList.add(hole);
                        Log.e("Address Line", addresses.get(0).getAddressLine(0));
                        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(holeArrayList);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.setAdapter(recyclerAdapter);

                    } else {
                        Toast.makeText(getContext(), "Location null", Toast.LENGTH_SHORT).show();
                    }

                });


            } catch (SecurityException e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void sendHolePosition(float variation, Location currentLocation) {
        if (currentLocation != null) {
            Thread rec = new Thread(new SendHoleThread(currentLocation, variation));
            rec.start();
        } else {
            //TODO:RICHIESTA GPS
        }
    }





}