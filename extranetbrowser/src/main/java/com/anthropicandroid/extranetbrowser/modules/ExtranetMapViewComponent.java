package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/24/2016.
 */

import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.view.ExtranetMapView;

import dagger.Component;

@ExtranetMapViewScope
@Component(
        modules = {
                ContextModule.class,
                MapModule.class,
                WaspModule.class
        })
public interface ExtranetMapViewComponent {
    ExtranetOccasionProvider extranetOccasionProvider();
    void inject(ExtranetMapView extranetMapView);
}
