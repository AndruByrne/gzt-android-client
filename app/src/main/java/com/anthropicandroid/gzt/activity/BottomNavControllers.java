package com.anthropicandroid.gzt.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.databinding.GztSettingsActivityBinding;
import com.anthropicandroid.gzt.databinding.InventoryViewBinding;
import com.anthropicandroid.gzt.databinding.PowerUpsMapViewBinding;
import com.anthropicandroid.gzt.databinding.StatsViewBinding;
import com.anthropicandroid.gzt.modules.GZTMapComponent;
import com.anthropicandroid.gzt.modules.SansUserSettingsAdapterComponent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.HashMap;

/*
 * Created by Andrew Brin on 6/30/2016.
 */
public class BottomNavControllers {

    public static final String TAG = BottomNavControllers.class.getSimpleName();
    private UpperActionHandlers upperActionHandlers;
    private GZTOverlayAnimator gztOverlayAnimator;
    private HashMap<BottomNav, Integer> childrenIndices = new HashMap<>();

    public boolean backPressedConsumed() {
        return upperActionHandlers.backPressedConsumed();
    }

    enum BottomNav {
        STATS,
        INVENTORY,
        MAP
    }

    public BottomNavControllers(
            UpperActionHandlers upperActionHandlers,
            GZTOverlayAnimator gztOverlayAnimator) {
        this.upperActionHandlers = upperActionHandlers;
        this.gztOverlayAnimator = gztOverlayAnimator;
    }

    public boolean showStats(View view, MotionEvent event) {
        BottomNav navigation = BottomNav.STATS;
        Activity activity = (Activity) view.getContext();
        FrameLayout contentFrame = ((GztSettingsActivityBinding) DataBindingUtil
                .findBinding(
                        view))
                .gztSettingsContentFrame;
        Integer childIndex = childrenIndices.get(navigation);
        if (childIndex == null) {
            ZombieTrackerApplication application = (ZombieTrackerApplication) activity
                    .getApplication();
            SansUserSettingsAdapterComponent settingsAdapterComponent = application
                    .createOrGetSansUserSettingsAdapterComponent();
            StatsViewBinding statsViewBinding = StatsViewBinding.inflate(
                    activity.getLayoutInflater(),
                    settingsAdapterComponent);
            // set handlers, animate to the view and remember this
            statsViewBinding.setUserActionHandlers(upperActionHandlers);
            gztOverlayAnimator.replaceFrameContentsAt(
                    contentFrame,
                    statsViewBinding.statsRootView,
                    view.getLeft() + event.getX());
            childrenIndices.put(navigation, childrenIndices.size());
            return true;
        } else {
            View activeView = contentFrame.getChildAt(childIndex);
            if (activeView.getVisibility() == View.VISIBLE) return false;
            gztOverlayAnimator.updateVisibleChildAt(
                    contentFrame,
                    activeView,
                    view.getLeft() + event.getX());
            return true;
        }
    }

    public boolean showInventory(View view, MotionEvent event) {
        Activity activity = (Activity) view.getContext();
        BottomNav navigation = BottomNav.INVENTORY;
        FrameLayout contentFrame = ((GztSettingsActivityBinding) DataBindingUtil.findBinding(view))
                .gztSettingsContentFrame;
        Integer childIndex = childrenIndices.get(navigation);
        if (childIndex == null) {
            ZombieTrackerApplication application = (ZombieTrackerApplication) activity
                    .getApplication();
            SansUserSettingsAdapterComponent settingsAdapterComponent = application
                    .createOrGetSansUserSettingsAdapterComponent();
            InventoryViewBinding inventoryViewBinding = InventoryViewBinding.inflate(
                    activity.getLayoutInflater(),
                    settingsAdapterComponent);
            // set handlers, animate to the view and remember this
            inventoryViewBinding.setUserActionHandlers(upperActionHandlers);
            gztOverlayAnimator.replaceFrameContentsAt(
                    contentFrame,
                    inventoryViewBinding.inventoryRootView,
                    view.getLeft() + event.getX());
            childrenIndices.put(navigation, childrenIndices.size());
            return true;
        } else {
            View activeView = contentFrame.getChildAt(childIndex);
            if (activeView.getVisibility() == View.VISIBLE) return false;
            gztOverlayAnimator.updateVisibleChildAt(
                    contentFrame,
                    activeView,
                    view.getLeft() + event.getX());
            return true;
        }
    }

    public boolean showMap(View view, MotionEvent event) {
        BottomNav navigation = BottomNav.MAP;
        Activity activity = (Activity) view.getContext();
        Integer childIndex = childrenIndices.get(navigation);
        FrameLayout contentFrame = ((GztSettingsActivityBinding) DataBindingUtil
                .findBinding(
                        view))
                .gztSettingsContentFrame;
        if (childIndex == null) {
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(view.getContext())
                    != ConnectionResult.SUCCESS) {
                Log.e(TAG, "google play services not available");
            } else {
                ZombieTrackerApplication application = (ZombieTrackerApplication) activity
                        .getApplication();
                GZTMapComponent mapComponent = application.createMapComponent();
                PowerUpsMapViewBinding powerUpsMapViewBinding = PowerUpsMapViewBinding.inflate(
                        activity.getLayoutInflater(),
                        mapComponent);
                // set handlers, animate to the view and remember this
                powerUpsMapViewBinding.setUserActionHandlers(upperActionHandlers);
                gztOverlayAnimator.replaceFrameContentsAt(
                        contentFrame,
                        powerUpsMapViewBinding.extranetMapView,
                        view.getLeft() + event.getX());
                childrenIndices.put(navigation, childrenIndices.size());
                return true;
            }
            return false;
        } else {
            View activeView = contentFrame.getChildAt(childIndex);
            if (activeView.getVisibility() == View.VISIBLE) return false;
            gztOverlayAnimator.updateVisibleChildAt(
                    contentFrame,
                    activeView,
                    view.getLeft() + event.getX());
            return true;
        }
    }
}
