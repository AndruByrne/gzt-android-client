package com.anthropicandroid.gzt.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.test.mock.MockApplication;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.anthropicandroid.R;
import com.anthropicandroid.databinding.GztSettingsActivityBinding;
import com.anthropicandroid.databinding.PowerUpsMapHolderBinding;
import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.modules.GZTMapComponent;
import com.anthropicandroid.gzt.services.ApplicationPreferences;

/*
 * Created by Andrew Brin on 4/6/2016.
 */
final public class TouchHandlers {

    public static final String TAG = TouchHandlers.class.getCanonicalName();
    GZTAnimatorRepository animatorSetRepository;
    private GztSettingsActivityBinding activityBinding;

    public TouchHandlers(GZTAnimatorRepository animatorSetRepository) {
        this.animatorSetRepository = animatorSetRepository;
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
        activityBinding = DataBindingUtil.findBinding(view);

        ImageView mapViewOrigin = activityBinding.mapViewOrigin; //  get views for start and stop bounds
        LinearLayout rootView = activityBinding.gztSettingsRootView;

        GZTMapComponent mapComponent = application.createMapComponent(); //  create map component and assign it to the map view
        PowerUpsMapHolderBinding mapHolderBinding = PowerUpsMapHolderBinding.inflate(
                context.getLayoutInflater(),
                mapComponent);

        mapHolderBinding.setTouchHandlers(this); //  assign touch handlers to map view

        View mapViewFrame = mapHolderBinding.mapViewFrame; //  get map view

        animatorSetRepository.initializeAnimationSet(mapViewFrame, mapViewOrigin, rootView); //  store necessary variables for animation

        mapViewOrigin.setAlpha(0f);  //  disappear old view and add new view
        rootView.addView(mapViewFrame);
        mapViewFrame.bringToFront(); //  bring to front for systems without elevation
    }

    public void hide_powerups(View view) {
        animatorSetRepository.unZoomAndReplaceWithInDuration(view, activityBinding.mapViewOrigin, 1000);
        ((ZombieTrackerApplication) ((Activity) view.getContext()).getApplication()).retireMapComponent();

    }

    public void purchaseMolotovs(View view) {
        Log.d(TAG, "recieved request to purchase molotovs");
    }

    static public void onGoClickedNoEmail(View view) {
        Toast.makeText(view.getContext(), R.string.no_email_toast, Toast.LENGTH_SHORT).show();
    }

    public void onPlayClicked(View view) {

    }

    public void onProgressBarClicked(View view) {

    }
}
