/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;

public abstract class FocusTraversalPolicy {
    protected FocusTraversalPolicy() {
    }

    public abstract Component getComponentAfter(Container var1, Component var2);

    public abstract Component getComponentBefore(Container var1, Component var2);

    public abstract Component getFirstComponent(Container var1);

    public abstract Component getLastComponent(Container var1);

    public abstract Component getDefaultComponent(Container var1);

    public Component getInitialComponent(Window window) {
        if (window == null) {
            throw new IllegalArgumentException("window cannot be equal to null.");
        }
        Component def = this.getDefaultComponent(window);
        if (def == null && window.isFocusableWindow()) {
            def = window;
        }
        return def;
    }
}

