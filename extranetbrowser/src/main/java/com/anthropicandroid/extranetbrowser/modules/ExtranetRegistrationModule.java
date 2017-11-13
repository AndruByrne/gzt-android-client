package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 6/9/2016.
 */

import android.app.PendingIntent;

import com.anthropicandroid.extranetbrowser.ExtranetRegistration;
import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.model.PylonDAO;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.GeofencingApi;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import rx.Observable;

@Module
public class ExtranetRegistrationModule {

    @Provides
    @ExtranetMapViewScope
    public ExtranetRegistration getExtranetRegistration(
            @Named("LocationServicesAPIClient") Observable<GoogleApiClient> apiClientObservable,
            ExtranetOccasionProvider extranetOccasionProvider,
            GeofencingApi geofencingApi,
            @Named("GeofencePendingIntent") PendingIntent pendingIntent,
            PylonDAO pylonDAO
    ) {
        return new ExtranetRegistration(
                apiClientObservable,
                extranetOccasionProvider, geofencingApi,
                pendingIntent,
                pylonDAO);
    }

}
