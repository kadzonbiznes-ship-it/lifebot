/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.AWTException;
import java.awt.AWTKeyStroke;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.BufferCapabilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.FocusManager;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.im.InputContext;
import java.awt.image.BufferStrategy;
import java.awt.peer.WindowPeer;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import sun.awt.AWTAccessor;
import sun.awt.AWTPermissions;
import sun.awt.AppContext;
import sun.awt.DebugSettings;
import sun.awt.SunToolkit;
import sun.awt.util.IdentityArrayList;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;
import sun.java2d.pipe.Region;
import sun.security.action.GetPropertyAction;
import sun.util.logging.PlatformLogger;

public class Window
extends Container
implements Accessible {
    String warningString;
    transient List<Image> icons;
    private transient Component temporaryLostComponent;
    static boolean systemSyncLWRequests = false;
    boolean syncLWRequests = false;
    transient boolean beforeFirstShow = true;
    private transient boolean disposing = false;
    transient WindowDisposerRecord disposerRecord = null;
    static final int OPENED = 1;
    int state;
    private boolean alwaysOnTop;
    private static final IdentityArrayList<Window> allWindows = new IdentityArrayList();
    transient Vector<WeakReference<Window>> ownedWindowList = new Vector();
    private transient WeakReference<Window> weakThis;
    transient boolean showWithParent;
    transient Dialog modalBlocker;
    Dialog.ModalExclusionType modalExclusionType;
    transient WindowListener windowListener;
    transient WindowStateListener windowStateListener;
    transient WindowFocusListener windowFocusListener;
    transient InputContext inputContext;
    private transient Object inputContextLock = new Object();
    private FocusManager focusMgr;
    private boolean focusableWindowState = true;
    private volatile boolean autoRequestFocus = true;
    transient boolean isInShow = false;
    private volatile float opacity = 1.0f;
    private Shape shape = null;
    private static final String base = "win";
    private static int nameCounter = 0;
    private static final long serialVersionUID = 4497834738069338734L;
    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.Window");
    private static final boolean locationByPlatformProp;
    transient boolean isTrayIconWindow = false;
    private volatile transient int securityWarningWidth;
    private volatile transient int securityWarningHeight;
    transient Object anchor = new Object();
    private static final AtomicBoolean beforeFirstWindowShown;
    private Type type = Type.NORMAL;
    private int windowSerializedDataVersion = 2;
    private volatile boolean locationByPlatform = locationByPlatformProp;

    private static native void initIDs();

    Window(GraphicsConfiguration gc) {
        this.init(gc);
    }

    private GraphicsConfiguration initGC(GraphicsConfiguration gc) {
        GraphicsEnvironment.checkHeadless();
        if (gc == null) {
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        }
        this.setGraphicsConfiguration(gc);
        return gc;
    }

    private void init(GraphicsConfiguration gc) {
        GraphicsEnvironment.checkHeadless();
        this.syncLWRequests = systemSyncLWRequests;
        this.weakThis = new WeakReference<Window>(this);
        this.addToWindowList();
        this.setWarningString();
        this.cursor = Cursor.getPredefinedCursor(0);
        this.visible = false;
        gc = this.initGC(gc);
        if (gc.getDevice().getType() != 0) {
            throw new IllegalArgumentException("not a screen device");
        }
        this.setLayout(new BorderLayout());
        Rectangle screenBounds = gc.getBounds();
        Insets screenInsets = this.getToolkit().getScreenInsets(gc);
        int x = this.getX() + screenBounds.x + screenInsets.left;
        int y = this.getY() + screenBounds.y + screenInsets.top;
        if (x != this.x || y != this.y) {
            this.setLocation(x, y);
            this.setLocationByPlatform(locationByPlatformProp);
        }
        this.modalExclusionType = Dialog.ModalExclusionType.NO_EXCLUDE;
        this.disposerRecord = new WindowDisposerRecord(this.appContext, this);
        Disposer.addRecord(this.anchor, this.disposerRecord);
        SunToolkit.checkAndSetPolicy(this);
    }

    Window() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.init(null);
    }

    public Window(Frame owner) {
        this(owner == null ? (GraphicsConfiguration)null : owner.getGraphicsConfiguration());
        this.ownedInit(owner);
    }

    public Window(Window owner) {
        this(owner == null ? (GraphicsConfiguration)null : owner.getGraphicsConfiguration());
        this.ownedInit(owner);
    }

    public Window(Window owner, GraphicsConfiguration gc) {
        this(gc);
        this.ownedInit(owner);
    }

    private void ownedInit(Window owner) {
        this.parent = owner;
        if (owner != null) {
            owner.addOwnedWindow(this.weakThis);
            if (owner.isAlwaysOnTop()) {
                try {
                    this.setAlwaysOnTop(true);
                }
                catch (SecurityException securityException) {
                    // empty catch block
                }
            }
        }
        this.disposerRecord.updateOwner();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    String constructComponentName() {
        Class<Window> clazz = Window.class;
        synchronized (Window.class) {
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return base + nameCounter++;
        }
    }

    public List<Image> getIconImages() {
        List<Image> icons = this.icons;
        if (icons == null || icons.size() == 0) {
            return new ArrayList<Image>();
        }
        return new ArrayList<Image>(icons);
    }

    public synchronized void setIconImages(List<? extends Image> icons) {
        this.icons = icons == null ? new ArrayList<Image>() : new ArrayList<Image>(icons);
        WindowPeer peer = (WindowPeer)this.peer;
        if (peer != null) {
            peer.updateIconImages();
        }
        this.firePropertyChange("iconImage", null, null);
    }

    public void setIconImage(Image image) {
        ArrayList<Image> imageList = new ArrayList<Image>();
        if (image != null) {
            imageList.add(image);
        }
        this.setIconImages(imageList);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addNotify() {
        Object object = this.getTreeLock();
        synchronized (object) {
            Container parent = this.parent;
            if (parent != null && parent.peer == null) {
                parent.addNotify();
            }
            if (this.peer == null) {
                this.peer = this.getComponentFactory().createWindow(this);
            }
            IdentityArrayList<Window> identityArrayList = allWindows;
            synchronized (identityArrayList) {
                allWindows.add(this);
            }
            super.addNotify();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeNotify() {
        Object object = this.getTreeLock();
        synchronized (object) {
            IdentityArrayList<Window> identityArrayList = allWindows;
            synchronized (identityArrayList) {
                allWindows.remove(this);
            }
            super.removeNotify();
        }
    }

    public void pack() {
        Container parent = this.parent;
        if (parent != null && parent.peer == null) {
            parent.addNotify();
        }
        if (this.peer == null) {
            this.addNotify();
        }
        Dimension newSize = this.getPreferredSize();
        if (this.peer != null) {
            this.setClientSize(newSize.width, newSize.height);
        }
        if (this.beforeFirstShow) {
            this.isPacked = true;
        }
        this.validateUnconditionally();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setMinimumSize(Dimension minimumSize) {
        Object object = this.getTreeLock();
        synchronized (object) {
            super.setMinimumSize(minimumSize);
            Dimension size = this.getSize();
            if (this.isMinimumSizeSet() && (size.width < minimumSize.width || size.height < minimumSize.height)) {
                int nw = Math.max(this.width, minimumSize.width);
                int nh = Math.max(this.height, minimumSize.height);
                this.setSize(nw, nh);
            }
            if (this.peer != null) {
                ((WindowPeer)this.peer).updateMinimumSize();
            }
        }
    }

    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
    }

    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
    }

    @Override
    public void setLocation(Point p) {
        super.setLocation(p);
    }

    @Override
    @Deprecated
    public void reshape(int x, int y, int width, int height) {
        if (this.isMinimumSizeSet()) {
            Dimension minSize = this.getMinimumSize();
            if (width < minSize.width) {
                width = minSize.width;
            }
            if (height < minSize.height) {
                height = minSize.height;
            }
        }
        super.reshape(x, y, width, height);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void setClientSize(int w, int h) {
        Object object = this.getTreeLock();
        synchronized (object) {
            this.setBoundsOp(4);
            this.setBounds(this.x, this.y, w, h);
        }
    }

    final void closeSplashScreen() {
        if (this.isTrayIconWindow) {
            return;
        }
        if (beforeFirstWindowShown.getAndSet(false)) {
            SunToolkit.closeSplashScreen();
            SplashScreen.markClosed();
        }
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    @Override
    @Deprecated
    public void show() {
        if (this.peer == null) {
            this.addNotify();
        }
        this.validateUnconditionally();
        this.isInShow = true;
        if (this.visible) {
            this.toFront();
        } else {
            this.beforeFirstShow = false;
            this.closeSplashScreen();
            Dialog.checkShouldBeBlocked(this);
            super.show();
            this.locationByPlatform = false;
            for (int i = 0; i < this.ownedWindowList.size(); ++i) {
                Window child = (Window)this.ownedWindowList.elementAt(i).get();
                if (child == null || !child.showWithParent) continue;
                child.show();
                child.showWithParent = false;
            }
            if (!this.isModalBlocked()) {
                this.updateChildrenBlocking();
            } else {
                this.modalBlocker.toFront_NoClientCode();
            }
            if (this instanceof Frame || this instanceof Dialog) {
                Window.updateChildFocusableWindowState(this);
            }
        }
        this.isInShow = false;
        if ((this.state & 1) == 0) {
            this.postWindowEvent(200);
            this.state |= 1;
        }
    }

    static void updateChildFocusableWindowState(Window w) {
        if (w.peer != null && w.isShowing()) {
            ((WindowPeer)w.peer).updateFocusableWindowState();
        }
        for (int i = 0; i < w.ownedWindowList.size(); ++i) {
            Window child = (Window)w.ownedWindowList.elementAt(i).get();
            if (child == null) continue;
            Window.updateChildFocusableWindowState(child);
        }
    }

    synchronized void postWindowEvent(int id) {
        if (this.windowListener != null || (this.eventMask & 0x40L) != 0L || Toolkit.enabledOnToolkit(64L)) {
            WindowEvent e = new WindowEvent(this, id);
            Toolkit.getEventQueue().postEvent(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    @Deprecated
    public void hide() {
        Vector<WeakReference<Window>> vector = this.ownedWindowList;
        synchronized (vector) {
            for (int i = 0; i < this.ownedWindowList.size(); ++i) {
                Window child = (Window)this.ownedWindowList.elementAt(i).get();
                if (child == null || !child.visible) continue;
                child.hide();
                child.showWithParent = true;
            }
        }
        if (this.isModalBlocked()) {
            this.modalBlocker.unblockWindow(this);
        }
        super.hide();
        this.locationByPlatform = false;
    }

    @Override
    final void clearMostRecentFocusOwnerOnHide() {
    }

    public void dispose() {
        this.doDispose();
    }

    void disposeImpl() {
        this.dispose();
        if (this.peer != null) {
            this.doDispose();
        }
    }

    void doDispose() {
        boolean fireWindowClosedEvent = this.isDisplayable();
        class DisposeAction
        implements Runnable {
            DisposeAction() {
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                Window.this.disposing = true;
                try {
                    Object[] ownedWindowArray;
                    GraphicsDevice gd = Window.this.getGraphicsConfiguration().getDevice();
                    if (gd.getFullScreenWindow() == Window.this) {
                        gd.setFullScreenWindow(null);
                    }
                    Vector<WeakReference<Window>> vector = Window.this.ownedWindowList;
                    synchronized (vector) {
                        ownedWindowArray = new Object[Window.this.ownedWindowList.size()];
                        Window.this.ownedWindowList.copyInto(ownedWindowArray);
                    }
                    for (int i = 0; i < ownedWindowArray.length; ++i) {
                        Window child = (Window)((WeakReference)ownedWindowArray[i]).get();
                        if (child == null) continue;
                        child.disposeImpl();
                    }
                    Window.this.hide();
                    Window.this.beforeFirstShow = true;
                    Window.this.removeNotify();
                    Object object = Window.this.inputContextLock;
                    synchronized (object) {
                        if (Window.this.inputContext != null) {
                            Window.this.inputContext.dispose();
                            Window.this.inputContext = null;
                        }
                    }
                    Window.this.clearCurrentFocusCycleRootOnHide();
                }
                finally {
                    Window.this.disposing = false;
                }
            }
        }
        DisposeAction action = new DisposeAction();
        if (EventQueue.isDispatchThread()) {
            action.run();
        } else {
            try {
                EventQueue.invokeAndWait(this, action);
            }
            catch (InterruptedException e) {
                System.err.println("Disposal was interrupted:");
                e.printStackTrace();
            }
            catch (InvocationTargetException e) {
                System.err.println("Exception during disposal:");
                e.printStackTrace();
            }
        }
        if (fireWindowClosedEvent) {
            this.postWindowEvent(202);
        }
    }

    @Override
    void adjustListeningChildrenOnParent(long mask, int num) {
    }

    @Override
    void adjustDescendantsOnParent(int num) {
    }

    public void toFront() {
        this.toFront_NoClientCode();
    }

    final void toFront_NoClientCode() {
        if (this.visible) {
            WindowPeer peer = (WindowPeer)this.peer;
            if (peer != null) {
                peer.toFront();
            }
            if (this.isModalBlocked()) {
                this.modalBlocker.toFront_NoClientCode();
            }
        }
    }

    public void toBack() {
        this.toBack_NoClientCode();
    }

    final void toBack_NoClientCode() {
        WindowPeer peer;
        if (this.isAlwaysOnTop()) {
            try {
                this.setAlwaysOnTop(false);
            }
            catch (SecurityException securityException) {
                // empty catch block
            }
        }
        if (this.visible && (peer = (WindowPeer)this.peer) != null) {
            peer.toBack();
        }
    }

    @Override
    public Toolkit getToolkit() {
        return Toolkit.getDefaultToolkit();
    }

    public final String getWarningString() {
        return this.warningString;
    }

    private void setWarningString() {
        this.warningString = null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                sm.checkPermission(AWTPermissions.TOPLEVEL_WINDOW_PERMISSION);
            }
            catch (SecurityException se) {
                this.warningString = AccessController.doPrivileged(new GetPropertyAction("awt.appletWarning", "Java Applet Window"));
            }
        }
    }

    @Override
    public Locale getLocale() {
        if (this.locale == null) {
            return Locale.getDefault();
        }
        return this.locale;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public InputContext getInputContext() {
        Object object = this.inputContextLock;
        synchronized (object) {
            if (this.inputContext == null) {
                this.inputContext = InputContext.getInstance();
            }
        }
        return this.inputContext;
    }

    @Override
    public void setCursor(Cursor cursor) {
        if (cursor == null) {
            cursor = Cursor.getPredefinedCursor(0);
        }
        super.setCursor(cursor);
    }

    public Window getOwner() {
        return this.getOwner_NoClientCode();
    }

    final Window getOwner_NoClientCode() {
        return (Window)this.parent;
    }

    public Window[] getOwnedWindows() {
        return this.getOwnedWindows_NoClientCode();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final Window[] getOwnedWindows_NoClientCode() {
        Window[] realCopy;
        Vector<WeakReference<Window>> vector = this.ownedWindowList;
        synchronized (vector) {
            int fullSize = this.ownedWindowList.size();
            int realSize = 0;
            Window[] fullCopy = new Window[fullSize];
            for (int i = 0; i < fullSize; ++i) {
                fullCopy[realSize] = (Window)this.ownedWindowList.elementAt(i).get();
                if (fullCopy[realSize] == null) continue;
                ++realSize;
            }
            realCopy = fullSize != realSize ? Arrays.copyOf(fullCopy, realSize) : fullCopy;
        }
        return realCopy;
    }

    boolean isModalBlocked() {
        return this.modalBlocker != null;
    }

    void setModalBlocked(Dialog blocker, boolean blocked, boolean peerCall) {
        WindowPeer peer;
        Dialog dialog = this.modalBlocker = blocked ? blocker : null;
        if (peerCall && (peer = (WindowPeer)this.peer) != null) {
            peer.setModalBlocked(blocker, blocked);
        }
    }

    Dialog getModalBlocker() {
        return this.modalBlocker;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static IdentityArrayList<Window> getAllWindows() {
        IdentityArrayList<Window> identityArrayList = allWindows;
        synchronized (identityArrayList) {
            IdentityArrayList<Window> v = new IdentityArrayList<Window>();
            v.addAll(allWindows);
            return v;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static IdentityArrayList<Window> getAllUnblockedWindows() {
        IdentityArrayList<Window> identityArrayList = allWindows;
        synchronized (identityArrayList) {
            IdentityArrayList<Window> unblocked = new IdentityArrayList<Window>();
            for (int i = 0; i < allWindows.size(); ++i) {
                Window w = allWindows.get(i);
                if (w.isModalBlocked()) continue;
                unblocked.add(w);
            }
            return unblocked;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Window[] getWindows(AppContext appContext) {
        Class<Window> clazz = Window.class;
        synchronized (Window.class) {
            Window[] realCopy;
            Vector windowList = (Vector)appContext.get(Window.class);
            if (windowList != null) {
                int fullSize = windowList.size();
                int realSize = 0;
                Window[] fullCopy = new Window[fullSize];
                for (int i = 0; i < fullSize; ++i) {
                    Window w = (Window)((WeakReference)windowList.get(i)).get();
                    if (w == null) continue;
                    fullCopy[realSize++] = w;
                }
                realCopy = fullSize != realSize ? Arrays.copyOf(fullCopy, realSize) : fullCopy;
            } else {
                realCopy = new Window[]{};
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return realCopy;
        }
    }

    public static Window[] getWindows() {
        return Window.getWindows(AppContext.getAppContext());
    }

    public static Window[] getOwnerlessWindows() {
        Window[] allWindows = Window.getWindows();
        int ownerlessCount = 0;
        for (Window w : allWindows) {
            if (w.getOwner() != null) continue;
            ++ownerlessCount;
        }
        Window[] ownerless = new Window[ownerlessCount];
        int c = 0;
        for (Window w : allWindows) {
            if (w.getOwner() != null) continue;
            ownerless[c++] = w;
        }
        return ownerless;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Window getDocumentRoot() {
        Object object = this.getTreeLock();
        synchronized (object) {
            Window w = this;
            while (w.getOwner() != null) {
                w = w.getOwner();
            }
            return w;
        }
    }

    public void setModalExclusionType(Dialog.ModalExclusionType exclusionType) {
        SecurityManager sm;
        if (exclusionType == null) {
            exclusionType = Dialog.ModalExclusionType.NO_EXCLUDE;
        }
        if (!Toolkit.getDefaultToolkit().isModalExclusionTypeSupported(exclusionType)) {
            exclusionType = Dialog.ModalExclusionType.NO_EXCLUDE;
        }
        if (this.modalExclusionType == exclusionType) {
            return;
        }
        if (exclusionType == Dialog.ModalExclusionType.TOOLKIT_EXCLUDE && (sm = System.getSecurityManager()) != null) {
            sm.checkPermission(AWTPermissions.TOOLKIT_MODALITY_PERMISSION);
        }
        this.modalExclusionType = exclusionType;
    }

    public Dialog.ModalExclusionType getModalExclusionType() {
        return this.modalExclusionType;
    }

    boolean isModalExcluded(Dialog.ModalExclusionType exclusionType) {
        if (this.modalExclusionType != null && this.modalExclusionType.compareTo(exclusionType) >= 0) {
            return true;
        }
        Window owner = this.getOwner_NoClientCode();
        return owner != null && owner.isModalExcluded(exclusionType);
    }

    void updateChildrenBlocking() {
        ArrayList<Window> childHierarchy = new ArrayList<Window>();
        Window[] ownedWindows = this.getOwnedWindows();
        for (int i = 0; i < ownedWindows.length; ++i) {
            childHierarchy.add(ownedWindows[i]);
        }
        for (int k = 0; k < childHierarchy.size(); ++k) {
            Window w = (Window)childHierarchy.get(k);
            if (!w.isVisible()) continue;
            if (w.isModalBlocked()) {
                Dialog blocker = w.getModalBlocker();
                blocker.unblockWindow(w);
            }
            Dialog.checkShouldBeBlocked(w);
            Window[] wOwned = w.getOwnedWindows();
            for (int j = 0; j < wOwned.length; ++j) {
                childHierarchy.add(wOwned[j]);
            }
        }
    }

    public synchronized void addWindowListener(WindowListener l) {
        if (l == null) {
            return;
        }
        this.newEventsOnly = true;
        this.windowListener = AWTEventMulticaster.add(this.windowListener, l);
    }

    public synchronized void addWindowStateListener(WindowStateListener l) {
        if (l == null) {
            return;
        }
        this.windowStateListener = AWTEventMulticaster.add(this.windowStateListener, l);
        this.newEventsOnly = true;
    }

    public synchronized void addWindowFocusListener(WindowFocusListener l) {
        if (l == null) {
            return;
        }
        this.windowFocusListener = AWTEventMulticaster.add(this.windowFocusListener, l);
        this.newEventsOnly = true;
    }

    public synchronized void removeWindowListener(WindowListener l) {
        if (l == null) {
            return;
        }
        this.windowListener = AWTEventMulticaster.remove(this.windowListener, l);
    }

    public synchronized void removeWindowStateListener(WindowStateListener l) {
        if (l == null) {
            return;
        }
        this.windowStateListener = AWTEventMulticaster.remove(this.windowStateListener, l);
    }

    public synchronized void removeWindowFocusListener(WindowFocusListener l) {
        if (l == null) {
            return;
        }
        this.windowFocusListener = AWTEventMulticaster.remove(this.windowFocusListener, l);
    }

    public synchronized WindowListener[] getWindowListeners() {
        return (WindowListener[])this.getListeners(WindowListener.class);
    }

    public synchronized WindowFocusListener[] getWindowFocusListeners() {
        return (WindowFocusListener[])this.getListeners(WindowFocusListener.class);
    }

    public synchronized WindowStateListener[] getWindowStateListeners() {
        return (WindowStateListener[])this.getListeners(WindowStateListener.class);
    }

    @Override
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if (listenerType == WindowFocusListener.class) {
            l = this.windowFocusListener;
        } else if (listenerType == WindowStateListener.class) {
            l = this.windowStateListener;
        } else if (listenerType == WindowListener.class) {
            l = this.windowListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners((EventListener)l, listenerType);
    }

    @Override
    boolean eventEnabled(AWTEvent e) {
        switch (e.id) {
            case 200: 
            case 201: 
            case 202: 
            case 203: 
            case 204: 
            case 205: 
            case 206: {
                return (this.eventMask & 0x40L) != 0L || this.windowListener != null;
            }
            case 207: 
            case 208: {
                return (this.eventMask & 0x80000L) != 0L || this.windowFocusListener != null;
            }
            case 209: {
                return (this.eventMask & 0x40000L) != 0L || this.windowStateListener != null;
            }
        }
        return super.eventEnabled(e);
    }

    @Override
    protected void processEvent(AWTEvent e) {
        if (e instanceof WindowEvent) {
            switch (e.getID()) {
                case 200: 
                case 201: 
                case 202: 
                case 203: 
                case 204: 
                case 205: 
                case 206: {
                    this.processWindowEvent((WindowEvent)e);
                    break;
                }
                case 207: 
                case 208: {
                    this.processWindowFocusEvent((WindowEvent)e);
                    break;
                }
                case 209: {
                    this.processWindowStateEvent((WindowEvent)e);
                }
            }
            return;
        }
        super.processEvent(e);
    }

    protected void processWindowEvent(WindowEvent e) {
        WindowListener listener = this.windowListener;
        if (listener != null) {
            switch (e.getID()) {
                case 200: {
                    listener.windowOpened(e);
                    break;
                }
                case 201: {
                    listener.windowClosing(e);
                    break;
                }
                case 202: {
                    listener.windowClosed(e);
                    break;
                }
                case 203: {
                    listener.windowIconified(e);
                    break;
                }
                case 204: {
                    listener.windowDeiconified(e);
                    break;
                }
                case 205: {
                    listener.windowActivated(e);
                    break;
                }
                case 206: {
                    listener.windowDeactivated(e);
                    break;
                }
            }
        }
    }

    protected void processWindowFocusEvent(WindowEvent e) {
        WindowFocusListener listener = this.windowFocusListener;
        if (listener != null) {
            switch (e.getID()) {
                case 207: {
                    listener.windowGainedFocus(e);
                    break;
                }
                case 208: {
                    listener.windowLostFocus(e);
                    break;
                }
            }
        }
    }

    protected void processWindowStateEvent(WindowEvent e) {
        WindowStateListener listener = this.windowStateListener;
        if (listener != null) {
            switch (e.getID()) {
                case 209: {
                    listener.windowStateChanged(e);
                    break;
                }
            }
        }
    }

    @Override
    void preProcessKeyEvent(KeyEvent e) {
        if (DebugSettings.getInstance().getBoolean("on", false) && e.isActionKey() && e.getKeyCode() == 112 && e.isControlDown() && e.isShiftDown() && e.getID() == 401) {
            this.list(System.out, 0);
        }
    }

    @Override
    void postProcessKeyEvent(KeyEvent e) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void setAlwaysOnTop(boolean alwaysOnTop) throws SecurityException {
        boolean oldAlwaysOnTop;
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(AWTPermissions.SET_WINDOW_ALWAYS_ON_TOP_PERMISSION);
        }
        Window window = this;
        synchronized (window) {
            oldAlwaysOnTop = this.alwaysOnTop;
            this.alwaysOnTop = alwaysOnTop;
        }
        if (oldAlwaysOnTop != alwaysOnTop) {
            if (this.isAlwaysOnTopSupported()) {
                WindowPeer peer = (WindowPeer)this.peer;
                Object object = this.getTreeLock();
                synchronized (object) {
                    if (peer != null) {
                        peer.updateAlwaysOnTopState();
                    }
                }
            }
            this.firePropertyChange("alwaysOnTop", oldAlwaysOnTop, alwaysOnTop);
        }
        this.setOwnedWindowsAlwaysOnTop(alwaysOnTop);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setOwnedWindowsAlwaysOnTop(boolean alwaysOnTop) {
        Object[] objectArray = this.ownedWindowList;
        synchronized (this.ownedWindowList) {
            Object[] ownedWindowArray = new WeakReference[this.ownedWindowList.size()];
            this.ownedWindowList.copyInto(ownedWindowArray);
            // ** MonitorExit[var3_2] (shouldn't be in output)
            for (Object ref : ownedWindowArray) {
                Window window = (Window)((Reference)ref).get();
                if (window == null) continue;
                try {
                    window.setAlwaysOnTop(alwaysOnTop);
                }
                catch (SecurityException securityException) {
                    // empty catch block
                }
            }
            return;
        }
    }

    public boolean isAlwaysOnTopSupported() {
        return Toolkit.getDefaultToolkit().isAlwaysOnTopSupported();
    }

    public final boolean isAlwaysOnTop() {
        return this.alwaysOnTop;
    }

    public Component getFocusOwner() {
        return this.isFocused() ? KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() : null;
    }

    public Component getMostRecentFocusOwner() {
        if (this.isFocused()) {
            return this.getFocusOwner();
        }
        Component mostRecent = KeyboardFocusManager.getMostRecentFocusOwner(this);
        if (mostRecent != null) {
            return mostRecent;
        }
        return this.isFocusableWindow() ? this.getFocusTraversalPolicy().getInitialComponent(this) : null;
    }

    public boolean isActive() {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() == this;
    }

    public boolean isFocused() {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getGlobalFocusedWindow() == this;
    }

    @Override
    public Set<AWTKeyStroke> getFocusTraversalKeys(int id) {
        Set keystrokes;
        if (id < 0 || id >= 4) {
            throw new IllegalArgumentException("invalid focus traversal key identifier");
        }
        Set set = keystrokes = this.focusTraversalKeys != null ? this.focusTraversalKeys[id] : null;
        if (keystrokes != null) {
            return keystrokes;
        }
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(id);
    }

    @Override
    public final void setFocusCycleRoot(boolean focusCycleRoot) {
    }

    @Override
    public final boolean isFocusCycleRoot() {
        return true;
    }

    @Override
    public final Container getFocusCycleRootAncestor() {
        return null;
    }

    public final boolean isFocusableWindow() {
        if (!this.getFocusableWindowState()) {
            return false;
        }
        if (this instanceof Frame || this instanceof Dialog) {
            return true;
        }
        if (this.getFocusTraversalPolicy().getDefaultComponent(this) == null) {
            return false;
        }
        for (Window owner = this.getOwner(); owner != null; owner = owner.getOwner()) {
            if (!(owner instanceof Frame) && !(owner instanceof Dialog)) continue;
            return owner.isShowing();
        }
        return false;
    }

    public boolean getFocusableWindowState() {
        return this.focusableWindowState;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setFocusableWindowState(boolean focusableWindowState) {
        boolean oldFocusableWindowState;
        Window window = this;
        synchronized (window) {
            oldFocusableWindowState = this.focusableWindowState;
            this.focusableWindowState = focusableWindowState;
        }
        WindowPeer peer = (WindowPeer)this.peer;
        if (peer != null) {
            peer.updateFocusableWindowState();
        }
        this.firePropertyChange("focusableWindowState", oldFocusableWindowState, focusableWindowState);
        if (oldFocusableWindowState && !focusableWindowState && this.isFocused()) {
            for (Window owner = this.getOwner(); owner != null; owner = owner.getOwner()) {
                Component toFocus = KeyboardFocusManager.getMostRecentFocusOwner(owner);
                if (toFocus == null || !toFocus.requestFocus(false, FocusEvent.Cause.ACTIVATION)) continue;
                return;
            }
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwnerPriv();
        }
    }

    public void setAutoRequestFocus(boolean autoRequestFocus) {
        this.autoRequestFocus = autoRequestFocus;
    }

    public boolean isAutoRequestFocus() {
        return this.autoRequestFocus;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        super.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public boolean isValidateRoot() {
        return true;
    }

    @Override
    void dispatchEventImpl(AWTEvent e) {
        if (e.getID() == 101) {
            this.invalidate();
            this.validate();
        }
        super.dispatchEventImpl(e);
    }

    @Override
    @Deprecated
    public boolean postEvent(Event e) {
        if (this.handleEvent(e)) {
            e.consume();
            return true;
        }
        return false;
    }

    @Override
    public boolean isShowing() {
        return this.visible;
    }

    boolean isDisposing() {
        return this.disposing;
    }

    @Deprecated
    public void applyResourceBundle(ResourceBundle rb) {
        this.applyComponentOrientation(ComponentOrientation.getOrientation(rb));
    }

    @Deprecated
    public void applyResourceBundle(String rbName) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        this.applyResourceBundle(ResourceBundle.getBundle(rbName, cl.getUnnamedModule()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void addOwnedWindow(WeakReference<Window> weakWindow) {
        if (weakWindow != null) {
            Vector<WeakReference<Window>> vector = this.ownedWindowList;
            synchronized (vector) {
                if (!this.ownedWindowList.contains(weakWindow)) {
                    this.ownedWindowList.addElement(weakWindow);
                }
            }
        }
    }

    void removeOwnedWindow(WeakReference<Window> weakWindow) {
        if (weakWindow != null) {
            this.ownedWindowList.removeElement(weakWindow);
        }
    }

    void connectOwnedWindow(Window child) {
        child.parent = this;
        this.addOwnedWindow(child.weakThis);
        child.disposerRecord.updateOwner();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addToWindowList() {
        Class<Window> clazz = Window.class;
        synchronized (Window.class) {
            Vector<WeakReference<Window>> windowList = (Vector<WeakReference<Window>>)this.appContext.get(Window.class);
            if (windowList == null) {
                windowList = new Vector<WeakReference<Window>>();
                this.appContext.put(Window.class, windowList);
            }
            windowList.add(this.weakThis);
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void removeFromWindowList(AppContext context, WeakReference<Window> weakThis) {
        Class<Window> clazz = Window.class;
        synchronized (Window.class) {
            Vector windowList = (Vector)context.get(Window.class);
            if (windowList != null) {
                windowList.remove(weakThis);
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    private void removeFromWindowList() {
        Window.removeFromWindowList(this.appContext, this.weakThis);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type should not be null.");
        }
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.isDisplayable()) {
                throw new IllegalComponentStateException("The window is displayable.");
            }
            Object object2 = this.getObjectLock();
            synchronized (object2) {
                this.type = type;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Type getType() {
        Object object = this.getObjectLock();
        synchronized (object) {
            return this.type;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        Object object = this;
        synchronized (object) {
            this.focusMgr = new FocusManager();
            this.focusMgr.focusRoot = this;
            this.focusMgr.focusOwner = this.getMostRecentFocusOwner();
            s.defaultWriteObject();
            this.focusMgr = null;
            AWTEventMulticaster.save(s, "windowL", this.windowListener);
            AWTEventMulticaster.save(s, "windowFocusL", this.windowFocusListener);
            AWTEventMulticaster.save(s, "windowStateL", this.windowStateListener);
        }
        s.writeObject(null);
        object = this.ownedWindowList;
        synchronized (object) {
            for (int i = 0; i < this.ownedWindowList.size(); ++i) {
                Window child = (Window)this.ownedWindowList.elementAt(i).get();
                if (child == null) continue;
                s.writeObject("ownedL");
                s.writeObject(child);
            }
        }
        s.writeObject(null);
        if (this.icons != null) {
            for (Image i : this.icons) {
                if (!(i instanceof Serializable)) continue;
                s.writeObject(i);
            }
        }
        s.writeObject(null);
    }

    private void initDeserializedWindow() {
        this.setWarningString();
        this.inputContextLock = new Object();
        this.visible = false;
        this.weakThis = new WeakReference<Window>(this);
        this.anchor = new Object();
        this.disposerRecord = new WindowDisposerRecord(this.appContext, this);
        Disposer.addRecord(this.anchor, this.disposerRecord);
        this.addToWindowList();
        this.initGC(null);
        this.ownedWindowList = new Vector();
    }

    private void deserializeResources(ObjectInputStream s) throws ClassNotFoundException, IOException, HeadlessException {
        String key;
        Object keyOrNull;
        if (this.windowSerializedDataVersion < 2) {
            if (this.focusMgr != null && this.focusMgr.focusOwner != null) {
                KeyboardFocusManager.setMostRecentFocusOwner(this, this.focusMgr.focusOwner);
            }
            this.focusableWindowState = true;
        }
        while (null != (keyOrNull = s.readObject())) {
            key = ((String)keyOrNull).intern();
            if ("windowL" == key) {
                this.addWindowListener((WindowListener)s.readObject());
                continue;
            }
            if ("windowFocusL" == key) {
                this.addWindowFocusListener((WindowFocusListener)s.readObject());
                continue;
            }
            if ("windowStateL" == key) {
                this.addWindowStateListener((WindowStateListener)s.readObject());
                continue;
            }
            s.readObject();
        }
        try {
            while (null != (keyOrNull = s.readObject())) {
                key = ((String)keyOrNull).intern();
                if ("ownedL" == key) {
                    this.connectOwnedWindow((Window)s.readObject());
                    continue;
                }
                s.readObject();
            }
            Object obj = s.readObject();
            this.icons = new ArrayList<Image>();
            while (obj != null) {
                if (obj instanceof Image) {
                    this.icons.add((Image)obj);
                }
                obj = s.readObject();
            }
        }
        catch (OptionalDataException optionalDataException) {
            // empty catch block
        }
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException, HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.initDeserializedWindow();
        ObjectInputStream.GetField f = s.readFields();
        this.syncLWRequests = f.get("syncLWRequests", systemSyncLWRequests);
        this.state = f.get("state", 0);
        this.focusableWindowState = f.get("focusableWindowState", true);
        this.windowSerializedDataVersion = f.get("windowSerializedDataVersion", 1);
        this.locationByPlatform = f.get("locationByPlatform", locationByPlatformProp);
        this.focusMgr = (FocusManager)f.get("focusMgr", null);
        Dialog.ModalExclusionType et = (Dialog.ModalExclusionType)((Object)f.get("modalExclusionType", (Object)Dialog.ModalExclusionType.NO_EXCLUDE));
        this.setModalExclusionType(et);
        boolean aot = f.get("alwaysOnTop", false);
        if (aot) {
            this.setAlwaysOnTop(aot);
        }
        this.shape = (Shape)f.get("shape", null);
        this.opacity = Float.valueOf(f.get("opacity", 1.0f)).floatValue();
        this.securityWarningWidth = 0;
        this.securityWarningHeight = 0;
        this.deserializeResources(s);
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleAWTWindow();
        }
        return this.accessibleContext;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void setGraphicsConfiguration(GraphicsConfiguration gc) {
        if (gc == null) {
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        }
        Object object = this.getTreeLock();
        synchronized (object) {
            super.setGraphicsConfiguration(gc);
            if (log.isLoggable(PlatformLogger.Level.FINER)) {
                log.finer("+ Window.setGraphicsConfiguration(): new GC is \n+ " + String.valueOf(this.getGraphicsConfiguration_NoClientCode()) + "\n+ this is " + String.valueOf(this));
            }
        }
    }

    public void setLocationRelativeTo(Component c) {
        int dx = 0;
        int dy = 0;
        GraphicsConfiguration gc = this.getGraphicsConfiguration_NoClientCode();
        Rectangle gcBounds = gc.getBounds();
        Dimension windowSize = this.getSize();
        Window componentWindow = SunToolkit.getContainingWindow(c);
        if (c == null || componentWindow == null) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
            gcBounds = gc.getBounds();
            Point centerPoint = ge.getCenterPoint();
            dx = centerPoint.x - windowSize.width / 2;
            dy = centerPoint.y - windowSize.height / 2;
        } else if (!c.isShowing()) {
            gc = componentWindow.getGraphicsConfiguration();
            gcBounds = gc.getBounds();
            dx = gcBounds.x + (gcBounds.width - windowSize.width) / 2;
            dy = gcBounds.y + (gcBounds.height - windowSize.height) / 2;
        } else {
            gc = componentWindow.getGraphicsConfiguration();
            gcBounds = gc.getBounds();
            Dimension compSize = c.getSize();
            Point compLocation = c.getLocationOnScreen();
            dx = compLocation.x + (compSize.width - windowSize.width) / 2;
            dy = compLocation.y + (compSize.height - windowSize.height) / 2;
            if (dy + windowSize.height > gcBounds.y + gcBounds.height) {
                dy = gcBounds.y + gcBounds.height - windowSize.height;
                dx = compLocation.x - gcBounds.x + compSize.width / 2 < gcBounds.width / 2 ? compLocation.x + compSize.width : compLocation.x - windowSize.width;
            }
        }
        if (dy + windowSize.height > gcBounds.y + gcBounds.height) {
            dy = gcBounds.y + gcBounds.height - windowSize.height;
        }
        if (dy < gcBounds.y) {
            dy = gcBounds.y;
        }
        if (dx + windowSize.width > gcBounds.x + gcBounds.width) {
            dx = gcBounds.x + gcBounds.width - windowSize.width;
        }
        if (dx < gcBounds.x) {
            dx = gcBounds.x;
        }
        this.setLocation(dx, dy);
    }

    void deliverMouseWheelToAncestor(MouseWheelEvent e) {
    }

    @Override
    boolean dispatchMouseWheelToAncestor(MouseWheelEvent e) {
        return false;
    }

    @Override
    public void createBufferStrategy(int numBuffers) {
        super.createBufferStrategy(numBuffers);
    }

    @Override
    public void createBufferStrategy(int numBuffers, BufferCapabilities caps) throws AWTException {
        super.createBufferStrategy(numBuffers, caps);
    }

    @Override
    public BufferStrategy getBufferStrategy() {
        return super.getBufferStrategy();
    }

    Component getTemporaryLostComponent() {
        return this.temporaryLostComponent;
    }

    Component setTemporaryLostComponent(Component component) {
        Component previousComp = this.temporaryLostComponent;
        this.temporaryLostComponent = component == null || component.canBeFocusOwner() ? component : null;
        return previousComp;
    }

    @Override
    boolean canContainFocusOwner(Component focusOwnerCandidate) {
        return super.canContainFocusOwner(focusOwnerCandidate) && this.isFocusableWindow();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setLocationByPlatform(boolean locationByPlatform) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (locationByPlatform && this.isShowing()) {
                throw new IllegalComponentStateException("The window is showing on screen.");
            }
            this.locationByPlatform = locationByPlatform;
        }
    }

    public boolean isLocationByPlatform() {
        return this.locationByPlatform;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.getBoundsOp() == 1 || this.getBoundsOp() == 3) {
                this.locationByPlatform = false;
            }
            super.setBounds(x, y, width, height);
        }
    }

    @Override
    public void setBounds(Rectangle r) {
        this.setBounds(r.x, r.y, r.width, r.height);
    }

    @Override
    boolean isRecursivelyVisible() {
        return this.visible;
    }

    public float getOpacity() {
        return this.opacity;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setOpacity(float opacity) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (opacity < 0.0f || opacity > 1.0f) {
                throw new IllegalArgumentException("The value of opacity should be in the range [0.0f .. 1.0f].");
            }
            if (opacity < 1.0f) {
                GraphicsConfiguration gc = this.getGraphicsConfiguration();
                GraphicsDevice gd = gc.getDevice();
                if (gc.getDevice().getFullScreenWindow() == this) {
                    throw new IllegalComponentStateException("Setting opacity for full-screen window is not supported.");
                }
                if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
                    throw new UnsupportedOperationException("TRANSLUCENT translucency is not supported.");
                }
            }
            this.opacity = opacity;
            WindowPeer peer = (WindowPeer)this.peer;
            if (peer != null) {
                peer.setOpacity(opacity);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Shape getShape() {
        Object object = this.getTreeLock();
        synchronized (object) {
            return this.shape == null ? null : new Path2D.Float(this.shape);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setShape(Shape shape) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (shape != null) {
                GraphicsConfiguration gc = this.getGraphicsConfiguration();
                GraphicsDevice gd = gc.getDevice();
                if (gc.getDevice().getFullScreenWindow() == this) {
                    throw new IllegalComponentStateException("Setting shape for full-screen window is not supported.");
                }
                if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
                    throw new UnsupportedOperationException("PERPIXEL_TRANSPARENT translucency is not supported.");
                }
            }
            this.shape = shape == null ? null : new Path2D.Float(shape);
            WindowPeer peer = (WindowPeer)this.peer;
            if (peer != null) {
                peer.applyShape(shape == null ? null : Region.getInstance(shape, null));
            }
        }
    }

    @Override
    public Color getBackground() {
        return super.getBackground();
    }

    @Override
    public void setBackground(Color bgColor) {
        int alpha;
        Color oldBg = this.getBackground();
        super.setBackground(bgColor);
        if (oldBg != null && oldBg.equals(bgColor)) {
            return;
        }
        int oldAlpha = oldBg != null ? oldBg.getAlpha() : 255;
        int n = alpha = bgColor != null ? bgColor.getAlpha() : 255;
        if (oldAlpha == 255 && alpha < 255) {
            GraphicsConfiguration gc = this.getGraphicsConfiguration();
            GraphicsDevice gd = gc.getDevice();
            if (gc.getDevice().getFullScreenWindow() == this) {
                throw new IllegalComponentStateException("Making full-screen window non opaque is not supported.");
            }
            if (!gc.isTranslucencyCapable()) {
                GraphicsConfiguration capableGC = gd.getTranslucencyCapableGC();
                if (capableGC == null) {
                    throw new UnsupportedOperationException("PERPIXEL_TRANSLUCENT translucency is not supported");
                }
                this.setGraphicsConfiguration(capableGC);
            }
            Window.setLayersOpaque(this, false);
        } else if (oldAlpha < 255 && alpha == 255) {
            Window.setLayersOpaque(this, true);
        }
        WindowPeer peer = (WindowPeer)this.peer;
        if (peer != null) {
            peer.setOpaque(alpha == 255);
        }
    }

    @Override
    public boolean isOpaque() {
        Color bg = this.getBackground();
        return bg != null ? bg.getAlpha() == 255 : true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateWindow() {
        Object object = this.getTreeLock();
        synchronized (object) {
            WindowPeer peer = (WindowPeer)this.peer;
            if (peer != null) {
                peer.updateWindow();
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        if (!this.isOpaque()) {
            Graphics gg = g.create();
            try {
                if (gg instanceof Graphics2D) {
                    gg.setColor(this.getBackground());
                    ((Graphics2D)gg).setComposite(AlphaComposite.getInstance(2));
                    gg.fillRect(0, 0, this.getWidth(), this.getHeight());
                }
            }
            finally {
                gg.dispose();
            }
        }
        super.paint(g);
    }

    private static void setLayersOpaque(Component component, boolean isOpaque) {
        if (SunToolkit.isInstanceOf(component, "javax.swing.RootPaneContainer")) {
            RootPaneContainer rpc = (RootPaneContainer)((Object)component);
            JRootPane root = rpc.getRootPane();
            JLayeredPane lp = root.getLayeredPane();
            Container c = root.getContentPane();
            JComponent content = c instanceof JComponent ? (JComponent)c : null;
            lp.setOpaque(isOpaque);
            root.setOpaque(isOpaque);
            if (content != null) {
                Component child;
                content.setOpaque(isOpaque);
                int numChildren = content.getComponentCount();
                if (numChildren > 0 && (child = content.getComponent(0)) instanceof RootPaneContainer) {
                    Window.setLayersOpaque(child, isOpaque);
                }
            }
        }
    }

    @Override
    final Container getContainer() {
        return null;
    }

    @Override
    final void applyCompoundShape(Region shape) {
    }

    @Override
    final void applyCurrentShape() {
    }

    @Override
    final void mixOnReshaping() {
    }

    @Override
    final Point getLocationOnWindow() {
        return new Point(0, 0);
    }

    private static double limit(double value, double min, double max) {
        value = Math.max(value, min);
        value = Math.min(value, max);
        return value;
    }

    private Point2D calculateSecurityWarningPosition(double x, double y, double w, double h) {
        double wx = x + w * 1.0 + 2.0;
        double wy = y + h * 0.0 + 0.0;
        wx = Window.limit(wx, x - (double)this.securityWarningWidth - 2.0, x + w + 2.0);
        wy = Window.limit(wy, y - (double)this.securityWarningHeight - 2.0, y + h + 2.0);
        GraphicsConfiguration graphicsConfig = this.getGraphicsConfiguration_NoClientCode();
        Rectangle screenBounds = graphicsConfig.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfig);
        wx = Window.limit(wx, screenBounds.x + screenInsets.left, screenBounds.x + screenBounds.width - screenInsets.right - this.securityWarningWidth);
        wy = Window.limit(wy, screenBounds.y + screenInsets.top, screenBounds.y + screenBounds.height - screenInsets.bottom - this.securityWarningHeight);
        return new Point2D.Double(wx, wy);
    }

    @Override
    void updateZOrder() {
    }

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            Window.initIDs();
        }
        String s = AccessController.doPrivileged(new GetPropertyAction("java.awt.syncLWRequests"));
        systemSyncLWRequests = "true".equals(s);
        String s2 = AccessController.doPrivileged(new GetPropertyAction("java.awt.Window.locationByPlatform"));
        locationByPlatformProp = "true".equals(s2);
        beforeFirstWindowShown = new AtomicBoolean(true);
        AWTAccessor.setWindowAccessor(new AWTAccessor.WindowAccessor(){

            @Override
            public void updateWindow(Window window) {
                window.updateWindow();
            }

            @Override
            public void setSecurityWarningSize(Window window, int width, int height) {
                window.securityWarningWidth = width;
                window.securityWarningHeight = height;
            }

            @Override
            public Point2D calculateSecurityWarningPosition(Window window, double x, double y, double w, double h) {
                return window.calculateSecurityWarningPosition(x, y, w, h);
            }

            @Override
            public void setLWRequestStatus(Window changed, boolean status) {
                changed.syncLWRequests = status;
            }

            @Override
            public boolean isAutoRequestFocus(Window w) {
                return w.autoRequestFocus;
            }

            @Override
            public boolean isTrayIconWindow(Window w) {
                return w.isTrayIconWindow;
            }

            @Override
            public void setTrayIconWindow(Window w, boolean isTrayIconWindow) {
                w.isTrayIconWindow = isTrayIconWindow;
            }

            @Override
            public Window[] getOwnedWindows(Window w) {
                return w.getOwnedWindows_NoClientCode();
            }
        });
    }

    static class WindowDisposerRecord
    implements DisposerRecord {
        WeakReference<Window> owner;
        final WeakReference<Window> weakThis;
        final WeakReference<AppContext> context;

        WindowDisposerRecord(AppContext context, Window victim) {
            this.weakThis = victim.weakThis;
            this.context = new WeakReference<AppContext>(context);
        }

        public void updateOwner() {
            Window victim = (Window)this.weakThis.get();
            this.owner = victim == null ? null : new WeakReference<Window>(victim.getOwner());
        }

        @Override
        public void dispose() {
            AppContext ac;
            Window parent;
            if (this.owner != null && (parent = (Window)this.owner.get()) != null) {
                parent.removeOwnedWindow(this.weakThis);
            }
            if (null != (ac = (AppContext)this.context.get())) {
                Window.removeFromWindowList(ac, this.weakThis);
            }
        }
    }

    public static enum Type {
        NORMAL,
        UTILITY,
        POPUP;

    }

    protected class AccessibleAWTWindow
    extends Container.AccessibleAWTContainer {
        private static final long serialVersionUID = 4215068635060671780L;

        protected AccessibleAWTWindow() {
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.WINDOW;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (Window.this.getFocusOwner() != null) {
                states.add(AccessibleState.ACTIVE);
            }
            return states;
        }
    }
}

