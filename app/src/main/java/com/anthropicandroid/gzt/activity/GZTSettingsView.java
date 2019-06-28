package com.anthropicandroid.gzt.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.activity.rendering.CanvasQuad;
import com.anthropicandroid.gzt.databinding.GztSettingsOverViewBinding;
import com.anthropicandroid.gzt.modules.SansUserSettingsAdapterComponent;

final public class GZTSettingsView extends RelativeLayout {

    public static final String TAG = GZTSettingsView.class.getSimpleName();

    // The canvasQuad is only not null when this View is in a VR Activity. It provides the backing
    // canvas that standard Android child Views render to.
    @Nullable
    private CanvasQuad canvasQuad;

    // package-private
    BottomNavControllers bottomNavControllers;

    public GZTSettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @MainThread
    public static GZTSettingsView createForOpenGl(Context context, ViewGroup parent, CanvasQuad canvasQuad) {
        ZombieTrackerApplication application = (ZombieTrackerApplication) ((Activity) context).getApplication();
        SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent = application
                .createOrGetSansUserSettingsAdapterComponent();
        // Inflate and Bind
        GztSettingsOverViewBinding gztSettingsOverViewBinding = GztSettingsOverViewBinding.inflate(
                ((Activity) context).getLayoutInflater());

        GZTSettingsView settingsView = gztSettingsOverViewBinding.settingsRootLayout;
        settingsView.canvasQuad = canvasQuad;
        settingsView.bottomNavControllers = sansUserSettingsAdapterComponent.getBottomNavHandlers();
        settingsView.setLayoutParams(CanvasQuad.getLayoutParams());
        settingsView.setVisibility(VISIBLE);
        settingsView.showInventory(gztSettingsOverViewBinding.inventoryNavButton);

        return settingsView;
    }

    /**
     * Renders this View and its children to either Android View hierarchy's Canvas or to the VR
     * scene's CanvasQuad.
     *
     * @param androidUiCanvas used in 2D mode to render children to the screen
     */
    @Override
    public void dispatchDraw(Canvas androidUiCanvas) {
        if (canvasQuad == null) {
            // Handle non-VR rendering.
            super.dispatchDraw(androidUiCanvas);
            return;
        }

        // Handle VR rendering.
        Canvas glCanvas = canvasQuad.lockCanvas();
        if (glCanvas == null) {
            // This happens if Android tries to draw this View before GL initialization completes. We need
            // to retry until the draw call happens after GL invalidation.
            postInvalidate();
            return;
        }

        // Clear the canvas first.
        glCanvas.drawColor(Color.BLACK);
        // Have Android render the child views.
        super.dispatchDraw(glCanvas);
        // Commit the changes.
        canvasQuad.unlockCanvasAndPost(glCanvas);
    }

    private boolean showInventory(View animationOrigin) {
        return bottomNavControllers.showInventory(
                animationOrigin,
                MotionEvent.obtain( //  honestly...
                        SystemClock.uptimeMillis(), //  downtime
                        SystemClock.uptimeMillis(), //  eventTime
                        MotionEvent.ACTION_DOWN, //  action
                        (float)

                                getResources()
                                        .getConfiguration().screenWidthDp / 2, // 3 button x
                        0f, //  y
                        1f, //  pressure
                        .5f, //  size
                        0, //  metaState
                        1f, //  xPrecision
                        1f, //  yPrecision
                        0, //  deviceId
                        0 //  edgeFlags
                ));
    }

    /**
     * Ignores 2D touch events when this View is used in a VR Activity.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (canvasQuad == null) {
            // Not in VR mode so use standard behavior.
            return super.onInterceptTouchEvent(event);
        }

        if (ActivityManager.isRunningInTestHarness()) {
            // If your app uses UI Automator tests, it's useful to have this touch system handle touch
            // events created during tests. This allows you to create UI tests that work while the app
            // is in VR.
            return false;
        }

        // We are in VR mode. Synthetic events generated by SceneRenderer are marked as SOURCE_GAMEPAD
        // events. For this class of events, we will let the Android Touch system handle the event so we
        // return false. Other classes of events were generated by the user accidentally touching the
        // screen where this hidden view is attached.
        if (event.getSource() != InputDevice.SOURCE_GAMEPAD) {
            // Intercept and suppress touchscreen events so child buttons aren't clicked.
            return true;
        } else {
            // Don't intercept SOURCE_GAMEPAD events. onTouchEvent will handle these.
            return false;
        }
    }

    /**
     * Handles standard Android touch events or synthetic VR events.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (canvasQuad != null) {
            // In VR mode so process controller events & ignore touchscreen events.
            if (event.getSource() != InputDevice.SOURCE_GAMEPAD) {
                // Tell the system that we handled the event. This prevents children from seeing the event.
                return true;
            } else {
                // Have the system send the event to child Views and they will handle clicks.
                return super.onTouchEvent(event);
            }
        } else {
            // Not in VR mode so use standard behavior.
            return super.onTouchEvent(event);
        }
    }
}