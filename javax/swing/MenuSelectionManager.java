/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.swing.SwingUtilities2;

public class MenuSelectionManager {
    private Vector<MenuElement> selection = new Vector();
    private static final boolean TRACE = false;
    private static final boolean VERBOSE = false;
    private static final boolean DEBUG = false;
    private static final StringBuilder MENU_SELECTION_MANAGER_KEY = new StringBuilder("javax.swing.MenuSelectionManager");
    protected transient ChangeEvent changeEvent = null;
    protected EventListenerList listenerList = new EventListenerList();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static MenuSelectionManager defaultManager() {
        StringBuilder stringBuilder = MENU_SELECTION_MANAGER_KEY;
        synchronized (stringBuilder) {
            AppContext context = AppContext.getAppContext();
            MenuSelectionManager msm = (MenuSelectionManager)context.get(MENU_SELECTION_MANAGER_KEY);
            if (msm == null) {
                msm = new MenuSelectionManager();
                context.put(MENU_SELECTION_MANAGER_KEY, msm);
                Object o = context.get(SwingUtilities2.MENU_SELECTION_MANAGER_LISTENER_KEY);
                if (o instanceof ChangeListener) {
                    ChangeListener listener = (ChangeListener)o;
                    msm.addChangeListener(listener);
                }
            }
            return msm;
        }
    }

    public void setSelectedPath(MenuElement[] path) {
        int i;
        int currentSelectionCount = this.selection.size();
        int firstDifference = 0;
        if (path == null) {
            path = new MenuElement[]{};
        }
        int c = path.length;
        for (i = 0; i < c && i < currentSelectionCount && this.selection.elementAt(i) == path[i]; ++i) {
            ++firstDifference;
        }
        for (i = currentSelectionCount - 1; i >= firstDifference; --i) {
            MenuElement me = this.selection.elementAt(i);
            this.selection.removeElementAt(i);
            me.menuSelectionChanged(false);
        }
        c = path.length;
        for (i = firstDifference; i < c; ++i) {
            if (path[i] == null) continue;
            this.selection.addElement(path[i]);
            path[i].menuSelectionChanged(true);
        }
        this.fireStateChanged();
    }

    public MenuElement[] getSelectedPath() {
        MenuElement[] res = new MenuElement[this.selection.size()];
        int c = this.selection.size();
        for (int i = 0; i < c; ++i) {
            res[i] = this.selection.elementAt(i);
        }
        return res;
    }

    public void clearSelectedPath() {
        if (this.selection.size() > 0) {
            this.setSelectedPath(null);
        }
    }

    public void addChangeListener(ChangeListener l) {
        this.listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        this.listenerList.remove(ChangeListener.class, l);
    }

    public ChangeListener[] getChangeListeners() {
        return (ChangeListener[])this.listenerList.getListeners(ChangeListener.class);
    }

