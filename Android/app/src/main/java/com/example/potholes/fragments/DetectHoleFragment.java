package com.example.potholes.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.potholes.utils.MySensorEventListener;
import com.example.potholes.R;


public class DetectHoleFragment extends Fragment {


    private final String IP = "20.126.138.164";
    private final int PORT = 80;
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    private float limit;
    private String buffer = new String();
    boolean checkConnection = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        limit = getArguments().getFloat("limit");
        return inflater.inflate(R.layout.fragment_rileva_buche, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponents();
    }

    private void setupComponents() {
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensorEventListener = new MySensorEventListener(sensorManager, getContext(), limit);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

}