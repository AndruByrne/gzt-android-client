package com.anthropicandroid.extranetbrowser.modules;

import android.content.Context;

import com.anthropicandroid.extranetbrowser.model.WaspHolder;

import org.mockito.Mockito;

/*
 * Created by Andrew Brin on 5/25/2016.
 */
public class TestWaspModule extends WaspModule {

    @Override
    public WaspHolder getWaspHolder(Context context) {
        return Mockito.mock(WaspHolder.class);
    }
}
