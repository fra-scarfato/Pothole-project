package com.example.potholes;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.example.potholes.entities.Hole;
import com.example.potholes.fragments.ViewHoleFragment;
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ViewHolesThread implements Runnable{
    private boolean checkConnection = false;
    private final double latitude, longitude;
    private final Context context;
    private FragmentActivity activity;
    private final String IP = "192.168.1.23";
    private final int PORT = 10000;
    private Handler handler;
    private String message;
    private ArrayList<Hole> holes;

    public ViewHolesThread(double latitude, double longitude, Context context, FragmentActivity activity) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.context = context;
        this.activity = activity;
        handler = new Handler();
    }

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
            //Formattazione richiesta
            out.println("2"+latitude+"*"+longitude+"#");

            BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
            final String buffer = fromServer.readLine();
            passString(buffer);
            s.close();
        } catch (IOException /*| InterruptedException*/ e) {
            e.printStackTrace();
            setNoConnection();
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                //Se la connessione al server è stata effettuata correttamente allora passa a rilevare le buche
                if(checkConnection) {
                    parseJSON();
                    Toast.makeText(context, "OLEEEE\nUsername:"+holes.get(0).getUsername()+",Lat:"+holes.get(0).getLat()+",Lon:"+holes.get(0).getLon(),Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Connessione al server non riuscita.\nRiprova più tardi.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setNoConnection() {
        checkConnection = false;
    }

    private void setConnection() {
        checkConnection = true;
    }

    private void passString(String st) {
        message = String.copyValueOf(st.toCharArray());
    }

    private ArrayList<Hole> parseJSON() {
        ArrayList <Hole> holes = null;
        try {
            JSONObject myJSON = new JSONObject(message);
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
        return holes;
    }

    private void sendHoleArrayListToViewHoleFragment(Context context,ArrayList<Hole> holes){
        Intent intent = new Intent(context, ViewHoleFragment.class);
        intent.putExtra("holes",holes);
        context.startActivity(intent);
    }
}
