package com.anthropicandroid.extranetbrowser.view;

/*
 * Created by Andrew Brin on 5/11/2016.
 */

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;

import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.model.Occasion;
import com.anthropicandroid.extranetbrowser.modules.ContextModule;
import com.anthropicandroid.extranetbrowser.modules.DaggerExtranetMapViewComponent;
import com.anthropicandroid.extranetbrowser.modules.ExtranetMapViewComponent;
import com.anthropicandroid.extranetbrowser.modules.MapModule;
import com.anthropicandroid.extranetbrowser.modules.WaspModule;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func2;

public class ExtranetMapView extends MapView implements MapModule.GoogleMapAsyncGetter{

    public static final String TAG = ExtranetMapView.class.getSimpleName();

    @Inject public ExtranetOccasionProvider extranetOccasionProvider;
    @Inject public Observable<GoogleMap> googleMapObservable;

    private GoogleMap myGoogleMap;

    private ExtranetMapViewComponent extranetMapViewComponent;

    public ExtranetMapView(Context context) {
        super(context);
        initialize(context);
    }

    public ExtranetMapView(Context context, AttributeSet attributes) {
        super(context, attributes);
        initialize(context);
    }

    public void getMapAsync(BitmapDrawable bitmapDrawable, final List<String> keysToShow, final OnMapReadyCallback clientCallback) {
        // save & index bitmap
        // pass on to constructor
        getMapAsync(keysToShow, clientCallback);
    }

    public void getMapAsync(final List<String> keysToShow, final OnMapReadyCallback clientCallback) {
        populateAndReturnMapToCallback(
                clientCallback,
                googleMapObservable,
                extranetOccasionProvider.getOccasionsSubsetObservable(keysToShow));
    }

    @Override
    public void getMapAsync(final OnMapReadyCallback clientCallback) {
        populateAndReturnMapToCallback(
                clientCallback,
                googleMapObservable,
                extranetOccasionProvider.getGlobalOccasions());
    }

    void setExtranetMapViewComponent(ExtranetMapViewComponent extranetMapViewComponent) {
        this.extranetMapViewComponent = extranetMapViewComponent;
        extranetMapViewComponent.inject(this);
    }

    public void getSuperMapViewAsync(OnMapReadyCallback callback) {
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
                .waspModule(new WaspModule())
                .mapModule(new MapModule(this))
                .build();
        extranetMapViewComponent.inject(this);
    }

    private void populateAndReturnMapToCallback(
            final OnMapReadyCallback clientCallback,
            Observable<GoogleMap> googleMapObservable,
            Observable<Occasion> extranetOccasionsObservable) {

        Observable
                .combineLatest( //  perform combining function for every emission, after both have emitted at least once
                        googleMapObservable,
                        extranetOccasionsObservable,
                        // On call, combine marker and map; add markers to map and return callback at some point
                        new Func2<GoogleMap, Occasion, MapAndMarkers>() {
                            @Override
                            public MapAndMarkers call(GoogleMap googleMap, Occasion occasion) {
                                if (googleMap == null)
                                    Log.e(TAG, "Google map view returned null googleMap");
                                return new MapAndMarkers(
                                        googleMap,
                                        new MarkerOptions()
                                                .position(new LatLng( //  copy occasion position
                                                        occasion.getLatitude(),
                                                        occasion.getLongitude()))
                                        //  copy all other parameters from occasion
                                );
                            }
                        })
//                        mapToMarkerOptions) //  wait until both obs return first onNext, then combine with function
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<MapAndMarkers>() {
                            @Override
                            public void call(MapAndMarkers mapAndMarkers) {
                                if (myGoogleMap == null)
                                    myGoogleMap = mapAndMarkers.googleMap;
                                Log.e(TAG, "using null googleMap");
                                myGoogleMap.addMarker(mapAndMarkers.markerOptions);
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e(TAG, "Error in map marker populating observable: " + throwable.getMessage());
                                throwable.printStackTrace();
                            }
                        },
                        new Action0() {
                            @Override
                            public void call() {
                                if (myGoogleMap == null)
                                    Log.e(TAG, "returning null googleMap, perhaps no onNextCalled");
                                clientCallback.onMapReady(myGoogleMap); //  should be earlier
                                Log.d(TAG, "map marker populating observable completed");
                            }
                        });
    }

    private class MapAndMarkers {
        private final GoogleMap googleMap;
        private final MarkerOptions markerOptions;

        public MapAndMarkers(GoogleMap googleMap, MarkerOptions markerOptions) {
            this.googleMap = googleMap;
            this.markerOptions = markerOptions;
        }
    }
}
