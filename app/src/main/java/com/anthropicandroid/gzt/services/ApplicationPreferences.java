package com.anthropicandroid.gzt.services;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.anthropicandroid.gzt.ZombieTrackerApplication;

/*
 * Created by Andrew Brin on 5/6/2016.
 */
public class ApplicationPreferences {

    private static String SANS_USER_INTS = "sans_user_ints";
    private final SharedPreferences preferences;

    public enum Preferences {
        MUTE_NOTIFICATIONS(1),
        NUMBER_OF_BUILT_MOLOTOVS(0),
        NUMBER_OF_PURCHASED_MOLOTOVS(0),
        NUMBER_OF_GARLIC_CLOVES(0),
        PLAYER_HEALTH(0),
        ZOMBIES_KILLED(0),
        DAYS_ALIVE(0),
        SLAIN_ZOMBIES(0);

        public Integer defaultInt;

        Preferences(Integer defaultInt) {
            this.defaultInt = defaultInt;
        }
    }

    public ApplicationPreferences(Application application) {
        preferences = application.getSharedPreferences(SANS_USER_INTS, Context.MODE_PRIVATE);
    }

    public Integer getPreference(Preferences preference) {
        return preferences.getInt(preference.name(), preference.defaultInt);
    }

    public boolean setPreference(Preferences preference, Integer integer) {
        boolean result;
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(preference.name(), integer);
            editor.apply();
            result = true;
        } catch (Throwable t) {
            Log.e(
                    ZombieTrackerApplication.TAG,
                    "unable to write to preferences: " + t.getMessage());
            result = false;
        }
        return result;
    }
}
