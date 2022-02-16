package com.example.potholes.entities;

import java.io.Serializable;

public class Hole implements Serializable {

    String username;
    double lat;
    double lon;
    double var;


    public Hole(String username, double lat, double lon, double depth) {
        this.username = username;
        this.lat = lat;
        this.lon = lon;
        this.var = var;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getVar() {
        return var;
    }

    public void setVar(double var) {
        this.var = var;
    }
}
