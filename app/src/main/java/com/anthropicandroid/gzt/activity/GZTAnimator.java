package com.anthropicandroid.gzt.activity;

/*
 * Created by Andrew Brin on 5/10/2016.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.Stack;

public class GZTAnimator {

    public static final String TAG = GZTAnimator.class.getSimpleName();
    public static final int UNZOOM_SPEED = 500;
    public static final int ZOOM_SPEED = 700;

    private AnimatorSet currentAnimatorSet;
    private Stack<View> reversableViews = new Stack<>();

    private final SparseArray<AnimationPrecursor> animationPrecursors = new SparseArray<>();

    public GZTAnimator() {
    }

    public void addViewAndPrepareToZoom(View targetView, View beginningView, View viewToMatch) {
        // get stand and end bounds as well as global offset; build an animation precursor with those params and add to map
        Log.d(TAG, "initializing animation set");
        View rootView = beginningView.getRootView();
        //  index by id of targetView
        animationPrecursors.put(targetView.getId(), new AnimationPrecursor(
                targetView,
                beginningView,
                viewToMatch));
        //  disappear old view and add new view
        beginningView.setAlpha(0f);
        ((ViewGroup) rootView).addView(targetView); //  this view starts the animation
        targetView.bringToFront(); //  bring to front for systems without elevation
    }

    public void zoomToView(final View viewToZoomTo) { //  will eventually be animateView
        if (currentAnimatorSet != null) currentAnimatorSet.cancel();
        AnimationPrecursor precursor = animationPrecursors.get(viewToZoomTo.getId());
        if (precursor == null)
            Log.e(TAG, "referenced null animator precursor while zooming ");
        else {
            viewToZoomTo.setPivotX(0f);
            viewToZoomTo.setPivotY(0f);
            AnimatorSet animatorSet = getZoomAnimatorSet(precursor);
            animatorSet.start();
            currentAnimatorSet = animatorSet;
            reversableViews.add(viewToZoomTo);
        }
    }

    public boolean undoLastAnimation() {
        return !reversableViews.isEmpty() && undoAnimation(reversableViews.pop());
    }

    private boolean undoAnimation(View view) { //  should be general unanimate
        int id = view.getId();
        AnimationPrecursor animationPrecursor = animationPrecursors.get(id);
        animationPrecursors.delete(id);
        // when other animations are added, add a switch for a animation-type enum in precursor
        return unZoomPrecursor(animationPrecursor);
    }

    private boolean unZoomPrecursor(final AnimationPrecursor precursor) {
        if (precursor == null) {
            Log.e(TAG, "precursor null when unZooming ");
            return false;
        } else {
            if (currentAnimatorSet != null) currentAnimatorSet.cancel();
            AnimatorSet animatorSet = getUnZoomAnimatorSet(precursor);
            animatorSet.start();
            currentAnimatorSet = animatorSet;
            return true;
        }
    }

    @NonNull
    private AnimatorSet getZoomAnimatorSet(final AnimationPrecursor precursor) {
        AnimatorSet animatorSet = new AnimatorSet();
        Log.d(TAG, "startbounds top: " + precursor.startBounds.top + " bottom: " + precursor.startBounds.bottom);
        animatorSet //  assign location and scaling values with precursor as beginning
                .play(ObjectAnimator.ofFloat(precursor.targetView, View.Y, precursor.startBounds.top, precursor.finalBounds.top))
                .with(ObjectAnimator.ofFloat(precursor.targetView, View.X, precursor.startBounds.left, precursor.finalBounds.left))
                .with(ObjectAnimator.ofFloat(precursor.targetView, View.SCALE_X, precursor.startScale, 1f))
                .with(ObjectAnimator.ofFloat(precursor.targetView, View.SCALE_Y, precursor.startScale, 1f));
        animatorSet.setDuration(ZOOM_SPEED);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                currentAnimatorSet = null;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                currentAnimatorSet = null;
            }
        });
        return animatorSet;
    }

    @NonNull
    private AnimatorSet getUnZoomAnimatorSet(final AnimationPrecursor precursor) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet //  assign location and scaling values with precursor as target
                .play(ObjectAnimator.ofFloat(precursor.targetView, View.X, precursor.startBounds.left))
                .with(ObjectAnimator.ofFloat(precursor.targetView, View.Y, precursor.startBounds.top))
                .with(ObjectAnimator.ofFloat(precursor.targetView, View.SCALE_X, precursor.startScale))
                .with(ObjectAnimator.ofFloat(precursor.targetView, View.SCALE_Y, precursor.startScale));
        animatorSet
                .setDuration(UNZOOM_SPEED)
                .setInterpolator(new DecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                ((ViewGroup) precursor.rootView).removeView(precursor.targetView); //  remove zooming view from layout
                precursor.beginningView.setAlpha(1f); //  restore old view's opacity
                currentAnimatorSet = null; //  remove animation
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ((ViewGroup) precursor.rootView).removeView(precursor.targetView); //  remove zooming view from layout
                precursor.beginningView.setAlpha(1f); //  restore old view's opacity
                currentAnimatorSet = null; //  remove animation
            }
        });
        return animatorSet;
    }

    private static class AnimationPrecursor {

        private View targetView;
        private View beginningView;
        private View rootView;
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();
        final private float startScale;

        public AnimationPrecursor(View targetView, View beginningView, View viewToMatch) {
            this.targetView = targetView;
            this.beginningView = beginningView;
            this.rootView = beginningView.getRootView();
            // calculate starting and ending bounds for the zoomed-in view

            beginningView.getGlobalVisibleRect(startBounds);
            viewToMatch.getGlobalVisibleRect(finalBounds, globalOffset);

//            startBounds.offset(-globalOffset.x, -globalOffset.y);
            finalBounds.offset(-globalOffset.x, -globalOffset.y);
            // set starting bounds to same aspect ratio as final bounds
            if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()) {
                //  extend start bounds horizontally
                startScale = (float) startBounds.height() / finalBounds.height();
                float startWidth = startScale * finalBounds.width();
                float deltaWidth = (startWidth - startBounds.width()) / 2;
                startBounds.left -= deltaWidth;
                startBounds.right += deltaWidth;
            } else {
                //  extend start bounds vertically
                Log.d(TAG, "extending vertically");
                startScale = (float) startBounds.width() / finalBounds.width();
                float startHeight = startScale * finalBounds.height();
                float deltaHeight = (startHeight - startBounds.height()) / 2;
                startBounds.top -= deltaHeight;
                startBounds.bottom += deltaHeight;
            }
        }
    }
}
