package com.anthropicandroid.extranetbrowser.model;

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

final public class WaspHolder {

    private WaspDb waspDb;
    WaspHash bulkAddedListsHash;
    WaspHash extranetOccasionsHash;
    WaspHash erroneousOccasionsHash;
    private final ConnectableObservable<Boolean> waspDBInitObservable;

    public WaspHolder(final String path) {
        waspDBInitObservable = Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(final Subscriber<? super Boolean> subscriber) {
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
        if (this.waspDb == null) { //  if waspDb null, add db and create Hashes
            this.waspDb = waspDb;
            bulkAddedListsHash = waspDb.openOrCreateHash(ExtranetOccasionProvider.BULK_LIST_HASH);
            extranetOccasionsHash = waspDb.openOrCreateHash(ExtranetOccasionProvider.EXTRANET_OCCASIONS_HASH);
            erroneousOccasionsHash = waspDb.openOrCreateHash(ExtranetOccasionProvider.ERRONEOUS_OCCASION_HASH);
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

    public void setBulkStringList(final ExtranetOccasionProvider.BulkStringList listKey, final List<String> list) {
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

    public void addErroneousOccasion(final String key, final ExtranetOccasionProvider.OccasionDeficit deficit) {
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
