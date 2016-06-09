package com.anthropicandroid.extranetbrowser;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
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
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowNotificationManager;
import org.robolectric.util.ServiceController;

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
    public static final int MOCK_NOTIFICATION_ICON_RES_ID = 234;
    public static final int MOCK_DEFAULT_ICON_RES_ID = 654;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testRegisterAppForKeys() throws Exception {
        MapViewTestActivity testContext = Robolectric.setupActivity(MapViewTestActivity.class);
        String notificationText = "Notification Text";
        ExtranetRegistration.Builder builder = new ExtranetRegistration
                .Builder(testContext)
                .addNotificationText(notificationText)
                .addNotificationIcon(MOCK_NOTIFICATION_ICON_RES_ID)
                .addDefaultMapIcon(MOCK_DEFAULT_ICON_RES_ID);
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
        assertEquals(
                MOCK_NOTIFICATION_ICON_RES_ID,
                startedServiceIntent.getIntExtra(
                        ExtranetRegistrationService.NOTIFICATION_ICON_RES_ID, 0));
        assertEquals(MOCK_DEFAULT_ICON_RES_ID,
                startedServiceIntent.getIntExtra(
                        ExtranetRegistrationService.DEFAULT_MAP_ICON_RES_ID, 0));
    }

    @Test
    public void testNoBundleOrBundleWithoutPackageNameDoesNothing(){
        ServiceController<ExtranetRegistrationService> serviceController = Robolectric.buildService(ExtranetRegistrationService.class);
        serviceController.attach();
        serviceController.create();
        ExtranetRegistrationService extranetRegistrationService = serviceController.get();
        NotificationManager notificationManager = (NotificationManager) RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent blankIntent = new Intent(RuntimeEnvironment.application, ExtranetRegistrationService.class);
        extranetRegistrationService.onHandleIntent(blankIntent);

        blankIntent.putExtra(ExtranetRegistrationService.NOTIFICATION_TEXT_EXTRA, "packageName");
        extranetRegistrationService.onHandleIntent(blankIntent);

        assertEquals(0, ((ShadowNotificationManager) ShadowExtractor.extract(notificationManager)).size());
    }

    @Test
    public void testOnHandleIntentDeterminesKeysToDownloadAndRegistersTheirGeofences() throws Exception {
        ServiceController<ExtranetRegistrationService> serviceController = Robolectric.buildService(ExtranetRegistrationService.class);
        serviceController.attach();
        serviceController.create();
        ExtranetRegistrationService extranetRegistrationService = serviceController.get();
        NotificationManager notificationManager = (NotificationManager) RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent blankIntent = new Intent(RuntimeEnvironment.application, ExtranetRegistrationService.class);
        extranetRegistrationService.onHandleIntent(blankIntent);


//        ExtranetRegistration.Builder builder = new ExtranetRegistration.Builder();

        // service test
        // should determine which keys need occasions
        // should ask network for needed occasions
        // should register geofences for first 80 occasions with valid lat/long
        // should note occasions with invalid lat/long
        // could limit return to occasions nearest to square location


    }

    private class SolutionParameters {
        private final int[] ints;
        private final int intValue;

        public SolutionParameters(int[] ints, Integer intValue) {

            this.ints = ints;
            this.intValue = intValue;
        }
    }
}