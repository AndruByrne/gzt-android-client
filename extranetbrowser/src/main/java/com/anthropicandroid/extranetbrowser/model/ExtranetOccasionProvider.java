package com.anthropicandroid.extranetbrowser.model;

/*
 * Created by Andrew Brin on 5/17/2016.
 */

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

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
    private final PylonDAO                      pylonDAO;
    private       ExtranetAPIModule.ExtranetAPI extranetAPI;
    private       Observable<LatLng>            locationProvider;

    public ExtranetOccasionProvider(
            Context context,
            PylonDAO pylonDAO,
            ExtranetAPIModule.ExtranetAPI extranetAPI,
            Observable<LatLng> locationProvider) {
        this.pylonDAO = pylonDAO;
        this.extranetAPI = extranetAPI;
        this.locationProvider = locationProvider;
        Log.i(this.getClass().getSimpleName(),"EOP created");
    }

    public Observable<Occasion> getContinuousOccasionsSubset(final List<String> keysToShow) {
        // may want to remove this write
        pylonDAO.setBulkStringList(PylonDAO.BulkStringList.RECENTLY_DISPLAYED_KEYS, keysToShow);
        // async start serviceIntent for data downloads with preference to this method(we have
        // requested keys, should d/l data)
        // (with download limitations passed into ExtranetMapView)
        return Observable.concat(
                getCachedOccasionsAndRecordFailures(keysToShow),
                getAndSaveMissingOccasions());
    }

    public Observable<Occasion> getContinuousGlobalOccasions() {
        return pylonDAO
                .getOccasionKeys()
                .flatMap(new Func1<List<String>, Observable<Occasion>>() {
                    @Override
                    public Observable<Occasion> call(final List<String> occasionKeys) {
                        return Observable.concat(
                                getCachedOccasionsAndRecordFailures(occasionKeys),
                                getAndSaveMissingOccasions());
                    }
                });
    }

    private Observable<Occasion> getAndSaveMissingOccasions() { //  TODO(Andrew Brin): add saving
        // of occasions and updating of errouneous list
        // request Occasions from extranet server in batches, return newly populated occasions,
        // TODO(Andrew Brin):stinkin' batches
        // THEN should start async service to ask for missing keys again
        return locationProvider.take(1).flatMap(new Func1<LatLng, Observable<Occasion>>() {
            @Override
            public Observable<Occasion> call(LatLng latLng) {
                List<String> erroneousOccasions = pylonDAO.getKeysForErroneousOccasions();
                return extranetAPI.getOccasionsFromLocation(
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
                    Occasion occasion = pylonDAO.getCachedOccasion(key);
                    if (occasion != null) {
                        subscriber.onNext(occasion);
                    } else pylonDAO.addErroneousOccasion(key);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<Occasion>> getSegmentedOccasionsSubsetNoMoreThan(
            List<String> keysToRegister,
            int maxReturn) {
        return null;
    }
}