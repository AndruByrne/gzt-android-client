package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 5/4/2016.
 */

import com.anthropicandroid.gzt.activity.GZTAnimator;
import com.anthropicandroid.gzt.activity.UserActionHandlers;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TouchHandlersModule {

    @Provides
    @Singleton
    UserActionHandlers getTouchHandlers(GZTAnimator gztAnimator){
        return new UserActionHandlers(gztAnimator);
    }

    @Provides
    @Singleton
    GZTAnimator getGZTAnimatorSetRepository(){
        return new GZTAnimator();
    }
}
