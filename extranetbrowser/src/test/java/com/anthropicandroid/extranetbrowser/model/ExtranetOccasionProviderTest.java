package com.anthropicandroid.extranetbrowser.model;

import android.support.annotation.NonNull;

import com.anthropicandroid.extranetbrowser.BuildConfig;
import com.anthropicandroid.extranetbrowser.MapViewTestActivity;
import com.anthropicandroid.extranetbrowser.modules.ContextModule;
import com.anthropicandroid.extranetbrowser.modules.MapModule;
import com.anthropicandroid.extranetbrowser.testUtils.DaggerExtranetMapViewTestComponent;
import com.anthropicandroid.extranetbrowser.testUtils.ExtranetMapViewTestComponent;
import com.anthropicandroid.extranetbrowser.testUtils.RoboTestRunner;
import com.anthropicandroid.extranetbrowser.testUtils.TestMapModule;
import com.anthropicandroid.extranetbrowser.testUtils.TestWaspModule;
import com.anthropicandroid.extranetbrowser.view.ExtranetMapView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

/*
 * Created by Andrew Brin on 5/20/2016.
 */

@RunWith(RoboTestRunner.class)
@Config(constants = BuildConfig.class)
public class ExtranetOccasionProviderTest extends TestCase {

    private ExtranetMapView extranetMapView;

    @Before
    public void setUp() throws Exception {
        MapViewTestActivity testContext = Robolectric.setupActivity(MapViewTestActivity.class);
        ExtranetMapViewTestComponent testComponent = DaggerExtranetMapViewTestComponent
                .builder()
                .contextModule(new ContextModule(testContext))
                .waspModule(new TestWaspModule())
                .mapModule(new TestMapModule(getSuperMapViewAsyncMock()))
                .build();
        testComponent.inject(this);
    }

    @NonNull
    private MapModule.GoogleMapAsyncGetter getSuperMapViewAsyncMock() {
        return new MapModule.GoogleMapAsyncGetter() {
            @Override
            public void getSuperMapViewAsync(OnMapReadyCallback callback) {
                GoogleMap mockMap = Mockito.mock(GoogleMap.class);
                callback.onMapReady(mockMap);
            }
        };
    }

    @Test
    public void testGetOccasionsSubsetObservable() throws Exception {

    }

    public void testGetGlobalOccasions() throws Exception {

    }
}