package com.anthropicandroid.gzt.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.databinding.GztSettingsActivityBinding;
import com.anthropicandroid.gzt.databinding.InventoryViewBinding;
import com.anthropicandroid.gzt.databinding.PowerUpsMapViewBinding;
import com.anthropicandroid.gzt.databinding.StatsViewBinding;
import com.anthropicandroid.gzt.modules.GZTMapComponent;
import com.anthropicandroid.gzt.modules.SansUserSettingsAdapterComponent;
import com.anthropicandroid.gzt.services.ApplicationPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/*
 * Created by Andrew Brin on 4/6/2016.
 */
final public class UserActionHandlers {

    public static final String TAG = UserActionHandlers.class.getSimpleName();

    private GZTZoomAnimator zoomAnimator;
    private GZTOverlayAnimator gztOverlayAnimator;

    enum BottomNav {
        STATS,
        INVENTORY,
        MAP
    }

    public UserActionHandlers(GZTZoomAnimator zoomAnimator, GZTOverlayAnimator gztOverlayAnimator) {
        this.zoomAnimator = zoomAnimator;
        this.gztOverlayAnimator = gztOverlayAnimator;
    }

    public void showStats(View view) {
        Activity context = (Activity) view.getContext();
        ZombieTrackerApplication application = (ZombieTrackerApplication) context.getApplication();
        GztSettingsActivityBinding activityBinding = DataBindingUtil.findBinding(view);
        if (!gztOverlayAnimator.recoverView(BottomNav.STATS
                , activityBinding.gztSettingsContentFrame)) {
            SansUserSettingsAdapterComponent settingsAdapterComponent = application.createOrGetSansUserSettingsAdapterComponent();
            StatsViewBinding statsViewBinding = StatsViewBinding.inflate(context.getLayoutInflater(), settingsAdapterComponent);
            statsViewBinding.setUserActionHandlers(this);
            gztOverlayAnimator.replaceFrameContentsWith(activityBinding.gztSettingsContentFrame, statsViewBinding.statsRootView, BottomNav.STATS);
        }
    }

    public void showInventory(View view) {
        Activity context = (Activity) view.getContext();
        ZombieTrackerApplication application = (ZombieTrackerApplication) context.getApplication();
        GztSettingsActivityBinding activityBinding = DataBindingUtil.findBinding(view);
        if (!gztOverlayAnimator.recoverView(BottomNav.INVENTORY, activityBinding.gztSettingsContentFrame)) {
            SansUserSettingsAdapterComponent settingsAdapterComponent = application.createOrGetSansUserSettingsAdapterComponent();
            InventoryViewBinding inventoryViewBinding = InventoryViewBinding.inflate(context.getLayoutInflater(), settingsAdapterComponent);
            inventoryViewBinding.setUserActionHandlers(this);
            gztOverlayAnimator.replaceFrameContentsWith(activityBinding.gztSettingsContentFrame, inventoryViewBinding.inventoryRootView, BottomNav.INVENTORY);
        }
    }

    public void showMap(View view) {
        Activity context = (Activity) view.getContext();
        ZombieTrackerApplication application = (ZombieTrackerApplication) context.getApplication();
        GztSettingsActivityBinding activityBinding = DataBindingUtil.findBinding(view);
        GoogleApiAvailability instance = GoogleApiAvailability.getInstance();
        if (instance.isGooglePlayServicesAvailable(view.getContext()) != ConnectionResult.SUCCESS) {
            Log.e(TAG, "google play services not available");
        } else {
            Log.d(TAG, "google play services available");
            if (!gztOverlayAnimator.recoverView(BottomNav.MAP, activityBinding.gztSettingsContentFrame)) {
                GZTMapComponent mapComponent = application.createMapComponent(); //  create map component and assign it to the map view
                PowerUpsMapViewBinding mapBinding = PowerUpsMapViewBinding.inflate(context.getLayoutInflater(), mapComponent);
                gztOverlayAnimator.replaceFrameContentsWith(activityBinding.gztSettingsContentFrame, mapBinding.extranetMapView, BottomNav.MAP);
            }
        }
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

    public void purchaseMolotovs(View view) {
        Log.d(TAG, "recieved request to purchase molotovs");
    }

    public boolean backPressedConsumed() {
        return zoomAnimator.undoLastAnimation();
//        ((ZombieTrackerApplication) ((Activity) view.getContext()).getApplication()).releaseMapComponent();  //  unaccounted for
    }
}
