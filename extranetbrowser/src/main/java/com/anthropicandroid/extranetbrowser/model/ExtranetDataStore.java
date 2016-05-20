package com.anthropicandroid.extranetbrowser.model;

/*
 * Created by Andrew Brin on 5/17/2016.
 */

import android.support.annotation.NonNull;
import android.util.Log;

import net.rehacktive.waspdb.WaspDb;
import net.rehacktive.waspdb.WaspFactory;
import net.rehacktive.waspdb.WaspHash;
import net.rehacktive.waspdb.WaspListener;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

public class ExtranetDataStore {

    public static final String TAG = ExtranetDataStore.class.getSimpleName();
    public static final String EXTRANET_DATABASE = "ExtranetDatabase";
    public static final String EXTRANET_OCCASIONS_HASH = "ExtranetOccasionsHash";
    public static final String BULK_LIST_HASH = "BulkListHash";
    public static final String ERRONEOUS_OCCASION_HASH = "erroneous_occasions_hash";

    private final WaspHolder waspHolder;

    enum BulkStringList {
        REQUESTED_KEYS
    }

    enum OccasionDeficit {
        NO_CACHED_OCCASION
    }

    public ExtranetDataStore(final String path) {
        waspHolder = new WaspHolder(path);
    }

    public Observable<Occasion> getOccasionsSubsetObservable(final List<String> keysToShow) {
        waspHolder.setBulkStringList(BulkStringList.REQUESTED_KEYS, keysToShow);
        // async start serviceIntent for data downloads with preference to this method(we have requested keys, should d/l data)
        // (with download limitations passed into ExtranetMapView)
        return Observable.concat(getCachedOccasionsAndRecordFailures(keysToShow), getMissingOccasions());
    }

    private Observable<Occasion> getMissingOccasions() {
        // request Occasions from extranet server in batches, return newly populated occasions,
        // may want to use IntentService/BroadcastReciever pattern
        return null;
    }

    public Observable<Occasion> getGlobalOccasions() {
        Log.d(TAG, "got global occasions hash call");
        return waspHolder
                .getOccasionKeys()
                .flatMap(new Func1<List<String>, Observable<Occasion>>() {
                    @Override
                    public Observable<Occasion> call(final List<String> occasionKeys) {
                        waspHolder.extranetOccasionsHash.put("Occasion_Key", new Occasion());
                        return getCachedOccasionsAndRecordFailures(occasionKeys);
                    }
                });
    }

    @NonNull
    private Observable<Occasion> getCachedOccasionsAndRecordFailures(final List<String> keys) {
        Log.d(TAG, "getting cached occasions and redording failures");
        return Observable.create(new Observable.OnSubscribe<Occasion>() {
            @Override
            public void call(Subscriber<? super Occasion> subscriber) {
                WaspHash extranetOccasions = waspHolder.getExtranetOccasionsHash(); //  want actual hash to reduce method calls in loop
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

    private class WaspHolder {

        private WaspDb waspDb;
        WaspHash bulkAddedListsHash;
        WaspHash extranetOccasionsHash;
        WaspHash erroneousOccasionsHash;
        private final ConnectableObservable<Boolean> waspDBInitObservable;

        private WaspHolder() {
            Log.e(TAG, "Wrong waspholder constructor used");
            waspDBInitObservable = null;
        }

        public WaspHolder(final String path) {
            waspDBInitObservable = Observable
                    .create(new Observable.OnSubscribe<Boolean>() {
                        @Override
                        public void call(final Subscriber<? super Boolean> subscriber) {
                            WaspFactory.openOrCreateDatabase(path, EXTRANET_DATABASE, "password", new WaspListener<WaspDb>() {
                                @Override
                                public void onDone(WaspDb waspDb) {
                                    initHolder(waspDb);
                                    Log.d(TAG, "holder initialized");
                                    subscriber.onNext(true);
                                }
                            });
                        }
                    })
                    .take(1) //  do once
                    .subscribeOn(Schedulers.computation()) //  allow for as many threads as processors
                    .replay(Schedulers.io());
            waspDBInitObservable.connect(); //  start DB init
        }

        private void initHolder(WaspDb waspDb) {
            if (this.waspDb == null) { //  if waspDb null, add db and create Hashes
                this.waspDb = waspDb;
                bulkAddedListsHash = waspDb.openOrCreateHash(BULK_LIST_HASH);
                extranetOccasionsHash = waspDb.openOrCreateHash(EXTRANET_OCCASIONS_HASH);
                erroneousOccasionsHash = waspDb.openOrCreateHash(ERRONEOUS_OCCASION_HASH);
            }
        }

        public Observable<List<String>> getOccasionKeys() {
            return waspDBInitObservable //  subscribing to replaying obs. field to prevent race bet. init & first get
                    .map(new Func1<Boolean, List<String>>() {
                        @Override
                        public List<String> call(Boolean initSuccess) {
                            return extranetOccasionsHash.getAllKeys();
                        }
                    })
                    .take(1);
        }

        public void setBulkStringList(final BulkStringList listKey, final List<String> list) {
            if (waspDb == null)
                waspDBInitObservable
                        .subscribe(
                                new Action1<Boolean>() {
                                    @Override
                                    public void call(Boolean aBoolean) {
                                        bulkAddedListsHash.put(listKey, list);
                                    }
                                });
            else bulkAddedListsHash.put(listKey, list);
        }

        public void addErroneousOccasion(final String key, final OccasionDeficit deficit) {
            if (waspDb == null)
                waspDBInitObservable
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                erroneousOccasionsHash.put(key, deficit);
                            }
                        });
            else erroneousOccasionsHash.put(key, deficit);
        }

        public WaspHash getExtranetOccasionsHash() {
            if (waspDb == null)
                throw new RuntimeException("wasp db not initialized before getting hashes");
            else return extranetOccasionsHash;
        }
    }
}