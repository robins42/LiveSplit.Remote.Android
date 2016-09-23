package de.ekelbatzen.livesplitremote;

import android.util.Log;

import de.ekelbatzen.livesplitremote.model.LiveSplitCommand;
import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;
import de.ekelbatzen.livesplitremote.model.TimerState;

public class Poller {
    public static long pollDelayMs = 2000L;
    private boolean ipLastOnline;
    private TimerState lastTimerstate;
    private boolean running;
    private Thread pollingThread;
    private int maybeOutdatedCounter;
    private int oooCounter; //out of order, lol
    private final MainActivity act;

    public Poller(MainActivity act) {
        this.act = act;
        running = true;
        ipLastOnline = false;
        lastTimerstate = TimerState.ERROR;
        maybeOutdatedCounter = 0;
        oooCounter = 0;
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
        if (act != null && Network.hasIp()) {
            act.onPollStart();

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

                            if (newTimerstate != null) {
                                oooCounter = 0;
                                // Received unparsable result, maybe this is a network hiccup where the socket received time instead of timerphase
                                if (!ipLastOnline) {
                                    ipLastOnline = true;
                                    act.onServerWentOnline(newTimerstate);
                                }

                                if (lastTimerstate != newTimerstate) {
                                    act.onStateChanged(newTimerstate);
                                }

                                lastTimerstate = newTimerstate;
                            } else {
                                Log.w("Poller", "Received unparsable timerphase response: " + lsTimerphase);
                                oooCounter++;
                                if (oooCounter > 3) {
                                    act.onProblem(act.getString(R.string.networkHiccup));
                                }
                            }

                            act.onTimeSynchronized(lsTime);
                            act.onPollEnd();
                            finishedListener.onResponse(null);
                        }

                        @Override
                        public void onError() {
                            // gettimerphase is maybe not supported
                            maybeOutdatedCounter++;
                            if (maybeOutdatedCounter >= 3) {
                                act.onProblem(act.getString(R.string.outdatedServer));
                            }

                            if (ipLastOnline) {
                                act.onServerWentOffline();
                            }

                            act.onTimeSynchronized(lsTime);
                            ipLastOnline = false;
                            act.onPollEnd();
                            finishedListener.onResponse(null);
                        }
                    }).execute(LiveSplitCommand.GETTIMERSTATE.toString(), Boolean.toString(true));
                }

                @Override
                public void onError() {
                    if (ipLastOnline) {
                        ipLastOnline = false;
                        act.onServerWentOffline();
                        act.onPollEnd();
                        finishedListener.onResponse(null);
                    }

                    maybeOutdatedCounter = 0;
                    ipLastOnline = false;
                    act.onPollEnd();
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
