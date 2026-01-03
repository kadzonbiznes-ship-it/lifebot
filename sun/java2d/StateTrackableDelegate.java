/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import sun.java2d.StateTrackable;
import sun.java2d.StateTracker;

public final class StateTrackableDelegate
implements StateTrackable {
    public static final StateTrackableDelegate UNTRACKABLE_DELEGATE = new StateTrackableDelegate(StateTrackable.State.UNTRACKABLE);
    public static final StateTrackableDelegate IMMUTABLE_DELEGATE = new StateTrackableDelegate(StateTrackable.State.IMMUTABLE);
    private StateTrackable.State theState;
    StateTracker theTracker;
    private int numDynamicAgents;

    public static StateTrackableDelegate createInstance(StateTrackable.State state) {
        switch (state) {
            case UNTRACKABLE: {
                return UNTRACKABLE_DELEGATE;
            }
            case STABLE: {
                return new StateTrackableDelegate(StateTrackable.State.STABLE);
            }
            case DYNAMIC: {
                return new StateTrackableDelegate(StateTrackable.State.DYNAMIC);
            }
            case IMMUTABLE: {
                return IMMUTABLE_DELEGATE;
            }
        }
        throw new InternalError("unknown state");
    }

    private StateTrackableDelegate(StateTrackable.State state) {
        this.theState = state;
    }

    @Override
    public StateTrackable.State getState() {
        return this.theState;
    }

    @Override
    public synchronized StateTracker getStateTracker() {
        StateTracker st = this.theTracker;
        if (st == null) {
            switch (this.theState) {
                case IMMUTABLE: {
                    st = StateTracker.ALWAYS_CURRENT;
                    break;
                }
                case STABLE: {
                    st = new StateTracker(){

                        @Override
                        public boolean isCurrent() {
                            return StateTrackableDelegate.this.theTracker == this;
                        }
                    };
                    break;
                }
                case UNTRACKABLE: 
                case DYNAMIC: {
                    st = StateTracker.NEVER_CURRENT;
                }
            }
            this.theTracker = st;
        }
        return st;
    }

    public synchronized void setImmutable() {
        if (this.theState == StateTrackable.State.UNTRACKABLE || this.theState == StateTrackable.State.DYNAMIC) {
            throw new IllegalStateException("UNTRACKABLE or DYNAMIC objects cannot become IMMUTABLE");
        }
        this.theState = StateTrackable.State.IMMUTABLE;
        this.theTracker = null;
    }

    public synchronized void setUntrackable() {
        if (this.theState == StateTrackable.State.IMMUTABLE) {
            throw new IllegalStateException("IMMUTABLE objects cannot become UNTRACKABLE");
        }
        this.theState = StateTrackable.State.UNTRACKABLE;
        this.theTracker = null;
    }

    public synchronized void addDynamicAgent() {
        if (this.theState == StateTrackable.State.IMMUTABLE) {
            throw new IllegalStateException("Cannot change state from IMMUTABLE");
        }
        ++this.numDynamicAgents;
        if (this.theState == StateTrackable.State.STABLE) {
            this.theState = StateTrackable.State.DYNAMIC;
            this.theTracker = null;
        }
    }

    protected synchronized void removeDynamicAgent() {
        if (--this.numDynamicAgents == 0 && this.theState == StateTrackable.State.DYNAMIC) {
            this.theState = StateTrackable.State.STABLE;
            this.theTracker = null;
        }
    }

    public void markDirty() {
        this.theTracker = null;
    }
}

