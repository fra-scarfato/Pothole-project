package com.example.potholes.fragments;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import java.util.ArrayList;


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



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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
        sensorManager = (SensorManager) getContext().getSystemService(getActivity().SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }


    private void setUpViewComponents(View view ) {
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float z = sensorEvent.values[2];
        if (z > limit) {
            try {
                sensorManager.unregisterListener(this);
                Task<Location> taskLocation = LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation();
                taskLocation.addOnCompleteListener(task -> {
                    currentLocation = task.getResult();
                    if (currentLocation != null){
                        sendHolePosition((z - limit), currentLocation);
                        //TODO:Passare l'username con le shared preferences
                        hole = new Hole("username",currentLocation.getLatitude(),currentLocation.getLongitude(),z-limit);
                        holeArrayList.add(hole);
                        //todo adapter
                        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(holeArrayList);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.setAdapter(recyclerAdapter);

                    }else{
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