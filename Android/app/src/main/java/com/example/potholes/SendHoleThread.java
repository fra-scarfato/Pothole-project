package com.example.potholes;

import android.location.Location;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SendHoleThread implements Runnable{
    private final String IP = "192.168.1.23";
    private final int PORT = 10000;
    private final Location location;
    private final float variation;

    public SendHoleThread(Location location, float variation) {
        this.location = location;
        this.variation = variation;
    }

    @Override
    public void run() {
        try {
            Socket s = new Socket(IP, PORT);
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
            out.println("1" + location.getLatitude() + "*" + location.getLongitude() + "$" + variation + "#");
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
