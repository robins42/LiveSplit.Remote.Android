package de.ekelbatzen.livesplitremote;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SettingsActivity extends PreferenceActivity {
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private String lastIp;
    private String lastPort;
    private DialogPreference prefIp;
    private DialogPreference prefPort;
    private DialogPreference prefPolling;
    private DialogPreference prefTimeout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        addPreferencesFromResource(R.xml.preferences);
        prefIp = (DialogPreference) findPreference(getString(R.string.settingsIdIp));
        prefPort = (DialogPreference) findPreference(getString(R.string.settingsIdPort));
        prefPolling = (DialogPreference) findPreference(getString(R.string.settingsIdPolldelay));
        prefTimeout = (DialogPreference) findPreference(getString(R.string.settingsIdTimeout));

        updatePreferenceSummaryTexts();
    }

    @Override
    protected void onResume() {
        super.onResume();

        lastIp = getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdIp), null);
        lastPort = getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdPort), "16834");

        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
                if (key.equals(getString(R.string.settingsIdIp))) {
                    final String input = sharedPreferences.getString(key, "");
                    Log.v("Test", "ip input: " + input);

                    if (input.length() == 0) {
                        Toast.makeText(SettingsActivity.this, R.string.ipSyntaxError, Toast.LENGTH_SHORT).show();
                        Log.v("Test", "setting ip to " + lastIp);
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
                                Log.v("Test", "ip has been updated to " + Network.getIp().getHostAddress());
                                lastIp = input;
                            } catch (UnknownHostException ignored) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(SettingsActivity.this, R.string.ipSyntaxError, Toast.LENGTH_SHORT).show();
                                        Log.v("Test", "setting ip to " + lastIp);
                                        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(prefListener);
                                        sharedPreferences.edit().putString(key, lastIp).apply();
                                        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(prefListener);
                                    }
                                });
                            }
                        }
                    }.start();
                } else if (key.equals(getString(R.string.settingsIdPort))) {
                    final String portInput = sharedPreferences.getString(key, "16834");
                    try{
                        int port = Integer.parseInt(portInput);
                        if(port < 1 || port > 65535){
                            throw new NumberFormatException("Number is out of valid port range");
                        }
                        Network.setPort(port);
                        lastPort = portInput;
                    } catch (NumberFormatException e){
                        Log.v("Test", "setting port to " + lastPort);
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
                    String pollingDelayStr = sharedPreferences.getString(key, "2 s");
                    Poller.pollDelayMs = (long) (1000.0f * Float.parseFloat(pollingDelayStr.split(" ")[0]));
                } else if (key.equals(getString(R.string.settingsIdTimeout))) {
                    String timeoutStr = sharedPreferences.getString(key, "2 s");
                    Network.setTimeoutMs((int) (1000.0f * Float.parseFloat(timeoutStr.split(" ")[0])));
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
        prefIp.setSummary(getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdIp), "Not set yet"));
        prefPort.setSummary(getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdPort), "16834"));
        prefPolling.setSummary(getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdPolldelay), "2 s") + '\n' + getString(R.string.prefPollingSummary));
        prefTimeout.setSummary(getPreferenceScreen().getSharedPreferences().getString(getString(R.string.settingsIdTimeout), "3 s") + '\n' + getString(R.string.prefTimeoutSummary));
    }
}
