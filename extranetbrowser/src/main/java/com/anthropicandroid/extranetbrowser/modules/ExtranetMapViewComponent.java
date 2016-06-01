package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/24/2016.
 */

import com.anthropicandroid.extranetbrowser.ExtranetMapView;

import dagger.Component;

@ExtranetMapViewScope
@Component(
        modules = {
                ContextModule.class,
                ExtranetAPIModule.class,
                LocationModule.class,
                MapModule.class,
                OccasionProviderModule.class,
                WaspModule.class
        })
public interface ExtranetMapViewComponent {
    void inject(ExtranetMapView extranetMapView);
}
