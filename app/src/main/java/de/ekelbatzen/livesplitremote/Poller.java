package de.ekelbatzen.livesplitremote;

import java.net.InetAddress;

import de.ekelbatzen.livesplitremote.model.LiveSplitCommand;
import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;
import de.ekelbatzen.livesplitremote.model.PollUpdateListener;
import de.ekelbatzen.livesplitremote.model.TimerState;

public class Poller {
    private final PollUpdateListener listener;
    private static final long POLL_DELAY_MS = 2000L;
    private InetAddress ip;
    private int port;
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

    public void setIp(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void stopPolling() {
        running = false;
    }

    public void startPolling() {
        // Has listener so the poller only starts when the latest poll has been finished
        poll(new NetworkResponseListener() {
            @Override
            public void onResponse(String response) {
                pollingThread = new Thread(){
                    @Override
                    public void run() {
                        try {
                            sleep(POLL_DELAY_MS);
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
                //nothing
            }
        });
    }

    public void instantPoll(){
        if(pollingThread != null){
            pollingThread.interrupt();
        }
    }

    private void poll(final NetworkResponseListener finishedListener) {
        if (listener != null && ip != null) {
            listener.onPollStart();

            new Network(new NetworkResponseListener() {
                @Override
                public void onResponse(String response) {
                    maybeOutdatedCounter = 0;
                    TimerState newTimerstate = parseTimerstate(response);

                    if (!ipLastOnline) {
                        ipLastOnline = true;
                        listener.onServerWentOnline(newTimerstate);
                    }

                    if (lastTimerstate != newTimerstate) {
                        listener.onStateChanged(newTimerstate);
                    }

                    lastTimerstate = newTimerstate;

                    new Network(new NetworkResponseListener() {
                        @Override
                        public void onResponse(String response) {
                            listener.onPollEnd();
                            listener.onTimeSynchronized(response);
                            finishedListener.onResponse(null);
                        }

                        @Override
                        public void onError() {
                            listener.onPollEnd();
                            if (ipLastOnline) {
                                listener.onServerWentOffline();
                            }
                            ipLastOnline = false;
                            finishedListener.onResponse(null);
                        }
                    }).execute(ip.getHostAddress(), "" + port, LiveSplitCommand.GETTIME.toString(), Boolean.toString(true));
                }

                @Override
                public void onError() {
                    listener.onPollEnd();
                    if (ipLastOnline) {
                        listener.onServerWentOffline();
                        ipLastOnline = false;
                        finishedListener.onResponse(null);
                    } else {
                        // Test if server may not support gettimerstate yet
                        new Network(new NetworkResponseListener() {
                            @Override
                            public void onResponse(String response) {
                                // Is maybe not supported
                                maybeOutdatedCounter++;
                                if(maybeOutdatedCounter >= 3){
                                    listener.onOutdatedServer();
                                }
                                ipLastOnline = false;
                                finishedListener.onResponse(null);
                            }

                            @Override
                            public void onError() {
                                //nothing, server might really not be available
                                maybeOutdatedCounter = 0;
                                ipLastOnline = false;
                                finishedListener.onResponse(null);
                            }
                        }).execute(ip.getHostAddress(), "" + port, LiveSplitCommand.GETTIME.toString(), Boolean.toString(true));
                    }
                }
            }).execute(ip.getHostAddress(), "" + port, LiveSplitCommand.GETTIMERSTATE.toString(), Boolean.toString(true));
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
