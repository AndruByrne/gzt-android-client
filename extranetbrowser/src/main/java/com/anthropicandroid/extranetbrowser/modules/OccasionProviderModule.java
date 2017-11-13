package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/25/2016.
 */

import android.content.Context;

import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.model.PylonDAO;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import rx.Observable;

@Module
public class OccasionProviderModule {

    @Provides
    @ExtranetMapViewScope
    public ExtranetOccasionProvider getExtranetOccasionProvider(
            Context context,
            PylonDAO pylonDAO,
            ExtranetAPIModule.ExtranetAPI extranetAPI,
            @Named("LocationProvider") Observable<LatLng> locationProvider) {
        return new ExtranetOccasionProvider(context, pylonDAO, extranetAPI, locationProvider);
    }
}
