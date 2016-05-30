package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/29/2016.
 */
public class TestExtranetAPIModule extends ExtranetAPIModule{

    private ExtranetAPI testExtranetAPI;

    public TestExtranetAPIModule(ExtranetAPI testExtranetAPI) { this.testExtranetAPI = testExtranetAPI; }

    public ExtranetAPI getTestExtranetAPI() { return testExtranetAPI; }
}
