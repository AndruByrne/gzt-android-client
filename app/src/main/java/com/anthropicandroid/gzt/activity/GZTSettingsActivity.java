package com.anthropicandroid.gzt.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;

import com.anthropicandroid.gzt.R;
import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.databinding.GztSettingsActivityBinding;
import com.anthropicandroid.gzt.modules.SansUserSettingsAdapterComponent;

import javax.inject.Inject;

import rx.functions.Action1;

final public class GZTSettingsActivity extends AppCompatActivity {

    public static final String TAG = GZTSettingsActivity.class.getSimpleName();

    @Inject public MapViewLifecycleHolder mapViewHolder;
    @Inject public BottomNavControllers bottomNavControllers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate and Bind
        ZombieTrackerApplication application = (ZombieTrackerApplication) getApplication();
        SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent = application
                .createOrGetSansUserSettingsAdapterComponent();
        // bootstrap into dagger graph
        sansUserSettingsAdapterComponent.inject(this);
        final GztSettingsActivityBinding gztSettingsActivityBinding = DataBindingUtil
                .setContentView(
                        this,
                        R.layout.gzt_settings_activity,
                        sansUserSettingsAdapterComponent);

        mapViewHolder.onCreate(savedInstanceState);
        // assign user action handlers
        gztSettingsActivityBinding.setBottomNavControllers(bottomNavControllers);
        bottomNavControllers.showStats(
                gztSettingsActivityBinding.statsNavButton,
                MotionEvent.obtain( //  honestly...
                        SystemClock.uptimeMillis(), //  downtime
                        SystemClock.uptimeMillis(), //  eventTime
                        MotionEvent.ACTION_DOWN, //  action
                        (float) getResources().getConfiguration().screenWidthDp / 6, // 3 button x
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

    @Override
    protected void onResume() {
        super.onResume();
        mapViewHolder.onResume();
    }

    @Override
    protected void onPause() {
        mapViewHolder.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapViewHolder.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        bottomNavControllers
                .backPressedConsumed()
                .subscribe(
                        new Action1<Boolean>() {
                            @Override
                            public void call(Boolean result) {
                                Log.d(TAG, "get result of value: " + result);
                                if (!result) backpressNotHandled();
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e(TAG, "error performing backpress: " + throwable.getMessage());
                                throwable.printStackTrace();
                            }
                        });
    }

    private void backpressNotHandled() {
        super.onBackPressed();
    }
}