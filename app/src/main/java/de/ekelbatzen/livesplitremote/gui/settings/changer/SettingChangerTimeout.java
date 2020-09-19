package de.ekelbatzen.livesplitremote.gui.settings.changer;

import android.content.SharedPreferences;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.model.SettingsFragment;
import de.ekelbatzen.livesplitremote.network.Network;

public class SettingChangerTimeout extends SettingChanger {
    private final SettingsFragment fragment;

    public SettingChangerTimeout(SettingsFragment fragment) {
        super(fragment.getString(R.string.settingsIdTimeout));
        this.fragment = fragment;
    }

    @Override
    public void changeSetting(SharedPreferences sharedPreferences, String key) {
        String defaultTimeout = fragment.getString(R.string.defaultPrefTimeout);
        String timeoutStr = getNonNullPreferenceString(sharedPreferences, key, defaultTimeout);
        String timeoutSeconds = parseSecondsFromString(timeoutStr);
        Network.setTimeoutMs((int) convertSecondsToMs(timeoutSeconds));
    }
}
