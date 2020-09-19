package de.ekelbatzen.livesplitremote.model;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.gui.settings.changer.SettingChanger;
import de.ekelbatzen.livesplitremote.gui.settings.changer.SettingChangerIp;
import de.ekelbatzen.livesplitremote.gui.settings.changer.SettingChangerPollDelay;
import de.ekelbatzen.livesplitremote.gui.settings.changer.SettingChangerPort;
import de.ekelbatzen.livesplitremote.gui.settings.changer.SettingChangerTheme;
import de.ekelbatzen.livesplitremote.gui.settings.changer.SettingChangerTimeout;
import de.ekelbatzen.livesplitremote.gui.settings.changer.SettingChangerTimerFormat;
import de.ekelbatzen.livesplitremote.gui.settings.changer.SettingChangerVibration;

public class SettingsChanger {
    private final SettingsFragment fragment;
    private final SettingChangerIp settingChangerIp;
    private final SettingChangerPort settingChangerPort;
    private final List<SettingChanger> settingChangers;
    private String lastPort;

    public SettingsChanger(SettingsFragment fragment) {
        this.fragment = fragment;
        settingChangerIp = new SettingChangerIp(fragment);
        settingChangerPort = new SettingChangerPort(fragment);
        settingChangers = new ArrayList<>();
        settingChangers.add(settingChangerIp);
        settingChangers.add(settingChangerPort);
        settingChangers.add(new SettingChangerPollDelay(fragment));
        settingChangers.add(new SettingChangerTheme(fragment));
        settingChangers.add(new SettingChangerTimeout(fragment));
        settingChangers.add(new SettingChangerTimerFormat(fragment));
        settingChangers.add(new SettingChangerVibration(fragment));
    }

    public void onPropertyChanged(SharedPreferences sharedPreferences, String key) {
        boolean wasKeyFound = false;
        for (SettingChanger settingChanger : settingChangers) {
            if (settingChanger.isPreferenceKey(key)) {
                settingChanger.changeSetting(sharedPreferences, key);
                wasKeyFound = true;
                break;
            }
        }
        if (!wasKeyFound) {
            Log.w(getClass().getName(), "Invalid preference key: " + key);
        }
    }


    public void readLastIpAndPort() {
        String prefIp = fragment.getPreferenceString(fragment.getString(R.string.settingsIdIp), null);
        String prefPort = fragment.getPreferenceString(
                fragment.getString(R.string.settingsIdPort),
                fragment.getString(R.string.defaultPrefPort));
        settingChangerIp.setLastIp(prefIp);
        settingChangerPort.setLastPort(prefPort);
    }
}
