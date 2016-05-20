package com.anthropicandroid.extranetbrowser.view;

/*
 * Created by Andrew Brin on 5/11/2016.
 */

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;

import com.anthropicandroid.extranetbrowser.R;
import com.anthropicandroid.extranetbrowser.model.ExtranetDataStore;
import com.anthropicandroid.extranetbrowser.model.Occasion;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func2;

public class ExtranetMapView extends MapView {

    public static final String TAG = ExtranetMapView.class.getSimpleName();
    private ExtranetDataStore extranetDataStore;
    private GoogleMap myGoogleMap;

    public ExtranetMapView(Context context) {
        super(context);
        initialize(context);
    }

    public ExtranetMapView(Context context, AttributeSet attributes) {
        super(context, attributes);
        initialize(context);
    }

    private void initialize(Context context) {
        inflate(context, R.layout.extranet_map_view, this);
        extranetDataStore = new ExtranetDataStore(context.getFilesDir().getPath());
    }

    public void getMapAsync(BitmapDrawable bitmapDrawable, final List<String> keysToShow, final OnMapReadyCallback clientCallback) {
        // save bitmap
        // pass on to constructor
        getMapAsync(keysToShow, clientCallback);
    }

    public void getMapAsync(final List<String> keysToShow, final OnMapReadyCallback clientCallback) {
        returnMapToCallbackAndPopulate(clientCallback, getGoogleMapObservable(), extranetDataStore.getOccasionsSubsetObservable(keysToShow));
    }

    @Override
    public void getMapAsync(final OnMapReadyCallback clientCallback) {
        returnMapToCallbackAndPopulate(clientCallback, getGoogleMapObservable(), extranetDataStore.getGlobalOccasions());
    }

    private void returnMapToCallbackAndPopulate(
            final OnMapReadyCallback clientCallback,
            Observable<GoogleMap> googleMapObservable,
            Observable<Occasion> extranetMarkersObservable) {
        // On call, zip marker and map using marker thread once; add markers to map and return callback at some point
        Func2<GoogleMap, Occasion, MarkerOptions> mapToMarkerOptions = new Func2<GoogleMap, Occasion, MarkerOptions>() {
            @Override
            public MarkerOptions call(GoogleMap googleMap, Occasion occasion) {
                if (myGoogleMap == null)
                    myGoogleMap = googleMap;
                return new MarkerOptions()
                        .position(new LatLng(occasion.getLatitude(), occasion.getLongitude()));
            }
        };

        Observable.combineLatest(googleMapObservable, extranetMarkersObservable, mapToMarkerOptions)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<MarkerOptions>() {
                            @Override
                            public void call(MarkerOptions markerOptions) {
                                myGoogleMap.addMarker(markerOptions);
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
                                clientCallback.onMapReady(myGoogleMap); //  should be earlier
                                Log.d(TAG, "map marker populating observable completed");
                            }
                        });
    }

    private Observable<GoogleMap> getGoogleMapObservable() {
        // On subscription, get googlemap and return when onMapReady is called
        return Observable
                .create(new Observable.OnSubscribe<GoogleMap>() {
                    @Override
                    public void call(final Subscriber<? super GoogleMap> subscriber) {
                        getGoogleMap(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                subscriber.onNext(googleMap);
                            }
                        });
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    private void getGoogleMap(OnMapReadyCallback callback) {
        super.getMapAsync(callback);
    }

}
