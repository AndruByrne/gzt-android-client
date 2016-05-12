package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 5/10/2016.
 */

import android.databinding.DataBindingComponent;

import com.anthropicandroid.gzt.activity.GZTZoomAnimator;

import dagger.Component;

@MapScope
@Component(
        dependencies = ApplicationComponent.class
)
public interface GZTMapComponent extends DataBindingComponent{
    GZTZoomAnimator getGZTAnimatorSetRepository();
}
