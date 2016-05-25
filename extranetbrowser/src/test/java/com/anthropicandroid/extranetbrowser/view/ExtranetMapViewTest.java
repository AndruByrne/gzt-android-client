package com.anthropicandroid.extranetbrowser.view;

import com.anthropicandroid.extranetbrowser.MapViewTestActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;

/*
 * Created by Andrew Brin on 5/20/2016.
 */

public class ExtranetMapViewTest extends TestCase {

    private MapViewTestActivity testContext;

    @Before
    public void setUp() throws Exception {
        testContext = Robolectric.setupActivity(MapViewTestActivity.class);
    }

    @Test
    public void testGetMapAsync() throws Exception {

        ExtranetMapView extranetMapView = new ExtranetMapView(testContext);
        extranetMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                System.out.println("test get a googleMap");
            }
        });
    }

    @Test
    public void testGetMapAsync1() throws Exception {

    }

    @Test
    public void testGetMapAsync2() throws Exception {

    }
}