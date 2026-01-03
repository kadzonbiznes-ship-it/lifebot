/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.AWTEvent;
import java.awt.Component;

public class UngrabEvent
extends AWTEvent {
    private static final int UNGRAB_EVENT_ID = 1998;

    public UngrabEvent(Component source) {
        super(source, 1998);
    }

    @Override
    public String toString() {
        return "sun.awt.UngrabEvent[" + String.valueOf(this.getSource()) + "]";
    }
}

