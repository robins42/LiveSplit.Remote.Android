package de.ekelbatzen.livesplitremote.model;

public enum TimerState {
    NOT_RUNNING("NotRunning"),
    RUNNING("Running"),
    ENDED("Ended"),
    PAUSED("Paused"),
    ERROR("Error");

    private final String lsName;

    TimerState(String lsName){
        this.lsName = lsName;
    }

    @Override
    public String toString() {
        return lsName;
    }
}
