package de.ekelbatzen.livesplitremote;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;

@SuppressWarnings("HardCodedStringLiteral")
public class Timer extends TextView {
    private static final long MS_BETWEEN_POLLS = 3000L;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd m:ss.SS", Locale.ENGLISH);
    private long ms;
    private long lastPoll;
    private boolean running;
    long lastTick;
    private MainActivity act;

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
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ms = 0L;
        running = false;
        lastTick = System.currentTimeMillis();
    }

    public void stopTimer() {
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

            if (System.currentTimeMillis() - lastPoll > MS_BETWEEN_POLLS && act != null) {
                act.getTimeInMs(new NetworkResponseListener() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            setMs(response);
                        }
                    }
                });
                lastPoll = System.currentTimeMillis();
            }

            super.onDraw(canvas);
            invalidate();
        } else {
            super.onDraw(canvas);
        }
    }

    public void setActivity(MainActivity act) {
        this.act = act;
    }

    public void setMs(long ms) {
        this.ms = ms;
        lastPoll = System.currentTimeMillis();
        setText(msToTimeformat());
    }

    public void setMs(String lsTime) {
        Date d = null;
        try {
            d = sdf.parse("1970-01-01 00:" + lsTime);
        } catch (ParseException e) {
            try {
                d = sdf.parse("1970-01-01 " + lsTime);
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
        }
        if (d != null) {
            setMs(d.getTime());
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
}
