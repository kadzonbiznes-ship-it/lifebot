/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.applet.Applet;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import sun.awt.EmbeddedFrame;

class KeyboardManager {
    static KeyboardManager currentManager = new KeyboardManager();
    Hashtable<Container, Hashtable<Object, Object>> containerMap = new Hashtable();
    Hashtable<ComponentKeyStrokePair, Container> componentKeyStrokeMap = new Hashtable();

    KeyboardManager() {
    }

    public static KeyboardManager getCurrentManager() {
        return currentManager;
    }

    public static void setCurrentManager(KeyboardManager km) {
        currentManager = km;
    }

    public void registerKeyStroke(KeyStroke k, JComponent c) {
        Object tmp;
        Container topContainer = KeyboardManager.getTopAncestor(c);
        if (topContainer == null) {
            return;
        }
        Hashtable<Object, Object> keyMap = this.containerMap.get(topContainer);
        if (keyMap == null) {
            keyMap = this.registerNewTopContainer(topContainer);
        }
        if ((tmp = keyMap.get(k)) == null) {
            keyMap.put(k, c);
        } else if (tmp instanceof Vector) {
            Vector v = (Vector)tmp;
            if (!v.contains(c)) {
                v.addElement(c);
            }
        } else if (tmp instanceof JComponent) {
            if (tmp != c) {
                Vector<JComponent> v = new Vector<JComponent>();
                v.addElement((JComponent)tmp);
                v.addElement(c);
                keyMap.put(k, v);
            }
        } else {
            System.out.println("Unexpected condition in registerKeyStroke");
            Thread.dumpStack();
        }
        this.componentKeyStrokeMap.put(new ComponentKeyStrokePair(c, k), topContainer);
        if (topContainer instanceof EmbeddedFrame) {
            ((EmbeddedFrame)topContainer).registerAccelerator(k);
        }
    }

    private static Container getTopAncestor(JComponent c) {
        for (Container p = c.getParent(); p != null; p = p.getParent()) {
            if ((!(p instanceof Window) || !((Window)p).isFocusableWindow()) && !(p instanceof Applet) && !(p instanceof JInternalFrame)) continue;
            return p;
        }
        return null;
    }

    public void unregisterKeyStroke(KeyStroke ks, JComponent c) {
        ComponentKeyStrokePair ckp = new ComponentKeyStrokePair(c, ks);
        Container topContainer = this.componentKeyStrokeMap.get(ckp);
        if (topContainer == null) {
            return;
        }
        Hashtable<Object, Object> keyMap = this.containerMap.get(topContainer);
        if (keyMap == null) {
            Thread.dumpStack();
            return;
        }
        Object tmp = keyMap.get(ks);
        if (tmp == null) {
            Thread.dumpStack();
            return;
        }
        if (tmp instanceof JComponent && tmp == c) {
            keyMap.remove(ks);
        } else if (tmp instanceof Vector) {
            Vector v = (Vector)tmp;
            v.removeElement(c);
            if (v.isEmpty()) {
                keyMap.remove(ks);
            }
        }
        if (keyMap.isEmpty()) {
            this.containerMap.remove(topContainer);
        }
        this.componentKeyStrokeMap.remove(ckp);
        if (topContainer instanceof EmbeddedFrame) {
            ((EmbeddedFrame)topContainer).unregisterAccelerator(ks);
        }
    }

