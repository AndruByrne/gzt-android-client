package com.anthropicandroid.extranetbrowser.modules;

import android.app.PendingIntent;

import com.anthropicandroid.extranetbrowser.ExtranetRegistration;
import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.model.WaspHolder;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.GeofencingApi;

import javax.inject.Named;

import rx.Observable;

/*
 * Created by Andrew Brin on 6/10/2016.
 */
public class TestExtranetRegistrationModule extends ExtranetRegistrationModule {
    private ExtranetRegistration testExtranetRegistration;

    public TestExtranetRegistrationModule(ExtranetRegistration testExtranetRegistration){
        this.testExtranetRegistration = testExtranetRegistration;
    }

    @Override
    public ExtranetRegistration getExtranetRegistration(@Named("LocationServicesAPIClient") Observable<GoogleApiClient> apiClientObservable,
                                                        ExtranetOccasionProvider extranetOccasionProvider,
                                                        GeofencingApi geofencingApi,
                                                        @Named("GeofencePendingIntent") PendingIntent pendingIntent,
                                                        WaspHolder waspHolder){
        return testExtranetRegistration;
    }
}
