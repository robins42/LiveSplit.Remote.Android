package de.ekelbatzen.livesplitremote.controller.network;

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
import java.nio.charset.Charset;

import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;

public class Network extends AsyncTask<String, String, String> {
    private static final String TAG = Network.class.getName();
    private static int timeoutMs = 3000;
    private static InetAddress ip;
    private static int port = 16834;
    private static Socket socket;
    private static OutputStreamWriter out;
    private static BufferedReader in;
    private static final Charset charset = Charset.forName("UTF-8");
    private final NetworkResponseListener listener;
    private boolean cmdSuccessful;
    private static final Object LOCK = new Object();

    public Network(NetworkResponseListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String command = params[0];
        synchronized (LOCK) {
            try {
                return sendCommand(params, command);
            } catch (SocketTimeoutException e) {
                socketTimedOut(command, e);
            } catch (Exception e) {
                genericException(command, e);
            }
            return null;
        }
    }

    private String sendCommand(String[] params, String command) throws IOException {
        Log.v(TAG, "Sending " + params[0] + " to " + ip.getHostAddress() + ':' + port);
        String response = null;
        boolean commandHasResponse = Boolean.parseBoolean(params[1]);
        openConnectionIfNecessary();
        sendCommand(command);
        if (commandHasResponse) {
            response = in.readLine();
        }
        cmdSuccessful = !commandHasResponse || response != null;
        return response;
    }

    private void socketTimedOut(String command, SocketTimeoutException e) {
        // Just print the short timeout message without printing stacktrace, not necessary to spam logcat
        handleException(command, e, false);
    }

    private void genericException(String command, Exception e) {
        handleException(command, e, true);
    }

    private void handleException(String command, Exception e, boolean printStackTrace) {
        cmdSuccessful = false;
        String msg = "Got an exception, closing connection. " +
                "Socket was rying to send " + command + " to " + ip + ':' + port + " - " + e.getMessage();
        if (printStackTrace) {
            Log.w(TAG, msg, e);
        } else {
            Log.w(TAG, msg);
        }
        closeConnection();
    }

    private void sendCommand(String cmd) throws IOException {
        out.write(cmd + "\r\n");
        out.flush();
    }

    private void openConnectionIfNecessary() throws IOException {
        if (socket == null || !socket.isConnected()) {
            openConnection();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        synchronized (LOCK) {
            triggerListener(result);
        }
    }

    private void triggerListener(String result) {
        if (listener == null) {
            return;
        }
        if (cmdSuccessful) {
            listener.onResponse(result);
        } else {
            listener.onError();
        }
    }

    public static void openConnection() throws IOException {
        synchronized (LOCK) {
            closeConnection();

            socket = new Socket();
            socket.setSoTimeout(timeoutMs);
            socket.connect(new InetSocketAddress(ip, port), timeoutMs);
            out = new OutputStreamWriter(socket.getOutputStream(), charset);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset));
        }
    }

    public static void closeConnection() {
        synchronized (LOCK) {
            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (IOException e) {
                    Log.w(TAG, "Could not close input stream", e);
                }
            }

            if (out != null) {
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    Log.w(TAG, "Could not close output stream", e);
                }
            }

            if (socket != null) {
                try {
                    socket.close();
                    socket = null;
                } catch (IOException e) {
                    Log.w(TAG, "Could not close socket", e);
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
                Log.w(TAG, "Could not set network timeout", e);
            }
        }
    }

    public static void runInThread(Runnable action) {
        new Thread(action).start();
    }
}
