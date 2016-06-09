package com.anthropicandroid.extranetbrowser;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.anthropicandroid.extranetbrowser.model.WaspHolder;
import com.anthropicandroid.extranetbrowser.view.ExtranetRegistration;

import java.util.List;


/*
 * Created by Andrew Brin on 5/31/2016.
 */
public class ExtranetRegistrationService extends IntentService {
    public static final String PACKAGE_NAME_EXTRA = "package_name_extra";
    public static final String TAG = ExtranetRegistrationService.class.getSimpleName();
    public static final String NOTIFICATION_TEXT_EXTRA = "notification_text_extra";
    public static final String NOTIFICATION_ICON_RES_ID = "notification_icon_res_id";
    public static final String DEFAULT_MAP_ICON_RES_ID = "default_map_icon_res_id";

    static void registerAppForKeys(ExtranetRegistration registration, List<String> requestedKeys, WaspHolder waspHolder) {
        Context context = registration.context;
        waspHolder.setBulkStringList(
                WaspHolder.BulkStringList.REQUESTED_KEYS,
                requestedKeys);
        Intent registrationService = new Intent(context, ExtranetRegistrationService.class);
        registrationService.putExtra(
                PACKAGE_NAME_EXTRA,
                registration.packageName);
        registrationService.putExtra(
                NOTIFICATION_ICON_RES_ID,
                registration.resIdNotificationIcon);
        registrationService.putExtra(
                NOTIFICATION_TEXT_EXTRA,
                registration.notificationText);
        registrationService.putExtra(
                DEFAULT_MAP_ICON_RES_ID,
                registration.resIdDefaultMapIcon);
        context.startService(registrationService);
    }

    public ExtranetRegistrationService() {
        super(ExtranetRegistrationService.class.getSimpleName());
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null || bundle.getString(
                PACKAGE_NAME_EXTRA) == null) return;
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this).setSmallIcon(R.drawable.common_google_signin_btn_icon_dark).build();
        notificationManager.notify(0, notification);
    }
}
