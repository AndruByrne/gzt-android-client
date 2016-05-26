package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/25/2016.
 */

import android.content.Context;

import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.model.WaspHolder;

import dagger.Module;
import dagger.Provides;

@Module
public class OccasionProviderModule {

    @Provides
    @ExtranetMapViewScope
    public ExtranetOccasionProvider getExtranetOccasionProvider(Context context, WaspHolder waspHolder){
        return new ExtranetOccasionProvider(context, waspHolder);
    }
}
