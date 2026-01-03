/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.util.EventObject;

public abstract class CaretEvent
extends EventObject {
    public CaretEvent(Object source) {
        super(source);
    }

    public abstract int getDot();

    public abstract int getMark();
}

