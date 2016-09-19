package de.ekelbatzen.livesplitremote.model;

public interface PollUpdateListener {
    void onStateChanged(TimerState newState);
    void onTimeSynchronized(String lsTime);
    void onServerWentOffline();
    void onServerWentOnline(TimerState currentState);
    void onPollStart();
    void onPollEnd();
    void onOutdatedServer();
}
