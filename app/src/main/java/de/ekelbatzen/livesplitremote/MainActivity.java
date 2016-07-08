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

public class MainActivity extends AppCompatActivity {
    private static int port = 16834; //I know, hardcoding is bad, will be implemented when I have time
    private String ipDialogSaved;
    private InetAddress ip;
    private TimerState timerState;
    private TextView info;
    private Button theButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, final Throwable e) {
                new Thread() {
                    @Override
                    public void run() {
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
                }.start();
            }
        });

        setContentView(R.layout.activity_main);
        timerState = TimerState.STOPPED;

        info = (TextView) findViewById(R.id.text_warnings);
        theButton = (Button) findViewById(R.id.theBigButton);

        String savedIP = getSharedPreferences(getString(R.string.pref_id), Activity.MODE_PRIVATE).getString("IP", null);
        if (savedIP != null) {
            try {
                ip = InetAddress.getByName(savedIP);
                info.setText("Server IP is set to " + ip.getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error reading saved IP", Toast.LENGTH_SHORT).show();
            }
        }

        theButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ip == null) {
                            Toast.makeText(MainActivity.this, "Server IP has not been set yet.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (timerState == TimerState.STOPPED) {
                            new Network().execute(ip.getHostAddress(), "" + port, LiveSplitCommand.START.toString());
                            timerState = TimerState.STARTED;
                            theButton.setText("Split");
                            invalidateOptionsMenu();
                        } else if (timerState == TimerState.PAUSED) {
                            new Network().execute(ip.getHostAddress(), "" + port, LiveSplitCommand.RESUME.toString());
                            timerState = TimerState.STARTED;
                            theButton.setText("Split");
                            invalidateOptionsMenu();
                        } else if (timerState == TimerState.STARTED) {
                            new Network().execute(ip.getHostAddress(), "" + port, LiveSplitCommand.SPLIT.toString());
                        }
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
        menu.getItem(0).setEnabled(timerState == TimerState.STOPPED);
        menu.getItem(1).setEnabled(timerState != TimerState.STOPPED && ip != null);
        menu.getItem(2).setEnabled(ip != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_ip:
                setIP();
                return true;
            case R.id.menu_pausetimer:
                new Network().execute(ip.getHostAddress(), "" + port, LiveSplitCommand.PAUSE.toString());
                timerState = TimerState.PAUSED;
                invalidateOptionsMenu();
                theButton.setText("Resume Timer");
                return true;
            case R.id.menu_resettimer:
                new Network().execute(ip.getHostAddress(), "" + port, LiveSplitCommand.RESET.toString());
                timerState = TimerState.STOPPED;
                invalidateOptionsMenu();
                theButton.setText("Start Timer");
                return true;
//            case R.id.menu_crash:
//                throw new NullPointerException("This is a test");
            default:
                return false;
        }
    }

    public void setIP() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set LiveSplit Server IP");
        final EditText inputField = new EditText(this);
        inputField.setHint("LiveSplit Server IP here");
        if (ipDialogSaved != null) {
            inputField.setText(ipDialogSaved);
        }
        builder.setView(inputField);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                final String input = inputField.getText().toString();

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            ip = InetAddress.getByName(input);
                            ipDialogSaved = null;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    info.setText("Server IP is set to " + ip.getHostAddress());
                                    getSharedPreferences(getString(R.string.pref_id), Activity.MODE_PRIVATE).edit().putString("IP", input).apply();
                                }
                            });
                        } catch (UnknownHostException e) {
                            ipDialogSaved = input;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "IP syntax is not valid", Toast.LENGTH_SHORT).show();
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
}
