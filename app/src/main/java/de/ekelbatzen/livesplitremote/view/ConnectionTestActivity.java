package de.ekelbatzen.livesplitremote.view;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.net.util.SubnetUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.model.TimerState;

public class ConnectionTestActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY);
    private static final int PING_TIMEOUT_MS = 5000;
    private static final AtomicInteger testCounter = new AtomicInteger(0);
    private String serverIP;
    private String serverPort;

    private Button button;
    private CheckBox test1Checkbox;
    private CheckBox test2Checkbox;
    private CheckBox test3Checkbox;
    private CheckBox test4Checkbox;
    private CheckBox test5Checkbox;
    private CheckBox test6Checkbox;
    private CheckBox test7Checkbox;
    private CheckBox test8Checkbox;
    private CheckBox test9Checkbox;
    private CheckBox test10Checkbox;
    private CheckBox test11Checkbox;
    private CheckBox test12Checkbox;
    private CheckBox test13Checkbox;
    private TextView test1Info;
    private TextView test2Info;
    private TextView test3Info;
    private TextView test4Info;
    private TextView test5Info;
    private TextView test6Info;
    private TextView test7Info;
    private TextView test8Info;
    private TextView test9Info;
    private TextView test10Info;
    private TextView test11Info;
    private TextView test12Info;
    private TextView test13Info;
    private ProgressBar test1Progressbar;
    private ProgressBar test2Progressbar;
    private ProgressBar test3Progressbar;
    private ProgressBar test4Progressbar;
    private ProgressBar test5Progressbar;
    private ProgressBar test6Progressbar;
    private ProgressBar test7Progressbar;
    private ProgressBar test8Progressbar;
    private ProgressBar test9Progressbar;
    private ProgressBar test10Progressbar;
    private ProgressBar test11Progressbar;
    private ProgressBar test12Progressbar;
    private ProgressBar test13Progressbar;
    private TextView resultTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(MainActivity.darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_connection);

        readPreferences();
        findViews();
        assignButton();
    }

    private void findViews() {
        button = findViewById(R.id.connection_test_button);
        test1Checkbox = findViewById(R.id.connection_test_1_checkbox);
        test2Checkbox = findViewById(R.id.connection_test_2_checkbox);
        test3Checkbox = findViewById(R.id.connection_test_3_checkbox);
        test4Checkbox = findViewById(R.id.connection_test_4_checkbox);
        test5Checkbox = findViewById(R.id.connection_test_5_checkbox);
        test6Checkbox = findViewById(R.id.connection_test_6_checkbox);
        test7Checkbox = findViewById(R.id.connection_test_7_checkbox);
        test8Checkbox = findViewById(R.id.connection_test_8_checkbox);
        test9Checkbox = findViewById(R.id.connection_test_9_checkbox);
        test10Checkbox = findViewById(R.id.connection_test_10_checkbox);
        test11Checkbox = findViewById(R.id.connection_test_11_checkbox);
        test12Checkbox = findViewById(R.id.connection_test_12_checkbox);
        test13Checkbox = findViewById(R.id.connection_test_13_checkbox);
        test1Info = findViewById(R.id.connection_test_1_info);
        test2Info = findViewById(R.id.connection_test_2_info);
        test3Info = findViewById(R.id.connection_test_3_info);
        test4Info = findViewById(R.id.connection_test_4_info);
        test5Info = findViewById(R.id.connection_test_5_info);
        test6Info = findViewById(R.id.connection_test_6_info);
        test7Info = findViewById(R.id.connection_test_7_info);
        test8Info = findViewById(R.id.connection_test_8_info);
        test9Info = findViewById(R.id.connection_test_9_info);
        test10Info = findViewById(R.id.connection_test_10_info);
        test11Info = findViewById(R.id.connection_test_11_info);
        test12Info = findViewById(R.id.connection_test_12_info);
        test13Info = findViewById(R.id.connection_test_13_info);
        test1Progressbar = findViewById(R.id.connection_test_1_progressbar);
        test2Progressbar = findViewById(R.id.connection_test_2_progressbar);
        test3Progressbar = findViewById(R.id.connection_test_3_progressbar);
        test4Progressbar = findViewById(R.id.connection_test_4_progressbar);
        test5Progressbar = findViewById(R.id.connection_test_5_progressbar);
        test6Progressbar = findViewById(R.id.connection_test_6_progressbar);
        test7Progressbar = findViewById(R.id.connection_test_7_progressbar);
        test8Progressbar = findViewById(R.id.connection_test_8_progressbar);
        test9Progressbar = findViewById(R.id.connection_test_9_progressbar);
        test10Progressbar = findViewById(R.id.connection_test_10_progressbar);
        test11Progressbar = findViewById(R.id.connection_test_11_progressbar);
        test12Progressbar = findViewById(R.id.connection_test_12_progressbar);
        test13Progressbar = findViewById(R.id.connection_test_13_progressbar);
        resultTitle = findViewById(R.id.connection_test_result_title);
    }

    private void readPreferences() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        serverIP = prefs.getString(getString(R.string.settingsIdIp), null);
        serverPort = prefs.getString(getString(R.string.settingsIdPort), null);
    }

    private void assignButton() {
        button.setOnClickListener(ignored -> runAllTests());
    }

    private void runAllTests() {
        Log.i(TAG, "Starting connection tests…");
        button.setEnabled(false);
        button.setText(R.string.connection_test_button_running);
        testCounter.set(0);
        resetView();

        runTest1();
        runTest2();
        runTest3();
        runTest4();
        runTest5();
        runTest6();
        runTest7();
        runTest8();
        runTest9();
        runTest10();
        runTest11();
        runTest12();
        runTest13();
    }

    private void resetView() {
        test1Checkbox.setChecked(false);
        test2Checkbox.setChecked(false);
        test3Checkbox.setChecked(false);
        test4Checkbox.setChecked(false);
        test5Checkbox.setChecked(false);
        test6Checkbox.setChecked(false);
        test7Checkbox.setChecked(false);
        test8Checkbox.setChecked(false);
        test9Checkbox.setChecked(false);
        test10Checkbox.setChecked(false);
        test11Checkbox.setChecked(false);
        test12Checkbox.setChecked(false);
        test13Checkbox.setChecked(false);

        test1Info.setText(R.string.connection_test_info_not_run);
        test2Info.setText(R.string.connection_test_info_not_run);
        test3Info.setText(R.string.connection_test_info_not_run);
        test4Info.setText(R.string.connection_test_info_not_run);
        test5Info.setText(R.string.connection_test_info_not_run);
        test6Info.setText(R.string.connection_test_info_not_run);
        test7Info.setText(R.string.connection_test_info_not_run);
        test8Info.setText(R.string.connection_test_info_not_run);
        test9Info.setText(R.string.connection_test_info_not_run);
        test10Info.setText(R.string.connection_test_info_not_run);
        test11Info.setText(R.string.connection_test_info_not_run);
        test12Info.setText(R.string.connection_test_info_not_run);
        test13Info.setText(R.string.connection_test_info_not_run);

        test1Info.setTextColor(getResources().getColor(R.color.connection_test_error));
        test2Info.setTextColor(getResources().getColor(R.color.connection_test_error));
        test3Info.setTextColor(getResources().getColor(R.color.connection_test_error));
        test4Info.setTextColor(getResources().getColor(R.color.connection_test_error));
        test5Info.setTextColor(getResources().getColor(R.color.connection_test_error));
        test6Info.setTextColor(getResources().getColor(R.color.connection_test_error));
        test7Info.setTextColor(getResources().getColor(R.color.connection_test_error));
        test8Info.setTextColor(getResources().getColor(R.color.connection_test_error));
        test9Info.setTextColor(getResources().getColor(R.color.connection_test_error));
        test10Info.setTextColor(getResources().getColor(R.color.connection_test_error));
        test11Info.setTextColor(getResources().getColor(R.color.connection_test_error));
        test12Info.setTextColor(getResources().getColor(R.color.connection_test_error));
        test13Info.setTextColor(getResources().getColor(R.color.connection_test_error));
    }

    private void runTest1() {
        runTest(1, test1Progressbar, test1Info, () -> {
            try {
                if (serverIP == null || serverIP.trim().isEmpty()) {
                    throw new UnknownHostException();
                }
                InetAddress byName = InetAddress.getByName(serverIP);
                if (byName == null) {
                    throw new UnknownHostException();
                }
                testSuccess(test1Info, test1Checkbox, R.string.connection_test_1_success, serverIP);
            } catch (UnknownHostException ignored) {
                testFailure(1, test1Info, test1Checkbox, R.string.connection_test_1_error, serverIP);
            }
        });
    }

    private void runTest2() {
        runTest(2, test2Progressbar, test2Info, () -> {
            try {
                int port = Integer.parseInt(serverPort);
                if (port < 0 || port > 65536) {
                    throw new NumberFormatException();
                }

                testSuccess(test2Info, test2Checkbox, R.string.connection_test_2_success, serverPort);
            } catch (NumberFormatException ignored) {
                testFailure(2, test2Info, test2Checkbox, R.string.connection_test_2_error, serverPort);
            }
        });
    }

    private void runTest3() {
        runTest(3, test3Progressbar, test3Info, () -> {
            boolean hasInternet =
                    checkCallingOrSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
            boolean hasNetworkState =
                    checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
            boolean hasWifiState =
                    checkCallingOrSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;

            if (!hasInternet) {
                testFailure(3, test3Info, test3Checkbox, R.string.connection_test_3_error_no_internet);
                return;
            }
            if (!hasNetworkState) {
                testFailure(3, test3Info, test3Checkbox, R.string.connection_test_3_error_no_network_state);
                return;
            }
            if (!hasWifiState) {
                testFailure(3, test3Info, test3Checkbox, R.string.connection_test_3_error_no_wifi_state);
                return;
            }

            testSuccess(test3Info, test3Checkbox, R.string.connection_test_3_success);
        });
    }

    private void runTest4() {
        runTest(4, test4Progressbar, test4Info, () -> {
            try {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()) {
                    testSuccess(test4Info, test4Checkbox, R.string.connection_test_4_success);
                    return;
                }
                throw new Exception();
            } catch (Exception ignored) {
                testFailure(4, test4Info, test4Checkbox, R.string.connection_test_4_error);
            }
        });
    }

    private void runTest5() {
        runTest(5, test5Progressbar, test5Info, () -> {
            try {
                ConnectivityManager connManager =
                        (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (wifi != null && wifi.isConnected()) {
                    testSuccess(test5Info, test5Checkbox, R.string.connection_test_5_success);
                    return;
                }

                throw new Exception(wifi != null ? "Wifi is not connected" : "Wifi info could not be retrieved");
            } catch (Exception e) {
                testFailure(5, test5Info, test5Checkbox, R.string.connection_test_5_error, e.getMessage());
            }
        });
    }

    private void runTest6() {
        runTest(6, test6Progressbar, test6Info, () -> {
            try {
                IpResult ipv4 = getIPAddress(true);
                if (ipv4 == null || !ipv4.ip.trim().isEmpty()) {
                    testSuccess(test6Info, test6Checkbox, R.string.connection_test_6_success, ipv4.ip);
                    return;
                } else {
                    IpResult ipv6 = getIPAddress(false);
                    if (ipv6 == null || !ipv6.ip.trim().isEmpty()) {
                        testFailure(6, test6Info, test6Checkbox, R.string.connection_test_6_error_ipv6, ipv6);
                        return;
                    }
                }
                throw new SocketException("No IP was found");
            } catch (SocketException e) {
                testFailure(6, test6Info, test6Checkbox, R.string.connection_test_6_error_no_ip, e.getMessage());
            }
        });
    }

    /**
     * Based on: https://stackoverflow.com/a/13007325/3014722
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static IpResult getIPAddress(boolean useIPv4) throws SocketException {
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface intf : interfaces) {
            List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            for (InetAddress addr : addrs) {
                if (!addr.isLoopbackAddress()) {
                    String sAddr = addr.getHostAddress();
                    boolean isIPv4 = sAddr.indexOf(':') < 0;

                    if (useIPv4) {
                        if (isIPv4) {
                            return new IpResult(sAddr, extractSubnetMaskLength(sAddr, intf.getInterfaceAddresses()));
                        }
                    } else {
                        if (!isIPv4) {
                            int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                            sAddr = delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            return new IpResult(sAddr, extractSubnetMaskLength(sAddr, intf.getInterfaceAddresses()));
                        }
                    }
                }
            }
        }
        return null;
    }

    private static int extractSubnetMaskLength(String ip, List<InterfaceAddress> interfaceAddresses) {
        for (InterfaceAddress ifa : interfaceAddresses) {
            if (ifa.getAddress().getHostAddress().contains(ip)) {
                return ifa.getNetworkPrefixLength();
            }
        }
        return 0;
    }

    static class IpResult {
        String ip;
        int subnetMaskLenth;

        public IpResult(String ip, int subnetMaskLenth) {
            this.ip = ip;
            this.subnetMaskLenth = subnetMaskLenth;
        }
    }

    private void runTest7() {
        runTest(7, test7Progressbar, test7Info, () -> {
            final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            final DhcpInfo dhcp = wifiManager.getDhcpInfo();
            final String gatewayAddress = Formatter.formatIpAddress(dhcp.gateway);

            try {
                InetAddress gateway = InetAddress.getByName(gatewayAddress);
                if (!gateway.isReachable(PING_TIMEOUT_MS)) {
                    throw new UnknownHostException("Not reachable");
                }
                testSuccess(test7Info, test7Checkbox, R.string.connection_test_7_success, gatewayAddress);
            } catch (IOException e) {
                testFailure(7, test7Info, test7Checkbox, R.string.connection_test_7_error, gatewayAddress, e.getMessage());
            }
        });
    }

    private void runTest8() {
        runTest(8, test8Progressbar, test8Info, () -> {
            try {
                InetAddress server = InetAddress.getByName(serverIP);

                if (!server.isReachable(PING_TIMEOUT_MS)) {
                    throw new UnknownHostException("Method isReachable got timeout");
                }
                testSuccess(test8Info, test8Checkbox, R.string.connection_test_8_success, serverIP);
            } catch (IOException e) {
                testFailure(8, test8Info, test8Checkbox, R.string.connection_test_8_error, serverIP, e.getMessage());
            }
        });
    }

    private void runTest9() {
        runTest(9, test9Progressbar, test9Info, () -> {
            int serverPortInt = Integer.parseInt(serverPort);

            try (Socket socket = new Socket()) {
                socket.setSoTimeout(PING_TIMEOUT_MS);
                socket.connect(new InetSocketAddress(serverIP, serverPortInt), PING_TIMEOUT_MS);
                testSuccess(test9Info, test9Checkbox, R.string.connection_test_9_success, serverIP, serverPort);
            } catch (Exception e) {
                testFailure(9, test9Info, test9Checkbox, R.string.connection_test_9_error, serverIP, serverPort, e.getMessage());
            }
        });
    }

    private void runTest10() {
        runTest(10, test10Progressbar, test10Info, () -> {
            int serverPortInt = Integer.parseInt(serverPort);
            Charset charset = Charset.forName("UTF-8");
            try (Socket socket = new Socket()) {
                socket.setSoTimeout(PING_TIMEOUT_MS);
                socket.connect(new InetSocketAddress(serverIP, serverPortInt), PING_TIMEOUT_MS);
                new OutputStreamWriter(socket.getOutputStream(), charset);
                new BufferedReader(new InputStreamReader(socket.getInputStream(), charset));
                testSuccess(test10Info, test10Checkbox, R.string.connection_test_10_success);
            } catch (Exception e) {
                testFailure(10, test10Info, test10Checkbox, R.string.connection_test_10_error);
            }
        });
    }

    private void runTest11() {
        runTest(11, test11Progressbar, test11Info, () -> {
            int serverPortInt = Integer.parseInt(serverPort);
            Charset charset = Charset.forName("UTF-8");
            try (Socket socket = new Socket()) {
                socket.setSoTimeout(PING_TIMEOUT_MS);
                socket.connect(new InetSocketAddress(serverIP, serverPortInt), PING_TIMEOUT_MS);
                OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), charset);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset));

                out.write("getcurrenttimerphase\r\n");
                out.flush();
                String response = in.readLine();
                TimerState newTimerstate = TimerState.parseState(response);
                if (newTimerstate == null) {
                    throw new Exception();
                }
                testSuccess(test11Info, test11Checkbox, R.string.connection_test_11_success, response);
            } catch (Exception e) {
                testFailure(11, test11Info, test11Checkbox, R.string.connection_test_11_error);
            }
        });
    }

    private void runTest12() {
        runTest(12, test12Progressbar, test12Info, () -> {
            int serverPortInt = Integer.parseInt(serverPort);
            Charset charset = Charset.forName("UTF-8");
            try (Socket socket = new Socket()) {
                socket.setSoTimeout(PING_TIMEOUT_MS);
                socket.connect(new InetSocketAddress(serverIP, serverPortInt), PING_TIMEOUT_MS);
                OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), charset);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset));

                out.write("getcurrenttimerphase\r\n");
                out.flush();
                String response = in.readLine();
                testSuccess(test12Info, test12Checkbox, R.string.connection_test_12_success, response);
            } catch (Exception e) {
                testFailure(12, test12Info, test12Checkbox, R.string.connection_test_12_error, e.getMessage());
            }
        });
    }

    private void runTest13() {
        runTest(13, test13Progressbar, test13Info, () -> {
            int serverPortInt = Integer.parseInt(serverPort);
            Charset charset = Charset.forName("UTF-8");

            try {
                IpResult ipAddress = getIPAddress(true);
                SubnetUtils subnetUtils = new SubnetUtils(ipAddress.ip + "/" + ipAddress.subnetMaskLenth);
                String[] allAddresses = subnetUtils.getInfo().getAllAddresses();

                ConcurrentLinkedQueue<String> gotResponse = new ConcurrentLinkedQueue<String>();
                AtomicInteger counter = new AtomicInteger(1);
                int timeout = 5000;
                List<Thread> threads = new ArrayList<Thread>();
                for (String ip : allAddresses) {
                    threads.add(new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            int count = counter.getAndIncrement();
                            runOnUiThread(() -> {
                                test13Info.setText("Checking IP #" + count + " / " + allAddresses.length + "...");
                            });
                            try (Socket socket = new Socket()) {
                                socket.setSoTimeout(timeout);
                                socket.connect(new InetSocketAddress(ip, serverPortInt), timeout);
                                OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), charset);
                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset));

                                out.write("getcurrenttimerphase\r\n");
                                out.flush();
                                in.readLine();
                                gotResponse.add(ip);
                            } catch (Exception ignored) {
                            }
                        }
                    });
                }

                for (Thread t : threads) {
                    t.start();
                }
                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                String responseString = "Got responses from: ";
                for (String r : gotResponse) {
                    if (responseString.length() > 20) {
                        responseString += "\r\n";
                    }
                    responseString += "- " + r;
                }
                testSuccess(test13Info, test13Checkbox, R.string.connection_test_13_success, responseString);
            } catch (SocketException e) {
                e.printStackTrace();
                testFailure(13, test13Info, test13Checkbox, R.string.connection_test_13_error, e.getMessage());
            }

//            dhcp.netmask;
//            SubnetUtils subnetUtils = new SubnetUtils(serverIP, subnetMask);


        });
    }

    private synchronized void runTest(int testNumber, ProgressBar testProgressbar, TextView testInfo,
                                      Runnable test) {
        Log.i(TAG, "Running connection test " + testNumber + "...");
        new Thread() {
            @Override
            public void run() {
                super.run();
                synchronized (TAG) {
                    adjustGuiToTestStart(testProgressbar, testInfo);
                    test.run();
                    adjustGuiToTestEnd(testProgressbar);
                    testCounter.getAndIncrement();
                    checkAllTestsFinished();
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void adjustGuiToTestStart(ProgressBar testProgressbar, TextView testInfo) {
        runOnUiThread(() -> {
            testProgressbar.setVisibility(View.VISIBLE);
            testInfo.setText(R.string.connection_test_running);
            testInfo.setTextColor(getResources().getColor(R.color.connection_test_running));
        });
    }

    private void adjustGuiToTestEnd(ProgressBar testProgressbar) {
        runOnUiThread(() -> testProgressbar.setVisibility(View.INVISIBLE));
    }

    private void testSuccess(TextView testInfo, CheckBox testCheckbox, int infoStringId, Object... stringArgs) {
        runOnUiThread(() -> {
            testInfo.setTextColor(getResources().getColor(R.color.connection_test_success));
            testInfo.setText(getString(infoStringId, stringArgs));
            testCheckbox.setChecked(true);
        });
    }

    private void testFailure(int testNumber, TextView testInfo, CheckBox testCheckbox, int infoStringId, Object... stringArgs) {
        runOnUiThread(() -> {
            Log.i(TAG, "Test " + testNumber + " failed.");
            testInfo.setTextColor(getResources().getColor(R.color.connection_test_error));
            testInfo.setText(getString(infoStringId, stringArgs));
            testCheckbox.setChecked(false);
        });
    }

    private void checkAllTestsFinished() {
        if (testCounter.get() >= 10) {
            runOnUiThread(() -> {
                Log.i(TAG, "Connection tests are finished.");
                resultTitle.setText(getString(R.string.connection_test_result_title, sdf.format(new Date())));
                button.setText(R.string.connection_test_button);
                button.setEnabled(true);
            });
        }
    }
}
