package com.anthropicandroid.gzt.modules;

import android.app.Application;

import com.anthropicandroid.gzt.activity.TouchHandlers;
import com.anthropicandroid.gzt.services.ApplicationPreferences;
import com.anthropicandroid.gzt.services.SubscriptionAccountant;

import javax.inject.Singleton;

import dagger.Component;

/*
 * Created by Andrew Brin on 3/1/2016.
 */

@Singleton //  Singleton is the annotation for the Application scope
@Component(modules = {
        AppModule.class,
        PreferenceStorageModule.class,
        SubscriptionAccountantModule.class,
        TouchHandlersModule.class,
        ThreadingModule.class})
public interface ApplicationComponent {

    Application getContext();

    ApplicationPreferences getPreferenceStorage();

    SubscriptionAccountant getSubscriptionAccountant();

    TouchHandlers getTouchHandlers(); //  for the first View Model

}
