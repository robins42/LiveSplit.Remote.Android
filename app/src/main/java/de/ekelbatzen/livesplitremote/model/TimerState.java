package de.ekelbatzen.livesplitremote.model;

public enum TimerState {
    NOT_RUNNING("NotRunning"),
    RUNNING("Running"),
    ENDED("Ended"),
    PAUSED("Paused"),
    ERROR("Error");

    private final String lsName;

    TimerState(String lsName) {
        this.lsName = lsName;
    }

    @Override
    public String toString() {
        return lsName;
    }

    public static TimerState parseState(String toParse) {
        if (toParse == null) {
            return TimerState.ERROR;
        }

        if (toParse.equals(TimerState.NOT_RUNNING.toString())) {
            return TimerState.NOT_RUNNING;
        } else if (toParse.equals(TimerState.PAUSED.toString())) {
            return TimerState.PAUSED;
        } else if (toParse.equals(TimerState.RUNNING.toString())) {
            return TimerState.RUNNING;
        } else if (toParse.equals(TimerState.ENDED.toString())) {
            return TimerState.ENDED;
        } else {
            return TimerState.ERROR;
        }
    }
}
