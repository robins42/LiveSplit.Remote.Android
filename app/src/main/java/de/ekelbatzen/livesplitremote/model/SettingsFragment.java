package de.ekelbatzen.livesplitremote.model;

public interface SettingsFragment {
    void changeSetting(Runnable changeAction);
    String getString(int id);
    void showToast(int msgId, Object... arguments);
    void runOnUiThread(Runnable action);
    void recreate();
    String getPreferenceString(String key, String defValue);
}
