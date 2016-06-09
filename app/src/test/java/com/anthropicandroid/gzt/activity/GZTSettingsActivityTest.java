package com.anthropicandroid.gzt.activity;

import com.anthropicandroid.gzt.BuildConfig;
import com.anthropicandroid.gzt.TestZombieTrackerApplication;
import com.anthropicandroid.gzt.test_utils.RoboTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;


@RunWith(RoboTestRunner.class)
@Config(constants = BuildConfig.class,
        application = TestZombieTrackerApplication.class,
        sdk = 21)
public class GZTSettingsActivityTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testOnResume() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }
}
