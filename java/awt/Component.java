/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.applet.Applet;
import java.awt.AWTError;
import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.AWTException;
import java.awt.AWTKeyStroke;
import java.awt.BufferCapabilities;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsCallback;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.MenuComponent;
import java.awt.MenuContainer;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.dnd.DropTarget;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.PaintEvent;
import java.awt.im.InputContext;
import java.awt.im.InputMethodRequests;
import java.awt.image.BufferStrategy;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.ComponentPeer;
import java.awt.peer.ContainerPeer;
import java.awt.peer.LightweightPeer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.JComponent;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.ComponentFactory;
import sun.awt.ConstrainableGraphics;
import sun.awt.EmbeddedFrame;
import sun.awt.EventQueueItem;
import sun.awt.RequestFocusController;
import sun.awt.SubRegionShowable;
import sun.awt.SunToolkit;
import sun.awt.dnd.SunDropTargetEvent;
import sun.awt.im.CompositionArea;
import sun.awt.image.VSyncedBSManager;
import sun.font.FontDesignMetrics;
import sun.java2d.SunGraphics2D;
import sun.java2d.SunGraphicsEnvironment;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.hw.ExtendedBufferCapabilities;
import sun.security.action.GetPropertyAction;
import sun.swing.SwingAccessor;
import sun.util.logging.PlatformLogger;

