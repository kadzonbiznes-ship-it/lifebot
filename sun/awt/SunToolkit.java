/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Label;
import java.awt.List;
import java.awt.MenuComponent;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.SystemTray;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.MultiResolutionImage;
import java.awt.image.WritableRaster;
import java.awt.peer.FramePeer;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.awt.peer.SystemTrayPeer;
import java.awt.peer.TrayIconPeer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.AccessController;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import sun.awt.AWTAccessor;
import sun.awt.AWTAutoShutdown;
import sun.awt.AWTPermissions;
import sun.awt.AppContext;
import sun.awt.ComponentFactory;
import sun.awt.DebugSettings;
import sun.awt.InputMethodSupport;
import sun.awt.KeyboardFocusManagerPeerProvider;
import sun.awt.LightweightFrame;
import sun.awt.ModalityEvent;
import sun.awt.ModalityListener;
import sun.awt.PeerEvent;
import sun.awt.PostEventQueue;
import sun.awt.SoftCache;
import sun.awt.TimedWindowEvent;
import sun.awt.WeakIdentityHashMap;
import sun.awt.im.InputContext;
import sun.awt.im.SimpleInputMethodWindow;
import sun.awt.image.ByteArrayImageSource;
import sun.awt.image.FileImageSource;
import sun.awt.image.ImageRepresentation;
import sun.awt.image.MultiResolutionToolkitImage;
import sun.awt.image.ToolkitImage;
import sun.awt.image.URLImageSource;
import sun.font.FontDesignMetrics;
import sun.net.util.URLUtil;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetPropertyAction;
import sun.util.logging.PlatformLogger;

