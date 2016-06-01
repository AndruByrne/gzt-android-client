package com.anthropicandroid.extranetbrowser;

import android.support.annotation.NonNull;

import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.model.Occasion;
import com.anthropicandroid.extranetbrowser.modules.ContextModule;
import com.anthropicandroid.extranetbrowser.modules.DaggerExtranetMapViewTestComponent;
import com.anthropicandroid.extranetbrowser.modules.ExtranetAPIModule;
import com.anthropicandroid.extranetbrowser.modules.ExtranetMapViewTestComponent;
import com.anthropicandroid.extranetbrowser.modules.LocationModule;
import com.anthropicandroid.extranetbrowser.modules.MapModule;
import com.anthropicandroid.extranetbrowser.modules.TestExtranetAPIModule;
import com.anthropicandroid.extranetbrowser.modules.TestMapModule;
import com.anthropicandroid.extranetbrowser.modules.TestOccasionProviderModule;
import com.anthropicandroid.extranetbrowser.modules.TestWaspModule;
import com.anthropicandroid.extranetbrowser.testUtils.MapViewTestActivity;
import com.anthropicandroid.extranetbrowser.testUtils.RoboTestRunner;
import com.anthropicandroid.extranetbrowser.testUtils.TestingModel;
import com.anthropicandroid.extranetbrowser.view.ExtranetMapWrapper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
@Config(constants = BuildConfig.class)
public class ExtranetMapViewTest extends TestCase {

    public static final String TAG = ExtranetMapView.class.getSimpleName();
    private ExtranetMapView extranetMapView;
    private ExtranetOccasionProvider mockOccasionProvider;
    private ExtranetMapWrapper mockWrapper;
    private MapViewTestActivity testContext;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        testContext = Robolectric.setupActivity(MapViewTestActivity.class);
        mockOccasionProvider = mock(ExtranetOccasionProvider.class);
        mockWrapper = mock(ExtranetMapWrapper.class);
        ExtranetAPIModule.ExtranetAPI testExtranetAPI = mock(ExtranetAPIModule.ExtranetAPI.class);
        LocationModule testLocationModule = Mockito.mock(LocationModule.class);
        LatLng testCurrentLocation = new LatLng(TestingModel.centerOfTestingLatitude, TestingModel.centerOfTestingLongitude);
        when(testLocationModule.getLocationProvider()).thenReturn(Observable.just(testCurrentLocation));
        ExtranetMapViewTestComponent testComponent = DaggerExtranetMapViewTestComponent
                .builder()
                .contextModule(new ContextModule(testContext))
                .extranetAPIModule(new TestExtranetAPIModule(testExtranetAPI))
                .locationModule(testLocationModule)
                .mapModule(new TestMapModule(getGoogleMapAsyncGetter(), mockWrapper))
                .occasionProviderModule(new TestOccasionProviderModule(mockOccasionProvider))
                .waspModule(new TestWaspModule()) //  not needed for these tests
                .build();
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
    public void testGetMapAsyncOneParamUsesGlobalOccasionsToPopulateAGoogleMapAndReturnsCallback() throws Exception {
        // create mocks

        // undifferentiated occasions
        List<Occasion> globalOccasions = TestingModel.getMockGlobalOccasions();
        Observable<Occasion> testOccasionObservable = Observable.from(globalOccasions);
        when(mockOccasionProvider.getGlobalOccasions())
                .thenReturn(testOccasionObservable.take(3));
        // captor for markers added to map (wrapping object)
        ArgumentCaptor<MarkerOptions> markerToAddCaptor = ArgumentCaptor.forClass(MarkerOptions.class);
        // client callback
        OnMapReadyCallback mockReadyCallback = mock(OnMapReadyCallback.class);
        ArgumentCaptor<GoogleMap> googleMapCaptor = ArgumentCaptor.forClass(GoogleMap.class);

        // test function
        extranetMapView.getMapAsync(mockReadyCallback);

        // verify googleMap had markers added to it
        verify(mockWrapper, times(3)).addMarker(markerToAddCaptor.capture());
        List<MarkerOptions> addedMarkers = markerToAddCaptor.getAllValues();
        assertEquals(
                globalOccasions.get(0).getLatitude(),
                addedMarkers.get(0).getPosition().latitude);
        assertEquals(
                globalOccasions.get(2).getLongitude(),
                addedMarkers.get(2).getPosition().longitude);
        // check callback received a googleMap (actually null)
        verify(mockReadyCallback).onMapReady(googleMapCaptor.capture());
        List<GoogleMap> googleMaps = googleMapCaptor.getAllValues();
        assertEquals(1, googleMaps.size());
    }

    @Test
    public void testGetMapAsyncTwoParamUsesSubsetObservableToPopulateAGoogleMapWithAndReturnsCallback() throws Exception {
        // create mocks

        // differentiated occasions
        List<Occasion> occasionsSubset = TestingModel.getMockOccasionsSubset();
        List<String> mockRequestingKeys = TestingModel.getMockRequestingKeys();
        ArgumentCaptor<List> requestedKeysCaptor = ArgumentCaptor.forClass(List.class);
        when(mockOccasionProvider.getOccasionsSubset(requestedKeysCaptor.capture()))
                .thenReturn(Observable.from(occasionsSubset).take(3));
        // captor for markers added to map (wrapping object)
        ArgumentCaptor<MarkerOptions> markerToAddCaptor = ArgumentCaptor.forClass(MarkerOptions.class);
        // client callback
        OnMapReadyCallback mockReadyCallback = mock(OnMapReadyCallback.class);
        ArgumentCaptor<GoogleMap> googleMapCaptor = ArgumentCaptor.forClass(GoogleMap.class);

        // test function
        extranetMapView.getMapAsync(mockRequestingKeys, mockReadyCallback);

        // verify occasion provider received list of keys to retrieve
        List<String> capturedRequestedKeys = requestedKeysCaptor.getAllValues().get(0);
        assertEquals(
                mockRequestingKeys,
                capturedRequestedKeys);
        // verify googleMap had markers added to it
        verify(mockWrapper, times(3)).addMarker(markerToAddCaptor.capture());
        List<MarkerOptions> addedMarkers = markerToAddCaptor.getAllValues();
        assertEquals(
                occasionsSubset.get(0).getLatitude(),
                addedMarkers.get(0).getPosition().latitude);
        assertEquals(
                occasionsSubset.get(2).getLongitude(),
                addedMarkers.get(2).getPosition().longitude);
        // check callback received a googleMap (actually null)
        verify(mockReadyCallback).onMapReady(googleMapCaptor.capture());
        List<GoogleMap> googleMaps = googleMapCaptor.getAllValues();
        assertEquals(1, googleMaps.size());
    }

    @Test
    public void testGetMapAsync2() throws Exception {
        // NYI
    }

    @Test
    public void testNotifyMeOnRegistersAppParticulars() throws Exception{
        // not covered; the function should only call the static method in the Registration Service
    }
}