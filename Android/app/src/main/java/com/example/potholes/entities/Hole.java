package com.example.potholes.entities;

import java.io.Serializable;

public class Hole implements Serializable {

    String indirizzo;
    double lat;
    double lon;
    double valore_variazione;


    public Hole(String indirizzo, double lat, double lon, double valore_variazione) {
        this.indirizzo = indirizzo;
        this.lat = lat;
        this.lon = lon;
        this.valore_variazione = valore_variazione;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
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

    public double getValore_variazione() {
        return valore_variazione;
    }

    public void setValore_variazione(double indirizzo) {
        this.valore_variazione = valore_variazione;
    }
}
