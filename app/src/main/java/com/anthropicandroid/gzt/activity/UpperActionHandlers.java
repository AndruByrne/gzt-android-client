package com.anthropicandroid.gzt.activity;

import android.content.ContextWrapper;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.databinding.InventoryViewBinding;
import com.anthropicandroid.gzt.databinding.StoreViewBinding;
import com.anthropicandroid.gzt.services.ApplicationPreferences;

import rx.Observable;

/*
 * Created by Andrew Brin on 4/6/2016.
 */
final public class UpperActionHandlers {

    public static final String TAG = UpperActionHandlers.class.getSimpleName();
    private OpenStoreAnimator openStoreAnimator;

    public UpperActionHandlers(OpenStoreAnimator openStoreAnimator) {
        this.openStoreAnimator = openStoreAnimator;
    }


    public void muteNotifications(View view) {
        CheckBox checkBox = (CheckBox) view;
        ApplicationPreferences preferenceStorage = ((ZombieTrackerApplication) ((AppCompatActivity)
                ((ContextWrapper) checkBox.getContext()).getBaseContext()).getApplication())
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

//    public boolean purchaseMolotovs(View view, MotionEvent motionEvent) {}
    public void purchaseMolotovs(View view) {
        AppCompatActivity activity = ((AppCompatActivity) ((ContextWrapper) view.getContext())
                .getBaseContext());
        ZombieTrackerApplication application = (ZombieTrackerApplication) activity
                .getApplication();

        openStoreAnimator.animateInventoryToStoreFromCard(
                (InventoryViewBinding) DataBindingUtil.findBinding(view),
                StoreViewBinding.inflate(
                        activity.getLayoutInflater(),
                        application.createOrGetSansUserSettingsAdapterComponent()),
                (CardView) view.getParent().getParent(), //  go up view hierarchy to card
                (RelativeLayout) view.getParent(),
                view);
    }

    public Observable<Boolean> backPressedConsumed() {
        // do a complete redraw of the inventory if returning from store
        return openStoreAnimator.undoLastAnimation();
//        ((ZombieTrackerApplication) ((Activity) view.getContext()).getApplication())
// .releaseMapComponent();  //  unaccounted for
    }
}
