/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import com.sun.java.swing.SwingUtilities3;
import java.applet.Applet;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InvocationEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.BufferStrategyPaintManager;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import jdk.internal.access.JavaSecurityAccess;
import jdk.internal.access.SharedSecrets;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.DisplayChangedListener;
import sun.awt.SunToolkit;
import sun.java2d.SunGraphics2D;
import sun.java2d.SunGraphicsEnvironment;
import sun.java2d.pipe.Region;
import sun.security.action.GetPropertyAction;
import sun.swing.SwingAccessor;
import sun.swing.SwingUtilities2;

public class RepaintManager {
    static final boolean HANDLE_TOP_LEVEL_PAINT;
    private static final short BUFFER_STRATEGY_NOT_SPECIFIED = 0;
    private static final short BUFFER_STRATEGY_SPECIFIED_ON = 1;
    private static final short BUFFER_STRATEGY_SPECIFIED_OFF = 2;
    private static final short BUFFER_STRATEGY_TYPE;
    private Map<GraphicsConfiguration, VolatileImage> volatileMap = new HashMap<GraphicsConfiguration, VolatileImage>(1);
    private Map<Container, Rectangle> hwDirtyComponents;
    private Map<Component, Rectangle> dirtyComponents;
    private Map<Component, Rectangle> tmpDirtyComponents;
    private List<Component> invalidComponents;
    private List<Runnable> runnableList;
    boolean doubleBufferingEnabled = true;
    private Dimension doubleBufferMaxSize;
    private boolean isCustomMaxBufferSizeSet = false;
    DoubleBufferInfo standardDoubleBuffer;
    private PaintManager paintManager;
    private static final Object repaintManagerKey;
    static boolean volatileImageBufferEnabled;
    private static final int volatileBufferType;
    private static boolean nativeDoubleBuffering;
    private static final int VOLATILE_LOOP_MAX = 2;
    private int paintDepth = 0;
    private short bufferStrategyType;
    private boolean painting;
    private JComponent repaintRoot;
    private Thread paintThread;
    private final ProcessingRunnable processingRunnable;
    private static final JavaSecurityAccess javaSecurityAccess;
    private static final DisplayChangedListener displayChangedHandler;
    Rectangle tmp = new Rectangle();
    private List<SwingUtilities2.RepaintListener> repaintListeners = new ArrayList<SwingUtilities2.RepaintListener>(1);

    public static RepaintManager currentManager(Component c) {
        return RepaintManager.currentManager(AppContext.getAppContext());
    }

    static RepaintManager currentManager(AppContext appContext) {
        RepaintManager rm = (RepaintManager)appContext.get(repaintManagerKey);
        if (rm == null) {
            rm = new RepaintManager(BUFFER_STRATEGY_TYPE);
            appContext.put(repaintManagerKey, rm);
        }
        return rm;
    }

    public static RepaintManager currentManager(JComponent c) {
        return RepaintManager.currentManager((Component)c);
    }

    public static void setCurrentManager(RepaintManager aRepaintManager) {
        if (aRepaintManager != null) {
            SwingUtilities.appContextPut(repaintManagerKey, aRepaintManager);
        } else {
            SwingUtilities.appContextRemove(repaintManagerKey);
        }
    }

