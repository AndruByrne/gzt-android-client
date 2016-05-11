package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 5/4/2016.
 */

import com.anthropicandroid.gzt.activity.GZTAnimatorRepository;
import com.anthropicandroid.gzt.activity.TouchHandlers;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TouchHandlersModule {

    @Provides
    @Singleton
    TouchHandlers getTouchHandlers(GZTAnimatorRepository gztAnimatorRepository){
        return new TouchHandlers(gztAnimatorRepository);
    }

    @Provides
    @Singleton
    GZTAnimatorRepository getGZTAnimatorSetRepository(){
        return new GZTAnimatorRepository();
    }
}
