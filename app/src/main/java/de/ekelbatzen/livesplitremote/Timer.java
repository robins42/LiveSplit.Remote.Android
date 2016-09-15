package de.ekelbatzen.livesplitremote;

import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.ekelbatzen.livesplitremote.model.NetworkResponseListener;

@SuppressWarnings("HardCodedStringLiteral")
public class Timer extends Thread {
    private static final long MS_BETWEEN_POLLS = 10_000L;
    private TextView text;
    private MainActivity act;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd m:ss.SS", Locale.ENGLISH);
    private long ms;
    private long lastPoll;

    public Timer(TextView text, MainActivity act) {
        this.text = text;
        this.act = act;
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ms = 0L;
    }

    public void stopTimer(){
        interrupt();
        text = null;
        act = null;
    }

    @Override
    public void run() {
        long lastTick = System.currentTimeMillis();

        while (true) {
            try {
                sleep(100L);
                ms += System.currentTimeMillis() - lastTick;
                lastTick = System.currentTimeMillis();
                updateText();

                if(System.currentTimeMillis() - lastPoll > MS_BETWEEN_POLLS && act != null) {
                    act.getTimeInMs(new NetworkResponseListener() {
                        @Override
                        public void onResponse(String response) {
                            if(response != null){
                                setMs(response);
                            } else {
                                lastPoll = System.currentTimeMillis();
                            }
                        }
                    });
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void updateText() {
        if(act != null){
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(text != null){
                        text.setText(msToTimeformat());
                    }
                }
            });
        }
    }

    public void setMs(long ms) {
        this.ms = ms;
        lastPoll = System.currentTimeMillis();
        updateText();
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
        if(d != null){
            setMs(d.getTime());
        }
    }

    public long getMs() {
        return ms;
    }

    private String msToTimeformat() {
        String msShort = String.format(Locale.ENGLISH, "%1d", ms % 1000L / 100L);
        long allSeconds = ms / 1000L;
        String s = String.format(Locale.ENGLISH, "%02d", allSeconds % 60L);
        String m = String.format(Locale.ENGLISH, "%02d", (allSeconds / 60L) % 60L);
        String h = String.format(Locale.ENGLISH, "%02d", (allSeconds / (60L * 60L)) % 24L);
        return h + ':' + m + ':' + s + '.' + msShort;
    }


}
