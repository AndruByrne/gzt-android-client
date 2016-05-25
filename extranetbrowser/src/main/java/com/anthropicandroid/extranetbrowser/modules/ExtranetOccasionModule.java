package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/24/2016.
 */

import android.content.Context;

import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.model.WaspHolder;

import dagger.Module;
import dagger.Provides;

@Module
public class ExtranetOccasionModule {

    @Provides
    @ExtranetMapViewScope
    ExtranetOccasionProvider getExtranetOccasionProvider(Context context, WaspHolder waspHolder){
        return new ExtranetOccasionProvider(context, waspHolder);
    }

    @Provides
    @ExtranetMapViewScope
    WaspHolder getWaspHolder(Context context){
        return new WaspHolder(context.getFilesDir().getPath());
    }

}
