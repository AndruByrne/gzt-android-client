package com.anthropicandroid.gzt.services;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/*
 * Created by Andrew Brin on 5/5/2016.
 */
public class SubscriptionAccountant {

    private Subscription downloadSubscription;

    private ArrayList<Subscription> audioSubscriptions;

    public SubscriptionAccountant() {
    }

    public void subscribeAudio(Observable<Integer> observable, Subscriber<Integer> subscriber) {
        if (downloadSubscription != null) unsubscribeDownload();
        audioSubscriptions.add(observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber));
    }

    public void subscribeDownload(Observable<Integer> observable, Subscriber<Integer> subscriber) {
        if (audioSubscriptions.size() > 0) unsubscribeAudio();
        unsubscribeDownload();
        downloadSubscription = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void unsubscribeAudio() {
        int i = 0;
        int subscriptions = audioSubscriptions.size();
        for (; i < subscriptions; i++) {
            audioSubscriptions.get(i).unsubscribe();
            audioSubscriptions.remove(i);
        }
    }

    public void unsubscribeDownload() {
        if (downloadSubscription != null && !downloadSubscription.isUnsubscribed())
            downloadSubscription.unsubscribe();
        downloadSubscription = null;
    }
}