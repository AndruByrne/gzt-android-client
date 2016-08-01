package com.anthropicandroid.gzt.activity;

/*
 * Created by Andrew Brin on 6/16/2016.
 */

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.anthropicandroid.gzt.R;

public class OverlayAnimator {

    public static final String TAG = OverlayAnimator.class.getSimpleName();
    private final int DURATION;

    public OverlayAnimator(Resources resources) {
        DURATION = resources.getInteger(R.integer.duration_overlay_view);
    }

    public void replaceFrameContentsAt(
            final FrameLayout contentFrame,
            View activeView,
            float xOrigin) {
        AnimatorSet animatorSet = getAnimatorSet(contentFrame, activeView, xOrigin);
        contentFrame.addView(activeView);
        animatorSet.start();
    }

    public boolean updateVisibleChildAt(
            final FrameLayout contentFrame,
            View activeView,
            float xOrigin) {
        try {
            AnimatorSet animatorSet = getAnimatorSet(contentFrame, activeView, xOrigin);
            animatorSet.start();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "error updating to existing child: " + e.getMessage());
            return false;
        }
    }

    @NonNull
    private AnimatorSet getAnimatorSet(
            final FrameLayout contentFrame,
            final View activeView,
            float xOrigin) {
        Rect finalBounds = new Rect();
        Point globalOffset = new Point();
        AnimatorSet animatorSet = new AnimatorSet();

        final View visibleView = getVisibleWithin(contentFrame);
        contentFrame.getGlobalVisibleRect(finalBounds, globalOffset);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);
        ObjectAnimator activeYPosAnim = getActiveYPosAnim(activeView, finalBounds);
        ObjectAnimator activeYScaleAnim = getActiveYScaleAnim(activeView);
        ObjectAnimator activeXPosAnim = getActiveXPosAnim(activeView, finalBounds, xOrigin);
        ObjectAnimator activeXScaleAnim = getActiveXScaleAnim(activeView);

        if (visibleView == null) {
            animatorSet
                    .play(activeYPosAnim)
                    .with(activeYScaleAnim)
                    .with(activeXPosAnim)
                    .with(activeXScaleAnim);
        } else {
            activeYScaleAnim.addListener(getAnimationReset(activeView, visibleView));
//            activeXScaleAnim.addListener(getLoggingListener(activeView));
            animatorSet
                    .play(activeYPosAnim)
                    .with(activeYScaleAnim)
                    .with(activeXPosAnim)
                    .with(activeXScaleAnim)
                    .with(getLeavingAlphaAnim(visibleView));
        }
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(DURATION);
        return animatorSet;
    }

    @NonNull
    private static Animator.AnimatorListener getAnimationReset(
            final View activeView,
            final View visibleView) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                activeView.setVisibility(View.VISIBLE);
                activeView.setAlpha(1f);
            }

            @Override
            public void onAnimationEnd(Animator animation) { visibleView.setVisibility(View.GONE); }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        };
    }

    private ObjectAnimator getLeavingAlphaAnim(View leavingView) {
        return ObjectAnimator.ofFloat(leavingView, View.ALPHA, 1f, 0f);
    }

    private ObjectAnimator getActiveYScaleAnim(View activeView) {
        return ObjectAnimator.ofFloat(activeView, View.SCALE_Y, 0f, 1f);
    }

    private ObjectAnimator getActiveXScaleAnim(View activeView) {
        return ObjectAnimator.ofFloat(activeView, View.SCALE_X, 0f, 1f);
    }

    private ObjectAnimator getActiveYPosAnim(View activeView, Rect finalBounds) {
        return ObjectAnimator.ofFloat(
                activeView,
                View.Y,
                finalBounds.bottom,
                finalBounds.top);
    }

    private ObjectAnimator getActiveXPosAnim(View activeView, Rect finalBounds, float origin) {
        return ObjectAnimator.ofFloat(
                activeView,
                View.X,
                origin - finalBounds.right / 2,
//                0,
                finalBounds.left);
    }

    private static View getVisibleWithin(FrameLayout contentFrame) {
        int childCount = contentFrame.getChildCount();
        View childView;
        for (int i = 0; i < childCount; i++) {
            childView = contentFrame.getChildAt(i);
            if (childView.getVisibility() == View.VISIBLE)
                return childView;
        }
        return null;
    }
}
