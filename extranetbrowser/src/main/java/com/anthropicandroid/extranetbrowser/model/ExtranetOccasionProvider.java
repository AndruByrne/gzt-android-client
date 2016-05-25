package com.anthropicandroid.extranetbrowser.model;

/*
 * Created by Andrew Brin on 5/17/2016.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import net.rehacktive.waspdb.WaspHash;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

final public class ExtranetOccasionProvider {

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

    @Inject public ExtranetOccasionProvider(Context context, WaspHolder waspHolder) {
        this.context = context;
        this.waspHolder = waspHolder;
    }

    public Observable<Occasion> getOccasionsSubsetObservable(final List<String> keysToShow) {
        waspHolder.setBulkStringList(BulkStringList.REQUESTED_KEYS, keysToShow);
        // async start serviceIntent for data downloads with preference to this method(we have requested keys, should d/l data)
        // (with download limitations passed into ExtranetMapView)
        return Observable.concat(getCachedOccasionsAndRecordFailures(keysToShow), getMissingOccasions());
    }

    public Observable<Occasion> getGlobalOccasions() {
        Log.d(TAG, "got global occasions hash call");
        return waspHolder
                .getOccasionKeys()
                .flatMap(new Func1<List<String>, Observable<Occasion>>() {
                    @Override
                    public Observable<Occasion> call(final List<String> occasionKeys) {
                        waspHolder.getExtranetOccasionsHash().put("Occasion_Key", new Occasion());
                        return getCachedOccasionsAndRecordFailures(occasionKeys);
                    }
                });
    }

    private Observable<Occasion> getMissingOccasions() {
        // request Occasions from extranet server in batches, return newly populated occasions,
        // may want to use IntentService/BroadcastReceiver pattern
        return null;
    }

    @NonNull
    private Observable<Occasion> getCachedOccasionsAndRecordFailures(final List<String> keys) {
        Log.d(TAG, "getting cached occasions and redording failures");
        return Observable.create(new Observable.OnSubscribe<Occasion>() {
            @Override
            public void call(Subscriber<? super Occasion> subscriber) {
                WaspHash extranetOccasions = waspHolder.getExtranetOccasionsHash();
                for (String key : keys) {
                    Occasion occasion = extranetOccasions.get(key);
                    if (occasion != null)
                        subscriber.onNext(occasion);
                    else waspHolder.addErroneousOccasion(key, OccasionDeficit.NO_CACHED_OCCASION);
                }
                subscriber.onCompleted();
            }
        });
    }

}