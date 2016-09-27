package de.ekelbatzen.livesplitremote.model;

import java.net.InetAddress;

public interface BroadcastResponseListener {
    void onBroadcastStart();
    void onBroadcastResponse(String response, InetAddress source);
    void onBroadcastEnd();
    void onError(String msg);
}
