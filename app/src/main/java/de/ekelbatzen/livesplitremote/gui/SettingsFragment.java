package de.ekelbatzen.livesplitremote.gui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.network.Network;
import de.ekelbatzen.livesplitremote.network.Poller;

public class SettingsFragment extends PreferenceFragmentCompat {
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private String lastIp;
    private String lastPort;
    private DialogPreference prefIp;
    private DialogPreference prefPort;
    private DialogPreference prefPolling;
    private DialogPreference prefTimeout;
    private DialogPreference prefTimerformat;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        prefIp = findPreference(getString(R.string.settingsIdIp));
        prefPort = findPreference(getString(R.string.settingsIdPort));
        prefPolling = findPreference(getString(R.string.settingsIdPolldelay));
        prefTimeout = findPreference(getString(R.string.settingsIdTimeout));
        prefTimerformat = findPreference(getString(R.string.settingsIdTimerformat));

        updatePreferenceSummaryTexts();
    }

    @Override
    public void onResume() {
        super.onResume();

        lastIp = getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdIp), null);
        lastPort = getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdPort), getString(R.string.defaultPrefPort));

        prefListener = (sharedPreferences, key) -> {
            if (key.equals(getString(R.string.settingsIdIp))) {
                final String input = sharedPreferences.getString(key, "");

                if (input == null || input.isEmpty()) {
                    Toast.makeText(getActivity(), R.string.ipSyntaxError, Toast.LENGTH_SHORT).show();
                    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(prefListener);
                    sharedPreferences.edit().putString(key, lastIp).apply();
                    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(prefListener);
                }

                // Network has to be on non-UI thread
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Network.setIp(InetAddress.getByName(input));
                            lastIp = input;
                        } catch (UnknownHostException ignored) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getActivity(), R.string.ipSyntaxError, Toast.LENGTH_SHORT).show();
                                getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(prefListener);
                                sharedPreferences.edit().putString(key, lastIp).apply();
                                getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(prefListener);
                            });
                        }
                    }
                }.start();
            } else if (key.equals(getString(R.string.settingsIdPort))) {
                final String portInput = sharedPreferences.getString(key, getString(R.string.defaultPrefPort));
                try {
                    int port = Integer.parseInt(portInput);
                    if (port < 1 || port > 65535) {
                        throw new NumberFormatException("Number is out of valid port range");
                    }
                    Network.setPort(port);
                    lastPort = portInput;
                } catch (NumberFormatException ignored) {
                    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(prefListener);
                    sharedPreferences.edit().putString(key, lastPort).apply();
                    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(prefListener);

                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.portSyntaxError, portInput), Toast.LENGTH_SHORT).show());
                }
            } else if (key.equals(getString(R.string.settingsIdPolldelay))) {
                String pollingDelayStr = sharedPreferences.getString(key, getString(R.string.defaultPrefPolling));
                Poller.pollDelayMs = (long) (1000.0f * Float.parseFloat(pollingDelayStr.split(" ")[0]));
            } else if (key.equals(getString(R.string.settingsIdTimeout))) {
                String timeoutStr = sharedPreferences.getString(key, getString(R.string.defaultPrefTimeout));
                Network.setTimeoutMs((int) (1000.0f * Float.parseFloat(timeoutStr.split(" ")[0])));
            } else if (key.equals(getString(R.string.settingsIdDarktheme))) {
                MainActivity.darkTheme = sharedPreferences.getBoolean(key, true);
                MainActivity.themeChanged = true;
                getActivity().recreate();
            } else if (key.equals(getString(R.string.settingsIdTimerformat))) {
                String format = sharedPreferences.getString(key, getString(R.string.defaultPrefTimerformat));
                Timer.setFormatting(format);
            } else if (key.equals(getString(R.string.settingsIdVibrate))) {
                MainActivity.vibrationEnabled = sharedPreferences.getBoolean(key, true);
            }

            updatePreferenceSummaryTexts();
        };

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(prefListener);
    }

    private void updatePreferenceSummaryTexts() {
        prefIp.setSummary(getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdIp), getString(R.string.defaultPrefIp)));
        prefPort.setSummary(getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdPort), getString(R.string.defaultPrefPort)));
        prefPolling.setSummary(getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdPolldelay), getString(R.string.defaultPrefPolling)) + '\n' + getString(R.string.prefPollingSummary));
        prefTimeout.setSummary(getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdTimeout), getString(R.string.defaultPrefTimeout)) + '\n' + getString(R.string.prefTimeoutSummary));
        prefTimerformat.setSummary(getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdTimerformat), getString(R.string.defaultPrefTimerformat)) + '\n' + getString(R.string.prefTimerformatSummary));
    }
}
