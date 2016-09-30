package de.ekelbatzen.livesplitremote.network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;

@SuppressWarnings("HardCodedStringLiteral")
public class Network extends AsyncTask<String, String, String> {
    private static int timeoutMs = 3000;
    private static InetAddress ip;
    private static int port = 16834;
    private static Socket socket;
    private static OutputStreamWriter out;
    private static BufferedReader in;
    private final NetworkResponseListener listener;
    private boolean cmdSuccessful;
    private static final Object LOCK = new Object();

    public Network(NetworkResponseListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String response = null;

        synchronized (LOCK) {
            Log.v("Network", "Sending " + params[0] + " to " + ip.getHostAddress() + ':' + port);
            cmdSuccessful = false;
            String cmd = params[0];
            boolean listenForResponse = Boolean.parseBoolean(params[1]);

            try {
                if (socket == null || !socket.isConnected()) {
                    openConnection();
                }
                out.write(cmd + "\r\n");
                out.flush();
                if (listenForResponse) {
                    response = in.readLine();
                }
                cmdSuccessful = true;
            } catch (SocketTimeoutException e) {
                // Just print the short timeout message without printing stacktrace, not necessary to spam logcat
                Log.w("Network", "Got an exception trying to send " + cmd + " to " + ip + ':' + port + " - " + e.getMessage());
            } catch (Exception e) {
                Log.w("Network", "Got an exception trying to send " + cmd + " to " + ip + ':' + port);
                e.printStackTrace();
                closeConnection();
                try {
                    openConnection();
                } catch (IOException e1) {
                    Log.w("Network", "Got an exception after reopening connection after previous exception: ");
                    e1.printStackTrace();
                }
            }
        }

        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        synchronized (LOCK) {
            if (listener != null) {
                if (cmdSuccessful) {
                    listener.onResponse(result);
                } else {
                    listener.onError();
                }
            }
        }
    }

    public static void openConnection() throws IOException {
        synchronized (LOCK) {
            if (socket != null) {
                closeConnection();
            }

            if (socket == null) {
                socket = new Socket();
            }
            socket.setSoTimeout(timeoutMs);
            socket.connect(new InetSocketAddress(ip, port), timeoutMs);
            out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        }
    }

    public static void closeConnection() {
        synchronized (LOCK) {
            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (out != null) {
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (socket != null) {
                try {
                    socket.close();
                    socket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static InetAddress getIp() {
        return ip;
    }

    public static void setIp(InetAddress ip) {
        closeConnection();
        Network.ip = ip;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        closeConnection();
        Network.port = port;
    }

    public static boolean hasIp() {
        return ip != null;
    }

    public static void setTimeoutMs(int timeoutMs) {
        Network.timeoutMs = timeoutMs;
        if (socket != null) {
            try {
                socket.setSoTimeout(timeoutMs);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }
}
