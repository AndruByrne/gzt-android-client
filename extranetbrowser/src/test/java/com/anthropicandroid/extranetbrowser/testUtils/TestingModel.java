package com.anthropicandroid.extranetbrowser.testUtils;

import com.anthropicandroid.extranetbrowser.model.Occasion;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Andrew Brin on 5/25/2016.
 */
public class TestingModel {

    public static final String DOMAIN_STRING = "domain.string";
    public static final String KEY0 = "fasd876faf43fa";
    public static final String KEY1 = "jklasf345asdfg";
    public static final String KEY2 = "dfgl32jlfas4fg";
    public static final String KEY3 = "djkdfgjkfasl45";
    public static final double centerOfTestingLatitude = 37.860d;
    public static final double centerOfTestingLongitude = -122.487d;


    public static List<Occasion> getMockGlobalOccasions() {
        return new ArrayList<Occasion>() { {
            add(new Occasion(KEY0, centerOfTestingLatitude, centerOfTestingLongitude));
            add(new Occasion(KEY1, centerOfTestingLatitude-1, centerOfTestingLongitude-1));
            add(new Occasion(KEY2, centerOfTestingLatitude-2, centerOfTestingLongitude-2));
            add(new Occasion(KEY3, centerOfTestingLatitude-5, centerOfTestingLongitude-5)); // this Occasion is to not appear in test results
        }};
    }

    public static List<Occasion> getMockOccasionsSubset() {
        return new ArrayList<Occasion>(){{
            add(new Occasion(KEY0, centerOfTestingLatitude, centerOfTestingLongitude));
            add(new Occasion(KEY1, centerOfTestingLatitude-3, centerOfTestingLongitude-3));
            add(new Occasion(KEY2, centerOfTestingLatitude-4, centerOfTestingLongitude-4));
            add(new Occasion(KEY3, centerOfTestingLatitude-5, centerOfTestingLongitude-5)); // this Occasion is to not appear in test results
        }};
    }

    public static List<String> getMockRequestingKeys() {
        return new ArrayList<String>(){{
            add(DOMAIN_STRING+"/"+ KEY0);
            add(DOMAIN_STRING+"/"+ KEY1);
            add(DOMAIN_STRING+"/"+ KEY2);
        }};
    }

    public static List<String> getMockGlobalKeys() {
        return getMockRequestingKeys();
    }
}
