/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.applet.Applet;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleStateSet;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.CellRendererPane;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.plaf.UIResource;
import javax.swing.text.View;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.reflect.misc.ReflectUtil;
import sun.security.action.GetPropertyAction;
import sun.swing.SwingUtilities2;

public class SwingUtilities
implements SwingConstants {
    private static boolean canAccessEventQueue = false;
    private static boolean eventQueueTested = false;
    private static boolean suppressDropSupport;
    private static boolean checkedSuppressDropSupport;
    private static final Object sharedOwnerFrameKey;

    private static boolean getSuppressDropTarget() {
        if (!checkedSuppressDropSupport) {
            suppressDropSupport = Boolean.parseBoolean(AccessController.doPrivileged(new GetPropertyAction("suppressSwingDropSupport")));
            checkedSuppressDropSupport = true;
        }
        return suppressDropSupport;
    }

    static void installSwingDropTargetAsNecessary(Component c, TransferHandler t) {
        DropTarget dropHandler;
        if (!SwingUtilities.getSuppressDropTarget() && ((dropHandler = c.getDropTarget()) == null || dropHandler instanceof UIResource)) {
            if (t == null) {
                c.setDropTarget(null);
            } else if (!GraphicsEnvironment.isHeadless()) {
                c.setDropTarget(new TransferHandler.SwingDropTarget(c));
            }
        }
    }

    public static final boolean isRectangleContainingRectangle(Rectangle a, Rectangle b) {
        return b.x >= a.x && b.x + b.width <= a.x + a.width && b.y >= a.y && b.y + b.height <= a.y + a.height;
    }

    public static Rectangle getLocalBounds(Component aComponent) {
        Rectangle b = new Rectangle(aComponent.getBounds());
        b.y = 0;
        b.x = 0;
        return b;
    }

    public static Window getWindowAncestor(Component c) {
        for (Container p = c.getParent(); p != null; p = p.getParent()) {
            if (!(p instanceof Window)) continue;
            return (Window)p;
        }
        return null;
    }

    static Point convertScreenLocationToParent(Container parent, int x, int y) {
        for (Container p = parent; p != null; p = p.getParent()) {
            if (!(p instanceof Window)) continue;
            Point point = new Point(x, y);
            SwingUtilities.convertPointFromScreen(point, parent);
            return point;
        }
        throw new Error("convertScreenLocationToParent: no window ancestor");
    }

    public static Point convertPoint(Component source, Point aPoint, Component destination) {
        if (source == null && destination == null) {
            return aPoint;
        }
        if (source == null && (source = SwingUtilities.getWindowAncestor(destination)) == null) {
            throw new Error("Source component not connected to component tree hierarchy");
        }
        Point p = new Point(aPoint);
        SwingUtilities.convertPointToScreen(p, source);
        if (destination == null && (destination = SwingUtilities.getWindowAncestor(source)) == null) {
            throw new Error("Destination component not connected to component tree hierarchy");
        }
        SwingUtilities.convertPointFromScreen(p, destination);
        return p;
    }

    public static Point convertPoint(Component source, int x, int y, Component destination) {
        Point point = new Point(x, y);
        return SwingUtilities.convertPoint(source, point, destination);
    }

    public static Rectangle convertRectangle(Component source, Rectangle aRectangle, Component destination) {
        Point point = new Point(aRectangle.x, aRectangle.y);
        point = SwingUtilities.convertPoint(source, point, destination);
        return new Rectangle(point.x, point.y, aRectangle.width, aRectangle.height);
    }

    public static Container getAncestorOfClass(Class<?> c, Component comp) {
        Container parent;
        if (comp == null || c == null) {
            return null;
        }
        for (parent = comp.getParent(); parent != null && !c.isInstance(parent); parent = parent.getParent()) {
        }
        return parent;
    }

    public static Container getAncestorNamed(String name, Component comp) {
        Container parent;
        if (comp == null || name == null) {
            return null;
        }
        for (parent = comp.getParent(); parent != null && !name.equals(parent.getName()); parent = parent.getParent()) {
        }
        return parent;
    }

    public static Component getDeepestComponentAt(Component parent, int x, int y) {
        if (!parent.contains(x, y)) {
            return null;
        }
        if (parent instanceof Container) {
            Component[] components;
            for (Component comp : components = ((Container)parent).getComponents()) {
                if (comp == null || !comp.isVisible()) continue;
                Point loc = comp.getLocation();
                if ((comp = comp instanceof Container ? SwingUtilities.getDeepestComponentAt(comp, x - loc.x, y - loc.y) : comp.getComponentAt(x - loc.x, y - loc.y)) == null || !comp.isVisible()) continue;
                return comp;
            }
        }
        return parent;
    }

    public static MouseEvent convertMouseEvent(Component source, MouseEvent sourceEvent, Component destination) {
        MouseEvent newEvent;
        Point p = SwingUtilities.convertPoint(source, new Point(sourceEvent.getX(), sourceEvent.getY()), destination);
        Component newSource = destination != null ? destination : source;
        if (sourceEvent instanceof MouseWheelEvent) {
            MouseWheelEvent sourceWheelEvent = (MouseWheelEvent)sourceEvent;
            newEvent = new MouseWheelEvent(newSource, sourceWheelEvent.getID(), sourceWheelEvent.getWhen(), sourceWheelEvent.getModifiers() | sourceWheelEvent.getModifiersEx(), p.x, p.y, sourceWheelEvent.getXOnScreen(), sourceWheelEvent.getYOnScreen(), sourceWheelEvent.getClickCount(), sourceWheelEvent.isPopupTrigger(), sourceWheelEvent.getScrollType(), sourceWheelEvent.getScrollAmount(), sourceWheelEvent.getWheelRotation(), sourceWheelEvent.getPreciseWheelRotation());
        } else if (sourceEvent instanceof MenuDragMouseEvent) {
            MenuDragMouseEvent sourceMenuDragEvent = (MenuDragMouseEvent)sourceEvent;
            newEvent = new MenuDragMouseEvent(newSource, sourceMenuDragEvent.getID(), sourceMenuDragEvent.getWhen(), sourceMenuDragEvent.getModifiers() | sourceMenuDragEvent.getModifiersEx(), p.x, p.y, sourceMenuDragEvent.getXOnScreen(), sourceMenuDragEvent.getYOnScreen(), sourceMenuDragEvent.getClickCount(), sourceMenuDragEvent.isPopupTrigger(), sourceMenuDragEvent.getPath(), sourceMenuDragEvent.getMenuSelectionManager());
        } else {
            newEvent = new MouseEvent(newSource, sourceEvent.getID(), sourceEvent.getWhen(), sourceEvent.getModifiers() | sourceEvent.getModifiersEx(), p.x, p.y, sourceEvent.getXOnScreen(), sourceEvent.getYOnScreen(), sourceEvent.getClickCount(), sourceEvent.isPopupTrigger(), sourceEvent.getButton());
            AWTAccessor.MouseEventAccessor meAccessor = AWTAccessor.getMouseEventAccessor();
            meAccessor.setCausedByTouchEvent(newEvent, meAccessor.isCausedByTouchEvent(sourceEvent));
        }
        return newEvent;
    }

    public static void convertPointToScreen(Point p, Component c) {
        do {
            int y;
            int x;
            if (c instanceof JComponent) {
                x = c.getX();
                y = c.getY();
            } else if (c instanceof Applet || c instanceof Window) {
                try {
                    Point pp = c.getLocationOnScreen();
                    x = pp.x;
                    y = pp.y;
                }
                catch (IllegalComponentStateException icse) {
                    x = c.getX();
                    y = c.getY();
                }
            } else {
                x = c.getX();
                y = c.getY();
            }
            p.x += x;
            p.y += y;
        } while (!(c instanceof Window) && !(c instanceof Applet) && (c = c.getParent()) != null);
    }

    public static void convertPointFromScreen(Point p, Component c) {
        do {
            int y;
            int x;
            if (c instanceof JComponent) {
                x = c.getX();
                y = c.getY();
            } else if (c instanceof Applet || c instanceof Window) {
                try {
                    Point pp = c.getLocationOnScreen();
                    x = pp.x;
                    y = pp.y;
                }
                catch (IllegalComponentStateException icse) {
                    x = c.getX();
                    y = c.getY();
                }
            } else {
                x = c.getX();
                y = c.getY();
            }
            p.x -= x;
            p.y -= y;
        } while (!(c instanceof Window) && !(c instanceof Applet) && (c = c.getParent()) != null);
    }

    public static Window windowForComponent(Component c) {
        return SwingUtilities.getWindowAncestor(c);
    }

    public static boolean isDescendingFrom(Component a, Component b) {
        if (a == b) {
            return true;
        }
        for (Container p = a.getParent(); p != null; p = p.getParent()) {
            if (p != b) continue;
            return true;
        }
        return false;
    }

    public static Rectangle computeIntersection(int x, int y, int width, int height, Rectangle dest) {
        int x1 = x > dest.x ? x : dest.x;
        int x2 = x + width < dest.x + dest.width ? x + width : dest.x + dest.width;
        int y1 = y > dest.y ? y : dest.y;
        int y2 = y + height < dest.y + dest.height ? y + height : dest.y + dest.height;
        dest.x = x1;
        dest.y = y1;
        dest.width = x2 - x1;
        dest.height = y2 - y1;
        if (dest.width < 0 || dest.height < 0) {
            dest.height = 0;
            dest.width = 0;
            dest.y = 0;
            dest.x = 0;
        }
        return dest;
    }

    public static Rectangle computeUnion(int x, int y, int width, int height, Rectangle dest) {
        int x1 = x < dest.x ? x : dest.x;
        int x2 = x + width > dest.x + dest.width ? x + width : dest.x + dest.width;
        int y1 = y < dest.y ? y : dest.y;
        int y2 = y + height > dest.y + dest.height ? y + height : dest.y + dest.height;
        dest.x = x1;
        dest.y = y1;
        dest.width = x2 - x1;
        dest.height = y2 - y1;
        return dest;
    }

    public static Rectangle[] computeDifference(Rectangle rectA, Rectangle rectB) {
        if (rectB == null || !rectA.intersects(rectB) || SwingUtilities.isRectangleContainingRectangle(rectB, rectA)) {
            return new Rectangle[0];
        }
        Rectangle t = new Rectangle();
        Rectangle a = null;
        Rectangle b = null;
        Rectangle c = null;
        Rectangle d = null;
        int rectCount = 0;
        if (SwingUtilities.isRectangleContainingRectangle(rectA, rectB)) {
            t.x = rectA.x;
            t.y = rectA.y;
            t.width = rectB.x - rectA.x;
            t.height = rectA.height;
            if (t.width > 0 && t.height > 0) {
                a = new Rectangle(t);
                ++rectCount;
            }
            t.x = rectB.x;
            t.y = rectA.y;
            t.width = rectB.width;
            t.height = rectB.y - rectA.y;
            if (t.width > 0 && t.height > 0) {
                b = new Rectangle(t);
                ++rectCount;
            }
            t.x = rectB.x;
            t.y = rectB.y + rectB.height;
            t.width = rectB.width;
            t.height = rectA.y + rectA.height - (rectB.y + rectB.height);
            if (t.width > 0 && t.height > 0) {
                c = new Rectangle(t);
                ++rectCount;
            }
            t.x = rectB.x + rectB.width;
            t.y = rectA.y;
            t.width = rectA.x + rectA.width - (rectB.x + rectB.width);
            t.height = rectA.height;
            if (t.width > 0 && t.height > 0) {
                d = new Rectangle(t);
                ++rectCount;
            }
        } else if (rectB.x <= rectA.x && rectB.y <= rectA.y) {
            if (rectB.x + rectB.width > rectA.x + rectA.width) {
                t.x = rectA.x;
                t.y = rectB.y + rectB.height;
                t.width = rectA.width;
                t.height = rectA.y + rectA.height - (rectB.y + rectB.height);
                if (t.width > 0 && t.height > 0) {
                    a = t;
                    ++rectCount;
                }
            } else if (rectB.y + rectB.height > rectA.y + rectA.height) {
                t.setBounds(rectB.x + rectB.width, rectA.y, rectA.x + rectA.width - (rectB.x + rectB.width), rectA.height);
                if (t.width > 0 && t.height > 0) {
                    a = t;
                    ++rectCount;
                }
            } else {
                t.setBounds(rectB.x + rectB.width, rectA.y, rectA.x + rectA.width - (rectB.x + rectB.width), rectB.y + rectB.height - rectA.y);
                if (t.width > 0 && t.height > 0) {
                    a = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectA.x, rectB.y + rectB.height, rectA.width, rectA.y + rectA.height - (rectB.y + rectB.height));
                if (t.width > 0 && t.height > 0) {
                    b = new Rectangle(t);
                    ++rectCount;
                }
            }
        } else if (rectB.x <= rectA.x && rectB.y + rectB.height >= rectA.y + rectA.height) {
            if (rectB.x + rectB.width > rectA.x + rectA.width) {
                t.setBounds(rectA.x, rectA.y, rectA.width, rectB.y - rectA.y);
                if (t.width > 0 && t.height > 0) {
                    a = t;
                    ++rectCount;
                }
            } else {
                t.setBounds(rectA.x, rectA.y, rectA.width, rectB.y - rectA.y);
                if (t.width > 0 && t.height > 0) {
                    a = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectB.x + rectB.width, rectB.y, rectA.x + rectA.width - (rectB.x + rectB.width), rectA.y + rectA.height - rectB.y);
                if (t.width > 0 && t.height > 0) {
                    b = new Rectangle(t);
                    ++rectCount;
                }
            }
        } else if (rectB.x <= rectA.x) {
            if (rectB.x + rectB.width >= rectA.x + rectA.width) {
                t.setBounds(rectA.x, rectA.y, rectA.width, rectB.y - rectA.y);
                if (t.width > 0 && t.height > 0) {
                    a = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectA.x, rectB.y + rectB.height, rectA.width, rectA.y + rectA.height - (rectB.y + rectB.height));
                if (t.width > 0 && t.height > 0) {
                    b = new Rectangle(t);
                    ++rectCount;
                }
            } else {
                t.setBounds(rectA.x, rectA.y, rectA.width, rectB.y - rectA.y);
                if (t.width > 0 && t.height > 0) {
                    a = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectB.x + rectB.width, rectB.y, rectA.x + rectA.width - (rectB.x + rectB.width), rectB.height);
                if (t.width > 0 && t.height > 0) {
                    b = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectA.x, rectB.y + rectB.height, rectA.width, rectA.y + rectA.height - (rectB.y + rectB.height));
                if (t.width > 0 && t.height > 0) {
                    c = new Rectangle(t);
                    ++rectCount;
                }
            }
        } else if (rectB.x <= rectA.x + rectA.width && rectB.x + rectB.width > rectA.x + rectA.width) {
            if (rectB.y <= rectA.y && rectB.y + rectB.height > rectA.y + rectA.height) {
                t.setBounds(rectA.x, rectA.y, rectB.x - rectA.x, rectA.height);
                if (t.width > 0 && t.height > 0) {
                    a = t;
                    ++rectCount;
                }
            } else if (rectB.y <= rectA.y) {
                t.setBounds(rectA.x, rectA.y, rectB.x - rectA.x, rectB.y + rectB.height - rectA.y);
                if (t.width > 0 && t.height > 0) {
                    a = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectA.x, rectB.y + rectB.height, rectA.width, rectA.y + rectA.height - (rectB.y + rectB.height));
                if (t.width > 0 && t.height > 0) {
                    b = new Rectangle(t);
                    ++rectCount;
                }
            } else if (rectB.y + rectB.height > rectA.y + rectA.height) {
                t.setBounds(rectA.x, rectA.y, rectA.width, rectB.y - rectA.y);
                if (t.width > 0 && t.height > 0) {
                    a = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectA.x, rectB.y, rectB.x - rectA.x, rectA.y + rectA.height - rectB.y);
                if (t.width > 0 && t.height > 0) {
                    b = new Rectangle(t);
                    ++rectCount;
                }
            } else {
                t.setBounds(rectA.x, rectA.y, rectA.width, rectB.y - rectA.y);
                if (t.width > 0 && t.height > 0) {
                    a = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectA.x, rectB.y, rectB.x - rectA.x, rectB.height);
                if (t.width > 0 && t.height > 0) {
                    b = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectA.x, rectB.y + rectB.height, rectA.width, rectA.y + rectA.height - (rectB.y + rectB.height));
                if (t.width > 0 && t.height > 0) {
                    c = new Rectangle(t);
                    ++rectCount;
                }
            }
        } else if (rectB.x >= rectA.x && rectB.x + rectB.width <= rectA.x + rectA.width) {
            if (rectB.y <= rectA.y && rectB.y + rectB.height > rectA.y + rectA.height) {
                t.setBounds(rectA.x, rectA.y, rectB.x - rectA.x, rectA.height);
                if (t.width > 0 && t.height > 0) {
                    a = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectB.x + rectB.width, rectA.y, rectA.x + rectA.width - (rectB.x + rectB.width), rectA.height);
                if (t.width > 0 && t.height > 0) {
                    b = new Rectangle(t);
                    ++rectCount;
                }
            } else if (rectB.y <= rectA.y) {
                t.setBounds(rectA.x, rectA.y, rectB.x - rectA.x, rectA.height);
                if (t.width > 0 && t.height > 0) {
                    a = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectB.x, rectB.y + rectB.height, rectB.width, rectA.y + rectA.height - (rectB.y + rectB.height));
                if (t.width > 0 && t.height > 0) {
                    b = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectB.x + rectB.width, rectA.y, rectA.x + rectA.width - (rectB.x + rectB.width), rectA.height);
                if (t.width > 0 && t.height > 0) {
                    c = new Rectangle(t);
                    ++rectCount;
                }
            } else {
                t.setBounds(rectA.x, rectA.y, rectB.x - rectA.x, rectA.height);
                if (t.width > 0 && t.height > 0) {
                    a = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectB.x, rectA.y, rectB.width, rectB.y - rectA.y);
                if (t.width > 0 && t.height > 0) {
                    b = new Rectangle(t);
                    ++rectCount;
                }
                t.setBounds(rectB.x + rectB.width, rectA.y, rectA.x + rectA.width - (rectB.x + rectB.width), rectA.height);
                if (t.width > 0 && t.height > 0) {
                    c = new Rectangle(t);
                    ++rectCount;
                }
            }
        }
        Rectangle[] result = new Rectangle[rectCount];
        rectCount = 0;
        if (a != null) {
            result[rectCount++] = a;
        }
        if (b != null) {
            result[rectCount++] = b;
        }
        if (c != null) {
            result[rectCount++] = c;
        }
        if (d != null) {
            result[rectCount++] = d;
        }
        return result;
    }

    private static boolean checkMouseButton(MouseEvent anEvent, int mouseButton, int mouseButtonDownMask) {
        switch (anEvent.getID()) {
            case 500: 
            case 501: 
            case 502: {
                return anEvent.getButton() == mouseButton;
            }
            case 504: 
            case 505: 
            case 506: {
                return (anEvent.getModifiersEx() & mouseButtonDownMask) != 0;
            }
        }
        return (anEvent.getModifiersEx() & mouseButtonDownMask) != 0 || anEvent.getButton() == mouseButton;
    }

    public static boolean isLeftMouseButton(MouseEvent anEvent) {
        return SwingUtilities.checkMouseButton(anEvent, 1, 1024);
    }

    public static boolean isMiddleMouseButton(MouseEvent anEvent) {
        return SwingUtilities.checkMouseButton(anEvent, 2, 2048);
    }

    public static boolean isRightMouseButton(MouseEvent anEvent) {
        return SwingUtilities.checkMouseButton(anEvent, 3, 4096);
    }

    public static int computeStringWidth(FontMetrics fm, String str) {
        return SwingUtilities2.stringWidth(null, fm, str);
    }

    public static String layoutCompoundLabel(JComponent c, FontMetrics fm, String text, Icon icon, int verticalAlignment, int horizontalAlignment, int verticalTextPosition, int horizontalTextPosition, Rectangle viewR, Rectangle iconR, Rectangle textR, int textIconGap) {
        boolean orientationIsLeftToRight = true;
        int hAlign = horizontalAlignment;
        int hTextPos = horizontalTextPosition;
        if (c != null && !c.getComponentOrientation().isLeftToRight()) {
            orientationIsLeftToRight = false;
        }
        switch (horizontalAlignment) {
            case 10: {
                hAlign = orientationIsLeftToRight ? 2 : 4;
                break;
            }
            case 11: {
                hAlign = orientationIsLeftToRight ? 4 : 2;
            }
        }
        switch (horizontalTextPosition) {
            case 10: {
                hTextPos = orientationIsLeftToRight ? 2 : 4;
                break;
            }
            case 11: {
                hTextPos = orientationIsLeftToRight ? 4 : 2;
            }
        }
        return SwingUtilities.layoutCompoundLabelImpl(c, fm, text, icon, verticalAlignment, hAlign, verticalTextPosition, hTextPos, viewR, iconR, textR, textIconGap);
    }

    public static String layoutCompoundLabel(FontMetrics fm, String text, Icon icon, int verticalAlignment, int horizontalAlignment, int verticalTextPosition, int horizontalTextPosition, Rectangle viewR, Rectangle iconR, Rectangle textR, int textIconGap) {
        return SwingUtilities.layoutCompoundLabelImpl(null, fm, text, icon, verticalAlignment, horizontalAlignment, verticalTextPosition, horizontalTextPosition, viewR, iconR, textR, textIconGap);
    }

    private static String layoutCompoundLabelImpl(JComponent c, FontMetrics fm, String text, Icon icon, int verticalAlignment, int horizontalAlignment, int verticalTextPosition, int horizontalTextPosition, Rectangle viewR, Rectangle iconR, Rectangle textR, int textIconGap) {
        int gap;
        if (icon != null) {
            iconR.width = icon.getIconWidth();
            iconR.height = icon.getIconHeight();
        } else {
            iconR.height = 0;
            iconR.width = 0;
        }
        boolean textIsEmpty = text == null || text.isEmpty();
        int lsb = 0;
        int rsb = 0;
        if (textIsEmpty) {
            textR.height = 0;
            textR.width = 0;
            text = "";
            gap = 0;
        } else {
            View v;
            gap = icon == null ? 0 : textIconGap;
            int availTextWidth = horizontalTextPosition == 0 ? viewR.width : viewR.width - (iconR.width + gap);
            View view = v = c != null ? (View)c.getClientProperty("html") : null;
            if (v != null) {
                textR.width = Math.min(availTextWidth, (int)v.getPreferredSpan(0));
                textR.height = (int)v.getPreferredSpan(1);
            } else {
                textR.width = SwingUtilities2.stringWidth(c, fm, text);
                lsb = SwingUtilities2.getLeftSideBearing(c, fm, text);
                if (lsb < 0) {
                    textR.width -= lsb;
                }
                if (textR.width > availTextWidth) {
                    text = SwingUtilities2.clipString(c, fm, text, availTextWidth);
                    textR.width = SwingUtilities2.stringWidth(c, fm, text);
                }
                textR.height = fm.getHeight();
            }
        }
        textR.y = verticalTextPosition == 1 ? (horizontalTextPosition != 0 ? 0 : -(textR.height + gap)) : (verticalTextPosition == 0 ? iconR.height / 2 - textR.height / 2 : (horizontalTextPosition != 0 ? iconR.height - textR.height : iconR.height + gap));
        textR.x = horizontalTextPosition == 2 ? -(textR.width + gap) : (horizontalTextPosition == 0 ? iconR.width / 2 - textR.width / 2 : iconR.width + gap);
        int labelR_x = Math.min(iconR.x, textR.x);
        int labelR_width = Math.max(iconR.x + iconR.width, textR.x + textR.width) - labelR_x;
        int labelR_y = Math.min(iconR.y, textR.y);
        int labelR_height = Math.max(iconR.y + iconR.height, textR.y + textR.height) - labelR_y;
        int dy = verticalAlignment == 1 ? viewR.y - labelR_y : (verticalAlignment == 0 ? viewR.y + viewR.height / 2 - (labelR_y + labelR_height / 2) : viewR.y + viewR.height - (labelR_y + labelR_height));
        int dx = horizontalAlignment == 2 ? viewR.x - labelR_x : (horizontalAlignment == 4 ? viewR.x + viewR.width - (labelR_x + labelR_width) : viewR.x + viewR.width / 2 - (labelR_x + labelR_width / 2));
        textR.x += dx;
        textR.y += dy;
        iconR.x += dx;
        iconR.y += dy;
        if (lsb < 0) {
            textR.x -= lsb;
            textR.width += lsb;
        }
        if (rsb > 0) {
            textR.width -= rsb;
        }
        return text;
    }

    public static void paintComponent(Graphics g, Component c, Container p, int x, int y, int w, int h) {
        SwingUtilities.getCellRendererPane(c, p).paintComponent(g, c, p, x, y, w, h, false);
    }

    public static void paintComponent(Graphics g, Component c, Container p, Rectangle r) {
        SwingUtilities.paintComponent(g, c, p, r.x, r.y, r.width, r.height);
    }

    private static CellRendererPane getCellRendererPane(Component c, Container p) {
        Container shell = c.getParent();
        if (shell instanceof CellRendererPane) {
            if (shell.getParent() != p) {
                p.add(shell);
            }
        } else {
            shell = new CellRendererPane();
            shell.add(c);
            p.add(shell);
        }
        return (CellRendererPane)shell;
    }

    public static void updateComponentTreeUI(Component c) {
        SwingUtilities.updateComponentTreeUI0(c);
        c.invalidate();
        c.validate();
        c.repaint();
    }

    private static void updateComponentTreeUI0(Component c) {
        if (c instanceof JComponent) {
            JComponent jc = (JComponent)c;
            jc.updateUI();
            JPopupMenu jpm = jc.getComponentPopupMenu();
            if (jpm != null) {
                SwingUtilities.updateComponentTreeUI(jpm);
            }
        }
        Component[] children = null;
        if (c instanceof JMenu) {
            children = ((JMenu)c).getMenuComponents();
        } else if (c instanceof Container) {
            children = ((Container)c).getComponents();
        }
        if (children != null) {
            for (Component child : children) {
                SwingUtilities.updateComponentTreeUI0(child);
            }
        }
    }

    public static void invokeLater(Runnable doRun) {
        EventQueue.invokeLater(doRun);
    }

    public static void invokeAndWait(Runnable doRun) throws InterruptedException, InvocationTargetException {
        EventQueue.invokeAndWait(doRun);
    }

    public static boolean isEventDispatchThread() {
        return EventQueue.isDispatchThread();
    }

    public static int getAccessibleIndexInParent(Component c) {
        return c.getAccessibleContext().getAccessibleIndexInParent();
    }

    public static Accessible getAccessibleAt(Component c, Point p) {
        if (c instanceof Container) {
            return c.getAccessibleContext().getAccessibleComponent().getAccessibleAt(p);
        }
        if (c instanceof Accessible) {
            AccessibleContext ac;
            Accessible a = (Accessible)((Object)c);
            if (a != null && (ac = a.getAccessibleContext()) != null) {
                int nchildren = ac.getAccessibleChildrenCount();
                for (int i = 0; i < nchildren; ++i) {
                    AccessibleComponent acmp;
                    a = ac.getAccessibleChild(i);
                    if (a == null || (ac = a.getAccessibleContext()) == null || (acmp = ac.getAccessibleComponent()) == null || !acmp.isShowing()) continue;
                    Point location = acmp.getLocation();
                    Point np = new Point(p.x - location.x, p.y - location.y);
                    if (!acmp.contains(np)) continue;
                    return a;
                }
            }
            return (Accessible)((Object)c);
        }
        return null;
    }

    public static AccessibleStateSet getAccessibleStateSet(Component c) {
        return c.getAccessibleContext().getAccessibleStateSet();
    }

    public static int getAccessibleChildrenCount(Component c) {
        return c.getAccessibleContext().getAccessibleChildrenCount();
    }

    public static Accessible getAccessibleChild(Component c, int i) {
        return c.getAccessibleContext().getAccessibleChild(i);
    }

    @Deprecated
    public static Component findFocusOwner(Component c) {
        Component focusOwner;
        Component temp = focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        while (temp != null) {
            if (temp == c) {
                return focusOwner;
            }
            temp = temp instanceof Window ? null : temp.getParent();
        }
        return null;
    }

    public static JRootPane getRootPane(Component c) {
        if (c instanceof RootPaneContainer) {
            return ((RootPaneContainer)((Object)c)).getRootPane();
        }
        while (c != null) {
            if (c instanceof JRootPane) {
                return (JRootPane)c;
            }
            c = c.getParent();
        }
        return null;
    }

    public static Component getRoot(Component c) {
        Component applet = null;
        for (Component p = c; p != null; p = p.getParent()) {
            if (p instanceof Window) {
                return p;
            }
            if (!(p instanceof Applet)) continue;
            applet = p;
        }
        return applet;
    }

    static JComponent getPaintingOrigin(JComponent c) {
        Container p = c;
        while ((p = p.getParent()) instanceof JComponent) {
            Container jp = p;
            if (!((JComponent)jp).isPaintingOrigin()) continue;
            return jp;
        }
        return null;
    }

    public static boolean processKeyBindings(KeyEvent event) {
        if (event != null) {
            boolean pressed;
            if (event.isConsumed()) {
                return false;
            }
            boolean bl = pressed = event.getID() == 401;
            if (!SwingUtilities.isValidKeyEventForKeyBindings(event)) {
                return false;
            }
            for (Component component = event.getComponent(); component != null; component = component.getParent()) {
                if (component instanceof JComponent) {
                    return ((JComponent)component).processKeyBindings(event, pressed);
                }
                if (!(component instanceof Applet) && !(component instanceof Window)) continue;
                return JComponent.processKeyBindingsForAllComponents(event, (Container)component, pressed);
            }
        }
        return false;
    }

    static boolean isValidKeyEventForKeyBindings(KeyEvent e) {
        return true;
    }

    public static boolean notifyAction(Action action, KeyStroke ks, KeyEvent event, Object sender, int modifiers) {
        if (action == null || !action.accept(sender)) {
            return false;
        }
        Object commandO = action.getValue("ActionCommandKey");
        boolean stayNull = commandO == null && action instanceof JComponent.ActionStandin;
        String command = commandO != null ? commandO.toString() : (!stayNull && event.getKeyChar() != '\uffff' ? String.valueOf(event.getKeyChar()) : null);
        action.actionPerformed(new ActionEvent(sender, 1001, command, event.getWhen(), modifiers));
        Object object = event.getSource();
        if (object instanceof JToggleButton) {
            JToggleButton tb = (JToggleButton)object;
            commandO = action.getValue("SwingSelectedKey");
            if (commandO != null) {
                tb.setSelected(!tb.isSelected());
            }
        }
        return true;
    }

    public static void replaceUIInputMap(JComponent component, int type, InputMap uiInputMap) {
        InputMap map = component.getInputMap(type, uiInputMap != null);
        while (map != null) {
            InputMap parent = map.getParent();
            if (parent == null || parent instanceof UIResource) {
                map.setParent(uiInputMap);
                return;
            }
            map = parent;
        }
    }

    public static void replaceUIActionMap(JComponent component, ActionMap uiActionMap) {
        ActionMap map = component.getActionMap(uiActionMap != null);
        while (map != null) {
            ActionMap parent = map.getParent();
            if (parent == null || parent instanceof UIResource) {
                map.setParent(uiActionMap);
                return;
            }
            map = parent;
        }
    }

    public static InputMap getUIInputMap(JComponent component, int condition) {
        InputMap map = component.getInputMap(condition, false);
        while (map != null) {
            InputMap parent = map.getParent();
            if (parent instanceof UIResource) {
                return parent;
            }
            map = parent;
        }
        return null;
    }

    public static ActionMap getUIActionMap(JComponent component) {
        ActionMap map = component.getActionMap(false);
        while (map != null) {
            ActionMap parent = map.getParent();
            if (parent instanceof UIResource) {
                return parent;
            }
            map = parent;
        }
        return null;
    }

    static Frame getSharedOwnerFrame() throws HeadlessException {
        Frame sharedOwnerFrame = (Frame)SwingUtilities.appContextGet(sharedOwnerFrameKey);
        if (sharedOwnerFrame == null) {
            sharedOwnerFrame = new SharedOwnerFrame();
            SwingUtilities.appContextPut(sharedOwnerFrameKey, sharedOwnerFrame);
        }
        return sharedOwnerFrame;
    }

    static WindowListener getSharedOwnerFrameShutdownListener() throws HeadlessException {
        Frame sharedOwnerFrame = SwingUtilities.getSharedOwnerFrame();
        return (WindowListener)((Object)sharedOwnerFrame);
    }

    static Object appContextGet(Object key) {
        return AppContext.getAppContext().get(key);
    }

    static void appContextPut(Object key, Object value) {
        AppContext.getAppContext().put(key, value);
    }

    static void appContextRemove(Object key) {
        AppContext.getAppContext().remove(key);
    }

    static Class<?> loadSystemClass(String className) throws ClassNotFoundException {
        ReflectUtil.checkPackageAccess(className);
        return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
    }

    static boolean isLeftToRight(Component c) {
        return c.getComponentOrientation().isLeftToRight();
    }

    private SwingUtilities() {
        throw new Error("SwingUtilities is just a container for static methods");
    }

    static boolean doesIconReferenceImage(Icon icon, Image image) {
        Image image2;
        if (icon instanceof ImageIcon) {
            ImageIcon i = (ImageIcon)icon;
            image2 = i.getImage();
        } else {
            image2 = null;
        }
        Image iconImage = image2;
        return iconImage == image;
    }

    static int findDisplayedMnemonicIndex(String text, int mnemonic) {
        if (text == null || mnemonic == 0) {
            return -1;
        }
        if (mnemonic >= 97 && mnemonic <= 122) {
            return -1;
        }
        char uc = Character.toUpperCase((char)mnemonic);
        char lc = Character.toLowerCase((char)mnemonic);
        int uci = text.indexOf(uc);
        int lci = text.indexOf(lc);
        if (uci == -1) {
            return lci;
        }
        if (lci == -1) {
            return uci;
        }
        return lci < uci ? lci : uci;
    }

    public static Rectangle calculateInnerArea(JComponent c, Rectangle r) {
        if (c == null) {
            return null;
        }
        Rectangle rect = r;
        Insets insets = c.getInsets();
        if (rect == null) {
            rect = new Rectangle();
        }
        rect.x = insets.left;
        rect.y = insets.top;
        rect.width = c.getWidth() - insets.left - insets.right;
        rect.height = c.getHeight() - insets.top - insets.bottom;
        return rect;
    }

    static void updateRendererOrEditorUI(Object rendererOrEditor) {
        if (rendererOrEditor == null) {
            return;
        }
        Component component = null;
        if (rendererOrEditor instanceof Component) {
            component = (Component)rendererOrEditor;
        }
        if (rendererOrEditor instanceof DefaultCellEditor) {
            component = ((DefaultCellEditor)rendererOrEditor).getComponent();
        }
        if (component != null) {
            SwingUtilities.updateComponentTreeUI(component);
        }
    }

    public static Container getUnwrappedParent(Component component) {
        Container parent = component.getParent();
        while (parent instanceof JLayer) {
            parent = parent.getParent();
        }
        return parent;
    }

    public static Component getUnwrappedView(JViewport viewport) {
        Component view = viewport.getView();
        while (view instanceof JLayer) {
            view = ((JLayer)view).getView();
        }
        return view;
    }

    static Container getValidateRoot(Container c, boolean visibleOnly) {
        Container root = null;
        while (c != null) {
            if (!c.isDisplayable() || c instanceof CellRendererPane) {
                return null;
            }
            if (c.isValidateRoot()) {
                root = c;
                break;
            }
            c = c.getParent();
        }
        if (root == null) {
            return null;
        }
        while (c != null) {
            if (!c.isDisplayable() || visibleOnly && !c.isVisible()) {
                return null;
            }
            if (c instanceof Window || c instanceof Applet) {
                return root;
            }
            c = c.getParent();
        }
        return null;
    }

    static {
        sharedOwnerFrameKey = new StringBuffer("SwingUtilities.sharedOwnerFrame");
    }

    static class SharedOwnerFrame
    extends Frame
    implements WindowListener {
        SharedOwnerFrame() {
        }

        @Override
        public void addNotify() {
            super.addNotify();
            this.installListeners();
        }

        void installListeners() {
            Window[] windows;
            for (Window window : windows = this.getOwnedWindows()) {
                if (window == null) continue;
                window.removeWindowListener(this);
                window.addWindowListener(this);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void windowClosed(WindowEvent e) {
            Object object = this.getTreeLock();
            synchronized (object) {
                Window[] windows;
                for (Window window : windows = this.getOwnedWindows()) {
                    if (window == null) continue;
                    if (window.isDisplayable()) {
                        return;
                    }
                    window.removeWindowListener(this);
                }
                this.dispose();
            }
        }

        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowClosing(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }

        @Override
        public void show() {
        }

        @Override
        public void dispose() {
            try {
                this.getToolkit().getSystemEventQueue();
                super.dispose();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

