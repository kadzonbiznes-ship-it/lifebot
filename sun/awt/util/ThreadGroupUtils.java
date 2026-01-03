/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.util;

public final class ThreadGroupUtils {
    private ThreadGroupUtils() {
    }

    public static ThreadGroup getRootThreadGroup() {
        ThreadGroup currentTG = Thread.currentThread().getThreadGroup();
        ThreadGroup parentTG = currentTG.getParent();
        while (parentTG != null) {
            currentTG = parentTG;
            parentTG = currentTG.getParent();
        }
        return currentTG;
    }
}

