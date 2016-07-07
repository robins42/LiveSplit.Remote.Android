package de.ekelbatzen.livesplitremote;

import android.os.AsyncTask;

import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Network extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... args) {
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        int port = Integer.parseInt(args[1]);
        String cmd = args[2];

        System.out.println("Sending 1: " + cmd);

        try {
            System.out.println("Sending 2: " + ip + ":" + port + ", " + cmd);
            Socket socket = new Socket(ip, port);
            System.out.println("Sending 3: " + cmd);
            OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            osw.write(cmd + "\r\n");
            osw.flush();
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void ignored) {
        System.out.println("Cmd was sent");
    }
}
