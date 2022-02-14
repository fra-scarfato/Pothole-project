package com.example.potholes.fragments;

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
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class RilevaBucheFragment extends Fragment {


    private Handler receiveLimitHandler;
    private final String IP = "192.168.1.23";
    private final int PORT = 10000;
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private SensorEventListener sensorEventListener;
    private Location currentLocation = null;
    private float limit;
    private String buffer = new String();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_rileva_buche, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponents();
        receiveLimit();


    }

    private void setupComponents() {
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float z = sensorEvent.values[2];


                System.out.println("Z Axis: " + z);
                if (z > limit) {
                    try {
                        sensorManager.unregisterListener(sensorEventListener);
                        currentLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                        LatLng location = new LatLng(45.484483, 9.173818);
                        sendHolePosition(z - limit, location);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

    }

    private void sendHolePosition(float variation,LatLng currentLocation) {

        if (currentLocation != null) {
            System.out.println("Check1");
            double lat = currentLocation.latitude;
            double lon = currentLocation.longitude;

            Thread rec = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket s = new Socket(IP, PORT);
                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                        out.println("1" + String.valueOf(lat) + "+" + lon + "-" + variation + "#");
                        s.close();
                    } catch (IOException /*| InterruptedException*/ e) {
                        e.printStackTrace();
                    }
                }
            });
            rec.start();
        } else {
            //TODO:RICHIESTA GPS
            System.out.println("Check2");
        }
    }

    //Funzione per ricevere il valore soglia
    private void receiveLimit() {
        receiveLimitHandler = new Handler();

        Thread rec = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket(IP, PORT);
                    //Prima connessione e invio della richiesta da parte del client
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                    out.println("0");
                    //Ricezione del valore soglia
                    BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    final String limit = fromServer.readLine();
                    passString(limit);
                    passFloat(limit);
                    s.close();
                } catch (IOException /*| InterruptedException*/ e) {
                    e.printStackTrace();
                }

                receiveLimitHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "" + buffer, Toast.LENGTH_LONG).show();
                        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                    }
                });
            }
        });

        rec.start();

    }

    private void passString(String st) {
        buffer = String.copyValueOf(st.toCharArray());
    }

    private void passFloat(String st) {
        limit = Float.parseFloat(st);
    }


}