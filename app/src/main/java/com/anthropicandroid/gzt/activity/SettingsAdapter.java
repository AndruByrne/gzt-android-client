package com.anthropicandroid.gzt.activity;

import android.app.Activity;
import android.databinding.BindingAdapter;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.anthropicandroid.R;
import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.modules.SansUserSettingsAdapterComponent;
import com.anthropicandroid.gzt.services.ApplicationPreferences;

/*
 * Created by Andrew Brin on 5/6/2016.
 */
public class SettingsAdapter {

    @BindingAdapter("checked_settings_value")
    public static void getChecked(SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent, CheckBox checkbox, ApplicationPreferences.Preferences preference) {
        ApplicationPreferences applicationPreferences = sansUserSettingsAdapterComponent.getPreferenceStorage();
        checkbox.setChecked(0 != applicationPreferences.getPreference(preference));
    }

    @BindingAdapter("settings_value")
    public static void getSettingsValue(SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent, TextView textView, ApplicationPreferences.Preferences preference) {
        ApplicationPreferences preferenceStorage = sansUserSettingsAdapterComponent.getPreferenceStorage();
        textView.setText(Integer.toString(preferenceStorage.getPreference(preference)));
    }

    @BindingAdapter("settings_warning_value")
    public static void getSettingsWarningValue(SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent, TextView textView, ApplicationPreferences.Preferences preference) {
        ApplicationPreferences preferenceStorage = sansUserSettingsAdapterComponent.getPreferenceStorage();
        Integer userPreference = preferenceStorage.getPreference(preference);
        if (userPreference == 0)
            textView.setTextColor(textView.getResources().getColor(R.color.stop_red));
        else
            textView.setTextColor(textView.getResources().getColor(R.color.bright_green));

        textView.setText(Integer.toString(userPreference));
    }

    @BindingAdapter("purchased_settings_value")
    public static void getPurchasedSettingsValue(SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent, Button button, ApplicationPreferences.Preferences preference) {
        ApplicationPreferences preferenceStorage = sansUserSettingsAdapterComponent.getPreferenceStorage();
        Integer userPreference = preferenceStorage.getPreference(preference);
        if (userPreference == 0)
            button.setTextColor(button.getResources().getColor(R.color.trans_red));
        else
            button.setTextColor(button.getResources().getColor(R.color.bright_green));

        button.setText(Integer.toString(userPreference));
    }

    @BindingAdapter("settings_health_value")
    public static void getSettingsHealthValue(SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent, TextView textView, ApplicationPreferences.Preferences preference) {
        ApplicationPreferences preferenceStorage = sansUserSettingsAdapterComponent.getPreferenceStorage();
        Integer userPreference = preferenceStorage.getPreference(preference);
        if (userPreference == 0) {
            textView.setText(R.string.healthy_status_label);
            textView.setTextColor(textView.getResources().getColor(R.color.bright_green));
        } else if (userPreference == 1) {
            textView.setText(R.string.bitten_status_label);
            textView.setTextColor(textView.getResources().getColor(R.color.trans_red));
        } else if (userPreference > 1) {
            textView.setText(R.string.zombie_status_label);
            textView.setTextColor(textView.getResources().getColor(R.color.pure_black));
        }
    }
}