    protected void fireStateChanged() {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ChangeListener.class) continue;
            if (this.changeEvent == null) {
                this.changeEvent = new ChangeEvent(this);
            }
            ((ChangeListener)listeners[i + 1]).stateChanged(this.changeEvent);
        }
    }

    public void processMouseEvent(MouseEvent event) {
        Point p = event.getPoint();
        Component source = event.getComponent();
        if (source != null && !source.isShowing()) {
            return;
        }
        int type = event.getID();
        int modifiers = event.getModifiers();
        if ((type == 504 || type == 505) && (modifiers & 0x1C) != 0) {
            return;
        }
        if (source != null) {
            SwingUtilities.convertPointToScreen(p, source);
        }
        int screenX = p.x;
        int screenY = p.y;
        Vector tmp = (Vector)this.selection.clone();
        int selectionSize = tmp.size();
        boolean success = false;
        for (int i = selectionSize - 1; i >= 0 && !success; --i) {
            MenuElement menuElement = (MenuElement)tmp.elementAt(i);
            MenuElement[] subElements = menuElement.getSubElements();
            MenuElement[] path = null;
            int d = subElements.length;
            for (int j = 0; j < d && !success; ++j) {
                int cHeight;
                int cWidth;
                Component mc;
                if (subElements[j] == null || !(mc = subElements[j].getComponent()).isShowing()) continue;
                if (mc instanceof JComponent) {
                    cWidth = mc.getWidth();
                    cHeight = mc.getHeight();
                } else {
                    Rectangle r2 = mc.getBounds();
                    cWidth = r2.width;
                    cHeight = r2.height;
                }
                p.x = screenX;
                p.y = screenY;
                SwingUtilities.convertPointFromScreen(p, mc);
                if (p.x < 0 || p.x >= cWidth || p.y < 0 || p.y >= cHeight) continue;
                if (path == null) {
                    path = new MenuElement[i + 2];
                    for (int k = 0; k <= i; ++k) {
                        path[k] = (MenuElement)tmp.elementAt(k);
                    }
                }
                path[i + 1] = subElements[j];
                MenuElement[] currentSelection = this.getSelectedPath();
                if (currentSelection[currentSelection.length - 1] != path[i + 1] && (currentSelection.length < 2 || currentSelection[currentSelection.length - 2] != path[i + 1])) {
                    Component oldMC = currentSelection[currentSelection.length - 1].getComponent();
                    MouseEvent exitEvent = new MouseEvent(oldMC, 505, event.getWhen(), event.getModifiers(), p.x, p.y, event.getXOnScreen(), event.getYOnScreen(), event.getClickCount(), event.isPopupTrigger(), 0);
                    AWTAccessor.MouseEventAccessor meAccessor = AWTAccessor.getMouseEventAccessor();
                    meAccessor.setCausedByTouchEvent(exitEvent, meAccessor.isCausedByTouchEvent(event));
                    currentSelection[currentSelection.length - 1].processMouseEvent(exitEvent, path, this);
                    MouseEvent enterEvent = new MouseEvent(mc, 504, event.getWhen(), event.getModifiers(), p.x, p.y, event.getXOnScreen(), event.getYOnScreen(), event.getClickCount(), event.isPopupTrigger(), 0);
                    meAccessor.setCausedByTouchEvent(enterEvent, meAccessor.isCausedByTouchEvent(event));
                    subElements[j].processMouseEvent(enterEvent, path, this);
                }
                MouseEvent mouseEvent = new MouseEvent(mc, event.getID(), event.getWhen(), event.getModifiers(), p.x, p.y, event.getXOnScreen(), event.getYOnScreen(), event.getClickCount(), event.isPopupTrigger(), 0);
                AWTAccessor.MouseEventAccessor meAccessor = AWTAccessor.getMouseEventAccessor();
                meAccessor.setCausedByTouchEvent(mouseEvent, meAccessor.isCausedByTouchEvent(event));
                subElements[j].processMouseEvent(mouseEvent, path, this);
                success = true;
                event.consume();
            }
        }
    }

    private void printMenuElementArray(MenuElement[] path) {
        this.printMenuElementArray(path, false);
    }

    private void printMenuElementArray(MenuElement[] path, boolean dumpStack) {
        System.out.println("Path is(");
        int j = path.length;
        for (int i = 0; i < j; ++i) {
            for (int k = 0; k <= i; ++k) {
                System.out.print("  ");
            }
            MenuElement me = path[i];
            if (me instanceof JMenuItem) {
                System.out.println(((JMenuItem)me).getText() + ", ");
                continue;
            }
            if (me instanceof JMenuBar) {
                System.out.println("JMenuBar, ");
                continue;
            }
            if (me instanceof JPopupMenu) {
                System.out.println("JPopupMenu, ");
                continue;
            }
            if (me == null) {
                System.out.println("NULL , ");
                continue;
            }
            System.out.println(String.valueOf(me) + ", ");
        }
        System.out.println(")");
        if (dumpStack) {
            Thread.dumpStack();
        }
    }

    public Component componentForPoint(Component source, Point sourcePoint) {
        Point p = sourcePoint;
        SwingUtilities.convertPointToScreen(p, source);
        int screenX = p.x;
        int screenY = p.y;
        Vector tmp = (Vector)this.selection.clone();
        int selectionSize = tmp.size();
        for (int i = selectionSize - 1; i >= 0; --i) {
            MenuElement menuElement = (MenuElement)tmp.elementAt(i);
            MenuElement[] subElements = menuElement.getSubElements();
            int d = subElements.length;
            for (int j = 0; j < d; ++j) {
                int cHeight;
                int cWidth;
                Component mc;
                if (subElements[j] == null || !(mc = subElements[j].getComponent()).isShowing()) continue;
                if (mc instanceof JComponent) {
                    cWidth = mc.getWidth();
                    cHeight = mc.getHeight();
                } else {
                    Rectangle r2 = mc.getBounds();
                    cWidth = r2.width;
                    cHeight = r2.height;
                }
                p.x = screenX;
                p.y = screenY;
                SwingUtilities.convertPointFromScreen(p, mc);
                if (p.x < 0 || p.x >= cWidth || p.y < 0 || p.y >= cHeight) continue;
                return mc;
            }
        }
        return null;
    }

    public void processKeyEvent(KeyEvent e) {
        MenuElement[] path;
        MenuElement[] sel2 = new MenuElement[]{};
        int selSize = (sel2 = this.selection.toArray(sel2)).length;
        if (selSize < 1) {
            return;
        }
        for (int i = selSize - 1; i >= 0; --i) {
            MenuElement elem = sel2[i];
            MenuElement[] subs = elem.getSubElements();
            path = null;
            for (int j = 0; j < subs.length; ++j) {
                if (subs[j] == null || !subs[j].getComponent().isShowing() || !subs[j].getComponent().isEnabled()) continue;
                if (path == null) {
                    path = new MenuElement[i + 2];
                    System.arraycopy(sel2, 0, path, 0, i + 1);
                }
                path[i + 1] = subs[j];
                subs[j].processKeyEvent(e, path, this);
                if (!e.isConsumed()) continue;
                return;
            }
        }
        path = new MenuElement[]{sel2[0]};
        path[0].processKeyEvent(e, path, this);
        if (e.isConsumed()) {
            return;
        }
    }

    public boolean isComponentPartOfCurrentMenu(Component c) {
        if (this.selection.size() > 0) {
            MenuElement me = this.selection.elementAt(0);
            return this.isComponentPartOfCurrentMenu(me, c);
        }
        return false;
    }

    private boolean isComponentPartOfCurrentMenu(MenuElement root, Component c) {
        if (root == null) {
            return false;
        }
        if (root.getComponent() == c) {
            return true;
        }
        MenuElement[] children = root.getSubElements();
        int d = children.length;
        for (int i = 0; i < d; ++i) {
            if (!this.isComponentPartOfCurrentMenu(children[i], c)) continue;
            return true;
        }
        return false;
    }
}

