/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;

interface EventFilter {
    public FilterAction acceptEvent(AWTEvent var1);

    public static enum FilterAction {
        ACCEPT,
        REJECT,
        ACCEPT_IMMEDIATELY;

    }
}

