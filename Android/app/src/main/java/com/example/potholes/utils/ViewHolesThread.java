package com.example.potholes.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.potholes.R;
import com.example.potholes.entities.Hole;
import com.example.potholes.fragments.ViewHoleFragment;
import com.example.potholes.map.MapsActivity;
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

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class ViewHolesThread implements Runnable{
    private boolean checkConnection = false;
    private final double latitude, longitude;
    private final Context context;
    private FragmentActivity activity;
    private final String IP = "20.73.84.69";
    private final int PORT = 80;
    private Handler handler;
    private String message;
    private ArrayList<Hole> holeArrayList;
    private ProgressDialog dialog;
    private boolean toMap = false;

    public ViewHolesThread(double latitude, double longitude, Context context, FragmentActivity activity,ProgressDialog dialog,boolean toMap) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.context = context;
        this.activity = activity;
        handler = new Handler();
        this.dialog = dialog;
        this.toMap = toMap;
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
                    holeArrayList = new ArrayList<>();
                    holeArrayList = parseJSON();
                    if (toMap == false)
                        sendHoleArrayListToViewHoleFragment(context,holeArrayList);
                    else
                        sendHoleArrayListToMapActivity(context,holeArrayList);
                    dialog.dismiss();
                } else {
                    MotionToast.Companion.darkToast(activity, "Errore","Connessione al server non riuscita.\nRiprova più tardi.", MotionToastStyle.ERROR,MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION, ResourcesCompat.getFont(context, R.font.helveticabold));

                }
            }
        });
    }

    private void sendHoleArrayListToMapActivity(Context context, ArrayList<Hole> holeArrayList) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putParcelableArrayListExtra("hole",(ArrayList<? extends Parcelable>) holeArrayList);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        activity.startActivity(intent);
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
        ArrayList<Hole> holes = new ArrayList<>();
        try {
            JSONObject myJSON = new JSONObject(message);
            JSONArray holesJSON = myJSON.getJSONArray("potholes");
            int size = holesJSON.length();

            for (int i = 0; i < size; i++) {
                JSONObject singleHole = holesJSON.getJSONObject(i);
                holes.add(new Gson().fromJson(String.valueOf(singleHole), Hole.class));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return holes;
    }

    private void sendHoleArrayListToViewHoleFragment(Context context,ArrayList<Hole> holes){
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("hole",(ArrayList<? extends Parcelable>) holes);
        ViewHoleFragment viewHoleFragment = new ViewHoleFragment();
        viewHoleFragment.setArguments(bundle);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addFragment(viewHoleFragment);
    }

    private void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment, fragment,"ViewHole");
        fragmentTransaction.addToBackStack("ViewHole");
        fragmentTransaction.commit();
    }
}
