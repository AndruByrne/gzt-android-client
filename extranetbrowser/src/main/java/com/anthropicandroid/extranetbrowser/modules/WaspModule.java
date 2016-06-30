package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/24/2016.
 */

import android.content.Context;

import com.anthropicandroid.extranetbrowser.model.WaspHolder;

import dagger.Module;
import dagger.Provides;

@Module
public class WaspModule {

    @Provides
    @ExtranetMapViewScope
    public WaspHolder getWaspHolder(Context context) {
        return new WaspHolder(context);
    }

}
