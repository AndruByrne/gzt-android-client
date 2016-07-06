package com.anthropicandroid.gzt.activity;

/*
 * Created by Andrew Brin on 5/10/2016.
 */

import android.databinding.BindingAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.anthropicandroid.extranetbrowser.ExtranetMapView;
import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.modules.GZTMapComponent;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class GZTMapViewAdapter {

    public static final String TAG = GZTMapViewAdapter.class.getSimpleName();

    @BindingAdapter("get_extranet_map")
    public static void getExtranetMap(
            final GZTMapComponent mapComponent,
            final ExtranetMapView view,
            boolean shouldGetMap) {
        if (shouldGetMap) {
            view.onCreate(new Bundle());
            view.onResume();
            view.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    mapComponent.getMapViewHolder().setMapView(view); //  activity lifecycle
                    // accounting
                }
            });
            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    // no op
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    // remove dagger component
                    ((ZombieTrackerApplication) ((AppCompatActivity) v.getContext()).getApplication())
                            .releaseMapComponent();
                }
            });
        }
    }
}
