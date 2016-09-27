package de.ekelbatzen.livesplitremote.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.model.BroadcastResponseListener;
import de.ekelbatzen.livesplitremote.model.LiveSplitCommand;
import de.ekelbatzen.livesplitremote.network.Network;

public class BroadcastActivity extends AppCompatActivity {
    private TextView info;
    private ProgressBar networkIndicator;
    private ListView list;
    private Button manualIpButton;
    private boolean gotError;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> ips;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_broadcast);
        info = (TextView) findViewById(R.id.infoBroadcast);
        networkIndicator = (ProgressBar) findViewById(R.id.broadcastingIndicator);
        list = (ListView) findViewById(R.id.serversListview);
        manualIpButton = (Button) findViewById(R.id.buttonManualIp);

        manualIpButton.setEnabled(false);
        manualIpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        refresh();
    }

    private void refresh() {
        gotError = false;

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        list.setAdapter(adapter);
        ips = new ArrayList<String>(1);

        new Thread() {
            @Override
            public void run() {
                new Network(BroadcastActivity.this, new BroadcastResponseListener() {
                    @Override
                    public void onBroadcastStart() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                info.setText(getString(R.string.broadcastSearching, Network.getPort()));
                                networkIndicator.setVisibility(View.VISIBLE);
                                manualIpButton.setEnabled(true);
                            }
                        });
                    }

                    @Override
                    public void onBroadcastResponse(final String response, final InetAddress source) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!ips.contains(source.getHostAddress())) {
                                    adapter.add(source.getHostAddress() + "\nCurrent time: " + response);
                                    adapter.notifyDataSetChanged();
                                    ips.add(source.getHostAddress());
                                }
                                manualIpButton.setEnabled(true);
                            }
                        });
                    }

                    @Override
                    public void onBroadcastEnd() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!gotError){
                                    if (adapter.getCount() != 1) {
                                        info.setText(getString(R.string.broadcastEndPlural, adapter.getCount()));
                                    } else {
                                        info.setText(R.string.broadcastEndSingular);
                                    }
                                }
                                networkIndicator.setVisibility(View.INVISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onError(final String msg) {
                        gotError = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                info.setText(msg);
                            }
                        });
                    }
                }).execute(LiveSplitCommand.GETTIME.toString(), Boolean.toString(true), Boolean.toString(true));
            }
        }.start();
    }
}