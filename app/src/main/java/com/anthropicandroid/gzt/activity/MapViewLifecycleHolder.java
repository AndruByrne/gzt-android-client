package com.anthropicandroid.gzt.activity;

import android.os.Bundle;

import com.google.android.gms.maps.MapView;

/*
 * Created by Andrew Brin on 5/14/2016.
 */
public class MapViewLifecycleHolder {

    private MapView mapView;

    public void setMapView(MapView mapView){
        this.mapView = mapView;
    }

    protected void onCreate(Bundle savedInstanceState) {
        if(mapView != null)
            mapView.onCreate(savedInstanceState);
    }

    protected void onResume() {
        if(mapView != null)
            mapView.onResume();
    }

    protected void onPause() {
        if(mapView != null)
            mapView.onPause();
    }

    protected void onDestroy() {
        if(mapView != null)
            mapView.onDestroy();
    }
}
