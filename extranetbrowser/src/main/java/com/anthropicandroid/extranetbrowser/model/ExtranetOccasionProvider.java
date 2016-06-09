package com.anthropicandroid.extranetbrowser.model;

/*
 * Created by Andrew Brin on 5/17/2016.
 */

import android.content.Context;
import android.support.annotation.NonNull;

import com.anthropicandroid.extranetbrowser.modules.ExtranetAPIModule;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class ExtranetOccasionProvider {
    /*
    * class to provide observables of extranet occasions to be mapped into map markers */

    public static final String TAG = ExtranetOccasionProvider.class.getSimpleName();
    public static final String EXTRANET_DATABASE = "ExtranetDatabase";
    public static final String EXTRANET_OCCASIONS_HASH = "ExtranetOccasionsHash";
    public static final String BULK_LIST_HASH = "BulkListHash";
    public static final String ERRONEOUS_OCCASION_HASH = "erroneous_occasions_hash";
    private final Context context; //  may well be used
    private final WaspHolder waspHolder;
    private ExtranetAPIModule.ExtranetAPI extranetAPI;
    private Observable<LatLng> locationProvider;

    public ExtranetOccasionProvider(Context context, WaspHolder waspHolder, ExtranetAPIModule.ExtranetAPI extranetAPI, Observable<LatLng> locationProvider) {
        this.context = context;
        this.waspHolder = waspHolder;
        this.extranetAPI = extranetAPI;
        this.locationProvider = locationProvider;
    }

    public Observable<Occasion> getOccasionsSubset(final List<String> keysToShow) {
        waspHolder.setBulkStringList(WaspHolder.BulkStringList.REQUESTED_KEYS, keysToShow);
        // async start serviceIntent for data downloads with preference to this method(we have requested keys, should d/l data)
        // (with download limitations passed into ExtranetMapView)
        return Observable.concat(
                getCachedOccasionsAndRecordFailures(keysToShow),
                getMissingOccasions());
    }

    public Observable<Occasion> getGlobalOccasions() {
        return waspHolder
                .getOccasionKeys()
                .flatMap(new Func1<List<String>, Observable<Occasion>>() {
                    @Override
                    public Observable<Occasion> call(final List<String> occasionKeys) {
                        return Observable.concat(
                                getCachedOccasionsAndRecordFailures(occasionKeys),
                                getMissingOccasions());
                    }
                });
    }

    private Observable<Occasion> getMissingOccasions() {
        // request Occasions from extranet server in batches, return newly populated occasions, TODO(Andrew Brin):stinkin' batches
        return locationProvider.take(1).flatMap(new Func1<LatLng, Observable<Occasion>>() {
            @Override
            public Observable<Occasion> call(LatLng latLng) {
                List<String> erroneousOccasions = waspHolder.getKeysForErroneousOccasions();
                return extranetAPI.getOccasionsAtLocation(
                        latLng.latitude,
                        latLng.longitude,
                        erroneousOccasions.toArray(new String[erroneousOccasions.size()]));
            }
        });
    }

    @NonNull
    private Observable<Occasion> getCachedOccasionsAndRecordFailures(final List<String> keys) {
        return Observable.create(new Observable.OnSubscribe<Occasion>() {
            @Override
            public void call(Subscriber<? super Occasion> subscriber) {
                for (String key : keys) {
                    Occasion occasion = waspHolder.getCachedOccasion(key);
                    if (occasion != null) {
                        subscriber.onNext(occasion);
                    } else waspHolder.addErroneousOccasion(key);
                }
                subscriber.onCompleted();
            }
        });
    }

}