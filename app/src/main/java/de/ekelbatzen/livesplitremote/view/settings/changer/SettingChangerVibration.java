package de.ekelbatzen.livesplitremote.view.settings.changer;

import android.content.SharedPreferences;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.view.MainActivity;
import de.ekelbatzen.livesplitremote.model.SettingsFragment;

public class SettingChangerVibration extends SettingChanger {
    public SettingChangerVibration(SettingsFragment fragment) {
        super(fragment.getString(R.string.settingsIdVibrate));
    }

    @Override
    public void changeSetting(SharedPreferences sharedPreferences, String key) {
        MainActivity.vibrationEnabled = sharedPreferences.getBoolean(key, true);
    }
}
