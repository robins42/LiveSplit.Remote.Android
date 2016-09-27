package de.ekelbatzen.livesplitremote;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

import de.ekelbatzen.livesplitremote.gui.MainActivity;

@SuppressWarnings("HardCodedStringLiteral")
public class Timer extends TextView {
    private long ms;
    private boolean running;
    private long lastTick;
    private MainActivity act;
    private int oooCounter; //out of order, lol

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
            setText(msToTimeformat());

            super.onDraw(canvas);
            invalidate();
        } else {
            super.onDraw(canvas);
        }
    }

    private void setMs(long ms) {
        this.ms = ms;
        setText(msToTimeformat());
    }

    public void setMs(String lsTime) {
        try{
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

            //noinspection StringConcatenationMissingWhitespace
            millis = Long.parseLong(secondsAndMs[1] + '0'); // Fixing the things SimpleDateFormat does wrong on some phones

            long totalMs = (hours * 60L * 60L * 1000L) + (minutes * 60L * 1000L) + (seconds * 1000L) + millis;
            setMs(totalMs);
            oooCounter = 0;
        } catch (NumberFormatException ignored){
            // Received unparsable result, maybe this is a network hiccup where the socket received timerphase instead of time
            Log.w("Timer", "Received unparsable time response: " + lsTime);
            oooCounter++;
            if (oooCounter > 3 && act != null) {
                act.onProblem(act.getString(R.string.networkHiccup));
            }
        }
    }

    private String msToTimeformat() {
        String msShort = String.format(Locale.ENGLISH, "%02d", (ms % 1000L) / 10L);

        long allSeconds = ms / 1000L;
        String s = String.format(Locale.ENGLISH, "%02d", allSeconds % 60L);
        String m = String.format(Locale.ENGLISH, "%02d", (allSeconds / 60L) % 60L);
        String h = String.format(Locale.ENGLISH, "%02d", (allSeconds / (60L * 60L)) % 24L);
        return h + ':' + m + ':' + s + '.' + msShort;
    }

    public void setActivity(MainActivity act){
        this.act = act;
    }
}
