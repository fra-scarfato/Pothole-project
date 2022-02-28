package com.example.potholes.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.potholes.HomeActivity;
import com.example.potholes.R;
import com.example.potholes.map.MapsActivity;
import com.example.potholes.utils.ReceiveLimitThread;
import com.example.potholes.utils.ViewHolesThread;
import com.example.potholes.entities.Hole;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Locale;

public class MainFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    private Button detect_holes;
    private Button view_holes;
    private Button view_onMap;
    private ProgressDialog dialog;
    private ArrayList<Hole> holeArrayList;
    private LocationManager locationManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpViewComponents(view);
        setUpComponents(view);
        setUpListeners(view);

        if (!gpsIsEnabled())
            showGPSDisabledDialog();

    }


    private void setUpComponents(View view) {
        locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        holeArrayList = new ArrayList<>();
        dialog = new ProgressDialog(getContext());

    }

    private void setUpViewComponents(View view) {
        detect_holes = view.findViewById(R.id.rilevaBtn);
        view_holes = view.findViewById(R.id.vediBucheBtn);
        view_onMap = view.findViewById(R.id.viewOnMapbtn);
    }

    private void setUpListeners(View view) {

        detect_holes.setOnClickListener(v -> {
            if (gpsIsEnabled()) {
                dialog.setMessage("In attesa del valore soglia");
                dialog.show();
                receiveLimit();
            } else {
                showGPSDisabledDialog();
            }
        });

        view_holes.setOnClickListener(v -> {
            if (gpsIsEnabled()) {
                dialog.setMessage("Caricamento di tutte le buche");
                dialog.show();
                viewHoles(false);
            }else
                showGPSDisabledDialog();
        });

        view_onMap.setOnClickListener(v -> {
            if (gpsIsEnabled()) {
                dialog.setMessage("Caricamento");
                dialog.show();
                viewHoles(true);
            }else
                showGPSDisabledDialog();
        });

    }

    //Funzione per ricevere il valore soglia
    private void receiveLimit() {
        Thread rec = new Thread(new ReceiveLimitThread(getContext(), getActivity(), dialog));
        rec.start();
    }

    private void viewHoles(boolean toMap) {
        try {
            LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Thread rec = new Thread(new ViewHolesThread(location.getLatitude(), location.getLongitude(), getContext(), getActivity(),dialog,toMap));
                    rec.start();
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    private boolean gpsIsEnabled() {
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }

    public void showGPSDisabledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("GPS Disabled");
        builder.setMessage("Gps is disabled, in order to use the application properly you need to enable GPS of your device");

        builder.setPositiveButton("Enable GPS", (dialog, which) ->
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)).setNegativeButton("No, Just Exit", (dialog, which) -> {
        });
        AlertDialog mGPSDialog = builder.create();
        mGPSDialog.show();
    }


}



