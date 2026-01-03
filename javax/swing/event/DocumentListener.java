/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.util.EventListener;
import javax.swing.event.DocumentEvent;

public interface DocumentListener
extends EventListener {
    public void insertUpdate(DocumentEvent var1);

    public void removeUpdate(DocumentEvent var1);

    public void changedUpdate(DocumentEvent var1);
}

