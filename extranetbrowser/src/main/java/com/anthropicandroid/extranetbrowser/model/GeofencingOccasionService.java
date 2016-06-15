package com.anthropicandroid.extranetbrowser.model;

import android.app.IntentService;
import android.content.Intent;

/*
 * Created by AndrewBrin on 6/14/2016.
 */
public class GeofencingOccasionService extends IntentService{

    public GeofencingOccasionService() {
        super(GeofencingOccasionService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
