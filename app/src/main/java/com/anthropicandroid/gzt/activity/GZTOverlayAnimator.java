package com.anthropicandroid.gzt.activity;

/*
 * Created by Andrew Brin on 6/16/2016.
 */

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

public class GZTOverlayAnimator {
    public void replaceFrameContentsWith(final FrameLayout contentFrame, View activeView) {
        Rect finalBounds = new Rect();
        Point globalOffset = new Point();
        contentFrame.getGlobalVisibleRect(finalBounds, globalOffset);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);
        AnimatorSet animatorSet = new AnimatorSet();
        final View possibleChild = contentFrame.getChildAt(0);
        if (possibleChild == null)
            animatorSet
                    .play(getActiveYPosAnim(activeView, finalBounds))
                    .with(getActiveYScaleAnim(activeView));
        else {
            ObjectAnimator leavingYScaleAnim = getLeavingYScaleAnim(possibleChild);
            animatorSet
                    .play(getActiveYPosAnim(activeView, finalBounds))
                    .with(getActiveYScaleAnim(activeView))
                    .with(getLeavingTransAnim(possibleChild))
                    .with(getLeavingXScaleAnim(possibleChild))
                    .with(leavingYScaleAnim);
            leavingYScaleAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // no op
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    contentFrame.removeView(possibleChild);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    // no op
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    // no op
                }
            });
        }
        animatorSet.setInterpolator(new DecelerateInterpolator());
        contentFrame.addView(activeView);
        animatorSet.start();

    }

    private ObjectAnimator getLeavingTransAnim(View leavingView) {
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

    private ObjectAnimator getActiveYPosAnim(View activeView, Rect finalBounds) {
        return ObjectAnimator.ofFloat(
                activeView,
                View.Y,
                finalBounds.bottom,
                finalBounds.top);
    }
}
