package com.anthropicandroid.extranetbrowser.testUtils;

import com.anthropicandroid.extranetbrowser.model.Occasion;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Andrew Brin on 5/25/2016.
 */
public class TestingModel {

    public static final String DOMAIN_STRING = "domain.string";
    static private double centerOfTestingLatitude = 37.860d;
    static private double centerOfTestingLongitude = -122.487d;


    public static List<Occasion> getMockGlobalOccasions() {
        return new ArrayList<Occasion>() { {
            add(new Occasion(centerOfTestingLatitude, centerOfTestingLongitude));
            add(new Occasion(centerOfTestingLatitude-1, centerOfTestingLongitude-1));
            add(new Occasion(centerOfTestingLatitude-2, centerOfTestingLongitude-2));
        }};
    }

    public static List<Occasion> getMockOccasionsSubset() {
        return new ArrayList<Occasion>(){{
            add(new Occasion(centerOfTestingLatitude, centerOfTestingLongitude));
            add(new Occasion(centerOfTestingLatitude-3, centerOfTestingLongitude-3));
            add(new Occasion(centerOfTestingLatitude-4, centerOfTestingLongitude-4));
        }};
    }

    public static List<String> getMockRequestingKeys() {
        return new ArrayList<String>(){{
            add(DOMAIN_STRING+"/"+"fasd876faf43fa");
            add(DOMAIN_STRING+"/"+"jklasf345asdfg");
            add(DOMAIN_STRING+"/"+"dfgl32jlfas4fg");
        }};
    }
}
