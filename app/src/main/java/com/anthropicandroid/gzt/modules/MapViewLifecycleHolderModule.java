package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 5/14/2016.
 */

import com.anthropicandroid.gzt.activity.MapViewLifecycleHolder;

import dagger.Module;
import dagger.Provides;

@Module
public class MapViewLifecycleHolderModule {

    @Provides
    @SansUserSettingsAdapterScope
    MapViewLifecycleHolder getMapViewLifecycleHolder(){
        return new MapViewLifecycleHolder();
    }
}
