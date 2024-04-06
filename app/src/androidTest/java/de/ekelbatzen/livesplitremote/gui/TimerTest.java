package de.ekelbatzen.livesplitremote.gui;

import static org.junit.Assert.assertEquals;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

public class TimerTest {
    private Timer timer;

    @Before
    public void init() {
        timer = new Timer(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Test
    public void formatTestNegativeMS() {
        assertEquals(0, timer.ms);
        timer.setMs("-0:00.99");
        assertEquals(-990, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("-00.99", timer.getText());
    }

    @Test
    public void formatTestNegativeSeconds() {
        assertEquals(0, timer.ms);
        timer.setMs("-0:59.00");
        assertEquals(-59 * 1000, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("-59.00", timer.getText());
    }

    @Test
    public void formatTestNegativeMinutes() {
        assertEquals(0, timer.ms);
        timer.setMs("-59:00.00");
        assertEquals(-59 * 60 * 1000, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("-59:00.00", timer.getText());
    }

    @Test
    public void formatTestNegativeHours() {
        assertEquals(0, timer.ms);
        timer.setMs("-23:00:00.00");
        assertEquals(-23L * 60 * 60 * 1000, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("-23:00:00.00", timer.getText());
    }

    @Test
    public void formatTestNegativeMinuteCustomMinute() {
        assertEquals(0, timer.ms);
        timer.setMs("-1:06.31");
        assertEquals(-(1L * 60 * 1000 + 6 * 1000 + 310), timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("-01:06.31", timer.getText());
    }

    @Test
    public void formatTestNegativeMinuteCustomHour() {
        assertEquals(0, timer.ms);
        timer.setMs("-1:00:03.17");
        assertEquals(-(1L * 60 * 60 * 1000 + 3 * 1000 + 170), timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("-01:00:03.17", timer.getText());
    }

    @Test
    public void formatTestMaxMS() {
        assertEquals(0, timer.ms);
        timer.setMs("0:00.99");
        assertEquals(990, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("00.99", timer.getText());
    }

    @Test
    public void formatTestMaxMSPrecision() {
        assertEquals(0, timer.ms);
        timer.setMs("0:00.9999999");
        assertEquals(999, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("00.99", timer.getText());
    }

    @Test
    public void formatTestMaxSeconds() {
        assertEquals(0, timer.ms);
        timer.setMs("0:59.00");
        assertEquals(59 * 1_000, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("59.00", timer.getText());
    }

    @Test
    public void formatTestMaxMinutes() {
        assertEquals(0, timer.ms);
        timer.setMs("59:00.00");
        assertEquals(59 * 60 * 1_000, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("59:00.00", timer.getText());
    }

    @Test
    public void formatTest23Hours() {
        assertEquals(0, timer.ms);
        timer.setMs("23:00:00.00");
        assertEquals(23L * 60L * 60L * 1_000L, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("23:00:00.00", timer.getText());
    }

    @Test
    public void formatTest99Hours2023() {
        assertEquals(0, timer.ms);
        timer.setMs("99:00:00.00");
        assertEquals(99L * 60L * 60L * 1_000L, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("99:00:00.00", timer.getText());
    }

    @Test
    public void formatTest99Hours2024() {
        assertEquals(0, timer.ms);
        timer.setMs("4.03:00:00.00");
        assertEquals(99L * 60L * 60L * 1_000L, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("99:00:00.00", timer.getText());
    }

    @Test
    public void formatTest99Hours2024Negative() {
        assertEquals(0, timer.ms);
        timer.setMs("-4.03:00:00.00");
        assertEquals(-99L * 60L * 60L * 1_000L, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("-99:00:00.00", timer.getText());
    }

    @Test
    public void formatTest2024Minus() {
        assertEquals(0, timer.ms);
        timer.setMs("-");
        assertEquals(0, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("00.00", timer.getText());
    }

    @Test
    public void formatTest2024Dash() {
        assertEquals(0, timer.ms);
        timer.setMs("−");
        assertEquals(0, timer.ms);
        assertEquals(0, timer.oooCounter);
        assertEquals("00.00", timer.getText());
    }
}