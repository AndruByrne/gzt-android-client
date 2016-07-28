package com.anthropicandroid.gzt.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.databinding.DataBindingUtil;
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
import java.util.LinkedList;
import java.util.Queue;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/*
 * Created by Andrew Brin on 7/7/2016.
 */
public class OpenStoreAnimator {

    public static final String TAG = OpenStoreAnimator.class.getSimpleName();
    public static final int DURATION = 666;
    public static final int HALF_DURATION = DURATION / 2;
    private Queue<ObjectAnimator> reversableListOutAnims = new LinkedList<>();
    private Queue<ObjectAnimator> reversableStoreInAnims = new LinkedList<>();
    private ViewGroup currentStoreLayout;

    public Observable<Boolean> undoLastAnimation() {
        if (currentStoreLayout == null
                || reversableStoreInAnims.size() == 0
                || reversableListOutAnims.size() == 0)
            return Observable.just(false);
        // Get binding for removing view after animations
        final InventoryViewBinding binding = DataBindingUtil
                .findBinding((ViewGroup) currentStoreLayout.getParent());
        // Will emit queue of store animations after 2 subscriptions
        Observable<ObjectAnimator> animatorObs = Observable
                .from(reversableStoreInAnims)
                .publish()
                .autoConnect(2);
        // Will reverse each animator
        Observable<Boolean> reversals = animatorObs
                .map(new Func1<ObjectAnimator, Boolean>() {
                    @Override
                    public Boolean call(ObjectAnimator objectAnimator) {
                        objectAnimator.reverse();
                        return true;
                    }
                });
        // Will subscribe to the last animator, return true & remove store view on listener callback
        Observable<Boolean> lastListeningObs = animatorObs
                .last()
                .flatMap(new Func1<ObjectAnimator, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(final ObjectAnimator objectAnimator) {
                        return Observable
                                .create(new Observable.OnSubscribe<Boolean>() {
                                    @Override
                                    public void call(final Subscriber<? super Boolean> subscriber) {
                                        objectAnimator.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                subscriber.onNext(true);
                                            }
                                        });
                                        if (!objectAnimator.isRunning()) {
                                            subscriber.onNext(true);
                                        }
                                    }
                                })
                                .take(1);
                    }
                })
                .map(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        binding.inventoryRootView.removeView(currentStoreLayout);
                        currentStoreLayout = null;
                        return true;
                    }
                });
        // Will pass subscribing action to both obs above, on both returning, returns true
        // and removes remaining animations, then clears queues.
        return Observable.combineLatest(
                lastListeningObs,
                reversals,
                new Func2<Boolean, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean listenerReturn, Boolean aBoolean2) {
                        return listenerReturn;
                    }
                })
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean success) {
                        return success;
                    }
                })
                .first()
                .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean result) {
                        // Simpler process with rest of animations
                        return reverseLaterAnimations();
                    }
                })
                .doOnNext(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        // Clear queue
                        reversableStoreInAnims.clear();
                        reversableListOutAnims.clear();
                    }
                });
    }

    private Observable<Boolean> reverseLaterAnimations() {
        // Reverse the inventory animations then return true
        return Observable
                .from(reversableListOutAnims)
                .map(new Func1<ObjectAnimator, Boolean>() {
                    @Override
                    public Boolean call(ObjectAnimator objectAnimator) {
                        objectAnimator.reverse();
                        return true;
                    }
                })
                .last();
    }

    public void animateInventoryToStoreFromCard(
            final InventoryViewBinding inventoryViewBinding,
            StoreViewBinding storeViewBinding,
            CardView parentCard,
            RelativeLayout parentRelativeLayout,
            View clickedButton) {
        AnimatorSet animatorSet = new AnimatorSet();
        Rect parentCardRect = new Rect();
        Rect buttonRect = new Rect();
        Rect rootViewRect = new Rect();
        Point rootViewOffset = new Point();
        LinearLayout rootView = inventoryViewBinding.inventoryRootView;
        final RelativeLayout rootStoreLayout = storeViewBinding.storeRootView;
        // get offset
        rootView.getGlobalVisibleRect(rootViewRect, rootViewOffset);
        // get coordinates of pressed button and surrounding card view
        parentCard.getGlobalVisibleRect(parentCardRect);
        clickedButton.getGlobalVisibleRect(buttonRect);
        // transmit offset
        rootViewRect.offset(-rootViewOffset.x, -rootViewOffset.y);
        parentCardRect.offset(-rootViewOffset.x, -rootViewOffset.y);
        buttonRect.offset(-rootViewOffset.x, -rootViewOffset.y);

        // set incoming view visibility to gone so as to not disrupt current layout on add
        rootStoreLayout.setVisibility(View.GONE);

        // add store hierarchy
        rootView.addView(rootStoreLayout);

        // define animation
        animatorSet
                .play(inventoryLeavingAnimation(
                        inventoryViewBinding,
                        parentCard.getId(),
                        parentCardRect))
                .with(cardClearingAnimation(
                        parentRelativeLayout,
                        clickedButton.getId()))
                .before(getStoreEntryAnimation(
                        rootViewRect,
                        parentCardRect,
                        buttonRect,
                        storeViewBinding,
                        inventoryViewBinding));
        // add new hierarchy
        animatorSet.setInterpolator(new LinearInterpolator());
        // animate
        animatorSet.start();
        // remove old views (will ensure a refresh by drawing new view in backPress handling)
    }

    private AnimatorSet cardClearingAnimation(
            RelativeLayout parentRelativeLayout,
            int clickedButtonId) {
        // Remove items in the card whose button was clicked
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<>();
        int childCount = parentRelativeLayout.getChildCount();
        int i = 0;
        for (; i < childCount; i++) {
            View childAt = parentRelativeLayout.getChildAt(i);
            if (childAt.getId() != clickedButtonId) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(childAt, View.ALPHA, 1f, 0f);
                animators.add(animator);
                reversableListOutAnims.add(animator);
            }
        }
        animatorSet.playTogether(animators);
        animatorSet.setStartDelay(HALF_DURATION);
        animatorSet.setDuration(HALF_DURATION);
        animatorSet.setInterpolator(new LinearInterpolator());
        return animatorSet;
    }

    private AnimatorSet inventoryLeavingAnimation(
            final InventoryViewBinding inventoryViewBinding,
            int parentCardId,
            Rect parentCardRect) {
        // Part cards on top and bottom of card with clicked button
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .play(headerLeaving(inventoryViewBinding.inventoryCardHeader))
                .with(listParting(
                        inventoryViewBinding.inventoryListView,
                        parentCardId,
                        parentCardRect));
        animatorSet.setDuration(DURATION);
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
                    ObjectAnimator animator = upwardsItemLeaving(inventoryItem);
                    animators.add(animator);
                    reversableListOutAnims.add(animator);
                } else {
                    ObjectAnimator animator = downwardsItemLeaving(inventoryItem, listRect.bottom);
                    animators.add(animator);
                    reversableListOutAnims.add(animator);
                }
            }
        }

        animatorSet.playSequentially(animators);
        animatorSet.setInterpolator(new DecelerateInterpolator(2f));
        return animatorSet;
    }

    private ObjectAnimator downwardsItemLeaving(View item, int parentBottom) {
        // should be using the item Rect for the Y of the item?
        return ObjectAnimator.ofFloat(
                item,
                View.TRANSLATION_Y,
                0,
                parentBottom - item.getTop());
    }

    private ObjectAnimator upwardsItemLeaving(View item) {
        return ObjectAnimator.ofFloat(item, View.TRANSLATION_Y, item.getY(), 0);
    }

    private AnimatorSet headerLeaving(RelativeLayout header) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                header,
                View.TRANSLATION_Y,
                0,
                0 - header.getHeight());
        animatorSet.play(animator);
        reversableListOutAnims.add(animator);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        return animatorSet;
    }

    private AnimatorSet getStoreEntryAnimation(
            final Rect rootViewRect,
            Rect parentCardRect,
            final Rect buttonRect,
            final StoreViewBinding storeViewBinding,
            final InventoryViewBinding inventoryViewBinding) {
        final RelativeLayout storeContentView = storeViewBinding.storeViewContent;
        final RelativeLayout rootStoreLayout = storeViewBinding.storeRootView;
        AnimatorSet animatorSet = new AnimatorSet();

        // define animation
        AnimatorSet storeRootEntry = getStoreRootEntry(
                rootStoreLayout,
                parentCardRect,
                rootViewRect);
        storeRootEntry.addListener(getLayoutLogisticsListener(
                inventoryViewBinding,
                rootStoreLayout));
        animatorSet
                .play(getStoreContentEntry(
                        storeContentView,
                        buttonRect,
                        parentCardRect))
                .with(storeRootEntry);
        animatorSet.setDuration(HALF_DURATION);
//        animatorSet.addListener(getLoggingListener(storeContentView, "store root view"));
        animatorSet.setInterpolator(new AccelerateInterpolator());
        return animatorSet;
    }

    @NonNull
    private AnimatorListenerAdapter getLayoutLogisticsListener(
            final InventoryViewBinding inventoryViewBinding,
            final RelativeLayout rootStoreLayout) {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                rootStoreLayout.setVisibility(View.VISIBLE);
                currentStoreLayout = rootStoreLayout;
            }
        };
    }

    private AnimatorSet getStoreRootEntry(
            RelativeLayout rootStoreLayout,
            Rect parentCardRect,
            Rect rootViewRect) {
        final float heightScalingInv = ((float) parentCardRect.height()) / rootViewRect.height();
        final float widthScalingInv = ((float) parentCardRect.width()) / rootViewRect.width();
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator yTrans = ObjectAnimator.ofFloat(
                rootStoreLayout,
                View.TRANSLATION_Y,
                parentCardRect.top - (rootViewRect.height() * (1 - heightScalingInv)) / 2,
                0);
        reversableStoreInAnims.add(yTrans);
        ObjectAnimator xTrans = ObjectAnimator.ofFloat(
                rootStoreLayout,
                View.TRANSLATION_X,
                parentCardRect.left - (rootViewRect.width() * (1 - widthScalingInv)) / 2,
                0);
        reversableStoreInAnims.add(xTrans);
        ObjectAnimator yScale = ObjectAnimator.ofFloat(
                rootStoreLayout,
                View.SCALE_Y,
                heightScalingInv,
                1);
        reversableStoreInAnims.add(yScale);
        ObjectAnimator xScale = ObjectAnimator.ofFloat(
                rootStoreLayout,
                View.SCALE_X,
                widthScalingInv,
                1);
        reversableStoreInAnims.add(xScale);
        animatorSet
                .play(yTrans)
                .with(xTrans)
                .with(yScale)
                .with(xScale);
        return animatorSet;
    }

    private AnimatorSet getStoreContentEntry(
            RelativeLayout contentView,
            Rect buttonRect,
            Rect parentCardRect) {
        final float widthScalingInv = ((float) buttonRect.width()) / parentCardRect.width();
        final float heightScalingInv = ((float) buttonRect.height()) / parentCardRect.height();

        AnimatorSet animatorSet = new AnimatorSet();
        // y Trans handled by expanding root view + layout_margin
        ObjectAnimator xTrans = ObjectAnimator.ofFloat(
                contentView,
                View.TRANSLATION_X,
                buttonRect.left - (parentCardRect.width() * (1 - widthScalingInv)) / 2,
                0);
        reversableStoreInAnims.add(xTrans);
        ObjectAnimator xScale = ObjectAnimator.ofFloat(
                contentView,
                View.SCALE_X,
                widthScalingInv,
                1);
        reversableStoreInAnims.add(xScale);
        ObjectAnimator yScale = ObjectAnimator.ofFloat(
                contentView,
                View.SCALE_Y,
                heightScalingInv,
                1);
        reversableStoreInAnims.add(yScale);
        animatorSet
                .play(xTrans)
                .with(yScale)
                .with(xScale);
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
}
