package com.anthropicandroid.gzt.activity;

import android.widget.CheckBox;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.anthropicandroid.gzt.R;
import com.anthropicandroid.gzt.modules.SansUserSettingsAdapterComponent;
import com.anthropicandroid.gzt.services.ApplicationPreferences;

import java.text.NumberFormat;

/*
 * Created by Andrew Brin on 5/6/2016.
 */
public class SettingsAdapter {

    @BindingAdapter("checked_settings_value")
    public static void getChecked(
            SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent,
            CheckBox checkbox,
            ApplicationPreferences.Preferences preference) {
        ApplicationPreferences applicationPreferences = sansUserSettingsAdapterComponent
                .getPreferenceStorage();
        // set checkbox
        checkbox.setChecked(0 != applicationPreferences.getPreference(preference));
    }

    @BindingAdapter("settings_value")
    public static void getSettingsValue(
            SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent,
            TextView textView,
            ApplicationPreferences.Preferences preference) {
        ApplicationPreferences preferenceStorage = sansUserSettingsAdapterComponent
                .getPreferenceStorage();
        // set string value
        textView.setText(NumberFormat.getIntegerInstance().format(preferenceStorage.getPreference
                (preference)));
    }

    @BindingAdapter("settings_warning_value")
    public static void getSettingsWarningValue(
            SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent,
            TextView textView,
            ApplicationPreferences.Preferences preference) {
        ApplicationPreferences preferenceStorage = sansUserSettingsAdapterComponent
                .getPreferenceStorage();
        Integer userPreference = preferenceStorage.getPreference(preference);
        if (userPreference == 0)
            textView.setTextColor(textView.getResources().getColor(R.color.color_stop_red));
        else
            textView.setTextColor(textView.getResources().getColor(R.color.color_bright_green));

        textView.setText(NumberFormat.getIntegerInstance().format(userPreference));
    }

    @BindingAdapter("purchased_settings_value")
    public static void getPurchasedSettingsValue(
            SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent,
            TextView button,
            ApplicationPreferences.Preferences preference) {
        ApplicationPreferences preferenceStorage = sansUserSettingsAdapterComponent
                .getPreferenceStorage();
        Integer userPreference = preferenceStorage.getPreference(preference);
        if (userPreference == 0)
            button.setTextColor(button.getResources().getColor(R.color.color_trans_red));
        else
            button.setTextColor(button.getResources().getColor(R.color.color_bright_green));

        button.setText(NumberFormat.getIntegerInstance().format(userPreference));
    }

    @BindingAdapter("settings_health_value")
    public static void getSettingsHealthValue(
            SansUserSettingsAdapterComponent sansUserSettingsAdapterComponent,
            TextView textView,
            ApplicationPreferences.Preferences preference) {
        ApplicationPreferences preferenceStorage = sansUserSettingsAdapterComponent
                .getPreferenceStorage();
        Integer userPreference = preferenceStorage.getPreference(preference);
        if (userPreference == 0) {
            textView.setText(R.string.healthy_status_label);
            textView.setTextColor(textView.getResources().getColor(R.color.color_bright_green));
        } else if (userPreference == 1) {
            textView.setText(R.string.bitten_status_label);
            textView.setTextColor(textView.getResources().getColor(R.color.color_trans_red));
        } else if (userPreference > 1) {
            textView.setText(R.string.zombie_status_label);
            textView.setTextColor(textView.getResources().getColor(R.color.color_pure_black));
        }
    }
}
