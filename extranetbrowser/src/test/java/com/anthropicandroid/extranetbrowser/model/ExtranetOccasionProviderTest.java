package com.anthropicandroid.extranetbrowser.model;

import android.support.annotation.NonNull;

import com.anthropicandroid.extranetbrowser.BuildConfig;
import com.anthropicandroid.extranetbrowser.modules.ContextModule;
import com.anthropicandroid.extranetbrowser.modules.DaggerExtranetMapViewTestComponent;
import com.anthropicandroid.extranetbrowser.modules.ExtranetAPIModule;
import com.anthropicandroid.extranetbrowser.modules.ExtranetMapViewTestComponent;
import com.anthropicandroid.extranetbrowser.modules.LocationModule;
import com.anthropicandroid.extranetbrowser.modules.MapModule;
import com.anthropicandroid.extranetbrowser.modules.OccasionProviderModule;
import com.anthropicandroid.extranetbrowser.modules.TestExtranetAPIModule;
import com.anthropicandroid.extranetbrowser.modules.TestMapModule;
import com.anthropicandroid.extranetbrowser.modules.TestWaspModule;
import com.anthropicandroid.extranetbrowser.testUtils.MapViewTestActivity;
import com.anthropicandroid.extranetbrowser.testUtils.RoboTestRunner;
import com.anthropicandroid.extranetbrowser.testUtils.TestingModel;
import com.anthropicandroid.extranetbrowser.view.ExtranetMapWrapper;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
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
public class ExtranetOccasionProviderTest extends TestCase {

    public static final String TAG = ExtranetOccasionProviderTest.class.getSimpleName();

    @Inject
    ExtranetOccasionProvider subject;

    @Inject
    WaspHolder mockWaspHolder;

    @Inject
    ExtranetAPIModule.ExtranetAPI mockExtranetAPI;

    private LocationModule testLocationModule;

    @Before
    public void setUp() throws Exception {
        MapViewTestActivity testContext = Robolectric.setupActivity(MapViewTestActivity.class);
        ExtranetMapWrapper testWrapper = mock(ExtranetMapWrapper.class);
        ExtranetAPIModule.ExtranetAPI testExtranetAPI = mock(ExtranetAPIModule.ExtranetAPI.class);
        testLocationModule = Mockito.mock(LocationModule.class);
        LatLng testCurrentLocation = new LatLng(TestingModel.centerOfTestingLatitude, TestingModel.centerOfTestingLongitude);
        when(testLocationModule.getLocationProvider()).thenReturn(Observable.just(testCurrentLocation));
        ExtranetMapViewTestComponent testComponent = DaggerExtranetMapViewTestComponent
                .builder()
                .contextModule(new ContextModule(testContext))
                .extranetAPIModule(new TestExtranetAPIModule(testExtranetAPI))
                .locationModule(testLocationModule)
                .mapModule(new TestMapModule(getGoogleMapAsyncGetter(), testWrapper))
                .occasionProviderModule(new OccasionProviderModule()) //  provides class under test
                .waspModule(new TestWaspModule())
                .build();
        testComponent.inject(this);
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
    public void testGetOccasionsSubsetObservable() throws Exception {
        // mock calling parameters
        final List<String> mockRequestingKeys = TestingModel.getMockRequestingKeys();
        // mock getCachedOccasion
        final List<Occasion> mockOccasionsSubset = TestingModel.getMockOccasionsSubset();
        ArgumentCaptor<String> requestedKeyCaptor = ArgumentCaptor.forClass(String.class);
        when(mockWaspHolder.getCachedOccasion(requestedKeyCaptor.capture()))
                .thenReturn(mockOccasionsSubset.get(0))
                .thenReturn(mockOccasionsSubset.get(1))
                .thenReturn(null)
                .thenReturn(mockOccasionsSubset.get(3));
        when(mockWaspHolder.getKeysForErroneousOccasions())
                .thenReturn(new ArrayList<String>(){{add(mockRequestingKeys.get(2));}});
        // mock getOccasionsFromExtranet
        ArgumentCaptor<String> networkKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> networkLatitudeCaptor = ArgumentCaptor.forClass(double.class);
        ArgumentCaptor<Double> networkLongitudeCaptor = ArgumentCaptor.forClass(double.class);
        when(mockExtranetAPI
                .getOccasionsFromLocation(
                        networkLatitudeCaptor.capture(),
                        networkLongitudeCaptor.capture(),
                        networkKeyCaptor.capture()))
                .thenReturn(Observable.just(mockOccasionsSubset.get(2)));

        // test method
        Observable<Occasion> occasionsSubset = subject.getContinuousOccasionsSubset(mockRequestingKeys);
        TestSubscriber<Occasion> occasionTestSubscriber = new TestSubscriber<>();
        occasionsSubset.subscribe(occasionTestSubscriber);

        // should pass requested keys to DB holder
        assertEquals(
                mockRequestingKeys,
                requestedKeyCaptor.getAllValues());
        // requested keys should be stored exactly as is
        ArgumentCaptor<List> bulkListAddCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<WaspHolder.BulkStringList> bulkListTypeCaptor = ArgumentCaptor.forClass(WaspHolder.BulkStringList.class);
        verify(mockWaspHolder).setBulkStringList(
                bulkListTypeCaptor.capture(),
                bulkListAddCaptor.capture());
        assertEquals(
                bulkListTypeCaptor.getValue(),
                WaspHolder.BulkStringList.RECENTLY_DISPLAYED_KEYS);
        assertEquals(
                bulkListAddCaptor.getValue(),
                mockRequestingKeys);
        // erroneous key should be stored with identification
        ArgumentCaptor<String> erroneousKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWaspHolder, times(1)).addErroneousOccasion(
                erroneousKeyCaptor.capture());
        assertEquals(
                mockRequestingKeys.get(2),
                erroneousKeyCaptor.getValue());
        // locationProvider
        verify(testLocationModule).getLocationProvider();
        // extranetAPI
        verify(mockExtranetAPI, never()).getOccasionsFromLocation(any(double.class), any(double.class));
        assertEquals(
                mockRequestingKeys.subList(2, 3),
                networkKeyCaptor.getAllValues()); // must handle list of erroneous keys as well
        assertEquals(
                TestingModel.centerOfTestingLatitude,
                networkLatitudeCaptor.getValue());
        assertEquals(
                TestingModel.centerOfTestingLongitude,
                networkLongitudeCaptor.getValue());
        // only two Occasions should be returned
        occasionTestSubscriber.assertNoErrors();
        occasionTestSubscriber.assertCompleted();
        occasionTestSubscriber.assertValues(
                mockOccasionsSubset.get(0),
                mockOccasionsSubset.get(1),
                mockOccasionsSubset.get(2));
    }

