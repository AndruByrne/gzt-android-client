package com.anthropicandroid.extranetbrowser.view;

import android.content.Context;

/*
 * Created by Andrew Brin on 5/30/2016.
 */
public class ExtranetRegistration {

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

        public ExtranetRegistration build() {
            return new ExtranetRegistration(this);
        }
    }

    private ExtranetRegistration(Builder builder) {
        packageName = builder.packageName;
        context = builder.context;
        resIdDefaultMapIcon = builder.resIdDefaultMapIcon;
        resIdNotificationIcon = builder.resIdNotificationIcon;
        notificationText = builder.notificationText;
    }
}
