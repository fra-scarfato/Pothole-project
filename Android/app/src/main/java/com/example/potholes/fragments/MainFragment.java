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
    private Handler receiveLimitHandler;
    private final String IP = "192.168.1.23";
    private final int PORT = 10000;
    private String buffer = new String();
    boolean checkConnection = false;
    private float limit;
    double lat, lon;
    ArrayList <Hole> holes;


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
        receiveLimitHandler = new Handler();

        Thread rec = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket();
                    //Creo la socket con un timeout di 2 secondi per accorciare i tempi di ricezione di un'eventuale eccezione
                    s.connect(new InetSocketAddress(IP, PORT), 1000);
                    //Connessione effettuata correttamente
                    setConnection();
                    //Prima connessione e invio della richiesta da parte del client
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                    out.println("0");
                    //Ricezione del valore soglia
                    BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    final String buffer = fromServer.readLine();
                    passString(buffer);
                    passFloat(buffer);
                    s.close();
                } catch (IOException /*| InterruptedException*/ e) {
                    e.printStackTrace();
                    setNoConnection();
                }

                receiveLimitHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Se la connessione al server è stata effettuata correttamente allora passa a rilevare le buche
                        if(checkConnection) {
                            Toast.makeText(getContext(), "Valore soglia ricevuto: " + buffer, Toast.LENGTH_LONG).show();
                            Bundle bundle = new Bundle();
                            bundle.putFloat("limit", limit);
                            RilevaBucheFragment rilevaBucheFragment = new RilevaBucheFragment();
                            rilevaBucheFragment.setArguments(bundle);
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.fragment, rilevaBucheFragment);
                            fragmentTransaction.commit();
                        } else {
                            Toast.makeText(getContext(), "Connessione al server non riuscita.\nRiprova più tardi.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        rec.start();
    }

    private void viewHoles() {
        receiveLimitHandler = new Handler();
        Thread rec = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket();
                    //Creo la socket con un timeout di 2 secondi per accorciare i tempi di ricezione di un'eventuale eccezione
                    s.connect(new InetSocketAddress(IP, PORT), 1000);
                    //Connessione effettuata correttamente
                    setConnection();
                    //Prima connessione e invio della richiesta da parte del client
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                    out.println("2"+lat+"*"+lon+"#");
                    BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    final String buffer = fromServer.readLine();
                    passString(buffer);
                    parseJSON();
                    s.close();
                } catch (IOException /*| InterruptedException*/ e) {
                    e.printStackTrace();
                    setNoConnection();
                }

                receiveLimitHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Se la connessione al server è stata effettuata correttamente allora passa a rilevare le buche
                        if(checkConnection) {
                            Toast.makeText(getContext(), "OLEEEE\nUsername:"+holes.get(0).getUsername()+",Lat:"+holes.get(0).getLat()+",Lon:"+holes.get(0).getLon(),Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Connessione al server non riuscita.\nRiprova più tardi.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        try{
            LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    rec.start();
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
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

    private void parseJSON() {
        try {
            JSONObject myJSON = new JSONObject(buffer);
            JSONArray holesJSON = myJSON.getJSONArray("potholes");


            int size = holesJSON.length();


            for (int i = 0; i < size; i++) {
                JSONObject singleHole = holesJSON.getJSONObject(i);
                holes = new ArrayList<Hole>(size);
                holes.add(new Gson().fromJson(String.valueOf(singleHole), Hole.class));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}



