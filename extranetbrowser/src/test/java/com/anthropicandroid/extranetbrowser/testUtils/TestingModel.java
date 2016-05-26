package com.anthropicandroid.extranetbrowser.testUtils;

import com.anthropicandroid.extranetbrowser.model.Occasion;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Andrew Brin on 5/25/2016.
 */
public class TestingModel {

    static private double centerOfTestingLatitude = 37.860d;
    static private double centerOfTestingLongitude = -122.487d;


    public static List<Occasion> getGlobalOccasions() {
        return new ArrayList<Occasion>() { {
            add(new Occasion(centerOfTestingLatitude, centerOfTestingLongitude));
            add(new Occasion(centerOfTestingLatitude-1, centerOfTestingLongitude-1));
            add(new Occasion(centerOfTestingLatitude-2, centerOfTestingLongitude-2));
        }};
    }
}
