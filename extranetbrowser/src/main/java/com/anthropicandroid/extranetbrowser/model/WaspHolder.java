package com.anthropicandroid.extranetbrowser.model;

import android.content.Context;
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

/*
 * Created by Andrew Brin on 5/24/2016.
 */

public class WaspHolder {

    public static final String TAG = WaspHolder.class.getSimpleName();
    private WaspDb waspDb;
    WaspHash bulkAddedListsHash;
    WaspHash extranetOccasionsHash;
    WaspHash erroneousOccasionsHash;
    private final ConnectableObservable<Boolean> waspDBInitObservable;

    public WaspHolder(final Context context) {
        waspDBInitObservable = Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(final Subscriber<? super Boolean> subscriber) {
                        String path = context.getFilesDir().getPath(); //  TODO(Andrew Brin): extend path for specific directory
                        WaspFactory.openOrCreateDatabase(path, ExtranetOccasionProvider.EXTRANET_DATABASE, "password", new WaspListener<WaspDb>() {
                            @Override
                            public void onDone(WaspDb waspDb) {
                                initHolder(waspDb); //  this ordering important
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
        // TODO(Andrew Brin): the db reads will generate a "Serialization Error" if the Occasion fields have changed but the program only update; may need to test and delete
        if (this.waspDb == null) { //  if waspDb null, add db and create Hashes
            this.waspDb = waspDb;
            bulkAddedListsHash = waspDb.openOrCreateHash(ExtranetOccasionProvider.BULK_LIST_HASH);
            extranetOccasionsHash = waspDb.openOrCreateHash(ExtranetOccasionProvider.EXTRANET_OCCASIONS_HASH);
            erroneousOccasionsHash = waspDb.openOrCreateHash(ExtranetOccasionProvider.ERRONEOUS_OCCASION_HASH);
        }
    }

    public Observable<List<String>> getOccasionKeys() {
//        return waspDBInitObservable //  subscribing to replaying obs. field to prevent race bet. init & first get
        return setDemoOccasion() //  wait for demo occasion to be inserted
                .map(new Func1<Boolean, List<String>>() {
                    @Override
                    public List<String> call(Boolean initSuccess) {
                        return extranetOccasionsHash.getAllKeys();
                    }
                })
                .take(1);
    }

    private Observable<Boolean> setDemoOccasion() {
        return waspDBInitObservable
                .map(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        extranetOccasionsHash.put("Demo Key 1", new Occasion("Demo Key 1", 37.85d, -122.48d));
                        return true;
                    }
                });
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

    public void addErroneousOccasion(final String key) {
        if (waspDb == null)
            waspDBInitObservable
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            erroneousOccasionsHash.put(key, 0);
                        }
                    });
        else erroneousOccasionsHash.put(key, 0);
    }

    public List<String> getKeysForErroneousOccasions() {
        if (waspDb == null)
            return waspDBInitObservable.map(new Func1<Boolean, List<String>>() {
                @Override
                public List<String> call(Boolean aBoolean) {
                    return erroneousOccasionsHash.getAllKeys();
                }
            }).take(1).toBlocking().first();
        else return extranetOccasionsHash.getAllKeys();
    }

    public Occasion getCachedOccasion(String key) {
        // This is where the Occasion is casted
        return extranetOccasionsHash.get(key);
    }

    public List<String> getBulkStringList(final BulkStringList listKey) {
        Log.d(TAG, "getting bulk List");
        if (waspDb == null){
            Log.d(TAG, "waspdb null, observing");
            return waspDBInitObservable.map(new Func1<Boolean, List<String>>() {
                @Override
                public List<String> call(Boolean aBoolean) {
                    Log.d(TAG, "waspdb init is true");
                    return bulkAddedListsHash.get(listKey);
                }
            }).take(1).toBlocking().first();}
        else {
            Log.d(TAG, "waspdb not null");
            return bulkAddedListsHash.get(listKey);
        }
    }

    public enum BulkStringList {
        REQUESTED_KEYS
    }
}
