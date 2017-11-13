package com.anthropicandroid.extranetbrowser.model;

import android.content.Context;
import android.util.Log;

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

public class PylonDAO
{

    public static final String TAG = PylonDAO.class.getSimpleName();
    private final ConnectableObservable<Boolean> dBInitObservable;

    public PylonDAO(final Context context) {
        Log.i(this.getClass().getSimpleName(),"creating waspholder");
        dBInitObservable = Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(final Subscriber<? super Boolean> subscriber) {
                        String path = context.getFilesDir().getPath();
                        Log.i(this.getClass().getSimpleName(),"opening db");
                        WaspFactory.openOrCreateDatabase(
                                path,
                                ExtranetOccasionProvider.EXTRANET_DATABASE,
                                "password",
                                new WaspListener<WaspDb>() {
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
        Log.i(this.getClass().getSimpleName(),"obs defined");
        dBInitObservable.connect(); //  start DB init
        Log.i(this.getClass().getSimpleName(),"obs connected");
    }

    private void initHolder(WaspDb waspDb) {
        // TODO(Andrew Brin): the db reads will generate a "Serialization Error" if the Occasion
        // fields have changed but the program only update; may need to test and delete
        if (this.waspDb == null) { //  if waspDb null, add db and create Hashes
            this.waspDb = waspDb;
            bulkAddedListsHash = waspDb.openOrCreateHash(ExtranetOccasionProvider.BULK_LIST_HASH);
            extranetOccasionsHash = waspDb.openOrCreateHash(ExtranetOccasionProvider
                    .EXTRANET_OCCASIONS_HASH);
            erroneousOccasionsHash = waspDb.openOrCreateHash(ExtranetOccasionProvider
                    .ERRONEOUS_OCCASION_HASH);
        }
    }

    public Observable<List<String>> getOccasionKeys() {
//        return dBInitObservable //  subscribing to replaying obs. field to prevent race bet
// . init & first get
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
        return dBInitObservable
                .map(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        extranetOccasionsHash.put(
                                "Demo Key 1",
                                new Occasion("Demo Key 1", 37.85d, -122.48d, 5));
                        return true;
                    }
                });
    }

    public void setBulkStringList(final BulkStringList listKey, final List<String> list) {
        if (waspDb == null)
            dBInitObservable
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
            dBInitObservable
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
            return dBInitObservable.map(new Func1<Boolean, List<String>>() {
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
        if (waspDb == null) {
            return dBInitObservable.map(new Func1<Boolean, List<String>>() {
                @Override
                public List<String> call(Boolean aBoolean) {
                    return bulkAddedListsHash.get(listKey);
                }
            }).take(1).toBlocking().first();
        } else {
            return bulkAddedListsHash.get(listKey);
        }
    }

    public void clearBulkStringList(BulkStringList listKey) {
        bulkAddedListsHash.remove(listKey);
    }

    public void addToBulkStringList(BulkStringList listKey, List<String> occasionKeys) {
        List<String> bulkStringList = getBulkStringList(listKey);
        if (bulkStringList == null)
            bulkAddedListsHash.put(listKey, occasionKeys);
        else {
            bulkStringList.addAll(occasionKeys);
            bulkAddedListsHash.put(listKey, bulkStringList);
        }
    }

    public enum BulkStringList {
        REQUESTED_BROADCAST_KEYS,
        RECENTLY_DISPLAYED_KEYS
    }
}