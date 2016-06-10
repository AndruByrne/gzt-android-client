package com.anthropicandroid.extranetbrowser.modules;

import com.anthropicandroid.extranetbrowser.view.ExtranetMapWrapper;
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
        this.googleMapGetter = googleMapGetter; }

    @Provides
    @ExtranetMapViewScope
    public Observable<ExtranetMapWrapper> googleMapObservable(){
        return Observable
                .create(new Observable.OnSubscribe<ExtranetMapWrapper>() {
                    @Override
                    public void call(final Subscriber<? super ExtranetMapWrapper> subscriber) {
                        googleMapGetter.getGoogleMapViewAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                subscriber.onNext(new ExtranetMapWrapper(googleMap));
                            }
                        });
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    public interface GoogleMapAsyncGetter {
        void getGoogleMapViewAsync(OnMapReadyCallback callback);
    }
}
