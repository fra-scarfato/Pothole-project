package com.example.potholes.fragments;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.potholes.R;
import com.example.potholes.ReceiveLimitThread;
import com.example.potholes.ViewHolesThread;
import com.example.potholes.entities.Hole;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class MainFragment extends Fragment {


    private Button detect_holes;
    private Button view_holes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpListeners(view);
    }

    private void setUpListeners(View view) {
        detect_holes = view.findViewById(R.id.rilevaBtn);
        view_holes = view.findViewById(R.id.vediBucheBtn);

        detect_holes.setOnClickListener(v -> {
            receiveLimit();
        });

        view_holes.setOnClickListener(v -> {
            viewHoles();
        });

    }

    //Funzione per ricevere il valore soglia
    private void receiveLimit() {
        Thread rec = new Thread(new ReceiveLimitThread(getContext(), getActivity()));
        rec.start();
    }

    private void viewHoles() {
        try{
            LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Thread rec = new Thread(new ViewHolesThread(location.getLatitude(), location.getLongitude(), getContext(), getActivity()));
                    rec.start();
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

}



