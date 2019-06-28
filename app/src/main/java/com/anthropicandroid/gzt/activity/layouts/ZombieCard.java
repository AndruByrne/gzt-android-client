package com.anthropicandroid.gzt.activity.layouts;

import android.content.Context;

import com.anthropicandroid.gzt.R;
import com.google.android.material.card.MaterialCardView;

import android.util.AttributeSet;

/*
 * Created by Andrew Brin on 7/28/2016.
 */
public class ZombieCard extends MaterialCardView{

    public ZombieCard(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.zombieCardStyle);
    }

    public ZombieCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