    public boolean fireKeyboardAction(KeyEvent e, boolean pressed, Container topAncestor) {
        Vector v;
        KeyStroke ks;
        if (e.isConsumed()) {
            System.out.println("Acquired pre-used event!");
            Thread.dumpStack();
        }
        KeyStroke ksE = null;
        if (e.getID() == 400) {
            ks = KeyStroke.getKeyStroke(e.getKeyChar());
        } else {
            if (e.getKeyCode() != e.getExtendedKeyCode()) {
                ksE = KeyStroke.getKeyStroke(e.getExtendedKeyCode(), e.getModifiers(), !pressed);
            }
            ks = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers(), !pressed);
        }
        Hashtable<Object, Object> keyMap = this.containerMap.get(topAncestor);
        if (keyMap != null) {
            Object tmp = null;
            if (ksE != null && (tmp = keyMap.get(ksE)) != null) {
                ks = ksE;
            }
            if (tmp == null) {
                tmp = keyMap.get(ks);
            }
            if (tmp != null) {
                if (tmp instanceof JComponent) {
                    JComponent c = (JComponent)tmp;
                    if (c.isShowing() && c.isEnabled()) {
                        this.fireBinding(c, ks, e, pressed);
                    }
                } else if (tmp instanceof Vector) {
                    Vector v2 = (Vector)tmp;
                    for (int counter = v2.size() - 1; counter >= 0; --counter) {
                        JComponent c = (JComponent)v2.elementAt(counter);
                        if (!c.isShowing() || !c.isEnabled()) continue;
                        this.fireBinding(c, ks, e, pressed);
                        if (!e.isConsumed()) continue;
                        return true;
                    }
                } else {
                    System.out.println("Unexpected condition in fireKeyboardAction " + String.valueOf(tmp));
                    Thread.dumpStack();
                }
            }
        }
        if (e.isConsumed()) {
            return true;
        }
        if (keyMap != null && (v = (Vector)keyMap.get(JMenuBar.class)) != null) {
            Enumeration iter = v.elements();
            while (iter.hasMoreElements()) {
                boolean extended;
                JMenuBar mb = (JMenuBar)iter.nextElement();
                if (!mb.isShowing() || !mb.isEnabled()) continue;
                boolean bl = extended = ksE != null && !ksE.equals(ks);
                if (extended) {
                    this.fireBinding(mb, ksE, e, pressed);
                }
                if (!extended || !e.isConsumed()) {
                    this.fireBinding(mb, ks, e, pressed);
                }
                if (!e.isConsumed()) continue;
                return true;
            }
        }
        return e.isConsumed();
    }

    void fireBinding(JComponent c, KeyStroke ks, KeyEvent e, boolean pressed) {
        if (c.processKeyBinding(ks, e, 2, pressed)) {
            e.consume();
        }
    }

    public void registerMenuBar(JMenuBar mb) {
        Vector<JMenuBar> menuBars;
        Container top = KeyboardManager.getTopAncestor(mb);
        if (top == null) {
            return;
        }
        Hashtable<Object, Object> keyMap = this.containerMap.get(top);
        if (keyMap == null) {
            keyMap = this.registerNewTopContainer(top);
        }
        if ((menuBars = (Vector<JMenuBar>)keyMap.get(JMenuBar.class)) == null) {
            menuBars = new Vector<JMenuBar>();
            keyMap.put(JMenuBar.class, menuBars);
        }
        if (!menuBars.contains(mb)) {
            menuBars.addElement(mb);
        }
    }

    public void unregisterMenuBar(JMenuBar mb) {
        Vector v;
        Container topContainer = KeyboardManager.getTopAncestor(mb);
        if (topContainer == null) {
            return;
        }
        Hashtable<Object, Object> keyMap = this.containerMap.get(topContainer);
        if (keyMap != null && (v = (Vector)keyMap.get(JMenuBar.class)) != null) {
            v.removeElement(mb);
            if (v.isEmpty()) {
                keyMap.remove(JMenuBar.class);
                if (keyMap.isEmpty()) {
                    this.containerMap.remove(topContainer);
                }
            }
        }
    }

    protected Hashtable<Object, Object> registerNewTopContainer(Container topContainer) {
        Hashtable<Object, Object> keyMap = new Hashtable<Object, Object>();
        this.containerMap.put(topContainer, keyMap);
        return keyMap;
    }

    static class ComponentKeyStrokePair {
        Object component;
        Object keyStroke;

        public ComponentKeyStrokePair(Object comp, Object key) {
            this.component = comp;
            this.keyStroke = key;
        }

        public boolean equals(Object o) {
            if (!(o instanceof ComponentKeyStrokePair)) {
                return false;
            }
            ComponentKeyStrokePair ckp = (ComponentKeyStrokePair)o;
            return this.component.equals(ckp.component) && this.keyStroke.equals(ckp.keyStroke);
        }

        public int hashCode() {
            return this.component.hashCode() * this.keyStroke.hashCode();
        }
    }
}

