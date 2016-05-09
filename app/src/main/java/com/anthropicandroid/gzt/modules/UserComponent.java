package com.anthropicandroid.gzt.modules;

/*
 * Created by Andrew Brin on 5/3/2016.
 */

import com.anthropicandroid.gzt.services.SubscriptionAccountant;

import dagger.Component;

@UserScope
@Component(
        dependencies = ApplicationComponent.class,
        modules = {
                PacketMapModule.class,
                })
public interface UserComponent {

    //  expose methods for downstream adapters

    SubscriptionAccountant getSubscriptionAccountant();

}
