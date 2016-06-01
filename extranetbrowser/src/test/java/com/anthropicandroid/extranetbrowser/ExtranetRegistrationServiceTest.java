package com.anthropicandroid.extranetbrowser;

import android.content.ComponentName;
import android.content.Intent;

import com.anthropicandroid.extranetbrowser.model.WaspHolder;
import com.anthropicandroid.extranetbrowser.testUtils.MapViewTestActivity;
import com.anthropicandroid.extranetbrowser.testUtils.RoboTestRunner;
import com.anthropicandroid.extranetbrowser.testUtils.TestingModel;
import com.anthropicandroid.extranetbrowser.view.ExtranetRegistration;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.ShadowActivity;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/*
 * Created by Andrew Brin on 5/31/2016.
 */

@RunWith(RoboTestRunner.class)
@Config(constants = BuildConfig.class)
public class ExtranetRegistrationServiceTest extends TestCase {

    public static final String TAG = ExtranetRegistrationServiceTest.class.getSimpleName();
    private MapViewTestActivity testContext;

    @Before
    public void setUp() throws Exception{
        testContext = Robolectric.setupActivity(MapViewTestActivity.class);
//        ExtranetRegistrationService registrationService = Robolectric.setupService(ExtranetRegistrationService.class);
    }

    @Test
    public void testRegisterAppForKeys() throws Exception {
        String notificationText = "Notification Text";
        ExtranetRegistration.Builder builder = new ExtranetRegistration
                .Builder(testContext)
                .addNotificationText(notificationText);
        ExtranetRegistration registration = builder.build();
        List<String> mockGlobalKeys = TestingModel.getMockGlobalKeys();

        // test function
        WaspHolder mockWaspHolder = mock(WaspHolder.class);
        ExtranetRegistrationService.registerAppForKeys(registration, mockGlobalKeys, mockWaspHolder);

        // Should write to DB
        ArgumentCaptor<WaspHolder.BulkStringList> bulkStringListTypeCaptor = ArgumentCaptor.forClass(WaspHolder.BulkStringList.class);
        ArgumentCaptor<List> requestedKeysCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockWaspHolder).setBulkStringList(bulkStringListTypeCaptor.capture(), requestedKeysCaptor.capture());
        assertEquals(
                WaspHolder.BulkStringList.REQUESTED_KEYS,
                bulkStringListTypeCaptor.getValue());
        assertEquals(
                mockGlobalKeys,
                requestedKeysCaptor.getValue());
        // should start registration service
        Intent startedServiceIntent = ((ShadowActivity) ShadowExtractor.extract(testContext))
                .peekNextStartedService();
        assertNotNull(startedServiceIntent);
        assertEquals(
                new ComponentName(testContext, ExtranetRegistrationService.class),
                startedServiceIntent.getComponent());
        // check extras for desired data
        assertEquals(testContext.getPackageName(),
                startedServiceIntent.getStringExtra(
                        ExtranetRegistrationService.PACKAGE_NAME_EXTRA));
        assertEquals(
                notificationText,
                startedServiceIntent.getStringExtra(
                        ExtranetRegistrationService.NOTIFICATION_TEXT_EXTRA));
    }


    @Test
    public void testOnHandleIntent() throws Exception {

    }
}