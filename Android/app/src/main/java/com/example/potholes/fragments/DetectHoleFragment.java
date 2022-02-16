package com.example.potholes.fragments;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.potholes.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class DetectHoleFragment extends Fragment {


    private final String IP = "192.168.1.23";
    private final int PORT = 10000;
    private SensorManager sensorManager;
    private FusedLocationProviderClient fusedLocationClient;
    private SensorEventListener sensorEventListener;
    private Location currentLocation = null;
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
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

                float z = sensorEvent.values[2];
                if (z > limit) {
                    try {
                        sensorManager.unregisterListener(sensorEventListener);
                        LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                currentLocation = location;
                                sendHolePosition((z-limit),currentLocation);
                            }
                        });
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

    }

    private void sendHolePosition(float variation,Location currentLocation) {

        if (currentLocation != null) {
            double lat = currentLocation.getLatitude();
            double lon = currentLocation.getLongitude();

            Thread rec = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket s = new Socket(IP, PORT);
                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                        out.println("1" + lat + "*" + lon + "$" + variation + "#");
                        s.close();
                    } catch (IOException /*| InterruptedException*/ e) {
                        e.printStackTrace();
                    }
                }
            });
            rec.start();
        } else {
            //TODO:RICHIESTA GPS
        }
    }

    private void passString(String st) {
        buffer = String.copyValueOf(st.toCharArray());
    }

    private void passFloat(String st) {
        limit = Float.parseFloat(st);
    }

    private void setNoConnection() {
        checkConnection = false;
    }

    private void setConnection() {
        checkConnection = true;
    }


}