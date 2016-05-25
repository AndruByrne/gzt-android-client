package com.anthropicandroid.extranetbrowser.modules;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import dagger.Module;
import dagger.Provides;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/*
 * Created by Andrew Brin on 5/25/2016.
 */

@Module
public class MapModule {
    /*
    * Module for supplying map view; want interface for testing */
    private GoogleMapAsyncGetter googleMapGetter;

    public MapModule(GoogleMapAsyncGetter googleMapGetter) {
        Log.d(MapModule.class.getSimpleName(), "constructing MapModule; mapView null?"+Boolean.toString(googleMapGetter == null));
        this.googleMapGetter = googleMapGetter; }

    @Provides
    @ExtranetMapViewScope
    Observable<GoogleMap> googleMapObservable(){
        return Observable
                .create(new Observable.OnSubscribe<GoogleMap>() {
                    @Override
                    public void call(final Subscriber<? super GoogleMap> subscriber) {
                        googleMapGetter.getSuperMapViewAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                subscriber.onNext(googleMap);
                            }
                        });
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    public interface GoogleMapAsyncGetter {
        void getSuperMapViewAsync(OnMapReadyCallback callback);
    }
}
