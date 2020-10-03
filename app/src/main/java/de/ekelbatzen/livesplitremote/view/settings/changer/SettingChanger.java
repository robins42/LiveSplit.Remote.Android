package de.ekelbatzen.livesplitremote.view.settings.changer;

import android.content.SharedPreferences;

public abstract class SettingChanger {
    private final String key;

    public SettingChanger(String key) {
        this.key = key;
    }

    public abstract void changeSetting(SharedPreferences sharedPreferences, String key);

    public boolean isPreferenceKey(String relevantKey) {
        return key.equals(relevantKey);
    }

    String parseSecondsFromString(String timeString) {
        return timeString.split(" ")[0];
    }

    long convertSecondsToMs(String seconds) {
        return (long) (1000.0f * Float.parseFloat(seconds));
    }

    static String getNonNullPreferenceString(SharedPreferences sharedPreferences, String key, String defaultString) {
        String preference = sharedPreferences.getString(key, defaultString);
        if (preference == null) {
            preference = defaultString;
        }
        return preference;
    }
}
