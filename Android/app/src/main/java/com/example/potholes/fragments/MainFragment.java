package com.example.potholes.fragments;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import com.google.android.gms.location.FusedLocationProviderClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MainFragment extends Fragment {


    private Button detect_holes;
    private Button view_holes;
    private Handler receiveLimitHandler;
    private final String IP = "192.168.1.23";
    private final int PORT = 10000;
    private String buffer = new String();
    boolean checkConnection = false;


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
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, new RilevaBucheFragment());
            fragmentTransaction.commit();

        });

        view_holes.setOnClickListener(v -> {
            //TODO:Implementare visualizza buche

        });

    }

    //Funzione per ricevere il valore soglia
    private void receiveLimit() {
        receiveLimitHandler = new Handler();

        Thread rec = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket(IP, PORT);
                    setConnection();
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
                    setNoConnection();
                }

                receiveLimitHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Valore soglia ricevuto: " + buffer, Toast.LENGTH_LONG).show();

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

    private void setNoConnection() {
        checkConnection = false;
    }

    private void setConnection() {
        checkConnection = true;
    }

}



