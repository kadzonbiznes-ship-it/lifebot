/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.logic.hid;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.crypto.SecretKey;
import org.pbrands.hid.HIDSimulator;
import org.pbrands.hid.uber.UberWrapper;

public class UberHIDSimulator
implements HIDSimulator {
    private boolean mousePressed = false;
    private final UberWrapper wrapper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UberHIDSimulator(byte[] encodedDll, SecretKey key) {
        this.wrapper = new UberWrapper(encodedDll, key, true);
    }

    @Override
    public void sendKeystroke(char letter, long pressTime) {
        this.executor.submit(() -> this.wrapper.pressChar(letter, pressTime));
    }

    @Override
    public void sendAltKeystroke(long pressTime) {
        this.executor.submit(() -> {
            this.wrapper.keyDown(164);
            try {
                Thread.sleep(pressTime);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.wrapper.keyUp(164);
        });
    }

    @Override
    public void sendMouseClick(long pressTime) {
        new Thread(() -> {
            this.wrapper.mouseDown(1);
            this.mousePressed = true;
            try {
                Thread.sleep(pressTime);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.wrapper.mouseUp(1);
            this.mousePressed = false;
        }).start();
    }

    @Override
    public void sendMousePress() {
        this.wrapper.mouseDown(1);
        this.mousePressed = true;
    }

    @Override
    public void sendMouseRelease() {
        this.wrapper.mouseUp(1);
        this.mousePressed = false;
    }

    @Override
    public boolean isMousePressed() {
        return this.mousePressed;
    }
}

