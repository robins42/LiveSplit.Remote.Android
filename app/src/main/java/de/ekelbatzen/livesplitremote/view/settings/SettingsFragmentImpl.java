package de.ekelbatzen.livesplitremote.view.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceFragmentCompat;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.controller.SettingsChanger;
import de.ekelbatzen.livesplitremote.model.SettingsFragment;

public class SettingsFragmentImpl extends PreferenceFragmentCompat implements SettingsFragment {
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private SettingsChanger settingsChanger;

    private DialogPreference prefIp;
    private DialogPreference prefPort;
    private DialogPreference prefPolling;
    private DialogPreference prefTimeout;
    private DialogPreference prefTimerformat;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        readPreferences();
        updatePreferenceSummaryTexts();
        settingsChanger = new SettingsChanger(this);
    }

    private void readPreferences() {
        prefIp = findPreference(getString(R.string.settingsIdIp));
        prefPort = findPreference(getString(R.string.settingsIdPort));
        prefPolling = findPreference(getString(R.string.settingsIdPolldelay));
        prefTimeout = findPreference(getString(R.string.settingsIdTimeout));
        prefTimerformat = findPreference(getString(R.string.settingsIdTimerformat));
    }

    private void updatePreferenceSummaryTexts() {
        prefIp.setSummary(
                getPreferenceString(getString(R.string.settingsIdIp), getString(R.string.defaultPrefIp)));
        prefPort.setSummary(
                getPreferenceString(getString(R.string.settingsIdPort), getString(R.string.defaultPrefPort)));
        prefPolling.setSummary(
                getPreferenceString(getString(R.string.settingsIdPolldelay), getString(R.string.defaultPrefPolling))
                        + '\n' + getString(R.string.prefPollingSummary));
        prefTimeout.setSummary(
                getPreferenceString(getString(R.string.settingsIdTimeout), getString(R.string.defaultPrefTimeout))
                        + '\n' + getString(R.string.prefTimeoutSummary));
        prefTimerformat.setSummary(
                getPreferenceString(getString(R.string.settingsIdTimerformat), getString(R.string.defaultPrefTimerformat))
                        + '\n' + getString(R.string.prefTimerformatSummary));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterChangeListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerChangeListener();
        settingsChanger.readLastIpAndPort();
    }

    private void registerChangeListener() {
        prefListener = (sharedPreferences, key) -> {
            settingsChanger.onPropertyChanged(sharedPreferences, key);
            updatePreferenceSummaryTexts();
        };
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(prefListener);
    }

    private void unregisterChangeListener() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    public void changeSetting(Runnable changeAction) {
        unregisterChangeListener();
        changeAction.run();
        registerChangeListener();
    }

    @Override
    public void showToast(int msgId, Object... arguments) {
        Toast.makeText(getContext(), getString(msgId, arguments), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void runOnUiThread(Runnable action) {
        requireActivity().runOnUiThread(action);
    }

    @Override
    public void recreate() {
        requireActivity().recreate();
    }

    @Override
    public String getPreferenceString(String key, String defValue) {
        return getPreferenceScreen().getSharedPreferences().getString(key, defValue);
    }
}
