package de.ekelbatzen.livesplitremote.gui.settings;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.gui.MainActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(MainActivity.darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_fragment, new SettingsFragmentImpl())
                .commit();
    }
}
