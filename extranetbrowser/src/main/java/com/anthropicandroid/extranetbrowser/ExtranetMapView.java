package com.anthropicandroid.extranetbrowser;

/*
 * Created by Andrew Brin on 5/11/2016.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.model.Occasion;
import com.anthropicandroid.extranetbrowser.modules.ContextModule;
import com.anthropicandroid.extranetbrowser.modules.DaggerExtranetMapViewComponent;
import com.anthropicandroid.extranetbrowser.modules.ExtranetAPIModule;
import com.anthropicandroid.extranetbrowser.modules.ExtranetMapViewComponent;
import com.anthropicandroid.extranetbrowser.modules.ExtranetRegistrationModule;
import com.anthropicandroid.extranetbrowser.modules.LocationModule;
import com.anthropicandroid.extranetbrowser.modules.MapModule;
import com.anthropicandroid.extranetbrowser.modules.OccasionProviderModule;
import com.anthropicandroid.extranetbrowser.modules.PylonDAOModule;
import com.anthropicandroid.extranetbrowser.view.ExtranetMapWrapper;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;

public class ExtranetMapView extends MapView implements MapModule.GoogleMapAsyncGetter {

    public static final String TAG = ExtranetMapView.class.getSimpleName();

    @Inject public ExtranetOccasionProvider extranetOccasionProvider;
    @Inject public Observable<ExtranetMapWrapper> googleMapObservable;
    @Inject public ExtranetRegistration extranetRegistration;

    private ExtranetMapWrapper mapWrapper;

    private ExtranetMapViewComponent extranetMapViewComponent;

    public ExtranetMapView(Context context) {
        super(context);
        initialize(context);
    }

    public ExtranetMapView(Context context, AttributeSet attributes) {
        super(context, attributes);
        initialize(context);
    }

    ExtranetMapView(Context context, ExtranetMapViewComponent mapViewComponent) {
        super(context);
        mapViewComponent.inject(this);
    }

    public void broadcastToMeOnOccasions(
            ExtranetRegistration.Registration registration,
            List<String> requestedKeys) {
        if (registration == null) Log.e(TAG, "registration required for notification");
        else extranetRegistration.registerAppForKeys(
                registration,
                requestedKeys != null ? requestedKeys : new ArrayList<String>());
    }

    public void getMapAsync(
            final List<String> keysToShow,
            final OnMapReadyCallback clientCallback) {
        populateAndReturnMapToCallback(
                clientCallback,
                googleMapObservable,
                extranetOccasionProvider.getContinuousOccasionsSubset(keysToShow));
    }

    @Override
    public void getMapAsync(final OnMapReadyCallback clientCallback) {
        populateAndReturnMapToCallback(
                clientCallback,
                googleMapObservable,
                extranetOccasionProvider.getContinuousGlobalOccasions());
    }

    public void getGoogleMapViewAsync(OnMapReadyCallback callback) {
        super.getMapAsync(callback);
    }

    @Override
    protected void onDetachedFromWindow() {
        extranetMapViewComponent = null;
        super.onDetachedFromWindow();
    }

    private void initialize(Context context) {
        extranetMapViewComponent = DaggerExtranetMapViewComponent
                .builder()
                .contextModule(new ContextModule(context))
                .extranetAPIModule(new ExtranetAPIModule())
                .extranetRegistrationModule(new ExtranetRegistrationModule())
                .locationModule(new LocationModule())
                .mapModule(new MapModule(this))
                .occasionProviderModule(new OccasionProviderModule())
//                .pylonDAO(new PylonDAOModule(context.getFilesDir().getPath()))
                .build();
        extranetMapViewComponent.inject(this);
    }

    private void populateAndReturnMapToCallback(
            final OnMapReadyCallback clientCallback,
            Observable<ExtranetMapWrapper> googleMapObservable,
            Observable<Occasion> extranetOccasionsObservable) {
        Observable
                .combineLatest(
                        //  perform combining function for every emission, after both have
                        // emitted at least once
                        googleMapObservable,
                        extranetOccasionsObservable,
                        // On call, combine marker and map; add markers to map and return
                        // callback at some point
                        new Func2<ExtranetMapWrapper, Occasion, MapAndMarkers>() {
                            @Override
                            public MapAndMarkers call(
                                    ExtranetMapWrapper mapWrapper,
                                    Occasion occasion) {
                                if (mapWrapper == null)
                                    Log.e(TAG, "Google map view returned null mapWrapper");
                                return new MapAndMarkers(
                                        mapWrapper,
                                        new MarkerOptions()
                                                .position(new LatLng( //  copy occasion position
                                                        occasion.getLatitude(),
                                                        occasion.getLongitude()))
                                        //  copy all other parameters from occasion
                                );
                            }
                        })
//                        mapToMarkerOptions) //  wait until both obs return first onNext, then
// combine with function
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<MapAndMarkers>() {
                            @Override
                            public void call(MapAndMarkers mapAndMarkers) {
                                if (mapWrapper == null) {
                                    mapWrapper = mapAndMarkers.mapWrapper; //  assign to local
                                    clientCallback.onMapReady(mapWrapper.getGoogleMap()); //
                                    // return google map
                                }
                                mapWrapper.addMarker(mapAndMarkers.markerOptions);
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e(
                                        TAG,
                                        "Error in map marker populating observable: " + throwable
                                                .getMessage());
                                throwable.printStackTrace();
                            }
                        });
        // TODO(Andrew Brin): publish and make a second obs that takes elementAtOrDefault
    }

    private class MapAndMarkers {
        private final ExtranetMapWrapper mapWrapper;
        private final MarkerOptions markerOptions;

        public MapAndMarkers(ExtranetMapWrapper mapWrapper, MarkerOptions markerOptions) {
            this.mapWrapper = mapWrapper;
            this.markerOptions = markerOptions;
        }
    }
}
