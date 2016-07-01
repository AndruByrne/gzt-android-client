package com.anthropicandroid.gzt.activity;

/*
 * Created by Andrew Brin on 6/16/2016.
 */

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

public class GZTOverlayAnimator {

    public static final String TAG = GZTOverlayAnimator.class.getSimpleName();

    public void replaceFrameContentsWith(
            final FrameLayout contentFrame,
            View activeView) {
        AnimatorSet animatorSet = getAnimatorSet(contentFrame, activeView);
        contentFrame.addView(activeView);
        animatorSet.start();
    }

    public boolean updateVisibleChildWith(final FrameLayout contentFrame, View activeView) {
        try {
            AnimatorSet animatorSet = getAnimatorSet(contentFrame, activeView);
            animatorSet.start();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "error updating to existing child: " + e.getMessage());
            return false;
        }
    }

    @NonNull
    private AnimatorSet getAnimatorSet(final FrameLayout contentFrame, final View activeView) {
        Rect finalBounds = new Rect();
        Point globalOffset = new Point();
        AnimatorSet animatorSet = new AnimatorSet();

        final View visibleView = getVisibleWithin(contentFrame);
        contentFrame.getGlobalVisibleRect(finalBounds, globalOffset);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);
        if (visibleView == null) {
            animatorSet
                    .play(getActiveYPosAnim(activeView, finalBounds))
                    .with(getActiveYScaleAnim(activeView))
                    .with(getActiveXScaleAnim(activeView));
        } else {
            ObjectAnimator activeYScaleAnim = getActiveYScaleAnim(activeView);
            animatorSet
                    .play(getActiveYPosAnim(activeView, finalBounds))
                    .with(activeYScaleAnim)
                    .with(getActiveXScaleAnim(activeView))
                    .with(getLeavingAlphaAnim(visibleView));
            activeYScaleAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    activeView.setVisibility(View.VISIBLE);
                    activeView.setAlpha(1f);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    visibleView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) { }

                @Override
                public void onAnimationRepeat(Animator animation) { }
            });
        }
        animatorSet.setInterpolator(new DecelerateInterpolator());
        return animatorSet;
    }

    private ObjectAnimator getLeavingAlphaAnim(View leavingView) {
        return ObjectAnimator.ofFloat(leavingView, View.ALPHA, 1f, 0f);
    }

    private ObjectAnimator getLeavingYScaleAnim(View leavingView) {
        return ObjectAnimator.ofFloat(leavingView, View.SCALE_Y, 1f, 0f);
    }

    private ObjectAnimator getLeavingXScaleAnim(View leavingView) {
        return ObjectAnimator.ofFloat(leavingView, View.SCALE_X, 1f, 0f);
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
