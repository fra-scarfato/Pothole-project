package com.example.potholes;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

public class MySensorEventListener implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Context context;
    private final float limit;
    private Location currentLocation;

    public MySensorEventListener(SensorManager sensorManager, Context context, float limit) {
        this.sensorManager = sensorManager;
        this.context = context;
        this.limit = limit;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float z = sensorEvent.values[2];
        if (z > limit) {
            try {
                sensorManager.unregisterListener(this);
                Task<Location> taskLocation = LocationServices.getFusedLocationProviderClient(context).getLastLocation();
                taskLocation.addOnSuccessListener(location -> {
                    currentLocation = location;
                    sendHolePosition((z-limit),currentLocation);
                });
                /*taskLocation.addOnCompleteListener(task -> {
                    currentLocation = task.getResult();
                    sendHolePosition((z-limit),currentLocation);
                });*/
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void sendHolePosition(float variation,Location currentLocation) {
        if (currentLocation != null) {
            Thread rec = new Thread(new SendHoleThread(currentLocation, variation));
            rec.start();
        } else {
            //TODO:RICHIESTA GPS
        }
    }
}
