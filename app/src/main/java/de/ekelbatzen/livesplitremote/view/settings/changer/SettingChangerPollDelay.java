package de.ekelbatzen.livesplitremote.view.settings.changer;

import android.content.SharedPreferences;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.model.SettingsFragment;
import de.ekelbatzen.livesplitremote.controller.network.Poller;

public class SettingChangerPollDelay extends SettingChanger {
    private final SettingsFragment fragment;

    public SettingChangerPollDelay(SettingsFragment fragment) {
        super(fragment.getString(R.string.settingsIdPolldelay));
        this.fragment = fragment;
    }

    @Override
    public void changeSetting(SharedPreferences sharedPreferences, String key) {
        String defaultPollDelay = fragment.getString(R.string.defaultPrefPolling);
        String pollingDelayStr = getNonNullPreferenceString(sharedPreferences, key, defaultPollDelay);
        String pollingDelaySeconds = parseSecondsFromString(pollingDelayStr);
        Poller.pollDelayMs = convertSecondsToMs(pollingDelaySeconds);
    }
}
