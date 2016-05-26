package com.anthropicandroid.extranetbrowser.model;

/*
 * Created by Andrew Brin on 5/17/2016.
 */

final public class Occasion {

    private double latitude;
    private double longitude;

    public Occasion(){}

    public Occasion(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

}