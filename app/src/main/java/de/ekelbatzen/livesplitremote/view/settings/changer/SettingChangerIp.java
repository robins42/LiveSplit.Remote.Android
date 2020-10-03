package de.ekelbatzen.livesplitremote.view.settings.changer;

import android.content.SharedPreferences;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.model.SettingsFragment;
import de.ekelbatzen.livesplitremote.controller.network.Network;

public class SettingChangerIp extends SettingChanger {
    private final SettingsFragment fragment;
    private String lastIp;

    public SettingChangerIp(SettingsFragment fragment) {
        super(fragment.getString(R.string.settingsIdIp));
        this.fragment = fragment;
    }

    public void setLastIp(String ip) {
        lastIp = ip;
    }

    @Override
    public void changeSetting(SharedPreferences sharedPreferences, String key) {
        final String input = sharedPreferences.getString(key, "");

        if (input == null || input.isEmpty()) {
            fragment.showToast(R.string.ipSyntaxError);
            fragment.changeSetting(() -> sharedPreferences.edit().putString(key, lastIp).apply());
        } else {
            Network.runInThread(() -> parseAndSetIp(sharedPreferences, key, input));
        }
    }

    private void parseAndSetIp(SharedPreferences sharedPreferences, String key, String input) {
        try {
            Network.setIp(InetAddress.getByName(input));
            lastIp = input;
        } catch (UnknownHostException ignored) {
            fragment.runOnUiThread(() -> {
                fragment.showToast(R.string.ipSyntaxError);
                fragment.changeSetting(() -> sharedPreferences.edit().putString(key, lastIp).apply());
            });
        }
    }
}
