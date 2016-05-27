package com.anthropicandroid.extranetbrowser.model;

/*
 * Created by Andrew Brin on 5/17/2016.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

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
    private final Context context; //  will be used
    private final WaspHolder waspHolder;

    enum BulkStringList {
        REQUESTED_KEYS
    }

    enum OccasionDeficit {
        NO_CACHED_OCCASION
    }

    public ExtranetOccasionProvider(Context context, WaspHolder waspHolder) {
        this.context = context;
        this.waspHolder = waspHolder;
    }

    public Observable<Occasion> getOccasionsSubset(final List<String> keysToShow) {
        waspHolder.setBulkStringList(BulkStringList.REQUESTED_KEYS, keysToShow);
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
                        return getCachedOccasionsAndRecordFailures(occasionKeys);
                    }
                });
    }

    private Observable<Occasion> getMissingOccasions() {
        Log.d(TAG, "getting missing occasions");
        // request Occasions from extranet server in batches, return newly populated occasions,
        // may want to use IntentService/BroadcastReceiver pattern
        return Observable.empty();
    }

    @NonNull
    private Observable<Occasion> getCachedOccasionsAndRecordFailures(final List<String> keys) {
        Log.d(TAG, "getting cached occasions and recording failures");
        return Observable.create(new Observable.OnSubscribe<Occasion>() {
            @Override
            public void call(Subscriber<? super Occasion> subscriber) {
                for (String key : keys) {
                    Occasion occasion = waspHolder.getCachedOccasion(key);
                    if (occasion != null){
                        subscriber.onNext(occasion);
                    }
                    else waspHolder.addErroneousOccasion(key, OccasionDeficit.NO_CACHED_OCCASION);
                }
                subscriber.onCompleted();
            }
        });
    }

}