package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 5/4/2016.
 */

import com.anthropicandroid.gzt.activity.GZTOverlayAnimator;
import com.anthropicandroid.gzt.activity.GZTZoomAnimator;
import com.anthropicandroid.gzt.activity.UserActionHandlers;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TouchHandlersModule {

    @Provides
    @Singleton
    UserActionHandlers getTouchHandlers(GZTZoomAnimator gztZoomAnimator, GZTOverlayAnimator gztOverlayAnimator){
        return new UserActionHandlers(gztZoomAnimator, gztOverlayAnimator);
    }

    @Provides
    @Singleton
    GZTZoomAnimator getGZTAnimatorSetRepository(){
        return new GZTZoomAnimator();
    }

    @Provides
    @Singleton
    GZTOverlayAnimator getGZTOverlayAnimator(){
        return new GZTOverlayAnimator();
    }
}
