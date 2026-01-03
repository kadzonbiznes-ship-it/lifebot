/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.dnd.DropTarget;
import java.awt.dnd.peer.DropTargetPeer;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.InvocationEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.PaintEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;
import java.awt.peer.ComponentPeer;
import java.awt.peer.ContainerPeer;
import sun.awt.AWTAccessor;
import sun.awt.PaintEventDispatcher;
import sun.awt.RepaintArea;
import sun.awt.SunToolkit;
import sun.awt.Win32GraphicsConfig;
import sun.awt.Win32GraphicsEnvironment;
import sun.awt.event.IgnorePaintEvent;
import sun.awt.image.SunVolatileImage;
import sun.awt.windows.WEmbeddedFrame;
import sun.awt.windows.WEmbeddedFramePeer;
import sun.awt.windows.WFontMetrics;
import sun.awt.windows.WGlobalCursorManager;
import sun.awt.windows.WKeyboardFocusManagerPeer;
import sun.awt.windows.WObjectPeer;
import sun.awt.windows.WToolkit;
import sun.awt.windows.WWindowPeer;
import sun.java2d.InvalidPipeException;
import sun.java2d.ScreenUpdateManager;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.opengl.OGLSurfaceData;
import sun.java2d.pipe.Region;
import sun.util.logging.PlatformLogger;

