package com.anthropicandroid.gzt.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.anthropicandroid.gzt.databinding.InventoryViewBinding;
import com.anthropicandroid.gzt.databinding.StoreViewBinding;

import java.util.ArrayList;

/*
 * Created by Andrew Brin on 7/7/2016.
 */
public class OpenStoreAnimator {

    public static final String TAG = OpenStoreAnimator.class.getSimpleName();
    public static final int DURATION = 1500;

    public boolean undoLastAnimation() {
        return false;
    }

    public void animateInventoryToStoreFromCard(
            InventoryViewBinding inventoryViewBinding,
            StoreViewBinding storeViewBinding,
            CardView parentCard,
            RelativeLayout parentRelativeLayout,
            View clickedButton) {
        AnimatorSet animatorSet = new AnimatorSet();
        Rect parentCardRect = new Rect();
        Rect buttonRect = new Rect();
        Rect rootViewRect = new Rect();
        Point rootViewOffset = new Point();
        ((View) inventoryViewBinding.inventoryListView.getParent())
                .getGlobalVisibleRect(rootViewRect, rootViewOffset);
        // get coordinates of pressed button and surrounding card view
        parentCard.getGlobalVisibleRect(parentCardRect);
        clickedButton.getGlobalVisibleRect(buttonRect);
        rootViewRect.offset(-rootViewOffset.x, -rootViewOffset.y);
        parentCardRect.offset(-rootViewOffset.x, -rootViewOffset.y);
        buttonRect.offset(-rootViewOffset.x, -rootViewOffset.y);

        // set incoming view visibility to gone so as to not disrupt current layout on add
        storeViewBinding.storeRootView.setVisibility(View.GONE);
        // add view
        parentRelativeLayout.addView(storeViewBinding.storeRootView);
        // craft animation
        animatorSet
                .play(inventoryLeavingAnimation(
                        inventoryViewBinding,
                        parentCard.getId(),
                        parentCardRect))
                .with(cardClearingAnimation(
                        parentRelativeLayout,
                        clickedButton.getId()))
                .before(storeEntryAnimation(
                        parentCard,
                        parentCardRect,
                        rootViewRect,
                        parentRelativeLayout,
                        clickedButton,
                        buttonRect,
                        storeViewBinding));
        // add new hierarchy
        animatorSet.setInterpolator(new LinearInterpolator());
        // set view params
//        alignEnteringViewToTarget(storeViewBinding, buttonRect, rootViewRect);
        // animate
        animatorSet.start();
        // remove old views (will ensure a refresh by drawing new view in backPress handling)
    }

    private AnimatorSet cardClearingAnimation(
            RelativeLayout parentRelativeLayout,
            int clickedButtonId) {
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<>();
        int childCount = parentRelativeLayout.getChildCount();
        int i = 0;
        for (; i < childCount; i++) {
            View childAt = parentRelativeLayout.getChildAt(i);
            if (childAt.getId() != clickedButtonId) {
                animators.add(ObjectAnimator.ofFloat(childAt, View.ALPHA, 1f, 0f));
                addViewToBeDeleted(childAt);
            }
        }
        animatorSet.playTogether(animators);
        animatorSet.setInterpolator(new LinearInterpolator());
        return animatorSet;
    }

    private void addViewToBeDeleted(View view) {

    }

    private AnimatorSet inventoryLeavingAnimation(
            final InventoryViewBinding inventoryViewBinding,
            int parentCardId,
            Rect parentCardRect) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .play(headerLeaving(inventoryViewBinding.inventoryCardHeader))
                .with(listParting(
                        inventoryViewBinding.inventoryListView,
                        parentCardId,
                        parentCardRect));
        final LinearLayout inventoryRootView = inventoryViewBinding.inventoryRootView;
        animatorSet.addListener(getExitingViewRemover(inventoryRootView, inventoryViewBinding));
        return animatorSet;
    }

    private AnimatorSet listParting(ViewGroup listView, int parentCardId, Rect parentRect) {
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<>();
        Rect listRect = new Rect();
        listView.getGlobalVisibleRect(listRect);
        int inventoryItems = listView.getChildCount();
        int i = 0;
        for (; i < inventoryItems; i++) {
            View inventoryItem = listView.getChildAt(i);
            if (inventoryItem.getId() != parentCardId) {
                Rect itemRect = new Rect();
                inventoryItem.getGlobalVisibleRect(itemRect);
                if (itemRect.bottom <= parentRect.bottom) {
                    animators.add(upwardsItemLeaving(inventoryItem));
                    addViewToBeDeleted(inventoryItem);
                } else {
                    animators.add(downwardsItemLeaving(inventoryItem, listRect.bottom));
                    addViewToBeDeleted(inventoryItem);
                }
            }
        }

        animatorSet.playSequentially(animators);
        animatorSet.setInterpolator(new DecelerateInterpolator(7f));
        return animatorSet;
    }

    private AnimatorSet downwardsItemLeaving(View item, int parentBottom) {
        AnimatorSet animatorSet = new AnimatorSet();
        // should be using the item Rect for the Y of the item?
        animatorSet
                .play(ObjectAnimator.ofFloat(
                        item,
                        View.TRANSLATION_Y,
                        item.getY(),
                        parentBottom + item
                                .getHeight()));
        return animatorSet;
    }

    private AnimatorSet upwardsItemLeaving(View item) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .play(ObjectAnimator.ofFloat(item, View.TRANSLATION_Y, item.getY(), 0));
        return animatorSet;
    }

    private AnimatorSet headerLeaving(RelativeLayout header) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .play(ObjectAnimator.ofFloat(
                        header,
                        View.TRANSLATION_Y,
                        header.getY(),
                        0 - header.getHeight()));
