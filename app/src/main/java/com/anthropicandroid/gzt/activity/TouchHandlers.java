package com.anthropicandroid.gzt.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.anthropicandroid.R;
import com.anthropicandroid.gzt.ZombieTrackerApplication;
import com.anthropicandroid.gzt.services.ApplicationPreferences;

/*
 * Created by Andrew Brin on 4/6/2016.
 */
final public class TouchHandlers {


    public TouchHandlers() {
    }

    public void muteNotifications(View view){
        ApplicationPreferences preferenceStorage = ((ZombieTrackerApplication) ((Activity) view.getContext()).getApplication())
                .getApplicationComponent().getPreferenceStorage();
        CheckBox checkBox = (CheckBox) view;
        ViewDataBinding binding = DataBindingUtil.findBinding(view);

        if(checkBox.isChecked()) //  user is muting notifications (view was just checked)
        {
            boolean checked = preferenceStorage.setPreference(ApplicationPreferences.Preferences.MUTE_NOTIFICATIONS, 1);
            checkBox.setChecked(checked);
        }
        else //  user is unmuting notifications, success in unmuting occassions a removal of checkmark
        {
            boolean checked = !preferenceStorage.setPreference(ApplicationPreferences.Preferences.MUTE_NOTIFICATIONS, 0);
            checkBox.setChecked(checked);
        }
    }

    public void onGoClickedWithEmail(View view) {

    }

    static public void onGoClickedNoEmail(View view) {
        Toast.makeText(view.getContext(), R.string.no_email_toast, Toast.LENGTH_SHORT).show();
    }

    public void onPlayClicked(View view) {

    }

    public void onProgressBarClicked(View view) {

    }
}
