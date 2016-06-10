package com.anthropicandroid.extranetbrowser;

import android.content.Context;
import android.content.Intent;

import com.anthropicandroid.extranetbrowser.model.WaspHolder;

import java.util.List;


/*
 * Created by Andrew Brin on 5/31/2016.
 */
public class ExtranetRegistration {
    public static final String PACKAGE_NAME_EXTRA = "package_name_extra";
    public static final String TAG = ExtranetRegistration.class.getSimpleName();
    public static final String NOTIFICATION_TEXT_EXTRA = "notification_text_extra";
    public static final String NOTIFICATION_ICON_RES_ID = "notification_icon_res_id";
    public static final String DEFAULT_MAP_ICON_RES_ID = "default_map_icon_res_id";
    private WaspHolder waspHolder;

    public ExtranetRegistration(WaspHolder waspHolder) {
        this.waspHolder = waspHolder;
    }

    public void registerAppForKeys(Registration registration, List<String> requestedKeys) {
        Context context = registration.context;
        waspHolder.setBulkStringList(
                WaspHolder.BulkStringList.REQUESTED_BROADCAST_KEYS,
                requestedKeys);
        Intent registrationService = new Intent(context, ExtranetRegistration.class);
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

    /*
         * Created by Andrew Brin on 5/30/2016.
         */
    public static class Registration {

        public final String packageName;
        public final String notificationText;
        public final Context context;
        public final int resIdNotificationIcon;
        public final int resIdDefaultMapIcon;

        public static class Builder {
            // required
            private final Context context;
            private final String packageName;
            // optional fields
            private String notificationText = "";
            private int resIdNotificationIcon = 0;
            private int resIdDefaultMapIcon = 0;

            public Builder(Context context) {
                this.context = context;
                this.packageName = context.getPackageName();
            }

            public Builder addNotificationIcon(int resId){
                this.resIdNotificationIcon = resId;
                return this;
            }

            public Builder addNotificationText(String notification){
                notificationText = notification;
                return this;
            }

            public Builder addDefaultMapIcon(int resId){
                this.resIdDefaultMapIcon = resId;
                return this;
            }

            public Registration build() {
                return new Registration(this);
            }
        }

        private Registration(Builder builder) {
            packageName = builder.packageName;
            context = builder.context;
            resIdDefaultMapIcon = builder.resIdDefaultMapIcon;
            resIdNotificationIcon = builder.resIdNotificationIcon;
            notificationText = builder.notificationText;
        }
    }
}