//                .with(ObjectAnimator.ofFloat(header, View.ALPHA, 1f, 0f));;
        animatorSet.setInterpolator(new AccelerateInterpolator());
        addViewToBeDeleted(header);
        return animatorSet;
    }

    private AnimatorSet storeEntryAnimation(
            final CardView parentCard,
            Rect parentCardRect,
            final Rect rootViewRect,
            final RelativeLayout parentRelativeLayout,
            final View clickedButton,
            final Rect buttonRect,
            final StoreViewBinding storeViewBinding) {
        AnimatorSet animatorSet = new AnimatorSet();
        final RelativeLayout storeRootView = storeViewBinding.storeRootView;
        float leftBar = storeRootView.getTranslationX();
        final float heightScalingInv = ((float) buttonRect.height()) / rootViewRect.height();
        final float widthScalingInv = ((float) buttonRect.width()) / rootViewRect.width();

        // craft animation
        animatorSet
                .play(ObjectAnimator.ofFloat(
                        storeRootView,
                        View.TRANSLATION_Y,
                        buttonRect.top-(rootViewRect.height()*(1-widthScalingInv))/2,
                        0))
                .with(ObjectAnimator.ofFloat(
                        storeRootView,
                        View.TRANSLATION_X,
                        buttonRect.left - rootViewRect.width()*(1-widthScalingInv) / 2,
                        0))
                .with(ObjectAnimator.ofFloat(
                        storeRootView,
                        View.SCALE_X,
                        widthScalingInv,
                        1))
                .with(ObjectAnimator.ofFloat(
                        storeRootView,
                        View.SCALE_Y,
                        heightScalingInv,
                        1));
        animatorSet.setDuration(DURATION);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                storeRootView.setVisibility(View.VISIBLE);
                parentRelativeLayout.removeView(clickedButton);
                super.onAnimationStart(animation);
            }
        });
        animatorSet.addListener(getLoggingListener(parentCard, "parent card"));
        animatorSet.addListener(getLoggingListener(
                storeRootView, "store root view"));
        animatorSet.setInterpolator(new AccelerateInterpolator());
        return animatorSet;
    }

    @NonNull
    private Animator.AnimatorListener getLoggingListener(
            final View view,
            final String layoutName) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                Rect readingRect = new Rect();
                view.getGlobalVisibleRect(readingRect);
                Log.d(
                        TAG,
                        "at anim start, " + layoutName
                                + " rect: " + readingRect.toString()
                                + " scalingX: " + view.getScaleX()
                                + " scalingY: " + view.getScaleY()
                                + " ytrans: " + view.getTranslationY()
                                + " y: " + view.getY());
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Rect readingRect = new Rect();
                view.getGlobalVisibleRect(readingRect);
                Log.d(
                        TAG,
                        "at anim end, " + layoutName
                                + " rect: " + readingRect.toString()
                                + " scalingX: " + view.getScaleX()
                                + " scalingY: " + view.getScaleY()
                                + " ytrans: " + view.getTranslationY()
                                + " y: " + view.getY());
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        };
    }

    @NonNull
    private Animator.AnimatorListener getExitingViewRemover(
            final LinearLayout inventoryRootView, final InventoryViewBinding inventoryViewBinding) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
//                ((ViewGroup) inventoryViewBinding.molotovsCard.getChildAt(0)).removeView
//                        (inventoryViewBinding.purchasedMolotovsButton);
//
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                inventoryRootView.removeView(inventoryViewBinding.inventoryCardHeader);
                inventoryViewBinding.inventoryListView.removeView(inventoryViewBinding
                        .garlicClovesCard);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        };
    }
}
