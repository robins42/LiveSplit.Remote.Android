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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.ekelbatzen.livesplitremote.model.LiveSplitCommand;
import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;
import de.ekelbatzen.livesplitremote.model.PingListener;
import de.ekelbatzen.livesplitremote.model.TimerState;

public class MainActivity extends AppCompatActivity {
    private static int port = 16834; //I know, hardcoding is bad, will be implemented when I have time
    private String tempIpFromDialog;
    private InetAddress ip;
    private TimerState timerState;
    private TextView info;
    private Button startSplitButton;
    private Button skipButton;
    private Button pauseButton;
    private Timer timer;
    private TextView timerText;
    private boolean checkTimerPhase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, final Throwable e) {
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

        timerState = TimerState.NotRunning;

        info = (TextView) findViewById(R.id.text_warnings);
        startSplitButton = (Button) findViewById(R.id.startSplitButton);
        Button undoButton = (Button) findViewById(R.id.undoButton);
        skipButton = (Button) findViewById(R.id.skipButton);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        timerText = (TextView) findViewById(R.id.timer);

        String savedIP = getSharedPreferences(getString(R.string.pref_id), Activity.MODE_PRIVATE).getString(getString(R.string.settingsIdIp), null);
        checkTimerPhase = getSharedPreferences(getString(R.string.pref_id), Activity.MODE_PRIVATE).getBoolean(getString(R.string.settingsIdTimerphase), false);
        if (savedIP != null) {
            try {
                ip = InetAddress.getByName(savedIP);
                info.setText(getString(R.string.displayIp, ip.getHostAddress()));
                readAllFromServer();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, R.string.ipParseError, Toast.LENGTH_SHORT).show();
            }
        }

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

                        if (timerState == TimerState.NotRunning) {
                            startTimer(true);
                        } else if (timerState == TimerState.Paused) {
                            resumeTimer();
                        } else if (timerState == TimerState.Running) {
                            split();
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

                        undoSplit();
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

                        skipSplit();
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

                        pauseTimer();
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setEnabled(timerState == TimerState.NotRunning);
        menu.getItem(1).setChecked(checkTimerPhase);
        menu.getItem(2).setEnabled(ip != null);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_ip:
                setIP();
                return true;
            case R.id.menu_timerphase:
                checkTimerPhase = !checkTimerPhase;
                item.setChecked(checkTimerPhase);
                if(checkTimerPhase){
                    Toast.makeText(this, R.string.timerphaseHint, Toast.LENGTH_LONG).show();
                }
                getSharedPreferences(getString(R.string.pref_id), Activity.MODE_PRIVATE).edit().putBoolean(getString(R.string.settingsIdTimerphase), checkTimerPhase).apply();
                return true;
            case R.id.menu_resettimer:
                resetTimer();
                return true;
//            case R.id.menu_crash:
//                throw new NullPointerException("This is a test");
            default:
                return false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.stopTimer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null) {
            timer = new Timer(timerText, this);
            getTimeInMs(new NetworkResponseListener() {
                @Override
                public void onResponse(String response) {
                    if (response != null) {
                        timer.setMs(response);
                        if(timerState == TimerState.Running){
                            timer.start();
                        }
                    }
                }
            });
        }
    }

    private void setIP() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.buttonSetIp);
        final EditText inputField = new EditText(this);
        inputField.setHint(R.string.setIpInputHint);
        if (tempIpFromDialog != null) {
            inputField.setText(tempIpFromDialog);
        } else if(ip != null){
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

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            ip = InetAddress.getByName(input);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    info.setText(getString(R.string.pingIp, ip.getHostAddress()));
                                }
                            });

                            pingServer(new PingListener() {
                                @Override
                                public void onPing(boolean wasReached) {
                                    if (wasReached) {
                                        tempIpFromDialog = null;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                info.setText(getString(R.string.displayIp, ip.getHostAddress()));
                                                getSharedPreferences(getString(R.string.pref_id), Activity.MODE_PRIVATE).edit().putString(getString(R.string.settingsIdIp), input).apply();
                                            }
                                        });
                                        readAllFromServer();
                                    } else {
                                        tempIpFromDialog = input;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                info.setText(getString(R.string.serverIpNotSet));
                                                Toast.makeText(MainActivity.this, R.string.ipNotReached, Toast.LENGTH_LONG).show();
                                                setIP();
                                            }
                                        });
                                    }
                                }
                            });
                        } catch (UnknownHostException e) {
                            tempIpFromDialog = input;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    info.setText(getString(R.string.serverIpNotSet));
                                    Toast.makeText(MainActivity.this, R.string.ipSyntaxError, Toast.LENGTH_SHORT).show();
                                    setIP();
                                }
                            });
                        }
                    }
                }.start();
            }
        });

        builder.show();
    }

    private void startTimer(boolean checkFirst) {
        if(checkFirst){
            pingServer(new PingListener() {
                @Override
                public void onPing(boolean wasReached) {
                    if (wasReached) {
                        new Network().execute(ip.getHostAddress(), "" + port, LiveSplitCommand.START.toString());
                        timerState = TimerState.Running;
                        startSplitButton.setText(R.string.splitText);
                        invalidateOptionsMenu();
                        timer = new Timer(timerText, MainActivity.this);
                        timer.start();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                info.setText(getString(R.string.serverIpNotSet));
                                Toast.makeText(MainActivity.this, R.string.ipNotReached, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        } else {
            new Network().execute(ip.getHostAddress(), "" + port, LiveSplitCommand.START.toString());
            timerState = TimerState.Running;
            startSplitButton.setText(R.string.splitText);
            invalidateOptionsMenu();
            timer = new Timer(timerText, MainActivity.this);
            timer.start();
        }
    }

    private void split() {
        new Network().execute(ip.getHostAddress(), "" + port, LiveSplitCommand.SPLIT.toString());

        if(checkTimerPhase) {
            getTimerPhase(new NetworkResponseListener() {
                @Override
                public void onResponse(String ignored) {
                    if(timerState == TimerState.Ended){
                        timeFinished();
                    }
                }
            });
        }
    }

    private void resumeTimer() {
        new Network().execute(ip.getHostAddress(), "" + port, LiveSplitCommand.RESUME.toString());
        timerState = TimerState.Running;
        startSplitButton.setText(R.string.splitText);
        invalidateOptionsMenu();
        long ms = 0L;
        if (timer != null) {
            ms = timer.getMs();
            timer.stopTimer();
        }
        timer = new Timer(timerText, this);
        timer.setMs(ms);
        timer.start();
    }

    private void undoSplit() {
        new Network().execute(ip.getHostAddress(), "" + port, LiveSplitCommand.UNDO.toString());
        if(timerState == TimerState.Ended){
            startSplitButton.setText(R.string.splitText);
            timerState = TimerState.Running;
        }
        startSplitButton.setEnabled(true);
        skipButton.setEnabled(true);
        pauseButton.setEnabled(true);

        if(checkTimerPhase){
            getTimerPhase(new NetworkResponseListener() {
                @Override
                public void onResponse(String response) {
                    if(timerState == TimerState.Running){
                        if(timer != null){
                            timer.stopTimer();
                        }

                        timer = new Timer(timerText, MainActivity.this);
                        timer.start();
                    }

                    getTimeInMs(new NetworkResponseListener() {
                        @Override
                        public void onResponse(String response) {
                            if (response != null) {
                                timer.setMs(response);
                            }
                        }
                    });
                }
            });
        }
    }

    private void skipSplit() {
        new Network().execute(ip.getHostAddress(), "" + port, LiveSplitCommand.SKIP.toString());
    }

    private void pauseTimer() {
        new Network().execute(ip.getHostAddress(), "" + port, LiveSplitCommand.PAUSE.toString());
        timerState = TimerState.Paused;
        invalidateOptionsMenu();
        startSplitButton.setText(R.string.resumeText);
        if (timer != null) {
            timer.interrupt();
        }
        getTimeInMs(new NetworkResponseListener() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    timer.setMs(response);
                }
            }
        });
    }

    private void resetTimer() {
        new Network().execute(ip.getHostAddress(), "" + port, LiveSplitCommand.RESET.toString());
        timerState = TimerState.NotRunning;
        startSplitButton.setText(R.string.startText);
        startSplitButton.setEnabled(true);
        skipButton.setEnabled(true);
        pauseButton.setEnabled(true);
        invalidateOptionsMenu();
        startSplitButton.setText(R.string.startText);
        if (timer != null) {
            timer.interrupt();
            timer.setMs(0L);
        }
    }

    void getTimeInMs(NetworkResponseListener listener) {
        new Network(listener).execute(ip.getHostAddress(), "" + port, LiveSplitCommand.GETTIME.toString(), Boolean.toString(true));
    }

    private void getTimerPhase(final NetworkResponseListener listener) {
        new Network(new NetworkResponseListener() {
            @Override
            public void onResponse(String response) {
                if(response != null){
                    if(response.equals(TimerState.NotRunning.toString())){
                        timerState = TimerState.NotRunning;
                    } else if (response.equals(TimerState.Paused.toString())){
                        timerState = TimerState.Paused;
                    } else if(response.equals(TimerState.Running.toString())){
                        timerState = TimerState.Running;
                    } else if(response.equals(TimerState.Ended.toString())){
                        timerState = TimerState.Ended;
                    }
                }

                listener.onResponse(null);
            }
        }).execute(ip.getHostAddress(), "" + port, LiveSplitCommand.GETTIMERSTATE.toString(), Boolean.toString(true));
    }

    private void pingServer(final PingListener listener) {
        final String lastInfoText = info.getText().toString();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                info.setText(getString(R.string.pingIp, ip.getHostAddress()));
            }
        });

        getTimeInMs(new NetworkResponseListener() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info.setText(lastInfoText);
                    }
                });
                listener.onPing(response != null);
            }
        });
    }

    private void readAllFromServer() {
        pingServer(new PingListener() {
            @Override
            public void onPing(boolean wasReached) {
                if (wasReached) {
                    if(checkTimerPhase){
                        getTimerPhase(new NetworkResponseListener() {
                            @Override
                            public void onResponse(String ignored) {
                                if (timerState == TimerState.Running) {
                                    startTimer(false);
                                } else if(timerState == TimerState.Ended){
                                    timeFinished();
                                } else if(timerState == TimerState.Paused){
                                    invalidateOptionsMenu();
                                    startSplitButton.setText(R.string.resumeText);
                                }

                                getTimeInMs(new NetworkResponseListener() {
                                    @Override
                                    public void onResponse(String response) {
                                        if (response != null) {
                                            if(timer == null){
                                                timer = new Timer(timerText, MainActivity.this);
                                            }
                                            timer.setMs(response);
                                        }
                                    }
                                });
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, R.string.ipNotReached, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void timeFinished() {
        if(timer != null){
            timer.interrupt();

            getTimeInMs(new NetworkResponseListener() {
                @Override
                public void onResponse(String response) {
                    if (response != null) {
                        timer.setMs(response);
                    }
                }
            });
        }

        startSplitButton.setText(R.string.timeFinishedText);
        startSplitButton.setEnabled(false);
        skipButton.setEnabled(false);
        pauseButton.setEnabled(false);
    }
}
