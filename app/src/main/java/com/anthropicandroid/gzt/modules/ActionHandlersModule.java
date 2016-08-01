package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 5/4/2016.
 */

import com.anthropicandroid.gzt.activity.BottomNavControllers;
import com.anthropicandroid.gzt.activity.OpenStoreAnimator;
import com.anthropicandroid.gzt.activity.OverlayAnimator;
import com.anthropicandroid.gzt.activity.UpperActionHandlers;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ActionHandlersModule {

    @Provides
    @Singleton
    UpperActionHandlers getUpperActionHandlers(OpenStoreAnimator openStoreAnimator) {
        return new UpperActionHandlers(openStoreAnimator);
    }

    @Provides
    @Singleton
    BottomNavControllers getBottomNavHandlers(
            UpperActionHandlers upperActionHandlers,
            OverlayAnimator overlayAnimator) {
        return new BottomNavControllers(
                upperActionHandlers,
                overlayAnimator);
    }
}
