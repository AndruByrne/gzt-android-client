package com.anthropicandroid.extranetbrowser;

import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.model.Occasion;
import com.anthropicandroid.extranetbrowser.modules.ContextModule;
import com.anthropicandroid.extranetbrowser.modules.ExtranetAPIModule;
import com.anthropicandroid.extranetbrowser.modules.ExtranetMapViewTestComponent;
import com.anthropicandroid.extranetbrowser.modules.LocationModule;
import com.anthropicandroid.extranetbrowser.modules.MapModule;
import com.anthropicandroid.extranetbrowser.modules.TestExtranetAPIModule;
import com.anthropicandroid.extranetbrowser.modules.TestExtranetRegistrationModule;
import com.anthropicandroid.extranetbrowser.modules.TestMapModule;
import com.anthropicandroid.extranetbrowser.modules.TestOccasionProviderModule;
import com.anthropicandroid.extranetbrowser.modules.TestPylonDAOModule;
import com.anthropicandroid.extranetbrowser.testUtils.MapViewTestActivity;
import com.anthropicandroid.extranetbrowser.testUtils.RoboTestRunner;
import com.anthropicandroid.extranetbrowser.testUtils.TestingModel;
import com.anthropicandroid.extranetbrowser.view.ExtranetMapWrapper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.GeofencingApi;
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    public static final int MOCK_NOTIFICATION_ICON_RES_ID = 234;
    public static final int MOCK_DEFAULT_ICON_RES_ID = 654;
    public static final String MOCK_NOTIFICATION_TEXT = "Notification Text";

    private ExtranetMapView subject;
    private ExtranetOccasionProvider mockOccasionProvider;
    private ExtranetMapWrapper mockWrapper;
    private MapViewTestActivity testContext;
    private ExtranetRegistration mockRegistrar;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        testContext = Robolectric.setupActivity(MapViewTestActivity.class);
        mockOccasionProvider = mock(ExtranetOccasionProvider.class);
        mockWrapper = mock(ExtranetMapWrapper.class);
        mockRegistrar = Mockito.mock(ExtranetRegistration.class);
        ExtranetAPIModule.ExtranetAPI testExtranetAPI = mock(ExtranetAPIModule.ExtranetAPI.class);
        LocationModule testLocationModule = Mockito.mock(LocationModule.class);
        LatLng testCurrentLocation = new LatLng(TestingModel.centerOfTestingLatitude, TestingModel.centerOfTestingLongitude);
        when(testLocationModule.getLocationProvider()).thenReturn(Observable.just(testCurrentLocation));
        GeofencingApi testGeofencingApi = Mockito.mock(GeofencingApi.class);
        when(testLocationModule.getGeofencingApi()).thenReturn(testGeofencingApi);
        GoogleApiClient testGoogleApiClient = Mockito.mock(GoogleApiClient.class);
        when(testLocationModule.getGoogleApiClient(any(Context.class))).thenReturn(Observable.just(testGoogleApiClient));
        PendingIntent testPendingIntent = Mockito.mock(PendingIntent.class);
        when(testLocationModule.getPendingIntent(any(Context.class))).thenReturn(testPendingIntent);
        ExtranetMapViewTestComponent testComponent = DaggerExtranetMapViewTestComponent
                .builder()
                .contextModule(new ContextModule(testContext))
                .extranetAPIModule(new TestExtranetAPIModule(testExtranetAPI))
                .extranetRegistrationModule(new TestExtranetRegistrationModule(mockRegistrar))
                .locationModule(testLocationModule)
                .mapModule(new TestMapModule(getGoogleMapAsyncGetter(), mockWrapper))
                .occasionProviderModule(new TestOccasionProviderModule(mockOccasionProvider))
                .waspModule(new TestPylonDAOModule()) //  not needed for these tests
                .build();
        subject = new ExtranetMapView(testContext, testComponent);
    }

    @NonNull
    private MapModule.GoogleMapAsyncGetter getGoogleMapAsyncGetter() {
        return new MapModule.GoogleMapAsyncGetter() {
            @Override
            public void getGoogleMapViewAsync(OnMapReadyCallback callback) {
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
        when(mockOccasionProvider.getContinuousGlobalOccasions())
                .thenReturn(testOccasionObservable.take(3));
        // captor for markers added to map (wrapping object)
        ArgumentCaptor<MarkerOptions> markerToAddCaptor = ArgumentCaptor.forClass(MarkerOptions.class);
        // client callback
        OnMapReadyCallback mockReadyCallback = mock(OnMapReadyCallback.class);
        ArgumentCaptor<GoogleMap> googleMapCaptor = ArgumentCaptor.forClass(GoogleMap.class);

        // test function
        subject.getMapAsync(mockReadyCallback);

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
        verify(mockRegistrar, never()).registerAppForKeys(any(ExtranetRegistration.Registration.class), any(List.class));
    }

    @Test
    public void testGetMapAsyncTwoParamUsesSubsetObservableToPopulateAGoogleMapWithAndReturnsCallback() throws Exception {
        // create mocks

        // differentiated occasions
        List<Occasion> occasionsSubset = TestingModel.getMockOccasionsSubset();
        List<String> mockRequestingKeys = TestingModel.getMockRequestingKeys();
        ArgumentCaptor<List> requestedKeysCaptor = ArgumentCaptor.forClass(List.class);
        when(mockOccasionProvider.getContinuousOccasionsSubset(requestedKeysCaptor.capture()))
                .thenReturn(Observable.from(occasionsSubset).take(3));
        // captor for markers added to map (wrapping object)
        ArgumentCaptor<MarkerOptions> markerToAddCaptor = ArgumentCaptor.forClass(MarkerOptions.class);
        // client callback
        OnMapReadyCallback mockReadyCallback = mock(OnMapReadyCallback.class);
        ArgumentCaptor<GoogleMap> googleMapCaptor = ArgumentCaptor.forClass(GoogleMap.class);

        // test function
        subject.getMapAsync(mockRequestingKeys, mockReadyCallback);

        // verify occasion provider received list of keys to retrieve
        List<String> capturedRequestedKeys = requestedKeysCaptor.getAllValues().get(0);
        assertEquals(
                mockRequestingKeys,
                capturedRequestedKeys);
        Log.d(TAG, "mockReqKeys: "+mockRequestingKeys.toString());
        Log.d(TAG, "capturedReqKEys: "+capturedRequestedKeys.toString());
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
        verify(mockRegistrar, never()).registerAppForKeys(any(ExtranetRegistration.Registration.class), any(List.class));
    }

    @Test
    public void testBroadcastToMePassesAlongAppParticulars() throws Exception {
        // set up function inputs
        List<String> mockRequestingKeys = TestingModel.getMockRequestingKeys();
        ExtranetRegistration.Registration.Builder builder = new ExtranetRegistration.Registration
                .Builder(testContext)
                .addNotificationText(MOCK_NOTIFICATION_TEXT)
                .addNotificationIcon(MOCK_NOTIFICATION_ICON_RES_ID)
                .addDefaultMapIcon(MOCK_DEFAULT_ICON_RES_ID);
        ExtranetRegistration.Registration testRegistration = builder.build();

        // mocks
//        subject.broadcastToMeOnOccasions();
        ArgumentCaptor<ExtranetRegistration.Registration> registrationCaptor = ArgumentCaptor.forClass(ExtranetRegistration.Registration.class);
        ArgumentCaptor<List> requestedKeysCaptor = ArgumentCaptor.forClass(List.class);

        // test function
        subject.broadcastToMeOnOccasions(testRegistration, mockRequestingKeys);

        verify(mockRegistrar).registerAppForKeys(registrationCaptor.capture(), requestedKeysCaptor.capture());
        List<String> capturedRequestedKeys = requestedKeysCaptor.getAllValues().get(0);
        assertEquals(
                testRegistration,
                registrationCaptor.getValue());
        assertEquals(
                mockRequestingKeys,
                capturedRequestedKeys);

    }
}