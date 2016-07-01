package com.anthropicandroid.gzt.activity;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.services.ApplicationPreferences;

/*
 * Created by Andrew Brin on 4/6/2016.
 */
final public class UpperActionHandlers {

    public static final String TAG = UpperActionHandlers.class.getSimpleName();

    private GZTZoomAnimator zoomAnimator;

    public UpperActionHandlers(GZTZoomAnimator zoomAnimator) {
        this.zoomAnimator = zoomAnimator;
    }


    public void muteNotifications(View view) {
        CheckBox checkBox = (CheckBox) view;
        ApplicationPreferences preferenceStorage = ((ZombieTrackerApplication) ((Activity)
                checkBox.getContext()).getApplication())
                .getApplicationComponent().getPreferenceStorage();
        checkBox.setChecked(
                checkBox.isChecked() ?
                        successfulMute(preferenceStorage) :
                        successfulUnMute(preferenceStorage));
    }

    private static boolean successfulMute(ApplicationPreferences preferenceStorage) {
        return preferenceStorage.setPreference(
                ApplicationPreferences.Preferences.MUTE_NOTIFICATIONS,
                1);
    }

    private static boolean successfulUnMute(ApplicationPreferences preferenceStorage) {
        return !preferenceStorage.setPreference(
                ApplicationPreferences.Preferences.MUTE_NOTIFICATIONS,
                0);
    }

    public void purchaseMolotovs(View view) {
        Log.d(TAG, "recieved request to purchase molotovs");
    }

    public boolean backPressedConsumed() {
        return zoomAnimator.undoLastAnimation();
//        ((ZombieTrackerApplication) ((Activity) view.getContext()).getApplication())
// .releaseMapComponent();  //  unaccounted for
    }
}