    public RepaintManager() {
        this(2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private RepaintManager(short bufferStrategyType) {
        this.doubleBufferingEnabled = !nativeDoubleBuffering;
        RepaintManager repaintManager = this;
        synchronized (repaintManager) {
            this.dirtyComponents = new IdentityHashMap<Component, Rectangle>();
            this.tmpDirtyComponents = new IdentityHashMap<Component, Rectangle>();
            this.bufferStrategyType = bufferStrategyType;
            this.hwDirtyComponents = new IdentityHashMap<Container, Rectangle>();
        }
        this.processingRunnable = new ProcessingRunnable();
    }

    private void displayChanged() {
        if (this.isCustomMaxBufferSizeSet) {
            this.clearImages();
        } else {
            this.setDoubleBufferMaximumSize(null);
        }
    }

    public synchronized void addInvalidComponent(JComponent invalidComponent) {
        RepaintManager delegate = this.getDelegate(invalidComponent);
        if (delegate != null) {
            delegate.addInvalidComponent(invalidComponent);
            return;
        }
        Container validateRoot = SwingUtilities.getValidateRoot(invalidComponent, true);
        if (validateRoot == null) {
            return;
        }
        if (this.invalidComponents == null) {
            this.invalidComponents = new ArrayList<Component>();
        } else {
            int n = this.invalidComponents.size();
            for (int i = 0; i < n; ++i) {
                if (validateRoot != this.invalidComponents.get(i)) continue;
                return;
            }
        }
        this.invalidComponents.add(validateRoot);
        this.scheduleProcessingRunnable(SunToolkit.targetToAppContext(invalidComponent));
    }

    public synchronized void removeInvalidComponent(JComponent component) {
        int index;
        RepaintManager delegate = this.getDelegate(component);
        if (delegate != null) {
            delegate.removeInvalidComponent(component);
            return;
        }
        if (this.invalidComponents != null && (index = this.invalidComponents.indexOf(component)) != -1) {
            this.invalidComponents.remove(index);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addDirtyRegion0(Container c, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0 || c == null) {
            return;
        }
        if (c.getWidth() <= 0 || c.getHeight() <= 0) {
            return;
        }
        if (this.extendDirtyRegion(c, x, y, w, h)) {
            return;
        }
        Container root = null;
        for (Container p = c; p != null; p = p.getParent()) {
            if (!p.isVisible() || !p.isDisplayable()) {
                return;
            }
            if (!(p instanceof Window) && !(p instanceof Applet)) continue;
            if (p instanceof Frame && (((Frame)p).getExtendedState() & 1) == 1) {
                return;
            }
            root = p;
            break;
        }
        if (root == null) {
            return;
        }
        RepaintManager repaintManager = this;
        synchronized (repaintManager) {
            if (this.extendDirtyRegion(c, x, y, w, h)) {
                return;
            }
            this.dirtyComponents.put(c, new Rectangle(x, y, w, h));
        }
        this.scheduleProcessingRunnable(SunToolkit.targetToAppContext(c));
    }

    public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
        RepaintManager delegate = this.getDelegate(c);
        if (delegate != null) {
            delegate.addDirtyRegion(c, x, y, w, h);
            return;
        }
        this.addDirtyRegion0(c, x, y, w, h);
    }

    public void addDirtyRegion(Window window, int x, int y, int w, int h) {
        this.addDirtyRegion0(window, x, y, w, h);
    }

    @Deprecated(since="9", forRemoval=true)
    public void addDirtyRegion(Applet applet, int x, int y, int w, int h) {
        this.addDirtyRegion0(applet, x, y, w, h);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void scheduleHeavyWeightPaints() {
        Map<Container, Rectangle> hws;
        RepaintManager repaintManager = this;
        synchronized (repaintManager) {
            if (this.hwDirtyComponents.size() == 0) {
                return;
            }
            hws = this.hwDirtyComponents;
            this.hwDirtyComponents = new IdentityHashMap<Container, Rectangle>();
        }
        for (Container hw : hws.keySet()) {
            Rectangle dirty = hws.get(hw);
            if (hw instanceof Window) {
                this.addDirtyRegion((Window)hw, dirty.x, dirty.y, dirty.width, dirty.height);
                continue;
            }
            if (hw instanceof Applet) {
                this.addDirtyRegion((Applet)hw, dirty.x, dirty.y, dirty.width, dirty.height);
                continue;
            }
            this.addDirtyRegion0(hw, dirty.x, dirty.y, dirty.width, dirty.height);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void nativeAddDirtyRegion(AppContext appContext, Container c, int x, int y, int w, int h) {
        if (w > 0 && h > 0) {
            RepaintManager repaintManager = this;
            synchronized (repaintManager) {
                Rectangle dirty = this.hwDirtyComponents.get(c);
                if (dirty == null) {
                    this.hwDirtyComponents.put(c, new Rectangle(x, y, w, h));
                } else {
                    this.hwDirtyComponents.put(c, SwingUtilities.computeUnion(x, y, w, h, dirty));
                }
            }
            this.scheduleProcessingRunnable(appContext);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void nativeQueueSurfaceDataRunnable(AppContext appContext, final Component c, final Runnable r) {
        RepaintManager repaintManager = this;
        synchronized (repaintManager) {
            if (this.runnableList == null) {
                this.runnableList = new LinkedList<Runnable>();
            }
            this.runnableList.add(new Runnable(){

                @Override
                public void run() {
                    AccessControlContext stack = AccessController.getContext();
                    AccessControlContext acc = AWTAccessor.getComponentAccessor().getAccessControlContext(c);
                    javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Void>(){

                        @Override
                        public Void run() {
                            r.run();
                            return null;
                        }
                    }, stack, acc);
                }
            });
        }
        this.scheduleProcessingRunnable(appContext);
    }

    private synchronized boolean extendDirtyRegion(Component c, int x, int y, int w, int h) {
        Rectangle r = this.dirtyComponents.get(c);
        if (r != null) {
            SwingUtilities.computeUnion(x, y, w, h, r);
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Rectangle getDirtyRegion(JComponent aComponent) {
        Rectangle r;
        RepaintManager delegate = this.getDelegate(aComponent);
        if (delegate != null) {
            return delegate.getDirtyRegion(aComponent);
        }
        RepaintManager repaintManager = this;
        synchronized (repaintManager) {
            r = this.dirtyComponents.get(aComponent);
        }
        if (r == null) {
            return new Rectangle(0, 0, 0, 0);
        }
        return new Rectangle(r);
    }

    public void markCompletelyDirty(JComponent aComponent) {
        RepaintManager delegate = this.getDelegate(aComponent);
        if (delegate != null) {
            delegate.markCompletelyDirty(aComponent);
            return;
        }
        this.addDirtyRegion(aComponent, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void markCompletelyClean(JComponent aComponent) {
        RepaintManager delegate = this.getDelegate(aComponent);
        if (delegate != null) {
            delegate.markCompletelyClean(aComponent);
            return;
        }
        RepaintManager repaintManager = this;
        synchronized (repaintManager) {
            this.dirtyComponents.remove(aComponent);
        }
    }

    public boolean isCompletelyDirty(JComponent aComponent) {
        RepaintManager delegate = this.getDelegate(aComponent);
        if (delegate != null) {
            return delegate.isCompletelyDirty(aComponent);
        }
        Rectangle r = this.getDirtyRegion(aComponent);
        return r.width == Integer.MAX_VALUE && r.height == Integer.MAX_VALUE;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void validateInvalidComponents() {
        List<Component> ic;
        RepaintManager repaintManager = this;
        synchronized (repaintManager) {
            if (this.invalidComponents == null) {
                return;
            }
            ic = this.invalidComponents;
            this.invalidComponents = null;
        }
        int n = ic.size();
        for (int i = 0; i < n; ++i) {
            final Component c = ic.get(i);
            AccessControlContext stack = AccessController.getContext();
            AccessControlContext acc = AWTAccessor.getComponentAccessor().getAccessControlContext(c);
            javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Void>(){

                @Override
                public Void run() {
                    c.validate();
                    return null;
                }
            }, stack, acc);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void prePaintDirtyRegions() {
        List<Runnable> runnableList;
        Map<Component, Rectangle> dirtyComponents;
        RepaintManager repaintManager = this;
        synchronized (repaintManager) {
            dirtyComponents = this.dirtyComponents;
            runnableList = this.runnableList;
            this.runnableList = null;
        }
        if (runnableList != null) {
            for (Runnable runnable : runnableList) {
                runnable.run();
            }
        }
        this.paintDirtyRegions();
        if (dirtyComponents.size() > 0) {
            this.paintDirtyRegions(dirtyComponents);
        }
    }

    private void updateWindows(Map<Component, Rectangle> dirtyComponents) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (!(toolkit instanceof SunToolkit) || !((SunToolkit)toolkit).needUpdateWindow()) {
            return;
        }
        HashSet<Window> windows = new HashSet<Window>();
        Set<Component> dirtyComps = dirtyComponents.keySet();
        for (Component dirty : dirtyComps) {
            Window window = dirty instanceof Window ? (Window)dirty : SwingUtilities.getWindowAncestor(dirty);
            if (window == null || window.isOpaque()) continue;
            windows.add(window);
        }
        for (Window window : windows) {
            AWTAccessor.getWindowAccessor().updateWindow(window);
        }
    }

    boolean isPainting() {
        return this.painting;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void paintDirtyRegions() {
        RepaintManager repaintManager = this;
        synchronized (repaintManager) {
            Map<Component, Rectangle> tmp = this.tmpDirtyComponents;
            this.tmpDirtyComponents = this.dirtyComponents;
            this.dirtyComponents = tmp;
            this.dirtyComponents.clear();
        }
        this.paintDirtyRegions(this.tmpDirtyComponents);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void paintDirtyRegions(final Map<Component, Rectangle> tmpDirtyComponents) {
        if (tmpDirtyComponents.isEmpty()) {
            return;
        }
        final ArrayList<Component> roots = new ArrayList<Component>(tmpDirtyComponents.size());
        for (Component dirty : tmpDirtyComponents.keySet()) {
            this.collectDirtyComponents(tmpDirtyComponents, dirty, roots);
        }
        final AtomicInteger count = new AtomicInteger(roots.size());
        this.painting = true;
        try {
            for (int j = 0; j < count.get(); ++j) {
                final int i = j;
                final Component dirtyComponent = (Component)roots.get(j);
                AccessControlContext stack = AccessController.getContext();
                AccessControlContext acc = AWTAccessor.getComponentAccessor().getAccessControlContext(dirtyComponent);
                javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Void>(){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    public Void run() {
                        Graphics g;
                        Rectangle rect = (Rectangle)tmpDirtyComponents.get(dirtyComponent);
                        if (rect == null) {
                            return null;
                        }
                        int localBoundsH = dirtyComponent.getHeight();
                        int localBoundsW = dirtyComponent.getWidth();
                        SwingUtilities.computeIntersection(0, 0, localBoundsW, localBoundsH, rect);
                        if (dirtyComponent instanceof JComponent) {
                            ((JComponent)dirtyComponent).paintImmediately(rect.x, rect.y, rect.width, rect.height);
                        } else if (dirtyComponent.isShowing() && (g = JComponent.safelyGetGraphics(dirtyComponent, dirtyComponent)) != null) {
                            g.setClip(rect.x, rect.y, rect.width, rect.height);
                            try {
                                dirtyComponent.paint(g);
                            }
                            finally {
                                g.dispose();
                            }
                        }
                        if (RepaintManager.this.repaintRoot != null) {
                            RepaintManager.this.adjustRoots(RepaintManager.this.repaintRoot, roots, i + 1);
                            count.set(roots.size());
                            RepaintManager.this.paintManager.isRepaintingRoot = true;
                            RepaintManager.this.repaintRoot.paintImmediately(0, 0, RepaintManager.this.repaintRoot.getWidth(), RepaintManager.this.repaintRoot.getHeight());
                            RepaintManager.this.paintManager.isRepaintingRoot = false;
                            RepaintManager.this.repaintRoot = null;
                        }
                        return null;
                    }
                }, stack, acc);
            }
        }
        finally {
            this.painting = false;
        }
        this.updateWindows(tmpDirtyComponents);
        tmpDirtyComponents.clear();
    }

    private void adjustRoots(JComponent root, List<Component> roots, int index) {
        for (int i = roots.size() - 1; i >= index; --i) {
            Component c;
            for (c = roots.get(i); c != root && c instanceof JComponent; c = c.getParent()) {
            }
            if (c != root) continue;
            roots.remove(i);
        }
    }

    void collectDirtyComponents(Map<Component, Rectangle> dirtyComponents, Component dirtyComponent, List<Component> roots) {
        Container parent;
        Component rootDirtyComponent;
        Component component = rootDirtyComponent = dirtyComponent;
        int x = dirtyComponent.getX();
        int y = dirtyComponent.getY();
        int w = dirtyComponent.getWidth();
        int h = dirtyComponent.getHeight();
        int rootDx = 0;
        int dx = 0;
        int rootDy = 0;
        int dy = 0;
        this.tmp.setBounds(dirtyComponents.get(dirtyComponent));
        SwingUtilities.computeIntersection(0, 0, w, h, this.tmp);
        if (this.tmp.isEmpty()) {
            return;
        }
        while (component instanceof JComponent && (parent = component.getParent()) != null) {
            component = parent;
            dx += x;
            dy += y;
            this.tmp.setLocation(this.tmp.x + x, this.tmp.y + y);
            x = component.getX();
            y = component.getY();
            w = component.getWidth();
            h = component.getHeight();
            this.tmp = SwingUtilities.computeIntersection(0, 0, w, h, this.tmp);
            if (this.tmp.isEmpty()) {
                return;
            }
            if (dirtyComponents.get(component) == null) continue;
            rootDirtyComponent = component;
            rootDx = dx;
            rootDy = dy;
        }
        if (dirtyComponent != rootDirtyComponent) {
            this.tmp.setLocation(this.tmp.x + rootDx - dx, this.tmp.y + rootDy - dy);
            Rectangle r = dirtyComponents.get(rootDirtyComponent);
            SwingUtilities.computeUnion(this.tmp.x, this.tmp.y, this.tmp.width, this.tmp.height, r);
        }
        if (!roots.contains(rootDirtyComponent)) {
            roots.add(rootDirtyComponent);
        }
    }

    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.dirtyComponents != null) {
            sb.append(String.valueOf(this.dirtyComponents));
        }
        return sb.toString();
    }

    public Image getOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {
        RepaintManager delegate = this.getDelegate(c);
        if (delegate != null) {
            return delegate.getOffscreenBuffer(c, proposedWidth, proposedHeight);
        }
        return this._getOffscreenBuffer(c, proposedWidth, proposedHeight);
    }

    public Image getVolatileOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {
        int width;
        Toolkit tk;
        Window w;
        RepaintManager delegate = this.getDelegate(c);
        if (delegate != null) {
            return delegate.getVolatileOffscreenBuffer(c, proposedWidth, proposedHeight);
        }
        Window window = w = c instanceof Window ? (Window)c : SwingUtilities.getWindowAncestor(c);
        if (w != null && !w.isOpaque() && (tk = Toolkit.getDefaultToolkit()) instanceof SunToolkit && ((SunToolkit)tk).needUpdateWindow()) {
            return null;
        }
        GraphicsConfiguration config = c.getGraphicsConfiguration();
        if (config == null) {
            config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        }
        Dimension maxSize = this.getDoubleBufferMaximumSize();
        int n = proposedWidth < 1 ? 1 : (width = proposedWidth > maxSize.width ? maxSize.width : proposedWidth);
        int height = proposedHeight < 1 ? 1 : (proposedHeight > maxSize.height ? maxSize.height : proposedHeight);
        VolatileImage image = this.volatileMap.get(config);
        if (image == null || image.getWidth() < width || image.getHeight() < height) {
            if (image != null) {
                image.flush();
            }
            image = config.createCompatibleVolatileImage(width, height, volatileBufferType);
            this.volatileMap.put(config, image);
        }
        return image;
    }

    private Image _getOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {
        int height;
        int width;
        Toolkit tk;
        Window w;
        Dimension maxSize = this.getDoubleBufferMaximumSize();
        Window window = w = c instanceof Window ? (Window)c : SwingUtilities.getWindowAncestor(c);
        if (!w.isOpaque() && (tk = Toolkit.getDefaultToolkit()) instanceof SunToolkit && ((SunToolkit)tk).needUpdateWindow()) {
            return null;
        }
        if (this.standardDoubleBuffer == null) {
            this.standardDoubleBuffer = new DoubleBufferInfo();
        }
        DoubleBufferInfo doubleBuffer = this.standardDoubleBuffer;
        int n = proposedWidth < 1 ? 1 : (width = proposedWidth > maxSize.width ? maxSize.width : proposedWidth);
        int n2 = proposedHeight < 1 ? 1 : (height = proposedHeight > maxSize.height ? maxSize.height : proposedHeight);
        if (doubleBuffer.needsReset || doubleBuffer.image != null && (doubleBuffer.size.width < width || doubleBuffer.size.height < height)) {
            doubleBuffer.needsReset = false;
            if (doubleBuffer.image != null) {
                doubleBuffer.image.flush();
                doubleBuffer.image = null;
            }
            width = Math.max(doubleBuffer.size.width, width);
            height = Math.max(doubleBuffer.size.height, height);
        }
        Image result = doubleBuffer.image;
        if (doubleBuffer.image == null) {
            result = c.createImage(width, height);
            doubleBuffer.size = new Dimension(width, height);
            if (c instanceof JComponent) {
                ((JComponent)c).setCreatedDoubleBuffer(true);
                doubleBuffer.image = result;
            }
        }
        return result;
    }

    public void setDoubleBufferMaximumSize(Dimension d) {
        this.doubleBufferMaxSize = d;
        if (this.doubleBufferMaxSize == null) {
            this.isCustomMaxBufferSizeSet = false;
            this.clearImages();
        } else {
            this.isCustomMaxBufferSizeSet = true;
            this.clearImages(d.width, d.height);
        }
    }

    private void clearImages() {
        this.clearImages(0, 0);
    }

    private void clearImages(int width, int height) {
        if (this.standardDoubleBuffer != null && this.standardDoubleBuffer.image != null && (this.standardDoubleBuffer.image.getWidth(null) > width || this.standardDoubleBuffer.image.getHeight(null) > height)) {
            this.standardDoubleBuffer.image.flush();
            this.standardDoubleBuffer.image = null;
        }
        Iterator<GraphicsConfiguration> gcs = this.volatileMap.keySet().iterator();
        while (gcs.hasNext()) {
            GraphicsConfiguration gc = gcs.next();
            VolatileImage image = this.volatileMap.get(gc);
            if (image.getWidth() <= width && image.getHeight() <= height) continue;
            image.flush();
            gcs.remove();
        }
    }

    public Dimension getDoubleBufferMaximumSize() {
        if (this.doubleBufferMaxSize == null) {
            try {
                Rectangle virtualBounds = new Rectangle();
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                for (GraphicsDevice gd : ge.getScreenDevices()) {
                    GraphicsConfiguration gc = gd.getDefaultConfiguration();
                    virtualBounds = virtualBounds.union(gc.getBounds());
                }
                this.doubleBufferMaxSize = new Dimension(virtualBounds.width, virtualBounds.height);
            }
            catch (HeadlessException e) {
                this.doubleBufferMaxSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
        }
        return this.doubleBufferMaxSize;
    }

    public void setDoubleBufferingEnabled(boolean aFlag) {
        this.doubleBufferingEnabled = aFlag;
        PaintManager paintManager = this.getPaintManager();
        if (!aFlag && paintManager.getClass() != PaintManager.class) {
            this.setPaintManager(new PaintManager());
        }
    }

    public boolean isDoubleBufferingEnabled() {
        return this.doubleBufferingEnabled;
    }

    void resetDoubleBuffer() {
        if (this.standardDoubleBuffer != null) {
            this.standardDoubleBuffer.needsReset = true;
        }
    }

    void resetVolatileDoubleBuffer(GraphicsConfiguration gc) {
        Image image = this.volatileMap.remove(gc);
        if (image != null) {
            image.flush();
        }
    }

    boolean useVolatileDoubleBuffer() {
        return volatileImageBufferEnabled;
    }

    private synchronized boolean isPaintingThread() {
        return Thread.currentThread() == this.paintThread;
    }

    void paint(JComponent paintingComponent, JComponent bufferComponent, Graphics g, int x, int y, int w, int h) {
        PaintManager paintManager = this.getPaintManager();
        if (!this.isPaintingThread() && paintManager.getClass() != PaintManager.class) {
            paintManager = new PaintManager();
            paintManager.repaintManager = this;
        }
        if (!paintManager.paint(paintingComponent, bufferComponent, g, x, y, w, h)) {
            g.setClip(x, y, w, h);
            paintingComponent.paintToOffscreen(g, x, y, w, h, x + w, y + h);
        }
    }

    void copyArea(JComponent c, Graphics g, int x, int y, int w, int h, int deltaX, int deltaY, boolean clip) {
        this.getPaintManager().copyArea(c, g, x, y, w, h, deltaX, deltaY, clip);
    }

    private void addRepaintListener(SwingUtilities2.RepaintListener l) {
        this.repaintListeners.add(l);
    }

    private void removeRepaintListener(SwingUtilities2.RepaintListener l) {
        this.repaintListeners.remove(l);
    }

    void notifyRepaintPerformed(JComponent c, int x, int y, int w, int h) {
        for (SwingUtilities2.RepaintListener l : this.repaintListeners) {
            l.repaintPerformed(c, x, y, w, h);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void beginPaint() {
        int paintDepth;
        boolean multiThreadedPaint = false;
        Thread currentThread = Thread.currentThread();
        RepaintManager repaintManager = this;
        synchronized (repaintManager) {
            paintDepth = this.paintDepth;
            if (this.paintThread == null || currentThread == this.paintThread) {
                this.paintThread = currentThread;
                ++this.paintDepth;
            } else {
                multiThreadedPaint = true;
            }
        }
        if (!multiThreadedPaint && paintDepth == 0) {
            this.getPaintManager().beginPaint();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void endPaint() {
        if (this.isPaintingThread()) {
            PaintManager paintManager = null;
            RepaintManager repaintManager = this;
            synchronized (repaintManager) {
                if (--this.paintDepth == 0) {
                    paintManager = this.getPaintManager();
                }
            }
            if (paintManager != null) {
                paintManager.endPaint();
                repaintManager = this;
                synchronized (repaintManager) {
                    this.paintThread = null;
                }
            }
        }
    }

    boolean show(Container c, int x, int y, int w, int h) {
        return this.getPaintManager().show(c, x, y, w, h);
    }

    void doubleBufferingChanged(JRootPane rootPane) {
        this.getPaintManager().doubleBufferingChanged(rootPane);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void setPaintManager(PaintManager paintManager) {
        PaintManager oldPaintManager;
        if (paintManager == null) {
            paintManager = new PaintManager();
        }
        RepaintManager repaintManager = this;
        synchronized (repaintManager) {
            oldPaintManager = this.paintManager;
            this.paintManager = paintManager;
            paintManager.repaintManager = this;
        }
        if (oldPaintManager != null) {
            oldPaintManager.dispose();
        }
    }

    private synchronized PaintManager getPaintManager() {
        if (this.paintManager == null) {
            BufferStrategyPaintManager paintManager = null;
            if (this.doubleBufferingEnabled && !nativeDoubleBuffering) {
                switch (this.bufferStrategyType) {
                    case 0: {
                        SunToolkit stk;
                        Toolkit tk = Toolkit.getDefaultToolkit();
                        if (!(tk instanceof SunToolkit) || !(stk = (SunToolkit)tk).useBufferPerWindow()) break;
                        paintManager = new BufferStrategyPaintManager();
                        break;
                    }
                    case 1: {
                        paintManager = new BufferStrategyPaintManager();
                        break;
                    }
                }
            }
            this.setPaintManager(paintManager);
        }
        return this.paintManager;
    }

    private void scheduleProcessingRunnable(AppContext context) {
        if (this.processingRunnable.markPending()) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            if (tk instanceof SunToolkit) {
                SunToolkit.getSystemEventQueueImplPP(context).postEvent(new InvocationEvent((Object)Toolkit.getDefaultToolkit(), this.processingRunnable));
            } else {
                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new InvocationEvent((Object)Toolkit.getDefaultToolkit(), this.processingRunnable));
            }
        }
    }

    private RepaintManager getDelegate(Component c) {
        RepaintManager delegate = SwingUtilities3.getDelegateRepaintManager(c);
        if (this == delegate) {
            delegate = null;
        }
        return delegate;
    }

    static {
        Toolkit tk;
        boolean t3;
        boolean t2;
        boolean t1;
        repaintManagerKey = RepaintManager.class;
        volatileImageBufferEnabled = true;
        javaSecurityAccess = SharedSecrets.getJavaSecurityAccess();
        displayChangedHandler = new DisplayChangedHandler();
        SwingAccessor.setRepaintManagerAccessor(new SwingAccessor.RepaintManagerAccessor(){

            @Override
            public void addRepaintListener(RepaintManager rm, SwingUtilities2.RepaintListener l) {
                rm.addRepaintListener(l);
            }

            @Override
            public void removeRepaintListener(RepaintManager rm, SwingUtilities2.RepaintListener l) {
                rm.removeRepaintListener(l);
            }
        });
        volatileImageBufferEnabled = t1 = "true".equals(AccessController.doPrivileged(new GetPropertyAction("swing.volatileImageBufferEnabled", "true")));
        boolean headless = GraphicsEnvironment.isHeadless();
        if (volatileImageBufferEnabled && headless) {
            volatileImageBufferEnabled = false;
        }
        nativeDoubleBuffering = t2 = "true".equals(AccessController.doPrivileged(new GetPropertyAction("awt.nativeDoubleBuffering")));
        String bs = AccessController.doPrivileged(new GetPropertyAction("swing.bufferPerWindow"));
        BUFFER_STRATEGY_TYPE = headless ? (short)2 : (bs == null ? (short)0 : ("true".equals(bs) ? (short)1 : (short)2));
        HANDLE_TOP_LEVEL_PAINT = t3 = "true".equals(AccessController.doPrivileged(new GetPropertyAction("swing.handleTopLevelPaint", "true")));
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge instanceof SunGraphicsEnvironment) {
            ((SunGraphicsEnvironment)ge).addDisplayChangedListener(displayChangedHandler);
        }
        volatileBufferType = (tk = Toolkit.getDefaultToolkit()) instanceof SunToolkit && ((SunToolkit)tk).isSwingBackbufferTranslucencySupported() ? 3 : 1;
    }

    private final class ProcessingRunnable
    implements Runnable {
        private boolean pending;

        private ProcessingRunnable() {
        }

        public synchronized boolean markPending() {
            if (!this.pending) {
                this.pending = true;
                return true;
            }
            return false;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            ProcessingRunnable processingRunnable = this;
            synchronized (processingRunnable) {
                this.pending = false;
            }
            RepaintManager.this.scheduleHeavyWeightPaints();
            RepaintManager.this.validateInvalidComponents();
            RepaintManager.this.prePaintDirtyRegions();
        }
    }

    private static class DoubleBufferInfo {
        public Image image;
        public Dimension size;
        public boolean needsReset = false;

        private DoubleBufferInfo() {
        }
    }

    static class PaintManager {
        protected RepaintManager repaintManager;
        boolean isRepaintingRoot;

        PaintManager() {
        }

        public boolean paint(JComponent paintingComponent, JComponent bufferComponent, Graphics g, int x, int y, int w, int h) {
            Image offscreen;
            boolean paintCompleted = false;
            int sw = w + 1;
            int sh = h + 1;
            if (this.repaintManager.useVolatileDoubleBuffer() && (offscreen = this.getValidImage(this.repaintManager.getVolatileOffscreenBuffer(bufferComponent, sw, sh))) != null) {
                VolatileImage vImage = (VolatileImage)offscreen;
                GraphicsConfiguration gc = bufferComponent.getGraphicsConfiguration();
                for (int i = 0; !paintCompleted && i < 2; ++i) {
                    if (vImage.validate(gc) == 2) {
                        this.repaintManager.resetVolatileDoubleBuffer(gc);
                        offscreen = this.repaintManager.getVolatileOffscreenBuffer(bufferComponent, sw, sh);
                        vImage = (VolatileImage)offscreen;
                    }
                    this.paintDoubleBuffered(paintingComponent, vImage, g, x, y, w, h);
                    paintCompleted = !vImage.contentsLost();
                }
            }
            if (!paintCompleted && (offscreen = this.getValidImage(this.repaintManager.getOffscreenBuffer(bufferComponent, w, h))) != null) {
                this.paintDoubleBuffered(paintingComponent, offscreen, g, x, y, w, h);
                paintCompleted = true;
            }
            return paintCompleted;
        }

        public void copyArea(JComponent c, Graphics g, int x, int y, int w, int h, int deltaX, int deltaY, boolean clip) {
            g.copyArea(x, y, w, h, deltaX, deltaY);
        }

        public void beginPaint() {
        }

        public void endPaint() {
        }

        public boolean show(Container c, int x, int y, int w, int h) {
            return false;
        }

        public void doubleBufferingChanged(JRootPane rootPane) {
        }

        protected void paintDoubleBuffered(JComponent c, Image image, Graphics g, int clipX, int clipY, int clipW, int clipH) {
            if (image instanceof VolatileImage && this.isPixelsCopying(c, g)) {
                this.paintDoubleBufferedFPScales(c, image, g, clipX, clipY, clipW, clipH);
            } else {
                this.paintDoubleBufferedImpl(c, image, g, clipX, clipY, clipW, clipH);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void paintDoubleBufferedImpl(JComponent c, Image image, Graphics g, int clipX, int clipY, int clipW, int clipH) {
            Graphics osg = image.getGraphics();
            int bw = Math.min(clipW, image.getWidth(null));
            int bh = Math.min(clipH, image.getHeight(null));
            try {
                int maxx = clipX + clipW;
                for (int x = clipX; x < maxx; x += bw) {
                    int maxy = clipY + clipH;
                    for (int y = clipY; y < maxy; y += bh) {
                        Graphics2D g2d;
                        osg.translate(-x, -y);
                        osg.setClip(x, y, bw, bh);
                        if (volatileBufferType != 1 && osg instanceof Graphics2D) {
                            g2d = (Graphics2D)osg;
                            Color oldBg = g2d.getBackground();
                            g2d.setBackground(c.getBackground());
                            g2d.clearRect(x, y, bw, bh);
                            g2d.setBackground(oldBg);
                        }
                        c.paintToOffscreen(osg, x, y, bw, bh, maxx, maxy);
                        g.setClip(x, y, bw, bh);
                        if (volatileBufferType != 1 && g instanceof Graphics2D) {
                            g2d = (Graphics2D)g;
                            Composite oldComposite = g2d.getComposite();
                            g2d.setComposite(AlphaComposite.Src);
                            g2d.drawImage(image, x, y, c);
                            g2d.setComposite(oldComposite);
                        } else {
                            g.drawImage(image, x, y, c);
                        }
                        osg.translate(x, y);
                    }
                }
            }
            finally {
                osg.dispose();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void paintDoubleBufferedFPScales(JComponent c, Image image, Graphics g, int clipX, int clipY, int clipW, int clipH) {
            Graphics osg = image.getGraphics();
            Graphics2D g2d = (Graphics2D)g;
            Graphics2D osg2d = (Graphics2D)osg;
            AffineTransform identity = new AffineTransform();
            int bw = Math.min(clipW, image.getWidth(null));
            int bh = Math.min(clipH, image.getHeight(null));
            AffineTransform tx = g2d.getTransform();
            double scaleX = tx.getScaleX();
            double scaleY = tx.getScaleY();
            double trX = tx.getTranslateX();
            double trY = tx.getTranslateY();
            boolean translucent = volatileBufferType != 1;
            Composite oldComposite = g2d.getComposite();
            try {
                int maxx = clipX + clipW;
                for (int x = clipX; x < maxx; x += bw) {
                    int maxy = clipY + clipH;
                    for (int y = clipY; y < maxy; y += bh) {
                        int pixelx1 = Region.clipRound((double)x * scaleX + trX);
                        int pixely1 = Region.clipRound((double)y * scaleY + trY);
                        int pixelx2 = Region.clipRound((double)(x + bw) * scaleX + trX);
                        int pixely2 = Region.clipRound((double)(y + bh) * scaleY + trY);
                        int pixelw = pixelx2 - pixelx1;
                        int pixelh = pixely2 - pixely1;
                        osg2d.setTransform(identity);
                        if (translucent) {
                            Color oldBg = g2d.getBackground();
                            g2d.setBackground(c.getBackground());
                            g2d.clearRect(pixelx1, pixely1, pixelw, pixelh);
                            g2d.setBackground(oldBg);
                        }
                        osg2d.setClip(0, 0, pixelw, pixelh);
                        osg2d.translate(trX - (double)pixelx1, trY - (double)pixely1);
                        osg2d.scale(scaleX, scaleY);
                        c.paintToOffscreen(osg, x, y, bw, bh, maxx, maxy);
                        g2d.setTransform(identity);
                        g2d.setClip(pixelx1, pixely1, pixelw, pixelh);
                        AffineTransform stx = new AffineTransform();
                        stx.translate(pixelx1, pixely1);
                        stx.scale(scaleX, scaleY);
                        g2d.setTransform(stx);
                        if (translucent) {
                            g2d.setComposite(AlphaComposite.Src);
                        }
                        g2d.drawImage(image, 0, 0, c);
                        if (translucent) {
                            g2d.setComposite(oldComposite);
                        }
                        g2d.setTransform(tx);
                    }
                }
            }
            finally {
                osg.dispose();
            }
        }

        private Image getValidImage(Image image) {
            if (image != null && image.getWidth(null) > 0 && image.getHeight(null) > 0) {
                return image;
            }
            return null;
        }

        protected void repaintRoot(JComponent root) {
            assert (this.repaintManager.repaintRoot == null);
            if (this.repaintManager.painting) {
                this.repaintManager.repaintRoot = root;
            } else {
                root.repaint();
            }
        }

        protected boolean isRepaintingRoot() {
            return this.isRepaintingRoot;
        }

        protected void dispose() {
        }

        private boolean isPixelsCopying(JComponent c, Graphics g) {
            AffineTransform tx = PaintManager.getTransform(g);
            GraphicsConfiguration gc = c.getGraphicsConfiguration();
            if (tx == null || gc == null || !SwingUtilities2.isFloatingPointScale(tx)) {
                return false;
            }
            AffineTransform gcTx = gc.getDefaultTransform();
            return gcTx.getScaleX() == tx.getScaleX() && gcTx.getScaleY() == tx.getScaleY();
        }

        private static AffineTransform getTransform(Graphics g) {
            if (g instanceof SunGraphics2D) {
                return ((SunGraphics2D)g).transform;
            }
            if (g instanceof Graphics2D) {
                return ((Graphics2D)g).getTransform();
            }
            return null;
        }
    }

    private static final class DisplayChangedHandler
    implements DisplayChangedListener {
        private DisplayChangedHandler() {
        }

        @Override
        public void displayChanged() {
            DisplayChangedHandler.scheduleDisplayChanges();
        }

        @Override
        public void paletteChanged() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static void scheduleDisplayChanges() {
            Iterator<AppContext> iterator = AppContext.getAppContexts().iterator();
            while (iterator.hasNext()) {
                AppContext context;
                AppContext appContext = context = iterator.next();
                synchronized (appContext) {
                    EventQueue eventQueue;
                    if (!context.isDisposed() && (eventQueue = (EventQueue)context.get(AppContext.EVENT_QUEUE_KEY)) != null) {
                        eventQueue.postEvent(new InvocationEvent((Object)Toolkit.getDefaultToolkit(), new DisplayChangedRunnable()));
                    }
                }
            }
        }
    }

    private static final class DisplayChangedRunnable
    implements Runnable {
        private DisplayChangedRunnable() {
        }

        @Override
        public void run() {
            RepaintManager.currentManager((JComponent)null).displayChanged();
        }
    }
}

