package de.ekelbatzen.livesplitremote.gui;

import android.content.Context;
import android.graphics.Canvas;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;

import java.util.Locale;

import de.ekelbatzen.livesplitremote.R;

public class Timer extends AppCompatTextView {
    private static final String TAG = Timer.class.getName();
    private long ms;
    private boolean running;
    private long lastTick;
    private MainActivity act;
    private int oooCounter; //out of order, lol
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
            String[] parts = lsTime.split(":");
            long hours = 0L;
            long minutes;
            long seconds;
            long millis;

            if (parts.length > 2) {
                // HHH…:mm:ss.SS
                hours = Long.parseLong(parts[0]);
                minutes = Long.parseLong(parts[1]);
            } else {
                // mm:ss.SS
                minutes = Long.parseLong(parts[0]);
            }

            String[] secondsAndMs = parts[parts.length - 1].split("\\.");
            seconds = Long.parseLong(secondsAndMs[0]);

            millis = Long.parseLong(secondsAndMs[1] + '0'); // Fixing the things SimpleDateFormat does wrong on some phones

            long totalMs = (hours * 60L * 60L * 1000L) + (minutes * 60L * 1000L) + (seconds * 1000L) + millis;
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
        long allSeconds = ms / 1000L;
        long hours = 0L;
        long minutes = 0L;
        long seconds = 0L;
        StringBuilder displayedTime = new StringBuilder(11);

        if (hEnabled) {
            hours = allSeconds / (60L * 60L);
            if (!hOptional || hours > 0L) {
                displayedTime.append(String.format(Locale.ENGLISH, "%02d", hours));
                displayedTime.append(':');
            }
        }

        if (mEnabled) {
            if (hEnabled && (!hOptional || hours > 0L)) {
                minutes = (allSeconds / 60L) % 60L;
            } else {
                minutes = allSeconds / 60L;
            }

            if (!mOptional || minutes > 0L || hours > 0L) {
                displayedTime.append(String.format(Locale.ENGLISH, "%02d", minutes));
                displayedTime.append(':');
            }
        }

        if (mEnabled) {
            seconds = allSeconds % 60L;
        } else {
            seconds = allSeconds;
        }
        displayedTime.append(String.format(Locale.ENGLISH, "%02d", seconds));

        String msShort = "";
        if (msDigits == 1) {
            msShort = '.' + String.format(Locale.ENGLISH, "%1d", (ms % 1000L) / 100L);
        } else if (msDigits == 2) {
            msShort = '.' + String.format(Locale.ENGLISH, "%02d", (ms % 1000L) / 10L);
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

    public void onTimerFormatChanged(){
        lastTick = System.currentTimeMillis();
        setText(msToTimeformat(ms)); //Redraw with new format
    }
}
