/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.event.InputMethodEvent;
import java.util.EventListener;

public interface InputMethodListener
extends EventListener {
    public void inputMethodTextChanged(InputMethodEvent var1);

    public void caretPositionChanged(InputMethodEvent var1);
}

