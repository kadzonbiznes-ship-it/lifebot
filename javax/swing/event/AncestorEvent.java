/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.awt.AWTEvent;
import java.awt.Container;
import javax.swing.JComponent;

public class AncestorEvent
extends AWTEvent {
    public static final int ANCESTOR_ADDED = 1;
    public static final int ANCESTOR_REMOVED = 2;
    public static final int ANCESTOR_MOVED = 3;
    Container ancestor;
    Container ancestorParent;

    public AncestorEvent(JComponent source, int id, Container ancestor, Container ancestorParent) {
        super(source, id);
        this.ancestor = ancestor;
        this.ancestorParent = ancestorParent;
    }

    public Container getAncestor() {
        return this.ancestor;
    }

    public Container getAncestorParent() {
        return this.ancestorParent;
    }

    public JComponent getComponent() {
        return (JComponent)this.getSource();
    }
}

