/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

public interface StateTracker {
    public static final StateTracker ALWAYS_CURRENT = new StateTracker(){

        @Override
        public boolean isCurrent() {
            return true;
        }
    };
    public static final StateTracker NEVER_CURRENT = new StateTracker(){

        @Override
        public boolean isCurrent() {
            return false;
        }
    };

    public boolean isCurrent();
}

