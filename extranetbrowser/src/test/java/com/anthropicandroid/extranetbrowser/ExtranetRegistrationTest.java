package com.anthropicandroid.extranetbrowser;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.anthropicandroid.extranetbrowser.model.WaspHolder;
import com.anthropicandroid.extranetbrowser.testUtils.MapViewTestActivity;
import com.anthropicandroid.extranetbrowser.testUtils.RoboTestRunner;
import com.anthropicandroid.extranetbrowser.testUtils.TestingModel;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowNotificationManager;

import java.util.List;

import static org.mockito.Mockito.verify;

/*
 * Created by Andrew Brin on 5/31/2016.
 */

@RunWith(RoboTestRunner.class)
@Config(constants = BuildConfig.class)
public class ExtranetRegistrationTest extends TestCase {

    public static final String TAG = ExtranetRegistrationTest.class.getSimpleName();
    public static final int MOCK_NOTIFICATION_ICON_RES_ID = 234;
    public static final int MOCK_DEFAULT_ICON_RES_ID = 654;
    public static final String MOCK_NOTIFICATION_TEXT = "Notification Text";
    private ExtranetRegistration subject;
    private WaspHolder mockWaspHolder;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        mockWaspHolder = Mockito.mock(WaspHolder.class);
        subject = new ExtranetRegistration(mockWaspHolder);
    }

    @Test
    public void testRegisterAppForKeys() throws Exception {
        MapViewTestActivity testContext = Robolectric.setupActivity(MapViewTestActivity.class);
        ExtranetRegistration.Registration.Builder builder = new ExtranetRegistration.Registration
                .Builder(testContext)
                .addNotificationText(MOCK_NOTIFICATION_TEXT)
                .addNotificationIcon(MOCK_NOTIFICATION_ICON_RES_ID)
                .addDefaultMapIcon(MOCK_DEFAULT_ICON_RES_ID);
        ExtranetRegistration.Registration registration = builder.build();
        List<String> mockGlobalKeys = TestingModel.getMockGlobalKeys();

        // test function
        subject.registerAppForKeys(registration, mockGlobalKeys);

        // Should write to DB
        ArgumentCaptor<WaspHolder.BulkStringList> bulkStringListTypeCaptor = ArgumentCaptor.forClass(WaspHolder.BulkStringList.class);
        ArgumentCaptor<List> requestedKeysCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockWaspHolder).setBulkStringList(bulkStringListTypeCaptor.capture(), requestedKeysCaptor.capture());
        assertEquals(
                WaspHolder.BulkStringList.REQUESTED_BROADCAST_KEYS,
                bulkStringListTypeCaptor.getValue());
        assertEquals(
                mockGlobalKeys,
                requestedKeysCaptor.getValue());
        // should register geofences for first 80 occasions with valid lat/long
        // should note occasions with invalid lat/long
        // THEN should call network for missing/invalid keys
        // THEN add missing (not invalid) keys to geofence registration, if needed
        // THEN should start async service to ask for missing keys again
        // AND should start async service to download data for valid registered keys
        // could limit return to occasions nearest to square location
//        Intent startedServiceIntent = ((ShadowActivity) ShadowExtractor.extract(testContext))
//                .peekNextStartedService();
//        assertNotNull(startedServiceIntent);
//        assertEquals(
//                new ComponentName(testContext, ExtranetRegistration.class),
//                startedServiceIntent.getComponent());
//        // check extras for desired data
//        assertEquals(testContext.getPackageName(),
//                startedServiceIntent.getStringExtra(
//                        ExtranetRegistration.PACKAGE_NAME_EXTRA));
//        assertEquals(
//                MOCK_NOTIFICATION_TEXT,
//                startedServiceIntent.getStringExtra(
//                        ExtranetRegistration.NOTIFICATION_TEXT_EXTRA));
//        assertEquals(
//                MOCK_NOTIFICATION_ICON_RES_ID,
//                startedServiceIntent.getIntExtra(
//                        ExtranetRegistration.NOTIFICATION_ICON_RES_ID, 0));
//        assertEquals(MOCK_DEFAULT_ICON_RES_ID,
//                startedServiceIntent.getIntExtra(
//                        ExtranetRegistration.DEFAULT_MAP_ICON_RES_ID, 0));
    }

    @Test
    public void testNoBundleOrBundleWithoutPackageNameDoesNothing(){
//        ServiceController<ExtranetRegistration> serviceController = Robolectric.buildService(ExtranetRegistration.class);
//        serviceController.attach();
//        serviceController.create();
//        ExtranetRegistration subject = serviceController.get();
        NotificationManager notificationManager = (NotificationManager) RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent blankIntent = new Intent(RuntimeEnvironment.application, ExtranetRegistration.class);
//        subject.onHandleIntent(blankIntent);

        blankIntent.putExtra(ExtranetRegistration.NOTIFICATION_TEXT_EXTRA, "packageName");
//        subject.onHandleIntent(blankIntent);

        assertEquals(0, ((ShadowNotificationManager) ShadowExtractor.extract(notificationManager)).size());
    }

}