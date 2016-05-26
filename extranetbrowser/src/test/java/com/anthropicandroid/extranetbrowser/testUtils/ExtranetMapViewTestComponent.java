package com.anthropicandroid.extranetbrowser.testUtils;

/*
 * Created by Andrew Brin on 5/24/2016.
 */

import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProviderTest;
import com.anthropicandroid.extranetbrowser.modules.ContextModule;
import com.anthropicandroid.extranetbrowser.modules.ExtranetMapViewComponent;
import com.anthropicandroid.extranetbrowser.modules.ExtranetMapViewScope;
import com.anthropicandroid.extranetbrowser.modules.MapModule;
import com.anthropicandroid.extranetbrowser.modules.OccasionProviderModule;
import com.anthropicandroid.extranetbrowser.modules.WaspModule;
import com.anthropicandroid.extranetbrowser.view.ExtranetMapViewTest;

import dagger.Component;

@ExtranetMapViewScope
@Component(
        modules = {
                ContextModule.class,
                MapModule.class,
                OccasionProviderModule.class,
                WaspModule.class
        })
public interface ExtranetMapViewTestComponent extends ExtranetMapViewComponent {

    void inject(ExtranetOccasionProviderTest extranetOccasionProviderTest);

    void inject(ExtranetMapViewTest extranetMapViewTest);
}
