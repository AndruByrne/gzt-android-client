package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 5/10/2016.
 */

import android.databinding.DataBindingComponent;

import com.anthropicandroid.gzt.activity.GZTAnimator;
import com.anthropicandroid.gzt.activity.MapViewLifecycleHolder;

import dagger.Component;

@MapScope
@Component(
        dependencies = SansUserSettingsAdapterComponent.class
)
public interface GZTMapComponent extends DataBindingComponent{
    GZTAnimator getGZTZoomAnimator();
    MapViewLifecycleHolder getMapViewHolder();
}
