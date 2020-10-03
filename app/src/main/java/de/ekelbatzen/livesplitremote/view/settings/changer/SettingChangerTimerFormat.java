package de.ekelbatzen.livesplitremote.view.settings.changer;

import android.content.SharedPreferences;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.view.Timer;
import de.ekelbatzen.livesplitremote.model.SettingsFragment;

public class SettingChangerTimerFormat extends SettingChanger {
    private final SettingsFragment fragment;

    public SettingChangerTimerFormat(SettingsFragment fragment) {
        super(fragment.getString(R.string.settingsIdTimerformat));
        this.fragment = fragment;
    }

    @Override
    public void changeSetting(SharedPreferences sharedPreferences, String key) {
        String defaultTimerFormat = fragment.getString(R.string.defaultPrefTimerformat);
        String format = getNonNullPreferenceString(sharedPreferences, key, defaultTimerFormat);
        Timer.setFormatting(format);
    }
}
