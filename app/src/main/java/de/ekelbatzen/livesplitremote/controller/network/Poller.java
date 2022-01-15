package de.ekelbatzen.livesplitremote.controller.network;

import android.util.Log;

import de.ekelbatzen.livesplitremote.R;
import de.ekelbatzen.livesplitremote.model.LiveSplitCommand;
import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;
import de.ekelbatzen.livesplitremote.model.TimerState;
import de.ekelbatzen.livesplitremote.view.MainActivity;

public class Poller {
    private static final String TAG = Poller.class.getName();
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

    public void startPolling() {
        // Has listener so the poller only starts when the latest poll has been finished
        poll(new NetworkResponseListener() {
            @Override
            public void onResponse(String ignored) {
                restartPolling();
            }

            @Override
            public void onError() {
                restartPolling();
            }
        });
    }

    private void restartPolling() {
        pollingThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(pollDelayMs);
                } catch (InterruptedException ignore) {
                    // nothing, was probably interrupted on purpose
                }

                if (running) {
                    startPolling();
                }
            }
        };

        pollingThread.start();
    }

    public void stopPolling() {
        running = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }

    public void instantPoll() {
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }

    private void poll(final NetworkResponseListener finishedListener) {
        if (act != null && Network.hasIp()) {
            act.onPollStart();

            pollTime(finishedListener);
        }
    }

    private void pollTime(NetworkResponseListener finishedListener) {
        // First get time to also see if server is available
        new Network(new NetworkResponseListener() {
            @Override
            public void onResponse(final String lsTime) {
                pollTimerState(finishedListener, lsTime);
            }

            @Override
            public void onError() {
                onPollTimeError(finishedListener);
            }
        }).execute(LiveSplitCommand.GETTIME.toString(), Boolean.toString(true));
    }

    private void pollTimerState(NetworkResponseListener finishedListener, String lsTime) {
        // Then get timer phase since it may not be supported while the server is online
        new Network(new NetworkResponseListener() {
            @Override
            public void onResponse(String lsTimerphase) {
                onPollComplete(finishedListener, lsTime, lsTimerphase);
            }

            @Override
            public void onError() {
                onPollTimerStateError(finishedListener, lsTime);
            }
        }).execute(LiveSplitCommand.GETTIMERSTATE.toString(), Boolean.toString(true));
    }

    private void onPollComplete(NetworkResponseListener finishedListener, String lsTime, String lsTimerphase) {
        maybeOutdatedCounter = 0;
        TimerState newTimerstate = TimerState.parseState(lsTimerphase);
        updateTimerState(newTimerstate);
        act.onTimeSynchronized(lsTime);
        act.onPollEnd();
        finishedListener.onResponse(null);
    }

    private void updateTimerState(TimerState newTimerstate) {
        oooCounter = 0;
        if (!ipLastOnline) {
            ipLastOnline = true;
            act.onServerWentOnline(newTimerstate);
        }

        if (lastTimerstate != newTimerstate) {
            act.onStateChanged(newTimerstate);
        }

        lastTimerstate = newTimerstate;
    }

    private void onPollTimeError(NetworkResponseListener finishedListener) {
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

    private void onPollTimerStateError(NetworkResponseListener finishedListener, String lsTime) {
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
}
