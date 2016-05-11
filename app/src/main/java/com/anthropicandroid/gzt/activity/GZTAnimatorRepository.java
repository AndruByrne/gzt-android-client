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
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.anthropicandroid.gzt.ZombieTrackerApplication;

public class GZTAnimatorRepository {

    public static final String TAG = GZTAnimatorRepository.class.getSimpleName();
    private AnimatorSet currentAnimatorSet;

    private final SparseArray<AnimationPrecursor> animationPrecursors = new SparseArray<>();

    public GZTAnimatorRepository() {
    }

    public void initializeAnimationSet(View targetView, View beginningView, View viewToFill) {
        // get stand and end bounds as well as global offset; build an animation precursor with those params and add to map
        Log.d(TAG, "initializing animation set");
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        beginningView.getGlobalVisibleRect(startBounds);
        viewToFill.getGlobalVisibleRect(finalBounds, globalOffset);

        //  index by id of targetView
        animationPrecursors.put(targetView.getId(), new AnimationPrecursor(startBounds, finalBounds, globalOffset));
    }

    public void zoomToViewInDuration(final View viewToZoomTo, int animationDuration) {
        if (currentAnimatorSet != null) currentAnimatorSet.cancel();
        AnimationPrecursor precursor = animationPrecursors.get(viewToZoomTo.getId());
        if (precursor == null)
            Log.e(TAG, "referenced null animator precursor while zooming ");
        else {
            viewToZoomTo.setPivotX(0f);
            viewToZoomTo.setPivotY(0f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet //  assign location and scaling values with precursor as beginning
                    .play(ObjectAnimator.ofFloat(viewToZoomTo, View.X, precursor.startBounds.left, precursor.finalBounds.left))
                    .with(ObjectAnimator.ofFloat(viewToZoomTo, View.Y, precursor.startBounds.top, precursor.finalBounds.top))
                    .with(ObjectAnimator.ofFloat(viewToZoomTo, View.SCALE_X, precursor.startScale, 1f))
                    .with(ObjectAnimator.ofFloat(viewToZoomTo, View.SCALE_Y, precursor.startScale, 1f));
            animatorSet.setDuration(animationDuration);
            animatorSet.setInterpolator(new DecelerateInterpolator());
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
            animatorSet.start();
            currentAnimatorSet = animatorSet;
        }
    }

    public void unZoomAndReplaceWithInDuration(final View zoomedView, final View viewToOpacify, int duration) {
        AnimationPrecursor precursor = animationPrecursors.get(zoomedView.getId());
        if (precursor == null)
            Log.e(TAG, "precursor null when unZooming ");
        else {
            if (currentAnimatorSet != null) currentAnimatorSet.cancel();
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet //  assign location and scaling values with precursor as target
                    .play(ObjectAnimator.ofFloat(zoomedView, View.X, precursor.startBounds.left))
                    .with(ObjectAnimator.ofFloat(zoomedView, View.Y, precursor.startBounds.top))
                    .with(ObjectAnimator.ofFloat(zoomedView, View.SCALE_X, precursor.startScale))
                    .with(ObjectAnimator.ofFloat(zoomedView, View.SCALE_Y, precursor.startScale));
            animatorSet
                    .setDuration(duration)
                    .setInterpolator(new DecelerateInterpolator());
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    ((ViewGroup) zoomedView.getRootView()).removeView(zoomedView); //  remove zooming view from layout
                    viewToOpacify.setAlpha(1f); //  restore old view's opacity
                    currentAnimatorSet = null; //  remove animation
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    ((ViewGroup)viewToOpacify.getRootView()).removeView(zoomedView); //  remove zooming view from layout
                    viewToOpacify.setAlpha(1f); //  restore old view's opacity
                    currentAnimatorSet = null; //  remove animation
                }
            });
            animatorSet.start();
            currentAnimatorSet = animatorSet;
        }
    }

    private static class AnimationPrecursor {

        private Rect startBounds;
        private Rect finalBounds;
        private float startScale;

        public AnimationPrecursor(Rect startBounds, Rect finalBounds, Point globalOffset) {
            // calculate starting and ending bounds for the zoomed-in view
            this.startBounds = startBounds;
            this.finalBounds = finalBounds;
            startBounds.offset(-globalOffset.x, -globalOffset.y);
            finalBounds.offset(-globalOffset.x, -globalOffset.y);
            // set starting bounds to same aspect ratio as final bounds
            if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / finalBounds.height()) {
                //  extend start bounds horizontally
                startScale = (float) startBounds.height() / finalBounds.height();
                float startWidth = startScale * finalBounds.width();
                float deltaWidth = (startWidth - finalBounds.width()) / 2;
                startBounds.left -= deltaWidth;
                startBounds.right += deltaWidth;
            } else {
                //  extend start bounds vertically
                startScale = (float) startBounds.width() / finalBounds.width();
                float startHeight = startScale * finalBounds.height();
                float deltaHeight = (startHeight - finalBounds.height()) / 2;
                startBounds.top -= deltaHeight;
                startBounds.bottom += deltaHeight;
            }
        }
    }
}
