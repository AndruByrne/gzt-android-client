package com.anthropicandroid.gzt.activity;

import android.app.Activity;
import androidx.databinding.DataBindingUtil;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.anthropicandroid.gzt.databinding.GztSettingsOverViewBinding;
import com.anthropicandroid.gzt.databinding.InventoryViewBinding;
import com.anthropicandroid.gzt.databinding.PowerUpsMapViewBinding;
import com.anthropicandroid.gzt.databinding.StatsViewBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.HashMap;

import rx.Observable;

/*
 * Created by Andrew Brin on 6/30/2016.
 */
public class BottomNavControllers {

    public static final String TAG = BottomNavControllers.class.getSimpleName();
    private UpperActionHandlers upperActionHandlers;
    private OverlayAnimator overlayAnimator;
    private HashMap<BottomNav, Integer> childrenIndices = new HashMap<>();

    public BottomNavControllers(
            UpperActionHandlers upperActionHandlers,
            OverlayAnimator overlayAnimator) {
        this.upperActionHandlers = upperActionHandlers;
        this.overlayAnimator = overlayAnimator;
    }

    public Observable<Boolean> backPressedConsumed() {
        return upperActionHandlers.backPressedConsumed();
    }

    public boolean showStats(View view, MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return false;
        BottomNav navigation = BottomNav.STATS;
        Integer childIndex = childrenIndices.get(navigation);
        FrameLayout contentFrame = ((GztSettingsOverViewBinding) DataBindingUtil
                .findBinding(
                        view))
                .gztSettingsContentFrame;
        if (childIndex == null) {
            replaceStatsViewAt(contentFrame, view.getLeft() + event.getX()); //  view added here
            childrenIndices.put(navigation, contentFrame.getChildCount() - 1); //  so -1 here
            return true;
        } else {
            View activeView = contentFrame.getChildAt(childIndex);
            if (activeView == null) {
                replaceStatsViewAt(contentFrame, view.getLeft() + event.getX()); //  view added here
                childrenIndices.put(navigation, contentFrame.getChildCount() - 1); //  so -1 here
                return true;
            }
            if (activeView.getVisibility() == View.VISIBLE) return false;
            overlayAnimator.updateVisibleChildAt(
                    contentFrame,
                    activeView,
                    view.getLeft() + event.getX());
            return true;
        }
    }

    private void replaceStatsViewAt(FrameLayout contentFrame, float xOrigin) {
        Activity activity = (Activity) contentFrame.getContext();
        StatsViewBinding statsViewBinding = StatsViewBinding.inflate(
                activity.getLayoutInflater());
        // set handlers, animate to the view and remember this
        statsViewBinding.setUserActionHandlers(upperActionHandlers);
        overlayAnimator.replaceFrameContentsAt( //  view is added here
                contentFrame,
                statsViewBinding.statsRootView,
                xOrigin);
    }

    public boolean showInventory(View view, MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return false;
        BottomNav navigation = BottomNav.INVENTORY;
        Integer childIndex = childrenIndices.get(navigation);
        FrameLayout contentFrame = ((GztSettingsOverViewBinding) DataBindingUtil.findBinding(view))
                .gztSettingsContentFrame;
        if (childIndex == null) {
            replaceInventoryViewAt(contentFrame, view.getLeft() + event.getX()); //  view added here
            childrenIndices.put(navigation, contentFrame.getChildCount() - 1); //  so -1 here
            return true;
        } else {
            View activeView = contentFrame.getChildAt(childIndex);
            if (activeView == null) {
                replaceInventoryViewAt(contentFrame, view.getLeft() + event.getX()); //  view added here
                childrenIndices.put(navigation, contentFrame.getChildCount() - 1); //  so -1 here
                return true;
            }
            if (activeView.getVisibility() == View.VISIBLE) return false;
            overlayAnimator.updateVisibleChildAt(
                    contentFrame,
                    activeView,
                    view.getLeft() + event.getX());
            return true;
        }
    }

    private void replaceInventoryViewAt(
            FrameLayout contentFrame,
            float xOrigin) {
        Activity activity = (Activity) contentFrame.getContext();
        InventoryViewBinding inventoryViewBinding = InventoryViewBinding.inflate(
                activity.getLayoutInflater());
        // set handlers, animate to the view and remember this
        inventoryViewBinding.setUserActionHandlers(upperActionHandlers);
        overlayAnimator.replaceFrameContentsAt( //  view is added here
                contentFrame,
                inventoryViewBinding.inventoryRootView,
                xOrigin);
    }

    public boolean showMap(View view, MotionEvent event) {
        if (true) return false;
        if (event.getAction() != MotionEvent.ACTION_DOWN) return false;
        BottomNav navigation = BottomNav.MAP;
        Integer childIndex = childrenIndices.get(navigation);
        FrameLayout contentFrame = ((GztSettingsOverViewBinding) DataBindingUtil
                .findBinding(
                        view))
                .gztSettingsContentFrame;
        Activity activity = (Activity) view.getContext();
        if (childIndex == null) {
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)
                    != ConnectionResult.SUCCESS) {
                Log.e(TAG, "google play services not available");
            } else {
                replaceWithMapViewAt(contentFrame, view.getLeft() + event.getX());
                childrenIndices.put(navigation, contentFrame.getChildCount() - 1);
                return true;
            }
            return false;
        } else {
            View activeView = contentFrame.getChildAt(childIndex);
            if (activeView == null) {
                replaceWithMapViewAt(contentFrame, view.getLeft() + event.getX());
                childrenIndices.put(navigation, contentFrame.getChildCount() - 1);
                return true;
            }
            if (activeView.getVisibility() == View.VISIBLE) return false;
            overlayAnimator.updateVisibleChildAt(
                    contentFrame,
                    activeView,
                    view.getLeft() + event.getX());
            return true;
        }
    }

    private void replaceWithMapViewAt(
            FrameLayout contentFrame,
            float xOrigin) {
        if (true) return;
        PowerUpsMapViewBinding powerUpsMapViewBinding = PowerUpsMapViewBinding.inflate(
                ((Activity) contentFrame.getContext()).getLayoutInflater());
        // set handlers, animate to the view and remember this
        powerUpsMapViewBinding.setUserActionHandlers(upperActionHandlers);
        overlayAnimator.replaceFrameContentsAt( //  view is added here
                contentFrame,
                powerUpsMapViewBinding.extranetMapView,
                xOrigin);
    }

    enum BottomNav {
        STATS,
        INVENTORY,
        MAP
    }
}
