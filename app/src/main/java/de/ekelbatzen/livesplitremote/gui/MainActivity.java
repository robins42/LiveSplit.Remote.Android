package de.ekelbatzen.livesplitremote.gui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.model.LiveSplitCommand;
import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;
import de.ekelbatzen.livesplitremote.model.PollUpdateListener;
import de.ekelbatzen.livesplitremote.model.TimerState;
import de.ekelbatzen.livesplitremote.network.Network;
import de.ekelbatzen.livesplitremote.network.Poller;

public class MainActivity extends AppCompatActivity implements PollUpdateListener {
    private static final long VIBRATION_TIME = 100L;
    private static final long OFFLINE_TOAST_COOLDOWN_MS = 10000L;
    private static final String TAG = MainActivity.class.getName();

    public static boolean darkTheme;
    public static boolean themeChanged;
    public static boolean vibrationEnabled;

    private TimerState timerState;
    private TextView info;
    private Button startSplitButton;
    private Button undoButton;
    private Button skipButton;
    private Button pauseButton;
    private ProgressBar networkIndicator;
    private Timer timer;
    private Poller poller;
    private NetworkResponseListener defaultCommandListener;
    private long timestampLastOfflineToast;
    private Toast offlineToast;
    private boolean isActive;
    private boolean cmdRequestActive;
    private boolean pollActive;
    private Vibrator vibrator;
    private boolean foundSavedIp;
    private boolean foundSavedPort;
    private boolean hasStartedBefore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        readPreferences();
        setTheme(darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setLogo(R.mipmap.ic_launcher);
            getSupportActionBar().setTitle("  " + getString(R.string.app_name));
            getSupportActionBar().setDisplayUseLogoEnabled(true);
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        timerState = TimerState.ERROR;

        info = (TextView) findViewById(R.id.text_warnings);
        startSplitButton = (Button) findViewById(R.id.startSplitButton);
        undoButton = (Button) findViewById(R.id.undoButton);
        skipButton = (Button) findViewById(R.id.skipButton);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        timer = (Timer) findViewById(R.id.timer);
        networkIndicator = (ProgressBar) findViewById(R.id.networkIndicator);

        timer.setTextColor(getResources().getColor(darkTheme ? R.color.darkTimerColor : R.color.lightTimerColor));

        timer.setActivity(this);
        updateGuiToTimerstate();

        defaultCommandListener = new NetworkResponseListener() {
            @Override
            public void onResponse(String response) {
                cmdRequestActive = false;
                if (!pollActive) {
                    runOnUiThread(() -> networkIndicator.setVisibility(View.INVISIBLE));
                }
                if (poller != null) {
                    poller.instantPoll();
                }
            }

            @Override
            public void onError() {
                cmdRequestActive = false;
                if (!pollActive) {
                    runOnUiThread(() -> networkIndicator.setVisibility(View.INVISIBLE));
                }
                onServerWentOffline();
            }
        };

        if(!hasStartedBefore){
            //It is the first app start
            GuideDialog.askForGuide(this);
        }
    }

