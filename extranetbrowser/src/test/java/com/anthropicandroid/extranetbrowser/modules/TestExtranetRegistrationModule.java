package com.anthropicandroid.extranetbrowser.modules;

import com.anthropicandroid.extranetbrowser.ExtranetRegistration;
import com.anthropicandroid.extranetbrowser.model.WaspHolder;

/*
 * Created by Andrew Brin on 6/10/2016.
 */
public class TestExtranetRegistrationModule extends ExtranetRegistrationModule {
    private ExtranetRegistration testExtranetRegistration;

    public TestExtranetRegistrationModule(ExtranetRegistration testExtranetRegistration){
        this.testExtranetRegistration = testExtranetRegistration;
    }

    @Override
    public ExtranetRegistration getExtranetRegistration(WaspHolder waspHolder){
        return testExtranetRegistration;
    }
}
