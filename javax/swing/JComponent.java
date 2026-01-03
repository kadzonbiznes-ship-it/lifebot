/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.applet.Applet;
import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.Transient;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleExtendedComponent;
import javax.accessibility.AccessibleKeyBinding;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.AncestorNotifier;
import javax.swing.ArrayTable;
import javax.swing.Autoscroller;
import javax.swing.CellRendererPane;
import javax.swing.ClientPropertyKey;
import javax.swing.ComponentInputMap;
import javax.swing.DebugGraphics;
import javax.swing.InputMap;
import javax.swing.InputVerifier;
import javax.swing.JInternalFrame;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.KeyboardManager;
import javax.swing.LegacyGlueFocusTraversalPolicy;
import javax.swing.LookAndFeel;
import javax.swing.Popup;
import javax.swing.RepaintManager;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIClientPropertyKey;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorListener;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.ComponentUI;
import javax.swing.table.JTableHeader;
import sun.awt.AWTAccessor;
import sun.awt.RequestFocusController;
import sun.awt.SunToolkit;
import sun.swing.SwingAccessor;
import sun.swing.SwingUtilities2;

@JavaBean(defaultProperty="UIClassID")
public abstract class JComponent
extends Container
implements Serializable,
TransferHandler.HasGetTransferHandler {
    private static final String uiClassID = "ComponentUI";
    private static final Hashtable<ObjectInputStream, ReadObjectCallback> readObjectCallbacks = new Hashtable(1);
    private static Set<KeyStroke> managingFocusForwardTraversalKeys;
    private static Set<KeyStroke> managingFocusBackwardTraversalKeys;
    private static final int NOT_OBSCURED = 0;
    private static final int PARTIALLY_OBSCURED = 1;
    private static final int COMPLETELY_OBSCURED = 2;
    static boolean DEBUG_GRAPHICS_LOADED;
    private static final Object INPUT_VERIFIER_SOURCE_KEY;
    private boolean isAlignmentXSet;
    private float alignmentX;
    private boolean isAlignmentYSet;
    private float alignmentY;
    protected transient ComponentUI ui;
    protected EventListenerList listenerList = new EventListenerList();
    private transient ArrayTable clientProperties;
    private VetoableChangeSupport vetoableChangeSupport;
    private boolean autoscrolls;
    private Border border;
    private int flags;
    private InputVerifier inputVerifier = null;
    private boolean verifyInputWhenFocusTarget = true;
    transient Component paintingChild;
    public static final int WHEN_FOCUSED = 0;
    public static final int WHEN_ANCESTOR_OF_FOCUSED_COMPONENT = 1;
    public static final int WHEN_IN_FOCUSED_WINDOW = 2;
    public static final int UNDEFINED_CONDITION = -1;
    private static final String KEYBOARD_BINDINGS_KEY = "_KeyboardBindings";
    private static final String WHEN_IN_FOCUSED_WINDOW_BINDINGS = "_WhenInFocusedWindow";
    public static final String TOOL_TIP_TEXT_KEY = "ToolTipText";
    private static final String NEXT_FOCUS = "nextFocus";
    private JPopupMenu popupMenu;
    private static final int IS_DOUBLE_BUFFERED = 0;
    private static final int ANCESTOR_USING_BUFFER = 1;
    private static final int IS_PAINTING_TILE = 2;
    private static final int IS_OPAQUE = 3;
    private static final int KEY_EVENTS_ENABLED = 4;
    private static final int FOCUS_INPUTMAP_CREATED = 5;
    private static final int ANCESTOR_INPUTMAP_CREATED = 6;
    private static final int WIF_INPUTMAP_CREATED = 7;
    private static final int ACTIONMAP_CREATED = 8;
    private static final int CREATED_DOUBLE_BUFFER = 9;
    private static final int IS_PRINTING = 11;
    private static final int IS_PRINTING_ALL = 12;
    private static final int IS_REPAINTING = 13;
    private static final int WRITE_OBJ_COUNTER_FIRST = 14;
    private static final int RESERVED_1 = 15;
    private static final int RESERVED_2 = 16;
    private static final int RESERVED_3 = 17;
    private static final int RESERVED_4 = 18;
    private static final int RESERVED_5 = 19;
    private static final int RESERVED_6 = 20;
    private static final int WRITE_OBJ_COUNTER_LAST = 21;
    private static final int REQUEST_FOCUS_DISABLED = 22;
    private static final int INHERITS_POPUP_MENU = 23;
    private static final int OPAQUE_SET = 24;
    private static final int AUTOSCROLLS_SET = 25;
    private static final int FOCUS_TRAVERSAL_KEYS_FORWARD_SET = 26;
    private static final int FOCUS_TRAVERSAL_KEYS_BACKWARD_SET = 27;
    private transient AtomicBoolean revalidateRunnableScheduled = new AtomicBoolean(false);
    private static List<Rectangle> tempRectangles;
    private InputMap focusInputMap;
    private InputMap ancestorInputMap;
    private ComponentInputMap windowInputMap;
    private ActionMap actionMap;
    private static final String defaultLocale = "JComponent.defaultLocale";
    private static Component componentObtainingGraphicsFrom;
    private static Object componentObtainingGraphicsFromLock;
    private transient Object aaHint;
    private transient Object lcdRenderingHint;
    static final RequestFocusController focusController;

    static Graphics safelyGetGraphics(Component c) {
        return JComponent.safelyGetGraphics(c, SwingUtilities.getRoot(c));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Graphics safelyGetGraphics(Component c, Component root) {
        Object object = componentObtainingGraphicsFromLock;
        synchronized (object) {
            componentObtainingGraphicsFrom = root;
            Graphics g = c.getGraphics();
            componentObtainingGraphicsFrom = null;
            return g;
        }
    }

    static void getGraphicsInvoked(Component root) {
        JRootPane rootPane;
        if (!JComponent.isComponentObtainingGraphicsFrom(root) && (rootPane = ((RootPaneContainer)((Object)root)).getRootPane()) != null) {
            rootPane.disableTrueDoubleBuffering();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean isComponentObtainingGraphicsFrom(Component c) {
        Object object = componentObtainingGraphicsFromLock;
        synchronized (object) {
            return componentObtainingGraphicsFrom == c;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Set<KeyStroke> getManagingFocusForwardTraversalKeys() {
        Class<JComponent> clazz = JComponent.class;
        synchronized (JComponent.class) {
            if (managingFocusForwardTraversalKeys == null) {
                managingFocusForwardTraversalKeys = new HashSet<KeyStroke>(1);
                managingFocusForwardTraversalKeys.add(KeyStroke.getKeyStroke(9, 2));
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            return managingFocusForwardTraversalKeys;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Set<KeyStroke> getManagingFocusBackwardTraversalKeys() {
        Class<JComponent> clazz = JComponent.class;
        synchronized (JComponent.class) {
            if (managingFocusBackwardTraversalKeys == null) {
                managingFocusBackwardTraversalKeys = new HashSet<KeyStroke>(1);
                managingFocusBackwardTraversalKeys.add(KeyStroke.getKeyStroke(9, 3));
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            return managingFocusBackwardTraversalKeys;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Rectangle fetchRectangle() {
        List<Rectangle> list = tempRectangles;
        synchronized (list) {
            int size = tempRectangles.size();
            Rectangle rect = size > 0 ? tempRectangles.remove(size - 1) : new Rectangle(0, 0, 0, 0);
            return rect;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void recycleRectangle(Rectangle rect) {
        List<Rectangle> list = tempRectangles;
        synchronized (list) {
            tempRectangles.add(rect);
        }
    }

    @BeanProperty(description="Whether or not the JPopupMenu is inherited")
    public void setInheritsPopupMenu(boolean value) {
        boolean oldValue = this.getFlag(23);
        this.setFlag(23, value);
        this.firePropertyChange("inheritsPopupMenu", oldValue, value);
    }

    public boolean getInheritsPopupMenu() {
        return this.getFlag(23);
    }

    @BeanProperty(preferred=true, description="Popup to show")
    public void setComponentPopupMenu(JPopupMenu popup) {
        if (popup != null) {
            this.enableEvents(16L);
        }
        JPopupMenu oldPopup = this.popupMenu;
        this.popupMenu = popup;
        this.firePropertyChange("componentPopupMenu", oldPopup, popup);
    }

    public JPopupMenu getComponentPopupMenu() {
        if (!this.getInheritsPopupMenu()) {
            return this.popupMenu;
        }
        if (this.popupMenu == null) {
            for (Container parent = this.getParent(); parent != null; parent = parent.getParent()) {
                if (parent instanceof JComponent) {
                    return ((JComponent)parent).getComponentPopupMenu();
                }
                if (parent instanceof Window || parent instanceof Applet) break;
            }
            return null;
        }
        return this.popupMenu;
    }

    public JComponent() {
        this.enableEvents(8L);
        if (this.isManagingFocus()) {
            LookAndFeel.installProperty(this, "focusTraversalKeysForward", JComponent.getManagingFocusForwardTraversalKeys());
            LookAndFeel.installProperty(this, "focusTraversalKeysBackward", JComponent.getManagingFocusBackwardTraversalKeys());
        }
        super.setLocale(JComponent.getDefaultLocale());
    }

    public void updateUI() {
    }

    @Transient
    public ComponentUI getUI() {
        return this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The component's look and feel delegate.")
    protected void setUI(ComponentUI newUI) {
        this.uninstallUIAndProperties();
        this.aaHint = UIManager.getDefaults().get(RenderingHints.KEY_TEXT_ANTIALIASING);
        this.lcdRenderingHint = UIManager.getDefaults().get(RenderingHints.KEY_TEXT_LCD_CONTRAST);
        ComponentUI oldUI = this.ui;
        this.ui = newUI;
        if (this.ui != null) {
            this.ui.installUI(this);
        }
        this.firePropertyChange("UI", oldUI, newUI);
        this.revalidate();
        this.repaint();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void uninstallUIAndProperties() {
        if (this.ui != null) {
            this.ui.uninstallUI(this);
            if (this.clientProperties != null) {
                ArrayTable arrayTable = this.clientProperties;
                synchronized (arrayTable) {
                    Object[] clientPropertyKeys = this.clientProperties.getKeys(null);
                    if (clientPropertyKeys != null) {
                        for (Object key : clientPropertyKeys) {
                            if (!(key instanceof UIClientPropertyKey)) continue;
                            this.putClientProperty(key, null);
                        }
                    }
                }
            }
        }
    }

    @BeanProperty(bound=false, expert=true, description="UIClassID")
    public String getUIClassID() {
        return uiClassID;
    }

    protected Graphics getComponentGraphics(Graphics g) {
        Graphics componentGraphics = g;
        if (this.ui != null && DEBUG_GRAPHICS_LOADED && DebugGraphics.debugComponentCount() != 0 && this.shouldDebugGraphics() != 0 && !(g instanceof DebugGraphics)) {
            componentGraphics = new DebugGraphics(g, this);
        }
        componentGraphics.setColor(this.getForeground());
        componentGraphics.setFont(this.getFont());
        return componentGraphics;
    }

    protected void paintComponent(Graphics g) {
        if (this.ui != null) {
            Graphics scratchGraphics = g == null ? null : g.create();
            try {
                this.ui.update(scratchGraphics, this);
            }
            finally {
                scratchGraphics.dispose();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void paintChildren(Graphics g) {
        Graphics sg = g;
        Object object = this.getTreeLock();
        synchronized (object) {
            boolean isWindowOpaque;
            int i;
            if (i < 0) {
                return;
            }
            if (this.paintingChild instanceof JComponent && this.paintingChild.isOpaque()) {
                for (i = this.getComponentCount() - 1; i >= 0 && this.getComponent(i) != this.paintingChild; --i) {
                }
            }
            Rectangle tmpRect = JComponent.fetchRectangle();
            boolean checkSiblings = !this.isOptimizedDrawingEnabled() && this.checkIfChildObscuredBySibling();
            Rectangle clipBounds = null;
            if (checkSiblings && (clipBounds = sg.getClipBounds()) == null) {
                clipBounds = new Rectangle(0, 0, this.getWidth(), this.getHeight());
            }
            boolean printing = this.getFlag(11);
            Window window = SwingUtilities.getWindowAncestor(this);
            boolean bl = isWindowOpaque = window == null || window.isOpaque();
            while (i >= 0) {
                block27: {
                    Rectangle cr;
                    boolean isJComponent;
                    Component comp;
                    block28: {
                        boolean hitClip;
                        comp = this.getComponent(i);
                        if (comp == null) break block27;
                        isJComponent = comp instanceof JComponent;
                        if (isWindowOpaque && !isJComponent && !JComponent.isLightweightComponent(comp) || !comp.isVisible()) break block27;
                        cr = comp.getBounds(tmpRect);
                        Shape clip = g.getClip();
                        boolean bl2 = hitClip = clip != null ? clip.intersects(cr.x, cr.y, cr.width, cr.height) : true;
                        if (!hitClip) break block27;
                        if (!checkSiblings || i <= 0) break block28;
                        int x = cr.x;
                        int y = cr.y;
                        int width = cr.width;
                        int height = cr.height;
                        SwingUtilities.computeIntersection(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height, cr);
                        if (this.getObscuredState(i, cr.x, cr.y, cr.width, cr.height) == 2) break block27;
                        cr.x = x;
                        cr.y = y;
                        cr.width = width;
                        cr.height = height;
                    }
                    Graphics cg = sg.create(cr.x, cr.y, cr.width, cr.height);
                    cg.setColor(comp.getForeground());
                    cg.setFont(comp.getFont());
                    boolean shouldSetFlagBack = false;
                    try {
                        if (isJComponent) {
                            if (this.getFlag(1)) {
                                ((JComponent)comp).setFlag(1, true);
                                shouldSetFlagBack = true;
                            }
                            if (this.getFlag(2)) {
                                ((JComponent)comp).setFlag(2, true);
                                shouldSetFlagBack = true;
                            }
                            if (!printing) {
                                comp.paint(cg);
                            } else if (!this.getFlag(12)) {
                                comp.print(cg);
                            } else {
                                comp.printAll(cg);
                            }
                        } else if (!printing) {
                            comp.paint(cg);
                        } else if (!this.getFlag(12)) {
                            comp.print(cg);
                        } else {
                            comp.printAll(cg);
                        }
                    }
                    finally {
                        cg.dispose();
                        if (shouldSetFlagBack) {
                            ((JComponent)comp).setFlag(1, false);
                            ((JComponent)comp).setFlag(2, false);
                        }
                    }
                }
                --i;
            }
            JComponent.recycleRectangle(tmpRect);
        }
    }

    protected void paintBorder(Graphics g) {
        Border border = this.getBorder();
        if (border != null) {
            border.paintBorder(this, g, 0, 0, this.getWidth(), this.getHeight());
        }
    }

    @Override
    public void update(Graphics g) {
        this.paint(g);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void paint(Graphics g) {
        block20: {
            boolean shouldClearPaintFlags = false;
            if (this.getWidth() <= 0 || this.getHeight() <= 0) {
                return;
            }
            Graphics componentGraphics = this.getComponentGraphics(g);
            Graphics co = componentGraphics.create();
            try {
                boolean printing;
                int clipH;
                int clipW;
                int clipX;
                int clipY;
                RepaintManager repaintManager = RepaintManager.currentManager(this);
                Rectangle clipRect = co.getClipBounds();
                if (clipRect == null) {
                    clipY = 0;
                    clipX = 0;
                    clipW = this.getWidth();
                    clipH = this.getHeight();
                } else {
                    clipX = clipRect.x;
                    clipY = clipRect.y;
                    clipW = clipRect.width;
                    clipH = clipRect.height;
                }
                if (clipW > this.getWidth()) {
                    clipW = this.getWidth();
                }
                if (clipH > this.getHeight()) {
                    clipH = this.getHeight();
                }
                if (this.getParent() != null && !(this.getParent() instanceof JComponent)) {
                    this.adjustPaintFlags();
                    shouldClearPaintFlags = true;
                }
                if (!(printing = this.getFlag(11)) && repaintManager.isDoubleBufferingEnabled() && !this.getFlag(1) && this.isDoubleBuffered() && (this.getFlag(13) || repaintManager.isPainting())) {
                    repaintManager.beginPaint();
                    try {
                        repaintManager.paint(this, this, co, clipX, clipY, clipW, clipH);
                        break block20;
                    }
                    finally {
                        repaintManager.endPaint();
                    }
                }
                if (clipRect == null) {
                    co.setClip(clipX, clipY, clipW, clipH);
                }
                if (!this.rectangleIsObscured(clipX, clipY, clipW, clipH)) {
                    if (!printing) {
                        this.paintComponent(co);
                        this.paintBorder(co);
                    } else {
                        this.printComponent(co);
                        this.printBorder(co);
                    }
                }
                if (!printing) {
                    this.paintChildren(co);
                } else {
                    this.printChildren(co);
                }
            }
            finally {
                co.dispose();
                if (shouldClearPaintFlags) {
                    this.setFlag(1, false);
                    this.setFlag(2, false);
                    this.setFlag(11, false);
                    this.setFlag(12, false);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void paintForceDoubleBuffered(Graphics g) {
        RepaintManager rm = RepaintManager.currentManager(this);
        Rectangle clip = g.getClipBounds();
        rm.beginPaint();
        this.setFlag(13, true);
        try {
            rm.paint(this, this, g, clip.x, clip.y, clip.width, clip.height);
        }
        finally {
            rm.endPaint();
            this.setFlag(13, false);
        }
    }

    boolean isPainting() {
        for (Container component = this; component != null; component = component.getParent()) {
            if (!(component instanceof JComponent) || !component.getFlag(1)) continue;
            return true;
        }
        return false;
    }

    private void adjustPaintFlags() {
        for (Container parent = this.getParent(); parent != null; parent = parent.getParent()) {
            if (!(parent instanceof JComponent)) continue;
            JComponent jparent = (JComponent)parent;
            if (jparent.getFlag(1)) {
                this.setFlag(1, true);
            }
            if (jparent.getFlag(2)) {
                this.setFlag(2, true);
            }
            if (jparent.getFlag(11)) {
                this.setFlag(11, true);
            }
            if (!jparent.getFlag(12)) break;
            this.setFlag(12, true);
            break;
        }
    }

    @Override
    public void printAll(Graphics g) {
        this.setFlag(12, true);
        try {
            this.print(g);
        }
        finally {
            this.setFlag(12, false);
        }
    }

    @Override
    public void print(Graphics g) {
        this.setFlag(11, true);
        this.firePropertyChange("paintingForPrint", false, true);
        try {
            this.paint(g);
        }
        finally {
            this.setFlag(11, false);
            this.firePropertyChange("paintingForPrint", true, false);
        }
    }

    protected void printComponent(Graphics g) {
        this.paintComponent(g);
    }

    protected void printChildren(Graphics g) {
        this.paintChildren(g);
    }

    protected void printBorder(Graphics g) {
        this.paintBorder(g);
    }

    @BeanProperty(bound=false)
    public boolean isPaintingTile() {
        return this.getFlag(2);
    }

    @BeanProperty(bound=false)
    public final boolean isPaintingForPrint() {
        return this.getFlag(11);
    }

    @Deprecated
    @BeanProperty(bound=false)
    public boolean isManagingFocus() {
        return false;
    }

    private void registerNextFocusableComponent() {
        this.registerNextFocusableComponent(this.getNextFocusableComponent());
    }

    private void registerNextFocusableComponent(Component nextFocusableComponent) {
        if (nextFocusableComponent == null) {
            return;
        }
        Container nearestRoot = this.isFocusCycleRoot() ? this : this.getFocusCycleRootAncestor();
        FocusTraversalPolicy policy = nearestRoot.getFocusTraversalPolicy();
        if (!(policy instanceof LegacyGlueFocusTraversalPolicy)) {
            policy = new LegacyGlueFocusTraversalPolicy(policy);
            nearestRoot.setFocusTraversalPolicy(policy);
        }
        ((LegacyGlueFocusTraversalPolicy)policy).setNextFocusableComponent(this, nextFocusableComponent);
    }

    private void deregisterNextFocusableComponent() {
        Container nearestRoot;
        Component nextFocusableComponent = this.getNextFocusableComponent();
        if (nextFocusableComponent == null) {
            return;
        }
        Container container = nearestRoot = this.isFocusCycleRoot() ? this : this.getFocusCycleRootAncestor();
        if (nearestRoot == null) {
            return;
        }
        FocusTraversalPolicy policy = nearestRoot.getFocusTraversalPolicy();
        if (policy instanceof LegacyGlueFocusTraversalPolicy) {
            ((LegacyGlueFocusTraversalPolicy)policy).unsetNextFocusableComponent(this, nextFocusableComponent);
        }
    }

    @Deprecated
    public void setNextFocusableComponent(Component aComponent) {
        boolean displayable = this.isDisplayable();
        if (displayable) {
            this.deregisterNextFocusableComponent();
        }
        this.putClientProperty(NEXT_FOCUS, aComponent);
        if (displayable) {
            this.registerNextFocusableComponent(aComponent);
        }
    }

    @Deprecated
    public Component getNextFocusableComponent() {
        return (Component)this.getClientProperty(NEXT_FOCUS);
    }

    public void setRequestFocusEnabled(boolean requestFocusEnabled) {
        this.setFlag(22, !requestFocusEnabled);
    }

    public boolean isRequestFocusEnabled() {
        return !this.getFlag(22);
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
    }

    @Override
    public boolean requestFocus(boolean temporary) {
        return super.requestFocus(temporary);
    }

    @Override
    public boolean requestFocusInWindow() {
        return super.requestFocusInWindow();
    }

    @Override
    protected boolean requestFocusInWindow(boolean temporary) {
        return super.requestFocusInWindow(temporary);
    }

    public void grabFocus() {
        this.requestFocus();
    }

    @BeanProperty(description="Whether the Component verifies input before accepting focus.")
    public void setVerifyInputWhenFocusTarget(boolean verifyInputWhenFocusTarget) {
        boolean oldVerifyInputWhenFocusTarget = this.verifyInputWhenFocusTarget;
        this.verifyInputWhenFocusTarget = verifyInputWhenFocusTarget;
        this.firePropertyChange("verifyInputWhenFocusTarget", oldVerifyInputWhenFocusTarget, verifyInputWhenFocusTarget);
    }

    public boolean getVerifyInputWhenFocusTarget() {
        return this.verifyInputWhenFocusTarget;
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        return SwingUtilities2.getFontMetrics(this, font);
    }

    @Override
    @BeanProperty(preferred=true, description="The preferred size of the component.")
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
    }

    @Override
    @Transient
    public Dimension getPreferredSize() {
        if (this.isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        Dimension size = null;
        if (this.ui != null) {
            size = this.ui.getPreferredSize(this);
        }
        return size != null ? size : super.getPreferredSize();
    }

    @Override
    @BeanProperty(description="The maximum size of the component.")
    public void setMaximumSize(Dimension maximumSize) {
        super.setMaximumSize(maximumSize);
    }

    @Override
    @Transient
    public Dimension getMaximumSize() {
        if (this.isMaximumSizeSet()) {
            return super.getMaximumSize();
        }
        Dimension size = null;
        if (this.ui != null) {
            size = this.ui.getMaximumSize(this);
        }
        return size != null ? size : super.getMaximumSize();
    }

    @Override
    @BeanProperty(description="The minimum size of the component.")
    public void setMinimumSize(Dimension minimumSize) {
        super.setMinimumSize(minimumSize);
    }

    @Override
    @Transient
    public Dimension getMinimumSize() {
        if (this.isMinimumSizeSet()) {
            return super.getMinimumSize();
        }
        Dimension size = null;
        if (this.ui != null) {
            size = this.ui.getMinimumSize(this);
        }
        return size != null ? size : super.getMinimumSize();
    }

    @Override
    public boolean contains(int x, int y) {
        return this.ui != null ? this.ui.contains(this, x, y) : super.contains(x, y);
    }

    @BeanProperty(preferred=true, visualUpdate=true, description="The component's border.")
    public void setBorder(Border border) {
        Border oldBorder = this.border;
        this.border = border;
        this.firePropertyChange("border", oldBorder, border);
        if (border != oldBorder) {
            if (border == null || oldBorder == null || !border.getBorderInsets(this).equals(oldBorder.getBorderInsets(this))) {
                this.revalidate();
            }
            this.repaint();
        }
    }

    public Border getBorder() {
        return this.border;
    }

    @Override
    @BeanProperty(expert=true)
    public Insets getInsets() {
        if (this.border != null) {
            return this.border.getBorderInsets(this);
        }
        return super.getInsets();
    }

    public Insets getInsets(Insets insets) {
        if (insets == null) {
            insets = new Insets(0, 0, 0, 0);
        }
        if (this.border != null) {
            if (this.border instanceof AbstractBorder) {
                return ((AbstractBorder)this.border).getBorderInsets(this, insets);
            }
            return this.border.getBorderInsets(this);
        }
        insets.bottom = 0;
        insets.right = 0;
        insets.top = 0;
        insets.left = 0;
        return insets;
    }

    @Override
    public float getAlignmentY() {
        if (this.isAlignmentYSet) {
            return this.alignmentY;
        }
        return super.getAlignmentY();
    }

    @BeanProperty(description="The preferred vertical alignment of the component.")
    public void setAlignmentY(float alignmentY) {
        this.alignmentY = this.validateAlignment(alignmentY);
        this.isAlignmentYSet = true;
    }

    @Override
    public float getAlignmentX() {
        if (this.isAlignmentXSet) {
            return this.alignmentX;
        }
        return super.getAlignmentX();
    }

    @BeanProperty(description="The preferred horizontal alignment of the component.")
    public void setAlignmentX(float alignmentX) {
        this.alignmentX = this.validateAlignment(alignmentX);
        this.isAlignmentXSet = true;
    }

    private float validateAlignment(float alignment) {
        return alignment > 1.0f ? 1.0f : (alignment < 0.0f ? 0.0f : alignment);
    }

    @BeanProperty(description="The component's input verifier.")
    public void setInputVerifier(InputVerifier inputVerifier) {
        InputVerifier oldInputVerifier = (InputVerifier)this.getClientProperty((Object)ClientPropertyKey.JComponent_INPUT_VERIFIER);
        this.putClientProperty((Object)ClientPropertyKey.JComponent_INPUT_VERIFIER, inputVerifier);
        this.firePropertyChange("inputVerifier", oldInputVerifier, inputVerifier);
    }

    public InputVerifier getInputVerifier() {
        return (InputVerifier)this.getClientProperty((Object)ClientPropertyKey.JComponent_INPUT_VERIFIER);
    }

    @Override
    @BeanProperty(bound=false)
    public Graphics getGraphics() {
        if (DEBUG_GRAPHICS_LOADED && this.shouldDebugGraphics() != 0) {
            DebugGraphics graphics = new DebugGraphics(super.getGraphics(), this);
            return graphics;
        }
        return super.getGraphics();
    }

    @BeanProperty(bound=false, preferred=true, enumerationValues={"DebugGraphics.NONE_OPTION", "DebugGraphics.LOG_OPTION", "DebugGraphics.FLASH_OPTION", "DebugGraphics.BUFFERED_OPTION"}, description="Diagnostic options for graphics operations.")
    public void setDebugGraphicsOptions(int debugOptions) {
        DebugGraphics.setDebugOptions(this, debugOptions);
    }

    public int getDebugGraphicsOptions() {
        return DebugGraphics.getDebugOptions(this);
    }

    int shouldDebugGraphics() {
        return DebugGraphics.shouldComponentDebug(this);
    }

    public void registerKeyboardAction(ActionListener anAction, String aCommand, KeyStroke aKeyStroke, int aCondition) {
        InputMap inputMap = this.getInputMap(aCondition, true);
        if (inputMap != null) {
            ActionMap actionMap = this.getActionMap(true);
            ActionStandin action = new ActionStandin(anAction, aCommand);
            inputMap.put(aKeyStroke, action);
            if (actionMap != null) {
                actionMap.put(action, action);
            }
        }
    }

    private void registerWithKeyboardManager(boolean onlyIfNew) {
        int counter;
        KeyStroke[] strokes;
        InputMap inputMap = this.getInputMap(2, false);
        Hashtable<KeyStroke, KeyStroke> registered = (Hashtable<KeyStroke, KeyStroke>)this.getClientProperty(WHEN_IN_FOCUSED_WINDOW_BINDINGS);
        if (inputMap != null) {
            strokes = inputMap.allKeys();
            if (strokes != null) {
                for (counter = strokes.length - 1; counter >= 0; --counter) {
                    if (!onlyIfNew || registered == null || registered.get(strokes[counter]) == null) {
                        this.registerWithKeyboardManager(strokes[counter]);
                    }
                    if (registered == null) continue;
                    registered.remove(strokes[counter]);
                }
            }
        } else {
            strokes = null;
        }
        if (registered != null && registered.size() > 0) {
            Enumeration keys = registered.keys();
            while (keys.hasMoreElements()) {
                KeyStroke ks = (KeyStroke)keys.nextElement();
                this.unregisterWithKeyboardManager(ks);
            }
            registered.clear();
        }
        if (strokes != null && strokes.length > 0) {
            if (registered == null) {
                registered = new Hashtable<KeyStroke, KeyStroke>(strokes.length);
                this.putClientProperty(WHEN_IN_FOCUSED_WINDOW_BINDINGS, registered);
            }
            for (counter = strokes.length - 1; counter >= 0; --counter) {
                registered.put(strokes[counter], strokes[counter]);
            }
        } else {
            this.putClientProperty(WHEN_IN_FOCUSED_WINDOW_BINDINGS, null);
        }
    }

    private void unregisterWithKeyboardManager() {
        Hashtable registered = (Hashtable)this.getClientProperty(WHEN_IN_FOCUSED_WINDOW_BINDINGS);
        if (registered != null && registered.size() > 0) {
            Enumeration keys = registered.keys();
            while (keys.hasMoreElements()) {
                KeyStroke ks = (KeyStroke)keys.nextElement();
                this.unregisterWithKeyboardManager(ks);
            }
        }
        this.putClientProperty(WHEN_IN_FOCUSED_WINDOW_BINDINGS, null);
    }

    void componentInputMapChanged(ComponentInputMap inputMap) {
        InputMap km;
        for (km = this.getInputMap(2, false); km != inputMap && km != null; km = km.getParent()) {
        }
        if (km != null) {
            this.registerWithKeyboardManager(false);
        }
    }

    private void registerWithKeyboardManager(KeyStroke aKeyStroke) {
        KeyboardManager.getCurrentManager().registerKeyStroke(aKeyStroke, this);
    }

    private void unregisterWithKeyboardManager(KeyStroke aKeyStroke) {
        KeyboardManager.getCurrentManager().unregisterKeyStroke(aKeyStroke, this);
    }

    public void registerKeyboardAction(ActionListener anAction, KeyStroke aKeyStroke, int aCondition) {
        this.registerKeyboardAction(anAction, null, aKeyStroke, aCondition);
    }

    public void unregisterKeyboardAction(KeyStroke aKeyStroke) {
        ActionMap am = this.getActionMap(false);
        for (int counter = 0; counter < 3; ++counter) {
            InputMap km = this.getInputMap(counter, false);
            if (km == null) continue;
            Object actionID = km.get(aKeyStroke);
            if (am != null && actionID != null) {
                am.remove(actionID);
            }
            km.remove(aKeyStroke);
        }
    }

    @BeanProperty(bound=false)
    public KeyStroke[] getRegisteredKeyStrokes() {
        int[] counts = new int[3];
        KeyStroke[][] strokes = new KeyStroke[3][];
        for (int counter = 0; counter < 3; ++counter) {
            InputMap km = this.getInputMap(counter, false);
            strokes[counter] = km != null ? km.allKeys() : null;
            counts[counter] = strokes[counter] != null ? strokes[counter].length : 0;
        }
        KeyStroke[] retValue = new KeyStroke[counts[0] + counts[1] + counts[2]];
        int last = 0;
        for (int counter = 0; counter < 3; ++counter) {
            if (counts[counter] <= 0) continue;
            System.arraycopy(strokes[counter], 0, retValue, last, counts[counter]);
            last += counts[counter];
        }
        return retValue;
    }

    public int getConditionForKeyStroke(KeyStroke aKeyStroke) {
        for (int counter = 0; counter < 3; ++counter) {
            InputMap inputMap = this.getInputMap(counter, false);
            if (inputMap == null || inputMap.get(aKeyStroke) == null) continue;
            return counter;
        }
        return -1;
    }

    public ActionListener getActionForKeyStroke(KeyStroke aKeyStroke) {
        ActionMap am = this.getActionMap(false);
        if (am == null) {
            return null;
        }
        for (int counter = 0; counter < 3; ++counter) {
            Object actionBinding;
            InputMap inputMap = this.getInputMap(counter, false);
            if (inputMap == null || (actionBinding = inputMap.get(aKeyStroke)) == null) continue;
            Action action = am.get(actionBinding);
            if (action instanceof ActionStandin) {
                return ((ActionStandin)action).actionListener;
            }
            return action;
        }
        return null;
    }

    public void resetKeyboardActions() {
        for (int counter = 0; counter < 3; ++counter) {
            InputMap inputMap = this.getInputMap(counter, false);
            if (inputMap == null) continue;
            inputMap.clear();
        }
        ActionMap am = this.getActionMap(false);
        if (am != null) {
            am.clear();
        }
    }

    public final void setInputMap(int condition, InputMap map) {
        switch (condition) {
            case 2: {
                if (map != null && !(map instanceof ComponentInputMap)) {
                    throw new IllegalArgumentException("WHEN_IN_FOCUSED_WINDOW InputMaps must be of type ComponentInputMap");
                }
                this.windowInputMap = (ComponentInputMap)map;
                this.setFlag(7, true);
                this.registerWithKeyboardManager(false);
                break;
            }
            case 1: {
                this.ancestorInputMap = map;
                this.setFlag(6, true);
                break;
            }
            case 0: {
                this.focusInputMap = map;
                this.setFlag(5, true);
                break;
            }
            default: {
                throw new IllegalArgumentException("condition must be one of JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_FOCUSED or JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT");
            }
        }
    }

    public final InputMap getInputMap(int condition) {
        return this.getInputMap(condition, true);
    }

    public final InputMap getInputMap() {
        return this.getInputMap(0, true);
    }

    public final void setActionMap(ActionMap am) {
        this.actionMap = am;
        this.setFlag(8, true);
    }

    public final ActionMap getActionMap() {
        return this.getActionMap(true);
    }

    final InputMap getInputMap(int condition, boolean create) {
        switch (condition) {
            case 0: {
                if (this.getFlag(5)) {
                    return this.focusInputMap;
                }
                if (!create) break;
                InputMap km = new InputMap();
                this.setInputMap(condition, km);
                return km;
            }
            case 1: {
                if (this.getFlag(6)) {
                    return this.ancestorInputMap;
                }
                if (!create) break;
                InputMap km = new InputMap();
                this.setInputMap(condition, km);
                return km;
            }
            case 2: {
                if (this.getFlag(7)) {
                    return this.windowInputMap;
                }
                if (!create) break;
                ComponentInputMap km = new ComponentInputMap(this);
                this.setInputMap(condition, km);
                return km;
            }
            default: {
                throw new IllegalArgumentException("condition must be one of JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_FOCUSED or JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT");
            }
        }
        return null;
    }

    final ActionMap getActionMap(boolean create) {
        if (this.getFlag(8)) {
            return this.actionMap;
        }
        if (create) {
            ActionMap am = new ActionMap();
            this.setActionMap(am);
            return am;
        }
        return null;
    }

    @Override
    public int getBaseline(int width, int height) {
        super.getBaseline(width, height);
        if (this.ui != null) {
            return this.ui.getBaseline(this, width, height);
        }
        return -1;
    }

    @Override
    @BeanProperty(bound=false)
    public Component.BaselineResizeBehavior getBaselineResizeBehavior() {
        if (this.ui != null) {
            return this.ui.getBaselineResizeBehavior(this);
        }
        return Component.BaselineResizeBehavior.OTHER;
    }

    @Deprecated
    public boolean requestDefaultFocus() {
        Container nearestRoot;
        Container container = nearestRoot = this.isFocusCycleRoot() ? this : this.getFocusCycleRootAncestor();
        if (nearestRoot == null) {
            return false;
        }
        Component comp = nearestRoot.getFocusTraversalPolicy().getDefaultComponent(nearestRoot);
        if (comp != null) {
            comp.requestFocus();
            return true;
        }
        return false;
    }

    @Override
    @BeanProperty(hidden=true, visualUpdate=true)
    public void setVisible(boolean aFlag) {
        if (aFlag != this.isVisible()) {
            super.setVisible(aFlag);
            if (aFlag) {
                Container parent = this.getParent();
                if (parent != null) {
                    Rectangle r = this.getBounds();
                    parent.repaint(r.x, r.y, r.width, r.height);
                }
                this.revalidate();
            }
        }
    }

    @Override
    @BeanProperty(expert=true, preferred=true, visualUpdate=true, description="The enabled state of the component.")
    public void setEnabled(boolean enabled) {
        boolean oldEnabled = this.isEnabled();
        super.setEnabled(enabled);
        this.firePropertyChange("enabled", oldEnabled, enabled);
        if (enabled != oldEnabled) {
            this.repaint();
        }
    }

    @Override
    @BeanProperty(preferred=true, visualUpdate=true, description="The foreground color of the component.")
    public void setForeground(Color fg) {
        Color oldFg = this.getForeground();
        super.setForeground(fg);
        if (oldFg != null ? !oldFg.equals(fg) : fg != null && !fg.equals(oldFg)) {
            this.repaint();
        }
    }

    @Override
    @BeanProperty(preferred=true, visualUpdate=true, description="The background color of the component.")
    public void setBackground(Color bg) {
        Color oldBg = this.getBackground();
        super.setBackground(bg);
        if (oldBg != null ? !oldBg.equals(bg) : bg != null && !bg.equals(oldBg)) {
            this.repaint();
        }
    }

    @Override
    @BeanProperty(preferred=true, visualUpdate=true, description="The font for the component.")
    public void setFont(Font font) {
        Font oldFont = this.getFont();
        super.setFont(font);
        if (font != oldFont) {
            this.revalidate();
            this.repaint();
        }
    }

    public static Locale getDefaultLocale() {
        Locale l = (Locale)SwingUtilities.appContextGet(defaultLocale);
        if (l == null) {
            l = Locale.getDefault();
            JComponent.setDefaultLocale(l);
        }
        return l;
    }

    public static void setDefaultLocale(Locale l) {
        SwingUtilities.appContextPut(defaultLocale, l);
    }

    protected void processComponentKeyEvent(KeyEvent e) {
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        super.processKeyEvent(e);
        if (!e.isConsumed()) {
            this.processComponentKeyEvent(e);
        }
        boolean shouldProcessKey = KeyboardState.shouldProcess(e);
        if (e.isConsumed()) {
            return;
        }
        if (shouldProcessKey && this.processKeyBindings(e, e.getID() == 401)) {
            e.consume();
        }
    }

    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        InputMap map = this.getInputMap(condition, false);
        ActionMap am = this.getActionMap(false);
        if (map != null && am != null && this.isEnabled()) {
            Action action;
            Object binding = map.get(ks);
            Action action2 = action = binding == null ? null : am.get(binding);
            if (action != null) {
                return SwingUtilities.notifyAction(action, ks, e, this, e.getModifiers());
            }
        }
        return false;
    }

    boolean processKeyBindings(KeyEvent e, boolean pressed) {
        Container parent;
        KeyStroke ks;
        if (!SwingUtilities.isValidKeyEventForKeyBindings(e)) {
            return false;
        }
        KeyStroke ksE = null;
        if (e.getID() == 400) {
            ks = KeyStroke.getKeyStroke(e.getKeyChar());
        } else {
            ks = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers(), !pressed);
            if (e.getKeyCode() != e.getExtendedKeyCode()) {
                ksE = KeyStroke.getKeyStroke(e.getExtendedKeyCode(), e.getModifiers(), !pressed);
            }
        }
        if (ksE != null && this.processKeyBinding(ksE, e, 0, pressed)) {
            return true;
        }
        if (this.processKeyBinding(ks, e, 0, pressed)) {
            return true;
        }
        for (parent = this; parent != null && !(parent instanceof Window) && !(parent instanceof Applet); parent = parent.getParent()) {
            if (parent instanceof JComponent) {
                if (ksE != null && parent.processKeyBinding(ksE, e, 1, pressed)) {
                    return true;
                }
                if (parent.processKeyBinding(ks, e, 1, pressed)) {
                    return true;
                }
            }
            if (!(parent instanceof JInternalFrame) || !JComponent.processKeyBindingsForAllComponents(e, parent, pressed)) continue;
            return true;
        }
        if (parent != null) {
            return JComponent.processKeyBindingsForAllComponents(e, parent, pressed);
        }
        return false;
    }

    static boolean processKeyBindingsForAllComponents(KeyEvent e, Container container, boolean pressed) {
        while (true) {
            if (KeyboardManager.getCurrentManager().fireKeyboardAction(e, pressed, container)) {
                return true;
            }
            if (!(container instanceof Popup.HeavyWeightWindow)) break;
            container = ((Window)container).getOwner();
        }
        return false;
    }

    @BeanProperty(bound=false, preferred=true, description="The text to display in a tool tip.")
    public void setToolTipText(String text) {
        String oldText = this.getToolTipText();
        this.putClientProperty(TOOL_TIP_TEXT_KEY, text);
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        if (text != null) {
            if (oldText == null) {
                toolTipManager.registerComponent(this);
            }
        } else {
            toolTipManager.unregisterComponent(this);
        }
    }

    public String getToolTipText() {
        return (String)this.getClientProperty(TOOL_TIP_TEXT_KEY);
    }

    public String getToolTipText(MouseEvent event) {
        return this.getToolTipText();
    }

    public Point getToolTipLocation(MouseEvent event) {
        return null;
    }

    public Point getPopupLocation(MouseEvent event) {
        return null;
    }

    public JToolTip createToolTip() {
        JToolTip tip = new JToolTip();
        tip.setComponent(this);
        return tip;
    }

    public void scrollRectToVisible(Rectangle aRect) {
        Container parent;
        int dx = this.getX();
        int dy = this.getY();
        for (parent = this.getParent(); parent != null && !(parent instanceof JComponent) && !(parent instanceof CellRendererPane); parent = parent.getParent()) {
            Rectangle bounds = parent.getBounds();
            dx += bounds.x;
            dy += bounds.y;
        }
        if (parent != null && !(parent instanceof CellRendererPane)) {
            aRect.x += dx;
            aRect.y += dy;
            ((JComponent)parent).scrollRectToVisible(aRect);
            aRect.x -= dx;
            aRect.y -= dy;
        }
    }

    @BeanProperty(bound=false, expert=true, description="Determines if this component automatically scrolls its contents when dragged.")
    public void setAutoscrolls(boolean autoscrolls) {
        this.setFlag(25, true);
        if (this.autoscrolls != autoscrolls) {
            this.autoscrolls = autoscrolls;
            if (autoscrolls) {
                this.enableEvents(16L);
                this.enableEvents(32L);
            } else {
                Autoscroller.stop(this);
            }
        }
    }

    public boolean getAutoscrolls() {
        return this.autoscrolls;
    }

    @BeanProperty(hidden=true, description="Mechanism for transfer of data to and from the component")
    public void setTransferHandler(TransferHandler newHandler) {
        TransferHandler oldHandler = (TransferHandler)this.getClientProperty((Object)ClientPropertyKey.JComponent_TRANSFER_HANDLER);
        this.putClientProperty((Object)ClientPropertyKey.JComponent_TRANSFER_HANDLER, newHandler);
        SwingUtilities.installSwingDropTargetAsNecessary(this, newHandler);
        this.firePropertyChange("transferHandler", oldHandler, newHandler);
    }

    @Override
    public TransferHandler getTransferHandler() {
        return (TransferHandler)this.getClientProperty((Object)ClientPropertyKey.JComponent_TRANSFER_HANDLER);
    }

    TransferHandler.DropLocation dropLocationForPoint(Point p) {
        return null;
    }

    Object setDropLocation(TransferHandler.DropLocation location, Object state, boolean forDrop) {
        return null;
    }

    void dndDone() {
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (this.autoscrolls && e.getID() == 502) {
            Autoscroller.stop(this);
        }
        super.processMouseEvent(e);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        boolean dispatch = true;
        if (this.autoscrolls && e.getID() == 506) {
            dispatch = !Autoscroller.isRunning(this);
            Autoscroller.processMouseDragged(e);
        }
        if (dispatch) {
            super.processMouseMotionEvent(e);
        }
    }

    void superProcessMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(e);
    }

    void setCreatedDoubleBuffer(boolean newValue) {
        this.setFlag(9, newValue);
    }

    boolean getCreatedDoubleBuffer() {
        return this.getFlag(9);
    }

    @Override
    @Deprecated
    public void enable() {
        if (!this.isEnabled()) {
            super.enable();
            if (this.accessibleContext != null) {
                this.accessibleContext.firePropertyChange("AccessibleState", null, AccessibleState.ENABLED);
            }
        }
    }

    @Override
    @Deprecated
    public void disable() {
        if (this.isEnabled()) {
            super.disable();
            if (this.accessibleContext != null) {
                this.accessibleContext.firePropertyChange("AccessibleState", AccessibleState.ENABLED, null);
            }
        }
    }

    private ArrayTable getClientProperties() {
        if (this.clientProperties == null) {
            this.clientProperties = new ArrayTable();
        }
        return this.clientProperties;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final Object getClientProperty(Object key) {
        if (key == RenderingHints.KEY_TEXT_ANTIALIASING) {
            return this.aaHint;
        }
        if (key == RenderingHints.KEY_TEXT_LCD_CONTRAST) {
            return this.lcdRenderingHint;
        }
        if (this.clientProperties == null) {
            return null;
        }
        ArrayTable arrayTable = this.clientProperties;
        synchronized (arrayTable) {
            return this.clientProperties.get(key);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void putClientProperty(Object key, Object value) {
        Object oldValue;
        ArrayTable clientProperties;
        if (key == RenderingHints.KEY_TEXT_ANTIALIASING) {
            this.aaHint = value;
            return;
        }
        if (key == RenderingHints.KEY_TEXT_LCD_CONTRAST) {
            this.lcdRenderingHint = value;
            return;
        }
        if (value == null && this.clientProperties == null) {
            return;
        }
        ArrayTable arrayTable = clientProperties = this.getClientProperties();
        synchronized (arrayTable) {
            oldValue = clientProperties.get(key);
            if (value != null) {
                clientProperties.put(key, value);
            } else if (oldValue != null) {
                clientProperties.remove(key);
            } else {
                return;
            }
        }
        this.clientPropertyChanged(key, oldValue, value);
        this.firePropertyChange(key.toString(), oldValue, value);
    }

    void clientPropertyChanged(Object key, Object oldValue, Object newValue) {
    }

    void setUIProperty(String propertyName, Object value) {
        if ("opaque".equals(propertyName)) {
            if (!this.getFlag(24)) {
                this.setOpaque((Boolean)value);
                this.setFlag(24, false);
            }
        } else if ("autoscrolls".equals(propertyName)) {
            if (!this.getFlag(25)) {
                this.setAutoscrolls((Boolean)value);
                this.setFlag(25, false);
            }
        } else if ("focusTraversalKeysForward".equals(propertyName)) {
            Set strokeSet = (Set)value;
            if (!this.getFlag(26)) {
                super.setFocusTraversalKeys(0, strokeSet);
            }
        } else if ("focusTraversalKeysBackward".equals(propertyName)) {
            Set strokeSet = (Set)value;
            if (!this.getFlag(27)) {
                super.setFocusTraversalKeys(1, strokeSet);
            }
        } else {
            throw new IllegalArgumentException("property \"" + propertyName + "\" cannot be set using this method");
        }
    }

    @Override
    public void setFocusTraversalKeys(int id, Set<? extends AWTKeyStroke> keystrokes) {
        if (id == 0) {
            this.setFlag(26, true);
        } else if (id == 1) {
            this.setFlag(27, true);
        }
        super.setFocusTraversalKeys(id, keystrokes);
    }

    public static boolean isLightweightComponent(Component c) {
        return AWTAccessor.getComponentAccessor().isLightweight(c);
    }

    @Override
    @Deprecated
    public void reshape(int x, int y, int w, int h) {
        super.reshape(x, y, w, h);
    }

    @Override
    public Rectangle getBounds(Rectangle rv) {
        if (rv == null) {
            return new Rectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
        rv.setBounds(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        return rv;
    }

    @Override
    public Dimension getSize(Dimension rv) {
        if (rv == null) {
            return new Dimension(this.getWidth(), this.getHeight());
        }
        rv.setSize(this.getWidth(), this.getHeight());
        return rv;
    }

    @Override
    public Point getLocation(Point rv) {
        if (rv == null) {
            return new Point(this.getX(), this.getY());
        }
        rv.setLocation(this.getX(), this.getY());
        return rv;
    }

    @Override
    @BeanProperty(bound=false)
    public int getX() {
        return super.getX();
    }

    @Override
    @BeanProperty(bound=false)
    public int getY() {
        return super.getY();
    }

    @Override
    @BeanProperty(bound=false)
    public int getWidth() {
        return super.getWidth();
    }

    @Override
    @BeanProperty(bound=false)
    public int getHeight() {
        return super.getHeight();
    }

    @Override
    public boolean isOpaque() {
        return this.getFlag(3);
    }

    @BeanProperty(expert=true, description="The component's opacity")
    public void setOpaque(boolean isOpaque) {
        boolean oldValue = this.getFlag(3);
        this.setFlag(3, isOpaque);
        this.setFlag(24, true);
        this.firePropertyChange("opaque", oldValue, isOpaque);
    }

    boolean rectangleIsObscured(int x, int y, int width, int height) {
        int numChildren = this.getComponentCount();
        for (int i = 0; i < numChildren; ++i) {
            Component child = this.getComponent(i);
            int cx = child.getX();
            int cy = child.getY();
            int cw = child.getWidth();
            int ch = child.getHeight();
            if (x < cx || x + width > cx + cw || y < cy || y + height > cy + ch || !child.isVisible()) continue;
            if (child instanceof JComponent) {
                return child.isOpaque();
            }
            return false;
        }
        return false;
    }

    static final void computeVisibleRect(Component c, Rectangle visibleRect) {
        Container p = c.getParent();
        Rectangle bounds = c.getBounds();
        if (p == null || p instanceof Window || p instanceof Applet) {
            visibleRect.setBounds(0, 0, bounds.width, bounds.height);
        } else {
            JComponent.computeVisibleRect(p, visibleRect);
            visibleRect.x -= bounds.x;
            visibleRect.y -= bounds.y;
            SwingUtilities.computeIntersection(0, 0, bounds.width, bounds.height, visibleRect);
        }
    }

    public void computeVisibleRect(Rectangle visibleRect) {
        JComponent.computeVisibleRect(this, visibleRect);
    }

    @BeanProperty(bound=false)
    public Rectangle getVisibleRect() {
        Rectangle visibleRect = new Rectangle();
        this.computeVisibleRect(visibleRect);
        return visibleRect;
    }

    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {
        if (this.vetoableChangeSupport == null) {
            return;
        }
        this.vetoableChangeSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }

    public synchronized void addVetoableChangeListener(VetoableChangeListener listener) {
        if (this.vetoableChangeSupport == null) {
            this.vetoableChangeSupport = new VetoableChangeSupport(this);
        }
        this.vetoableChangeSupport.addVetoableChangeListener(listener);
    }

    public synchronized void removeVetoableChangeListener(VetoableChangeListener listener) {
        if (this.vetoableChangeSupport == null) {
            return;
        }
        this.vetoableChangeSupport.removeVetoableChangeListener(listener);
    }

    @BeanProperty(bound=false)
    public synchronized VetoableChangeListener[] getVetoableChangeListeners() {
        if (this.vetoableChangeSupport == null) {
            return new VetoableChangeListener[0];
        }
        return this.vetoableChangeSupport.getVetoableChangeListeners();
    }

    @BeanProperty(bound=false)
    public Container getTopLevelAncestor() {
        for (Container p = this; p != null; p = p.getParent()) {
            if (!(p instanceof Window) && !(p instanceof Applet)) continue;
            return p;
        }
        return null;
    }

    private AncestorNotifier getAncestorNotifier() {
        return (AncestorNotifier)this.getClientProperty((Object)ClientPropertyKey.JComponent_ANCESTOR_NOTIFIER);
    }

    public void addAncestorListener(AncestorListener listener) {
        AncestorNotifier ancestorNotifier = this.getAncestorNotifier();
        if (ancestorNotifier == null) {
            ancestorNotifier = new AncestorNotifier(this);
            this.putClientProperty((Object)ClientPropertyKey.JComponent_ANCESTOR_NOTIFIER, ancestorNotifier);
        }
        ancestorNotifier.addAncestorListener(listener);
    }

    public void removeAncestorListener(AncestorListener listener) {
        AncestorNotifier ancestorNotifier = this.getAncestorNotifier();
        if (ancestorNotifier == null) {
            return;
        }
        ancestorNotifier.removeAncestorListener(listener);
        if (ancestorNotifier.listenerList.getListenerList().length == 0) {
            ancestorNotifier.removeAllListeners();
            this.putClientProperty((Object)ClientPropertyKey.JComponent_ANCESTOR_NOTIFIER, null);
        }
    }

    @BeanProperty(bound=false)
    public AncestorListener[] getAncestorListeners() {
        AncestorNotifier ancestorNotifier = this.getAncestorNotifier();
        if (ancestorNotifier == null) {
            return new AncestorListener[0];
        }
        return ancestorNotifier.getAncestorListeners();
    }

    @Override
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener[] result = listenerType == AncestorListener.class ? (EventListener[])this.getAncestorListeners() : (listenerType == VetoableChangeListener.class ? (EventListener[])this.getVetoableChangeListeners() : (listenerType == PropertyChangeListener.class ? (EventListener[])this.getPropertyChangeListeners() : this.listenerList.getListeners(listenerType)));
        if (result.length == 0) {
            return super.getListeners(listenerType);
        }
        return result;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.firePropertyChange("ancestor", null, this.getParent());
        this.registerWithKeyboardManager(false);
        this.registerNextFocusableComponent();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        this.firePropertyChange("ancestor", this.getParent(), null);
        this.unregisterWithKeyboardManager();
        this.deregisterNextFocusableComponent();
        if (this.getCreatedDoubleBuffer()) {
            RepaintManager.currentManager(this).resetDoubleBuffer();
            this.setCreatedDoubleBuffer(false);
        }
        if (this.autoscrolls) {
            Autoscroller.stop(this);
        }
    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
        RepaintManager.currentManager(SunToolkit.targetToAppContext(this)).addDirtyRegion(this, x, y, width, height);
    }

    public void repaint(Rectangle r) {
        this.repaint(0L, r.x, r.y, r.width, r.height);
    }

    @Override
    public void revalidate() {
        if (this.getParent() == null) {
            return;
        }
        if (SunToolkit.isDispatchThreadForAppContext(this)) {
            this.invalidate();
            RepaintManager.currentManager(this).addInvalidComponent(this);
        } else {
            if (this.revalidateRunnableScheduled.getAndSet(true)) {
                return;
            }
            SunToolkit.executeOnEventHandlerThread(this, () -> {
                this.revalidateRunnableScheduled.set(false);
                this.revalidate();
            });
        }
    }

    @Override
    public boolean isValidateRoot() {
        return false;
    }

    @BeanProperty(bound=false)
    public boolean isOptimizedDrawingEnabled() {
        return true;
    }

    protected boolean isPaintingOrigin() {
        return false;
    }

    public void paintImmediately(int x, int y, int w, int h) {
        Container parent;
        Container c = this;
        if (!this.isShowing()) {
            return;
        }
        JComponent paintingOigin = SwingUtilities.getPaintingOrigin(this);
        if (paintingOigin != null) {
            Rectangle rectangle = SwingUtilities.convertRectangle(c, new Rectangle(x, y, w, h), paintingOigin);
            paintingOigin.paintImmediately(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
            return;
        }
        while (!((Component)c).isOpaque() && (parent = c.getParent()) != null) {
            x += ((Component)c).getX();
            y += ((Component)c).getY();
            c = parent;
            if (c instanceof JComponent) continue;
        }
        if (c instanceof JComponent) {
            c._paintImmediately(x, y, w, h);
        } else {
            c.repaint(x, y, w, h);
        }
    }

    public void paintImmediately(Rectangle r) {
        this.paintImmediately(r.x, r.y, r.width, r.height);
    }

    boolean alwaysOnTop() {
        return false;
    }

    void setPaintingChild(Component paintingChild) {
        this.paintingChild = paintingChild;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void _paintImmediately(int x, int y, int w, int h) {
        Rectangle paintImmediatelyClip;
        block31: {
            Component comp;
            int i;
            Container c;
            boolean ontop;
            int offsetX = 0;
            int offsetY = 0;
            boolean hasBuffer = false;
            JComponent bufferedComponent = null;
            JComponent paintingComponent = this;
            RepaintManager repaintManager = RepaintManager.currentManager(this);
            ArrayList<JComponent> path = new ArrayList<JComponent>(7);
            int pIndex = -1;
            int pCount = 0;
            int tmpHeight = 0;
            int tmpWidth = 0;
            int tmpY = 0;
            int tmpX = 0;
            paintImmediatelyClip = JComponent.fetchRectangle();
            paintImmediatelyClip.x = x;
            paintImmediatelyClip.y = y;
            paintImmediatelyClip.width = w;
            paintImmediatelyClip.height = h;
            boolean bl = ontop = this.alwaysOnTop() && this.isOpaque();
            if (ontop) {
                SwingUtilities.computeIntersection(0, 0, this.getWidth(), this.getHeight(), paintImmediatelyClip);
                if (paintImmediatelyClip.width == 0) {
                    JComponent.recycleRectangle(paintImmediatelyClip);
                    return;
                }
            }
            JComponent child = null;
            for (c = this; c != null && !(c instanceof Window) && !(c instanceof Applet); c = c.getParent()) {
                JComponent jc = c instanceof JComponent ? c : null;
                path.add((JComponent)c);
                if (!ontop && jc != null && !jc.isOptimizedDrawingEnabled()) {
                    boolean resetPC;
                    if (c != this) {
                        if (jc.isPaintingOrigin()) {
                            resetPC = true;
                        } else {
                            int i2;
                            Component[] children = c.getComponents();
                            for (i2 = 0; i2 < children.length && children[i2] != child; ++i2) {
                            }
                            switch (jc.getObscuredState(i2, paintImmediatelyClip.x, paintImmediatelyClip.y, paintImmediatelyClip.width, paintImmediatelyClip.height)) {
                                case 0: {
                                    resetPC = false;
                                    break;
                                }
                                case 2: {
                                    JComponent.recycleRectangle(paintImmediatelyClip);
                                    return;
                                }
                                default: {
                                    resetPC = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        resetPC = false;
                    }
                    if (resetPC) {
                        paintingComponent = jc;
                        pIndex = pCount;
                        offsetY = 0;
                        offsetX = 0;
                        hasBuffer = false;
                    }
                }
                ++pCount;
                if (repaintManager.isDoubleBufferingEnabled() && jc != null && jc.isDoubleBuffered()) {
                    hasBuffer = true;
                    bufferedComponent = jc;
                }
                if (!ontop) {
                    int bx = c.getX();
                    int by = c.getY();
                    tmpWidth = c.getWidth();
                    tmpHeight = c.getHeight();
                    SwingUtilities.computeIntersection(tmpX, tmpY, tmpWidth, tmpHeight, paintImmediatelyClip);
                    paintImmediatelyClip.x += bx;
                    paintImmediatelyClip.y += by;
                    offsetX += bx;
                    offsetY += by;
                }
                child = c;
            }
            if (c == null || !c.isDisplayable() || paintImmediatelyClip.width <= 0 || paintImmediatelyClip.height <= 0) {
                JComponent.recycleRectangle(paintImmediatelyClip);
                return;
            }
            paintingComponent.setFlag(13, true);
            paintImmediatelyClip.x -= offsetX;
            paintImmediatelyClip.y -= offsetY;
            if (paintingComponent != this) {
                for (i = pIndex; i > 0; --i) {
                    comp = (Component)path.get(i);
                    if (!(comp instanceof JComponent)) continue;
                    ((JComponent)comp).setPaintingChild((Component)path.get(i - 1));
                }
            }
            try {
                Graphics g = JComponent.safelyGetGraphics(paintingComponent, c);
                if (g == null) break block31;
                try {
                    if (hasBuffer) {
                        RepaintManager rm = RepaintManager.currentManager(bufferedComponent);
                        rm.beginPaint();
                        try {
                            rm.paint(paintingComponent, bufferedComponent, g, paintImmediatelyClip.x, paintImmediatelyClip.y, paintImmediatelyClip.width, paintImmediatelyClip.height);
                            break block31;
                        }
                        finally {
                            rm.endPaint();
                        }
                    }
                    g.setClip(paintImmediatelyClip.x, paintImmediatelyClip.y, paintImmediatelyClip.width, paintImmediatelyClip.height);
                    paintingComponent.paint(g);
                }
                finally {
                    g.dispose();
                }
            }
            finally {
                if (paintingComponent != this) {
                    for (i = pIndex; i > 0; --i) {
                        comp = (Component)path.get(i);
                        if (!(comp instanceof JComponent)) continue;
                        ((JComponent)comp).setPaintingChild(null);
                    }
                }
                paintingComponent.setFlag(13, false);
            }
        }
        JComponent.recycleRectangle(paintImmediatelyClip);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void paintToOffscreen(Graphics g, int x, int y, int w, int h, int maxX, int maxY) {
        try {
            this.setFlag(1, true);
            if (y + h < maxY || x + w < maxX) {
                this.setFlag(2, true);
            }
            if (this.getFlag(13)) {
                this.paint(g);
            } else {
                if (!this.rectangleIsObscured(x, y, w, h)) {
                    this.paintComponent(g);
                    this.paintBorder(g);
                }
                this.paintChildren(g);
            }
        }
        finally {
            this.setFlag(1, false);
            this.setFlag(2, false);
        }
    }

    private int getObscuredState(int compIndex, int x, int y, int width, int height) {
        int retValue = 0;
        Rectangle tmpRect = JComponent.fetchRectangle();
        for (int i = compIndex - 1; i >= 0; --i) {
            boolean opaque;
            Component sibling = this.getComponent(i);
            if (!sibling.isVisible()) continue;
            if (sibling instanceof JComponent) {
                opaque = sibling.isOpaque();
                if (!opaque && retValue == 1) {
                    continue;
                }
            } else {
                opaque = true;
            }
            Rectangle siblingRect = sibling.getBounds(tmpRect);
            if (opaque && x >= siblingRect.x && x + width <= siblingRect.x + siblingRect.width && y >= siblingRect.y && y + height <= siblingRect.y + siblingRect.height) {
                JComponent.recycleRectangle(tmpRect);
                return 2;
            }
            if (retValue != 0 || x + width <= siblingRect.x || y + height <= siblingRect.y || x >= siblingRect.x + siblingRect.width || y >= siblingRect.y + siblingRect.height) continue;
            retValue = 1;
        }
        JComponent.recycleRectangle(tmpRect);
        return retValue;
    }

    boolean checkIfChildObscuredBySibling() {
        return true;
    }

    private void setFlag(int aFlag, boolean aValue) {
        this.flags = aValue ? (this.flags |= 1 << aFlag) : (this.flags &= ~(1 << aFlag));
    }

    private boolean getFlag(int aFlag) {
        int mask = 1 << aFlag;
        return (this.flags & mask) == mask;
    }

    static void setWriteObjCounter(JComponent comp, byte count) {
        comp.flags = comp.flags & 0xFFC03FFF | count << 14;
    }

    static byte getWriteObjCounter(JComponent comp) {
        return (byte)(comp.flags >> 14 & 0xFF);
    }

    public void setDoubleBuffered(boolean aFlag) {
        this.setFlag(0, aFlag);
    }

    @Override
    public boolean isDoubleBuffered() {
        return this.getFlag(0);
    }

    @BeanProperty(bound=false)
    public JRootPane getRootPane() {
        return SwingUtilities.getRootPane(this);
    }

    void compWriteObjectNotify() {
        byte count = JComponent.getWriteObjCounter(this);
        JComponent.setWriteObjCounter(this, (byte)(count + 1));
        if (count != 0) {
            return;
        }
        this.uninstallUIAndProperties();
        if (this.getToolTipText() != null || this instanceof JTableHeader) {
            ToolTipManager.sharedInstance().unregisterComponent(this);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField f = s.readFields();
        this.isAlignmentXSet = f.get("isAlignmentXSet", false);
        this.alignmentX = this.validateAlignment(f.get("alignmentX", 0.0f));
        this.isAlignmentYSet = f.get("isAlignmentYSet", false);
        this.alignmentY = this.validateAlignment(f.get("alignmentY", 0.0f));
        this.listenerList = (EventListenerList)f.get("listenerList", null);
        this.vetoableChangeSupport = (VetoableChangeSupport)f.get("vetoableChangeSupport", null);
        this.autoscrolls = f.get("autoscrolls", false);
        this.border = (Border)f.get("border", null);
        this.flags = f.get("flags", 0);
        this.inputVerifier = (InputVerifier)f.get("inputVerifier", null);
        this.verifyInputWhenFocusTarget = f.get("verifyInputWhenFocusTarget", false);
        this.popupMenu = (JPopupMenu)f.get("popupMenu", null);
        this.focusInputMap = (InputMap)f.get("focusInputMap", null);
        this.ancestorInputMap = (InputMap)f.get("ancestorInputMap", null);
        this.windowInputMap = (ComponentInputMap)f.get("windowInputMap", null);
        this.actionMap = (ActionMap)f.get("actionMap", null);
        ReadObjectCallback cb = readObjectCallbacks.get(s);
        if (cb == null) {
            try {
                cb = new ReadObjectCallback(s);
                readObjectCallbacks.put(s, cb);
            }
            catch (Exception e) {
                throw new IOException(e.toString());
            }
        }
        cb.registerComponent(this);
        int cpCount = s.readInt();
        if (cpCount > 0) {
            this.clientProperties = new ArrayTable();
            for (int counter = 0; counter < cpCount; ++counter) {
                this.clientProperties.put(s.readObject(), s.readObject());
            }
        }
        if (this.getToolTipText() != null) {
            ToolTipManager.sharedInstance().registerComponent(this);
        }
        JComponent.setWriteObjCounter(this, (byte)0);
        this.revalidateRunnableScheduled = new AtomicBoolean(false);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (this.getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            count = (byte)(count - 1);
            JComponent.setWriteObjCounter(this, count);
            if (count == 0 && this.ui != null) {
                this.ui.installUI(this);
            }
        }
        ArrayTable.writeArrayTable(s, this.clientProperties);
    }

    @Override
    protected String paramString() {
        String maximumSizeString;
        String preferredSizeString = this.isPreferredSizeSet() ? this.getPreferredSize().toString() : "";
        String minimumSizeString = this.isMinimumSizeSet() ? this.getMinimumSize().toString() : "";
        String string = maximumSizeString = this.isMaximumSizeSet() ? this.getMaximumSize().toString() : "";
        String borderString = this.border == null ? "" : (this.border == this ? "this" : this.border.toString());
        return super.paramString() + ",alignmentX=" + this.alignmentX + ",alignmentY=" + this.alignmentY + ",border=" + borderString + ",flags=" + this.flags + ",maximumSize=" + maximumSizeString + ",minimumSize=" + minimumSizeString + ",preferredSize=" + preferredSizeString;
    }

    @Override
    @Deprecated
    public void hide() {
        boolean showing = this.isShowing();
        super.hide();
        if (showing) {
            Container parent = this.getParent();
            if (parent != null) {
                Rectangle r = this.getBounds();
                parent.repaint(r.x, r.y, r.width, r.height);
            }
            this.revalidate();
        }
    }

    static {
        INPUT_VERIFIER_SOURCE_KEY = new StringBuilder("InputVerifierSourceKey");
        tempRectangles = new ArrayList<Rectangle>(11);
        componentObtainingGraphicsFromLock = new StringBuilder("componentObtainingGraphicsFrom");
        SwingAccessor.setJComponentAccessor(new SwingAccessor.JComponentAccessor(){

            @Override
            public boolean getFlag(JComponent comp, int aFlag) {
                return comp.getFlag(aFlag);
            }

            @Override
            public void compWriteObjectNotify(JComponent comp) {
                comp.compWriteObjectNotify();
            }
        });
        focusController = new RequestFocusController(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public boolean acceptRequestFocus(Component from, Component to, boolean temporary, boolean focusedWindowChangeAllowed, FocusEvent.Cause cause) {
                if (!(to instanceof JComponent)) {
                    return true;
                }
                JComponent target = (JComponent)to;
                if (!(from instanceof JComponent)) {
                    return true;
                }
                JComponent jFocusOwner = (JComponent)from;
                if (!target.getVerifyInputWhenFocusTarget()) {
                    return true;
                }
                InputVerifier iv = jFocusOwner.getInputVerifier();
                if (iv == null) {
                    return true;
                }
                Object currentSource = SwingUtilities.appContextGet(INPUT_VERIFIER_SOURCE_KEY);
                if (currentSource == jFocusOwner) {
                    return true;
                }
                SwingUtilities.appContextPut(INPUT_VERIFIER_SOURCE_KEY, jFocusOwner);
                try {
                    boolean bl = iv.shouldYieldFocus(jFocusOwner, target);
                    return bl;
                }
                finally {
                    if (currentSource != null) {
                        SwingUtilities.appContextPut(INPUT_VERIFIER_SOURCE_KEY, currentSource);
                    } else {
                        SwingUtilities.appContextRemove(INPUT_VERIFIER_SOURCE_KEY);
                    }
                }
            }
        };
    }

    static final class ActionStandin
    implements Action {
        private final ActionListener actionListener;
        private final String command;
        private final Action action;

        ActionStandin(ActionListener actionListener, String command) {
            this.actionListener = actionListener;
            this.action = actionListener instanceof Action ? (Action)actionListener : null;
            this.command = command;
        }

        @Override
        public Object getValue(String key) {
            if (key != null) {
                if (key.equals("ActionCommandKey")) {
                    return this.command;
                }
                if (this.action != null) {
                    return this.action.getValue(key);
                }
                if (key.equals("Name")) {
                    return "ActionStandin";
                }
            }
            return null;
        }

        @Override
        public boolean isEnabled() {
            if (this.actionListener == null) {
                return false;
            }
            if (this.action == null) {
                return true;
            }
            return this.action.isEnabled();
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (this.actionListener != null) {
                this.actionListener.actionPerformed(ae);
            }
        }

        @Override
        public void putValue(String key, Object value) {
        }

        @Override
        public void setEnabled(boolean b) {
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

    static class KeyboardState
    implements Serializable {
        private static final Object keyCodesKey = KeyboardState.class;

        KeyboardState() {
        }

        static IntVector getKeyCodeArray() {
            IntVector iv = (IntVector)SwingUtilities.appContextGet(keyCodesKey);
            if (iv == null) {
                iv = new IntVector();
                SwingUtilities.appContextPut(keyCodesKey, iv);
            }
            return iv;
        }

        static void registerKeyPressed(int keyCode) {
            IntVector kca = KeyboardState.getKeyCodeArray();
            int count = kca.size();
            for (int i = 0; i < count; ++i) {
                if (kca.elementAt(i) != -1) continue;
                kca.setElementAt(keyCode, i);
                return;
            }
            kca.addElement(keyCode);
        }

        static void registerKeyReleased(int keyCode) {
            IntVector kca = KeyboardState.getKeyCodeArray();
            int count = kca.size();
            for (int i = 0; i < count; ++i) {
                if (kca.elementAt(i) != keyCode) continue;
                kca.setElementAt(-1, i);
                return;
            }
        }

        static boolean keyIsPressed(int keyCode) {
            IntVector kca = KeyboardState.getKeyCodeArray();
            int count = kca.size();
            for (int i = 0; i < count; ++i) {
                if (kca.elementAt(i) != keyCode) continue;
                return true;
            }
            return false;
        }

        static boolean shouldProcess(KeyEvent e) {
            switch (e.getID()) {
                case 401: {
                    if (!KeyboardState.keyIsPressed(e.getKeyCode())) {
                        KeyboardState.registerKeyPressed(e.getKeyCode());
                    }
                    return true;
                }
                case 402: {
                    if (KeyboardState.keyIsPressed(e.getKeyCode()) || e.getKeyCode() == 154) {
                        KeyboardState.registerKeyReleased(e.getKeyCode());
                        return true;
                    }
                    return false;
                }
                case 400: {
                    return true;
                }
            }
            return false;
        }
    }

    private static class ReadObjectCallback
    implements ObjectInputValidation {
        private final Vector<JComponent> roots = new Vector(1);
        private final ObjectInputStream inputStream;

        ReadObjectCallback(ObjectInputStream s) throws Exception {
            this.inputStream = s;
            s.registerValidation(this, 0);
        }

        @Override
        public void validateObject() throws InvalidObjectException {
            try {
                for (JComponent root : this.roots) {
                    SwingUtilities.updateComponentTreeUI(root);
                }
            }
            finally {
                readObjectCallbacks.remove(this.inputStream);
            }
        }

        private void registerComponent(JComponent c) {
            Container p;
            for (JComponent root : this.roots) {
                for (p = c; p != null; p = p.getParent()) {
                    if (p != root) continue;
                    return;
                }
            }
            block2: for (int i = 0; i < this.roots.size(); ++i) {
                JComponent root;
                root = this.roots.elementAt(i);
                for (p = root.getParent(); p != null; p = p.getParent()) {
                    if (p != c) continue;
                    this.roots.removeElementAt(i--);
                    continue block2;
                }
            }
            this.roots.addElement(c);
        }
    }

    public abstract class AccessibleJComponent
    extends Container.AccessibleAWTContainer
    implements AccessibleExtendedComponent {
        private volatile transient int propertyListenersCount;
        @Deprecated
        protected FocusListener accessibleFocusHandler = null;

        protected AccessibleJComponent() {
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            super.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            super.removePropertyChangeListener(listener);
        }

        protected String getBorderTitle(Border b) {
            if (b instanceof TitledBorder) {
                return ((TitledBorder)b).getTitle();
            }
            if (b instanceof CompoundBorder) {
                String s = this.getBorderTitle(((CompoundBorder)b).getInsideBorder());
                if (s == null) {
                    s = this.getBorderTitle(((CompoundBorder)b).getOutsideBorder());
                }
                return s;
            }
            return null;
        }

        @Override
        public String getAccessibleName() {
            AccessibleContext ac;
            Object o;
            String name = this.accessibleName;
            if (name == null) {
                name = (String)JComponent.this.getClientProperty("AccessibleName");
            }
            if (name == null) {
                name = this.getBorderTitle(JComponent.this.getBorder());
            }
            if (name == null && (o = JComponent.this.getClientProperty("labeledBy")) instanceof Accessible && (ac = ((Accessible)o).getAccessibleContext()) != null) {
                name = ac.getAccessibleName();
            }
            return name;
        }

        @Override
        public String getAccessibleDescription() {
            AccessibleContext ac;
            Object o;
            String description = this.accessibleDescription;
            if (description == null) {
                description = (String)JComponent.this.getClientProperty("AccessibleDescription");
            }
            if (description == null) {
                try {
                    description = this.getToolTipText();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (description == null && (o = JComponent.this.getClientProperty("labeledBy")) instanceof Accessible && (ac = ((Accessible)o).getAccessibleContext()) != null) {
                description = ac.getAccessibleDescription();
            }
            return description;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SWING_COMPONENT;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (JComponent.this.isOpaque()) {
                states.add(AccessibleState.OPAQUE);
            }
            return states;
        }

        @Override
        public int getAccessibleChildrenCount() {
            return super.getAccessibleChildrenCount();
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            return super.getAccessibleChild(i);
        }

        AccessibleExtendedComponent getAccessibleExtendedComponent() {
            return this;
        }

        @Override
        public String getToolTipText() {
            return JComponent.this.getToolTipText();
        }

        @Override
        public String getTitledBorderText() {
            Border border = JComponent.this.getBorder();
            if (border instanceof TitledBorder) {
                return ((TitledBorder)border).getTitle();
            }
            return null;
        }

        @Override
        public AccessibleKeyBinding getAccessibleKeyBinding() {
            AccessibleContext ac;
            Object o = JComponent.this.getClientProperty("labeledBy");
            if (o instanceof Accessible && (ac = ((Accessible)o).getAccessibleContext()) != null) {
                AccessibleComponent comp = ac.getAccessibleComponent();
                if (!(comp instanceof AccessibleExtendedComponent)) {
                    return null;
                }
                return ((AccessibleExtendedComponent)comp).getAccessibleKeyBinding();
            }
            return null;
        }

        @Deprecated
        protected class AccessibleFocusHandler
        implements FocusListener {
            protected AccessibleFocusHandler() {
            }

            @Override
            public void focusGained(FocusEvent event) {
                if (JComponent.this.accessibleContext != null) {
                    JComponent.this.accessibleContext.firePropertyChange("AccessibleState", null, AccessibleState.FOCUSED);
                }
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (JComponent.this.accessibleContext != null) {
                    JComponent.this.accessibleContext.firePropertyChange("AccessibleState", AccessibleState.FOCUSED, null);
                }
            }
        }

        protected class AccessibleContainerHandler
        implements ContainerListener {
            protected AccessibleContainerHandler() {
            }

            @Override
            public void componentAdded(ContainerEvent e) {
                Component c = e.getChild();
                if (c instanceof Accessible) {
                    AccessibleJComponent.this.firePropertyChange("AccessibleChild", null, c.getAccessibleContext());
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                Component c = e.getChild();
                if (c instanceof Accessible) {
                    AccessibleJComponent.this.firePropertyChange("AccessibleChild", c.getAccessibleContext(), null);
                }
            }
        }
    }

    static final class IntVector {
        int[] array = null;
        int count = 0;
        int capacity = 0;

        IntVector() {
        }

        int size() {
            return this.count;
        }

        int elementAt(int index) {
            return this.array[index];
        }

        void addElement(int value) {
            if (this.count == this.capacity) {
                this.capacity = (this.capacity + 2) * 2;
                int[] newarray = new int[this.capacity];
                if (this.count > 0) {
                    System.arraycopy(this.array, 0, newarray, 0, this.count);
                }
                this.array = newarray;
            }
            this.array[this.count++] = value;
        }

        void setElementAt(int value, int index) {
            this.array[index] = value;
        }
    }
}

