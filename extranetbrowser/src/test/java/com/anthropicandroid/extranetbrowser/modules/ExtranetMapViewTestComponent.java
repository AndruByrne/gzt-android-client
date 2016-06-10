package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/24/2016.
 */

import com.anthropicandroid.extranetbrowser.ExtranetMapViewTest;
import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProviderTest;

import dagger.Component;

@ExtranetMapViewScope
@Component(
        modules = {
                ContextModule.class,
                ExtranetAPIModule.class,
                ExtranetRegistrationModule.class,
                LocationModule.class,
                MapModule.class,
                OccasionProviderModule.class,
                WaspModule.class
        })
public interface ExtranetMapViewTestComponent extends ExtranetMapViewComponent {

    void inject(ExtranetOccasionProviderTest extranetOccasionProviderTest);

    void inject(ExtranetMapViewTest extranetMapViewTest);
}
