package de.ekelbatzen.livesplitremote;

import de.ekelbatzen.livesplitremote.model.LiveSplitCommand;
import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;
import de.ekelbatzen.livesplitremote.model.PollUpdateListener;
import de.ekelbatzen.livesplitremote.model.TimerState;

public class Poller {
    public static long pollDelayMs = 2000L;
    private final PollUpdateListener listener;
    private boolean ipLastOnline;
    private TimerState lastTimerstate;
    private boolean running;
    private Thread pollingThread;
    private int maybeOutdatedCounter;

    public Poller(PollUpdateListener listener) {
        this.listener = listener;
        running = true;
        ipLastOnline = false;
        lastTimerstate = TimerState.ERROR;
        maybeOutdatedCounter = 0;
    }

    public void stopPolling() {
        running = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }

    public void startPolling() {
        // Has listener so the poller only starts when the latest poll has been finished
        poll(new NetworkResponseListener() {
            @Override
            public void onResponse(String ignored) {
                pollingThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(pollDelayMs);
                        } catch (InterruptedException ignored) {
                            // nothing, was probably interrupted on purpose
                        }

                        if (running) {
                            startPolling();
                        }
                    }
                };

                pollingThread.start();
            }

            @Override
            public void onError() {
                pollingThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(pollDelayMs);
                        } catch (InterruptedException ignored) {
                            // nothing, was probably interrupted on purpose
                        }

                        if (running) {
                            startPolling();
                        }
                    }
                };

                pollingThread.start();
            }
        });
    }

    public void instantPoll() {
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }

    private void poll(final NetworkResponseListener finishedListener) {
        if (listener != null && Network.ip != null) {
            listener.onPollStart();

            // First get time to also see if server is available
            new Network(new NetworkResponseListener() {
                @Override
                public void onResponse(final String lsTime) {
                    // Then get timer phase since it may not be supported while the server is online
                    new Network(new NetworkResponseListener() {
                        @Override
                        public void onResponse(String lsTimerphase) {
                            maybeOutdatedCounter = 0;
                            TimerState newTimerstate = parseTimerstate(lsTimerphase);

                            if (!ipLastOnline) {
                                ipLastOnline = true;
                                listener.onServerWentOnline(newTimerstate);
                            }

                            if (lastTimerstate != newTimerstate) {
                                listener.onStateChanged(newTimerstate);
                            }

                            lastTimerstate = newTimerstate;

                            listener.onTimeSynchronized(lsTime);
                            listener.onPollEnd();
                            finishedListener.onResponse(null);
                        }

                        @Override
                        public void onError() {
                            // gettimerphase is maybe not supported
                            maybeOutdatedCounter++;
                            if (maybeOutdatedCounter >= 3) {
                                listener.onOutdatedServer();
                            }

                            if (ipLastOnline) {
                                listener.onServerWentOffline();
                            }

                            listener.onTimeSynchronized(lsTime);
                            ipLastOnline = false;
                            listener.onPollEnd();
                            finishedListener.onResponse(null);
                        }
                    }).execute(LiveSplitCommand.GETTIMERSTATE.toString(), Boolean.toString(true));
                }

                @Override
                public void onError() {
                    if (ipLastOnline) {
                        ipLastOnline = false;
                        listener.onServerWentOffline();
                        listener.onPollEnd();
                        finishedListener.onResponse(null);
                    }

                    maybeOutdatedCounter = 0;
                    ipLastOnline = false;
                    listener.onPollEnd();
                    finishedListener.onResponse(null);
                }
            }).execute(LiveSplitCommand.GETTIME.toString(), Boolean.toString(true));
        }
    }

    private static TimerState parseTimerstate(String toParse) {
        if (toParse != null) {
            if (toParse.equals(TimerState.NOT_RUNNING.toString())) {
                return TimerState.NOT_RUNNING;
            } else if (toParse.equals(TimerState.PAUSED.toString())) {
                return TimerState.PAUSED;
            } else if (toParse.equals(TimerState.RUNNING.toString())) {
                return TimerState.RUNNING;
            } else if (toParse.equals(TimerState.ENDED.toString())) {
                return TimerState.ENDED;
            }
        }

        return TimerState.ERROR;
    }
}
