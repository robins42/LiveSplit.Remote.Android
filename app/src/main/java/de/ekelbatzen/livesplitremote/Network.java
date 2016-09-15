package de.ekelbatzen.livesplitremote;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;

@SuppressWarnings("HardCodedStringLiteral")
public class Network extends AsyncTask<String, String, String> {
    private final NetworkResponseListener listener;

    public Network(){
        this.listener = null;
    }

    public Network(NetworkResponseListener listener){
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... args) {
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        int port = Integer.parseInt(args[1]);
        String cmd = args[2];
        boolean listenForResponse = false;
        if(args.length > 3){
            listenForResponse = Boolean.parseBoolean(args[3]);
        }

        String response = null;
        Socket socket = null;
        OutputStreamWriter osw = null;
        BufferedReader br = null;

        try {
            socket = new Socket();
            socket.setSoTimeout(3000);
            socket.connect(new InetSocketAddress(ip, port), 3000);
            osw = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            osw.write(cmd + "\r\n");
            osw.flush();
            if(listenForResponse){
                response = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(br != null){
                try{
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(osw != null){
                try{
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(socket != null){
                try{
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return response;
    }

    @Override
    protected void onPostExecute(String response) {
        if(listener != null){
            listener.onResponse(response);
        }
    }
}
