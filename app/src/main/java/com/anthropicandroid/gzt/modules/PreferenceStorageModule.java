package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 5/6/2016.
 */

import android.app.Application;

import com.anthropicandroid.gzt.services.ApplicationPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PreferenceStorageModule {

    @Provides
    @Singleton
    ApplicationPreferences getPreferenceStorage(Application application){
        return new ApplicationPreferences(application);
    }
}
