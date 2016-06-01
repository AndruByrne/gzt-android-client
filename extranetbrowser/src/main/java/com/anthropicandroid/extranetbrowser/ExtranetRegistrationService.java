package com.anthropicandroid.extranetbrowser;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.anthropicandroid.extranetbrowser.model.WaspHolder;
import com.anthropicandroid.extranetbrowser.view.ExtranetRegistration;

import java.util.List;

/*
 * Created by Andrew Brin on 5/31/2016.
 */
public class ExtranetRegistrationService extends IntentService{
    public static final String PACKAGE_NAME_EXTRA = "package_name_extra";
    public static final String TAG = ExtranetRegistrationService.class.getSimpleName();
    public static final String NOTIFICATION_TEXT_EXTRA = "notification_text_extra";

    static void registerAppForKeys(ExtranetRegistration registration, List<String> requestedKeys, WaspHolder waspHolder){
        Context context = registration.context;
        waspHolder.setBulkStringList(
                WaspHolder.BulkStringList.REQUESTED_KEYS,
                requestedKeys);
        Intent registrationService = new Intent(context, ExtranetRegistrationService.class);
        registrationService.putExtra(
                PACKAGE_NAME_EXTRA,
                registration.packageName);
        registrationService.putExtra(
                NOTIFICATION_TEXT_EXTRA,
                registration.notificationText);
        context.startService(registrationService);
    }

    public ExtranetRegistrationService(String nameOfWorkerThread) { super(nameOfWorkerThread); }

    @Override
    public void onHandleIntent(Intent intent) {
//        intent.getStringExtra(PACKAGE_NAME_EXTRA);
    }
}
