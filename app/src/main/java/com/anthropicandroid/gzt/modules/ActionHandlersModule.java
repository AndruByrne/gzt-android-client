package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 5/4/2016.
 */

import com.anthropicandroid.gzt.activity.BottomNavControllers;
import com.anthropicandroid.gzt.activity.GZTOverlayAnimator;
import com.anthropicandroid.gzt.activity.GZTZoomAnimator;
import com.anthropicandroid.gzt.activity.UpperActionHandlers;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ActionHandlersModule {

    @Provides
    @Singleton
    UpperActionHandlers getUpperActionHandlers(GZTZoomAnimator gztZoomAnimator) {
        return new UpperActionHandlers(gztZoomAnimator);
    }

    @Provides
    @Singleton
    GZTZoomAnimator getGZTAnimatorSetRepository() {
        return new GZTZoomAnimator();
    }

    @Provides
    @Singleton
    GZTOverlayAnimator getGZTOverlayAnimator() {
        return new GZTOverlayAnimator();
    }

    @Provides
    @Singleton
    BottomNavControllers getBottomNavHandlers(
            UpperActionHandlers upperActionHandlers,
            GZTOverlayAnimator gztOverlayAnimator) {
        return new BottomNavControllers(
                upperActionHandlers,
                gztOverlayAnimator);
    }
}
