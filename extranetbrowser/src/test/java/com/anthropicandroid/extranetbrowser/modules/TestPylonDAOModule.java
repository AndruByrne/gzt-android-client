package com.anthropicandroid.extranetbrowser.modules;

import android.content.Context;

import com.anthropicandroid.extranetbrowser.model.PylonDAO;

import org.mockito.Mockito;

/*
 * Created by Andrew Brin on 5/25/2016.
 */
public class TestPylonDAOModule
        extends PylonDAOModule
{

    @Override
    public PylonDAO getWaspHolder(Context context) {
        return Mockito.mock(PylonDAO.class);
    }
}
