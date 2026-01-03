/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.FocusTraversalPolicy;
import javax.swing.JInternalFrame;

public abstract class InternalFrameFocusTraversalPolicy
extends FocusTraversalPolicy {
    protected InternalFrameFocusTraversalPolicy() {
    }

    public Component getInitialComponent(JInternalFrame frame) {
        return this.getDefaultComponent(frame);
    }
}

