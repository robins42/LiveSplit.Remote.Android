package de.ekelbatzen.livesplitremote;

public enum LiveSplitCommand {
    START("starttimer"),
    SPLIT("split"),
    PAUSE("pause"),
    RESUME("resume"),
    RESET("reset");

    private String name;

    LiveSplitCommand(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
