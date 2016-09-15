package de.ekelbatzen.livesplitremote.model;

@SuppressWarnings("HardCodedStringLiteral")
public enum LiveSplitCommand {
    START("starttimer"),
    SPLIT("split"),
    PAUSE("pause"),
    RESUME("resume"),
    UNDO("unsplit"),
    SKIP("skipsplit"),
    RESET("reset"),
    GETTIME("getcurrenttime"),
    GETTIMERSTATE("getcurrenttimerphase");

    private final String link;

    LiveSplitCommand(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return link;
    }
}
