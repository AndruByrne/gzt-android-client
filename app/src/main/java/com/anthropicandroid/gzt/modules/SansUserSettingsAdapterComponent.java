package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 5/4/2016.
 */

import android.databinding.DataBindingComponent;

import com.anthropicandroid.gzt.activity.GZTSettingsActivity;
import com.anthropicandroid.gzt.activity.GZTZoomAnimator;
import com.anthropicandroid.gzt.activity.MapViewLifecycleHolder;
import com.anthropicandroid.gzt.activity.UserActionHandlers;
import com.anthropicandroid.gzt.services.ApplicationPreferences;

import dagger.Component;

@SansUserSettingsAdapterScope
@Component(
        dependencies = ApplicationComponent.class,
        modules = {
                MapViewLifecycleHolderModule.class,
                EditTextModule.class,
        })

public interface SansUserSettingsAdapterComponent extends DataBindingComponent {
    UserActionHandlers getUserActionHandlers();

    ApplicationPreferences getPreferenceStorage();

    MapViewLifecycleHolder getMapViewHolder();

    GZTZoomAnimator getGZTZoomAnimator();

    void inject(GZTSettingsActivity gztSettingsActivity);
}