    private void readPreferences() {
        foundSavedIp = false;
        foundSavedPort = false;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String savedIP = prefs.getString(getString(R.string.settingsIdIp), null);
        if (savedIP != null) {
            // Network has to be on non-UI thread
            new Thread() {
                @Override
                public void run() {
                    try {
                        Network.setIp(InetAddress.getByName(savedIP));
                        foundSavedIp = true;
                        if(foundSavedPort){
                            runOnUiThread(() -> info.setText(getString(R.string.displayIpAppstart, Network.getIp().getHostAddress(), Network.getPort())));
                        }
                    } catch (UnknownHostException ignored) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.ipParseError, Toast.LENGTH_SHORT).show());
                    }
                }
            }.start();
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    Network.setPort(Integer.parseInt(prefs.getString(getString(R.string.settingsIdPort), getString(R.string.defaultPrefPort))));
                    foundSavedPort = true;
                    if(foundSavedIp){
                        runOnUiThread(() -> info.setText(getString(R.string.displayIpAppstart, Network.getIp().getHostAddress(), Network.getPort())));
                    }
                } catch (Exception ignored) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.portParseError, Toast.LENGTH_SHORT).show());
                }
            }
        }.start();

        try {
            String pollingDelayStr = prefs.getString(getString(R.string.settingsIdPolldelay), getString(R.string.defaultPrefPolling));
            Poller.pollDelayMs = (long) (1000.0f * Float.parseFloat(pollingDelayStr.split(" ")[0]));
        } catch (Exception ignored) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.pollingParseError, Toast.LENGTH_SHORT).show());
        }

        try {
            String networkTimeoutStr = prefs.getString(getString(R.string.settingsIdTimeout), getString(R.string.defaultPrefTimeout));
            Network.setTimeoutMs((int) (1000.0f * Float.parseFloat(networkTimeoutStr.split(" ")[0])));
        } catch (Exception ignored) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.timeoutParseError, Toast.LENGTH_SHORT).show());
        }

        try {
            darkTheme = prefs.getBoolean(getString(R.string.settingsIdDarktheme), true);
        } catch (Exception ignored) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.themeParseError, Toast.LENGTH_SHORT).show());
        }

        try {
            String timerFormat = prefs.getString(getString(R.string.settingsIdTimerformat), getString(R.string.defaultPrefTimerformat));
            Timer.setFormatting(timerFormat);
        } catch (Exception ignored) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.timerFormatParseError, Toast.LENGTH_SHORT).show());
        }

        try {
            vibrationEnabled = prefs.getBoolean(getString(R.string.settingsIdVibrate), true);
        } catch (Exception ignored) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.vibrationParseError, Toast.LENGTH_SHORT).show());
        }

        try {
            hasStartedBefore = prefs.getBoolean(getString(R.string.settingsIdFirstStart), false);

            // User may be new or patched from previous app version
            if(!hasStartedBefore){
                // Check if user has any settings, if he does, it is not the first start
                if(prefs.contains(getString(R.string.settingsIdDarktheme))) hasStartedBefore = true;
                if(!hasStartedBefore && prefs.contains(getString(R.string.settingsIdVibrate))) hasStartedBefore = true;
                if(!hasStartedBefore && prefs.contains(getString(R.string.settingsIdIp))) hasStartedBefore = true;
                if(!hasStartedBefore && prefs.contains(getString(R.string.settingsIdPolldelay))) hasStartedBefore = true;
                if(!hasStartedBefore && prefs.contains(getString(R.string.settingsIdPort))) hasStartedBefore = true;
                if(!hasStartedBefore && prefs.contains(getString(R.string.settingsIdTimeout))) hasStartedBefore = true;
                if(!hasStartedBefore && prefs.contains(getString(R.string.settingsIdTimerformat))) hasStartedBefore = true;
            }

            prefs.edit().putBoolean(getString(R.string.settingsIdFirstStart), true).apply();
        } catch (Exception ignored) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.firstStartParseError, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(3).setEnabled(Network.hasIp() && timerState != TimerState.ERROR);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_guide:
                new GuideDialog(this);
                return true;
            case R.id.menu_info:
                showInfo();
                return true;
            case R.id.menu_resettimer:
                vibrate();
                sendCommand(LiveSplitCommand.RESET);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;

        if (poller != null) {
            poller.stopPolling();
            poller = null;
        }
        poller = new Poller(this);
        poller.startPolling();

        timer.onTimerFormatChanged();

        new Thread() {
            @Override
            public void run() {
                cmdRequestActive = true;
                runOnUiThread(() -> networkIndicator.setVisibility(View.VISIBLE));
                try {
                    Network.openConnection();
                } catch (IOException e) {
                    Log.w(TAG, getString(R.string.errorSocketOpen), e);
                }
                cmdRequestActive = false;
                if (!pollActive) {
                    runOnUiThread(() -> networkIndicator.setVisibility(View.INVISIBLE));
                }
            }
        }.start();

        updateGuiToTimerstate();

        if (themeChanged) {
            themeChanged = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                recreate();
            } else {
                // recreate not available below android 3.0, doing workaround
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;

        if (timer != null) {
            timer.stop();
        }
        if (poller != null) {
            poller.stopPolling();
            poller = null;
        }

        new Thread(){
            @Override
            public void run() {
                Network.closeConnection();
            }
        }.start();
    }

    private void sendCommand(LiveSplitCommand cmd) {
        cmdRequestActive = true;
        networkIndicator.setVisibility(View.VISIBLE);
        new Network(defaultCommandListener).execute(cmd.toString(), Boolean.toString(false));
    }

    @Override
    public void onStateChanged(TimerState newState) {
        timerState = newState;

        if (newState == TimerState.RUNNING) {
            timer.start();
        } else {
            timer.stop();
        }

        updateGuiToTimerstate();
    }

    @Override
    public void onTimeSynchronized(String lsTime) {
        if (!"0.00".equals(lsTime)) {
            timer.setMs(lsTime);
        } else {
            // Bug on LiveSplit server when using game time comparison, ignore synchronization until fixed
            runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.gameTimeBug, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onServerWentOffline() {
        info.setText(getString(R.string.ipNotReachedInfo, Network.getIp().getHostAddress(), Network.getPort()));
        offlineToast = Toast.makeText(this, R.string.ipNotReached, Toast.LENGTH_LONG);
        offlineToast.show();
        timerState = TimerState.ERROR;
        timer.stop();
        updateGuiToTimerstate();
    }

    @Override
    public void onServerWentOnline(TimerState currentState) {
        info.setText(getString(R.string.displayIpConnected, Network.getIp().getHostAddress(), Network.getPort()));
        timerState = currentState;
        if (offlineToast != null) {
            offlineToast.cancel();
        }
        updateGuiToTimerstate();
        if (currentState == TimerState.RUNNING) {
            timer.start();
        }
    }

    @Override
    public void onPollStart() {
        pollActive = true;
        runOnUiThread(() -> networkIndicator.setVisibility(View.VISIBLE));
    }

    @Override
    public void onPollEnd() {
        runOnUiThread(() -> {
            if (Network.hasIp()) {
                if (timerState == TimerState.ERROR) {
                    info.setText(getString(R.string.ipNotReachedInfo, Network.getIp().getHostAddress(), Network.getPort()));
                    if (System.currentTimeMillis() - timestampLastOfflineToast > OFFLINE_TOAST_COOLDOWN_MS && isActive) {
                        offlineToast = Toast.makeText(MainActivity.this, R.string.ipNotReached, Toast.LENGTH_LONG);
                        offlineToast.show();
                        timestampLastOfflineToast = System.currentTimeMillis();
                    }
                } else {
                    info.setText(getString(R.string.displayIpConnected, Network.getIp().getHostAddress(), Network.getPort()));
                }
            } else {
                info.setText(R.string.serverIpNotSet);
            }

            pollActive = false;
            if (!cmdRequestActive) {
                networkIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onProblem(final String msg) {
        runOnUiThread(() -> {
            if (isActive) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateGuiToTimerstate() {
        runOnUiThread(() -> {
            switch (timerState) {
                case NOT_RUNNING:
                    startSplitButton.setEnabled(true);
                    undoButton.setEnabled(false);
                    skipButton.setEnabled(false);
                    pauseButton.setEnabled(false);
                    startSplitButton.setText(R.string.startTimer);
                    break;
                case RUNNING:
                    startSplitButton.setEnabled(true);
                    undoButton.setEnabled(true);
                    skipButton.setEnabled(true);
                    pauseButton.setEnabled(true);
                    startSplitButton.setText(R.string.splitText);
                    break;
                case ENDED:
                    startSplitButton.setEnabled(false);
                    undoButton.setEnabled(true);
                    skipButton.setEnabled(false);
                    pauseButton.setEnabled(false);
                    startSplitButton.setText(R.string.timeFinishedText);
                    break;
                case PAUSED:
                    startSplitButton.setEnabled(true);
                    undoButton.setEnabled(true);
                    skipButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                    startSplitButton.setText(R.string.resumeText);
                    break;
                case ERROR:
                    startSplitButton.setEnabled(false);
                    undoButton.setEnabled(false);
                    skipButton.setEnabled(false);
                    pauseButton.setEnabled(false);
                    break;
            }

            invalidateOptionsMenu();
        });
    }

    private void showInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.infosTitle);
        builder.setMessage(R.string.infosMsg);
        builder.setCancelable(true);

        builder.setPositiveButton(R.string.infosCancel, new MainActivity.InfoCloseListener());

        AlertDialog ad = builder.create();
        ad.show();

        // Make links clickable
        TextView msg = (TextView) ad.findViewById(android.R.id.message);
        if (msg != null) {
            msg.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void vibrate() {
        if(!vibrationEnabled) return;

        if (vibrator == null) {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        // Below Android 3.0 getSystemService returns null if no vibrator is available
        if (vibrator != null) {
            // hasVibrator check is only available since Android 3.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && vibrator.hasVibrator()) {
                vibrator.vibrate(VIBRATION_TIME);
            }
        }
    }

    public void StartSplitClicked(View view) {
        vibrate();
        if (!Network.hasIp()) {
            Toast.makeText(MainActivity.this, R.string.serverIpNotSet, Toast.LENGTH_SHORT).show();
            return;
        }

        if (timerState == TimerState.NOT_RUNNING) {
            sendCommand(LiveSplitCommand.START);
        } else if (timerState == TimerState.PAUSED) {
            sendCommand(LiveSplitCommand.RESUME);
        } else if (timerState == TimerState.RUNNING) {
            sendCommand(LiveSplitCommand.SPLIT);
        }
    }

    public void UndoClicked(View view) {
        vibrate();
        if (!Network.hasIp()) {
            Toast.makeText(MainActivity.this, R.string.serverIpNotSet, Toast.LENGTH_SHORT).show();
            return;
        }

        sendCommand(LiveSplitCommand.UNDO);
    }

    public void SkipClicked(View view) {
        vibrate();
        if (!Network.hasIp()) {
            Toast.makeText(MainActivity.this, R.string.serverIpNotSet, Toast.LENGTH_SHORT).show();
            return;
        }

        sendCommand(LiveSplitCommand.SKIP);
    }

    public void PauseClicked(View view) {
        vibrate();
        if (!Network.hasIp()) {
            Toast.makeText(MainActivity.this, R.string.serverIpNotSet, Toast.LENGTH_SHORT).show();
            return;
        }

        sendCommand(LiveSplitCommand.PAUSE);
    }

    private static class InfoCloseListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }
}
