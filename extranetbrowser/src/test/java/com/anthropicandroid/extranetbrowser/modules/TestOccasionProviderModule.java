package com.anthropicandroid.extranetbrowser.modules;

import android.content.Context;

import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.model.WaspHolder;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Named;

import rx.Observable;

/*
 * Created by Andrew Brin on 5/25/2016.
 */
public class TestOccasionProviderModule extends OccasionProviderModule {

    private ExtranetOccasionProvider mockOccasionProvider;

    public TestOccasionProviderModule(ExtranetOccasionProvider mockOccasionProvider) {
        this.mockOccasionProvider = mockOccasionProvider;
    }

    @Override
    public ExtranetOccasionProvider getExtranetOccasionProvider(
            Context context,
            WaspHolder waspHolder,
            ExtranetAPIModule.ExtranetAPI extranetAPI,
            @Named("LocationProvider")Observable<LatLng> locationProvider) {
        return mockOccasionProvider;
    }
}