public abstract class Component
implements ImageObserver,
MenuContainer,
Serializable {
    private static final PlatformLogger log;
    private static final PlatformLogger eventLog;
    private static final PlatformLogger focusLog;
    private static final PlatformLogger mixingLog;
    volatile transient ComponentPeer peer;
    transient Container parent;
    transient AppContext appContext;
    int x;
    int y;
    int width;
    int height;
    Color foreground;
    Color background;
    volatile Font font;
    Font peerFont;
    Cursor cursor;
    Locale locale;
    private volatile transient GraphicsConfiguration graphicsConfig;
    private transient BufferStrategy bufferStrategy = null;
    boolean ignoreRepaint = false;
    boolean visible = true;
    boolean enabled = true;
    private volatile boolean valid;
    DropTarget dropTarget;
    Vector<PopupMenu> popups;
    private String name;
    private boolean nameExplicitlySet = false;
    private boolean focusable = true;
    private static final int FOCUS_TRAVERSABLE_UNKNOWN = 0;
    private static final int FOCUS_TRAVERSABLE_DEFAULT = 1;
    private static final int FOCUS_TRAVERSABLE_SET = 2;
    private int isFocusTraversableOverridden = 0;
    Set<AWTKeyStroke>[] focusTraversalKeys;
    private static final String[] focusTraversalKeyPropertyNames;
    private boolean focusTraversalKeysEnabled = true;
    static final Object LOCK;
    private volatile transient AccessControlContext acc = AccessController.getContext();
    Dimension minSize;
    boolean minSizeSet;
    Dimension prefSize;
    boolean prefSizeSet;
    Dimension maxSize;
    boolean maxSizeSet;
    transient ComponentOrientation componentOrientation = ComponentOrientation.UNKNOWN;
    boolean newEventsOnly = false;
    transient ComponentListener componentListener;
    transient FocusListener focusListener;
    transient HierarchyListener hierarchyListener;
    transient HierarchyBoundsListener hierarchyBoundsListener;
    transient KeyListener keyListener;
    transient MouseListener mouseListener;
    transient MouseMotionListener mouseMotionListener;
    transient MouseWheelListener mouseWheelListener;
    transient InputMethodListener inputMethodListener;
    static final String actionListenerK = "actionL";
    static final String adjustmentListenerK = "adjustmentL";
    static final String componentListenerK = "componentL";
    static final String containerListenerK = "containerL";
    static final String focusListenerK = "focusL";
    static final String itemListenerK = "itemL";
    static final String keyListenerK = "keyL";
    static final String mouseListenerK = "mouseL";
    static final String mouseMotionListenerK = "mouseMotionL";
    static final String mouseWheelListenerK = "mouseWheelL";
    static final String textListenerK = "textL";
    static final String ownedWindowK = "ownedL";
    static final String windowListenerK = "windowL";
    static final String inputMethodListenerK = "inputMethodL";
    static final String hierarchyListenerK = "hierarchyL";
    static final String hierarchyBoundsListenerK = "hierarchyBoundsL";
    static final String windowStateListenerK = "windowStateL";
    static final String windowFocusListenerK = "windowFocusL";
    long eventMask = 4096L;
    static boolean isInc;
    static int incRate;
    public static final float TOP_ALIGNMENT = 0.0f;
    public static final float CENTER_ALIGNMENT = 0.5f;
    public static final float BOTTOM_ALIGNMENT = 1.0f;
    public static final float LEFT_ALIGNMENT = 0.0f;
    public static final float RIGHT_ALIGNMENT = 1.0f;
    private static final long serialVersionUID = -7644114512714619750L;
    private PropertyChangeSupport changeSupport;
    private transient Object objectLock = new Object();
    boolean isPacked = false;
    private int boundsOp = 3;
    private transient Region compoundShape = null;
    private transient Region mixingCutoutRegion = null;
    private transient boolean isAddNotifyComplete = false;
    transient boolean backgroundEraseDisabled;
    transient EventQueueItem[] eventCache;
    private transient boolean coalescingEnabled = this.checkCoalescing();
    private static final Map<Class<?>, Boolean> coalesceMap;
    private static final Class<?>[] coalesceEventsParams;
    private static RequestFocusController requestFocusController;
    private boolean autoFocusTransferOnDisposal = true;
    private int componentSerializedDataVersion = 4;
    protected AccessibleContext accessibleContext = null;

    Object getObjectLock() {
        return this.objectLock;
    }

    final AccessControlContext getAccessControlContext() {
        if (this.acc == null) {
            throw new SecurityException("Component is missing AccessControlContext");
        }
        return this.acc;
    }

    int getBoundsOp() {
        assert (Thread.holdsLock(this.getTreeLock()));
        return this.boundsOp;
    }

    void setBoundsOp(int op) {
        assert (Thread.holdsLock(this.getTreeLock()));
        if (op == 5) {
            this.boundsOp = 3;
        } else if (this.boundsOp == 3) {
            this.boundsOp = op;
        }
    }

    protected Component() {
        this.appContext = AppContext.getAppContext();
    }

    void initializeFocusTraversalKeys() {
        this.focusTraversalKeys = new Set[3];
    }

    String constructComponentName() {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getName() {
        if (this.name == null && !this.nameExplicitlySet) {
            Object object = this.getObjectLock();
            synchronized (object) {
                if (this.name == null && !this.nameExplicitlySet) {
                    this.name = this.constructComponentName();
                }
            }
        }
        return this.name;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setName(String name) {
        String oldName;
        Object object = this.getObjectLock();
        synchronized (object) {
            oldName = this.name;
            this.name = name;
            this.nameExplicitlySet = true;
        }
        this.firePropertyChange("name", oldName, name);
    }

    public Container getParent() {
        return this.getParent_NoClientCode();
    }

    final Container getParent_NoClientCode() {
        return this.parent;
    }

    Container getContainer() {
        return this.getParent_NoClientCode();
    }

    public synchronized void setDropTarget(DropTarget dt) {
        block9: {
            if (dt == this.dropTarget || this.dropTarget != null && this.dropTarget.equals(dt)) {
                return;
            }
            DropTarget old = this.dropTarget;
            if (old != null) {
                this.dropTarget.removeNotify();
                DropTarget t = this.dropTarget;
                this.dropTarget = null;
                try {
                    t.setComponent(null);
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
            if ((this.dropTarget = dt) != null) {
                try {
                    this.dropTarget.setComponent(this);
                    this.dropTarget.addNotify();
                }
                catch (IllegalArgumentException iae) {
                    if (old == null) break block9;
                    try {
                        old.setComponent(this);
                        this.dropTarget.addNotify();
                    }
                    catch (IllegalArgumentException illegalArgumentException) {
                        // empty catch block
                    }
                }
            }
        }
    }

    public synchronized DropTarget getDropTarget() {
        return this.dropTarget;
    }

    public GraphicsConfiguration getGraphicsConfiguration() {
        return this.getGraphicsConfiguration_NoClientCode();
    }

    final GraphicsConfiguration getGraphicsConfiguration_NoClientCode() {
        return this.graphicsConfig;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void setGraphicsConfiguration(GraphicsConfiguration gc) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.updateGraphicsData(gc)) {
                this.removeNotify();
                this.addNotify();
            }
        }
    }

    final boolean updateGraphicsData(GraphicsConfiguration gc) {
        GraphicsConfiguration oldConfig = this.graphicsConfig;
        boolean ret = this.updateSelfGraphicsData(gc);
        ret |= this.updateChildGraphicsData(gc);
        if (oldConfig != gc) {
            this.firePropertyChange("graphicsConfiguration", oldConfig, gc);
        }
        return ret;
    }

    private boolean updateSelfGraphicsData(GraphicsConfiguration gc) {
        this.checkTreeLock();
        if (this.graphicsConfig == gc) {
            return false;
        }
        this.graphicsConfig = gc;
        ComponentPeer peer = this.peer;
        if (peer != null) {
            return peer.updateGraphicsData(gc);
        }
        return false;
    }

    boolean updateChildGraphicsData(GraphicsConfiguration gc) {
        return false;
    }

    void checkGD(String stringID) {
        if (this.graphicsConfig != null && !this.graphicsConfig.getDevice().getIDstring().equals(stringID)) {
            throw new IllegalArgumentException("adding a container to a container on a different GraphicsDevice");
        }
    }

    public final Object getTreeLock() {
        return LOCK;
    }

    final void checkTreeLock() {
        if (!Thread.holdsLock(this.getTreeLock())) {
            throw new IllegalStateException("This function should be called while holding treeLock");
        }
    }

    public Toolkit getToolkit() {
        return this.getToolkitImpl();
    }

    final Toolkit getToolkitImpl() {
        Container parent = this.parent;
        if (parent != null) {
            return parent.getToolkitImpl();
        }
        return Toolkit.getDefaultToolkit();
    }

    final ComponentFactory getComponentFactory() {
        Toolkit toolkit = this.getToolkit();
        if (toolkit instanceof ComponentFactory) {
            return (ComponentFactory)((Object)toolkit);
        }
        throw new AWTError("UI components are unsupported by: " + String.valueOf(toolkit));
    }

    public boolean isValid() {
        return this.peer != null && this.valid;
    }

    public boolean isDisplayable() {
        return this.peer != null;
    }

    @Transient
    public boolean isVisible() {
        return this.isVisible_NoClientCode();
    }

    final boolean isVisible_NoClientCode() {
        return this.visible;
    }

    boolean isRecursivelyVisible() {
        return this.visible && (this.parent == null || this.parent.isRecursivelyVisible());
    }

    private Rectangle getRecursivelyVisibleBounds() {
        Container container = this.getContainer();
        Rectangle bounds = this.getBounds();
        if (container == null) {
            return bounds;
        }
        Rectangle parentsBounds = container.getRecursivelyVisibleBounds();
        parentsBounds.setLocation(0, 0);
        return parentsBounds.intersection(bounds);
    }

    Point pointRelativeToComponent(Point absolute) {
        Point compCoords = this.getLocationOnScreen();
        return new Point(absolute.x - compCoords.x, absolute.y - compCoords.y);
    }

    Component findUnderMouseInWindow(PointerInfo pi) {
        if (!this.isShowing()) {
            return null;
        }
        Window win = this.getContainingWindow();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (!(toolkit instanceof ComponentFactory)) {
            return null;
        }
        if (!((ComponentFactory)((Object)toolkit)).getMouseInfoPeer().isWindowUnderMouse(win)) {
            return null;
        }
        boolean INCLUDE_DISABLED = true;
        Point relativeToWindow = win.pointRelativeToComponent(pi.getLocation());
        Component inTheSameWindow = win.findComponentAt(relativeToWindow.x, relativeToWindow.y, true);
        return inTheSameWindow;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Point getMousePosition() throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        PointerInfo pi = AccessController.doPrivileged(new PrivilegedAction<PointerInfo>(this){

            @Override
            public PointerInfo run() {
                return MouseInfo.getPointerInfo();
            }
        });
        Object object = this.getTreeLock();
        synchronized (object) {
            Component inTheSameWindow = this.findUnderMouseInWindow(pi);
            if (!this.isSameOrAncestorOf(inTheSameWindow, true)) {
                return null;
            }
            return this.pointRelativeToComponent(pi.getLocation());
        }
    }

    boolean isSameOrAncestorOf(Component comp, boolean allowChildren) {
        return comp == this;
    }

    public boolean isShowing() {
        if (this.visible && this.peer != null) {
            Container parent = this.parent;
            return parent == null || parent.isShowing();
        }
        return false;
    }

    public boolean isEnabled() {
        return this.isEnabledImpl();
    }

    final boolean isEnabledImpl() {
        return this.enabled;
    }

    public void setEnabled(boolean b) {
        this.enable(b);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public void enable() {
        if (!this.enabled) {
            Object object = this.getTreeLock();
            synchronized (object) {
                this.enabled = true;
                ComponentPeer peer = this.peer;
                if (peer != null) {
                    peer.setEnabled(true);
                    if (this.visible && !this.getRecursivelyVisibleBounds().isEmpty()) {
                        this.updateCursorImmediately();
                    }
                }
            }
            if (this.accessibleContext != null) {
                this.accessibleContext.firePropertyChange("AccessibleState", null, AccessibleState.ENABLED);
            }
        }
    }

    @Deprecated
    public void enable(boolean b) {
        if (b) {
            this.enable();
        } else {
            this.disable();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public void disable() {
        if (this.enabled) {
            KeyboardFocusManager.clearMostRecentFocusOwner(this);
            Object object = this.getTreeLock();
            synchronized (object) {
                ComponentPeer peer;
                this.enabled = false;
                if ((this.isFocusOwner() || this.containsFocus() && !this.isLightweight()) && KeyboardFocusManager.isAutoFocusTransferEnabled()) {
                    this.transferFocus(false);
                }
                if ((peer = this.peer) != null) {
                    peer.setEnabled(false);
                    if (this.visible && !this.getRecursivelyVisibleBounds().isEmpty()) {
                        this.updateCursorImmediately();
                    }
                }
            }
            if (this.accessibleContext != null) {
                this.accessibleContext.firePropertyChange("AccessibleState", null, AccessibleState.ENABLED);
            }
        }
    }

    public boolean isDoubleBuffered() {
        return false;
    }

    public void enableInputMethods(boolean enable) {
        if (enable) {
            InputContext inputContext;
            if ((this.eventMask & 0x1000L) != 0L) {
                return;
            }
            if (this.isFocusOwner() && (inputContext = this.getInputContext()) != null) {
                FocusEvent focusGainedEvent = new FocusEvent(this, 1004);
                inputContext.dispatchEvent(focusGainedEvent);
            }
            this.eventMask |= 0x1000L;
        } else {
            InputContext inputContext;
            if ((this.eventMask & 0x1000L) != 0L && (inputContext = this.getInputContext()) != null) {
                inputContext.endComposition();
                inputContext.removeNotify(this);
            }
            this.eventMask &= 0xFFFFFFFFFFFFEFFFL;
        }
    }

    public void setVisible(boolean b) {
        this.show(b);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public void show() {
        if (!this.visible) {
            Object object = this.getTreeLock();
            synchronized (object) {
                this.visible = true;
                this.mixOnShowing();
                ComponentPeer peer = this.peer;
                if (peer != null) {
                    peer.setVisible(true);
                    this.createHierarchyEvents(1400, this, this.parent, 4L, Toolkit.enabledOnToolkit(32768L));
                    if (peer instanceof LightweightPeer) {
                        this.repaint();
                    }
                    this.updateCursorImmediately();
                }
                if (this.componentListener != null || (this.eventMask & 1L) != 0L || Toolkit.enabledOnToolkit(1L)) {
                    ComponentEvent e = new ComponentEvent(this, 102);
                    Toolkit.getEventQueue().postEvent(e);
                }
            }
            Container parent = this.parent;
            if (parent != null) {
                parent.invalidate();
            }
        }
    }

    @Deprecated
    public void show(boolean b) {
        if (b) {
            this.show();
        } else {
            this.hide();
        }
    }

    boolean containsFocus() {
        return this.isFocusOwner();
    }

    void clearMostRecentFocusOwnerOnHide() {
        KeyboardFocusManager.clearMostRecentFocusOwner(this);
    }

    void clearCurrentFocusCycleRootOnHide() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public void hide() {
        this.isPacked = false;
        if (this.visible) {
            this.clearCurrentFocusCycleRootOnHide();
            this.clearMostRecentFocusOwnerOnHide();
            Object object = this.getTreeLock();
            synchronized (object) {
                ComponentPeer peer;
                this.visible = false;
                this.mixOnHiding(this.isLightweight());
                if (this.containsFocus() && KeyboardFocusManager.isAutoFocusTransferEnabled()) {
                    this.transferFocus(true);
                }
                if ((peer = this.peer) != null) {
                    peer.setVisible(false);
                    this.createHierarchyEvents(1400, this, this.parent, 4L, Toolkit.enabledOnToolkit(32768L));
                    if (peer instanceof LightweightPeer) {
                        this.repaint();
                    }
                    this.updateCursorImmediately();
                }
                if (this.componentListener != null || (this.eventMask & 1L) != 0L || Toolkit.enabledOnToolkit(1L)) {
                    ComponentEvent e = new ComponentEvent(this, 103);
                    Toolkit.getEventQueue().postEvent(e);
                }
            }
            Container parent = this.parent;
            if (parent != null) {
                parent.invalidate();
            }
        }
    }

    @Transient
    public Color getForeground() {
        Color foreground = this.foreground;
        if (foreground != null) {
            return foreground;
        }
        Container parent = this.parent;
        return parent != null ? parent.getForeground() : null;
    }

    public void setForeground(Color c) {
        Color oldColor = this.foreground;
        ComponentPeer peer = this.peer;
        this.foreground = c;
        if (peer != null && (c = this.getForeground()) != null) {
            peer.setForeground(c);
        }
        this.firePropertyChange("foreground", oldColor, c);
    }

    public boolean isForegroundSet() {
        return this.foreground != null;
    }

    @Transient
    public Color getBackground() {
        Color background = this.background;
        if (background != null) {
            return background;
        }
        Container parent = this.parent;
        return parent != null ? parent.getBackground() : null;
    }

    public void setBackground(Color c) {
        Color oldColor = this.background;
        ComponentPeer peer = this.peer;
        this.background = c;
        if (peer != null && (c = this.getBackground()) != null) {
            peer.setBackground(c);
        }
        this.firePropertyChange("background", oldColor, c);
    }

    public boolean isBackgroundSet() {
        return this.background != null;
    }

    @Override
    @Transient
    public Font getFont() {
        return this.getFont_NoClientCode();
    }

    final Font getFont_NoClientCode() {
        Font font = this.font;
        if (font != null) {
            return font;
        }
        Container parent = this.parent;
        return parent != null ? parent.getFont_NoClientCode() : null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setFont(Font f) {
        Font newFont;
        Font oldFont;
        Object object = this.getTreeLock();
        synchronized (object) {
            oldFont = this.font;
            newFont = this.font = f;
            ComponentPeer peer = this.peer;
            if (peer != null && (f = this.getFont()) != null) {
                peer.setFont(f);
                this.peerFont = f;
            }
        }
        this.firePropertyChange("font", oldFont, newFont);
        if (!(f == oldFont || oldFont != null && oldFont.equals(f))) {
            this.invalidateIfValid();
        }
    }

    public boolean isFontSet() {
        return this.font != null;
    }

    public Locale getLocale() {
        Locale locale = this.locale;
        if (locale != null) {
            return locale;
        }
        Container parent = this.parent;
        if (parent == null) {
            throw new IllegalComponentStateException("This component must have a parent in order to determine its locale");
        }
        return parent.getLocale();
    }

    public void setLocale(Locale l) {
        Locale oldValue = this.locale;
        this.locale = l;
        this.firePropertyChange("locale", oldValue, l);
        this.invalidateIfValid();
    }

    public ColorModel getColorModel() {
        ComponentPeer peer = this.peer;
        if (peer != null && !(peer instanceof LightweightPeer)) {
            return peer.getColorModel();
        }
        if (GraphicsEnvironment.isHeadless()) {
            return ColorModel.getRGBdefault();
        }
        return this.getToolkit().getColorModel();
    }

    public Point getLocation() {
        return this.location();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Point getLocationOnScreen() {
        Object object = this.getTreeLock();
        synchronized (object) {
            return this.getLocationOnScreen_NoTreeLock();
        }
    }

    final Point getLocationOnScreen_NoTreeLock() {
        ComponentPeer peer = this.peer;
        if (peer != null && this.isShowing()) {
            if (peer instanceof LightweightPeer) {
                Container host = this.getNativeContainer();
                Point pt = host.peer.getLocationOnScreen();
                for (Component c = this; c != host; c = c.getContainer()) {
                    pt.x += c.x;
                    pt.y += c.y;
                }
                return pt;
            }
            Point pt = peer.getLocationOnScreen();
            return pt;
        }
        throw new IllegalComponentStateException("component must be showing on the screen to determine its location");
    }

    @Deprecated
    public Point location() {
        return this.location_NoClientCode();
    }

    private Point location_NoClientCode() {
        return new Point(this.x, this.y);
    }

    public void setLocation(int x, int y) {
        this.move(x, y);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public void move(int x, int y) {
        Object object = this.getTreeLock();
        synchronized (object) {
            this.setBoundsOp(1);
            this.setBounds(x, y, this.width, this.height);
        }
    }

    public void setLocation(Point p) {
        this.setLocation(p.x, p.y);
    }

    public Dimension getSize() {
        return this.size();
    }

    @Deprecated
    public Dimension size() {
        return new Dimension(this.width, this.height);
    }

    public void setSize(int width, int height) {
        this.resize(width, height);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public void resize(int width, int height) {
        Object object = this.getTreeLock();
        synchronized (object) {
            this.setBoundsOp(2);
            this.setBounds(this.x, this.y, width, height);
        }
    }

    public void setSize(Dimension d) {
        this.resize(d);
    }

    @Deprecated
    public void resize(Dimension d) {
        this.setSize(d.width, d.height);
    }

    public Rectangle getBounds() {
        return this.bounds();
    }

    @Deprecated
    public Rectangle bounds() {
        return new Rectangle(this.x, this.y, this.width, this.height);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.reshape(x, y, width, height);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public void reshape(int x, int y, int width, int height) {
        Object object = this.getTreeLock();
        synchronized (object) {
            try {
                boolean moved;
                this.setBoundsOp(3);
                boolean resized = this.width != width || this.height != height;
                boolean bl = moved = this.x != x || this.y != y;
                if (!resized && !moved) {
                    return;
                }
                int oldX = this.x;
                int oldY = this.y;
                int oldWidth = this.width;
                int oldHeight = this.height;
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
                if (resized) {
                    this.isPacked = false;
                }
                boolean needNotify = true;
                this.mixOnReshaping();
                if (this.peer != null) {
                    if (!(this.peer instanceof LightweightPeer)) {
                        this.reshapeNativePeer(x, y, width, height, this.getBoundsOp());
                        resized = oldWidth != this.width || oldHeight != this.height;
                        boolean bl2 = moved = oldX != this.x || oldY != this.y;
                        if (this instanceof Window) {
                            needNotify = false;
                        }
                    }
                    if (resized) {
                        this.invalidate();
                    }
                    if (this.parent != null) {
                        this.parent.invalidateIfValid();
                    }
                }
                if (needNotify) {
                    this.notifyNewBounds(resized, moved);
                }
                this.repaintParentIfNeeded(oldX, oldY, oldWidth, oldHeight);
            }
            finally {
                this.setBoundsOp(5);
            }
        }
    }

    private void repaintParentIfNeeded(int oldX, int oldY, int oldWidth, int oldHeight) {
        if (this.parent != null && this.peer instanceof LightweightPeer && this.isShowing()) {
            this.parent.repaint(oldX, oldY, oldWidth, oldHeight);
            this.repaint();
        }
    }

    private void reshapeNativePeer(int x, int y, int width, int height, int op) {
        int nativeX = x;
        int nativeY = y;
        Container c = this.parent;
        while (c != null && c.peer instanceof LightweightPeer) {
            nativeX += c.x;
            nativeY += c.y;
            c = c.parent;
        }
        this.peer.setBounds(nativeX, nativeY, width, height, op);
    }

    private void notifyNewBounds(boolean resized, boolean moved) {
        if (this.componentListener != null || (this.eventMask & 1L) != 0L || Toolkit.enabledOnToolkit(1L)) {
            ComponentEvent e;
            if (resized) {
                e = new ComponentEvent(this, 101);
                Toolkit.getEventQueue().postEvent(e);
            }
            if (moved) {
                e = new ComponentEvent(this, 100);
                Toolkit.getEventQueue().postEvent(e);
            }
        } else if (this instanceof Container && ((Container)this).countComponents() > 0) {
            boolean enabledOnToolkit = Toolkit.enabledOnToolkit(65536L);
            if (resized) {
                ((Container)this).createChildHierarchyEvents(1402, 0L, enabledOnToolkit);
            }
            if (moved) {
                ((Container)this).createChildHierarchyEvents(1401, 0L, enabledOnToolkit);
            }
        }
    }

    public void setBounds(Rectangle r) {
        this.setBounds(r.x, r.y, r.width, r.height);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Rectangle getBounds(Rectangle rv) {
        if (rv == null) {
            return new Rectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
        rv.setBounds(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        return rv;
    }

    public Dimension getSize(Dimension rv) {
        if (rv == null) {
            return new Dimension(this.getWidth(), this.getHeight());
        }
        rv.setSize(this.getWidth(), this.getHeight());
        return rv;
    }

    public Point getLocation(Point rv) {
        if (rv == null) {
            return new Point(this.getX(), this.getY());
        }
        rv.setLocation(this.getX(), this.getY());
        return rv;
    }

    public boolean isOpaque() {
        if (this.peer == null) {
            return false;
        }
        return !this.isLightweight();
    }

    public boolean isLightweight() {
        return this.peer instanceof LightweightPeer;
    }

    public void setPreferredSize(Dimension preferredSize) {
        Dimension old = this.prefSizeSet ? this.prefSize : null;
        this.prefSize = preferredSize;
        this.prefSizeSet = preferredSize != null;
        this.firePropertyChange("preferredSize", old, preferredSize);
    }

    public boolean isPreferredSizeSet() {
        return this.prefSizeSet;
    }

    public Dimension getPreferredSize() {
        return this.preferredSize();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public Dimension preferredSize() {
        Dimension dim = this.prefSize;
        if (dim == null || !this.isPreferredSizeSet() && !this.isValid()) {
            Object object = this.getTreeLock();
            synchronized (object) {
                dim = this.prefSize = this.peer != null ? this.peer.getPreferredSize() : this.getMinimumSize();
            }
        }
        return new Dimension(dim);
    }

    public void setMinimumSize(Dimension minimumSize) {
        Dimension old = this.minSizeSet ? this.minSize : null;
        this.minSize = minimumSize;
        this.minSizeSet = minimumSize != null;
        this.firePropertyChange("minimumSize", old, minimumSize);
    }

    public boolean isMinimumSizeSet() {
        return this.minSizeSet;
    }

    public Dimension getMinimumSize() {
        return this.minimumSize();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public Dimension minimumSize() {
        Dimension dim = this.minSize;
        if (dim == null || !this.isMinimumSizeSet() && !this.isValid()) {
            Object object = this.getTreeLock();
            synchronized (object) {
                dim = this.minSize = this.peer != null ? this.peer.getMinimumSize() : this.size();
            }
        }
        return new Dimension(dim);
    }

    public void setMaximumSize(Dimension maximumSize) {
        Dimension old = this.maxSizeSet ? this.maxSize : null;
        this.maxSize = maximumSize;
        this.maxSizeSet = maximumSize != null;
        this.firePropertyChange("maximumSize", old, maximumSize);
    }

    public boolean isMaximumSizeSet() {
        return this.maxSizeSet;
    }

    public Dimension getMaximumSize() {
        if (this.isMaximumSizeSet()) {
            return new Dimension(this.maxSize);
        }
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }

    public float getAlignmentX() {
        return 0.5f;
    }

    public float getAlignmentY() {
        return 0.5f;
    }

    public int getBaseline(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height must be >= 0");
        }
        return -1;
    }

    public BaselineResizeBehavior getBaselineResizeBehavior() {
        return BaselineResizeBehavior.OTHER;
    }

    public void doLayout() {
        this.layout();
    }

    @Deprecated
    public void layout() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void validate() {
        Object object = this.getTreeLock();
        synchronized (object) {
            ComponentPeer peer = this.peer;
            boolean wasValid = this.isValid();
            if (!wasValid && peer != null) {
                Font newfont = this.getFont();
                Font oldfont = this.peerFont;
                if (newfont != null && !Objects.equals(oldfont, newfont)) {
                    peer.setFont(newfont);
                    this.peerFont = newfont;
                }
                peer.layout();
            }
            this.valid = true;
            if (!wasValid) {
                this.mixOnValidating();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void invalidate() {
        Object object = this.getTreeLock();
        synchronized (object) {
            this.valid = false;
            if (!this.isPreferredSizeSet()) {
                this.prefSize = null;
            }
            if (!this.isMinimumSizeSet()) {
                this.minSize = null;
            }
            if (!this.isMaximumSizeSet()) {
                this.maxSize = null;
            }
            this.invalidateParent();
        }
    }

    void invalidateParent() {
        if (this.parent != null) {
            this.parent.invalidateIfValid();
        }
    }

    final void invalidateIfValid() {
        if (this.isValid()) {
            this.invalidate();
        }
    }

    public void revalidate() {
        this.revalidateSynchronously();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void revalidateSynchronously() {
        Object object = this.getTreeLock();
        synchronized (object) {
            this.invalidate();
            Container root = this.getContainer();
            if (root == null) {
                this.validate();
            } else {
                while (!root.isValidateRoot() && root.getContainer() != null) {
                    root = root.getContainer();
                }
                root.validate();
            }
        }
    }

    public Graphics getGraphics() {
        if (this.peer instanceof LightweightPeer) {
            if (this.parent == null) {
                return null;
            }
            Graphics g = this.parent.getGraphics();
            if (g == null) {
                return null;
            }
            if (g instanceof ConstrainableGraphics) {
                ((ConstrainableGraphics)((Object)g)).constrain(this.x, this.y, this.width, this.height);
            } else {
                g.translate(this.x, this.y);
                g.setClip(0, 0, this.width, this.height);
            }
            g.setFont(this.getFont());
            return g;
        }
        ComponentPeer peer = this.peer;
        return peer != null ? peer.getGraphics() : null;
    }

    final Graphics getGraphics_NoClientCode() {
        ComponentPeer peer = this.peer;
        if (peer instanceof LightweightPeer) {
            Container parent = this.parent;
            if (parent == null) {
                return null;
            }
            Graphics g = parent.getGraphics_NoClientCode();
            if (g == null) {
                return null;
            }
            if (g instanceof ConstrainableGraphics) {
                ((ConstrainableGraphics)((Object)g)).constrain(this.x, this.y, this.width, this.height);
            } else {
                g.translate(this.x, this.y);
                g.setClip(0, 0, this.width, this.height);
            }
            g.setFont(this.getFont_NoClientCode());
            return g;
        }
        return peer != null ? peer.getGraphics() : null;
    }

    public FontMetrics getFontMetrics(Font font) {
        return FontDesignMetrics.getMetrics(font);
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        this.updateCursorImmediately();
    }

    final void updateCursorImmediately() {
        if (this.peer instanceof LightweightPeer) {
            Container nativeContainer = this.getNativeContainer();
            if (nativeContainer == null) {
                return;
            }
            ComponentPeer cPeer = nativeContainer.peer;
            if (cPeer != null) {
                cPeer.updateCursorImmediately();
            }
        } else if (this.peer != null) {
            this.peer.updateCursorImmediately();
        }
    }

    public Cursor getCursor() {
        return this.getCursor_NoClientCode();
    }

    final Cursor getCursor_NoClientCode() {
        Cursor cursor = this.cursor;
        if (cursor != null) {
            return cursor;
        }
        Container parent = this.parent;
        if (parent != null) {
            return parent.getCursor_NoClientCode();
        }
        return Cursor.getPredefinedCursor(0);
    }

    public boolean isCursorSet() {
        return this.cursor != null;
    }

    public void paint(Graphics g) {
    }

    public void update(Graphics g) {
        this.paint(g);
    }

    public void paintAll(Graphics g) {
        if (this.isShowing()) {
            GraphicsCallback.PeerPaintCallback.getInstance().runOneComponent(this, new Rectangle(0, 0, this.width, this.height), g, g.getClip(), 3);
        }
    }

    void lightweightPaint(Graphics g) {
        this.paint(g);
    }

    void paintHeavyweightComponents(Graphics g) {
    }

    public void repaint() {
        this.repaint(0L, 0, 0, this.width, this.height);
    }

    public void repaint(long tm) {
        this.repaint(tm, 0, 0, this.width, this.height);
    }

    public void repaint(int x, int y, int width, int height) {
        this.repaint(0L, x, y, width, height);
    }

    public void repaint(long tm, int x, int y, int width, int height) {
        if (this.peer instanceof LightweightPeer) {
            if (this.parent != null) {
                int pheight;
                if (x < 0) {
                    width += x;
                    x = 0;
                }
                if (y < 0) {
                    height += y;
                    y = 0;
                }
                int pwidth = width > this.width ? this.width : width;
                int n = pheight = height > this.height ? this.height : height;
                if (pwidth <= 0 || pheight <= 0) {
                    return;
                }
                int px = this.x + x;
                int py = this.y + y;
                this.parent.repaint(tm, px, py, pwidth, pheight);
            }
        } else if (this.isVisible() && this.peer != null && width > 0 && height > 0) {
            PaintEvent e = new PaintEvent(this, 801, new Rectangle(x, y, width, height));
            SunToolkit.postEvent(SunToolkit.targetToAppContext(this), e);
        }
    }

    public void print(Graphics g) {
        this.paint(g);
    }

    public void printAll(Graphics g) {
        if (this.isShowing()) {
            GraphicsCallback.PeerPrintCallback.getInstance().runOneComponent(this, new Rectangle(0, 0, this.width, this.height), g, g.getClip(), 3);
        }
    }

    void lightweightPrint(Graphics g) {
        this.print(g);
    }

    void printHeavyweightComponents(Graphics g) {
    }

    private Insets getInsets_NoClientCode() {
        ComponentPeer peer = this.peer;
        if (peer instanceof ContainerPeer) {
            return (Insets)((ContainerPeer)peer).getInsets().clone();
        }
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        int rate = -1;
        if ((infoflags & 0x30) != 0) {
            rate = 0;
        } else if ((infoflags & 8) != 0 && isInc && (rate = incRate) < 0) {
            rate = 0;
        }
        if (rate >= 0) {
            this.repaint(rate, 0, 0, this.width, this.height);
        }
        return (infoflags & 0xA0) == 0;
    }

    public Image createImage(ImageProducer producer) {
        return this.getToolkit().createImage(producer);
    }

    public Image createImage(int width, int height) {
        ComponentPeer peer = this.peer;
        if (peer instanceof LightweightPeer) {
            if (this.parent != null) {
                return this.parent.createImage(width, height);
            }
            return null;
        }
        return peer != null ? peer.createImage(width, height) : null;
    }

    public VolatileImage createVolatileImage(int width, int height) {
        ComponentPeer peer = this.peer;
        if (peer instanceof LightweightPeer) {
            if (this.parent != null) {
                return this.parent.createVolatileImage(width, height);
            }
            return null;
        }
        return peer != null ? peer.createVolatileImage(width, height) : null;
    }

    public VolatileImage createVolatileImage(int width, int height, ImageCapabilities caps) throws AWTException {
        return this.createVolatileImage(width, height);
    }

    public boolean prepareImage(Image image, ImageObserver observer) {
        return this.prepareImage(image, -1, -1, observer);
    }

    public boolean prepareImage(Image image, int width, int height, ImageObserver observer) {
        return this.getToolkit().prepareImage(image, width, height, observer);
    }

    public int checkImage(Image image, ImageObserver observer) {
        return this.checkImage(image, -1, -1, observer);
    }

    public int checkImage(Image image, int width, int height, ImageObserver observer) {
        return this.getToolkit().checkImage(image, width, height, observer);
    }

    void createBufferStrategy(int numBuffers) {
        BufferCapabilities bufferCaps;
        if (numBuffers > 1) {
            bufferCaps = new BufferCapabilities(new ImageCapabilities(true), new ImageCapabilities(true), BufferCapabilities.FlipContents.UNDEFINED);
            try {
                this.createBufferStrategy(numBuffers, bufferCaps);
                return;
            }
            catch (AWTException aWTException) {
                // empty catch block
            }
        }
        bufferCaps = new BufferCapabilities(new ImageCapabilities(true), new ImageCapabilities(true), null);
        try {
            this.createBufferStrategy(numBuffers, bufferCaps);
            return;
        }
        catch (AWTException aWTException) {
            bufferCaps = new BufferCapabilities(new ImageCapabilities(false), new ImageCapabilities(false), null);
            try {
                this.createBufferStrategy(numBuffers, bufferCaps);
                return;
            }
            catch (AWTException e) {
                throw new InternalError("Could not create a buffer strategy", e);
            }
        }
    }

    void createBufferStrategy(int numBuffers, BufferCapabilities caps) throws AWTException {
        if (numBuffers < 1) {
            throw new IllegalArgumentException("Number of buffers must be at least 1");
        }
        if (caps == null) {
            throw new IllegalArgumentException("No capabilities specified");
        }
        if (this.bufferStrategy != null) {
            this.bufferStrategy.dispose();
        }
        if (numBuffers == 1) {
            this.bufferStrategy = new SingleBufferStrategy(caps);
        } else {
            SunGraphicsEnvironment sge = (SunGraphicsEnvironment)GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (!caps.isPageFlipping() && sge.isFlipStrategyPreferred(this.peer)) {
                caps = new ProxyCapabilities(caps);
            }
            this.bufferStrategy = caps.isPageFlipping() ? new FlipSubRegionBufferStrategy(this, numBuffers, caps) : new BltSubRegionBufferStrategy(this, numBuffers, caps);
        }
    }

    BufferStrategy getBufferStrategy() {
        return this.bufferStrategy;
    }

    Image getBackBuffer() {
        if (this.bufferStrategy != null) {
            if (this.bufferStrategy instanceof BltBufferStrategy) {
                BltBufferStrategy bltBS = (BltBufferStrategy)this.bufferStrategy;
                return bltBS.getBackBuffer();
            }
            if (this.bufferStrategy instanceof FlipBufferStrategy) {
                FlipBufferStrategy flipBS = (FlipBufferStrategy)this.bufferStrategy;
                return flipBS.getBackBuffer();
            }
        }
        return null;
    }

    public void setIgnoreRepaint(boolean ignoreRepaint) {
        this.ignoreRepaint = ignoreRepaint;
    }

    public boolean getIgnoreRepaint() {
        return this.ignoreRepaint;
    }

    public boolean contains(int x, int y) {
        return this.inside(x, y);
    }

    @Deprecated
    public boolean inside(int x, int y) {
        return x >= 0 && x < this.width && y >= 0 && y < this.height;
    }

    public boolean contains(Point p) {
        return this.contains(p.x, p.y);
    }

    public Component getComponentAt(int x, int y) {
        return this.locate(x, y);
    }

    @Deprecated
    public Component locate(int x, int y) {
        return this.contains(x, y) ? this : null;
    }

    public Component getComponentAt(Point p) {
        return this.getComponentAt(p.x, p.y);
    }

    @Deprecated
    public void deliverEvent(Event e) {
        this.postEvent(e);
    }

    public final void dispatchEvent(AWTEvent e) {
        this.dispatchEventImpl(e);
    }

    void dispatchEventImpl(AWTEvent e) {
        Event olde;
        InputContext inputContext;
        int id = e.getID();
        AppContext compContext = this.appContext;
        if (compContext != null && !compContext.equals(AppContext.getAppContext()) && eventLog.isLoggable(PlatformLogger.Level.FINE)) {
            eventLog.fine("Event " + String.valueOf(e) + " is being dispatched on the wrong AppContext");
        }
        if (eventLog.isLoggable(PlatformLogger.Level.FINEST)) {
            eventLog.finest("{0}", e);
        }
        if (!(e instanceof KeyEvent)) {
            EventQueue.setCurrentEventAndMostRecentTime(e);
        }
        if (e instanceof SunDropTargetEvent) {
            ((SunDropTargetEvent)e).dispatch();
            return;
        }
        if (!e.focusManagerIsDispatching) {
            if (e.isPosted) {
                e = KeyboardFocusManager.retargetFocusEvent(e);
                e.isPosted = true;
            }
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().dispatchEvent(e)) {
                return;
            }
        }
        if (e instanceof FocusEvent && focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
            focusLog.finest(String.valueOf(e));
        }
        if (id == 507 && !this.eventTypeEnabled(id) && this.peer != null && !this.peer.handlesWheelScrolling() && this.dispatchMouseWheelToAncestor((MouseWheelEvent)e)) {
            return;
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.notifyAWTEventListeners(e);
        if (!e.isConsumed() && e instanceof KeyEvent) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().processKeyEvent(this, (KeyEvent)e);
            if (e.isConsumed()) {
                return;
            }
        }
        if (this.areInputMethodsEnabled()) {
            if ((e instanceof InputMethodEvent && !(this instanceof CompositionArea) || e instanceof InputEvent || e instanceof FocusEvent) && (inputContext = this.getInputContext()) != null) {
                inputContext.dispatchEvent(e);
                if (e.isConsumed()) {
                    if (e instanceof FocusEvent && focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                        focusLog.finest("3579: Skipping " + String.valueOf(e));
                    }
                    return;
                }
            }
        } else if (id == 1004 && (inputContext = this.getInputContext()) instanceof sun.awt.im.InputContext) {
            sun.awt.im.InputContext ctx = (sun.awt.im.InputContext)inputContext;
            ctx.disableNativeIM();
        }
        switch (id) {
            case 401: 
            case 402: {
                Container p = (Container)(this instanceof Container ? this : this.parent);
                if (p == null) break;
                p.preProcessKeyEvent((KeyEvent)e);
                if (!e.isConsumed()) break;
                if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                    focusLog.finest("Pre-process consumed event");
                }
                return;
            }
        }
        if (this.newEventsOnly) {
            if (this.eventEnabled(e)) {
                this.processEvent(e);
            }
        } else if (id == 507) {
            this.autoProcessMouseWheel((MouseWheelEvent)e);
        } else if ((!(e instanceof MouseEvent) || this.postsOldMouseEvents()) && (olde = e.convertToOld()) != null) {
            int key = olde.key;
            int modifiers = olde.modifiers;
            this.postEvent(olde);
            if (olde.isConsumed()) {
                e.consume();
            }
            switch (olde.id) {
                case 401: 
                case 402: 
                case 403: 
                case 404: {
                    if (olde.key != key) {
                        ((KeyEvent)e).setKeyChar(olde.getKeyEventChar());
                    }
                    if (olde.modifiers == modifiers) break;
                    ((KeyEvent)e).setModifiers(olde.modifiers);
                    break;
                }
            }
        }
        if (!(e instanceof KeyEvent)) {
            Container target;
            Component source;
            ComponentPeer tpeer = this.peer;
            if (e instanceof FocusEvent && (tpeer == null || tpeer instanceof LightweightPeer) && (source = (Component)e.getSource()) != null && (target = source.getNativeContainer()) != null) {
                tpeer = target.peer;
            }
            if (tpeer != null) {
                tpeer.handleEvent(e);
            }
        }
        if (SunToolkit.isTouchKeyboardAutoShowEnabled() && toolkit instanceof SunToolkit && (e instanceof MouseEvent || e instanceof FocusEvent)) {
            ((SunToolkit)toolkit).showOrHideTouchKeyboard(this, e);
        }
    }

    void autoProcessMouseWheel(MouseWheelEvent e) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean dispatchMouseWheelToAncestor(MouseWheelEvent e) {
        int newX = e.getX() + this.getX();
        int newY = e.getY() + this.getY();
        if (eventLog.isLoggable(PlatformLogger.Level.FINEST)) {
            eventLog.finest("dispatchMouseWheelToAncestor");
            eventLog.finest("orig event src is of " + String.valueOf(e.getSource().getClass()));
        }
        Object object = this.getTreeLock();
        synchronized (object) {
            Container anc;
            for (anc = this.getParent(); anc != null && !anc.eventEnabled(e); anc = anc.getParent()) {
                newX += anc.getX();
                newY += anc.getY();
                if (anc instanceof Window) break;
            }
            if (eventLog.isLoggable(PlatformLogger.Level.FINEST)) {
                eventLog.finest("new event src is " + String.valueOf(anc.getClass()));
            }
            if (anc != null && anc.eventEnabled(e)) {
                MouseWheelEvent newMWE = new MouseWheelEvent(anc, e.getID(), e.getWhen(), e.getModifiers(), newX, newY, e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(), e.getWheelRotation(), e.getPreciseWheelRotation());
                e.copyPrivateDataInto(newMWE);
                anc.dispatchEventToSelf(newMWE);
                if (newMWE.isConsumed()) {
                    e.consume();
                }
                return true;
            }
        }
        return false;
    }

    boolean areInputMethodsEnabled() {
        return (this.eventMask & 0x1000L) != 0L && ((this.eventMask & 8L) != 0L || this.keyListener != null);
    }

    boolean eventEnabled(AWTEvent e) {
        return this.eventTypeEnabled(e.id);
    }

    boolean eventTypeEnabled(int type) {
        switch (type) {
            case 100: 
            case 101: 
            case 102: 
            case 103: {
                if ((this.eventMask & 1L) == 0L && this.componentListener == null) break;
                return true;
            }
            case 1004: 
            case 1005: {
                if ((this.eventMask & 4L) == 0L && this.focusListener == null) break;
                return true;
            }
            case 400: 
            case 401: 
            case 402: {
                if ((this.eventMask & 8L) == 0L && this.keyListener == null) break;
                return true;
            }
            case 500: 
            case 501: 
            case 502: 
            case 504: 
            case 505: {
                if ((this.eventMask & 0x10L) == 0L && this.mouseListener == null) break;
                return true;
            }
            case 503: 
            case 506: {
                if ((this.eventMask & 0x20L) == 0L && this.mouseMotionListener == null) break;
                return true;
            }
            case 507: {
                if ((this.eventMask & 0x20000L) == 0L && this.mouseWheelListener == null) break;
                return true;
            }
            case 1100: 
            case 1101: {
                if ((this.eventMask & 0x800L) == 0L && this.inputMethodListener == null) break;
                return true;
            }
            case 1400: {
                if ((this.eventMask & 0x8000L) == 0L && this.hierarchyListener == null) break;
                return true;
            }
            case 1401: 
            case 1402: {
                if ((this.eventMask & 0x10000L) == 0L && this.hierarchyBoundsListener == null) break;
                return true;
            }
            case 1001: {
                if ((this.eventMask & 0x80L) == 0L) break;
                return true;
            }
            case 900: {
                if ((this.eventMask & 0x400L) == 0L) break;
                return true;
            }
            case 701: {
                if ((this.eventMask & 0x200L) == 0L) break;
                return true;
            }
            case 601: {
                if ((this.eventMask & 0x100L) == 0L) break;
                return true;
            }
        }
        return type > 1999;
    }

    @Override
    @Deprecated
    public boolean postEvent(Event e) {
        ComponentPeer peer = this.peer;
        if (this.handleEvent(e)) {
            e.consume();
            return true;
        }
        Container parent = this.parent;
        int eventx = e.x;
        int eventy = e.y;
        if (parent != null) {
            e.translate(this.x, this.y);
            if (parent.postEvent(e)) {
                e.consume();
                return true;
            }
            e.x = eventx;
            e.y = eventy;
        }
        return false;
    }

    public synchronized void addComponentListener(ComponentListener l) {
        if (l == null) {
            return;
        }
        this.componentListener = AWTEventMulticaster.add(this.componentListener, l);
        this.newEventsOnly = true;
    }

    public synchronized void removeComponentListener(ComponentListener l) {
        if (l == null) {
            return;
        }
        this.componentListener = AWTEventMulticaster.remove(this.componentListener, l);
    }

    public synchronized ComponentListener[] getComponentListeners() {
        return (ComponentListener[])this.getListeners(ComponentListener.class);
    }

    public synchronized void addFocusListener(FocusListener l) {
        if (l == null) {
            return;
        }
        this.focusListener = AWTEventMulticaster.add(this.focusListener, l);
        this.newEventsOnly = true;
        if (this.peer instanceof LightweightPeer) {
            this.parent.proxyEnableEvents(4L);
        }
    }

    public synchronized void removeFocusListener(FocusListener l) {
        if (l == null) {
            return;
        }
        this.focusListener = AWTEventMulticaster.remove(this.focusListener, l);
    }

    public synchronized FocusListener[] getFocusListeners() {
        return (FocusListener[])this.getListeners(FocusListener.class);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addHierarchyListener(HierarchyListener l) {
        boolean notifyAncestors;
        if (l == null) {
            return;
        }
        Object object = this;
        synchronized (object) {
            notifyAncestors = this.hierarchyListener == null && (this.eventMask & 0x8000L) == 0L;
            this.hierarchyListener = AWTEventMulticaster.add(this.hierarchyListener, l);
            notifyAncestors = notifyAncestors && this.hierarchyListener != null;
            this.newEventsOnly = true;
        }
        if (notifyAncestors) {
            object = this.getTreeLock();
            synchronized (object) {
                this.adjustListeningChildrenOnParent(32768L, 1);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeHierarchyListener(HierarchyListener l) {
        boolean notifyAncestors;
        if (l == null) {
            return;
        }
        Object object = this;
        synchronized (object) {
            notifyAncestors = this.hierarchyListener != null && (this.eventMask & 0x8000L) == 0L;
            this.hierarchyListener = AWTEventMulticaster.remove(this.hierarchyListener, l);
            notifyAncestors = notifyAncestors && this.hierarchyListener == null;
        }
        if (notifyAncestors) {
            object = this.getTreeLock();
            synchronized (object) {
                this.adjustListeningChildrenOnParent(32768L, -1);
            }
        }
    }

    public synchronized HierarchyListener[] getHierarchyListeners() {
        return (HierarchyListener[])this.getListeners(HierarchyListener.class);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addHierarchyBoundsListener(HierarchyBoundsListener l) {
        boolean notifyAncestors;
        if (l == null) {
            return;
        }
        Object object = this;
        synchronized (object) {
            notifyAncestors = this.hierarchyBoundsListener == null && (this.eventMask & 0x10000L) == 0L;
            this.hierarchyBoundsListener = AWTEventMulticaster.add(this.hierarchyBoundsListener, l);
            notifyAncestors = notifyAncestors && this.hierarchyBoundsListener != null;
            this.newEventsOnly = true;
        }
        if (notifyAncestors) {
            object = this.getTreeLock();
            synchronized (object) {
                this.adjustListeningChildrenOnParent(65536L, 1);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeHierarchyBoundsListener(HierarchyBoundsListener l) {
        boolean notifyAncestors;
        if (l == null) {
            return;
        }
        Object object = this;
        synchronized (object) {
            notifyAncestors = this.hierarchyBoundsListener != null && (this.eventMask & 0x10000L) == 0L;
            this.hierarchyBoundsListener = AWTEventMulticaster.remove(this.hierarchyBoundsListener, l);
            notifyAncestors = notifyAncestors && this.hierarchyBoundsListener == null;
        }
        if (notifyAncestors) {
            object = this.getTreeLock();
            synchronized (object) {
                this.adjustListeningChildrenOnParent(65536L, -1);
            }
        }
    }

    int numListening(long mask) {
        if (eventLog.isLoggable(PlatformLogger.Level.FINE) && mask != 32768L && mask != 65536L) {
            eventLog.fine("Assertion failed");
        }
        if (mask == 32768L && (this.hierarchyListener != null || (this.eventMask & 0x8000L) != 0L) || mask == 65536L && (this.hierarchyBoundsListener != null || (this.eventMask & 0x10000L) != 0L)) {
            return 1;
        }
        return 0;
    }

    int countHierarchyMembers() {
        return 1;
    }

    int createHierarchyEvents(int id, Component changed, Container changedParent, long changeFlags, boolean enabledOnToolkit) {
        switch (id) {
            case 1400: {
                if (this.hierarchyListener == null && (this.eventMask & 0x8000L) == 0L && !enabledOnToolkit) break;
                HierarchyEvent e = new HierarchyEvent(this, id, changed, changedParent, changeFlags);
                this.dispatchEvent(e);
                return 1;
            }
            case 1401: 
            case 1402: {
                if (eventLog.isLoggable(PlatformLogger.Level.FINE) && changeFlags != 0L) {
                    eventLog.fine("Assertion (changeFlags == 0) failed");
                }
                if (this.hierarchyBoundsListener == null && (this.eventMask & 0x10000L) == 0L && !enabledOnToolkit) break;
                HierarchyEvent e = new HierarchyEvent(this, id, changed, changedParent);
                this.dispatchEvent(e);
                return 1;
            }
            default: {
                if (!eventLog.isLoggable(PlatformLogger.Level.FINE)) break;
                eventLog.fine("This code must never be reached");
            }
        }
        return 0;
    }

    public synchronized HierarchyBoundsListener[] getHierarchyBoundsListeners() {
        return (HierarchyBoundsListener[])this.getListeners(HierarchyBoundsListener.class);
    }

    void adjustListeningChildrenOnParent(long mask, int num) {
        if (this.parent != null) {
            this.parent.adjustListeningChildren(mask, num);
        }
    }

    public synchronized void addKeyListener(KeyListener l) {
        if (l == null) {
            return;
        }
        this.keyListener = AWTEventMulticaster.add(this.keyListener, l);
        this.newEventsOnly = true;
        if (this.peer instanceof LightweightPeer) {
            this.parent.proxyEnableEvents(8L);
        }
    }

    public synchronized void removeKeyListener(KeyListener l) {
        if (l == null) {
            return;
        }
        this.keyListener = AWTEventMulticaster.remove(this.keyListener, l);
    }

    public synchronized KeyListener[] getKeyListeners() {
        return (KeyListener[])this.getListeners(KeyListener.class);
    }

    public synchronized void addMouseListener(MouseListener l) {
        if (l == null) {
            return;
        }
        this.mouseListener = AWTEventMulticaster.add(this.mouseListener, l);
        this.newEventsOnly = true;
        if (this.peer instanceof LightweightPeer) {
            this.parent.proxyEnableEvents(16L);
        }
    }

    public synchronized void removeMouseListener(MouseListener l) {
        if (l == null) {
            return;
        }
        this.mouseListener = AWTEventMulticaster.remove(this.mouseListener, l);
    }

    public synchronized MouseListener[] getMouseListeners() {
        return (MouseListener[])this.getListeners(MouseListener.class);
    }

    public synchronized void addMouseMotionListener(MouseMotionListener l) {
        if (l == null) {
            return;
        }
        this.mouseMotionListener = AWTEventMulticaster.add(this.mouseMotionListener, l);
        this.newEventsOnly = true;
        if (this.peer instanceof LightweightPeer) {
            this.parent.proxyEnableEvents(32L);
        }
    }

    public synchronized void removeMouseMotionListener(MouseMotionListener l) {
        if (l == null) {
            return;
        }
        this.mouseMotionListener = AWTEventMulticaster.remove(this.mouseMotionListener, l);
    }

    public synchronized MouseMotionListener[] getMouseMotionListeners() {
        return (MouseMotionListener[])this.getListeners(MouseMotionListener.class);
    }

    public synchronized void addMouseWheelListener(MouseWheelListener l) {
        if (l == null) {
            return;
        }
        this.mouseWheelListener = AWTEventMulticaster.add(this.mouseWheelListener, l);
        this.newEventsOnly = true;
        if (this.peer instanceof LightweightPeer) {
            this.parent.proxyEnableEvents(131072L);
        }
    }

    public synchronized void removeMouseWheelListener(MouseWheelListener l) {
        if (l == null) {
            return;
        }
        this.mouseWheelListener = AWTEventMulticaster.remove(this.mouseWheelListener, l);
    }

    public synchronized MouseWheelListener[] getMouseWheelListeners() {
        return (MouseWheelListener[])this.getListeners(MouseWheelListener.class);
    }

    public synchronized void addInputMethodListener(InputMethodListener l) {
        if (l == null) {
            return;
        }
        this.inputMethodListener = AWTEventMulticaster.add(this.inputMethodListener, l);
        this.newEventsOnly = true;
    }

    public synchronized void removeInputMethodListener(InputMethodListener l) {
        if (l == null) {
            return;
        }
        this.inputMethodListener = AWTEventMulticaster.remove(this.inputMethodListener, l);
    }

    public synchronized InputMethodListener[] getInputMethodListeners() {
        return (InputMethodListener[])this.getListeners(InputMethodListener.class);
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if (listenerType == ComponentListener.class) {
            l = this.componentListener;
        } else if (listenerType == FocusListener.class) {
            l = this.focusListener;
        } else if (listenerType == HierarchyListener.class) {
            l = this.hierarchyListener;
        } else if (listenerType == HierarchyBoundsListener.class) {
            l = this.hierarchyBoundsListener;
        } else if (listenerType == KeyListener.class) {
            l = this.keyListener;
        } else if (listenerType == MouseListener.class) {
            l = this.mouseListener;
        } else if (listenerType == MouseMotionListener.class) {
            l = this.mouseMotionListener;
        } else if (listenerType == MouseWheelListener.class) {
            l = this.mouseWheelListener;
        } else if (listenerType == InputMethodListener.class) {
            l = this.inputMethodListener;
        } else if (listenerType == PropertyChangeListener.class) {
            return this.getPropertyChangeListeners();
        }
        return AWTEventMulticaster.getListeners((EventListener)l, listenerType);
    }

    public InputMethodRequests getInputMethodRequests() {
        return null;
    }

    public InputContext getInputContext() {
        Container parent = this.parent;
        if (parent == null) {
            return null;
        }
        return parent.getInputContext();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void enableEvents(long eventsToEnable) {
        long notifyAncestors = 0L;
        Object object = this;
        synchronized (object) {
            if ((eventsToEnable & 0x8000L) != 0L && this.hierarchyListener == null && (this.eventMask & 0x8000L) == 0L) {
                notifyAncestors |= 0x8000L;
            }
            if ((eventsToEnable & 0x10000L) != 0L && this.hierarchyBoundsListener == null && (this.eventMask & 0x10000L) == 0L) {
                notifyAncestors |= 0x10000L;
            }
            this.eventMask |= eventsToEnable;
            this.newEventsOnly = true;
        }
        if (this.peer instanceof LightweightPeer) {
            this.parent.proxyEnableEvents(this.eventMask);
        }
        if (notifyAncestors != 0L) {
            object = this.getTreeLock();
            synchronized (object) {
                this.adjustListeningChildrenOnParent(notifyAncestors, 1);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void disableEvents(long eventsToDisable) {
        long notifyAncestors = 0L;
        Object object = this;
        synchronized (object) {
            if ((eventsToDisable & 0x8000L) != 0L && this.hierarchyListener == null && (this.eventMask & 0x8000L) != 0L) {
                notifyAncestors |= 0x8000L;
            }
            if ((eventsToDisable & 0x10000L) != 0L && this.hierarchyBoundsListener == null && (this.eventMask & 0x10000L) != 0L) {
                notifyAncestors |= 0x10000L;
            }
            this.eventMask &= eventsToDisable ^ 0xFFFFFFFFFFFFFFFFL;
        }
        if (notifyAncestors != 0L) {
            object = this.getTreeLock();
            synchronized (object) {
                this.adjustListeningChildrenOnParent(notifyAncestors, -1);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean checkCoalescing() {
        if (this.getClass().getClassLoader() == null) {
            return false;
        }
        final Class<?> clazz = this.getClass();
        Map<Class<?>, Boolean> map = coalesceMap;
        synchronized (map) {
            Boolean value = coalesceMap.get(clazz);
            if (value != null) {
                return value;
            }
            Boolean enabled = AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

                @Override
                public Boolean run() {
                    return Component.isCoalesceEventsOverriden(clazz);
                }
            });
            coalesceMap.put(clazz, enabled);
            return enabled;
        }
    }

    private static boolean isCoalesceEventsOverriden(Class<?> clazz) {
        assert (Thread.holdsLock(coalesceMap));
        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null) {
            return false;
        }
        if (superclass.getClassLoader() != null) {
            Boolean value = coalesceMap.get(superclass);
            if (value == null) {
                if (Component.isCoalesceEventsOverriden(superclass)) {
                    coalesceMap.put(superclass, true);
                    return true;
                }
            } else if (value.booleanValue()) {
                return true;
            }
        }
        try {
            clazz.getDeclaredMethod("coalesceEvents", coalesceEventsParams);
            return true;
        }
        catch (NoSuchMethodException e) {
            return false;
        }
    }

    final boolean isCoalescingEnabled() {
        return this.coalescingEnabled;
    }

    protected AWTEvent coalesceEvents(AWTEvent existingEvent, AWTEvent newEvent) {
        return null;
    }

    protected void processEvent(AWTEvent e) {
        if (e instanceof FocusEvent) {
            this.processFocusEvent((FocusEvent)e);
        } else if (e instanceof MouseEvent) {
            switch (e.getID()) {
                case 500: 
                case 501: 
                case 502: 
                case 504: 
                case 505: {
                    this.processMouseEvent((MouseEvent)e);
                    break;
                }
                case 503: 
                case 506: {
                    this.processMouseMotionEvent((MouseEvent)e);
                    break;
                }
                case 507: {
                    this.processMouseWheelEvent((MouseWheelEvent)e);
                }
            }
        } else if (e instanceof KeyEvent) {
            this.processKeyEvent((KeyEvent)e);
        } else if (e instanceof ComponentEvent) {
            this.processComponentEvent((ComponentEvent)e);
        } else if (e instanceof InputMethodEvent) {
            this.processInputMethodEvent((InputMethodEvent)e);
        } else if (e instanceof HierarchyEvent) {
            switch (e.getID()) {
                case 1400: {
                    this.processHierarchyEvent((HierarchyEvent)e);
                    break;
                }
                case 1401: 
                case 1402: {
                    this.processHierarchyBoundsEvent((HierarchyEvent)e);
                }
            }
        }
    }

    protected void processComponentEvent(ComponentEvent e) {
        ComponentListener listener = this.componentListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
                case 101: {
                    listener.componentResized(e);
                    break;
                }
                case 100: {
                    listener.componentMoved(e);
                    break;
                }
                case 102: {
                    listener.componentShown(e);
                    break;
                }
                case 103: {
                    listener.componentHidden(e);
                }
            }
        }
    }

    protected void processFocusEvent(FocusEvent e) {
        FocusListener listener = this.focusListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
                case 1004: {
                    listener.focusGained(e);
                    break;
                }
                case 1005: {
                    listener.focusLost(e);
                }
            }
        }
    }

    protected void processKeyEvent(KeyEvent e) {
        KeyListener listener = this.keyListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
                case 400: {
                    listener.keyTyped(e);
                    break;
                }
                case 401: {
                    listener.keyPressed(e);
                    break;
                }
                case 402: {
                    listener.keyReleased(e);
                }
            }
        }
    }

    protected void processMouseEvent(MouseEvent e) {
        MouseListener listener = this.mouseListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
                case 501: {
                    listener.mousePressed(e);
                    break;
                }
                case 502: {
                    listener.mouseReleased(e);
                    break;
                }
                case 500: {
                    listener.mouseClicked(e);
                    break;
                }
                case 505: {
                    listener.mouseExited(e);
                    break;
                }
                case 504: {
                    listener.mouseEntered(e);
                }
            }
        }
    }

    protected void processMouseMotionEvent(MouseEvent e) {
        MouseMotionListener listener = this.mouseMotionListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
                case 503: {
                    listener.mouseMoved(e);
                    break;
                }
                case 506: {
                    listener.mouseDragged(e);
                }
            }
        }
    }

    protected void processMouseWheelEvent(MouseWheelEvent e) {
        MouseWheelListener listener = this.mouseWheelListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
                case 507: {
                    listener.mouseWheelMoved(e);
                }
            }
        }
    }

    boolean postsOldMouseEvents() {
        return false;
    }

    protected void processInputMethodEvent(InputMethodEvent e) {
        InputMethodListener listener = this.inputMethodListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
                case 1100: {
                    listener.inputMethodTextChanged(e);
                    break;
                }
                case 1101: {
                    listener.caretPositionChanged(e);
                }
            }
        }
    }

    protected void processHierarchyEvent(HierarchyEvent e) {
        HierarchyListener listener = this.hierarchyListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
                case 1400: {
                    listener.hierarchyChanged(e);
                }
            }
        }
    }

    protected void processHierarchyBoundsEvent(HierarchyEvent e) {
        HierarchyBoundsListener listener = this.hierarchyBoundsListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
                case 1401: {
                    listener.ancestorMoved(e);
                    break;
                }
                case 1402: {
                    listener.ancestorResized(e);
                }
            }
        }
    }

    @Deprecated
    public boolean handleEvent(Event evt) {
        switch (evt.id) {
            case 504: {
                return this.mouseEnter(evt, evt.x, evt.y);
            }
            case 505: {
                return this.mouseExit(evt, evt.x, evt.y);
            }
            case 503: {
                return this.mouseMove(evt, evt.x, evt.y);
            }
            case 501: {
                return this.mouseDown(evt, evt.x, evt.y);
            }
            case 506: {
                return this.mouseDrag(evt, evt.x, evt.y);
            }
            case 502: {
                return this.mouseUp(evt, evt.x, evt.y);
            }
            case 401: 
            case 403: {
                return this.keyDown(evt, evt.key);
            }
            case 402: 
            case 404: {
                return this.keyUp(evt, evt.key);
            }
            case 1001: {
                return this.action(evt, evt.arg);
            }
            case 1004: {
                return this.gotFocus(evt, evt.arg);
            }
            case 1005: {
                return this.lostFocus(evt, evt.arg);
            }
        }
        return false;
    }

    @Deprecated
    public boolean mouseDown(Event evt, int x, int y) {
        return false;
    }

    @Deprecated
    public boolean mouseDrag(Event evt, int x, int y) {
        return false;
    }

    @Deprecated
    public boolean mouseUp(Event evt, int x, int y) {
        return false;
    }

    @Deprecated
    public boolean mouseMove(Event evt, int x, int y) {
        return false;
    }

    @Deprecated
    public boolean mouseEnter(Event evt, int x, int y) {
        return false;
    }

    @Deprecated
    public boolean mouseExit(Event evt, int x, int y) {
        return false;
    }

    @Deprecated
    public boolean keyDown(Event evt, int key) {
        return false;
    }

    @Deprecated
    public boolean keyUp(Event evt, int key) {
        return false;
    }

    @Deprecated
    public boolean action(Event evt, Object what) {
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addNotify() {
        Object object = this.getTreeLock();
        synchronized (object) {
            ComponentPeer peer = this.peer;
            if (peer == null || peer instanceof LightweightPeer) {
                if (peer == null) {
                    this.peer = peer = this.getComponentFactory().createComponent(this);
                }
                if (this.parent != null) {
                    long mask = 0L;
                    if (this.mouseListener != null || (this.eventMask & 0x10L) != 0L) {
                        mask |= 0x10L;
                    }
                    if (this.mouseMotionListener != null || (this.eventMask & 0x20L) != 0L) {
                        mask |= 0x20L;
                    }
                    if (this.mouseWheelListener != null || (this.eventMask & 0x20000L) != 0L) {
                        mask |= 0x20000L;
                    }
                    if (this.focusListener != null || (this.eventMask & 4L) != 0L) {
                        mask |= 4L;
                    }
                    if (this.keyListener != null || (this.eventMask & 8L) != 0L) {
                        mask |= 8L;
                    }
                    if (mask != 0L) {
                        this.parent.proxyEnableEvents(mask);
                    }
                }
            } else {
                Container parent = this.getContainer();
                if (parent != null && parent.isLightweight()) {
                    this.relocateComponent();
                    if (!parent.isRecursivelyVisibleUpToHeavyweightContainer()) {
                        peer.setVisible(false);
                    }
                }
            }
            this.invalidate();
            int npopups = this.popups != null ? this.popups.size() : 0;
            for (int i = 0; i < npopups; ++i) {
                PopupMenu popup = this.popups.elementAt(i);
                popup.addNotify();
            }
            if (this.dropTarget != null) {
                this.dropTarget.addNotify();
            }
            this.peerFont = this.getFont();
            if (this.getContainer() != null && !this.isAddNotifyComplete) {
                this.getContainer().increaseComponentCount(this);
            }
            this.updateZOrder();
            if (!this.isAddNotifyComplete) {
                this.mixOnShowing();
            }
            this.isAddNotifyComplete = true;
            if (this.hierarchyListener != null || (this.eventMask & 0x8000L) != 0L || Toolkit.enabledOnToolkit(32768L)) {
                HierarchyEvent e = new HierarchyEvent(this, 1400, this, this.parent, 2 | (this.isRecursivelyVisible() ? 4 : 0));
                this.dispatchEvent(e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeNotify() {
        KeyboardFocusManager.clearMostRecentFocusOwner(this);
        if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner() == this) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().setGlobalPermanentFocusOwner(null);
        }
        Object object = this.getTreeLock();
        synchronized (object) {
            ComponentPeer p;
            InputContext inputContext;
            if (this.isFocusOwner() && KeyboardFocusManager.isAutoFocusTransferEnabledFor(this)) {
                this.transferFocus(true);
            }
            if (this.getContainer() != null && this.isAddNotifyComplete) {
                this.getContainer().decreaseComponentCount(this);
            }
            int npopups = this.popups != null ? this.popups.size() : 0;
            for (int i = 0; i < npopups; ++i) {
                PopupMenu popup = this.popups.elementAt(i);
                popup.removeNotify();
            }
            if ((this.eventMask & 0x1000L) != 0L && (inputContext = this.getInputContext()) != null) {
                inputContext.removeNotify(this);
            }
            if ((p = this.peer) != null) {
                boolean isLightweight = this.isLightweight();
                if (this.bufferStrategy instanceof FlipBufferStrategy) {
                    ((FlipBufferStrategy)this.bufferStrategy).invalidate();
                }
                if (this.dropTarget != null) {
                    this.dropTarget.removeNotify();
                }
                if (this.visible) {
                    p.setVisible(false);
                }
                this.peer = null;
                this.peerFont = null;
                Toolkit.getEventQueue().removeSourceEvents(this, false);
                KeyboardFocusManager.getCurrentKeyboardFocusManager().discardKeyEvents(this);
                p.dispose();
                this.mixOnHiding(isLightweight);
                this.isAddNotifyComplete = false;
                this.compoundShape = null;
            }
            if (this.hierarchyListener != null || (this.eventMask & 0x8000L) != 0L || Toolkit.enabledOnToolkit(32768L)) {
                HierarchyEvent e = new HierarchyEvent(this, 1400, this, this.parent, 2 | (this.isRecursivelyVisible() ? 4 : 0));
                this.dispatchEvent(e);
            }
        }
    }

    @Deprecated
    public boolean gotFocus(Event evt, Object what) {
        return false;
    }

    @Deprecated
    public boolean lostFocus(Event evt, Object what) {
        return false;
    }

    @Deprecated
    public boolean isFocusTraversable() {
        if (this.isFocusTraversableOverridden == 0) {
            this.isFocusTraversableOverridden = 1;
        }
        return this.focusable;
    }

    public boolean isFocusable() {
        return this.isFocusTraversable();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setFocusable(boolean focusable) {
        boolean oldFocusable;
        Component component = this;
        synchronized (component) {
            oldFocusable = this.focusable;
            this.focusable = focusable;
        }
        this.isFocusTraversableOverridden = 2;
        this.firePropertyChange("focusable", oldFocusable, focusable);
        if (oldFocusable && !focusable) {
            if (this.isFocusOwner() && KeyboardFocusManager.isAutoFocusTransferEnabled()) {
                this.transferFocus(true);
            }
            KeyboardFocusManager.clearMostRecentFocusOwner(this);
        }
    }

    final boolean isFocusTraversableOverridden() {
        return this.isFocusTraversableOverridden != 1;
    }

    public void setFocusTraversalKeys(int id, Set<? extends AWTKeyStroke> keystrokes) {
        if (id < 0 || id >= 3) {
            throw new IllegalArgumentException("invalid focus traversal key identifier");
        }
        this.setFocusTraversalKeys_NoIDCheck(id, keystrokes);
    }

    public Set<AWTKeyStroke> getFocusTraversalKeys(int id) {
        if (id < 0 || id >= 3) {
            throw new IllegalArgumentException("invalid focus traversal key identifier");
        }
        return this.getFocusTraversalKeys_NoIDCheck(id);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void setFocusTraversalKeys_NoIDCheck(int id, Set<? extends AWTKeyStroke> keystrokes) {
        Set<AWTKeyStroke> oldKeys;
        Component component = this;
        synchronized (component) {
            if (this.focusTraversalKeys == null) {
                this.initializeFocusTraversalKeys();
            }
            if (keystrokes != null) {
                for (AWTKeyStroke aWTKeyStroke : keystrokes) {
                    if (aWTKeyStroke == null) {
                        throw new IllegalArgumentException("cannot set null focus traversal key");
                    }
                    if (aWTKeyStroke.getKeyChar() != '\uffff') {
                        throw new IllegalArgumentException("focus traversal keys cannot map to KEY_TYPED events");
                    }
                    for (int i = 0; i < this.focusTraversalKeys.length; ++i) {
                        if (i == id || !this.getFocusTraversalKeys_NoIDCheck(i).contains(aWTKeyStroke)) continue;
                        throw new IllegalArgumentException("focus traversal keys must be unique for a Component");
                    }
                }
            }
            oldKeys = this.focusTraversalKeys[id];
            this.focusTraversalKeys[id] = keystrokes != null ? Collections.unmodifiableSet(new HashSet<AWTKeyStroke>(keystrokes)) : null;
        }
        this.firePropertyChange(focusTraversalKeyPropertyNames[id], oldKeys, keystrokes);
    }

    final Set<AWTKeyStroke> getFocusTraversalKeys_NoIDCheck(int id) {
        Set<AWTKeyStroke> keystrokes;
        Set<AWTKeyStroke> set = keystrokes = this.focusTraversalKeys != null ? this.focusTraversalKeys[id] : null;
        if (keystrokes != null) {
            return keystrokes;
        }
        Container parent = this.parent;
        if (parent != null) {
            return parent.getFocusTraversalKeys(id);
        }
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(id);
    }

    public boolean areFocusTraversalKeysSet(int id) {
        if (id < 0 || id >= 3) {
            throw new IllegalArgumentException("invalid focus traversal key identifier");
        }
        return this.focusTraversalKeys != null && this.focusTraversalKeys[id] != null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setFocusTraversalKeysEnabled(boolean focusTraversalKeysEnabled) {
        boolean oldFocusTraversalKeysEnabled;
        Component component = this;
        synchronized (component) {
            oldFocusTraversalKeysEnabled = this.focusTraversalKeysEnabled;
            this.focusTraversalKeysEnabled = focusTraversalKeysEnabled;
        }
        this.firePropertyChange("focusTraversalKeysEnabled", oldFocusTraversalKeysEnabled, focusTraversalKeysEnabled);
    }

    public boolean getFocusTraversalKeysEnabled() {
        return this.focusTraversalKeysEnabled;
    }

    public void requestFocus() {
        this.requestFocusHelper(false, true);
    }

    public void requestFocus(FocusEvent.Cause cause) {
        this.requestFocusHelper(false, true, cause);
    }

    protected boolean requestFocus(boolean temporary) {
        return this.requestFocusHelper(temporary, true);
    }

    protected boolean requestFocus(boolean temporary, FocusEvent.Cause cause) {
        return this.requestFocusHelper(temporary, true, cause);
    }

    public boolean requestFocusInWindow() {
        return this.requestFocusHelper(false, false);
    }

    public boolean requestFocusInWindow(FocusEvent.Cause cause) {
        return this.requestFocusHelper(false, false, cause);
    }

    protected boolean requestFocusInWindow(boolean temporary) {
        return this.requestFocusHelper(temporary, false);
    }

    boolean requestFocusInWindow(boolean temporary, FocusEvent.Cause cause) {
        return this.requestFocusHelper(temporary, false, cause);
    }

    final boolean requestFocusHelper(boolean temporary, boolean focusedWindowChangeAllowed) {
        return this.requestFocusHelper(temporary, focusedWindowChangeAllowed, FocusEvent.Cause.UNKNOWN);
    }

    final boolean requestFocusHelper(boolean temporary, boolean focusedWindowChangeAllowed, FocusEvent.Cause cause) {
        Component heavyweight;
        Component source;
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        if (currentEvent instanceof MouseEvent && SunToolkit.isSystemGenerated(currentEvent) && ((source = ((MouseEvent)currentEvent).getComponent()) == null || source.getContainingWindow() == this.getContainingWindow())) {
            focusLog.finest("requesting focus by mouse event \"in window\"");
            focusedWindowChangeAllowed = false;
        }
        if (!this.isRequestFocusAccepted(temporary, focusedWindowChangeAllowed, cause)) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("requestFocus is not accepted");
            }
            return false;
        }
        KeyboardFocusManager.setMostRecentFocusOwner(this);
        Component window = this;
        while (window != null && !(window instanceof Window)) {
            if (!window.isVisible()) {
                if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                    focusLog.finest("component is recursively invisible");
                }
                return false;
            }
            window = window.parent;
        }
        ComponentPeer peer = this.peer;
        Component component = heavyweight = peer instanceof LightweightPeer ? this.getNativeContainer() : this;
        if (heavyweight == null || !heavyweight.isVisible()) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("Component is not a part of visible hierarchy");
            }
            return false;
        }
        peer = heavyweight.peer;
        if (peer == null) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("Peer is null");
            }
            return false;
        }
        long time = 0L;
        time = EventQueue.isDispatchThread() ? Toolkit.getEventQueue().getMostRecentKeyEventTime() : System.currentTimeMillis();
        boolean success = peer.requestFocus(this, temporary, focusedWindowChangeAllowed, time, cause);
        if (!success) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager(this.appContext).dequeueKeyEvents(time, this);
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("Peer request failed");
            }
        } else if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
            focusLog.finest("Pass for " + String.valueOf(this));
        }
        return success;
    }

    private boolean isRequestFocusAccepted(boolean temporary, boolean focusedWindowChangeAllowed, FocusEvent.Cause cause) {
        if (!this.isFocusable() || !this.isVisible()) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("Not focusable or not visible");
            }
            return false;
        }
        ComponentPeer peer = this.peer;
        if (peer == null) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("peer is null");
            }
            return false;
        }
        Window window = this.getContainingWindow();
        if (window == null || !window.isFocusableWindow()) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("Component doesn't have toplevel");
            }
            return false;
        }
        Component focusOwner = KeyboardFocusManager.getMostRecentFocusOwner(window);
        if (focusOwner == null && (focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()) != null && focusOwner.getContainingWindow() != window) {
            focusOwner = null;
        }
        if (focusOwner == this || focusOwner == null) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("focus owner is null or this");
            }
            return true;
        }
        if (FocusEvent.Cause.ACTIVATION == cause) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("cause is activation");
            }
            return true;
        }
        boolean ret = requestFocusController.acceptRequestFocus(focusOwner, this, temporary, focusedWindowChangeAllowed, cause);
        if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
            focusLog.finest("RequestFocusController returns {0}", ret);
        }
        return ret;
    }

    static synchronized void setRequestFocusController(RequestFocusController requestController) {
        requestFocusController = requestController == null ? new DummyRequestFocusController() : requestController;
    }

    public Container getFocusCycleRootAncestor() {
        Container rootAncestor = this.parent;
        while (rootAncestor != null && !rootAncestor.isFocusCycleRoot()) {
            rootAncestor = rootAncestor.parent;
        }
        return rootAncestor;
    }

    public boolean isFocusCycleRoot(Container container) {
        Container rootAncestor = this.getFocusCycleRootAncestor();
        return rootAncestor == container;
    }

    Container getTraversalRoot() {
        return this.getFocusCycleRootAncestor();
    }

    public void transferFocus() {
        this.nextFocus();
    }

    @Deprecated
    public void nextFocus() {
        this.transferFocus(false);
    }

    boolean transferFocus(boolean clearOnFailure) {
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("clearOnFailure = " + clearOnFailure);
        }
        Component toFocus = this.getNextFocusCandidate();
        boolean res = false;
        if (toFocus != null && !toFocus.isFocusOwner() && toFocus != this) {
            res = toFocus.requestFocusInWindow(FocusEvent.Cause.TRAVERSAL_FORWARD);
        }
        if (clearOnFailure && !res) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                focusLog.finer("clear global focus owner");
            }
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwnerPriv();
        }
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("returning result: " + res);
        }
        return res;
    }

    final Component getNextFocusCandidate() {
        Container rootAncestor = this.getTraversalRoot();
        Component comp = this;
        while (!(rootAncestor == null || rootAncestor.isShowing() && rootAncestor.canBeFocusOwner())) {
            comp = rootAncestor;
            rootAncestor = comp.getFocusCycleRootAncestor();
        }
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("comp = " + String.valueOf(comp) + ", root = " + String.valueOf(rootAncestor));
        }
        Component candidate = null;
        if (rootAncestor != null) {
            Applet applet;
            FocusTraversalPolicy policy = rootAncestor.getFocusTraversalPolicy();
            Component toFocus = policy.getComponentAfter(rootAncestor, comp);
            if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                focusLog.finer("component after is " + String.valueOf(toFocus));
            }
            if (toFocus == null) {
                toFocus = policy.getDefaultComponent(rootAncestor);
                if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                    focusLog.finer("default component is " + String.valueOf(toFocus));
                }
            }
            if (toFocus == null && (applet = EmbeddedFrame.getAppletIfAncestorOf(this)) != null) {
                toFocus = applet;
            }
            candidate = toFocus;
        }
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("Focus transfer candidate: " + String.valueOf(candidate));
        }
        return candidate;
    }

    public void transferFocusBackward() {
        this.transferFocusBackward(false);
    }

    boolean transferFocusBackward(boolean clearOnFailure) {
        Container rootAncestor = this.getTraversalRoot();
        Component comp = this;
        while (!(rootAncestor == null || rootAncestor.isShowing() && rootAncestor.canBeFocusOwner())) {
            comp = rootAncestor;
            rootAncestor = comp.getFocusCycleRootAncestor();
        }
        boolean res = false;
        if (rootAncestor != null) {
            FocusTraversalPolicy policy = rootAncestor.getFocusTraversalPolicy();
            Component toFocus = policy.getComponentBefore(rootAncestor, comp);
            if (toFocus == null) {
                toFocus = policy.getDefaultComponent(rootAncestor);
            }
            if (toFocus != null) {
                res = toFocus.requestFocusInWindow(FocusEvent.Cause.TRAVERSAL_BACKWARD);
            }
        }
        if (clearOnFailure && !res) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                focusLog.finer("clear global focus owner");
            }
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwnerPriv();
        }
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("returning result: " + res);
        }
        return res;
    }

    public void transferFocusUpCycle() {
        Container rootAncestor;
        for (rootAncestor = this.getFocusCycleRootAncestor(); !(rootAncestor == null || rootAncestor.isShowing() && rootAncestor.isFocusable() && rootAncestor.isEnabled()); rootAncestor = rootAncestor.getFocusCycleRootAncestor()) {
        }
        if (rootAncestor != null) {
            Container rootAncestorRootAncestor = rootAncestor.getFocusCycleRootAncestor();
            Container fcr = rootAncestorRootAncestor != null ? rootAncestorRootAncestor : rootAncestor;
            KeyboardFocusManager.getCurrentKeyboardFocusManager().setGlobalCurrentFocusCycleRootPriv(fcr);
            rootAncestor.requestFocus(FocusEvent.Cause.TRAVERSAL_UP);
        } else {
            Component toFocus;
            Window window = this.getContainingWindow();
            if (window != null && (toFocus = window.getFocusTraversalPolicy().getDefaultComponent(window)) != null) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().setGlobalCurrentFocusCycleRootPriv(window);
                toFocus.requestFocus(FocusEvent.Cause.TRAVERSAL_UP);
            }
        }
    }

    public boolean hasFocus() {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this;
    }

    public boolean isFocusOwner() {
        return this.hasFocus();
    }

    void setAutoFocusTransferOnDisposal(boolean value) {
        this.autoFocusTransferOnDisposal = value;
    }

    boolean isAutoFocusTransferOnDisposal() {
        return this.autoFocusTransferOnDisposal;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void add(PopupMenu popup) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (popup.parent != null) {
                popup.parent.remove(popup);
            }
            if (this.popups == null) {
                this.popups = new Vector();
            }
            this.popups.addElement(popup);
            popup.parent = this;
            if (this.peer != null && popup.peer == null) {
                popup.addNotify();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void remove(MenuComponent popup) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.popups == null) {
                return;
            }
            int index = this.popups.indexOf(popup);
            if (index >= 0) {
                PopupMenu pmenu = (PopupMenu)popup;
                if (pmenu.peer != null) {
                    pmenu.removeNotify();
                }
                pmenu.parent = null;
                this.popups.removeElementAt(index);
                if (this.popups.size() == 0) {
                    this.popups = null;
                }
            }
        }
    }

    protected String paramString() {
        String thisName = Objects.toString(this.getName(), "");
        String invalid = this.isValid() ? "" : ",invalid";
        String hidden = this.visible ? "" : ",hidden";
        String disabled = this.enabled ? "" : ",disabled";
        return thisName + "," + this.x + "," + this.y + "," + this.width + "x" + this.height + invalid + hidden + disabled;
    }

    public String toString() {
        return this.getClass().getName() + "[" + this.paramString() + "]";
    }

    public void list() {
        this.list(System.out, 0);
    }

    public void list(PrintStream out) {
        this.list(out, 0);
    }

    public void list(PrintStream out, int indent) {
        for (int i = 0; i < indent; ++i) {
            out.print(" ");
        }
        out.println(this);
    }

    public void list(PrintWriter out) {
        this.list(out, 0);
    }

    public void list(PrintWriter out, int indent) {
        for (int i = 0; i < indent; ++i) {
            out.print(" ");
        }
        out.println(this);
    }

    final Container getNativeContainer() {
        Container p;
        for (p = this.getContainer(); p != null && p.peer instanceof LightweightPeer; p = p.getContainer()) {
        }
        return p;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        Object object = this.getObjectLock();
        synchronized (object) {
            if (listener == null) {
                return;
            }
            if (this.changeSupport == null) {
                this.changeSupport = new PropertyChangeSupport(this);
            }
            this.changeSupport.addPropertyChangeListener(listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        Object object = this.getObjectLock();
        synchronized (object) {
            if (listener == null || this.changeSupport == null) {
                return;
            }
            this.changeSupport.removePropertyChangeListener(listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        Object object = this.getObjectLock();
        synchronized (object) {
            if (this.changeSupport == null) {
                return new PropertyChangeListener[0];
            }
            return this.changeSupport.getPropertyChangeListeners();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        Object object = this.getObjectLock();
        synchronized (object) {
            if (listener == null) {
                return;
            }
            if (this.changeSupport == null) {
                this.changeSupport = new PropertyChangeSupport(this);
            }
            this.changeSupport.addPropertyChangeListener(propertyName, listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        Object object = this.getObjectLock();
        synchronized (object) {
            if (listener == null || this.changeSupport == null) {
                return;
            }
            this.changeSupport.removePropertyChangeListener(propertyName, listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        Object object = this.getObjectLock();
        synchronized (object) {
            if (this.changeSupport == null) {
                return new PropertyChangeListener[0];
            }
            return this.changeSupport.getPropertyChangeListeners(propertyName);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeSupport changeSupport;
        Object object = this.getObjectLock();
        synchronized (object) {
            changeSupport = this.changeSupport;
        }
        if (changeSupport == null || oldValue != null && newValue != null && oldValue.equals(newValue)) {
            return;
        }
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        PropertyChangeSupport changeSupport = this.changeSupport;
        if (changeSupport == null || oldValue == newValue) {
            return;
        }
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
        PropertyChangeSupport changeSupport = this.changeSupport;
        if (changeSupport == null || oldValue == newValue) {
            return;
        }
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
        if (this.changeSupport == null || oldValue == newValue) {
            return;
        }
        this.firePropertyChange(propertyName, (Object)oldValue, (Object)newValue);
    }

    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
        if (this.changeSupport == null || oldValue == newValue) {
            return;
        }
        this.firePropertyChange(propertyName, Character.valueOf(oldValue), Character.valueOf(newValue));
    }

    public void firePropertyChange(String propertyName, short oldValue, short newValue) {
        if (this.changeSupport == null || oldValue == newValue) {
            return;
        }
        this.firePropertyChange(propertyName, (Object)oldValue, (Object)newValue);
    }

    public void firePropertyChange(String propertyName, long oldValue, long newValue) {
        if (this.changeSupport == null || oldValue == newValue) {
            return;
        }
        this.firePropertyChange(propertyName, (Object)oldValue, (Object)newValue);
    }

    public void firePropertyChange(String propertyName, float oldValue, float newValue) {
        if (this.changeSupport == null || oldValue == newValue) {
            return;
        }
        this.firePropertyChange(propertyName, Float.valueOf(oldValue), Float.valueOf(newValue));
    }

    public void firePropertyChange(String propertyName, double oldValue, double newValue) {
        if (this.changeSupport == null || oldValue == newValue) {
            return;
        }
        this.firePropertyChange(propertyName, (Object)oldValue, (Object)newValue);
    }

    private void doSwingSerialization() {
        if (!(this instanceof JComponent)) {
            return;
        }
        Package swingPackage = Package.getPackage("javax.swing");
        for (Class<?> klass = this.getClass(); klass != null; klass = klass.getSuperclass()) {
            if (klass.getPackage() != swingPackage || klass.getClassLoader() != null) continue;
            SwingAccessor.getJComponentAccessor().compWriteObjectNotify((JComponent)this);
            return;
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        this.doSwingSerialization();
        s.defaultWriteObject();
        AWTEventMulticaster.save(s, componentListenerK, this.componentListener);
        AWTEventMulticaster.save(s, focusListenerK, this.focusListener);
        AWTEventMulticaster.save(s, keyListenerK, this.keyListener);
        AWTEventMulticaster.save(s, mouseListenerK, this.mouseListener);
        AWTEventMulticaster.save(s, mouseMotionListenerK, this.mouseMotionListener);
        AWTEventMulticaster.save(s, inputMethodListenerK, this.inputMethodListener);
        s.writeObject(null);
        s.writeObject(this.componentOrientation);
        AWTEventMulticaster.save(s, hierarchyListenerK, this.hierarchyListener);
        AWTEventMulticaster.save(s, hierarchyBoundsListenerK, this.hierarchyBoundsListener);
        s.writeObject(null);
        AWTEventMulticaster.save(s, mouseWheelListenerK, this.mouseWheelListener);
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        block23: {
            String key;
            Object keyOrNull;
            block22: {
                Object orient;
                block21: {
                    this.objectLock = new Object();
                    this.acc = AccessController.getContext();
                    s.defaultReadObject();
                    this.appContext = AppContext.getAppContext();
                    this.coalescingEnabled = this.checkCoalescing();
                    if (this.componentSerializedDataVersion < 4) {
                        this.focusable = true;
                        this.isFocusTraversableOverridden = 0;
                        this.initializeFocusTraversalKeys();
                        this.focusTraversalKeysEnabled = true;
                    }
                    while (null != (keyOrNull = s.readObject())) {
                        String key2 = ((String)keyOrNull).intern();
                        if (componentListenerK == key2) {
                            this.addComponentListener((ComponentListener)s.readObject());
                            continue;
                        }
                        if (focusListenerK == key2) {
                            this.addFocusListener((FocusListener)s.readObject());
                            continue;
                        }
                        if (keyListenerK == key2) {
                            this.addKeyListener((KeyListener)s.readObject());
                            continue;
                        }
                        if (mouseListenerK == key2) {
                            this.addMouseListener((MouseListener)s.readObject());
                            continue;
                        }
                        if (mouseMotionListenerK == key2) {
                            this.addMouseMotionListener((MouseMotionListener)s.readObject());
                            continue;
                        }
                        if (inputMethodListenerK == key2) {
                            this.addInputMethodListener((InputMethodListener)s.readObject());
                            continue;
                        }
                        s.readObject();
                    }
                    orient = null;
                    try {
                        orient = s.readObject();
                    }
                    catch (OptionalDataException e) {
                        if (e.eof) break block21;
                        throw e;
                    }
                }
                this.componentOrientation = orient != null ? (ComponentOrientation)orient : ComponentOrientation.UNKNOWN;
                try {
                    while (null != (keyOrNull = s.readObject())) {
                        key = ((String)keyOrNull).intern();
                        if (hierarchyListenerK == key) {
                            this.addHierarchyListener((HierarchyListener)s.readObject());
                            continue;
                        }
                        if (hierarchyBoundsListenerK == key) {
                            this.addHierarchyBoundsListener((HierarchyBoundsListener)s.readObject());
                            continue;
                        }
                        s.readObject();
                    }
                }
                catch (OptionalDataException e) {
                    if (e.eof) break block22;
                    throw e;
                }
            }
            try {
                while (null != (keyOrNull = s.readObject())) {
                    key = ((String)keyOrNull).intern();
                    if (mouseWheelListenerK == key) {
                        this.addMouseWheelListener((MouseWheelListener)s.readObject());
                        continue;
                    }
                    s.readObject();
                }
            }
            catch (OptionalDataException e) {
                if (e.eof) break block23;
                throw e;
            }
        }
        if (this.popups != null) {
            int npopups = this.popups.size();
            for (int i = 0; i < npopups; ++i) {
                PopupMenu popup = this.popups.elementAt(i);
                popup.parent = this;
            }
        }
    }

    public void setComponentOrientation(ComponentOrientation o) {
        ComponentOrientation oldValue = this.componentOrientation;
        this.componentOrientation = o;
        this.firePropertyChange("componentOrientation", oldValue, o);
        this.invalidateIfValid();
    }

    public ComponentOrientation getComponentOrientation() {
        return this.componentOrientation;
    }

    public void applyComponentOrientation(ComponentOrientation orientation) {
        if (orientation == null) {
            throw new NullPointerException();
        }
        this.setComponentOrientation(orientation);
    }

    final boolean canBeFocusOwner() {
        return this.isEnabled() && this.isDisplayable() && this.isVisible() && this.isFocusable();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final boolean canBeFocusOwnerRecursively() {
        if (!this.canBeFocusOwner()) {
            return false;
        }
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.parent != null) {
                return this.parent.canContainFocusOwner(this);
            }
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void relocateComponent() {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.peer == null) {
                return;
            }
            int nativeX = this.x;
            int nativeY = this.y;
            for (Container cont = this.getContainer(); cont != null && cont.isLightweight(); cont = cont.getContainer()) {
                nativeX += cont.x;
                nativeY += cont.y;
            }
            this.peer.setBounds(nativeX, nativeY, this.width, this.height, 1);
        }
    }

    Window getContainingWindow() {
        return SunToolkit.getContainingWindow(this);
    }

    private static native void initIDs();

    public AccessibleContext getAccessibleContext() {
        return this.accessibleContext;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    int getAccessibleIndexInParent() {
        Object object = this.getTreeLock();
        synchronized (object) {
            AccessibleContext accContext = this.getAccessibleContext();
            if (accContext == null) {
                return -1;
            }
            Accessible parent = accContext.getAccessibleParent();
            if (parent == null) {
                return -1;
            }
            accContext = parent.getAccessibleContext();
            for (int i = 0; i < accContext.getAccessibleChildrenCount(); ++i) {
                if (!this.equals(accContext.getAccessibleChild(i))) continue;
                return i;
            }
            return -1;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    AccessibleStateSet getAccessibleStateSet() {
        Object object = this.getTreeLock();
        synchronized (object) {
            AccessibleSelection as;
            AccessibleContext pac;
            Accessible ap;
            AccessibleContext ac;
            AccessibleStateSet states = new AccessibleStateSet();
            if (this.isEnabled()) {
                states.add(AccessibleState.ENABLED);
            }
            if (this.isFocusTraversable()) {
                states.add(AccessibleState.FOCUSABLE);
            }
            if (this.isVisible()) {
                states.add(AccessibleState.VISIBLE);
            }
            if (this.isShowing()) {
                states.add(AccessibleState.SHOWING);
            }
            if (this.isFocusOwner()) {
                states.add(AccessibleState.FOCUSED);
            }
            if (this instanceof Accessible && (ac = ((Accessible)((Object)this)).getAccessibleContext()) != null && (ap = ac.getAccessibleParent()) != null && (pac = ap.getAccessibleContext()) != null && (as = pac.getAccessibleSelection()) != null) {
                states.add(AccessibleState.SELECTABLE);
                int i = ac.getAccessibleIndexInParent();
                if (i >= 0 && as.isAccessibleChildSelected(i)) {
                    states.add(AccessibleState.SELECTED);
                }
            }
            if (Component.isInstanceOf(this, "javax.swing.JComponent") && ((JComponent)this).isOpaque()) {
                states.add(AccessibleState.OPAQUE);
            }
            return states;
        }
    }

    static boolean isInstanceOf(Object obj, String className) {
        if (obj == null) {
            return false;
        }
        if (className == null) {
            return false;
        }
        for (Class<?> cls = obj.getClass(); cls != null; cls = cls.getSuperclass()) {
            if (!cls.getName().equals(className)) continue;
            return true;
        }
        return false;
    }

    final boolean areBoundsValid() {
        Container cont = this.getContainer();
        return cont == null || cont.isValid() || cont.getLayout() == null;
    }

    void applyCompoundShape(Region shape) {
        ComponentPeer peer;
        this.checkTreeLock();
        if (!this.areBoundsValid()) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + String.valueOf(this) + "; areBoundsValid = " + this.areBoundsValid());
            }
            return;
        }
        if (!this.isLightweight() && (peer = this.peer) != null) {
            if (shape.isEmpty()) {
                shape = Region.EMPTY_REGION;
            }
            if (shape.equals(this.getNormalShape())) {
                if (this.compoundShape == null) {
                    return;
                }
                this.compoundShape = null;
                peer.applyShape(null);
            } else {
                if (shape.equals(this.getAppliedShape())) {
                    return;
                }
                this.compoundShape = shape;
                Point compAbsolute = this.getLocationOnWindow();
                if (mixingLog.isLoggable(PlatformLogger.Level.FINER)) {
                    mixingLog.fine("this = " + String.valueOf(this) + "; compAbsolute=" + String.valueOf(compAbsolute) + "; shape=" + String.valueOf(shape));
                }
                peer.applyShape(shape.getTranslatedRegion(-compAbsolute.x, -compAbsolute.y));
            }
        }
    }

    private Region getAppliedShape() {
        this.checkTreeLock();
        return this.compoundShape == null || this.isLightweight() ? this.getNormalShape() : this.compoundShape;
    }

    Point getLocationOnWindow() {
        this.checkTreeLock();
        Point curLocation = this.getLocation();
        for (Container parent = this.getContainer(); parent != null && !(parent instanceof Window); parent = parent.getContainer()) {
            curLocation.x += parent.getX();
            curLocation.y += parent.getY();
        }
        return curLocation;
    }

    final Region getNormalShape() {
        this.checkTreeLock();
        Point compAbsolute = this.getLocationOnWindow();
        return Region.getInstanceXYWH(compAbsolute.x, compAbsolute.y, this.getWidth(), this.getHeight());
    }

    Region getOpaqueShape() {
        this.checkTreeLock();
        if (this.mixingCutoutRegion != null) {
            return this.mixingCutoutRegion;
        }
        return this.getNormalShape();
    }

    final int getSiblingIndexAbove() {
        this.checkTreeLock();
        Container parent = this.getContainer();
        if (parent == null) {
            return -1;
        }
        int nextAbove = parent.getComponentZOrder(this) - 1;
        return nextAbove < 0 ? -1 : nextAbove;
    }

    final ComponentPeer getHWPeerAboveMe() {
        this.checkTreeLock();
        int indexAbove = this.getSiblingIndexAbove();
        for (Container cont = this.getContainer(); cont != null; cont = cont.getContainer()) {
            for (int i = indexAbove; i > -1; --i) {
                Component comp = cont.getComponent(i);
                if (comp == null || !comp.isDisplayable() || comp.isLightweight()) continue;
                return comp.peer;
            }
            if (!cont.isLightweight()) break;
            indexAbove = cont.getSiblingIndexAbove();
        }
        return null;
    }

    final int getSiblingIndexBelow() {
        this.checkTreeLock();
        Container parent = this.getContainer();
        if (parent == null) {
            return -1;
        }
        int nextBelow = parent.getComponentZOrder(this) + 1;
        return nextBelow >= parent.getComponentCount() ? -1 : nextBelow;
    }

    final boolean isNonOpaqueForMixing() {
        return this.mixingCutoutRegion != null && this.mixingCutoutRegion.isEmpty();
    }

    private Region calculateCurrentShape() {
        this.checkTreeLock();
        Region s = this.getNormalShape();
        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("this = " + String.valueOf(this) + "; normalShape=" + String.valueOf(s));
        }
        if (this.getContainer() != null) {
            Component comp = this;
            for (Container cont = comp.getContainer(); cont != null; cont = cont.getContainer()) {
                for (int index = comp.getSiblingIndexAbove(); index != -1; --index) {
                    Component c = cont.getComponent(index);
                    if (!c.isLightweight() || !c.isShowing()) continue;
                    s = s.getDifference(c.getOpaqueShape());
                }
                if (!cont.isLightweight()) break;
                s = s.getIntersection(cont.getNormalShape());
                comp = cont;
            }
        }
        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("currentShape=" + String.valueOf(s));
        }
        return s;
    }

    void applyCurrentShape() {
        this.checkTreeLock();
        if (!this.areBoundsValid()) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + String.valueOf(this) + "; areBoundsValid = " + this.areBoundsValid());
            }
            return;
        }
        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("this = " + String.valueOf(this));
        }
        this.applyCompoundShape(this.calculateCurrentShape());
    }

    final void subtractAndApplyShape(Region s) {
        this.checkTreeLock();
        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("this = " + String.valueOf(this) + "; s=" + String.valueOf(s));
        }
        this.applyCompoundShape(this.getAppliedShape().getDifference(s));
    }

    private void applyCurrentShapeBelowMe() {
        this.checkTreeLock();
        Container parent = this.getContainer();
        if (parent != null && parent.isShowing()) {
            parent.recursiveApplyCurrentShape(this.getSiblingIndexBelow());
            Container parent2 = parent.getContainer();
            while (!parent.isOpaque() && parent2 != null) {
                parent2.recursiveApplyCurrentShape(parent.getSiblingIndexBelow());
                parent = parent2;
                parent2 = parent.getContainer();
            }
        }
    }

    final void subtractAndApplyShapeBelowMe() {
        this.checkTreeLock();
        Container parent = this.getContainer();
        if (parent != null && this.isShowing()) {
            Region opaqueShape = this.getOpaqueShape();
            parent.recursiveSubtractAndApplyShape(opaqueShape, this.getSiblingIndexBelow());
            Container parent2 = parent.getContainer();
            while (!parent.isOpaque() && parent2 != null) {
                parent2.recursiveSubtractAndApplyShape(opaqueShape, parent.getSiblingIndexBelow());
                parent = parent2;
                parent2 = parent.getContainer();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void mixOnShowing() {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + String.valueOf(this));
            }
            if (!this.isMixingNeeded()) {
                return;
            }
            if (this.isLightweight()) {
                this.subtractAndApplyShapeBelowMe();
            } else {
                this.applyCurrentShape();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void mixOnHiding(boolean isLightweight) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + String.valueOf(this) + "; isLightweight = " + isLightweight);
            }
            if (!this.isMixingNeeded()) {
                return;
            }
            if (isLightweight) {
                this.applyCurrentShapeBelowMe();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void mixOnReshaping() {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + String.valueOf(this));
            }
            if (!this.isMixingNeeded()) {
                return;
            }
            if (this.isLightweight()) {
                this.applyCurrentShapeBelowMe();
            } else {
                this.applyCurrentShape();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void mixOnZOrderChanging(int oldZorder, int newZorder) {
        Object object = this.getTreeLock();
        synchronized (object) {
            boolean becameHigher = newZorder < oldZorder;
            Container parent = this.getContainer();
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + String.valueOf(this) + "; oldZorder=" + oldZorder + "; newZorder=" + newZorder + "; parent=" + String.valueOf(parent));
            }
            if (!this.isMixingNeeded()) {
                return;
            }
            if (this.isLightweight()) {
                if (becameHigher) {
                    if (parent != null && this.isShowing()) {
                        parent.recursiveSubtractAndApplyShape(this.getOpaqueShape(), this.getSiblingIndexBelow(), oldZorder);
                    }
                } else if (parent != null) {
                    parent.recursiveApplyCurrentShape(oldZorder, newZorder);
                }
            } else if (becameHigher) {
                this.applyCurrentShape();
            } else if (parent != null) {
                Region shape = this.getAppliedShape();
                for (int index = oldZorder; index < newZorder; ++index) {
                    Component c = parent.getComponent(index);
                    if (!c.isLightweight() || !c.isShowing()) continue;
                    shape = shape.getDifference(c.getOpaqueShape());
                }
                this.applyCompoundShape(shape);
            }
        }
    }

    void mixOnValidating() {
    }

    final boolean isMixingNeeded() {
        if (SunToolkit.getSunAwtDisableMixing()) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINEST)) {
                mixingLog.finest("this = " + String.valueOf(this) + "; Mixing disabled via sun.awt.disableMixing");
            }
            return false;
        }
        if (!this.areBoundsValid()) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + String.valueOf(this) + "; areBoundsValid = " + this.areBoundsValid());
            }
            return false;
        }
        Window window = this.getContainingWindow();
        if (window != null) {
            if (!window.hasHeavyweightDescendants() || !window.hasLightweightDescendants() || window.isDisposing()) {
                if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                    mixingLog.fine("containing window = " + String.valueOf(window) + "; has h/w descendants = " + window.hasHeavyweightDescendants() + "; has l/w descendants = " + window.hasLightweightDescendants() + "; disposing = " + window.isDisposing());
                }
                return false;
            }
        } else {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + String.valueOf(this) + "; containing window is null");
            }
            return false;
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setMixingCutoutShape(Shape shape) {
        Region region = shape == null ? null : Region.getInstance(shape, null);
        Object object = this.getTreeLock();
        synchronized (object) {
            boolean needShowing = false;
            boolean needHiding = false;
            if (!this.isNonOpaqueForMixing()) {
                needHiding = true;
            }
            this.mixingCutoutRegion = region;
            if (!this.isNonOpaqueForMixing()) {
                needShowing = true;
            }
            if (this.isMixingNeeded()) {
                if (needHiding) {
                    this.mixOnHiding(this.isLightweight());
                }
                if (needShowing) {
                    this.mixOnShowing();
                }
            }
        }
    }

    void updateZOrder() {
        this.peer.setZOrder(this.getHWPeerAboveMe());
    }

    static {
        String s;
        log = PlatformLogger.getLogger("java.awt.Component");
        eventLog = PlatformLogger.getLogger("java.awt.event.Component");
        focusLog = PlatformLogger.getLogger("java.awt.focus.Component");
        mixingLog = PlatformLogger.getLogger("java.awt.mixing.Component");
        focusTraversalKeyPropertyNames = new String[]{"forwardFocusTraversalKeys", "backwardFocusTraversalKeys", "upCycleFocusTraversalKeys", "downCycleFocusTraversalKeys"};
        LOCK = new AWTTreeLock();
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            Component.initIDs();
        }
        isInc = (s = AccessController.doPrivileged(new GetPropertyAction("awt.image.incrementaldraw"))) == null || s.equals("true");
        String s2 = AccessController.doPrivileged(new GetPropertyAction("awt.image.redrawrate"));
        incRate = s2 != null ? Integer.parseInt(s2) : 100;
        AWTAccessor.setComponentAccessor(new AWTAccessor.ComponentAccessor(){

            @Override
            public void setBackgroundEraseDisabled(Component comp, boolean disabled) {
                comp.backgroundEraseDisabled = disabled;
            }

            @Override
            public boolean getBackgroundEraseDisabled(Component comp) {
                return comp.backgroundEraseDisabled;
            }

            @Override
            public Rectangle getBounds(Component comp) {
                return new Rectangle(comp.x, comp.y, comp.width, comp.height);
            }

            @Override
            public void setGraphicsConfiguration(Component comp, GraphicsConfiguration gc) {
                comp.setGraphicsConfiguration(gc);
            }

            @Override
            public void requestFocus(Component comp, FocusEvent.Cause cause) {
                comp.requestFocus(cause);
            }

            @Override
            public boolean canBeFocusOwner(Component comp) {
                return comp.canBeFocusOwner();
            }

            @Override
            public boolean isVisible(Component comp) {
                return comp.isVisible_NoClientCode();
            }

            @Override
            public void setRequestFocusController(RequestFocusController requestController) {
                Component.setRequestFocusController(requestController);
            }

            @Override
            public AppContext getAppContext(Component comp) {
                return comp.appContext;
            }

            @Override
            public void setAppContext(Component comp, AppContext appContext) {
                comp.appContext = appContext;
            }

            @Override
            public Container getParent(Component comp) {
                return comp.getParent_NoClientCode();
            }

            @Override
            public void setParent(Component comp, Container parent) {
                comp.parent = parent;
            }

            @Override
            public void setSize(Component comp, int width, int height) {
                comp.width = width;
                comp.height = height;
            }

            @Override
            public Point getLocation(Component comp) {
                return comp.location_NoClientCode();
            }

            @Override
            public void setLocation(Component comp, int x, int y) {
                comp.x = x;
                comp.y = y;
            }

            @Override
            public boolean isEnabled(Component comp) {
                return comp.isEnabledImpl();
            }

            @Override
            public boolean isDisplayable(Component comp) {
                return comp.peer != null;
            }

            @Override
            public Cursor getCursor(Component comp) {
                return comp.getCursor_NoClientCode();
            }

            @Override
            public <T extends ComponentPeer> T getPeer(Component comp) {
                return (T)comp.peer;
            }

            @Override
            public void setPeer(Component comp, ComponentPeer peer) {
                comp.peer = peer;
            }

            @Override
            public boolean isLightweight(Component comp) {
                return comp.peer instanceof LightweightPeer;
            }

            @Override
            public boolean getIgnoreRepaint(Component comp) {
                return comp.ignoreRepaint;
            }

            @Override
            public int getWidth(Component comp) {
                return comp.width;
            }

            @Override
            public int getHeight(Component comp) {
                return comp.height;
            }

            @Override
            public int getX(Component comp) {
                return comp.x;
            }

            @Override
            public int getY(Component comp) {
                return comp.y;
            }

            @Override
            public Color getForeground(Component comp) {
                return comp.foreground;
            }

            @Override
            public Color getBackground(Component comp) {
                return comp.background;
            }

            @Override
            public void setBackground(Component comp, Color background) {
                comp.background = background;
            }

            @Override
            public Font getFont(Component comp) {
                return comp.getFont_NoClientCode();
            }

            @Override
            public void processEvent(Component comp, AWTEvent e) {
                comp.processEvent(e);
            }

            @Override
            public AccessControlContext getAccessControlContext(Component comp) {
                return comp.getAccessControlContext();
            }

            @Override
            public void revalidateSynchronously(Component comp) {
                comp.revalidateSynchronously();
            }

            @Override
            public void createBufferStrategy(Component comp, int numBuffers, BufferCapabilities caps) throws AWTException {
                comp.createBufferStrategy(numBuffers, caps);
            }

            @Override
            public BufferStrategy getBufferStrategy(Component comp) {
                return comp.getBufferStrategy();
            }
        });
        coalesceMap = new WeakHashMap();
        coalesceEventsParams = new Class[]{AWTEvent.class, AWTEvent.class};
        requestFocusController = new DummyRequestFocusController();
    }

    public static enum BaselineResizeBehavior {
        CONSTANT_ASCENT,
        CONSTANT_DESCENT,
        CENTER_OFFSET,
        OTHER;

    }

    private class SingleBufferStrategy
    extends BufferStrategy {
        private BufferCapabilities caps;

        public SingleBufferStrategy(BufferCapabilities caps) {
            this.caps = caps;
        }

        @Override
        public BufferCapabilities getCapabilities() {
            return this.caps;
        }

        @Override
        public Graphics getDrawGraphics() {
            return Component.this.getGraphics();
        }

        @Override
        public boolean contentsLost() {
            return false;
        }

        @Override
        public boolean contentsRestored() {
            return false;
        }

        @Override
        public void show() {
        }
    }

    private static class ProxyCapabilities
    extends ExtendedBufferCapabilities {
        private BufferCapabilities orig;

        private ProxyCapabilities(BufferCapabilities orig) {
            super(orig.getFrontBufferCapabilities(), orig.getBackBufferCapabilities(), orig.getFlipContents() == BufferCapabilities.FlipContents.BACKGROUND ? BufferCapabilities.FlipContents.BACKGROUND : BufferCapabilities.FlipContents.COPIED);
            this.orig = orig;
        }
    }

    private class FlipSubRegionBufferStrategy
    extends FlipBufferStrategy
    implements SubRegionShowable {
        protected FlipSubRegionBufferStrategy(Component component, int numBuffers, BufferCapabilities caps) throws AWTException {
            super(numBuffers, caps);
        }

        @Override
        public void show(int x1, int y1, int x2, int y2) {
            this.showSubRegion(x1, y1, x2, y2);
        }

        @Override
        public boolean showIfNotLost(int x1, int y1, int x2, int y2) {
            if (!this.contentsLost()) {
                this.showSubRegion(x1, y1, x2, y2);
                return !this.contentsLost();
            }
            return false;
        }
    }

    private class BltSubRegionBufferStrategy
    extends BltBufferStrategy
    implements SubRegionShowable {
        protected BltSubRegionBufferStrategy(Component component, int numBuffers, BufferCapabilities caps) {
            super(numBuffers, caps);
        }

        @Override
        public void show(int x1, int y1, int x2, int y2) {
            this.showSubRegion(x1, y1, x2, y2);
        }

        @Override
        public boolean showIfNotLost(int x1, int y1, int x2, int y2) {
            if (!this.contentsLost()) {
                this.showSubRegion(x1, y1, x2, y2);
                return !this.contentsLost();
            }
            return false;
        }
    }

    protected class BltBufferStrategy
    extends BufferStrategy {
        protected BufferCapabilities caps;
        protected VolatileImage[] backBuffers;
        protected boolean validatedContents;
        protected int width;
        protected int height;
        private Insets insets;

        protected BltBufferStrategy(int numBuffers, BufferCapabilities caps) {
            this.caps = caps;
            this.createBackBuffers(numBuffers - 1);
        }

        @Override
        public void dispose() {
            if (this.backBuffers != null) {
                for (int counter = this.backBuffers.length - 1; counter >= 0; --counter) {
                    if (this.backBuffers[counter] == null) continue;
                    this.backBuffers[counter].flush();
                    this.backBuffers[counter] = null;
                }
            }
            if (Component.this.bufferStrategy == this) {
                Component.this.bufferStrategy = null;
            }
        }

        protected void createBackBuffers(int numBuffers) {
            if (numBuffers == 0) {
                this.backBuffers = null;
            } else {
                int i;
                this.width = Component.this.getWidth();
                this.height = Component.this.getHeight();
                this.insets = Component.this.getInsets_NoClientCode();
                int iWidth = this.width - this.insets.left - this.insets.right;
                int iHeight = this.height - this.insets.top - this.insets.bottom;
                iWidth = Math.max(1, iWidth);
                iHeight = Math.max(1, iHeight);
                if (this.backBuffers == null) {
                    this.backBuffers = new VolatileImage[numBuffers];
                } else {
                    for (i = 0; i < numBuffers; ++i) {
                        if (this.backBuffers[i] == null) continue;
                        this.backBuffers[i].flush();
                        this.backBuffers[i] = null;
                    }
                }
                for (i = 0; i < numBuffers; ++i) {
                    this.backBuffers[i] = Component.this.createVolatileImage(iWidth, iHeight);
                }
            }
        }

        @Override
        public BufferCapabilities getCapabilities() {
            return this.caps;
        }

        @Override
        public Graphics getDrawGraphics() {
            this.revalidate();
            Image backBuffer = this.getBackBuffer();
            if (backBuffer == null) {
                return Component.this.getGraphics();
            }
            SunGraphics2D g = (SunGraphics2D)backBuffer.getGraphics();
            g.constrain(-this.insets.left, -this.insets.top, backBuffer.getWidth(null) + this.insets.left, backBuffer.getHeight(null) + this.insets.top);
            return g;
        }

        Image getBackBuffer() {
            if (this.backBuffers != null) {
                return this.backBuffers[this.backBuffers.length - 1];
            }
            return null;
        }

        @Override
        public void show() {
            this.showSubRegion(this.insets.left, this.insets.top, this.width - this.insets.right, this.height - this.insets.bottom);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void showSubRegion(int x1, int y1, int x2, int y2) {
            if (this.backBuffers == null) {
                return;
            }
            x1 -= this.insets.left;
            x2 -= this.insets.left;
            y1 -= this.insets.top;
            y2 -= this.insets.top;
            Graphics g = Component.this.getGraphics_NoClientCode();
            if (g == null) {
                return;
            }
            try {
                g.translate(this.insets.left, this.insets.top);
                for (int i = 0; i < this.backBuffers.length; ++i) {
                    g.drawImage(this.backBuffers[i], x1, y1, x2, y2, x1, y1, x2, y2, null);
                    g.dispose();
                    g = null;
                    g = this.backBuffers[i].getGraphics();
                }
            }
            finally {
                if (g != null) {
                    g.dispose();
                }
            }
        }

        protected void revalidate() {
            this.revalidate(true);
        }

        void revalidate(boolean checkSize) {
            GraphicsConfiguration gc;
            int returnCode;
            this.validatedContents = false;
            if (this.backBuffers == null) {
                return;
            }
            if (checkSize) {
                Insets insets = Component.this.getInsets_NoClientCode();
                if (Component.this.getWidth() != this.width || Component.this.getHeight() != this.height || !insets.equals(this.insets)) {
                    this.createBackBuffers(this.backBuffers.length);
                    this.validatedContents = true;
                }
            }
            if ((returnCode = this.backBuffers[this.backBuffers.length - 1].validate(gc = Component.this.getGraphicsConfiguration_NoClientCode())) == 2) {
                if (checkSize) {
                    this.createBackBuffers(this.backBuffers.length);
                    this.backBuffers[this.backBuffers.length - 1].validate(gc);
                }
                this.validatedContents = true;
            } else if (returnCode == 1) {
                this.validatedContents = true;
            }
        }

        @Override
        public boolean contentsLost() {
            if (this.backBuffers == null) {
                return false;
            }
            return this.backBuffers[this.backBuffers.length - 1].contentsLost();
        }

        @Override
        public boolean contentsRestored() {
            return this.validatedContents;
        }
    }

    protected class FlipBufferStrategy
    extends BufferStrategy {
        protected int numBuffers;
        protected BufferCapabilities caps;
        protected Image drawBuffer;
        protected VolatileImage drawVBuffer;
        protected boolean validatedContents;
        private int width;
        private int height;

        protected FlipBufferStrategy(int numBuffers, BufferCapabilities caps) throws AWTException {
            if (!(Component.this instanceof Window || Component.this instanceof Canvas || Component.this instanceof Applet)) {
                throw new ClassCastException("Component must be a Canvas or Window or Applet");
            }
            this.numBuffers = numBuffers;
            this.caps = caps;
            this.createBuffers(numBuffers, caps);
        }

        protected void createBuffers(int numBuffers, BufferCapabilities caps) throws AWTException {
            ExtendedBufferCapabilities ebc;
            if (numBuffers < 2) {
                throw new IllegalArgumentException("Number of buffers cannot be less than two");
            }
            if (Component.this.peer == null) {
                throw new IllegalStateException("Component must have a valid peer");
            }
            if (caps == null || !caps.isPageFlipping()) {
                throw new IllegalArgumentException("Page flipping capabilities must be specified");
            }
            this.width = Component.this.getWidth();
            this.height = Component.this.getHeight();
            if (this.drawBuffer != null) {
                this.invalidate();
            }
            if (caps instanceof ExtendedBufferCapabilities && (ebc = (ExtendedBufferCapabilities)caps).getVSync() == ExtendedBufferCapabilities.VSyncType.VSYNC_ON && !VSyncedBSManager.vsyncAllowed(this)) {
                caps = ebc.derive(ExtendedBufferCapabilities.VSyncType.VSYNC_DEFAULT);
            }
            Component.this.peer.createBuffers(numBuffers, caps);
            this.updateInternalBuffers();
        }

        private void updateInternalBuffers() {
            this.drawBuffer = this.getBackBuffer();
            this.drawVBuffer = this.drawBuffer instanceof VolatileImage ? (VolatileImage)this.drawBuffer : null;
        }

        protected Image getBackBuffer() {
            if (Component.this.peer != null) {
                return Component.this.peer.getBackBuffer();
            }
            throw new IllegalStateException("Component must have a valid peer");
        }

        protected void flip(BufferCapabilities.FlipContents flipAction) {
            if (Component.this.peer != null) {
                Image backBuffer = this.getBackBuffer();
                if (backBuffer != null) {
                    Component.this.peer.flip(0, 0, backBuffer.getWidth(null), backBuffer.getHeight(null), flipAction);
                }
            } else {
                throw new IllegalStateException("Component must have a valid peer");
            }
        }

        void flipSubRegion(int x1, int y1, int x2, int y2, BufferCapabilities.FlipContents flipAction) {
            if (Component.this.peer == null) {
                throw new IllegalStateException("Component must have a valid peer");
            }
            Component.this.peer.flip(x1, y1, x2, y2, flipAction);
        }

        private void invalidate() {
            this.drawBuffer = null;
            this.drawVBuffer = null;
            this.destroyBuffers();
        }

        protected void destroyBuffers() {
            VSyncedBSManager.releaseVsync(this);
            if (Component.this.peer == null) {
                throw new IllegalStateException("Component must have a valid peer");
            }
            Component.this.peer.destroyBuffers();
        }

        @Override
        public BufferCapabilities getCapabilities() {
            if (this.caps instanceof ProxyCapabilities) {
                return ((ProxyCapabilities)this.caps).orig;
            }
            return this.caps;
        }

        @Override
        public Graphics getDrawGraphics() {
            this.revalidate();
            return this.drawBuffer.getGraphics();
        }

        protected void revalidate() {
            this.validatedContents = false;
            if (Component.this.getWidth() != this.width || Component.this.getHeight() != this.height || this.drawBuffer == null) {
                try {
                    this.createBuffers(this.numBuffers, this.caps);
                }
                catch (AWTException aWTException) {
                    // empty catch block
                }
                this.validatedContents = true;
            }
            this.updateInternalBuffers();
            if (this.drawVBuffer != null) {
                GraphicsConfiguration gc = Component.this.getGraphicsConfiguration_NoClientCode();
                int returnCode = this.drawVBuffer.validate(gc);
                if (returnCode == 2) {
                    try {
                        this.createBuffers(this.numBuffers, this.caps);
                    }
                    catch (AWTException aWTException) {
                        // empty catch block
                    }
                    if (this.drawVBuffer != null) {
                        this.drawVBuffer.validate(gc);
                    }
                    this.validatedContents = true;
                } else if (returnCode == 1) {
                    this.validatedContents = true;
                }
            }
        }

        @Override
        public boolean contentsLost() {
            if (this.drawVBuffer == null) {
                return false;
            }
            return this.drawVBuffer.contentsLost();
        }

        @Override
        public boolean contentsRestored() {
            return this.validatedContents;
        }

        @Override
        public void show() {
            this.flip(this.caps.getFlipContents());
        }

        void showSubRegion(int x1, int y1, int x2, int y2) {
            this.flipSubRegion(x1, y1, x2, y2, this.caps.getFlipContents());
        }

        @Override
        public void dispose() {
            if (Component.this.bufferStrategy == this) {
                Component.this.bufferStrategy = null;
                if (Component.this.peer != null) {
                    this.invalidate();
                }
            }
        }
    }

    private static class DummyRequestFocusController
    implements RequestFocusController {
        private DummyRequestFocusController() {
        }

        @Override
        public boolean acceptRequestFocus(Component from, Component to, boolean temporary, boolean focusedWindowChangeAllowed, FocusEvent.Cause cause) {
            return true;
        }
    }

    static class AWTTreeLock {
        AWTTreeLock() {
        }
    }

    protected abstract class AccessibleAWTComponent
    extends AccessibleContext
    implements Serializable,
    AccessibleComponent {
        private static final long serialVersionUID = 642321655757800191L;
        private volatile transient int propertyListenersCount;
        protected ComponentListener accessibleAWTComponentHandler = null;
        protected FocusListener accessibleAWTFocusHandler = null;

        protected AccessibleAWTComponent() {
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            if (this.accessibleAWTComponentHandler == null) {
                this.accessibleAWTComponentHandler = new AccessibleAWTComponentHandler();
            }
            if (this.accessibleAWTFocusHandler == null) {
                this.accessibleAWTFocusHandler = new AccessibleAWTFocusHandler();
            }
            if (this.propertyListenersCount++ == 0) {
                Component.this.addComponentListener(this.accessibleAWTComponentHandler);
                Component.this.addFocusListener(this.accessibleAWTFocusHandler);
            }
            super.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            if (--this.propertyListenersCount == 0) {
                Component.this.removeComponentListener(this.accessibleAWTComponentHandler);
                Component.this.removeFocusListener(this.accessibleAWTFocusHandler);
            }
            super.removePropertyChangeListener(listener);
        }

        @Override
        public String getAccessibleName() {
            return this.accessibleName;
        }

        @Override
        public String getAccessibleDescription() {
            return this.accessibleDescription;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.AWT_COMPONENT;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            return Component.this.getAccessibleStateSet();
        }

        @Override
        public Accessible getAccessibleParent() {
            if (this.accessibleParent != null) {
                return this.accessibleParent;
            }
            Container parent = Component.this.getParent();
            if (parent instanceof Accessible) {
                return (Accessible)((Object)parent);
            }
            return null;
        }

        @Override
        public int getAccessibleIndexInParent() {
            return Component.this.getAccessibleIndexInParent();
        }

        @Override
        public int getAccessibleChildrenCount() {
            return 0;
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            return null;
        }

        @Override
        public Locale getLocale() {
            return Component.this.getLocale();
        }

        @Override
        public AccessibleComponent getAccessibleComponent() {
            return this;
        }

        @Override
        public Color getBackground() {
            return Component.this.getBackground();
        }

        @Override
        public void setBackground(Color c) {
            Component.this.setBackground(c);
        }

        @Override
        public Color getForeground() {
            return Component.this.getForeground();
        }

        @Override
        public void setForeground(Color c) {
            Component.this.setForeground(c);
        }

        @Override
        public Cursor getCursor() {
            return Component.this.getCursor();
        }

        @Override
        public void setCursor(Cursor cursor) {
            Component.this.setCursor(cursor);
        }

        @Override
        public Font getFont() {
            return Component.this.getFont();
        }

        @Override
        public void setFont(Font f) {
            Component.this.setFont(f);
        }

        @Override
        public FontMetrics getFontMetrics(Font f) {
            if (f == null) {
                return null;
            }
            return Component.this.getFontMetrics(f);
        }

        @Override
        public boolean isEnabled() {
            return Component.this.isEnabled();
        }

        @Override
        public void setEnabled(boolean b) {
            boolean old = Component.this.isEnabled();
            Component.this.setEnabled(b);
            if (b != old && Component.this.accessibleContext != null) {
                if (b) {
                    Component.this.accessibleContext.firePropertyChange("AccessibleState", null, AccessibleState.ENABLED);
                } else {
                    Component.this.accessibleContext.firePropertyChange("AccessibleState", AccessibleState.ENABLED, null);
                }
            }
        }

        @Override
        public boolean isVisible() {
            return Component.this.isVisible();
        }

        @Override
        public void setVisible(boolean b) {
            boolean old = Component.this.isVisible();
            Component.this.setVisible(b);
            if (b != old && Component.this.accessibleContext != null) {
                if (b) {
                    Component.this.accessibleContext.firePropertyChange("AccessibleState", null, AccessibleState.VISIBLE);
                } else {
                    Component.this.accessibleContext.firePropertyChange("AccessibleState", AccessibleState.VISIBLE, null);
                }
            }
        }

        @Override
        public boolean isShowing() {
            return Component.this.isShowing();
        }

        @Override
        public boolean contains(Point p) {
            return Component.this.contains(p);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Point getLocationOnScreen() {
            Object object = Component.this.getTreeLock();
            synchronized (object) {
                if (Component.this.isShowing()) {
                    return Component.this.getLocationOnScreen();
                }
                return null;
            }
        }

        @Override
        public Point getLocation() {
            return Component.this.getLocation();
        }

        @Override
        public void setLocation(Point p) {
            Component.this.setLocation(p);
        }

        @Override
        public Rectangle getBounds() {
            return Component.this.getBounds();
        }

        @Override
        public void setBounds(Rectangle r) {
            Component.this.setBounds(r);
        }

        @Override
        public Dimension getSize() {
            return Component.this.getSize();
        }

        @Override
        public void setSize(Dimension d) {
            Component.this.setSize(d);
        }

        @Override
        public Accessible getAccessibleAt(Point p) {
            return null;
        }

        @Override
        public boolean isFocusTraversable() {
            return Component.this.isFocusTraversable();
        }

        @Override
        public void requestFocus() {
            Component.this.requestFocus();
        }

        @Override
        public void addFocusListener(FocusListener l) {
            Component.this.addFocusListener(l);
        }

        @Override
        public void removeFocusListener(FocusListener l) {
            Component.this.removeFocusListener(l);
        }

        protected class AccessibleAWTComponentHandler
        implements ComponentListener,
        Serializable {
            private static final long serialVersionUID = -1009684107426231869L;

            protected AccessibleAWTComponentHandler() {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                if (Component.this.accessibleContext != null) {
                    Component.this.accessibleContext.firePropertyChange("AccessibleState", AccessibleState.VISIBLE, null);
                }
            }

            @Override
            public void componentShown(ComponentEvent e) {
                if (Component.this.accessibleContext != null) {
                    Component.this.accessibleContext.firePropertyChange("AccessibleState", null, AccessibleState.VISIBLE);
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
            }
        }

        protected class AccessibleAWTFocusHandler
        implements FocusListener,
        Serializable {
            private static final long serialVersionUID = 3150908257351582233L;

            protected AccessibleAWTFocusHandler() {
            }

            @Override
            public void focusGained(FocusEvent event) {
                if (Component.this.accessibleContext != null) {
                    Component.this.accessibleContext.firePropertyChange("AccessibleState", null, AccessibleState.FOCUSED);
                }
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (Component.this.accessibleContext != null) {
                    Component.this.accessibleContext.firePropertyChange("AccessibleState", AccessibleState.FOCUSED, null);
                }
            }
        }
    }
}

