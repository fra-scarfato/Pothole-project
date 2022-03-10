package com.example.potholes.utils;

import android.location.Location;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SendHoleThread implements Runnable{
    private final String IP = "20.73.84.69";
    private final int PORT = 80;
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
