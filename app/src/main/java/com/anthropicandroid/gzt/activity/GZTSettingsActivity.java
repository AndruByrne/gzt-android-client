package com.anthropicandroid.gzt.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.anthropicandroid.BR;
import com.anthropicandroid.R;
import com.anthropicandroid.databinding.GztSettingsActivityBinding;
import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.modules.SansUserSettingsAdapterComponent;

final public class GZTSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate and Bind
        ZombieTrackerApplication application = (ZombieTrackerApplication) getApplication();
        SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent = application.createSansUserAdapterComponent();
        // bootstrap into dagger graph
        final GztSettingsActivityBinding gztSettingsActivityBinding = DataBindingUtil.setContentView(
                this,
                R.layout.gzt_settings_activity,
                sansUserSettingsAdapterComponent);
        // assign touch handlers
        gztSettingsActivityBinding.setVariable(BR.touch_handlers, sansUserSettingsAdapterComponent.getTouchHandlers());
    }
}