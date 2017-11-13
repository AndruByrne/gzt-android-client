package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/24/2016.
 */

import android.content.Context;

import com.anthropicandroid.extranetbrowser.model.PylonDAO;

import dagger.Module;
import dagger.Provides;

@Module
public class PylonDAOModule
{

    @Provides
    @ExtranetMapViewScope
    public PylonDAO getWaspHolder(Context context) {
        return new PylonDAO(context);
    }

}
