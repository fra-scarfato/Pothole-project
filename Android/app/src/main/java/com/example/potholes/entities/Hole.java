package com.example.potholes.entities;

import android.os.Parcel;
import android.os.Parcelable;


public class Hole implements Parcelable {

    private String address;
    private double lat;
    private double lon;
    private double var;


    public Hole(String address, double lat, double lon, double variation) {
        this.address = address;
        this.lat = lat;
        this.lon = lon;
        this.var = variation;
    }

    public Hole(String address, double variation) {
        this.address = address;
        this.var = variation;
    }

    public Hole(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public Hole(Parcel in) {
        this.address = in.readString();
        this.lat = in.readDouble();
        this.lon = in.readDouble();
        this.var = in.readDouble();
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

    public double getVar() {
        return var;
    }

    public void setVar(double indirizzo) {
        this.var = var;
    }

    public static final Parcelable.Creator<Hole> CREATOR
            = new Parcelable.Creator<Hole>() {
        public Hole createFromParcel(Parcel in) {
            return new Hole(in);
        }

        public Hole[] newArray(int size) {
            return new Hole[size];
        }


    };

    @Override
    public String toString() {
        return "Hole{" +
                "address='" + address + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", var=" + var +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeDouble(lat);
        dest.writeDouble(lon);
        dest.writeDouble(var);
    }
}
