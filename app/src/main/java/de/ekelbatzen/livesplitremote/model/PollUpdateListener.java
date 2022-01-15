package de.ekelbatzen.livesplitremote.model;

public interface PollUpdateListener {
    void onStateChanged(TimerState newState);

    void onServerWentOffline();

    void onServerWentOnline(TimerState currentState);

    void onTimeSynchronized(String lsTime);

    void onPollStart();

    void onPollEnd();

    void onProblem(String msg);
}
