package com.anthropicandroid.gzt.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.anthropicandroid.extranetbrowser.ExtranetMapView;
import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.databinding.GztSettingsActivityBinding;
import com.anthropicandroid.gzt.databinding.PowerUpsMapHolderBinding;
import com.anthropicandroid.gzt.modules.GZTMapComponent;
import com.anthropicandroid.gzt.services.ApplicationPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/*
 * Created by Andrew Brin on 4/6/2016.
 */
final public class UserActionHandlers {

    public static final String TAG = UserActionHandlers.class.getSimpleName();

    private GZTAnimator zoomAnimator;

    public UserActionHandlers(GZTAnimator zoomAnimator) {
        this.zoomAnimator = zoomAnimator;
    }

    public void muteNotifications(View view) {
        CheckBox checkBox = (CheckBox) view;
        ApplicationPreferences preferenceStorage = ((ZombieTrackerApplication) ((Activity) checkBox.getContext()).getApplication())
                .getApplicationComponent().getPreferenceStorage();

        if (checkBox.isChecked()) //  user is muting notifications (view was just checked)
        {
            boolean checked = preferenceStorage.setPreference(ApplicationPreferences.Preferences.MUTE_NOTIFICATIONS, 1);
            checkBox.setChecked(checked);
        } else //  user is unmuting notifications, success in unmuting occassions a removal of checkmark
        {
            boolean checked = !preferenceStorage.setPreference(ApplicationPreferences.Preferences.MUTE_NOTIFICATIONS, 0);
            checkBox.setChecked(checked);
        }
    }

    public void show_powerups(View view) {
        Activity context = (Activity) view.getContext();
        ZombieTrackerApplication application = (ZombieTrackerApplication) context.getApplication();
        GztSettingsActivityBinding activityBinding = DataBindingUtil.findBinding(view);

        GoogleApiAvailability instance = GoogleApiAvailability.getInstance();
        if (instance.isGooglePlayServicesAvailable(view.getContext()) != ConnectionResult.SUCCESS) {
            Log.e(TAG, "google play services not available");
        } else {
            Log.d(TAG, "google play services available");
            GZTMapComponent mapComponent = application.createMapComponent(); //  create map component and assign it to the map view
            PowerUpsMapHolderBinding mapHolderBinding = PowerUpsMapHolderBinding.inflate(
                    context.getLayoutInflater(),
                    mapComponent);

            mapHolderBinding.setUserActionHandlers(this); //  assign touch handlers to map view

//            View viewToMatch = activityBinding.aCardView; //  get view to fill
            LinearLayout viewToMatch = activityBinding.gztSettingsRootView; //  get view to fill
            ExtranetMapView extranetMapView = mapHolderBinding.extranetMapView; //  get map view
            zoomAnimator.addViewAndPrepareToZoom(extranetMapView, view, viewToMatch); //  store necessary variables for animation and launch view with binding adapter with zoom
        }
    }
    public void purchaseMolotovs(View view) {
        Log.d(TAG, "recieved request to purchase molotovs");
    }

    public boolean backPressedConsumed() {
        return zoomAnimator.undoLastAnimation();
//        ((ZombieTrackerApplication) ((Activity) view.getContext()).getApplication()).releaseMapComponent();  //  unaccounted for
    }
}
