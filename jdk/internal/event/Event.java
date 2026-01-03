/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.event;

public abstract class Event {
    protected Event() {
    }

    public void begin() {
    }

    public void end() {
    }

    public void commit() {
    }

    public boolean isEnabled() {
        return false;
    }

    public boolean shouldCommit() {
        return false;
    }

    public void set(int index, Object value) {
    }
}

