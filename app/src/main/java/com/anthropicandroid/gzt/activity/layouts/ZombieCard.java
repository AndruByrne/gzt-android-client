package com.anthropicandroid.gzt.activity.layouts;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

import com.anthropicandroid.gzt.R;

/*
 * Created by Andrew Brin on 7/28/2016.
 */
public class ZombieCard extends CardView{

    public ZombieCard(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.zombieCardStyle);
    }

    public ZombieCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
