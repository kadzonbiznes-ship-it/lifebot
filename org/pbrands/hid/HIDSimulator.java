/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.hid;

public interface HIDSimulator {
    public void sendKeystroke(char var1, long var2);

    public void sendAltKeystroke(long var1);

    default public void sendArrowUp(long pressTime) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default public void sendMouseClick(long pressTime) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default public void sendMousePress() {
        throw new UnsupportedOperationException("Not implemented");
    }

    default public void sendMouseRelease() {
        throw new UnsupportedOperationException("Not implemented");
    }

    default public boolean isMousePressed() {
        return false;
    }
}

