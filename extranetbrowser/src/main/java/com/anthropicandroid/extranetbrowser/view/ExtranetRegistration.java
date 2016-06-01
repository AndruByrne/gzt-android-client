package com.anthropicandroid.extranetbrowser.view;

import android.content.Context;
import android.graphics.Bitmap;

/*
 * Created by Andrew Brin on 5/30/2016.
 */
public class ExtranetRegistration {

    public final String packageName;
    public final Bitmap titleIconBitmap;
    public final String notificationText;
    public final Bitmap defaultMapIcon;
    public final Context context;

    public static class Builder {
        private final Context context;
        private final String packageName;
        private Bitmap titleIconBitmap = null;
        private String notificationText = "";
        private Bitmap defaultMapIcon = null;

        public Builder(Context context) {
            this.context = context;
            this.packageName = context.getPackageName();
        }

        public Builder addTitleIcon(Bitmap bitmap){ // TODO: should just be reference to R. in client project
            titleIconBitmap = bitmap;
            return this;
        }

        public Builder addNotificationText(String notification){
            notificationText = notification;
            return this;
        }

        public Builder addDefaultMapIcon(Bitmap bitmap){
            defaultMapIcon = bitmap;
            return this;
        }

        public ExtranetRegistration build() {
            return new ExtranetRegistration(this);
        }
    }

    private ExtranetRegistration(Builder builder) {
        packageName = builder.packageName;
        context = builder.context;
        defaultMapIcon = builder.defaultMapIcon;
        titleIconBitmap = builder.titleIconBitmap;
        notificationText = builder.notificationText;
    }
}
