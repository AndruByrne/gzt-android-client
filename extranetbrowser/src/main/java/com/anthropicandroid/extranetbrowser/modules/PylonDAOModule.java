package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/24/2016.
 */

import com.anthropicandroid.extranetbrowser.model.PylonDAO;
import com.sparsity.sparksee.gdb.Database;
import com.sparsity.sparksee.gdb.Sparksee;
import com.sparsity.sparksee.gdb.SparkseeConfig;

import java.util.concurrent.Callable;

import dagger.Module;
import dagger.Provides;
import rx.Observable;
import rx.schedulers.Schedulers;

@Module
public class PylonDAOModule
{

    static final String OCCASIONS_STORE_PATH = "OccasionsStore.gdb";
    private String filesDir;

    public PylonDAOModule(String filesDir) {
        this.filesDir = filesDir;
    }

    @Provides
    @ExtranetMapViewScope
    public PylonDAO getPylonDAO(
            Database database
    ) {
        return new PylonDAO(database);
    }


    @Provides
    @ExtranetMapViewScope
    public Database getDatabase(
    ) {
        return Observable
                .fromCallable(new Callable<Database>()
                {
                    @Override
                    public Database call()
                            throws Exception {
                        return new Sparksee(new SparkseeConfig())
                                .open(
                                        filesDir+OCCASIONS_STORE_PATH,
                                        false);
                    }
                })
                .onExceptionResumeNext(Observable
                                               .fromCallable(new Callable<Database>()
                                               {
                                                   @Override
                                                   public Database call()
                                                           throws Exception {
                                                       return new Sparksee(new SparkseeConfig())
                                                               .create(
                                                                       OCCASIONS_STORE_PATH,
                                                                       "Occasions");
                                                   }
                                               }))
                .subscribeOn(Schedulers.computation())
                .toBlocking()
                .single();
    }
}
