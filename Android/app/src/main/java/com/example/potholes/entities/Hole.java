package com.example.potholes.entities;

public class Hole {

    double latitude;
    double longitude;
    double depth;


    public Hole(double latitude, double longitude, double depth) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.depth = depth;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }
}
