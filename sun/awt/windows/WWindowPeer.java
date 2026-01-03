/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.DataBufferInt;
import java.awt.peer.WindowPeer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.DisplayChangedListener;
import sun.awt.SunToolkit;
import sun.awt.TimedWindowEvent;
import sun.awt.Win32GraphicsConfig;
import sun.awt.Win32GraphicsDevice;
import sun.awt.Win32GraphicsEnvironment;
import sun.awt.windows.TranslucentWindowPainter;
import sun.awt.windows.WComponentPeer;
import sun.awt.windows.WFileDialogPeer;
import sun.awt.windows.WPanelPeer;
import sun.awt.windows.WPrintDialogPeer;
import sun.awt.windows.WToolkit;
import sun.java2d.SunGraphicsEnvironment;
import sun.java2d.pipe.Region;
import sun.util.logging.PlatformLogger;

public class WWindowPeer
extends WPanelPeer
implements WindowPeer,
DisplayChangedListener {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.windows.WWindowPeer");
    private static final PlatformLogger screenLog = PlatformLogger.getLogger("sun.awt.windows.screen.WWindowPeer");
    private WWindowPeer modalBlocker = null;
    private boolean isOpaque;
    private TranslucentWindowPainter painter;
    private static final StringBuffer ACTIVE_WINDOWS_KEY = new StringBuffer("active_windows_list");
    private static PropertyChangeListener activeWindowListener = new ActiveWindowListener();
    private static final PropertyChangeListener guiDisposedListener = new GuiDisposedListener();
    private WindowListener windowListener;
    private volatile Window.Type windowType = Window.Type.NORMAL;
    private float opacity = 1.0f;

    private static native void initIDs();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void disposeImpl() {
        AppContext appContext;
        AppContext appContext2 = appContext = SunToolkit.targetToAppContext(this.target);
        synchronized (appContext2) {
            List l = (List)appContext.get(ACTIVE_WINDOWS_KEY);
            if (l != null) {
                l.remove(this);
            }
        }
        GraphicsConfiguration gc = this.getGraphicsConfiguration();
        ((Win32GraphicsDevice)gc.getDevice()).removeDisplayChangedListener(this);
        Object object = this.getStateLock();
        synchronized (object) {
            TranslucentWindowPainter currentPainter = this.painter;
            if (currentPainter != null) {
                currentPainter.flush();
            }
        }
        super.disposeImpl();
    }

    @Override
    public void toFront() {
        this.updateFocusableWindowState();
        this._toFront();
    }

    private native void _toFront();

    @Override
    public native void toBack();

    private native void setAlwaysOnTopNative(boolean var1);

    public void setAlwaysOnTop(boolean value) {
        if (value && ((Window)this.target).isVisible() || !value) {
            this.setAlwaysOnTopNative(value);
        }
    }

    @Override
    public void updateAlwaysOnTopState() {
        this.setAlwaysOnTop(((Window)this.target).isAlwaysOnTop());
    }

    @Override
    public void updateFocusableWindowState() {
        this.setFocusableWindow(((Window)this.target).isFocusableWindow());
    }

    native void setFocusableWindow(boolean var1);

    public void setTitle(String title) {
        if (title == null) {
            title = "";
        }
        this._setTitle(title);
    }

    private native void _setTitle(String var1);

    public void setResizable(boolean resizable) {
        this._setResizable(resizable);
    }

    private native void _setResizable(boolean var1);

    WWindowPeer(Window target) {
        super(target);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void initialize() {
        float opacity;
        super.initialize();
        this.updateInsets(this.insets_);
        if (!((Window)this.target).isFontSet()) {
            ((Window)this.target).setFont(defaultFont);
            this.setFont(defaultFont);
        }
        if (!((Window)this.target).isForegroundSet()) {
            ((Window)this.target).setForeground(SystemColor.windowText);
        }
        if (!((Window)this.target).isBackgroundSet()) {
            ((Window)this.target).setBackground(SystemColor.window);
        }
        GraphicsConfiguration gc = this.getGraphicsConfiguration();
        Win32GraphicsDevice gd = (Win32GraphicsDevice)gc.getDevice();
        gd.addDisplayChangedListener(this);
        WWindowPeer.initActiveWindowsTracking((Window)this.target);
        this.updateIconImages();
        Shape shape = ((Window)this.target).getShape();
        if (shape != null) {
            this.applyShape(Region.getInstance(shape, null));
        }
        if ((opacity = ((Window)this.target).getOpacity()) < 1.0f) {
            this.setOpacity(opacity);
        }
        Object object = this.getStateLock();
        synchronized (object) {
            this.isOpaque = true;
            this.setOpaque(((Window)this.target).isOpaque());
        }
    }

    native void createAwtWindow(WComponentPeer var1);

    void preCreate(WComponentPeer parent) {
        this.windowType = ((Window)this.target).getType();
    }

    @Override
    void create(WComponentPeer parent) {
        this.preCreate(parent);
        this.createAwtWindow(parent);
    }

    @Override
    final WComponentPeer getNativeParent() {
        Window owner = ((Window)this.target).getOwner();
        return (WComponentPeer)WToolkit.targetToPeer(owner);
    }

    protected void realShow() {
        super.show();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void show() {
        this.updateFocusableWindowState();
        boolean alwaysOnTop = ((Window)this.target).isAlwaysOnTop();
        this.updateGC();
        this.realShow();
        this.updateMinimumSize();
        if (((Window)this.target).isAlwaysOnTopSupported() && alwaysOnTop) {
            this.setAlwaysOnTop(alwaysOnTop);
        }
        Object object = this.getStateLock();
        synchronized (object) {
            if (!this.isOpaque) {
                this.updateWindow(true);
            }
        }
        WComponentPeer owner = this.getNativeParent();
        if (owner != null && owner.isLightweightFramePeer()) {
            Rectangle b = this.getBounds();
            this.handleExpose(0, 0, b.width, b.height);
        }
    }

    @Override
    final void syncBounds() {
    }

    native void updateInsets(Insets var1);

    static native int getSysMinWidth();

    static native int getSysMinHeight();

    static native int getSysIconWidth();

    static native int getSysIconHeight();

    static native int getSysSmIconWidth();

    static native int getSysSmIconHeight();

    native void setIconImagesData(int[] var1, int var2, int var3, int[] var4, int var5, int var6);

    synchronized native void reshapeFrame(int var1, int var2, int var3, int var4);

    native Dimension getNativeWindowSize();

    public Dimension getScaledWindowSize() {
        return this.getNativeWindowSize();
    }

    public boolean requestWindowFocus(FocusEvent.Cause cause) {
        if (!this.focusAllowedFor()) {
            return false;
        }
        return this.requestWindowFocus(cause == FocusEvent.Cause.MOUSE_EVENT);
    }

    private native boolean requestWindowFocus(boolean var1);

    public boolean focusAllowedFor() {
        Window window = (Window)this.target;
        if (!(window.isVisible() && window.isEnabled() && window.isFocusableWindow())) {
            return false;
        }
        return !this.isModalBlocked();
    }

    @Override
    void hide() {
        WindowListener listener = this.windowListener;
        if (listener != null) {
            listener.windowClosing(new WindowEvent((Window)this.target, 201));
        }
        super.hide();
    }

    @Override
    void preprocessPostEvent(AWTEvent event) {
        WindowListener listener;
        if (event instanceof WindowEvent && (listener = this.windowListener) != null) {
            switch (event.getID()) {
                case 201: {
                    listener.windowClosing((WindowEvent)event);
                    break;
                }
                case 203: {
                    listener.windowIconified((WindowEvent)event);
                }
            }
        }
    }

    private void notifyWindowStateChanged(int oldState, int newState) {
        int changed = oldState ^ newState;
        if (changed == 0) {
            return;
        }
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("Reporting state change %x -> %x", oldState, newState);
        }
        if (this.target instanceof Frame) {
            AWTAccessor.getFrameAccessor().setExtendedState((Frame)this.target, newState);
        }
        if ((changed & 1) > 0) {
            if ((newState & 1) > 0) {
                this.postEvent(new TimedWindowEvent((Window)this.target, 203, null, 0, 0, System.currentTimeMillis()));
            } else {
                this.postEvent(new TimedWindowEvent((Window)this.target, 204, null, 0, 0, System.currentTimeMillis()));
            }
        }
        this.postEvent(new TimedWindowEvent((Window)this.target, 209, null, oldState, newState, System.currentTimeMillis()));
    }

    synchronized void addWindowListener(WindowListener l) {
        this.windowListener = AWTEventMulticaster.add(this.windowListener, l);
    }

    synchronized void removeWindowListener(WindowListener l) {
        this.windowListener = AWTEventMulticaster.remove(this.windowListener, l);
    }

    @Override
    public void updateMinimumSize() {
        Dimension minimumSize = null;
        if (((Component)this.target).isMinimumSizeSet()) {
            minimumSize = ((Component)this.target).getMinimumSize();
        }
        if (minimumSize != null) {
            Dimension sysMin = SunGraphicsEnvironment.toUserSpace(this.getGraphicsConfiguration(), WWindowPeer.getSysMinWidth(), WWindowPeer.getSysMinHeight());
            this.setMinSize(Math.max(minimumSize.width, sysMin.width), Math.max(minimumSize.height, sysMin.height));
        } else {
            this.setMinSize(0, 0);
        }
    }

    @Override
    public void updateIconImages() {
        List<Image> imageList = ((Window)this.target).getIconImages();
        if (imageList == null || imageList.size() == 0) {
            this.setIconImagesData(null, 0, 0, null, 0, 0);
        } else {
            int w = WWindowPeer.getSysIconWidth();
            int h = WWindowPeer.getSysIconHeight();
            int smw = WWindowPeer.getSysSmIconWidth();
            int smh = WWindowPeer.getSysSmIconHeight();
            AffineTransform tx = this.getGraphicsConfiguration().getDefaultTransform();
            w = Region.clipScale(w, tx.getScaleX());
            h = Region.clipScale(h, tx.getScaleY());
            smw = Region.clipScale(smw, tx.getScaleX());
            smh = Region.clipScale(smh, tx.getScaleY());
            DataBufferInt iconData = SunToolkit.getScaledIconData(imageList, w, h);
            DataBufferInt iconSmData = SunToolkit.getScaledIconData(imageList, smw, smh);
            if (iconData != null && iconSmData != null) {
                this.setIconImagesData(iconData.getData(), w, h, iconSmData.getData(), smw, smh);
            } else {
                this.setIconImagesData(null, 0, 0, null, 0, 0);
            }
        }
    }

    native void setMinSize(int var1, int var2);

    public boolean isModalBlocked() {
        return this.modalBlocker != null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setModalBlocked(Dialog dialog, boolean blocked) {
        Object object = ((Component)this.getTarget()).getTreeLock();
        synchronized (object) {
            WWindowPeer blockerPeer = (WWindowPeer)AWTAccessor.getComponentAccessor().getPeer(dialog);
            if (blocked) {
                this.modalBlocker = blockerPeer;
                if (blockerPeer instanceof WFileDialogPeer) {
                    ((WFileDialogPeer)blockerPeer).blockWindow(this);
                } else if (blockerPeer instanceof WPrintDialogPeer) {
                    ((WPrintDialogPeer)blockerPeer).blockWindow(this);
                } else {
                    this.modalDisable(dialog, blockerPeer.getHWnd());
                }
            } else {
                this.modalBlocker = null;
                if (blockerPeer instanceof WFileDialogPeer) {
                    ((WFileDialogPeer)blockerPeer).unblockWindow(this);
                } else if (blockerPeer instanceof WPrintDialogPeer) {
                    ((WPrintDialogPeer)blockerPeer).unblockWindow(this);
                } else {
                    this.modalEnable(dialog);
                }
            }
        }
    }

    native void modalDisable(Dialog var1, long var2);

    native void modalEnable(Dialog var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static long[] getActiveWindowHandles(Component target) {
        AppContext appContext = SunToolkit.targetToAppContext(target);
        if (appContext == null) {
            return null;
        }
        AppContext appContext2 = appContext;
        synchronized (appContext2) {
            List l = (List)appContext.get(ACTIVE_WINDOWS_KEY);
            if (l == null) {
                return null;
            }
            long[] result = new long[l.size()];
            for (int j = 0; j < l.size(); ++j) {
                result[j] = ((WWindowPeer)l.get(j)).getHWnd();
            }
            return result;
        }
    }

    void draggedToNewScreen() {
        this.displayChanged();
    }

    public void updateGC() {
        int scrn = this.getScreenImOn();
        if (screenLog.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("Screen number: " + scrn);
        }
        Win32GraphicsDevice oldDev = this.winGraphicsConfig.getDevice();
        GraphicsDevice[] devs = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        Win32GraphicsDevice newDev = scrn >= devs.length ? (Win32GraphicsDevice)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice() : (Win32GraphicsDevice)devs[scrn];
        this.winGraphicsConfig = (Win32GraphicsConfig)newDev.getDefaultConfiguration();
        if (screenLog.isLoggable(PlatformLogger.Level.FINE) && this.winGraphicsConfig == null) {
            screenLog.fine("Assertion (winGraphicsConfig != null) failed");
        }
        if (oldDev != newDev) {
            oldDev.removeDisplayChangedListener(this);
            newDev.addDisplayChangedListener(this);
        }
        if (((Window)this.target).isVisible()) {
            this.updateIconImages();
        }
        AWTAccessor.getComponentAccessor().setGraphicsConfiguration((Component)this.target, this.winGraphicsConfig);
    }

    @Override
    public void displayChanged() {
        SunToolkit.executeOnEventHandlerThread(this.target, this::updateGC);
    }

    @Override
    public void paletteChanged() {
    }

    private native int getScreenImOn();

    public final native void setFullScreenExclusiveModeState(boolean var1);

    public void grab() {
        this.nativeGrab();
    }

    public void ungrab() {
        this.nativeUngrab();
    }

    private native void nativeGrab();

    private native void nativeUngrab();

    private boolean hasWarningWindow() {
        return ((Window)this.target).getWarningString() != null;
    }

    boolean isTargetUndecorated() {
        return true;
    }

    @Override
    public native void repositionSecurityWarning();

    @Override
    public void print(Graphics g) {
        Shape shape = ((Window)this.target).getShape();
        if (shape != null) {
            g.setClip(shape);
        }
        super.print(g);
    }

    private void replaceSurfaceDataRecursively(Component c) {
        Object cp;
        if (c instanceof Container) {
            for (Component child : ((Container)c).getComponents()) {
                this.replaceSurfaceDataRecursively(child);
            }
        }
        if ((cp = AWTAccessor.getComponentAccessor().getPeer(c)) instanceof WComponentPeer) {
            ((WComponentPeer)cp).replaceSurfaceDataLater();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final Graphics getTranslucentGraphics() {
        Object object = this.getStateLock();
        synchronized (object) {
            return this.isOpaque ? null : this.painter.getGraphics(false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        Object object = this.getStateLock();
        synchronized (object) {
            if (!this.isOpaque && ((Window)this.target).isVisible()) {
                this.updateWindow(true);
            }
        }
    }

    private native void setOpacity(int var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setOpacity(float opacity) {
        if (!((SunToolkit)((Window)this.target).getToolkit()).isWindowOpacitySupported()) {
            return;
        }
        if (opacity < 0.0f || opacity > 1.0f) {
            throw new IllegalArgumentException("The value of opacity should be in the range [0.0f .. 1.0f].");
        }
        if ((this.opacity == 1.0f && opacity < 1.0f || this.opacity < 1.0f && opacity == 1.0f) && !Win32GraphicsEnvironment.isVistaOS()) {
            this.replaceSurfaceDataRecursively((Component)this.getTarget());
        }
        this.opacity = opacity;
        int maxOpacity = 255;
        int iOpacity = (int)(opacity * 255.0f);
        if (iOpacity < 0) {
            iOpacity = 0;
        }
        if (iOpacity > 255) {
            iOpacity = 255;
        }
        this.setOpacity(iOpacity);
        Object object = this.getStateLock();
        synchronized (object) {
            if (!this.isOpaque && ((Window)this.target).isVisible()) {
                this.updateWindow(true);
            }
        }
    }

    private native void setOpaqueImpl(boolean var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setOpaque(boolean isOpaque) {
        Shape shape;
        SunToolkit sunToolkit;
        Object object = this.getStateLock();
        synchronized (object) {
            if (this.isOpaque == isOpaque) {
                return;
            }
        }
        Window target = (Window)this.getTarget();
        if (!(isOpaque || (sunToolkit = (SunToolkit)target.getToolkit()).isWindowTranslucencySupported() && sunToolkit.isTranslucencyCapable(target.getGraphicsConfiguration()))) {
            return;
        }
        boolean isVistaOS = Win32GraphicsEnvironment.isVistaOS();
        if (this.isOpaque != isOpaque && !isVistaOS) {
            this.replaceSurfaceDataRecursively(target);
        }
        Object object2 = this.getStateLock();
        synchronized (object2) {
            this.isOpaque = isOpaque;
            this.setOpaqueImpl(isOpaque);
            if (isOpaque) {
                TranslucentWindowPainter currentPainter = this.painter;
                if (currentPainter != null) {
                    currentPainter.flush();
                    this.painter = null;
                }
            } else {
                this.painter = TranslucentWindowPainter.createInstance(this);
            }
        }
        if (isVistaOS && (shape = target.getShape()) != null) {
            target.setShape(shape);
        }
        if (target.isVisible()) {
            this.updateWindow(true);
        }
    }

    native void updateWindowImpl(int[] var1, int var2, int var3);

    @Override
    public void updateWindow() {
        this.updateWindow(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateWindow(boolean repaint) {
        Window w = (Window)this.target;
        Object object = this.getStateLock();
        synchronized (object) {
            if (this.isOpaque || !w.isVisible() || w.getWidth() <= 0 || w.getHeight() <= 0) {
                return;
            }
            TranslucentWindowPainter currentPainter = this.painter;
            if (currentPainter != null) {
                currentPainter.updateWindow(repaint);
            } else if (log.isLoggable(PlatformLogger.Level.FINER)) {
                log.finer("Translucent window painter is null in updateWindow");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void initActiveWindowsTracking(Window w) {
        AppContext appContext;
        AppContext appContext2 = appContext = AppContext.getAppContext();
        synchronized (appContext2) {
            LinkedList l = (LinkedList)appContext.get(ACTIVE_WINDOWS_KEY);
            if (l == null) {
                l = new LinkedList();
                appContext.put(ACTIVE_WINDOWS_KEY, l);
                appContext.addPropertyChangeListener("guidisposed", guiDisposedListener);
                KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                kfm.addPropertyChangeListener("activeWindow", activeWindowListener);
            }
        }
    }

    static {
        WWindowPeer.initIDs();
    }

    private static class ActiveWindowListener
    implements PropertyChangeListener {
        private ActiveWindowListener() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            AppContext appContext;
            Window w = (Window)e.getNewValue();
            if (w == null) {
                return;
            }
            AppContext appContext2 = appContext = SunToolkit.targetToAppContext(w);
            synchronized (appContext2) {
                WWindowPeer wp = (WWindowPeer)AWTAccessor.getComponentAccessor().getPeer(w);
                List l = (List)appContext.get(ACTIVE_WINDOWS_KEY);
                if (l != null) {
                    l.remove(wp);
                    l.add(wp);
                }
            }
        }
    }

    private static class GuiDisposedListener
    implements PropertyChangeListener {
        private GuiDisposedListener() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            AppContext appContext;
            boolean isDisposed = (Boolean)e.getNewValue();
            if (!isDisposed && log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine(" Assertion (newValue != true) failed for AppContext.GUI_DISPOSED ");
            }
            AppContext appContext2 = appContext = AppContext.getAppContext();
            synchronized (appContext2) {
                appContext.remove(ACTIVE_WINDOWS_KEY);
                appContext.removePropertyChangeListener("guidisposed", this);
                KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                kfm.removePropertyChangeListener("activeWindow", activeWindowListener);
            }
        }
    }
}