public abstract class WComponentPeer
extends WObjectPeer
implements ComponentPeer,
DropTargetPeer {
    protected volatile long hwnd;
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.windows.WComponentPeer");
    private static final PlatformLogger shapeLog = PlatformLogger.getLogger("sun.awt.windows.shape.WComponentPeer");
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("sun.awt.windows.focus.WComponentPeer");
    SurfaceData surfaceData;
    private RepaintArea paintArea;
    protected Win32GraphicsConfig winGraphicsConfig;
    boolean isLayouting = false;
    boolean paintPending = false;
    int oldWidth = -1;
    int oldHeight = -1;
    private int numBackBuffers = 0;
    private VolatileImage backBuffer = null;
    private BufferCapabilities backBufferCaps = null;
    private Color foreground;
    private Color background;
    private Font font;
    int nDropTargets;
    long nativeDropTargetContext;
    public int serialNum = 0;
    private static final double BANDING_DIVISOR = 4.0;
    static final Font defaultFont = new Font("Dialog", 0, 12);
    private int updateX1;
    private int updateY1;
    private int updateX2;
    private int updateY2;
    private volatile boolean isAccelCapable = true;

    @Override
    public native boolean isObscured();

    @Override
    public boolean canDetermineObscurity() {
        return true;
    }

    private synchronized native void pShow();

    synchronized native void hide();

    synchronized native void enable();

    synchronized native void disable();

    public long getHWnd() {
        return this.hwnd;
    }

    @Override
    public native Point getLocationOnScreen();

    @Override
    public void setVisible(boolean b) {
        if (b) {
            this.show();
        } else {
            this.hide();
        }
    }

    public void show() {
        Dimension s = ((Component)this.target).getSize();
        this.oldHeight = s.height;
        this.oldWidth = s.width;
        this.pShow();
    }

    @Override
    public void setEnabled(boolean b) {
        if (b) {
            this.enable();
        } else {
            this.disable();
        }
    }

    private native void reshapeNoCheck(int var1, int var2, int var3, int var4);

    @Override
    public void setBounds(int x, int y, int width, int height, int op) {
        boolean bl = this.paintPending = width != this.oldWidth || height != this.oldHeight;
        if ((op & 0x4000) != 0) {
            this.reshapeNoCheck(x, y, width, height);
        } else {
            this.reshape(x, y, width, height);
        }
        if (width != this.oldWidth || height != this.oldHeight) {
            try {
                this.replaceSurfaceData();
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
            this.oldWidth = width;
            this.oldHeight = height;
        }
        ++this.serialNum;
    }

    void dynamicallyLayoutContainer() {
        Container parent;
        if (log.isLoggable(PlatformLogger.Level.FINE) && (parent = WToolkit.getNativeContainer((Component)this.target)) != null) {
            log.fine("Assertion (parent == null) failed");
        }
        final Container cont = (Container)this.target;
        WToolkit.executeOnEventHandlerThread(cont, new Runnable(){
            final /* synthetic */ WComponentPeer this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public void run() {
                cont.invalidate();
                cont.validate();
                if (this.this$0.surfaceData instanceof D3DSurfaceData.D3DWindowSurfaceData || this.this$0.surfaceData instanceof OGLSurfaceData) {
                    try {
                        this.this$0.replaceSurfaceData();
                    }
                    catch (InvalidPipeException invalidPipeException) {
                        // empty catch block
                    }
                }
            }
        });
    }

    void paintDamagedAreaImmediately() {
        this.updateWindow();
        SunToolkit.flushPendingEvents();
        this.paintArea.paint(this.target, this.shouldClearRectBeforePaint());
    }

    synchronized native void updateWindow();

    @Override
    public void paint(Graphics g) {
        ((Component)this.target).paint(g);
    }

    public void repaint(long tm, int x, int y, int width, int height) {
    }

    private native int[] createPrintedPixels(int var1, int var2, int var3, int var4, int var5);

    @Override
    public void print(Graphics g) {
        Component comp = (Component)this.target;
        int totalW = comp.getWidth();
        int totalH = comp.getHeight();
        int hInc = (int)((double)totalH / 4.0);
        if (hInc == 0) {
            hInc = totalH;
        }
        for (int startY = 0; startY < totalH; startY += hInc) {
            Color bgColor;
            int h;
            int[] pix;
            int endY = startY + hInc - 1;
            if (endY >= totalH) {
                endY = totalH - 1;
            }
            if ((pix = this.createPrintedPixels(0, startY, totalW, h = endY - startY + 1, (bgColor = comp.getBackground()) == null ? 255 : bgColor.getAlpha())) == null) continue;
            BufferedImage bim = new BufferedImage(totalW, h, 2);
            bim.setRGB(0, 0, totalW, h, pix, 0, totalW);
            g.drawImage(bim, 0, startY, null);
            bim.flush();
        }
        comp.print(g);
    }

    @Override
    public void coalescePaintEvent(PaintEvent e) {
        Rectangle r = e.getUpdateRect();
        if (!(e instanceof IgnorePaintEvent)) {
            this.paintArea.add(r, e.getID());
        }
        if (log.isLoggable(PlatformLogger.Level.FINEST)) {
            switch (e.getID()) {
                case 801: {
                    log.finest("coalescePaintEvent: UPDATE: add: x = " + r.x + ", y = " + r.y + ", width = " + r.width + ", height = " + r.height);
                    return;
                }
                case 800: {
                    log.finest("coalescePaintEvent: PAINT: add: x = " + r.x + ", y = " + r.y + ", width = " + r.width + ", height = " + r.height);
                    return;
                }
            }
        }
    }

    public synchronized native void reshape(int var1, int var2, int var3, int var4);

    public boolean handleJavaKeyEvent(KeyEvent e) {
        return false;
    }

    public void handleJavaMouseEvent(MouseEvent e) {
        switch (e.getID()) {
            case 501: {
                if (this.target != e.getSource() || ((Component)this.target).isFocusOwner() || !WKeyboardFocusManagerPeer.shouldFocusOnClick((Component)this.target)) break;
                WKeyboardFocusManagerPeer.requestFocusFor((Component)this.target, FocusEvent.Cause.MOUSE_EVENT);
            }
        }
    }

    native void nativeHandleEvent(AWTEvent var1);

    @Override
    public void handleEvent(AWTEvent e) {
        int id = e.getID();
        if (e instanceof InputEvent && !((InputEvent)e).isConsumed() && ((Component)this.target).isEnabled()) {
            if (e instanceof MouseEvent && !(e instanceof MouseWheelEvent)) {
                this.handleJavaMouseEvent((MouseEvent)e);
            } else if (e instanceof KeyEvent && this.handleJavaKeyEvent((KeyEvent)e)) {
                return;
            }
        }
        switch (id) {
            case 800: {
                this.paintPending = false;
            }
            case 801: {
                if (!this.isLayouting && !this.paintPending) {
                    this.paintArea.paint(this.target, this.shouldClearRectBeforePaint());
                }
                return;
            }
            case 1004: 
            case 1005: {
                this.handleJavaFocusEvent((FocusEvent)e);
            }
        }
        this.nativeHandleEvent(e);
    }

    void handleJavaFocusEvent(FocusEvent fe) {
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer(fe.toString());
        }
        this.setFocus(fe.getID() == 1004);
    }

    native void setFocus(boolean var1);

    @Override
    public Dimension getMinimumSize() {
        return ((Component)this.target).getSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return this.getMinimumSize();
    }

    @Override
    public void layout() {
    }

    public Rectangle getBounds() {
        return ((Component)this.target).getBounds();
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public GraphicsConfiguration getGraphicsConfiguration() {
        if (this.winGraphicsConfig != null) {
            return this.winGraphicsConfig;
        }
        return ((Component)this.target).getGraphicsConfiguration();
    }

    public SurfaceData getSurfaceData() {
        return this.surfaceData;
    }

    public void replaceSurfaceData() {
        this.replaceSurfaceData(this.numBackBuffers, this.backBufferCaps);
    }

    public void createScreenSurface(boolean isResize) {
        Win32GraphicsConfig gc = (Win32GraphicsConfig)this.getGraphicsConfiguration();
        ScreenUpdateManager mgr = ScreenUpdateManager.getInstance();
        this.surfaceData = mgr.createScreenSurface(gc, this, this.numBackBuffers, isResize);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void replaceSurfaceData(int newNumBackBuffers, BufferCapabilities caps) {
        SurfaceData oldData = null;
        VolatileImage oldBB = null;
        Object object = ((Component)this.target).getTreeLock();
        synchronized (object) {
            WComponentPeer wComponentPeer = this;
            synchronized (wComponentPeer) {
                if (this.pData == 0L) {
                    return;
                }
                this.numBackBuffers = newNumBackBuffers;
                ScreenUpdateManager mgr = ScreenUpdateManager.getInstance();
                oldData = this.surfaceData;
                mgr.dropScreenSurface(oldData);
                this.createScreenSurface(true);
                if (oldData != null) {
                    oldData.invalidate();
                }
                oldBB = this.backBuffer;
                if (this.numBackBuffers > 0) {
                    this.backBufferCaps = caps;
                    Win32GraphicsConfig gc = (Win32GraphicsConfig)this.getGraphicsConfiguration();
                    this.backBuffer = gc.createBackBuffer(this);
                } else if (this.backBuffer != null) {
                    this.backBufferCaps = null;
                    this.backBuffer = null;
                }
            }
        }
        if (oldData != null) {
            oldData.flush();
            oldData = null;
        }
        if (oldBB != null) {
            oldBB.flush();
            oldData = null;
        }
    }

    public void replaceSurfaceDataLater() {
        Runnable r = new Runnable(){

            @Override
            public void run() {
                if (!WComponentPeer.this.isDisposed()) {
                    try {
                        WComponentPeer.this.replaceSurfaceData();
                    }
                    catch (InvalidPipeException invalidPipeException) {
                        // empty catch block
                    }
                }
            }
        };
        Component c = (Component)this.target;
        if (!PaintEventDispatcher.getPaintEventDispatcher().queueSurfaceDataReplacing(c, r)) {
            this.postEvent(new InvocationEvent((Object)c, r));
        }
    }

    @Override
    public boolean updateGraphicsData(GraphicsConfiguration gc) {
        AffineTransform old = this.getGraphicsConfiguration().getDefaultTransform();
        this.winGraphicsConfig = (Win32GraphicsConfig)gc;
        if (gc != null && !old.equals(gc.getDefaultTransform())) {
            this.syncBounds();
        }
        try {
            this.replaceSurfaceData();
        }
        catch (InvalidPipeException invalidPipeException) {
            // empty catch block
        }
        return false;
    }

    void syncBounds() {
        Rectangle r = ((Component)this.target).getBounds();
        this.setBounds(r.x, r.y, r.width, r.height, 3);
    }

    @Override
    public ColorModel getColorModel() {
        GraphicsConfiguration gc = this.getGraphicsConfiguration();
        if (gc != null) {
            return gc.getColorModel();
        }
        return null;
    }

    public ColorModel getDeviceColorModel() {
        Win32GraphicsConfig gc = (Win32GraphicsConfig)this.getGraphicsConfiguration();
        if (gc != null) {
            return gc.getDeviceColorModel();
        }
        return null;
    }

    public ColorModel getColorModel(int transparency) {
        GraphicsConfiguration gc = this.getGraphicsConfiguration();
        if (gc != null) {
            return gc.getColorModel(transparency);
        }
        return null;
    }

    @Override
    public Graphics getGraphics() {
        Graphics g;
        WWindowPeer wpeer;
        if (this.isDisposed()) {
            return null;
        }
        Component target = (Component)this.getTarget();
        Window window = SunToolkit.getContainingWindow(target);
        if (window != null && (wpeer = (WWindowPeer)AWTAccessor.getComponentAccessor().getPeer(window)) != null && (g = wpeer.getTranslucentGraphics()) != null) {
            int x = 0;
            int y = 0;
            for (Component c = target; c != window; c = c.getParent()) {
                x += c.getX();
                y += c.getY();
            }
            g.translate(x, y);
            g.clipRect(0, 0, target.getWidth(), target.getHeight());
            return g;
        }
        SurfaceData surfaceData = this.surfaceData;
        if (surfaceData != null) {
            Font font;
            Color fgColor;
            Color bgColor = this.background;
            if (bgColor == null) {
                bgColor = SystemColor.window;
            }
            if ((fgColor = this.foreground) == null) {
                fgColor = SystemColor.windowText;
            }
            if ((font = this.font) == null) {
                font = defaultFont;
            }
            ScreenUpdateManager mgr = ScreenUpdateManager.getInstance();
            return mgr.createGraphics(surfaceData, this, fgColor, bgColor, font);
        }
        return null;
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        return WFontMetrics.getFontMetrics(font);
    }

    private synchronized native void _dispose();

    @Override
    protected void disposeImpl() {
        SurfaceData oldData = this.surfaceData;
        this.surfaceData = null;
        ScreenUpdateManager.getInstance().dropScreenSurface(oldData);
        oldData.invalidate();
        WToolkit.targetDisposedPeer(this.target, this);
        this._dispose();
    }

    public void disposeLater() {
        this.postEvent(new InvocationEvent(this.target, new Runnable(){

            @Override
            public void run() {
                WComponentPeer.this.dispose();
            }
        }));
    }

    @Override
    public synchronized void setForeground(Color c) {
        this.foreground = c;
        this._setForeground(c.getRGB());
    }

    @Override
    public synchronized void setBackground(Color c) {
        this.background = c;
        this._setBackground(c.getRGB());
    }

    public Color getBackgroundNoSync() {
        return this.background;
    }

    private native void _setForeground(int var1);

    private native void _setBackground(int var1);

    @Override
    public synchronized void setFont(Font f) {
        this.font = f;
        this._setFont(f);
    }

    synchronized native void _setFont(Font var1);

    @Override
    public void updateCursorImmediately() {
        WGlobalCursorManager.getCursorManager().updateCursorImmediately();
    }

    @Override
    public boolean requestFocus(Component lightweightChild, boolean temporary, boolean focusedWindowChangeAllowed, long time, FocusEvent.Cause cause) {
        if (WKeyboardFocusManagerPeer.processSynchronousLightweightTransfer((Component)this.target, lightweightChild, temporary, focusedWindowChangeAllowed, time)) {
            return true;
        }
        int result = WKeyboardFocusManagerPeer.shouldNativelyFocusHeavyweight((Component)this.target, lightweightChild, temporary, focusedWindowChangeAllowed, time, cause);
        switch (result) {
            case 0: {
                return false;
            }
            case 2: {
                Window parentWindow;
                if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                    focusLog.finer("Proceeding with request to " + String.valueOf(lightweightChild) + " in " + String.valueOf(this.target));
                }
                if ((parentWindow = SunToolkit.getContainingWindow((Component)this.target)) == null) {
                    return this.rejectFocusRequestHelper("WARNING: Parent window is null");
                }
                WWindowPeer wpeer = (WWindowPeer)AWTAccessor.getComponentAccessor().getPeer(parentWindow);
                if (wpeer == null) {
                    return this.rejectFocusRequestHelper("WARNING: Parent window's peer is null");
                }
                boolean res = wpeer.requestWindowFocus(cause);
                if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                    focusLog.finer("Requested window focus: " + res);
                }
                if (!res || !parentWindow.isFocused()) {
                    return this.rejectFocusRequestHelper("Waiting for asynchronous processing of the request");
                }
                return WKeyboardFocusManagerPeer.deliverFocus(lightweightChild, (Component)this.target, temporary, focusedWindowChangeAllowed, time, cause);
            }
            case 1: {
                return true;
            }
        }
        return false;
    }

    private boolean rejectFocusRequestHelper(String logMsg) {
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer(logMsg);
        }
        WKeyboardFocusManagerPeer.removeLastFocusRequest((Component)this.target);
        return false;
    }

    @Override
    public Image createImage(int width, int height) {
        Win32GraphicsConfig gc = (Win32GraphicsConfig)this.getGraphicsConfiguration();
        return gc.createAcceleratedImage((Component)this.target, width, height);
    }

    @Override
    public VolatileImage createVolatileImage(int width, int height) {
        return new SunVolatileImage((Component)this.target, width, height);
    }

    public String toString() {
        return this.getClass().getName() + "[" + String.valueOf(this.target) + "]";
    }

    WComponentPeer(Component target) {
        this.target = target;
        this.paintArea = new RepaintArea();
        this.create(this.getNativeParent());
        this.checkCreation();
        this.createScreenSurface(false);
        this.initialize();
        this.start();
    }

    abstract void create(WComponentPeer var1);

    WComponentPeer getNativeParent() {
        Container parent = SunToolkit.getNativeContainer((Component)this.target);
        return (WComponentPeer)WToolkit.targetToPeer(parent);
    }

    protected void checkCreation() {
        if (this.hwnd == 0L || this.pData == 0L) {
            if (this.createError != null) {
                throw this.createError;
            }
            throw new InternalError("couldn't create component peer");
        }
    }

    synchronized native void start();

    void initialize() {
        Font f;
        Color fg;
        if (((Component)this.target).isVisible()) {
            this.show();
        }
        if ((fg = ((Component)this.target).getForeground()) != null) {
            this.setForeground(fg);
        }
        if ((f = ((Component)this.target).getFont()) != null) {
            this.setFont(f);
        }
        if (!((Component)this.target).isEnabled()) {
            this.disable();
        }
        Rectangle r = ((Component)this.target).getBounds();
        this.setBounds(r.x, r.y, r.width, r.height, 3);
    }

    void handleRepaint(int x, int y, int w, int h) {
    }

    void handleExpose(int x, int y, int w, int h) {
        this.postPaintIfNecessary(x, y, w, h);
    }

    public void handlePaint(int x, int y, int w, int h) {
        this.postPaintIfNecessary(x, y, w, h);
    }

    private void postPaintIfNecessary(int x, int y, int w, int h) {
        PaintEvent event;
        if (!AWTAccessor.getComponentAccessor().getIgnoreRepaint((Component)this.target) && (event = PaintEventDispatcher.getPaintEventDispatcher().createPaintEvent((Component)this.target, x, y, w, h)) != null) {
            this.postEvent(event);
        }
    }

    void postEvent(AWTEvent event) {
        this.preprocessPostEvent(event);
        WToolkit.postEvent(WToolkit.targetToAppContext(this.target), event);
    }

    void preprocessPostEvent(AWTEvent event) {
    }

    public void beginLayout() {
        this.isLayouting = true;
    }

    public void endLayout() {
        if (!(this.paintArea.isEmpty() || this.paintPending || ((Component)this.target).getIgnoreRepaint())) {
            this.postEvent(new PaintEvent((Component)this.target, 800, new Rectangle()));
        }
        this.isLayouting = false;
    }

    public native void beginValidate();

    public native void endValidate();

    @Override
    public synchronized void addDropTarget(DropTarget dt) {
        if (this.nDropTargets == 0) {
            this.nativeDropTargetContext = this.addNativeDropTarget();
        }
        ++this.nDropTargets;
    }

    @Override
    public synchronized void removeDropTarget(DropTarget dt) {
        --this.nDropTargets;
        if (this.nDropTargets == 0) {
            this.removeNativeDropTarget();
            this.nativeDropTargetContext = 0L;
        }
    }

    native long addNativeDropTarget();

    native void removeNativeDropTarget();

    native boolean nativeHandlesWheelScrolling();

    @Override
    public boolean handlesWheelScrolling() {
        return this.nativeHandlesWheelScrolling();
    }

    public boolean isPaintPending() {
        return this.paintPending && this.isLayouting;
    }

    @Override
    public void createBuffers(int numBuffers, BufferCapabilities caps) throws AWTException {
        Win32GraphicsConfig gc = (Win32GraphicsConfig)this.getGraphicsConfiguration();
        gc.assertOperationSupported((Component)this.target, numBuffers, caps);
        try {
            this.replaceSurfaceData(numBuffers - 1, caps);
        }
        catch (InvalidPipeException e) {
            throw new AWTException(e.getMessage());
        }
    }

    @Override
    public void destroyBuffers() {
        this.replaceSurfaceData(0, null);
    }

    @Override
    public void flip(int x1, int y1, int x2, int y2, BufferCapabilities.FlipContents flipAction) {
        VolatileImage backBuffer = this.backBuffer;
        if (backBuffer == null) {
            throw new IllegalStateException("Buffers have not been created");
        }
        Win32GraphicsConfig gc = (Win32GraphicsConfig)this.getGraphicsConfiguration();
        gc.flip(this, (Component)this.target, backBuffer, x1, y1, x2, y2, flipAction);
    }

    @Override
    public synchronized Image getBackBuffer() {
        VolatileImage backBuffer = this.backBuffer;
        if (backBuffer == null) {
            throw new IllegalStateException("Buffers have not been created");
        }
        return backBuffer;
    }

    public BufferCapabilities getBackBufferCaps() {
        return this.backBufferCaps;
    }

    public int getBackBuffersNum() {
        return this.numBackBuffers;
    }

    public boolean shouldClearRectBeforePaint() {
        return true;
    }

    native void pSetParent(ComponentPeer var1);

    @Override
    public void reparent(ContainerPeer newNativeParent) {
        this.pSetParent(newNativeParent);
    }

    @Override
    public boolean isReparentSupported() {
        return true;
    }

    public void setBoundsOperation(int operation) {
    }

    public boolean isAccelCapable() {
        if (!this.isAccelCapable || !WComponentPeer.isContainingTopLevelAccelCapable((Component)this.target)) {
            return false;
        }
        boolean isTranslucent = SunToolkit.isContainingTopLevelTranslucent((Component)this.target);
        return !isTranslucent || Win32GraphicsEnvironment.isVistaOS();
    }

    public void disableAcceleration() {
        this.isAccelCapable = false;
    }

    native void setRectangularShape(int var1, int var2, int var3, int var4, Region var5);

    private static final boolean isContainingTopLevelAccelCapable(Component c) {
        while (c != null && !(c instanceof WEmbeddedFrame)) {
            c = c.getParent();
        }
        if (c == null) {
            return true;
        }
        WEmbeddedFramePeer peer = (WEmbeddedFramePeer)AWTAccessor.getComponentAccessor().getPeer(c);
        return peer.isAccelCapable();
    }

    @Override
    public void applyShape(Region shape) {
        if (shapeLog.isLoggable(PlatformLogger.Level.FINER)) {
            shapeLog.finer("*** INFO: Setting shape: PEER: " + String.valueOf(this) + "; TARGET: " + String.valueOf(this.target) + "; SHAPE: " + String.valueOf(shape));
        }
        if (shape != null) {
            AffineTransform tx = this.winGraphicsConfig.getDefaultTransform();
            double scaleX = tx.getScaleX();
            double scaleY = tx.getScaleY();
            if (scaleX != 1.0 || scaleY != 1.0) {
                shape = shape.getScaledRegion(scaleX, scaleY);
            }
            this.setRectangularShape(shape.getLoX(), shape.getLoY(), shape.getHiX(), shape.getHiY(), shape.isRectangular() ? null : shape);
        } else {
            this.setRectangularShape(0, 0, 0, 0, null);
        }
    }

    @Override
    public void setZOrder(ComponentPeer above) {
        long aboveHWND = above != null ? ((WComponentPeer)above).getHWnd() : 0L;
        this.setZOrder(aboveHWND);
    }

    private native void setZOrder(long var1);

    public boolean isLightweightFramePeer() {
        return false;
    }
}

