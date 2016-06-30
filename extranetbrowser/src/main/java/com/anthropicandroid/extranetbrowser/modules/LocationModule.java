package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/29/2016.
 */

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.anthropicandroid.extranetbrowser.model.GeofencingOccasionService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

@Module
public class LocationModule {
    public static final double centerOfDemoLatitude = 37.860d;
    public static final double centerOfDemoLongitude = -122.487d;

    @Provides
    @ExtranetMapViewScope
    @Named("LocationProvider")
    public Observable<LatLng> getLocationProvider() {
        return Observable.just(new LatLng(centerOfDemoLatitude, centerOfDemoLongitude));
    }

    @Provides
    @ExtranetMapViewScope
    public GeofencingApi getGeofencingApi() {
        return LocationServices.GeofencingApi;
    }

    @Provides
    @ExtranetMapViewScope
    @Named("GeofencePendingIntent")
    public PendingIntent getPendingIntent(Context context) {
        return PendingIntent.getService(
                context,
                1,
                new Intent(
                        context,
                        GeofencingOccasionService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Provides
    @ExtranetMapViewScope
    @Named("LocationServicesAPIClient")
    public Observable<GoogleApiClient> getGoogleApiClient(final Context context) {
        return Observable.create(new Observable.OnSubscribe<GoogleApiClient>() {
            @Override
            public void call(final Subscriber<? super GoogleApiClient> subscriber) {
                final GoogleApiClient client = new GoogleApiClient.Builder(context)
                        .addApi(LocationServices.API)
                        .build();
                client.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        subscriber.onNext(client);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        subscriber.onError(new RuntimeException("APIClient connection suspended"));
                    }
                });
                client.registerConnectionFailedListener(new GoogleApiClient
                        .OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        subscriber.onError(new RuntimeException("APIClient connection failed: " +
                                connectionResult.getErrorMessage()));
                        // nothing, will retry
                    }
                });
                client.connect(GoogleApiClient.SIGN_IN_MODE_REQUIRED);
            }
        }).take(1).subscribeOn(Schedulers.io());
    }

}
