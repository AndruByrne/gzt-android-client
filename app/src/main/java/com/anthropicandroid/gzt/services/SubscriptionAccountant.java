package com.anthropicandroid.gzt.services;

import java.util.ArrayList;

import rx.Subscription;

/*
 * Created by Andrew Brin on 5/5/2016.
 */
public class SubscriptionAccountant {
    private Subscription downloadSubscription;

    private ArrayList<Subscription> audioSubscriptions;

    public SubscriptionAccountant() {
    }
}