    @Test
    public void testGetGlobalOccasions() throws Exception {
        // mock getCachedOccasion
        final List<Occasion> mockGlobalOccasions = TestingModel.getMockGlobalOccasions();
        final List<String> mockGlobalKeys = TestingModel.getMockGlobalKeys();
        ArgumentCaptor<String> requestedKeyCaptor = ArgumentCaptor.forClass(String.class);
        when(mockWaspHolder.getOccasionKeys())
                .thenReturn(Observable.just(mockGlobalKeys));
        when(mockWaspHolder.getCachedOccasion(requestedKeyCaptor.capture()))
                .thenReturn(mockGlobalOccasions.get(0))
                .thenReturn(null)
                .thenReturn(mockGlobalOccasions.get(2))
                .thenReturn(mockGlobalOccasions.get(3));
        when(mockWaspHolder.getKeysForErroneousOccasions())
                .thenReturn((List<String>) new ArrayList<String>(){{
                    add(mockGlobalKeys.get(1));
                    add(mockGlobalKeys.get(2));
                }});
        // mock getOccasionsFromExtranet
        ArgumentCaptor<String> networkKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> networkLatitudeCaptor = ArgumentCaptor.forClass(double.class);
        ArgumentCaptor<Double> networkLongitudeCaptor = ArgumentCaptor.forClass(double.class);
        when(mockExtranetAPI
                .getOccasionsFromLocation(
                        networkLatitudeCaptor.capture(),
                        networkLongitudeCaptor.capture(),
                        networkKeyCaptor.capture()))
                .thenReturn(Observable.just(mockGlobalOccasions.get(1)));

        // test method
        Observable<Occasion> globalOccasions = subject.getContinuousGlobalOccasions();
        TestSubscriber<Occasion> occasionTestSubscriber = new TestSubscriber<>();
        globalOccasions.subscribe(occasionTestSubscriber);

        // should pass requested keys to DB holder
        assertEquals(
                mockGlobalKeys,
                requestedKeyCaptor.getAllValues());
        // "requested" keys should not be stored
        verify(mockWaspHolder, never()).setBulkStringList(any(WaspHolder.BulkStringList.class), anyListOf(String.class));
        // erroneous key should be stored with identification
        ArgumentCaptor<String> erroneousKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWaspHolder, times(1)).addErroneousOccasion(
                erroneousKeyCaptor.capture());
        assertEquals(
                mockGlobalKeys.get(1),
                erroneousKeyCaptor.getValue());
        // locationProvider
        verify(testLocationModule).getLocationProvider();
        // extranetAPI called with erroneous keys
        verify(mockExtranetAPI, never()).getOccasionsFromLocation(any(double.class), any(double.class));
//        verify(mockExtranetAPI, never()).getOccasionsFromLocation(any(double.class), any(double.class), any(String.class));
        assertEquals(
                mockGlobalKeys.subList(1, 3),
                networkKeyCaptor.getAllValues());
        assertEquals(
                TestingModel.centerOfTestingLatitude,
                networkLatitudeCaptor.getValue());
        assertEquals(
                TestingModel.centerOfTestingLongitude,
                networkLongitudeCaptor.getValue());
        // only three Occasions should be returned
        occasionTestSubscriber.assertNoErrors();
        occasionTestSubscriber.assertCompleted();
        occasionTestSubscriber.assertValues(
                mockGlobalOccasions.get(0),
                mockGlobalOccasions.get(2),
                mockGlobalOccasions.get(1));
    }
}