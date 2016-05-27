package com.anthropicandroid.extranetbrowser.model;

import android.support.annotation.NonNull;

import com.anthropicandroid.extranetbrowser.BuildConfig;
import com.anthropicandroid.extranetbrowser.modules.ContextModule;
import com.anthropicandroid.extranetbrowser.modules.DaggerExtranetMapViewTestComponent;
import com.anthropicandroid.extranetbrowser.modules.ExtranetMapViewTestComponent;
import com.anthropicandroid.extranetbrowser.modules.MapModule;
import com.anthropicandroid.extranetbrowser.modules.OccasionProviderModule;
import com.anthropicandroid.extranetbrowser.modules.TestMapModule;
import com.anthropicandroid.extranetbrowser.modules.TestWaspModule;
import com.anthropicandroid.extranetbrowser.testUtils.MapViewTestActivity;
import com.anthropicandroid.extranetbrowser.testUtils.RoboTestRunner;
import com.anthropicandroid.extranetbrowser.testUtils.TestingModel;
import com.anthropicandroid.extranetbrowser.view.ExtranetMapWrapper;
import com.google.android.gms.maps.OnMapReadyCallback;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Mockito.mock;
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
    @Inject ExtranetOccasionProvider subject;

    @Inject WaspHolder mockWaspHolder;

    @Before
    public void setUp() throws Exception {
        MapViewTestActivity testContext = Robolectric.setupActivity(MapViewTestActivity.class);
        ExtranetMapWrapper mockWrapper = mock(ExtranetMapWrapper.class);
        ExtranetMapViewTestComponent testComponent = DaggerExtranetMapViewTestComponent
                .builder()
                .contextModule(new ContextModule(testContext))
                .mapModule(new TestMapModule(getGoogleMapAsyncGetter(), mockWrapper))
                .occasionProviderModule(new OccasionProviderModule()) //  provides class under test
                .waspModule(new TestWaspModule())
                .build();
        testComponent.inject(this);
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
    public void testGetOccasionsSubsetObservable() throws Exception {
        // mock calling parameters
        List<String> mockRequestingKeys = TestingModel.getMockRequestingKeys();
        // mock getCachedOccasion
        final List<Occasion> mockOccasionsSubset = TestingModel.getMockOccasionsSubset();
        ArgumentCaptor<String> requestedKeyCaptor = ArgumentCaptor.forClass(String.class);
        when(mockWaspHolder.getCachedOccasion(requestedKeyCaptor.capture()))
                .thenReturn(mockOccasionsSubset.get(0))
                .thenReturn(mockOccasionsSubset.get(1))
                .thenReturn(null)
                .thenReturn(mockOccasionsSubset.get(2));

        // test method
        Observable<Occasion> occasionsSubset = subject.getOccasionsSubset(mockRequestingKeys);
        TestSubscriber<Occasion> occasionTestSubscriber = new TestSubscriber<>();
        occasionsSubset.subscribe(occasionTestSubscriber);

        // should pass requested keys to DB holder
        assertEquals(
                requestedKeyCaptor.getAllValues(),
                mockRequestingKeys);
        // requested keys should be stored exactly as is
        ArgumentCaptor<List> bulkListAddCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<ExtranetOccasionProvider.BulkStringList> bulkListTypeCaptor = ArgumentCaptor.forClass(ExtranetOccasionProvider.BulkStringList.class);
        verify(mockWaspHolder).setBulkStringList(
                bulkListTypeCaptor.capture(),
                bulkListAddCaptor.capture());
        assertEquals(
                bulkListTypeCaptor.getValue(),
                ExtranetOccasionProvider.BulkStringList.REQUESTED_KEYS);
        assertEquals(
                bulkListAddCaptor.getValue(),
                mockRequestingKeys);
        // erroneous key should be stored with identification
        ArgumentCaptor<String> erroneousKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ExtranetOccasionProvider.OccasionDeficit> deficitCaptor = ArgumentCaptor.forClass(ExtranetOccasionProvider.OccasionDeficit.class);
        verify(mockWaspHolder, times(1)).addErroneousOccasion(
                erroneousKeyCaptor.capture(),
                deficitCaptor.capture());
        assertEquals(
                mockRequestingKeys.get(2),
                erroneousKeyCaptor.getValue());
        // only two Occasions should be returned
        occasionTestSubscriber.assertNoErrors();
        occasionTestSubscriber.assertCompleted();
        occasionTestSubscriber.assertValues(
                mockOccasionsSubset.get(0),
                mockOccasionsSubset.get(1));
    }

    @Test
    public void testGetGlobalOccasions() throws Exception {

    }
}