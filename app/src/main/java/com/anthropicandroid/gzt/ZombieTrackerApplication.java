package com.anthropicandroid.gzt;

import android.app.Application;

import com.anthropicandroid.gzt.modules.AppModule;
import com.anthropicandroid.gzt.modules.ApplicationComponent;
import com.anthropicandroid.gzt.modules.DaggerApplicationComponent;
import com.anthropicandroid.gzt.modules.DaggerGZTMapComponent;
import com.anthropicandroid.gzt.modules.DaggerSansUserSettingsAdapterComponent;
import com.anthropicandroid.gzt.modules.DaggerUserComponent;
import com.anthropicandroid.gzt.modules.GZTMapComponent;
import com.anthropicandroid.gzt.modules.SansUserSettingsAdapterComponent;
import com.anthropicandroid.gzt.modules.ThreadingModule;
import com.anthropicandroid.gzt.modules.UserComponent;


public class ZombieTrackerApplication extends Application {
    private ApplicationComponent applicationComponent;
    public static String TAG = ZombieTrackerApplication.class.getSimpleName();
    private static ZombieTrackerApplication instance;
    private UserComponent userComponent = null;
    private SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent;
    private GZTMapComponent mapComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        applicationComponent = DaggerApplicationComponent.builder()
                .appModule(new AppModule(this))
                .threadingModule(getThreadingModule()) //  getting for overriding with mocks in testing
                .build();
    }

    public GZTMapComponent createMapComponent() {
        if (mapComponent==null) {
            mapComponent = DaggerGZTMapComponent
                    .builder()
                    .sansUserSettingsAdapterComponent(sansUserSettingsAdapterComponent)
                    .build();
        }
        return mapComponent;
    }

    public UserComponent createUserComponent(String hailingEmail) {
        userComponent = DaggerUserComponent
                .builder()
                .applicationComponent(applicationComponent)
                .build();
        return userComponent;
    }

    public SansUserSettingsAdapterComponent createSansUserAdapterComponent() {
        sansUserSettingsAdapterComponent = DaggerSansUserSettingsAdapterComponent
                .builder()
                .applicationComponent(applicationComponent)
                .build();
        return sansUserSettingsAdapterComponent;
    }

    public static ZombieTrackerApplication getInstance() {
        return instance;
    }

    public ThreadingModule getThreadingModule() {
        return new ThreadingModule();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    public void releaseUserComponent() {
        userComponent = null;
    }

    public void releaseSansUserAdapterComponent() { sansUserSettingsAdapterComponent = null; }

    public void releaseMapComponent() { mapComponent = null; }
}
