package com.anthropicandroid.gzt.activity;

/*
 * Created by Andrew Brin on 5/10/2016.
 */

import android.databinding.BindingAdapter;
import android.databinding.DataBindingComponent;
import android.util.Log;
import android.view.View;

import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.modules.GZTMapComponent;

public class GZTMapViewAdapter {

    @BindingAdapter("zoom_in")
    public static void zoomIn(GZTMapComponent mapComponent, View view, boolean shouldAnimate) {

        if (shouldAnimate)
            mapComponent.getGZTAnimatorSetRepository().zoomToViewInDuration(view, 1000);

    }
}
