package com.anthropicandroid.gzt.activity;

import android.opengl.Matrix;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.activity.rendering.SceneRenderer;
import com.anthropicandroid.gzt.modules.SansUserSettingsAdapterComponent;
import com.google.vr.ndk.base.DaydreamApi;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;

import javax.inject.Inject;
import javax.microedition.khronos.egl.EGLConfig;

import rx.functions.Action1;

public class GZTSettingsActivity extends AppCompatActivity {
    public static final String TAG = GZTSettingsActivity.class.getSimpleName();
    private static final int EXIT_FROM_VR_REQUEST_CODE = 42;

    @Inject
    public MapViewLifecycleHolder mapViewHolder;

    private GZTSettingsView settingsView;
    private Renderer renderer;
    private ControllerManager controllerManager;
    private Controller controller;
    // Given an intent with a media file and format, this will load the file and generate the mesh.
    private MediaLoader mediaLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaLoader = new MediaLoader(this);

        ZombieTrackerApplication application = (ZombieTrackerApplication) getApplication();
        SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent = application
                .createOrGetSansUserSettingsAdapterComponent();
        // bootstrap into dagger graph
        sansUserSettingsAdapterComponent.inject(this);

        mapViewHolder.onCreate(savedInstanceState);
   }

    /**
     * Tries to exit gracefully from VR using a VR transition dialog.
     *
     * @return whether the exit request has started or whether the request failed due to the device
     * not being Daydream Ready
     */
    private boolean exitFromVr() {
        // This needs to use GVR's exit transition to avoid disorienting the user.
        DaydreamApi api = DaydreamApi.create(this);
        if (api != null) {
            api.exitFromVr(this, EXIT_FROM_VR_REQUEST_CODE, null);
            // Eventually, the Activity's onActivityResult will be called.
            api.close();
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapViewHolder.onResume();
        controllerManager.start();
        mediaLoader.resume();
    }

    @Override
    protected void onPause() {
        mediaLoader.pause();
        controllerManager.stop();
        mapViewHolder.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mediaLoader.destroy();
        settingsView.setMediaPlayer(null);
        mapViewHolder.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        settingsView.bottomNavControllers
                .backPressedConsumed()
                .subscribe(
                        new Action1<Boolean>() {
                            @Override
                            public void call(Boolean result) {
                                if (!result) backpressNotHandled();
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e(TAG, "error performing backpress: ", throwable);
                            }
                        });
    }

    private void backpressNotHandled() {
        super.onBackPressed();
    }

    /**
     * Standard GVR renderer. Most of the real work is done by {@link SceneRenderer}.
     */
    private class Renderer implements GvrView.StereoRenderer {
        private static final float Z_NEAR = .1f;
        private static final float Z_FAR = 100;

        // Used by ControllerEventListener to manipulate the scene.
        public final SceneRenderer scene;

        private final float[] viewProjectionMatrix = new float[16];

        /**
         * Creates the Renderer and configures the VR exit button.
         *
         * @param parent Any View that is already attached to the Window. The uiView will secretly be
         *               attached to this View in order to properly handle UI events.
         */
        @MainThread
        public Renderer(ViewGroup parent) {
            Pair<SceneRenderer, GZTSettingsView> pair
                    = SceneRenderer.createForVR(GZTSettingsActivity.this, parent);
            scene = pair.first;
            settingsView = pair.second;

            settingsView.setVrIconClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!exitFromVr()) {
                                // Directly exit Cardboard Activities.
                                onActivityResult(EXIT_FROM_VR_REQUEST_CODE, RESULT_OK, null);
                            }
                        }
                    });
        }

        @Override
        public void onNewFrame(HeadTransform headTransform) {
        }

        @Override
        public void onDrawEye(Eye eye) {
            Matrix.multiplyMM(
                    viewProjectionMatrix, 0, eye.getPerspective(Z_NEAR, Z_FAR), 0, eye.getEyeView(), 0);
            scene.glDrawFrame(viewProjectionMatrix, eye.getType());
        }

        @Override
        public void onFinishFrame(Viewport viewport) {
        }

        @Override
        public void onSurfaceCreated(EGLConfig config) {
            scene.glInit();
            mediaLoader.onGlSceneReady(scene);
        }

        @Override
        public void onSurfaceChanged(int width, int height) {
        }

        @Override
        public void onRendererShutdown() {
            scene.glShutdown();
        }
    }

    /**
     * Forwards Controller events to SceneRenderer.
     */
    private class ControllerEventListener extends Controller.EventListener
            implements ControllerManager.EventListener {
        private boolean touchpadDown = false;
        private boolean appButtonDown = false;

        @Override
        public void onApiStatusChanged(int status) {
            Log.i(TAG, ".onApiStatusChanged " + status);
        }

        @Override
        public void onRecentered() {
        }

        @Override
        public void onUpdate() {
            controller.update();

            renderer.scene.setControllerOrientation(controller.orientation);

            if (!touchpadDown && (controller.clickButtonState || controller.triggerButtonState)) {
                renderer.scene.handleClick();
            }

            if (!appButtonDown && controller.appButtonState) {
                renderer.scene.toggleUi();
            }

            touchpadDown = controller.clickButtonState || controller.triggerButtonState;
            appButtonDown = controller.appButtonState;
        }
    }
}
