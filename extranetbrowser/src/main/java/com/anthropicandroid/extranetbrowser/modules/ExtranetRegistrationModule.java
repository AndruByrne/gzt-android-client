package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 6/9/2016.
 */

import com.anthropicandroid.extranetbrowser.ExtranetRegistration;
import com.anthropicandroid.extranetbrowser.model.WaspHolder;

import dagger.Module;
import dagger.Provides;

@Module
public class ExtranetRegistrationModule {

    @Provides
    @ExtranetMapViewScope
    public ExtranetRegistration getExtranetRegistration(WaspHolder waspHolder){
        return new ExtranetRegistration(waspHolder);
    }

}
