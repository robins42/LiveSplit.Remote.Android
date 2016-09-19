package de.ekelbatzen.livesplitremote;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.ekelbatzen.livesplitremote.model.LiveSplitCommand;
import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;
import de.ekelbatzen.livesplitremote.model.PollUpdateListener;
import de.ekelbatzen.livesplitremote.model.TimerState;

public class MainActivity extends AppCompatActivity implements PollUpdateListener {
    private static int port = 16834; //I know, hardcoding is bad, will be implemented when I have time
    private String tempIpFromDialog;
    private InetAddress ip;
    private TimerState timerState;
    private TextView info;
    private Button startSplitButton;
    private Button undoButton;
    private Button skipButton;
    private Button pauseButton;
    private Timer timer;
    private Poller poller;
    private NetworkResponseListener defaultCommandListener;
    private long timestampLastOfflineToast;
    private static final long OFFLINE_TOAST_COOLDOWN_MS = 10000L;
    private Toast offlineToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Print any unexpected exceptions to a toast before crashing
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, final Throwable e) {
                e.printStackTrace();

                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), Log.getStackTraceString(e), Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                }.start();

                // Wait for the toast to disappear
                try {
                    Thread.sleep(4000L);
                } catch (InterruptedException ignored) {
                    // Ignored
                }

                finish();
                System.exit(0);
            }
        });

        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setLogo(R.mipmap.ic_launcher);
            getSupportActionBar().setTitle("  " + getString(R.string.app_name));
            getSupportActionBar().setDisplayUseLogoEnabled(true);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        timerState = TimerState.ERROR;

        info = (TextView) findViewById(R.id.text_warnings);
        startSplitButton = (Button) findViewById(R.id.startSplitButton);
        undoButton = (Button) findViewById(R.id.undoButton);
        skipButton = (Button) findViewById(R.id.skipButton);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        timer = (Timer) findViewById(R.id.timer);

        updateGuiToTimerstate();

        startSplitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ip == null) {
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
                });
            }
        });

        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ip == null) {
                            Toast.makeText(MainActivity.this, R.string.serverIpNotSet, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        sendCommand(LiveSplitCommand.UNDO);
                    }
                });
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ip == null) {
                            Toast.makeText(MainActivity.this, R.string.serverIpNotSet, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        sendCommand(LiveSplitCommand.SKIP);
                    }
                });
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ip == null) {
                            Toast.makeText(MainActivity.this, R.string.serverIpNotSet, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        sendCommand(LiveSplitCommand.PAUSE);
                    }
                });
            }
        });

        String savedIP = getSharedPreferences(getString(R.string.pref_id), Activity.MODE_PRIVATE).getString(getString(R.string.settingsIdIp), null);
        if (savedIP != null) {
            try {
                ip = InetAddress.getByName(savedIP);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, R.string.ipParseError, Toast.LENGTH_SHORT).show();
            }
        }

        defaultCommandListener = new NetworkResponseListener() {
            @Override
            public void onResponse(String response) {
                poller.instantPoll();
            }

            @Override
            public void onError() {
                onServerWentOffline();
            }
        };

        if(poller == null){
            poller = new Poller(this);
            if(ip != null){
                poller.setIp(ip, port);
            }
            poller.startPolling();
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
        menu.getItem(1).setEnabled(ip != null && timerState != TimerState.ERROR);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_ip:
                setIP();
                return true;
            case R.id.menu_resettimer:
                sendCommand(LiveSplitCommand.RESET);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (poller != null) {
            poller.stopPolling();
            poller = null;
        }
        poller = new Poller(this);
        poller.setIp(ip, port);
        poller.startPolling();

        updateGuiToTimerstate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.stop();
        }
        if (poller != null) {
            poller.stopPolling();
            poller = null;
        }
    }

    private void setIP() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.buttonSetIp);
        final EditText inputField = new EditText(this);
        inputField.setHint(R.string.setIpInputHint);
        if (tempIpFromDialog != null) {
            inputField.setText(tempIpFromDialog);
        } else if (ip != null) {
            inputField.setText(ip.getHostAddress());
        }
        builder.setView(inputField);

        builder.setNegativeButton(R.string.setIpDialogCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton(R.string.setIpDialogConfirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                final String input = inputField.getText().toString();

                try {
                    ip = InetAddress.getByName(input);
                    poller.setIp(ip, port);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getSharedPreferences(getString(R.string.pref_id), Activity.MODE_PRIVATE).edit().putString(getString(R.string.settingsIdIp), input).apply();
                        }
                    });
                } catch (UnknownHostException ignored) {
                    tempIpFromDialog = input;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, R.string.ipSyntaxError, Toast.LENGTH_SHORT).show();
                            setIP();
                        }
                    });
                }
            }
        });

        builder.show();
    }

    private void sendCommand(LiveSplitCommand cmd) {
        new Network(defaultCommandListener).execute(ip.getHostAddress(), "" + port, cmd.toString(), Boolean.toString(false));
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
        if (lsTime != null) {
            if (!lsTime.equals("0.00")) {
                timer.setMs(lsTime);
            } else {
                // Bug on LiveSplit server when using game time comparison, ignore synchronization until fixed
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, R.string.gameTimeBug, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public void onServerWentOffline() {
        info.setText(getString(R.string.ipNotReachedInfo, ip.getHostAddress(), port));
        offlineToast = Toast.makeText(MainActivity.this, R.string.ipNotReached, Toast.LENGTH_LONG);
        offlineToast.show();
        timerState = TimerState.ERROR;
        timer.stop();
        updateGuiToTimerstate();
    }

    @Override
    public void onServerWentOnline(TimerState currentState) {
        info.setText(getString(R.string.displayIp, ip.getHostAddress(), port));
        timerState = currentState;
        if(offlineToast != null){
            offlineToast.cancel();
        }
        updateGuiToTimerstate();
        if(currentState == TimerState.RUNNING){
            timer.start();
        }
    }

    @Override
    public void onPollStart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                info.setText(getString(R.string.pingingIp, ip.getHostAddress(), port));
            }
        });
    }

    @Override
    public void onPollEnd() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(ip != null){
                    if(timerState == TimerState.ERROR){
                        info.setText(getString(R.string.ipNotReachedInfo, ip.getHostAddress(), port));
                        if(System.currentTimeMillis() - timestampLastOfflineToast > OFFLINE_TOAST_COOLDOWN_MS){
                            offlineToast = Toast.makeText(MainActivity.this, R.string.ipNotReached, Toast.LENGTH_LONG);
                            offlineToast.show();
                            timestampLastOfflineToast = System.currentTimeMillis();
                        }
                    } else {
                        info.setText(getString(R.string.displayIp, ip.getHostAddress(), port));
                    }
                } else {
                    info.setText(R.string.serverIpNotSet);
                }
            }
        });
    }

    @Override
    public void onOutdatedServer() {
        if(offlineToast != null){
            offlineToast.cancel();
        }

        Toast.makeText(MainActivity.this, R.string.outdatedServer, Toast.LENGTH_LONG).show();
    }

    private void updateGuiToTimerstate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }
}
