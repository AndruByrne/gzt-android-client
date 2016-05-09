package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 5/4/2016.
 */

import android.databinding.DataBindingComponent;

import com.anthropicandroid.gzt.activity.TouchHandlers;
import com.anthropicandroid.gzt.services.ApplicationPreferences;

import dagger.Component;

@SansUserSettingsAdapterScope
@Component(
        dependencies = ApplicationComponent.class,
        modules = {
                EditTextModule.class,
        })

public interface SansUserSettingsAdapterComponent extends DataBindingComponent {
    TouchHandlers getTouchHandlers();
    ApplicationPreferences getPreferenceStorage();
}
