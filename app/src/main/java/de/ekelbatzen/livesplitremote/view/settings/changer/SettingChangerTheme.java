package de.ekelbatzen.livesplitremote.view.settings.changer;

import android.content.SharedPreferences;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.view.MainActivity;
import de.ekelbatzen.livesplitremote.model.SettingsFragment;

public class SettingChangerTheme extends SettingChanger {
    private final SettingsFragment fragment;

    public SettingChangerTheme(SettingsFragment fragment) {
        super(fragment.getString(R.string.settingsIdDarktheme));
        this.fragment = fragment;
    }

    @Override
    public void changeSetting(SharedPreferences sharedPreferences, String key) {
        MainActivity.darkTheme = sharedPreferences.getBoolean(key, true);
        MainActivity.themeChanged = true;
        fragment.recreate();
    }
}
