/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.EventListener;

public class AWTEventMulticaster
implements ComponentListener,
ContainerListener,
FocusListener,
KeyListener,
MouseListener,
MouseMotionListener,
WindowListener,
WindowFocusListener,
WindowStateListener,
ActionListener,
ItemListener,
AdjustmentListener,
TextListener,
InputMethodListener,
HierarchyListener,
HierarchyBoundsListener,
MouseWheelListener {
    protected final EventListener a;
    protected final EventListener b;

    protected AWTEventMulticaster(EventListener a, EventListener b) {
        this.a = a;
        this.b = b;
    }

    protected EventListener remove(EventListener oldl) {
        if (oldl == this.a) {
            return this.b;
        }
        if (oldl == this.b) {
            return this.a;
        }
        EventListener a2 = AWTEventMulticaster.removeInternal(this.a, oldl);
        EventListener b2 = AWTEventMulticaster.removeInternal(this.b, oldl);
        if (a2 == this.a && b2 == this.b) {
            return this;
        }
        return AWTEventMulticaster.addInternal(a2, b2);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        ((ComponentListener)this.a).componentResized(e);
        ((ComponentListener)this.b).componentResized(e);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        ((ComponentListener)this.a).componentMoved(e);
        ((ComponentListener)this.b).componentMoved(e);
    }

    @Override
    public void componentShown(ComponentEvent e) {
        ((ComponentListener)this.a).componentShown(e);
        ((ComponentListener)this.b).componentShown(e);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        ((ComponentListener)this.a).componentHidden(e);
        ((ComponentListener)this.b).componentHidden(e);
    }

    @Override
    public void componentAdded(ContainerEvent e) {
        ((ContainerListener)this.a).componentAdded(e);
        ((ContainerListener)this.b).componentAdded(e);
    }

    @Override
    public void componentRemoved(ContainerEvent e) {
        ((ContainerListener)this.a).componentRemoved(e);
        ((ContainerListener)this.b).componentRemoved(e);
    }

    @Override
    public void focusGained(FocusEvent e) {
        ((FocusListener)this.a).focusGained(e);
        ((FocusListener)this.b).focusGained(e);
    }

    @Override
    public void focusLost(FocusEvent e) {
        ((FocusListener)this.a).focusLost(e);
        ((FocusListener)this.b).focusLost(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        ((KeyListener)this.a).keyTyped(e);
        ((KeyListener)this.b).keyTyped(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        ((KeyListener)this.a).keyPressed(e);
        ((KeyListener)this.b).keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        ((KeyListener)this.a).keyReleased(e);
        ((KeyListener)this.b).keyReleased(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        ((MouseListener)this.a).mouseClicked(e);
        ((MouseListener)this.b).mouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        ((MouseListener)this.a).mousePressed(e);
        ((MouseListener)this.b).mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        ((MouseListener)this.a).mouseReleased(e);
        ((MouseListener)this.b).mouseReleased(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        ((MouseListener)this.a).mouseEntered(e);
        ((MouseListener)this.b).mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        ((MouseListener)this.a).mouseExited(e);
        ((MouseListener)this.b).mouseExited(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        ((MouseMotionListener)this.a).mouseDragged(e);
        ((MouseMotionListener)this.b).mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        ((MouseMotionListener)this.a).mouseMoved(e);
        ((MouseMotionListener)this.b).mouseMoved(e);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        ((WindowListener)this.a).windowOpened(e);
        ((WindowListener)this.b).windowOpened(e);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        ((WindowListener)this.a).windowClosing(e);
        ((WindowListener)this.b).windowClosing(e);
    }

    @Override
    public void windowClosed(WindowEvent e) {
        ((WindowListener)this.a).windowClosed(e);
        ((WindowListener)this.b).windowClosed(e);
    }

    @Override
    public void windowIconified(WindowEvent e) {
        ((WindowListener)this.a).windowIconified(e);
        ((WindowListener)this.b).windowIconified(e);
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        ((WindowListener)this.a).windowDeiconified(e);
        ((WindowListener)this.b).windowDeiconified(e);
    }

    @Override
    public void windowActivated(WindowEvent e) {
        ((WindowListener)this.a).windowActivated(e);
        ((WindowListener)this.b).windowActivated(e);
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        ((WindowListener)this.a).windowDeactivated(e);
        ((WindowListener)this.b).windowDeactivated(e);
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        ((WindowStateListener)this.a).windowStateChanged(e);
        ((WindowStateListener)this.b).windowStateChanged(e);
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        ((WindowFocusListener)this.a).windowGainedFocus(e);
        ((WindowFocusListener)this.b).windowGainedFocus(e);
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        ((WindowFocusListener)this.a).windowLostFocus(e);
        ((WindowFocusListener)this.b).windowLostFocus(e);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ((ActionListener)this.a).actionPerformed(e);
        ((ActionListener)this.b).actionPerformed(e);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        ((ItemListener)this.a).itemStateChanged(e);
        ((ItemListener)this.b).itemStateChanged(e);
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        ((AdjustmentListener)this.a).adjustmentValueChanged(e);
        ((AdjustmentListener)this.b).adjustmentValueChanged(e);
    }

    @Override
    public void textValueChanged(TextEvent e) {
        ((TextListener)this.a).textValueChanged(e);
        ((TextListener)this.b).textValueChanged(e);
    }

    @Override
    public void inputMethodTextChanged(InputMethodEvent e) {
        ((InputMethodListener)this.a).inputMethodTextChanged(e);
        ((InputMethodListener)this.b).inputMethodTextChanged(e);
    }

    @Override
    public void caretPositionChanged(InputMethodEvent e) {
        ((InputMethodListener)this.a).caretPositionChanged(e);
        ((InputMethodListener)this.b).caretPositionChanged(e);
    }

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        ((HierarchyListener)this.a).hierarchyChanged(e);
        ((HierarchyListener)this.b).hierarchyChanged(e);
    }

    @Override
    public void ancestorMoved(HierarchyEvent e) {
        ((HierarchyBoundsListener)this.a).ancestorMoved(e);
        ((HierarchyBoundsListener)this.b).ancestorMoved(e);
    }

    @Override
    public void ancestorResized(HierarchyEvent e) {
        ((HierarchyBoundsListener)this.a).ancestorResized(e);
        ((HierarchyBoundsListener)this.b).ancestorResized(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        ((MouseWheelListener)this.a).mouseWheelMoved(e);
        ((MouseWheelListener)this.b).mouseWheelMoved(e);
    }

    public static ComponentListener add(ComponentListener a, ComponentListener b) {
        return (ComponentListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static ContainerListener add(ContainerListener a, ContainerListener b) {
        return (ContainerListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static FocusListener add(FocusListener a, FocusListener b) {
        return (FocusListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static KeyListener add(KeyListener a, KeyListener b) {
        return (KeyListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static MouseListener add(MouseListener a, MouseListener b) {
        return (MouseListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static MouseMotionListener add(MouseMotionListener a, MouseMotionListener b) {
        return (MouseMotionListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static WindowListener add(WindowListener a, WindowListener b) {
        return (WindowListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static WindowStateListener add(WindowStateListener a, WindowStateListener b) {
        return (WindowStateListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static WindowFocusListener add(WindowFocusListener a, WindowFocusListener b) {
        return (WindowFocusListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static ActionListener add(ActionListener a, ActionListener b) {
        return (ActionListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static ItemListener add(ItemListener a, ItemListener b) {
        return (ItemListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static AdjustmentListener add(AdjustmentListener a, AdjustmentListener b) {
        return (AdjustmentListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static TextListener add(TextListener a, TextListener b) {
        return (TextListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static InputMethodListener add(InputMethodListener a, InputMethodListener b) {
        return (InputMethodListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static HierarchyListener add(HierarchyListener a, HierarchyListener b) {
        return (HierarchyListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static HierarchyBoundsListener add(HierarchyBoundsListener a, HierarchyBoundsListener b) {
        return (HierarchyBoundsListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static MouseWheelListener add(MouseWheelListener a, MouseWheelListener b) {
        return (MouseWheelListener)AWTEventMulticaster.addInternal(a, b);
    }

    public static ComponentListener remove(ComponentListener l, ComponentListener oldl) {
        return (ComponentListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static ContainerListener remove(ContainerListener l, ContainerListener oldl) {
        return (ContainerListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static FocusListener remove(FocusListener l, FocusListener oldl) {
        return (FocusListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static KeyListener remove(KeyListener l, KeyListener oldl) {
        return (KeyListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static MouseListener remove(MouseListener l, MouseListener oldl) {
        return (MouseListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static MouseMotionListener remove(MouseMotionListener l, MouseMotionListener oldl) {
        return (MouseMotionListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static WindowListener remove(WindowListener l, WindowListener oldl) {
        return (WindowListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static WindowStateListener remove(WindowStateListener l, WindowStateListener oldl) {
        return (WindowStateListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static WindowFocusListener remove(WindowFocusListener l, WindowFocusListener oldl) {
        return (WindowFocusListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static ActionListener remove(ActionListener l, ActionListener oldl) {
        return (ActionListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static ItemListener remove(ItemListener l, ItemListener oldl) {
        return (ItemListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static AdjustmentListener remove(AdjustmentListener l, AdjustmentListener oldl) {
        return (AdjustmentListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static TextListener remove(TextListener l, TextListener oldl) {
        return (TextListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static InputMethodListener remove(InputMethodListener l, InputMethodListener oldl) {
        return (InputMethodListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static HierarchyListener remove(HierarchyListener l, HierarchyListener oldl) {
        return (HierarchyListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static HierarchyBoundsListener remove(HierarchyBoundsListener l, HierarchyBoundsListener oldl) {
        return (HierarchyBoundsListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    public static MouseWheelListener remove(MouseWheelListener l, MouseWheelListener oldl) {
        return (MouseWheelListener)AWTEventMulticaster.removeInternal(l, oldl);
    }

    protected static EventListener addInternal(EventListener a, EventListener b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return new AWTEventMulticaster(a, b);
    }

    protected static EventListener removeInternal(EventListener l, EventListener oldl) {
        if (l == oldl || l == null) {
            return null;
        }
        if (l instanceof AWTEventMulticaster) {
            return ((AWTEventMulticaster)l).remove(oldl);
        }
        return l;
    }

    protected void saveInternal(ObjectOutputStream s, String k) throws IOException {
        if (this.a instanceof AWTEventMulticaster) {
            ((AWTEventMulticaster)this.a).saveInternal(s, k);
        } else if (this.a instanceof Serializable) {
            s.writeObject(k);
            s.writeObject(this.a);
        }
        if (this.b instanceof AWTEventMulticaster) {
            ((AWTEventMulticaster)this.b).saveInternal(s, k);
        } else if (this.b instanceof Serializable) {
            s.writeObject(k);
            s.writeObject(this.b);
        }
    }

    protected static void save(ObjectOutputStream s, String k, EventListener l) throws IOException {
        if (l == null) {
            return;
        }
        if (l instanceof AWTEventMulticaster) {
            ((AWTEventMulticaster)l).saveInternal(s, k);
        } else if (l instanceof Serializable) {
            s.writeObject(k);
            s.writeObject(l);
        }
    }

    private static int getListenerCount(EventListener l, Class<?> listenerType) {
        if (l instanceof AWTEventMulticaster) {
            AWTEventMulticaster mc = (AWTEventMulticaster)l;
            return AWTEventMulticaster.getListenerCount(mc.a, listenerType) + AWTEventMulticaster.getListenerCount(mc.b, listenerType);
        }
        return listenerType.isInstance(l) ? 1 : 0;
    }

    private static int populateListenerArray(EventListener[] a, EventListener l, int index) {
        if (l instanceof AWTEventMulticaster) {
            AWTEventMulticaster mc = (AWTEventMulticaster)l;
            int lhs = AWTEventMulticaster.populateListenerArray(a, mc.a, index);
            return AWTEventMulticaster.populateListenerArray(a, mc.b, lhs);
        }
        if (a.getClass().getComponentType().isInstance(l)) {
            a[index] = l;
            return index + 1;
        }
        return index;
    }

    public static <T extends EventListener> T[] getListeners(EventListener l, Class<T> listenerType) {
        if (listenerType == null) {
            throw new NullPointerException("Listener type should not be null");
        }
        int n = AWTEventMulticaster.getListenerCount(l, listenerType);
        EventListener[] result = (EventListener[])Array.newInstance(listenerType, n);
        AWTEventMulticaster.populateListenerArray(result, l, 0);
        return result;
    }
}

