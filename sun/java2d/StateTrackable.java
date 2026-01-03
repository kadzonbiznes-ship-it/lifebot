/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import sun.java2d.StateTracker;

public interface StateTrackable {
    public State getState();

    public StateTracker getStateTracker();

    public static enum State {
        IMMUTABLE,
        STABLE,
        DYNAMIC,
        UNTRACKABLE;

    }
}

