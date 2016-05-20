package com.anthropicandroid.gzt.activity;

/*
 * Created by Andrew Brin on 5/10/2016.
 */

import android.app.Activity;
import android.databinding.BindingAdapter;
import android.os.Bundle;
import android.view.View;

import com.anthropicandroid.extranetbrowser.view.ExtranetMapView;
import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.modules.GZTMapComponent;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class GZTMapViewAdapter {

    public static final String TAG = GZTMapViewAdapter.class.getSimpleName();

    @BindingAdapter("zoom_in")
    public static void zoomIn(GZTMapComponent mapComponent, View view, boolean shouldAnimate) {
        if (shouldAnimate)
            mapComponent.getGZTZoomAnimator().zoomToView(view);
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                // no op
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                // remove dagger component
                ((ZombieTrackerApplication) ((Activity) v.getContext()).getApplication()).releaseMapComponent();
            }
        });
    }

    @BindingAdapter("get_extranet_map")
    public static void getExtranetMap(final GZTMapComponent mapComponent, final ExtranetMapView view, boolean shouldGetMap) {
        if (shouldGetMap) {
            view.onCreate(new Bundle());
            view.onResume();
            view.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    mapComponent.getMapViewHolder().setMapView(view);
                }
            });
        }
    }
}
