package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 5/10/2016.
 */

import androidx.databinding.DataBindingComponent;

import com.anthropicandroid.gzt.activity.MapViewLifecycleHolder;
import com.anthropicandroid.gzt.activity.ZoomAnimator;

import dagger.Component;

@MapScope
@Component(
        dependencies = SansUserSettingsAdapterComponent.class
)
public interface GZTMapComponent extends DataBindingComponent {
    ZoomAnimator getGZTZoomAnimator();

    MapViewLifecycleHolder getMapViewHolder();
}
