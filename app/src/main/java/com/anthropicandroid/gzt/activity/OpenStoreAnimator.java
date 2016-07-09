package com.anthropicandroid.gzt.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
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

    @NonNull
    private static Animator.AnimatorListener getEntryValuesSetter(
            final Button button,
            final StoreViewBinding storeViewBinding) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
//                storeViewBinding.storeRootView.setAlpha(0f);
//                button.setText("");
                Rect buttonRect = new Rect();
                button.getGlobalVisibleRect(buttonRect);
//                alignEnteringViewToTarget(storeViewBinding, buttonRect);
                storeViewBinding.storeRootView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        };
    }

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
        LinearLayout inventoryListView = inventoryViewBinding.inventoryListView;
        // get coordinates of pressed button and surrounding card view
        parentCard.getGlobalVisibleRect(parentCardRect);
        clickedButton.getGlobalVisibleRect(buttonRect);
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
                        inventoryListView,
                        (Button) clickedButton,
                        storeViewBinding));
        // add new hierarchy
        animatorSet.setInterpolator(new LinearInterpolator());
        //debugging
//        animatorSet.addListener(getLoggingListener(inventoryViewBinding.inventoryCardHeader));
        alignEnteringViewToTarget(storeViewBinding, buttonRect);
        // add view
        parentRelativeLayout.addView(storeViewBinding.storeRootView);
        // animate
        animatorSet.start();
        // remove old views (will ensure a refresh by drawing new view in backPress handling)
    }

    private static void alignEnteringViewToTarget(
            StoreViewBinding storeViewBinding,
            Rect buttonRect) {// set incoming view visibility to gone so as to not disrupt
        // current layout on add
        storeViewBinding.storeRootView.setVisibility(View.GONE);
        // add new view
        Log.d(
                TAG,
                "aligning incoming to rect top: " + buttonRect.top + " bottom: " + buttonRect
                        .bottom + " left: " + buttonRect.left + " right: " + buttonRect.right);
        storeViewBinding.storeRootView.setTop(buttonRect.top);
        storeViewBinding.storeRootView.setBottom(buttonRect.bottom);
        storeViewBinding.storeRootView.setRight(buttonRect.right);
        storeViewBinding.storeRootView.setLeft(buttonRect.left);
        int leftBar = storeViewBinding.storeRootView.getLeft();
        Log.d(TAG, "left bar opening : "+ leftBar); //  TODO(Andrew Brin): change all
        // meausrement over to rects so this confusion doesn't happen
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
//        animators.add(ObjectAnimator.ofFloat(
//                parentCard,
//                View.ALPHA,
//                parentCard.getAlpha(),
//                1f));
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
                .play(ObjectAnimator.ofFloat(item, View.Y, item.getY(), parentBottom + item
                        .getHeight()));
        return animatorSet;
    }

    private AnimatorSet upwardsItemLeaving(View item) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .play(ObjectAnimator.ofFloat(item, View.Y, item.getY(), 0));
        return animatorSet;
    }

    private AnimatorSet headerLeaving(RelativeLayout header) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .play(ObjectAnimator.ofFloat(
                        header,
                        View.Y,
                        header.getY(),
                        0 - header.getHeight()));
//                .with(ObjectAnimator.ofFloat(header, View.ALPHA, 1f, 0f));;
        animatorSet.setInterpolator(new AccelerateInterpolator());
        addViewToBeDeleted(header);
        return animatorSet;
    }

    private AnimatorSet storeEntryAnimation(
            CardView parentCard,
            Rect parentCardRect,
            LinearLayout invListView,
            final Button button,
            final StoreViewBinding storeViewBinding) {
        AnimatorSet animatorSet = new AnimatorSet();
        Rect listRect = new Rect();
        invListView.getGlobalVisibleRect(listRect);
        Log.d(
                TAG,
                "inventoryListview top: " + listRect.top + " bottom: " + listRect.bottom + " " +
                        "left: " +
                        "" + listRect.left + " right: " + listRect.right);
        int rootViewHeight = ((View) invListView.getParent()).getHeight();
        int leftBar = storeViewBinding.storeRootView.getLeft();
        Log.d(TAG, "left bar: "+ leftBar);
        animatorSet
//                .play(ObjectAnimator.ofFloat(storeViewBinding.storeRootView, View.ALPHA, 0f, 1f))
                .play(ObjectAnimator.ofFloat(
                        parentCard,
                        View.Y,
                        parentCard.getTop(),
                        rootViewHeight / 2))
                .with(ObjectAnimator.ofFloat(
                        parentCard,
                        View.SCALE_Y,
                        (float) rootViewHeight / parentCard.getHeight()
                ))
//                .with(ObjectAnimator.ofFloat(parentCard, View.X, parentCardRect.left, 0));
                .with(ObjectAnimator.ofFloat(storeViewBinding.storeRootView, View.Y,
                        storeViewBinding.storeRootView.getTop(), 0))
                .with(ObjectAnimator.ofFloat(storeViewBinding.storeRootView, View.X,
                        leftBar, 0))
                .with(ObjectAnimator.ofFloat(storeViewBinding.storeRootView, View.SCALE_X, 1f))
                .with(ObjectAnimator.ofFloat(storeViewBinding.storeRootView, View.SCALE_Y, 1f));
        animatorSet.setDuration(2000);
        animatorSet.addListener(getEntryValuesSetter(button, storeViewBinding));
        animatorSet.addListener(getLoggingListener(parentCard));
        animatorSet.addListener(getLoggingListener(storeViewBinding.storeRootView));
        animatorSet.setInterpolator(new AccelerateInterpolator());
        return animatorSet;
    }

    @NonNull
    private Animator.AnimatorListener getLoggingListener(final View relativeLayout) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                Rect readingRect = new Rect();
                relativeLayout.getGlobalVisibleRect(readingRect);
                Log.d(
                        TAG,
                        "at anim start, targetlayout left: " + readingRect.left
                                + " top:" + readingRect.top
                                + " bottom: " + readingRect.bottom);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Rect readingRect = new Rect();
                relativeLayout.getGlobalVisibleRect(readingRect);
                Log.d(
                        TAG,
                        "at anim end, targetlayout left: " + readingRect.left
                                + " top:" + readingRect.top
                                + " bottom: " + readingRect.bottom);
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
                ((ViewGroup) inventoryViewBinding.molotovsCard.getChildAt(0)).removeView
                        (inventoryViewBinding.purchasedMolotovsButton);

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
