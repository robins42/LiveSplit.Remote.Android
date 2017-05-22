package de.ekelbatzen.livesplitremote.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.network.Network;
import de.ekelbatzen.livesplitremote.network.Poller;

public class SettingsActivity extends PreferenceActivity {
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private String lastIp;
    private String lastPort;
    private DialogPreference prefIp;
    private DialogPreference prefPort;
    private DialogPreference prefPolling;
    private DialogPreference prefTimeout;
    private DialogPreference prefTimerformat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(MainActivity.darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);
        super.onCreate(null);

        addPreferencesFromResource(R.xml.preferences);
        prefIp = (DialogPreference) findPreference(getString(R.string.settingsIdIp));
        prefPort = (DialogPreference) findPreference(getString(R.string.settingsIdPort));
        prefPolling = (DialogPreference) findPreference(getString(R.string.settingsIdPolldelay));
        prefTimeout = (DialogPreference) findPreference(getString(R.string.settingsIdTimeout));
        prefTimerformat = (DialogPreference) findPreference(getString(R.string.settingsIdTimerformat));

        updatePreferenceSummaryTexts();
    }

    @Override
    protected void onResume() {
        super.onResume();

        lastIp = getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdIp), null);
        lastPort = getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdPort), getString(R.string.defaultPrefPort));

        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
                if (key.equals(getString(R.string.settingsIdIp))) {
                    final String input = sharedPreferences.getString(key, "");

                    if (input.isEmpty()) {
                        Toast.makeText(SettingsActivity.this, R.string.ipSyntaxError, Toast.LENGTH_SHORT).show();
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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(SettingsActivity.this, R.string.ipSyntaxError, Toast.LENGTH_SHORT).show();
                                        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(prefListener);
                                        sharedPreferences.edit().putString(key, lastIp).apply();
                                        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(prefListener);
                                    }
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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SettingsActivity.this, getString(R.string.portSyntaxError, portInput), Toast.LENGTH_SHORT).show();
                            }
                        });
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        recreate();
                    } else {
                        // recreate not available below android 3.0, doing workaround
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                } else if (key.equals(getString(R.string.settingsIdTimerformat))) {
                    String format = sharedPreferences.getString(key, getString(R.string.defaultPrefTimerformat));
                    Timer.setFormatting(format);
                } else if (key.equals(getString(R.string.settingsIdVibrate))) {
                    MainActivity.vibrationEnabled = sharedPreferences.getBoolean(key, true);
                }

                updatePreferenceSummaryTexts();
            }
        };

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    protected void onPause() {
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
