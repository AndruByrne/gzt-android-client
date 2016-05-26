package com.anthropicandroid.extranetbrowser.view;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;

/*
 * Created by Andrew Brin on 5/25/2016.
 */
public class ExtranetMapWrapper {
    private GoogleMap googleMap;

    public ExtranetMapWrapper(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public GoogleMap getGoogleMap() {
        return googleMap;
    }

    public void addMarker(MarkerOptions markerOptions) {
        if (googleMap != null)
            googleMap.addMarker(markerOptions);
    }
}
