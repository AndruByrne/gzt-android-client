package com.anthropicandroid.extranetbrowser;

import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.model.Occasion;
import com.anthropicandroid.extranetbrowser.model.WaspHolder;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingRequest;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.observables.ConnectableObservable;


/*
 * Created by Andrew Brin on 5/31/2016.
 */
public class ExtranetRegistration {
    public static final String PACKAGE_NAME_EXTRA = "package_name_extra";
    public static final String TAG = ExtranetRegistration.class.getSimpleName();
    public static final String NOTIFICATION_TEXT_EXTRA = "notification_text_extra";
    public static final String NOTIFICATION_ICON_RES_ID = "notification_icon_res_id";
    public static final String DEFAULT_MAP_ICON_RES_ID = "default_map_icon_res_id";
    public static final int MAX_GEOFENCES = 80;
    public static final int GEOFENCE_EXPIRATION_IN_MILLISECONDS = 60480000;
    public static final int GEOFENCE_RESPONSIVENESS_IN_MILLISECONDS = 500;
    private final ConnectableObservable<GoogleApiClient> googleApiClientObservable;
    private final GeofencingApi geofencingApi;
    private final PendingIntent pendingIntent;
    private WaspHolder waspHolder;
    private ExtranetOccasionProvider extranetOccasionProvider;

    public ExtranetRegistration(
            Observable<GoogleApiClient> googleApiClientObservable,
            ExtranetOccasionProvider extranetOccasionProvider,
            GeofencingApi geofencingApi,
            PendingIntent pendingIntent,
            WaspHolder waspHolder) {
        this.extranetOccasionProvider = extranetOccasionProvider;
        this.googleApiClientObservable = googleApiClientObservable.replay();
        this.geofencingApi = geofencingApi;
        this.pendingIntent = pendingIntent;
        this.waspHolder = waspHolder;
        this.googleApiClientObservable.connect();
    }

    public void registerAppForKeys(Registration registration, List<String> requestedKeys) {
        // subscription to wait for both apiclient and occasionProvider, assign occasions to
        // geofences and register app
        waspHolder.clearBulkStringList(
                WaspHolder.BulkStringList.REQUESTED_BROADCAST_KEYS);
        Observable.combineLatest(
                googleApiClientObservable,
                extranetOccasionProvider
                        .getSegmentedOccasionsSubsetNoMoreThan(requestedKeys, MAX_GEOFENCES),
                // Combining function
                new Func2<GoogleApiClient, List<Occasion>, List<Occasion>>() {
                    @Override
                    public List<Occasion> call(
                            GoogleApiClient googleApiClient,
                            List<Occasion> occasions) {
                        if (occasions.size() == 0) return Lists.newArrayList();
                        try {
                            removeCurrentFences();
                            geofencingApi.addGeofences(
                                    googleApiClient,
                                    getGeofencingRequest(occasions),
                                    pendingIntent);
                            return occasions;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Lists.newArrayList();
                        }
                    }

                    @NonNull
                    private GeofencingRequest getGeofencingRequest(List<Occasion> occasions) {
                        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
                        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
                        for (Occasion occasion : occasions)
                            builder.addGeofence(
                                    new Geofence.Builder()
                                            .setCircularRegion(
                                                    occasion.getLatitude(),
                                                    occasion.getLongitude(),
                                                    occasion.getRadius())
                                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                            .setNotificationResponsiveness(
                                                    GEOFENCE_RESPONSIVENESS_IN_MILLISECONDS)
                                            .setRequestId(occasion.getKey())
                                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                            .build());
                        return builder.build();
                    }
                })
                .subscribe(new Action1<List<Occasion>>() {
                    @Override
                    public void call(List<Occasion> occasions) {
                        if (occasions.size() != 0) {
                            ArrayList<String> occasionKeys = new ArrayList<>();
                            for (Occasion o : occasions)
                                occasionKeys.add(o.getKey());
                            waspHolder.addToBulkStringList(
                                    WaspHolder.BulkStringList.REQUESTED_BROADCAST_KEYS,
                                    occasionKeys);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(
                                TAG,
                                "Error registering geofence with api: " + throwable.getMessage());
                    }
                });
    }

    private void removeCurrentFences() {
        googleApiClientObservable.subscribe(
                new Action1<GoogleApiClient>() {
                    @Override
                    public void call(GoogleApiClient googleApiClient) {
                        geofencingApi.removeGeofences(
                                googleApiClient,
                                pendingIntent);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "Error removing current fences: " + throwable.getMessage());
                    }
                }, new Action0() {
                    @Override
                    public void call() {

                    }
                }
        );
    }

    /*
         * Created by Andrew Brin on 5/30/2016.
         */
    public static class Registration {

        public final String packageName;
        public final String notificationText;
        public final Context context;
        public final int resIdNotificationIcon;
        public final int resIdDefaultMapIcon;

        public static class Builder {
            // required
            private final Context context;
            private final String packageName;
            // optional fields
            private String notificationText = "";
            private int resIdNotificationIcon = 0;
            private int resIdDefaultMapIcon = 0;

            public Builder(Context context) {
                this.context = context;
                this.packageName = context.getPackageName();
            }

            public Builder addNotificationIcon(int resId) {
                this.resIdNotificationIcon = resId;
                return this;
            }

            public Builder addNotificationText(String notification) {
                notificationText = notification;
                return this;
            }

            public Builder addDefaultMapIcon(int resId) {
                this.resIdDefaultMapIcon = resId;
                return this;
            }

            public Registration build() {
                return new Registration(this);
            }
        }

        private Registration(Builder builder) {
            packageName = builder.packageName;
            context = builder.context;
            resIdDefaultMapIcon = builder.resIdDefaultMapIcon;
            resIdNotificationIcon = builder.resIdNotificationIcon;
            notificationText = builder.notificationText;
        }
    }
}
