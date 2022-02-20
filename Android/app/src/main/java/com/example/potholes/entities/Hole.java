package com.example.potholes.entities;

import java.io.Serializable;

public class Hole implements Serializable {

    String address;
    double lat;
    double lon;
    double variation;


    public Hole(String address, double lat, double lon, double variation) {
        this.address = address;
        this.lat = lat;
        this.lon = lon;
        this.variation = variation;
    }

    public Hole(String address, double variation) {
        this.address = address;
        this.variation = variation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public double getVariation() {
        return variation;
    }

    public void setVariation(double indirizzo) {
        this.variation = variation;
    }
}
