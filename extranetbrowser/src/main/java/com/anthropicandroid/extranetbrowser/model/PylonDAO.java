package com.anthropicandroid.extranetbrowser.model;

import android.util.Log;

import com.sparsity.sparksee.gdb.AttributeKind;
import com.sparsity.sparksee.gdb.DataType;
import com.sparsity.sparksee.gdb.Database;
import com.sparsity.sparksee.gdb.Graph;
import com.sparsity.sparksee.gdb.Session;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/*
 * Created by Andrew Brin on 5/24/2016.
 */

public class PylonDAO
{

    public static final String TAG = PylonDAO.class.getSimpleName();
    private final Session session;
    private final Graph   graph;

    public PylonDAO(
            final Database database
    ) {
        Log.i(
                this.getClass()
                    .getSimpleName(),
                "starting db");
        session = database.newSession();
        graph = session.getGraph();
        initDBIfNew();
    }

    private void initDBIfNew() {
        // TODO(Andrew Brin): the db reads will generate a "Serialization Error" if the Occasion
        // fields have changed but the program only update; may need to test and delete
        if (graph.countEdges() + graph.countNodes() < 1) {
            int occasionType = graph.newNodeType("OCCASION");
            int occasionIdType = graph.newAttribute(
                    occasionType,
                    "ID",
                    DataType.Long,
                    AttributeKind.Unique);
            int occasionSuccessfulType = graph.newAttribute(
                    occasionType,
                    "SUCCESSFUL",
                    DataType.Boolean,
                    AttributeKind.Indexed);
        }
//            bulkAddedListsHash = waspDb.openOrCreateHash(ExtranetOccasionProvider.BULK_LIST_HASH);
//            extranetOccasionsHash = waspDb.openOrCreateHash(ExtranetOccasionProvider
//                                                                    .EXTRANET_OCCASIONS_HASH);
    }

    public Observable<List<String>> getOccasionKeys() {
//        return dBInitObservable //  subscribing to replaying obs. field to prevent race bet
// . init & first get
        return setDemoOccasion() //  wait for demo occasion to be inserted
                                 .map(new Func1<Boolean, List<String>>()
                                 {
                                     @Override
                                     public List<String> call(Boolean initSuccess) {
                                         return extranetOccasionsHash.getAllKeys();
                                     }
                                 })
                                 .take(1);
    }

    private Observable<Boolean> setDemoOccasion() {
        return dBInitObservable
                .map(new Func1<Boolean, Boolean>()
                {
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
                            new Action1<Boolean>()
                            {
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
                    .subscribe(new Action1<Boolean>()
                    {
                        @Override
                        public void call(Boolean aBoolean) {
                            erroneousOccasionsHash.put(key, 0);
                        }
                    });
        else erroneousOccasionsHash.put(key, 0);
    }

    public List<String> getKeysForErroneousOccasions() {
        if (waspDb == null)
            return dBInitObservable.map(new Func1<Boolean, List<String>>()
            {
                @Override
                public List<String> call(Boolean aBoolean) {
                    return erroneousOccasionsHash.getAllKeys();
                }
            })
                                   .take(1)
                                   .toBlocking()
                                   .first();
        else return extranetOccasionsHash.getAllKeys();
    }

    public Occasion getCachedOccasion(String key) {
        // This is where the Occasion is casted
        return extranetOccasionsHash.get(key);
    }

    public List<String> getBulkStringList(final BulkStringList listKey) {
        if (waspDb == null) {
            return dBInitObservable.map(new Func1<Boolean, List<String>>()
            {
                @Override
                public List<String> call(Boolean aBoolean) {
                    return bulkAddedListsHash.get(listKey);
                }
            })
                                   .take(1)
                                   .toBlocking()
                                   .first();
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

public enum BulkStringList
{
    REQUESTED_BROADCAST_KEYS,
    RECENTLY_DISPLAYED_KEYS
}
}
