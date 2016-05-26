package com.anthropicandroid.extranetbrowser.view;

import android.support.annotation.NonNull;

import com.anthropicandroid.extranetbrowser.BuildConfig;
import com.anthropicandroid.extranetbrowser.MapViewTestActivity;
import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.model.Occasion;
import com.anthropicandroid.extranetbrowser.modules.ContextModule;
import com.anthropicandroid.extranetbrowser.modules.MapModule;
import com.anthropicandroid.extranetbrowser.testUtils.DaggerExtranetMapViewTestComponent;
import com.anthropicandroid.extranetbrowser.testUtils.ExtranetMapViewTestComponent;
import com.anthropicandroid.extranetbrowser.testUtils.RoboTestRunner;
import com.anthropicandroid.extranetbrowser.testUtils.TestMapModule;
import com.anthropicandroid.extranetbrowser.testUtils.TestOccasionProviderModule;
import com.anthropicandroid.extranetbrowser.testUtils.TestWaspModule;
import com.anthropicandroid.extranetbrowser.testUtils.TestingModel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

import rx.Observable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Created by Andrew Brin on 5/20/2016.
 */

@RunWith(RoboTestRunner.class)
@Config(
        constants = BuildConfig.class)
public class ExtranetMapViewTest extends TestCase {

    private MapViewTestActivity testContext;
    private ExtranetMapView extranetMapView;
    private ExtranetOccasionProvider mockOccasionProvider;
    private ExtranetMapWrapper mockWrapper;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        testContext = Robolectric.setupActivity(MapViewTestActivity.class);
        mockOccasionProvider = mock(ExtranetOccasionProvider.class);
        mockWrapper = mock(ExtranetMapWrapper.class);
        ExtranetMapViewTestComponent testComponent = DaggerExtranetMapViewTestComponent
                .builder()
                .contextModule(new ContextModule(testContext))
                .occasionProviderModule(new TestOccasionProviderModule(mockOccasionProvider))
                .mapModule(new TestMapModule(getGoogleMapAsyncGetter(), mockWrapper))
                .waspModule(new TestWaspModule()) //  not needed for these tests
                .build();
        testComponent.inject(this);
        extranetMapView = new ExtranetMapView(testContext, testComponent);
    }

    @NonNull
    private MapModule.GoogleMapAsyncGetter getGoogleMapAsyncGetter() {
        return new MapModule.GoogleMapAsyncGetter() {
            @Override
            public void getSuperMapViewAsync(OnMapReadyCallback callback) {
                callback.onMapReady(null); //  value not used
            }
        };
    }

    @Test
    public void testGetMapAsyncOneParamPopulatesAGoogleMapAndReturnsCallback() throws Exception {

        // create mocks for endpoints
        List<Occasion> globalOccasions = TestingModel.getGlobalOccasions();
        Observable<Occasion> testOccasionObservable = Observable.from(globalOccasions);
        when(mockOccasionProvider.getGlobalOccasions()).thenReturn(testOccasionObservable);
        ArgumentCaptor<MarkerOptions> markerToAddCaptor = ArgumentCaptor.forClass(MarkerOptions.class);

        OnMapReadyCallback mockReadyCallback = mock(OnMapReadyCallback.class);
        ArgumentCaptor<GoogleMap> googleMapCaptor = ArgumentCaptor.forClass(GoogleMap.class);

        // test function
        extranetMapView.getMapAsync(mockReadyCallback);

        // verify googleMap had markers added to it
        verify(mockWrapper, times(3)).addMarker(markerToAddCaptor.capture());
        List<MarkerOptions> addedMarkers = markerToAddCaptor.getAllValues();
        assertEquals(
                addedMarkers.get(0).getPosition().latitude,
                globalOccasions.get(0).getLatitude());
        assertEquals(
                addedMarkers.get(2).getPosition().longitude,
                globalOccasions.get(2).getLongitude());

        // check callback received a googleMap (actually null)
        verify(mockReadyCallback).onMapReady(googleMapCaptor.capture());
        List<GoogleMap> googleMaps = googleMapCaptor.getAllValues();
        assertEquals(1, googleMaps.size());
    }

    @Test
    public void testGetMapAsync1() throws Exception {

    }

    @Test
    public void testGetMapAsync2() throws Exception {

    }
}