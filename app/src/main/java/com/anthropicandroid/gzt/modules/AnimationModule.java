package com.anthropicandroid.gzt.modules;

import android.app.Application;

import com.anthropicandroid.gzt.activity.OpenStoreAnimator;
import com.anthropicandroid.gzt.activity.OverlayAnimator;
import com.anthropicandroid.gzt.activity.ZoomAnimator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/*
 * Created by Andrew Brin on 7/31/2016.
 */

@Module
public class AnimationModule {

    @Provides
    @Singleton
    ZoomAnimator getGZTAnimatorSetRepository() {
        return new ZoomAnimator();
    }

    @Provides
    @Singleton
    OverlayAnimator getGZTOverlayAnimator(Application context) {

        return new OverlayAnimator(context.getResources());
    }

    @Provides
    @Singleton
    OpenStoreAnimator getOpenStoreAnimator(Application context) {
        return new OpenStoreAnimator(context.getResources()); }

}
