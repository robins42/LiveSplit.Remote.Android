package de.ekelbatzen.livesplitremote.model;

public interface NetworkResponseListener {
    void onResponse(String response);
    void onError();
}
