package com.anthropicandroid.extranetbrowser;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.anthropicandroid.extranetbrowser.model.ExtranetOccasionProvider;
import com.anthropicandroid.extranetbrowser.model.Occasion;
import com.anthropicandroid.extranetbrowser.model.PylonDAO;
import com.anthropicandroid.extranetbrowser.testUtils.MapViewTestActivity;
import com.anthropicandroid.extranetbrowser.testUtils.RoboTestRunner;
import com.anthropicandroid.extranetbrowser.testUtils.TestingModel;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.common.collect.Lists;

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

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private ExtranetRegistration     subject;
    private PylonDAO                 mockPylonDAO;
    private GeofencingApi            mockGeofencingApi;
    private ExtranetOccasionProvider mockOccasionProvider;
    private PendingIntent            mockPendingIntent;
    private GoogleApiClient          mockApiClient;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        mockPylonDAO = Mockito.mock(PylonDAO.class);
        mockGeofencingApi = Mockito.mock(LocationServices.GeofencingApi.getClass());
        mockApiClient = Mockito.mock(GoogleApiClient.class);
        mockOccasionProvider = Mockito.mock(ExtranetOccasionProvider.class);
        mockPendingIntent = Mockito.mock(PendingIntent.class);
        subject = new ExtranetRegistration(
                Observable.just(mockApiClient),
                mockOccasionProvider,
                mockGeofencingApi,
                mockPendingIntent,
                mockPylonDAO);
    }

    @Test
    public void testRegisterAppForKeys() throws Exception {
        // mock
        MapViewTestActivity testContext = Robolectric.setupActivity(MapViewTestActivity.class);
        ExtranetRegistration.Registration.Builder builder = new ExtranetRegistration.Registration
                .Builder(testContext)
                .addNotificationText(MOCK_NOTIFICATION_TEXT)
                .addNotificationIcon(MOCK_NOTIFICATION_ICON_RES_ID)
                .addDefaultMapIcon(MOCK_DEFAULT_ICON_RES_ID);
        ExtranetRegistration.Registration registration = builder.build();
        final List<Occasion> occasionsSubset = TestingModel.getMockOccasionsSubset();
        List<String> mockGlobalKeys = TestingModel.getMockGlobalKeys();
        ArgumentCaptor<GeofencingRequest> geofencingAddRequestCaptor = ArgumentCaptor.forClass(GeofencingRequest.class);
        ArgumentCaptor<PendingIntent>           pendingAddIntentCaptor      = ArgumentCaptor.forClass(PendingIntent.class);
        ArgumentCaptor<GoogleApiClient>         apiClientCaptor             = ArgumentCaptor.forClass(GoogleApiClient.class);
        ArgumentCaptor<PendingIntent>           pendingRemoveIntentCaptor   = ArgumentCaptor.forClass(PendingIntent.class);
        ArgumentCaptor<List>                    requestedWaspKeysCaptor     = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List>                    requestedEOPKeysCaptor      = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<PylonDAO.BulkStringList> bulkStringListTypeCaptor    = ArgumentCaptor.forClass(PylonDAO.BulkStringList.class);
        ArgumentCaptor<PylonDAO.BulkStringList> addBulkStringListTypeCaptor = ArgumentCaptor.forClass(PylonDAO.BulkStringList.class);

        ArgumentCaptor<Integer> maxReturnCaptor = ArgumentCaptor.forClass(int.class);
        when(mockOccasionProvider.getSegmentedOccasionsSubsetNoMoreThan(
                requestedEOPKeysCaptor.capture(),
                maxReturnCaptor.capture()))
                .thenReturn(Observable.just(
                        (List<Occasion>)new ArrayList<Occasion>() {{
                            add(occasionsSubset.get(0));
                            add(occasionsSubset.get(1));
                            add(occasionsSubset.get(2));
                        }},
                        Lists.<Occasion>newArrayList())); //  the network returns second

        // test function
        Log.d(TAG, "getting globalkeys: " + mockGlobalKeys.toString());
        subject.registerAppForKeys(registration, mockGlobalKeys);

        // Should remove old keys from geofencing registry once (empty network return)
        verify(mockGeofencingApi, times(1)).removeGeofences(
                any(GoogleApiClient.class),
                pendingRemoveIntentCaptor.capture());
        assertTrue(
                pendingRemoveIntentCaptor.getValue().equals(mockPendingIntent));

        // Should write request keys to DB
        verify(mockPylonDAO).clearBulkStringList(addBulkStringListTypeCaptor.capture());
        verify(mockPylonDAO).addToBulkStringList(
                bulkStringListTypeCaptor.capture(),
                requestedWaspKeysCaptor.capture());
        assertEquals(
                PylonDAO.BulkStringList.REQUESTED_BROADCAST_KEYS,
                bulkStringListTypeCaptor.getValue());
        List<String> requestedWaspKeys = requestedWaspKeysCaptor.getAllValues().get(0);
        Log.d(TAG, "mockReqKeys: " + mockGlobalKeys.toString());
        Log.d(TAG, "capturedReqKEys: " + requestedWaspKeys.toString());
        assertEquals(
                mockGlobalKeys,
                requestedWaspKeys);


        // Should write request App to DB

        // should ask occasion provider for occasions
        assertEquals(
                mockGlobalKeys,
                requestedEOPKeysCaptor.getValue());
        assertEquals(
                80,
                (int) maxReturnCaptor.getValue());

        // should register geofences for first 80 occasions with valid lat/long
        verify(mockGeofencingApi, times(1)).addGeofences(
                apiClientCaptor.capture(),
                geofencingAddRequestCaptor.capture(),
                pendingAddIntentCaptor.capture());

        assertEquals(
                mockApiClient,
                apiClientCaptor.getValue());

        assertEquals(
                mockGlobalKeys.get(0),
                geofencingAddRequestCaptor.getValue().getGeofences().get(0).getRequestId());

        assertTrue(pendingAddIntentCaptor.getValue().equals(mockPendingIntent));


        // THEN add missing (not invalid) keys to geofence registration, if needed
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
    public void testNoBundleOrBundleWithoutPackageNameDoesNothing() {
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