public abstract class SunToolkit
extends Toolkit
implements ComponentFactory,
InputMethodSupport,
KeyboardFocusManagerPeerProvider {
    public static final int GRAB_EVENT_MASK = Integer.MIN_VALUE;
    private static final String POST_EVENT_QUEUE_KEY = "PostEventQueue";
    protected static int numberOfButtons;
    public static final int MAX_BUTTONS_SUPPORTED = 20;
    private static final ReentrantLock AWT_LOCK;
    private static final Condition AWT_LOCK_COND;
    private static final Map<Object, AppContext> appContextMap;
    static final SoftCache fileImgCache;
    static final SoftCache urlImgCache;
    private static Locale startupLocale;
    private static Dialog.ModalExclusionType DEFAULT_MODAL_EXCLUSION_TYPE;
    private ModalityListenerList modalityListeners = new ModalityListenerList();
    public static final int DEFAULT_WAIT_TIME = 10000;
    private static final int MAX_ITERS = 100;
    private static final int MIN_ITERS = 1;
    private static final int MINIMAL_DELAY = 5;
    private final Object waitLock = new Object();
    private static boolean touchKeyboardAutoShowIsEnabled;
    private static boolean checkedSystemAAFontSettings;
    private static boolean useSystemAAFontSettings;
    private static boolean lastExtraCondition;
    private static RenderingHints desktopFontHints;
    public static final String DESKTOPFONTHINTS = "awt.font.desktophints";
    private static Boolean sunAwtDisableMixing;
    private static final Object DEACTIVATION_TIMES_MAP_KEY;

    private static void initStatic() {
        if (AccessController.doPrivileged(new GetBooleanAction("sun.awt.nativedebug")).booleanValue()) {
            DebugSettings.init();
        }
        touchKeyboardAutoShowIsEnabled = Boolean.parseBoolean(GetPropertyAction.privilegedGetProperty("awt.touchKeyboardAutoShowIsEnabled", "true"));
    }

    private static void initEQ(AppContext appContext) {
        EventQueue eventQueue = new EventQueue();
        appContext.put(AppContext.EVENT_QUEUE_KEY, eventQueue);
        PostEventQueue postEventQueue = new PostEventQueue(eventQueue);
        appContext.put(POST_EVENT_QUEUE_KEY, postEventQueue);
    }

    public boolean useBufferPerWindow() {
        return false;
    }

    public abstract FramePeer createLightweightFrame(LightweightFrame var1) throws HeadlessException;

    public abstract TrayIconPeer createTrayIcon(TrayIcon var1) throws HeadlessException, AWTException;

    public abstract SystemTrayPeer createSystemTray(SystemTray var1);

    public abstract boolean isTraySupported();

    @Override
    public abstract KeyboardFocusManagerPeer getKeyboardFocusManagerPeer() throws HeadlessException;

    public static final void awtLock() {
        AWT_LOCK.lock();
    }

    public static final boolean awtTryLock() {
        return AWT_LOCK.tryLock();
    }

    public static final void awtUnlock() {
        AWT_LOCK.unlock();
    }

    public static final void awtLockWait() throws InterruptedException {
        AWT_LOCK_COND.await();
    }

    public static final void awtLockWait(long timeout) throws InterruptedException {
        AWT_LOCK_COND.await(timeout, TimeUnit.MILLISECONDS);
    }

    public static final void awtLockNotify() {
        AWT_LOCK_COND.signal();
    }

    public static final void awtLockNotifyAll() {
        AWT_LOCK_COND.signalAll();
    }

    public static final boolean isAWTLockHeldByCurrentThread() {
        return AWT_LOCK.isHeldByCurrentThread();
    }

    public static AppContext createNewAppContext() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        return SunToolkit.createNewAppContext(threadGroup);
    }

    static final AppContext createNewAppContext(ThreadGroup threadGroup) {
        AppContext appContext = new AppContext(threadGroup);
        SunToolkit.initEQ(appContext);
        return appContext;
    }

    static void wakeupEventQueue(EventQueue q, boolean isShutdown) {
        AWTAccessor.getEventQueueAccessor().wakeup(q, isShutdown);
    }

    protected static Object targetToPeer(Object target) {
        if (target != null && !GraphicsEnvironment.isHeadless()) {
            return AWTAutoShutdown.getInstance().getPeer(target);
        }
        return null;
    }

    protected static void targetCreatedPeer(Object target, Object peer) {
        if (target != null && peer != null && !GraphicsEnvironment.isHeadless()) {
            AWTAutoShutdown.getInstance().registerPeer(target, peer);
        }
    }

    protected static void targetDisposedPeer(Object target, Object peer) {
        if (target != null && peer != null && !GraphicsEnvironment.isHeadless()) {
            AWTAutoShutdown.getInstance().unregisterPeer(target, peer);
        }
    }

    private static boolean setAppContext(Object target, AppContext context) {
        if (target instanceof Component) {
            AWTAccessor.getComponentAccessor().setAppContext((Component)target, context);
        } else if (target instanceof MenuComponent) {
            AWTAccessor.getMenuComponentAccessor().setAppContext((MenuComponent)target, context);
        } else {
            return false;
        }
        return true;
    }

    private static AppContext getAppContext(Object target) {
        if (target instanceof Component) {
            return AWTAccessor.getComponentAccessor().getAppContext((Component)target);
        }
        if (target instanceof MenuComponent) {
            return AWTAccessor.getMenuComponentAccessor().getAppContext((MenuComponent)target);
        }
        return null;
    }

    public static AppContext targetToAppContext(Object target) {
        if (target == null) {
            return null;
        }
        AppContext context = SunToolkit.getAppContext(target);
        if (context == null) {
            context = appContextMap.get(target);
        }
        return context;
    }

    public static void setLWRequestStatus(Window changed, boolean status) {
        AWTAccessor.getWindowAccessor().setLWRequestStatus(changed, status);
    }

    public static void checkAndSetPolicy(Container cont) {
        FocusTraversalPolicy defaultPolicy = KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalPolicy();
        cont.setFocusTraversalPolicy(defaultPolicy);
    }

    public static void insertTargetMapping(Object target, AppContext appContext) {
        if (!SunToolkit.setAppContext(target, appContext)) {
            appContextMap.put(target, appContext);
        }
    }

    public static void postEvent(AppContext appContext, AWTEvent event) {
        AWTEvent nested;
        if (event == null) {
            throw new NullPointerException();
        }
        AWTAccessor.SequencedEventAccessor sea = AWTAccessor.getSequencedEventAccessor();
        if (sea != null && sea.isSequencedEvent(event) && (nested = sea.getNested(event)).getID() == 208 && nested instanceof TimedWindowEvent) {
            TimedWindowEvent twe = (TimedWindowEvent)nested;
            ((SunToolkit)Toolkit.getDefaultToolkit()).setWindowDeactivationTime((Window)twe.getSource(), twe.getWhen());
        }
        SunToolkit.setSystemGenerated(event);
        AppContext eventContext = SunToolkit.targetToAppContext(event.getSource());
        if (eventContext != null && !eventContext.equals(appContext)) {
            throw new RuntimeException("Event posted on wrong app context : " + String.valueOf(event));
        }
        PostEventQueue postEventQueue = (PostEventQueue)appContext.get(POST_EVENT_QUEUE_KEY);
        if (postEventQueue != null) {
            postEventQueue.postEvent(event);
        }
    }

    public static void postPriorityEvent(final AWTEvent e) {
        PeerEvent pe = new PeerEvent(Toolkit.getDefaultToolkit(), new Runnable(){

            @Override
            public void run() {
                AWTAccessor.getAWTEventAccessor().setPosted(e);
                ((Component)e.getSource()).dispatchEvent(e);
            }
        }, 2L);
        SunToolkit.postEvent(SunToolkit.targetToAppContext(e.getSource()), pe);
    }

    public static void flushPendingEvents() {
        AppContext appContext = AppContext.getAppContext();
        SunToolkit.flushPendingEvents(appContext);
    }

    public static void flushPendingEvents(AppContext appContext) {
        PostEventQueue postEventQueue = (PostEventQueue)appContext.get(POST_EVENT_QUEUE_KEY);
        if (postEventQueue != null) {
            postEventQueue.flush();
        }
    }

    public static void executeOnEventHandlerThread(Object target, Runnable runnable) {
        SunToolkit.executeOnEventHandlerThread(new PeerEvent(target, runnable, 1L));
    }

    public static void executeOnEventHandlerThread(Object target, Runnable runnable, final long when) {
        SunToolkit.executeOnEventHandlerThread(new PeerEvent(target, runnable, 1L){

            @Override
            public long getWhen() {
                return when;
            }
        });
    }

    public static void executeOnEventHandlerThread(PeerEvent peerEvent) {
        SunToolkit.postEvent(SunToolkit.targetToAppContext(peerEvent.getSource()), peerEvent);
    }

    public static void invokeLaterOnAppContext(AppContext appContext, Runnable dispatcher) {
        SunToolkit.postEvent(appContext, new PeerEvent(Toolkit.getDefaultToolkit(), dispatcher, 1L));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void executeOnEDTAndWait(Object target, Runnable runnable) throws InterruptedException, InvocationTargetException {
        if (EventQueue.isDispatchThread()) {
            throw new Error("Cannot call executeOnEDTAndWait from any event dispatcher thread");
        }
        class AWTInvocationLock {
            AWTInvocationLock() {
            }
        }
        AWTInvocationLock lock = new AWTInvocationLock();
        PeerEvent event = new PeerEvent(target, runnable, lock, true, 1L);
        AWTInvocationLock aWTInvocationLock = lock;
        synchronized (aWTInvocationLock) {
            SunToolkit.executeOnEventHandlerThread(event);
            while (!event.isDispatched()) {
                lock.wait();
            }
        }
        Throwable eventThrowable = event.getThrowable();
        if (eventThrowable != null) {
            throw new InvocationTargetException(eventThrowable);
        }
    }

    public static boolean isDispatchThreadForAppContext(Object target) {
        AppContext appContext = SunToolkit.targetToAppContext(target);
        EventQueue eq = (EventQueue)appContext.get(AppContext.EVENT_QUEUE_KEY);
        AWTAccessor.EventQueueAccessor accessor = AWTAccessor.getEventQueueAccessor();
        return accessor.isDispatchThreadImpl(eq);
    }

    @Override
    public Dimension getScreenSize() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().getSize();
    }

    @Override
    public ColorModel getColorModel() throws HeadlessException {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getColorModel();
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        return FontDesignMetrics.getMetrics(font);
    }

    @Override
    public String[] getFontList() {
        String[] hardwiredFontList = new String[]{"Dialog", "SansSerif", "Serif", "Monospaced", "DialogInput"};
        return hardwiredFontList;
    }

    public void disableBackgroundErase(Canvas canvas) {
        this.disableBackgroundEraseImpl(canvas);
    }

    public void disableBackgroundErase(Component component) {
        this.disableBackgroundEraseImpl(component);
    }

    private void disableBackgroundEraseImpl(Component component) {
        AWTAccessor.getComponentAccessor().setBackgroundEraseDisabled(component, true);
    }

    public static boolean getSunAwtNoerasebackground() {
        return AccessController.doPrivileged(new GetBooleanAction("sun.awt.noerasebackground"));
    }

    public static boolean getSunAwtErasebackgroundonresize() {
        return AccessController.doPrivileged(new GetBooleanAction("sun.awt.erasebackgroundonresize"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Image getImageFromHash(Toolkit tk, URL url) {
        SunToolkit.checkPermissions(url);
        SoftCache softCache = urlImgCache;
        synchronized (softCache) {
            String key = url.toString();
            Image img = (Image)urlImgCache.get(key);
            if (img == null) {
                try {
                    img = tk.createImage(new URLImageSource(url));
                    urlImgCache.put(key, img);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            return img;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Image getImageFromHash(Toolkit tk, String filename) {
        SunToolkit.checkPermissions(filename);
        SoftCache softCache = fileImgCache;
        synchronized (softCache) {
            Image img = (Image)fileImgCache.get(filename);
            if (img == null) {
                try {
                    img = tk.createImage(new FileImageSource(filename));
                    fileImgCache.put(filename, img);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            return img;
        }
    }

    @Override
    public Image getImage(String filename) {
        return SunToolkit.getImageFromHash((Toolkit)this, filename);
    }

    @Override
    public Image getImage(URL url) {
        return SunToolkit.getImageFromHash((Toolkit)this, url);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Image getImageWithResolutionVariant(String fileName, String resolutionVariantName) {
        SoftCache softCache = fileImgCache;
        synchronized (softCache) {
            Image image = SunToolkit.getImageFromHash((Toolkit)this, fileName);
            if (image instanceof MultiResolutionImage) {
                return image;
            }
            Image resolutionVariant = SunToolkit.getImageFromHash((Toolkit)this, resolutionVariantName);
            image = SunToolkit.createImageWithResolutionVariant(image, resolutionVariant);
            fileImgCache.put(fileName, image);
            return image;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Image getImageWithResolutionVariant(URL url, URL resolutionVariantURL) {
        SoftCache softCache = urlImgCache;
        synchronized (softCache) {
            Image image = SunToolkit.getImageFromHash((Toolkit)this, url);
            if (image instanceof MultiResolutionImage) {
                return image;
            }
            Image resolutionVariant = SunToolkit.getImageFromHash((Toolkit)this, resolutionVariantURL);
            image = SunToolkit.createImageWithResolutionVariant(image, resolutionVariant);
            String key = url.toString();
            urlImgCache.put(key, image);
            return image;
        }
    }

    @Override
    public Image createImage(String filename) {
        SunToolkit.checkPermissions(filename);
        return this.createImage(new FileImageSource(filename));
    }

    @Override
    public Image createImage(URL url) {
        SunToolkit.checkPermissions(url);
        return this.createImage(new URLImageSource(url));
    }

    @Override
    public Image createImage(byte[] data, int offset, int length) {
        return this.createImage(new ByteArrayImageSource(data, offset, length));
    }

    @Override
    public Image createImage(ImageProducer producer) {
        return new ToolkitImage(producer);
    }

    public static Image createImageWithResolutionVariant(Image image, Image resolutionVariant) {
        return new MultiResolutionToolkitImage(image, resolutionVariant);
    }

    @Override
    public int checkImage(Image img, int w, int h, ImageObserver o) {
        if (!(img instanceof ToolkitImage)) {
            return 32;
        }
        ToolkitImage tkimg = (ToolkitImage)img;
        int repbits = w == 0 || h == 0 ? 32 : tkimg.getImageRep().check(o);
        return (tkimg.check(o) | repbits) & this.checkResolutionVariant(img, w, h, o);
    }

    @Override
    public boolean prepareImage(Image img, int w, int h, ImageObserver o) {
        if (w == 0 || h == 0) {
            return true;
        }
        if (!(img instanceof ToolkitImage)) {
            return true;
        }
        ToolkitImage tkimg = (ToolkitImage)img;
        if (tkimg.hasError()) {
            if (o != null) {
                o.imageUpdate(img, 192, -1, -1, -1, -1);
            }
            return false;
        }
        ImageRepresentation ir = tkimg.getImageRep();
        return ir.prepare(o) & this.prepareResolutionVariant(img, w, h, o);
    }

    private int checkResolutionVariant(Image img, int w, int h, ImageObserver o) {
        ToolkitImage rvImage = SunToolkit.getResolutionVariant(img);
        int rvw = SunToolkit.getRVSize(w);
        int rvh = SunToolkit.getRVSize(h);
        return rvImage == null || rvImage.hasError() ? 65535 : this.checkImage(rvImage, rvw, rvh, MultiResolutionToolkitImage.getResolutionVariantObserver(img, o, w, h, rvw, rvh, true));
    }

    private boolean prepareResolutionVariant(Image img, int w, int h, ImageObserver o) {
        ToolkitImage rvImage = SunToolkit.getResolutionVariant(img);
        int rvw = SunToolkit.getRVSize(w);
        int rvh = SunToolkit.getRVSize(h);
        return rvImage == null || rvImage.hasError() || this.prepareImage(rvImage, rvw, rvh, MultiResolutionToolkitImage.getResolutionVariantObserver(img, o, w, h, rvw, rvh, true));
    }

    private static int getRVSize(int size) {
        return size == -1 ? -1 : 2 * size;
    }

    private static ToolkitImage getResolutionVariant(Image image) {
        Image resolutionVariant;
        if (image instanceof MultiResolutionToolkitImage && (resolutionVariant = ((MultiResolutionToolkitImage)image).getResolutionVariant()) instanceof ToolkitImage) {
            return (ToolkitImage)resolutionVariant;
        }
        return null;
    }

    protected static boolean imageCached(String fileName) {
        return fileImgCache.containsKey(fileName);
    }

    protected static boolean imageCached(URL url) {
        String key = url.toString();
        return urlImgCache.containsKey(key);
    }

    protected static boolean imageExists(String filename) {
        if (filename != null) {
            SunToolkit.checkPermissions(filename);
            return new File(filename).exists();
        }
        return false;
    }

    protected static boolean imageExists(URL url) {
        if (url != null) {
            boolean bl;
            block9: {
                SunToolkit.checkPermissions(url);
                InputStream is = url.openStream();
                try {
                    bl = true;
                    if (is == null) break block9;
                }
                catch (Throwable throwable) {
                    try {
                        if (is != null) {
                            try {
                                is.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (IOException e) {
                        return false;
                    }
                }
                is.close();
            }
            return bl;
        }
        return false;
    }

    private static void checkPermissions(String filename) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(filename);
        }
    }

    private static void checkPermissions(URL url) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                Permission perm = URLUtil.getConnectPermission(url);
                if (perm != null) {
                    sm.checkPermission(perm);
                }
            }
            catch (IOException ioe) {
                sm.checkConnect(url.getHost(), url.getPort());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static BufferedImage getScaledIconImage(java.util.List<Image> imageList, int width, int height) {
        if (width == 0 || height == 0) {
            return null;
        }
        ArrayList<Image> multiResAndnormalImages = new ArrayList<Image>(imageList.size());
        for (Image image : imageList) {
            if (image instanceof MultiResolutionImage) {
                Image im = ((MultiResolutionImage)((Object)image)).getResolutionVariant(width, height);
                multiResAndnormalImages.add(im);
                continue;
            }
            multiResAndnormalImages.add(image);
        }
        Image bestImage = null;
        int bestWidth = 0;
        int bestHeight = 0;
        double bestSimilarity = 3.0;
        double bestScaleFactor = 0.0;
        for (Image im : multiResAndnormalImages) {
            int ih;
            int iw;
            if (im == null) continue;
            if (im instanceof ToolkitImage) {
                ImageRepresentation ir = ((ToolkitImage)im).getImageRep();
                ir.reconstruct(32);
            }
            try {
                iw = im.getWidth(null);
                ih = im.getHeight(null);
            }
            catch (Exception e) {
                continue;
            }
            if (iw <= 0 || ih <= 0) continue;
            double scaleFactor = Math.min((double)width / (double)iw, (double)height / (double)ih);
            int adjw = 0;
            int adjh = 0;
            double scaleMeasure = 1.0;
            if (scaleFactor >= 2.0) {
                scaleFactor = Math.floor(scaleFactor);
                adjw = iw * (int)scaleFactor;
                adjh = ih * (int)scaleFactor;
                scaleMeasure = 1.0 - 0.5 / scaleFactor;
            } else if (scaleFactor >= 1.0) {
                scaleFactor = 1.0;
                adjw = iw;
                adjh = ih;
                scaleMeasure = 0.0;
            } else if (scaleFactor >= 0.75) {
                scaleFactor = 0.75;
                adjw = iw * 3 / 4;
                adjh = ih * 3 / 4;
                scaleMeasure = 0.3;
            } else if (scaleFactor >= 0.6666) {
                scaleFactor = 0.6666;
                adjw = iw * 2 / 3;
                adjh = ih * 2 / 3;
                scaleMeasure = 0.33;
            } else {
                double scaleDivider = Math.ceil(1.0 / scaleFactor);
                scaleFactor = 1.0 / scaleDivider;
                adjw = (int)Math.round((double)iw / scaleDivider);
                adjh = (int)Math.round((double)ih / scaleDivider);
                scaleMeasure = 1.0 - 1.0 / scaleDivider;
            }
            double similarity = ((double)width - (double)adjw) / (double)width + ((double)height - (double)adjh) / (double)height + scaleMeasure;
            if (similarity < bestSimilarity) {
                bestSimilarity = similarity;
                bestScaleFactor = scaleFactor;
                bestImage = im;
                bestWidth = adjw;
                bestHeight = adjh;
            }
            if (similarity != 0.0) continue;
            break;
        }
        if (bestImage == null) {
            return null;
        }
        BufferedImage bimage = new BufferedImage(width, height, 2);
        Graphics2D g = bimage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        try {
            int x = (width - bestWidth) / 2;
            int y = (height - bestHeight) / 2;
            g.drawImage(bestImage, x, y, bestWidth, bestHeight, null);
        }
        finally {
            g.dispose();
        }
        return bimage;
    }

    public static DataBufferInt getScaledIconData(java.util.List<Image> imageList, int width, int height) {
        BufferedImage bimage = SunToolkit.getScaledIconImage(imageList, width, height);
        if (bimage == null) {
            return null;
        }
        WritableRaster raster = bimage.getRaster();
        DataBuffer buffer = raster.getDataBuffer();
        return (DataBufferInt)buffer;
    }

    @Override
    protected EventQueue getSystemEventQueueImpl() {
        return SunToolkit.getSystemEventQueueImplPP();
    }

    static EventQueue getSystemEventQueueImplPP() {
        return SunToolkit.getSystemEventQueueImplPP(AppContext.getAppContext());
    }

    public static EventQueue getSystemEventQueueImplPP(AppContext appContext) {
        EventQueue theEventQueue = (EventQueue)appContext.get(AppContext.EVENT_QUEUE_KEY);
        return theEventQueue;
    }

    public static Container getNativeContainer(Component c) {
        return Toolkit.getNativeContainer(c);
    }

    public static Component getHeavyweightComponent(Component c) {
        while (c != null && AWTAccessor.getComponentAccessor().isLightweight(c)) {
            c = AWTAccessor.getComponentAccessor().getParent(c);
        }
        return c;
    }

    public int getFocusAcceleratorKeyMask() {
        return 8;
    }

    public boolean isPrintableCharacterModifiersMask(int mods) {
        return (mods & 8) == (mods & 2);
    }

    public boolean canPopupOverlapTaskBar() {
        boolean result = true;
        try {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(AWTPermissions.SET_WINDOW_ALWAYS_ON_TOP_PERMISSION);
            }
        }
        catch (SecurityException se) {
            result = false;
        }
        return result;
    }

    @Override
    public Window createInputMethodWindow(String title, InputContext context) {
        return new SimpleInputMethodWindow(title, context);
    }

    @Override
    public boolean enableInputMethodsForTextComponent() {
        return false;
    }

    public static Locale getStartupLocale() {
        if (startupLocale == null) {
            String variant;
            String country;
            String language = AccessController.doPrivileged(new GetPropertyAction("user.language", "en"));
            String region = AccessController.doPrivileged(new GetPropertyAction("user.region"));
            if (region != null) {
                int i = region.indexOf(95);
                if (i >= 0) {
                    country = region.substring(0, i);
                    variant = region.substring(i + 1);
                } else {
                    country = region;
                    variant = "";
                }
            } else {
                country = AccessController.doPrivileged(new GetPropertyAction("user.country", ""));
                variant = AccessController.doPrivileged(new GetPropertyAction("user.variant", ""));
            }
            startupLocale = Locale.of(language, country, variant);
        }
        return startupLocale;
    }

    @Override
    public Locale getDefaultKeyboardLocale() {
        return SunToolkit.getStartupLocale();
    }

    public static boolean needsXEmbed() {
        String noxembed = AccessController.doPrivileged(new GetPropertyAction("sun.awt.noxembed", "false"));
        if ("true".equals(noxembed)) {
            return false;
        }
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk instanceof SunToolkit) {
            return ((SunToolkit)tk).needsXEmbedImpl();
        }
        return false;
    }

    protected boolean needsXEmbedImpl() {
        return false;
    }

    protected final boolean isXEmbedServerRequested() {
        return AccessController.doPrivileged(new GetBooleanAction("sun.awt.xembedserver"));
    }

    public static boolean isModalExcludedSupported() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        return tk.isModalExclusionTypeSupported(DEFAULT_MODAL_EXCLUSION_TYPE);
    }

    protected boolean isModalExcludedSupportedImpl() {
        return false;
    }

    public static void setModalExcluded(Window window) {
        if (DEFAULT_MODAL_EXCLUSION_TYPE == null) {
            DEFAULT_MODAL_EXCLUSION_TYPE = Dialog.ModalExclusionType.APPLICATION_EXCLUDE;
        }
        window.setModalExclusionType(DEFAULT_MODAL_EXCLUSION_TYPE);
    }

    public static boolean isModalExcluded(Window window) {
        if (DEFAULT_MODAL_EXCLUSION_TYPE == null) {
            DEFAULT_MODAL_EXCLUSION_TYPE = Dialog.ModalExclusionType.APPLICATION_EXCLUDE;
        }
        return window.getModalExclusionType().compareTo(DEFAULT_MODAL_EXCLUSION_TYPE) >= 0;
    }

    @Override
    public boolean isModalityTypeSupported(Dialog.ModalityType modalityType) {
        return modalityType == Dialog.ModalityType.MODELESS || modalityType == Dialog.ModalityType.APPLICATION_MODAL;
    }

    @Override
    public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType exclusionType) {
        return exclusionType == Dialog.ModalExclusionType.NO_EXCLUDE;
    }

    public void addModalityListener(ModalityListener listener) {
        this.modalityListeners.add(listener);
    }

    public void removeModalityListener(ModalityListener listener) {
        this.modalityListeners.remove(listener);
    }

    public void notifyModalityPushed(Dialog dialog) {
        this.notifyModalityChange(1300, dialog);
    }

    public void notifyModalityPopped(Dialog dialog) {
        this.notifyModalityChange(1301, dialog);
    }

    final void notifyModalityChange(int id, Dialog source) {
        ModalityEvent ev = new ModalityEvent(source, this.modalityListeners, id);
        ev.dispatch();
    }

    public static boolean isLightweightOrUnknown(Component comp) {
        if (comp.isLightweight() || !(SunToolkit.getDefaultToolkit() instanceof SunToolkit)) {
            return true;
        }
        return !(comp instanceof Button) && !(comp instanceof Canvas) && !(comp instanceof Checkbox) && !(comp instanceof Choice) && !(comp instanceof Label) && !(comp instanceof List) && !(comp instanceof Panel) && !(comp instanceof Scrollbar) && !(comp instanceof ScrollPane) && !(comp instanceof TextArea) && !(comp instanceof TextField) && !(comp instanceof Window);
    }

    public void realSync() {
        this.realSync(10000L);
    }

    public void realSync(long timeout) {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalThreadException("The SunToolkit.realSync() method cannot be used on the event dispatch thread (EDT).");
        }
        try {
            EventQueue.invokeAndWait(() -> {});
        }
        catch (InterruptedException | InvocationTargetException exception) {
            // empty catch block
        }
        int bigLoop = 0;
        long end = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) + timeout;
        do {
            int iters;
            if (this.timeout(end) < 0L) {
                return;
            }
            this.sync();
            for (iters = 0; iters < 1; ++iters) {
                this.syncNativeQueue(this.timeout(end));
            }
            while (this.syncNativeQueue(this.timeout(end)) && iters < 100) {
                ++iters;
            }
            for (iters = 0; iters < 1; ++iters) {
                this.waitForIdle(this.timeout(end));
            }
            while (this.waitForIdle(end) && iters < 100) {
                ++iters;
            }
        } while ((this.syncNativeQueue(this.timeout(end)) || this.waitForIdle(end)) && ++bigLoop < 100);
    }

    protected long timeout(long end) {
        return end - TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    protected abstract boolean syncNativeQueue(long var1);

    private boolean isEQEmpty() {
        EventQueue queue = this.getSystemEventQueueImpl();
        return AWTAccessor.getEventQueueAccessor().noEvents(queue);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final boolean waitForIdle(final long end) {
        boolean queueWasEmpty;
        if (this.timeout(end) <= 0L) {
            return false;
        }
        SunToolkit.flushPendingEvents();
        final AtomicBoolean queueEmpty = new AtomicBoolean();
        final AtomicBoolean eventDispatched = new AtomicBoolean();
        Object object = this.waitLock;
        synchronized (object) {
            queueWasEmpty = this.isEQEmpty();
            SunToolkit.postEvent(AppContext.getAppContext(), new PeerEvent(this, this.getSystemEventQueueImpl(), null, 4L){
                final /* synthetic */ SunToolkit this$0;
                {
                    this.this$0 = this$0;
                    super(source, runnable, flags);
                }

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void dispatch() {
                    int iters;
                    for (iters = 0; iters < 1; ++iters) {
                        this.this$0.syncNativeQueue(this.this$0.timeout(end));
                    }
                    while (this.this$0.syncNativeQueue(this.this$0.timeout(end)) && iters < 100) {
                        ++iters;
                    }
                    SunToolkit.flushPendingEvents();
                    Object object = this.this$0.waitLock;
                    synchronized (object) {
                        queueEmpty.set(this.this$0.isEQEmpty());
                        eventDispatched.set(true);
                        this.this$0.waitLock.notifyAll();
                    }
                }
            });
            try {
                while (!eventDispatched.get() && this.timeout(end) > 0L) {
                    this.waitLock.wait(this.timeout(end));
                }
            }
            catch (InterruptedException ie) {
                return false;
            }
        }
        try {
            Thread.sleep(5L);
        }
        catch (InterruptedException ie) {
            throw new RuntimeException("Interrupted");
        }
        SunToolkit.flushPendingEvents();
        object = this.waitLock;
        synchronized (object) {
            return !queueEmpty.get() || !this.isEQEmpty() || !queueWasEmpty;
        }
    }

    public abstract void grab(Window var1);

    public abstract void ungrab(Window var1);

    public void showOrHideTouchKeyboard(Component comp, AWTEvent e) {
    }

    public static boolean isTouchKeyboardAutoShowEnabled() {
        return touchKeyboardAutoShowIsEnabled;
    }

    public static native void closeSplashScreen();

    private void fireDesktopFontPropertyChanges() {
        this.setDesktopProperty(DESKTOPFONTHINTS, SunToolkit.getDesktopFontHints());
    }

    public static void setAAFontSettingsCondition(boolean extraCondition) {
        if (extraCondition != lastExtraCondition) {
            lastExtraCondition = extraCondition;
            if (checkedSystemAAFontSettings) {
                checkedSystemAAFontSettings = false;
                Toolkit tk = Toolkit.getDefaultToolkit();
                if (tk instanceof SunToolkit) {
                    ((SunToolkit)tk).fireDesktopFontPropertyChanges();
                }
            }
        }
    }

    private static RenderingHints getDesktopAAHintsByName(String hintname) {
        Object aaHint = null;
        if ((hintname = hintname.toLowerCase(Locale.ENGLISH)).equals("on")) {
            aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
        } else if (hintname.equals("gasp")) {
            aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
        } else if (hintname.equals("lcd") || hintname.equals("lcd_hrgb")) {
            aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
        } else if (hintname.equals("lcd_hbgr")) {
            aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR;
        } else if (hintname.equals("lcd_vrgb")) {
            aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB;
        } else if (hintname.equals("lcd_vbgr")) {
            aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR;
        }
        if (aaHint != null) {
            RenderingHints map = new RenderingHints(null);
            map.put(RenderingHints.KEY_TEXT_ANTIALIASING, aaHint);
            return map;
        }
        return null;
    }

    private static boolean useSystemAAFontSettings() {
        if (!checkedSystemAAFontSettings) {
            useSystemAAFontSettings = true;
            String systemAAFonts = null;
            Toolkit tk = Toolkit.getDefaultToolkit();
            if (tk instanceof SunToolkit) {
                systemAAFonts = AccessController.doPrivileged(new GetPropertyAction("awt.useSystemAAFontSettings"));
            }
            if (systemAAFonts != null && !(useSystemAAFontSettings = Boolean.parseBoolean(systemAAFonts))) {
                desktopFontHints = SunToolkit.getDesktopAAHintsByName(systemAAFonts);
            }
            if (useSystemAAFontSettings) {
                useSystemAAFontSettings = lastExtraCondition;
            }
            checkedSystemAAFontSettings = true;
        }
        return useSystemAAFontSettings;
    }

    protected RenderingHints getDesktopAAHints() {
        return null;
    }

    public static RenderingHints getDesktopFontHints() {
        if (SunToolkit.useSystemAAFontSettings()) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            if (tk instanceof SunToolkit) {
                RenderingHints map = ((SunToolkit)tk).getDesktopAAHints();
                return map;
            }
            return null;
        }
        if (desktopFontHints != null) {
            return (RenderingHints)desktopFontHints.clone();
        }
        return null;
    }

    public abstract boolean isDesktopSupported();

    public abstract boolean isTaskbarSupported();

    public static synchronized void consumeNextKeyTyped(KeyEvent keyEvent) {
        try {
            AWTAccessor.getDefaultKeyboardFocusManagerAccessor().consumeNextKeyTyped((DefaultKeyboardFocusManager)KeyboardFocusManager.getCurrentKeyboardFocusManager(), keyEvent);
        }
        catch (ClassCastException cce) {
            cce.printStackTrace();
        }
    }

    protected static void dumpPeers(PlatformLogger aLog) {
        AWTAutoShutdown.getInstance().dumpPeers(aLog);
    }

    public static Window getContainingWindow(Component comp) {
        while (comp != null && !(comp instanceof Window)) {
            comp = comp.getParent();
        }
        return (Window)comp;
    }

    public static synchronized boolean getSunAwtDisableMixing() {
        if (sunAwtDisableMixing == null) {
            sunAwtDisableMixing = AccessController.doPrivileged(new GetBooleanAction("sun.awt.disableMixing"));
        }
        return sunAwtDisableMixing;
    }

    public String getDesktop() {
        return null;
    }

    public boolean isNativeGTKAvailable() {
        return false;
    }

    public boolean isRunningOnWayland() {
        return false;
    }

    public void dismissPopupOnFocusLostIfNeeded(Window invoker) {
    }

    public void dismissPopupOnFocusLostIfNeededCleanUp(Window invoker) {
    }

    public synchronized void setWindowDeactivationTime(Window w, long time) {
        AppContext ctx = SunToolkit.getAppContext(w);
        if (ctx == null) {
            return;
        }
        WeakHashMap<Window, Long> map = (WeakHashMap<Window, Long>)ctx.get(DEACTIVATION_TIMES_MAP_KEY);
        if (map == null) {
            map = new WeakHashMap<Window, Long>();
            ctx.put(DEACTIVATION_TIMES_MAP_KEY, map);
        }
        map.put(w, time);
    }

    public synchronized long getWindowDeactivationTime(Window w) {
        AppContext ctx = SunToolkit.getAppContext(w);
        if (ctx == null) {
            return -1L;
        }
        WeakHashMap map = (WeakHashMap)ctx.get(DEACTIVATION_TIMES_MAP_KEY);
        if (map == null) {
            return -1L;
        }
        Long time = (Long)map.get(w);
        return time == null ? -1L : time;
    }

    public void updateScreenMenuBarUI() {
    }

    public boolean isWindowOpacitySupported() {
        return false;
    }

    public boolean isWindowShapingSupported() {
        return false;
    }

    public boolean isWindowTranslucencySupported() {
        return false;
    }

    public boolean isTranslucencyCapable(GraphicsConfiguration gc) {
        return false;
    }

    public boolean isSwingBackbufferTranslucencySupported() {
        return false;
    }

    public static boolean isContainingTopLevelOpaque(Component c) {
        Window w = SunToolkit.getContainingWindow(c);
        return w != null && w.isOpaque();
    }

    public static boolean isContainingTopLevelTranslucent(Component c) {
        Window w = SunToolkit.getContainingWindow(c);
        return w != null && w.getOpacity() < 1.0f;
    }

    public boolean needUpdateWindow() {
        return false;
    }

    public int getNumberOfButtons() {
        return 3;
    }

    public static boolean isInstanceOf(Object obj, String type) {
        if (obj == null) {
            return false;
        }
        if (type == null) {
            return false;
        }
        return SunToolkit.isInstanceOf(obj.getClass(), type);
    }

    private static boolean isInstanceOf(Class<?> cls, String type) {
        if (cls == null) {
            return false;
        }
        if (cls.getName().equals(type)) {
            return true;
        }
        for (Class<?> c : cls.getInterfaces()) {
            if (!c.getName().equals(type)) continue;
            return true;
        }
        return SunToolkit.isInstanceOf(cls.getSuperclass(), type);
    }

    protected static LightweightFrame getLightweightFrame(Component c) {
        while (c != null) {
            if (c instanceof LightweightFrame) {
                return (LightweightFrame)c;
            }
            if (c instanceof Window) {
                return null;
            }
            c = c.getParent();
        }
        return null;
    }

    public static void setSystemGenerated(AWTEvent e) {
        AWTAccessor.getAWTEventAccessor().setSystemGenerated(e);
    }

    public static boolean isSystemGenerated(AWTEvent e) {
        return AWTAccessor.getAWTEventAccessor().isSystemGenerated(e);
    }

    static {
        SunToolkit.initStatic();
        numberOfButtons = 0;
        AWT_LOCK = new ReentrantLock(AccessController.doPrivileged(new GetBooleanAction("awt.lock.fair")));
        AWT_LOCK_COND = AWT_LOCK.newCondition();
        appContextMap = Collections.synchronizedMap(new WeakIdentityHashMap());
        fileImgCache = new SoftCache();
        urlImgCache = new SoftCache();
        startupLocale = null;
        DEFAULT_MODAL_EXCLUSION_TYPE = null;
        lastExtraCondition = true;
        sunAwtDisableMixing = null;
        DEACTIVATION_TIMES_MAP_KEY = new Object();
    }

    static class ModalityListenerList
    implements ModalityListener {
        Vector<ModalityListener> listeners = new Vector();

        ModalityListenerList() {
        }

        void add(ModalityListener listener) {
            this.listeners.addElement(listener);
        }

        void remove(ModalityListener listener) {
            this.listeners.removeElement(listener);
        }

        @Override
        public void modalityPushed(ModalityEvent ev) {
            for (ModalityListener listener : this.listeners) {
                listener.modalityPushed(ev);
            }
        }

        @Override
        public void modalityPopped(ModalityEvent ev) {
            for (ModalityListener listener : this.listeners) {
                listener.modalityPopped(ev);
            }
        }
    }

    public static class IllegalThreadException
    extends RuntimeException {
        public IllegalThreadException(String msg) {
            super(msg);
        }

        public IllegalThreadException() {
        }
    }
}

