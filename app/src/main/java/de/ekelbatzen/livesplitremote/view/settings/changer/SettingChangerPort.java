package de.ekelbatzen.livesplitremote.view.settings.changer;

import android.content.SharedPreferences;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.model.SettingsFragment;
import de.ekelbatzen.livesplitremote.controller.network.Network;

public class SettingChangerPort extends SettingChanger {
    private final SettingsFragment fragment;
    private String lastPort;

    public SettingChangerPort(SettingsFragment fragment) {
        super(fragment.getString(R.string.settingsIdPort));
        this.fragment = fragment;
    }

    public void setLastPort(String port) {
        lastPort = port;
    }

    @Override
    public void changeSetting(SharedPreferences sharedPreferences, String key) {
        final String portInput = sharedPreferences.getString(key, fragment.getString(R.string.defaultPrefPort));
        if (portInput == null) {
            throw new NumberFormatException("Port is null");
        }
        try {
            parseAndSetPort(portInput);
        } catch (NumberFormatException ignored) {
            fragment.changeSetting(() -> sharedPreferences.edit().putString(key, lastPort).apply());
            fragment.runOnUiThread(() -> fragment.showToast(R.string.portSyntaxError, portInput));
        }
    }

    private void parseAndSetPort(String portInput) {
        int port = Integer.parseInt(portInput);
        if (port < 1 || port > 65535) {
            throw new NumberFormatException("Number is out of valid port range");
        }
        Network.setPort(port);
        lastPort = portInput;
    }
}
