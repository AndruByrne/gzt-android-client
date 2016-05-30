package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/29/2016.
 */

import com.google.android.gms.maps.model.LatLng;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import rx.Observable;

@Module
public class LocationModule {
    public static final double centerOfDemoLatitude = 37.860d;
    public static final double centerOfDemoLongitude = -122.487d;

    @Provides
    @ExtranetMapViewScope
    @Named("LocationProvider")
    public Observable<LatLng> getLocationProvider(){
        return Observable.just(new LatLng(centerOfDemoLatitude, centerOfDemoLongitude));
    }
}
