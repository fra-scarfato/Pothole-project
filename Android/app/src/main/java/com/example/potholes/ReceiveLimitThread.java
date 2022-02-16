package com.example.potholes;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.potholes.fragments.RilevaBucheFragment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ReceiveLimitThread implements Runnable{
    private boolean checkConnection = false;
    private float limitValue;
    private Context context;
    private FragmentActivity activity;
    private final String IP = "192.168.1.23";
    private final int PORT = 10000;
    private Handler handler;


    public ReceiveLimitThread(Context context, FragmentActivity activity) {
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
                    Toast.makeText(context, "Valore soglia ricevuto: " +limitValue, Toast.LENGTH_LONG).show();
                    Bundle bundle = new Bundle();
                    bundle.putFloat("limit", limitValue);
                    RilevaBucheFragment rilevaBucheFragment = new RilevaBucheFragment();
                    rilevaBucheFragment.setArguments(bundle);
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment, rilevaBucheFragment);
                    fragmentTransaction.commit();
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

    private void passFloat(String st) {
        limitValue = Float.parseFloat(st);
    }
}
