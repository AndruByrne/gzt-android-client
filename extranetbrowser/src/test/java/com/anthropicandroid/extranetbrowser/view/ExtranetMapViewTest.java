package com.anthropicandroid.extranetbrowser.view;

import android.support.annotation.NonNull;

import com.anthropicandroid.extranetbrowser.MapViewTestActivity;
import com.anthropicandroid.extranetbrowser.modules.ContextModule;
import com.anthropicandroid.extranetbrowser.modules.MapModule;
import com.anthropicandroid.extranetbrowser.testUtils.DaggerExtranetMapViewTestComponent;
import com.anthropicandroid.extranetbrowser.testUtils.ExtranetMapViewTestComponent;
import com.anthropicandroid.extranetbrowser.testUtils.TestMapModule;
import com.anthropicandroid.extranetbrowser.testUtils.TestWaspModule;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

/*
 * Created by Andrew Brin on 5/20/2016.
 */

public class ExtranetMapViewTest extends TestCase {

    private MapViewTestActivity testContext;
    private GoogleMap mockMap;
    private ExtranetMapView extranetMapView;

    @Before
    public void setUp() throws Exception {
        testContext = Robolectric.setupActivity(MapViewTestActivity.class);
        mockMap = Mockito.mock(GoogleMap.class);
        ExtranetMapViewTestComponent testComponent = DaggerExtranetMapViewTestComponent
                .builder()
                .contextModule(new ContextModule(testContext))
                .waspModule(new TestWaspModule())
                .mapModule(new TestMapModule(getSuperMapViewAsyncMock()))
                .build();
        testComponent.inject(this);
        extranetMapView = new ExtranetMapView(testContext);
        extranetMapView.setExtranetMapViewComponent(testComponent);
        // a difficult class to mock; will have to expose a component setter method
    }

    @NonNull
    private MapModule.GoogleMapAsyncGetter getSuperMapViewAsyncMock() {
        return new MapModule.GoogleMapAsyncGetter() {
            @Override
            public void getSuperMapViewAsync(OnMapReadyCallback callback) {
                callback.onMapReady(mockMap);
            }
        };
    }

    @Test
    public void testGetMapAsyncOneParamPopulatesAGoogleMapAndReturnsCallback() throws Exception {
//        extranetMapView.getMapAsync(callback) should call the mock global occasions function
        //add the occasions to the googleMap
        //and return the googleMap to callback
    }

    @Test
    public void testGetMapAsync1() throws Exception {

    }

    @Test
    public void testGetMapAsync2() throws Exception {

    }
}