package com.anthropicandroid.extranetbrowser.modules;

import com.anthropicandroid.extranetbrowser.view.ExtranetMapWrapper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/*
 * Created by Andrew Brin on 5/25/2016.
 */

public class TestMapModule extends MapModule {
    private GoogleMapAsyncGetter googleMapGetter;
    private ExtranetMapWrapper mockWrapper;

    public TestMapModule(GoogleMapAsyncGetter googleMapAsyncGetter, ExtranetMapWrapper mockWrapper) {
        super(googleMapAsyncGetter);
        this.googleMapGetter = googleMapAsyncGetter;
        this.mockWrapper = mockWrapper;
    }

    @Override
    public Observable<ExtranetMapWrapper> googleMapObservable() {
        return Observable
                .create(new Observable.OnSubscribe<ExtranetMapWrapper>() {
                    @Override
                    public void call(final Subscriber<? super ExtranetMapWrapper> subscriber) {
                        googleMapGetter.getGoogleMapViewAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                subscriber.onNext(mockWrapper);
                            }
                        });
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread());
    }
}
