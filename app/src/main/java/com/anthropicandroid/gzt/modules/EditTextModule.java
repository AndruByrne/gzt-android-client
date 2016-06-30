package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 4/18/2016.
 */

import com.anthropicandroid.gzt.activity.LoopingEditTextAdapter;

import dagger.Module;
import dagger.Provides;

@Module
public class EditTextModule {

    @Provides
    LoopingEditTextAdapter getEditTextLooper() {
        return new LoopingEditTextAdapter();
    }
}
