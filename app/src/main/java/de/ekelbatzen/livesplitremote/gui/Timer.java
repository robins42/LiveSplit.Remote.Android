package de.ekelbatzen.livesplitremote.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

import java.util.Locale;

import de.ekelbatzen.livesplitremote.R;

public class Timer extends AppCompatTextView {
    private static final String TAG = Timer.class.getName();
    long ms;
    private boolean running;
    private long lastTick;
    private MainActivity act;
    int oooCounter; //out of order, lol
    private static boolean hEnabled = true;
    private static boolean hOptional = true;
    private static boolean mEnabled = true;
    private static boolean mOptional = true;
    private static int msDigits = 2;

    public Timer(Context context) {
        super(context);
        init();
    }

    public Timer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Timer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        oooCounter = 0;
        ms = 0L;
        running = false;
        lastTick = System.currentTimeMillis();
        setText(msToTimeformat(ms));
    }

    public void stop() {
        running = false;
    }

    public void start() {
        running = true;
        lastTick = System.currentTimeMillis();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (running) {
            ms += System.currentTimeMillis() - lastTick;
            lastTick = System.currentTimeMillis();
            setText(msToTimeformat(ms));

            super.onDraw(canvas);
            invalidate();
        } else {
            super.onDraw(canvas);
        }
    }

    private void setMs(long ms) {
        this.ms = ms;
        setText(msToTimeformat(ms));
    }

    public void setMs(String lsTime) {
        try {
            // New internal server uses this format for 0
            if ("00:00:00".equals(lsTime)) {
                setMs(0);
                return;
            }

            /* "−" Dash seems to be used for null, Unicode 8722.
               "-" Minus seems to be used for negative time, Unicode 45.
               Accept both, just treat them as default - */
            lsTime = lsTime.replace("−", "-");

            if (lsTime.equals("-")) {
                // This is supposed to be "null" or invalid value
                setMs(0);
                Log.w(TAG, "Received 'null' / 'invalid time' response '-', setting time to 0");
                return;
            }

            String[] parts = lsTime.split(":");
            long hours = 0L;
            long minutes;
            long seconds;
            long millis;
            long days;

            if (parts.length > 2) {
                String[] hourParts = parts[0].split("\\.");
                if (hourParts.length == 2) {
                    // LiveSplit 2024+ version, format [-][d.]hh:mm:ss[.SSSSSSS]
                    days = Math.abs(Long.parseLong(hourParts[0]));
                    hours = days * 24L + Long.parseLong(hourParts[1]);
                } else {
                    // LiveSplit pre 2024 version, format HHH…:mm:ss.SS
                    hours = Math.abs(Long.parseLong(parts[0]));
                }
                minutes = Math.abs(Long.parseLong(parts[1]));
            } else {
                // mm:ss.SS
                minutes = Math.abs(Long.parseLong(parts[0]));
            }

            String[] secondsAndMs = parts[parts.length - 1].split("\\.");
            seconds = Long.parseLong(secondsAndMs[0]);

            if (secondsAndMs.length > 1) {
                /* The "+ '0'" fixed the things SimpleDateFormat does wrong on some phones with only 2 digits.
                   There may be 7 digits of millisecond accuracy supplied by the server in the 2024+ version,
                   but more than 3 are unnecessary for this app */
                millis = Long.parseLong((secondsAndMs[1] + '0').substring(0, 3));
            } else {
                // No milliseconds were sent
                millis = 0;
            }

            long totalMs = (hours * 60L * 60L * 1000L) + (minutes * 60L * 1000L) + (seconds * 1000L) + millis;

            if (lsTime.startsWith("-") && totalMs > 0) {
                totalMs *= -1;
            }

            setMs(totalMs);
            oooCounter = 0;
        } catch (NumberFormatException ignored) {
            // Received unparsable result, maybe this is a network hiccup where the socket received timerphase instead of time
            Log.w(TAG, "Received unparsable time response: " + lsTime);
            oooCounter++;
            if (oooCounter > 3 && act != null) {
                act.onProblem(act.getString(R.string.networkHiccup));
            }
        } catch (NullPointerException e) {
            Log.w(TAG, "Received null time response", e);

            oooCounter++;
            if (oooCounter > 3 && act != null) {
                act.onProblem(act.getString(R.string.networkHiccup));
            }
        }
    }

    private static String msToTimeformat(long ms) {
        long hours = 0L;
        long minutes = 0L;
        long seconds = 0L;
        StringBuilder displayedTime = new StringBuilder(12);

        if (ms < 0) {
            displayedTime.append("-");
            ms *= -1;
        }

        long allSeconds = ms / 1000L;

        if (hEnabled) {
            hours = allSeconds / (60L * 60L);
            if (!hOptional || hours != 0L) {
                displayedTime.append(String.format(Locale.ENGLISH, "%02d", Math.abs(hours)));
                displayedTime.append(':');
            }
        }

        if (mEnabled) {
            if (hEnabled && (!hOptional || hours != 0L)) {
                minutes = (allSeconds / 60L) % 60L;
            } else {
                minutes = allSeconds / 60L;
            }

            if (!mOptional || minutes != 0L || hours != 0L) {
                displayedTime.append(String.format(Locale.ENGLISH, "%02d", Math.abs(minutes)));
                displayedTime.append(':');
            }
        }

        if (mEnabled) {
            seconds = allSeconds % 60L;
        } else {
            seconds = allSeconds;
        }
        displayedTime.append(String.format(Locale.ENGLISH, "%02d", Math.abs(seconds)));

        String msShort = "";
        if (msDigits == 1) {
            msShort = '.' + String.format(Locale.ENGLISH, "%1d", (Math.abs(ms) % 1000L) / 100L);
        } else if (msDigits == 2) {
            msShort = '.' + String.format(Locale.ENGLISH, "%02d", (Math.abs(ms) % 1000L) / 10L);
        }
        displayedTime.append(msShort);

        return displayedTime.toString();
    }

    public void setActivity(MainActivity act) {
        this.act = act;
    }

    public static void setFormatting(String format) {
        boolean hEnabled = format.contains("HH:");
        boolean hOptional = format.contains("[HH:]");
        boolean mEnabled = format.contains("mm:");
        boolean mOptional = format.contains("[mm:]");
        int dotIndex = format.indexOf('.');
        int msDigits = dotIndex >= 0 ? format.substring(dotIndex + 1).length() : 0;
        Log.v(TAG, "format " + format + " -> " + hEnabled + ", " + hOptional + ", " + mEnabled + ", " + mOptional + ", " + msDigits + ",,, " + dotIndex + ", " + (dotIndex >= 0 ? format.substring(dotIndex + 1) : "null"));
        setFormatting(hEnabled, hOptional, mEnabled, mOptional, msDigits);
    }

    private static void setFormatting(boolean hEnabled, boolean hOptional, boolean mEnabled, boolean mOptional, int msDigits) {
        Timer.hEnabled = hEnabled;
        Timer.hOptional = hOptional;
        Timer.mEnabled = mEnabled;
        Timer.mOptional = mOptional;
        Timer.msDigits = msDigits;
    }

    public void onTimerFormatChanged() {
        lastTick = System.currentTimeMillis();
        setText(msToTimeformat(ms)); //Redraw with new format
    }
}
