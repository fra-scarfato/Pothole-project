package com.example.potholes.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.potholes.R;
import com.example.potholes.RegisterActivity;
import com.example.potholes.fragments.DetectHoleFragment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class ReceiveLimitThread implements Runnable{
    private boolean checkConnection = false;
    private float limitValue;
    private Context context;
    private FragmentActivity activity;
    private final String IP = "20.73.84.69";
    private final int PORT = 80;
    private Handler handler;
    private ProgressDialog dialog;


    public ReceiveLimitThread(Context context, FragmentActivity activity, ProgressDialog dialog) {
        this.context = context;
        this.activity = activity;
        handler = new Handler();
        this.dialog = dialog;
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
            out.println("0");
            //Ricezione del valore soglia
            BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
            final String buffer = fromServer.readLine();
            passFloat(buffer);
            s.close();
        } catch (IOException /*| InterruptedException*/ e) {
            setNoConnection();
            e.printStackTrace();
        }

        handler.post(new Runnable() {
            @Override
            public void run() {

                //Se la connessione al server è stata effettuata correttamente allora passa a rilevare le buche
                if(checkConnection) {
                    MotionToast.Companion.darkToast(activity, "","Soglia ricevuta: "+limitValue, MotionToastStyle.SUCCESS,MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION, ResourcesCompat.getFont(context, R.font.helveticabold));

                    dialog.dismiss();
                    Bundle bundle = new Bundle();
                    bundle.putFloat("limit", limitValue);
                    DetectHoleFragment detectHoleFragment = new DetectHoleFragment();
                    detectHoleFragment.setArguments(bundle);
                    addFragment(detectHoleFragment);
                } else {
                    dialog.dismiss();
                    MotionToast.Companion.darkToast(activity, "Errore","Connessione al server non riuscita.\nRiprova più tardi.", MotionToastStyle.ERROR,MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION, ResourcesCompat.getFont(context, R.font.helveticabold));

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

    private void passFloat(String st) {
        limitValue = Float.parseFloat(st);
    }

    private void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment, fragment,"DetectHole");
        fragmentTransaction.addToBackStack("DetectHole");
        fragmentTransaction.commit();
    }
}
