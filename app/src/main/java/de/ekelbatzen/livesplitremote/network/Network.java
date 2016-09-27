package de.ekelbatzen.livesplitremote.network;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import de.ekelbatzen.livesplitremote.model.BroadcastResponseListener;
import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;

@SuppressWarnings("HardCodedStringLiteral")
public class Network extends AsyncTask<String, String, String> {
    private static final String TAG = "Network";
    private static int timeoutMs = 3000;
    private static InetAddress ip;
    private static int port = 16834;
    private static Socket socket;
    private static DatagramSocket broadcastSocket;
    private static OutputStreamWriter out;
    private static BufferedReader in;
    private NetworkResponseListener listener;
    private BroadcastResponseListener broadcastListener;
    private boolean cmdSuccessful;
    private WifiManager wm;
    private static final Object LOCK = new Object();

    public Network(Context c, NetworkResponseListener listener) {
        this.listener = listener;
        wm = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
    }

    public Network(Context c, BroadcastResponseListener listener) {
        this.broadcastListener = listener;
        wm = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    protected String doInBackground(String... params) {
        synchronized (LOCK) {
            cmdSuccessful = false;
            String cmd = params[0];
            boolean listenForResponse = Boolean.parseBoolean(params[1]);
            boolean broadcast = Boolean.parseBoolean(params[2]);
            Log.v(TAG, "Sending " + params[0] + " to " + ip.getHostAddress() + ':' + port + ", broadcast: " + broadcast);

            String response = null;

            if (!broadcast) {
                // Send normal command
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
                    Log.w(TAG, "Got an exception trying to send " + cmd + " to " + ip + ':' + port + " - " + e.getMessage());
                } catch (Exception e) {
                    Log.w(TAG, "Got an exception trying to send " + cmd + " to " + ip + ':' + port);
                    e.printStackTrace();
                    closeConnection();
                    try {
                        openConnection();
                    } catch (IOException e1) {
                        Log.w(TAG, "Got an exception after reopening connection after previous exception: ");
                        e1.printStackTrace();
                    }
                }
            } else {
                // Broadcast to find server
                try {
                    broadcastListener.onBroadcastStart();
                    startUdpListener();
                    InetAddress ip = getBroadcastIp();
                    if (ip != null) {
                        broadcast(ip, cmd);
                        cmdSuccessful = true;
                    }
                } catch (IOException e) {
                    Log.w(TAG, "Got an exception trying to send broadcast to " + ip + ':' + port);
                    e.printStackTrace();
                    broadcastListener.onError("Got an error sending broadcast to " + ip.getHostAddress() + ':' + port);
                }
            }

            return response;
        }
    }

    private void startUdpListener() throws IOException {
        if (broadcastSocket == null) {
            broadcastSocket = new DatagramSocket(port);
            broadcastSocket.setReceiveBufferSize(32);
            broadcastSocket.setSoTimeout(timeoutMs);
        }

        new Thread() {
            byte[] buffer = "#########_#########_#########_##".getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            @Override
            public void run() {
                Log.d(TAG, "Starting to listen to udp");

                while (true) {
                    try {
                        broadcastSocket.receive(packet);
                        Log.v(TAG, "got something: " + new String(packet.getData(), "UTF-8") + " from " + packet.getAddress().getHostAddress());
                        String myIp = getMyWifiIP().getAddress().toString();

                        if (!packet.getAddress().toString().equals(myIp) && broadcastListener != null) {
                            broadcastListener.onBroadcastResponse(new String(packet.getData(), "UTF-8"), packet.getAddress());
                        }
                    } catch (IOException e) {
                        break;
                    }
                }

                Log.d(TAG, "Not listening to udp anymore");
                broadcastSocket.close();
                broadcastSocket = null;
                broadcastListener.onBroadcastEnd();
            }
        }.start();
    }

    private static void broadcast(InetAddress ip, String cmd) throws IOException {
        Log.v(TAG, "Sending broadcast " + cmd + " to " + ip.getHostAddress());
        cmd += "\r\n";

        if (broadcastSocket == null) {
            broadcastSocket = new DatagramSocket(port);
            broadcastSocket.setSendBufferSize(32);
            broadcastSocket.setReceiveBufferSize(32);
            broadcastSocket.setBroadcast(true);
        }

        byte[] data = cmd.getBytes("UTF-8");
        DatagramPacket packetD = new DatagramPacket(data, data.length, ip, port);

        broadcastSocket.send(packetD);
        broadcastSocket.disconnect();
    }

    private InetAddress getBroadcastIp() throws SocketException {
        InterfaceAddress ip = getMyWifiIP();
        if (ip != null) {
            return ip.getBroadcast();
        } else {
            return null;
        }
    }

    private InterfaceAddress getMyWifiIP() throws SocketException {
        List<InterfaceAddress> ips = getMyWifiIPs();
        InterfaceAddress temp = null;

        if (ips != null) {
            for (InterfaceAddress i : ips) {
                temp = i;
                if (isIpv4(i.getAddress())) {
                    return i;
                }
            }
        }

        return temp;
    }

    private List<InterfaceAddress> getMyWifiIPs() throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface networkInterface = e.nextElement();
            String name = networkInterface.getName();
            ArrayList<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());

            if (!name.contains("dummy")) {
                for (InetAddress ip : addresses) {
                    if (!ip.isAnyLocalAddress() && !ip.isLoopbackAddress() && isIpv4(ip)) {
                        return networkInterface.getInterfaceAddresses();
                    }
                }
            }
        }

        throw new SocketException("Could not find suitable phone ip");
    }


    private static boolean isIpv4(InetAddress ip) {
        if (ip.getAddress().length != 4) {
            return false;
        }

        String ipS = ip.getHostAddress();
        String[] split = ipS.split("\\.");
        if (split.length != 4) {
            return false;
        }

        try {
            for (String b : split) {
                int temp = Integer.parseInt(b);
                if (temp > 255 || temp < 0) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
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

    public static void setIp(InetAddress ip) {
        closeConnection();
        Network.ip = ip;
    }

    public static void setPort(int port) {
        closeConnection();
        Network.port = port;
    }

    public static boolean hasIp() {
        return ip != null;
    }

    public static InetAddress getIp() {
        return ip;
    }

    public static int getPort() {
        return port;
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
