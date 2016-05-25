package com.anthropicandroid.extranetbrowser.model;

import com.anthropicandroid.extranetbrowser.BuildConfig;
import com.anthropicandroid.extranetbrowser.MapViewTestActivity;
import com.anthropicandroid.extranetbrowser.testUtils.RoboTestRunner;
import com.anthropicandroid.extranetbrowser.view.ExtranetMapView;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
        extranetMapView = new ExtranetMapView(testContext);
    }

    @Test
    public void testGetOccasionsSubsetObservable() throws Exception {

    }

    public void testGetGlobalOccasions() throws Exception {

    }
}