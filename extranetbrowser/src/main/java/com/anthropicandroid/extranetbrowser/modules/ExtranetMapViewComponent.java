package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/24/2016.
 */

import com.anthropicandroid.extranetbrowser.view.ExtranetMapView;

import dagger.Component;

@ExtranetMapViewScope
@Component(
        modules = {
                ContextModule.class,
                ExtranetOccasionModule.class
        })
public interface ExtranetMapViewComponent {
    void inject(ExtranetMapView extranetMapView);
}
