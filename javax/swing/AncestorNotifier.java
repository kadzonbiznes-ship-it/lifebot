/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.EventListenerList;

class AncestorNotifier
implements ComponentListener,
PropertyChangeListener,
Serializable {
    transient Component firstInvisibleAncestor;
    EventListenerList listenerList = new EventListenerList();
    JComponent root;

    AncestorNotifier(JComponent root) {
        this.root = root;
        this.addListeners(root, true);
    }

    void addAncestorListener(AncestorListener l) {
        this.listenerList.add(AncestorListener.class, l);
    }

    void removeAncestorListener(AncestorListener l) {
        this.listenerList.remove(AncestorListener.class, l);
    }

    AncestorListener[] getAncestorListeners() {
        return (AncestorListener[])this.listenerList.getListeners(AncestorListener.class);
    }

    protected void fireAncestorAdded(JComponent source, int id, Container ancestor, Container ancestorParent) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != AncestorListener.class) continue;
            AncestorEvent ancestorEvent = new AncestorEvent(source, id, ancestor, ancestorParent);
            ((AncestorListener)listeners[i + 1]).ancestorAdded(ancestorEvent);
        }
    }

    protected void fireAncestorRemoved(JComponent source, int id, Container ancestor, Container ancestorParent) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != AncestorListener.class) continue;
            AncestorEvent ancestorEvent = new AncestorEvent(source, id, ancestor, ancestorParent);
            ((AncestorListener)listeners[i + 1]).ancestorRemoved(ancestorEvent);
        }
    }

    protected void fireAncestorMoved(JComponent source, int id, Container ancestor, Container ancestorParent) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != AncestorListener.class) continue;
            AncestorEvent ancestorEvent = new AncestorEvent(source, id, ancestor, ancestorParent);
            ((AncestorListener)listeners[i + 1]).ancestorMoved(ancestorEvent);
        }
    }

    void removeAllListeners() {
        this.removeListeners(this.root);
    }

    void addListeners(Component ancestor, boolean addToFirst) {
        this.firstInvisibleAncestor = null;
        Component a = ancestor;
        while (this.firstInvisibleAncestor == null) {
            if (addToFirst || a != ancestor) {
                a.addComponentListener(this);
                if (a instanceof JComponent) {
                    JComponent jAncestor = (JComponent)a;
                    jAncestor.addPropertyChangeListener(this);
                }
            }
            if (!a.isVisible() || a.getParent() == null || a instanceof Window) {
                this.firstInvisibleAncestor = a;
            }
            a = a.getParent();
        }
        if (this.firstInvisibleAncestor instanceof Window && this.firstInvisibleAncestor.isVisible()) {
            this.firstInvisibleAncestor = null;
        }
    }

    void removeListeners(Component ancestor) {
        for (Component a = ancestor; a != null; a = a.getParent()) {
            a.removeComponentListener(this);
            if (a instanceof JComponent) {
                JComponent jAncestor = (JComponent)a;
                jAncestor.removePropertyChangeListener(this);
            }
            if (a == this.firstInvisibleAncestor || a instanceof Window) break;
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        Component source = e.getComponent();
        this.fireAncestorMoved(this.root, 3, (Container)source, source.getParent());
    }

    @Override
    public void componentShown(ComponentEvent e) {
        Component ancestor = e.getComponent();
        if (ancestor == this.firstInvisibleAncestor) {
            this.addListeners(ancestor, false);
            if (this.firstInvisibleAncestor == null) {
                this.fireAncestorAdded(this.root, 1, (Container)ancestor, ancestor.getParent());
            }
        }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        boolean needsNotify;
        Component ancestor = e.getComponent();
        boolean bl = needsNotify = this.firstInvisibleAncestor == null;
        if (!(ancestor instanceof Window)) {
            this.removeListeners(ancestor.getParent());
        }
        this.firstInvisibleAncestor = ancestor;
        if (needsNotify) {
            this.fireAncestorRemoved(this.root, 2, (Container)ancestor, ancestor.getParent());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String s = evt.getPropertyName();
        if (s != null && (s.equals("parent") || s.equals("ancestor"))) {
            JComponent component = (JComponent)evt.getSource();
            if (evt.getNewValue() != null) {
                if (component == this.firstInvisibleAncestor) {
                    this.addListeners(component, false);
                    if (this.firstInvisibleAncestor == null) {
                        this.fireAncestorAdded(this.root, 1, component, component.getParent());
                    }
                }
            } else {
                boolean needsNotify = this.firstInvisibleAncestor == null;
                Container oldParent = (Container)evt.getOldValue();
                this.removeListeners(oldParent);
                this.firstInvisibleAncestor = component;
                if (needsNotify) {
                    this.fireAncestorRemoved(this.root, 2, component, oldParent);
                }
            }
        }
    }
}

