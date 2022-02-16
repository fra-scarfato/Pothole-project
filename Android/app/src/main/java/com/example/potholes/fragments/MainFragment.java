package com.example.potholes.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class MainFragment extends Fragment {


    private Button detect_holes;
    private Button view_holes;
    private Handler receiveLimitHandler;
    private final String IP = "20.73.84.69";
    private final int PORT = 80;
    private String buffer = new String();
    boolean checkConnection = false;
    private float limit;
    double lat, lon;

    private ProgressDialog dialog;
    private ArrayList<Hole> holeArrayList;
    private TextView username;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpComponents(view);
        setUpListeners(view);
    }


    private void setUpComponents(View view){
        holeArrayList = new ArrayList<>();
        dialog = new ProgressDialog(getContext());
        username = view.findViewById(R.id.usernameString);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user",Context.MODE_PRIVATE);
        username.setText(sharedPreferences.getString("username",null));
    }

    private void setUpListeners(View view) {
        detect_holes = view.findViewById(R.id.rilevaBtn);
        view_holes = view.findViewById(R.id.vediBucheBtn);

        detect_holes.setOnClickListener(v -> {
            dialog.setMessage("In attesa del valore soglia");
            dialog.show();
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
                        dialog.dismiss();
                        //Se la connessione al server è stata effettuata correttamente allora passa a rilevare le buche
                        if(checkConnection) {
                            Toast.makeText(getContext(), "Valore soglia "+ buffer, Toast.LENGTH_SHORT).show();
                            Bundle bundle = new Bundle();
                            bundle.putFloat("limit", limit);
                            DetectHoleFragment detectHoleFragment = new DetectHoleFragment();
                            detectHoleFragment.setArguments(bundle);
                            addFragment(detectHoleFragment);
                            detect_holes.setClickable(false);
                            view_holes.setClickable(false);
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
                    holeArrayList = parseJSON();
                    s.close();
                    sendHoleArrayListToViewHoleFragment(getContext(),holeArrayList);
                } catch (IOException /*| InterruptedException*/ e) {
                    e.printStackTrace();
                    setNoConnection();
                }

                receiveLimitHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Se la connessione al server è stata effettuata correttamente allora passa a rilevare le buche
                        if(checkConnection) {
                            Toast.makeText(getContext(), "OLEEEE\nUsername:"+holeArrayList.get(0).getUsername()+",Lat:"+holeArrayList.get(0).getLat()+",Lon:"+holeArrayList.get(0).getLon(),Toast.LENGTH_LONG).show();
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

    private ArrayList<Hole> parseJSON() {
        ArrayList <Hole> holes = null;
        try {
            JSONObject myJSON = new JSONObject(buffer);
            JSONArray holesJSON = myJSON.getJSONArray("potholes");


            int size = holesJSON.length();


            for (int i = 0; i < size; i++) {
                JSONObject singleHole = holesJSON.getJSONObject(i);
                holes = new ArrayList<>(size);
                holes.add(new Gson().fromJson(String.valueOf(singleHole), Hole.class));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return  holes;
    }

    private void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment, fragment);
        fragmentTransaction.commit();

    }

    private void sendHoleArrayListToViewHoleFragment(Context context,ArrayList<Hole> holes){
        Intent intent = new Intent(context, ViewHoleFragment.class);
        intent.putExtra("holes",holes);
        context.startActivity(intent);
    }


}



