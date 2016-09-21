package com.seminnova.mvpar.ejemplogpsnuevo;


public class MonumentoMarker {

    private String nombre;
    private double lat;
    private double lon;

    public  MonumentoMarker(){}

    public MonumentoMarker(String nombre, double lat, double lon) {
        this.nombre = nombre;
        this.lat = lat;
        this.lon = lon;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
}
