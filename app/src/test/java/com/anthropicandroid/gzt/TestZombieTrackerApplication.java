package com.anthropicandroid.gzt;

/*
 * Created by Andrew Brin on 3/2/2016.
 */

import com.anthropicandroid.gzt.modules.TestThreadingModule;
import com.anthropicandroid.gzt.modules.ThreadingModule;

public class TestZombieTrackerApplication extends ZombieTrackerApplication {

    @Override
    public ThreadingModule getThreadingModule(){
        return new TestThreadingModule();
    }
}
