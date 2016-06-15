package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/29/2016.
 */

import com.anthropicandroid.extranetbrowser.model.Occasion;

import dagger.Module;
import dagger.Provides;
import rx.Observable;

@Module
public class ExtranetAPIModule {

    @Provides
    @ExtranetMapViewScope
    public ExtranetAPI getTestExtranetAPI(){
        return null;
    }

    public interface ExtranetAPI{
        public Observable<Occasion> getOccasionsFromLocation(double latitude, double longitude, String... keys);
        public Observable<Occasion> getOccasionsFromLocation(double latitude, double longitude);
    }
}
