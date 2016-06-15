package com.anthropicandroid.extranetbrowser.model;

/*
 * Created by Andrew Brin on 5/17/2016.
 */

final public class Occasion {

    private String key;
    private double latitude;
    private double longitude;
    private float radius;

    public Occasion(){}

    public Occasion(String key, double latitude, double longitude, float radius) {
        this.key = key;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getKey() { return key; }

    public float getRadius() {
        return radius;
    }
}