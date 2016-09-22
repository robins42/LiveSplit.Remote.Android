package de.ekelbatzen.livesplitremote;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;

@SuppressWarnings("HardCodedStringLiteral")
public class Network extends AsyncTask<String, String, String> {
    public static InetAddress ip;
    public static int port = 16834;
    public static int timeoutMs = 3000;
    private final NetworkResponseListener listener;
    private boolean cmdSuccessful;

    public Network(NetworkResponseListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        Log.v("Network", "Sending " + params[0] + " to " + ip.getHostAddress() + ':' + port);
        cmdSuccessful = false;
        String cmd = params[0];
        boolean listenForResponse = false;
        if (params.length > 1) {
            listenForResponse = Boolean.parseBoolean(params[1]);
        }

        String response = null;
        Socket socket = null;
        OutputStreamWriter osw = null;
        BufferedReader br = null;

        try {
            socket = new Socket();
            socket.setSoTimeout(timeoutMs);
            socket.connect(new InetSocketAddress(ip, port), timeoutMs);
            osw = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            osw.write(cmd + "\r\n");
            osw.flush();
            if (listenForResponse) {
                response = br.readLine();
            }
            cmdSuccessful = true;
        } catch (SocketTimeoutException e) {
            // Just print the short timeout message without printing stacktrace, not necessary to spam logcat
            Log.w("Network", "Got an exception trying to send " + cmd + " to " + ip + ':' + port + " - " + e.getMessage());
        } catch (Exception e) {
            Log.w("Network", "Got an exception trying to send " + cmd + " to " + ip + ':' + port);
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        if (listener != null) {
            if (cmdSuccessful) {
                listener.onResponse(result);
            } else {
                listener.onError();
            }
        }
    }
}
