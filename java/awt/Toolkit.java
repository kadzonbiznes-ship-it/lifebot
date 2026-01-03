/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTError;
import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.JobAttributes;
import java.awt.PageAttributes;
import java.awt.Point;
import java.awt.PrintJob;
import java.awt.RenderingHints;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.event.AWTEventListener;
import java.awt.event.AWTEventListenerProxy;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.accessibility.AccessibilityProvider;
import sun.awt.AWTAccessor;
import sun.awt.AWTPermissions;
import sun.awt.AppContext;
import sun.awt.HeadlessToolkit;
import sun.awt.PeerEvent;
import sun.awt.PlatformGraphicsInfo;
import sun.awt.SunToolkit;
import sun.awt.UngrabEvent;

public abstract class Toolkit {
    private static Toolkit toolkit;
    private static String atNames;
    private static ResourceBundle resources;
    private static ResourceBundle platformResources;
    private static boolean loaded;
    protected final Map<String, Object> desktopProperties = new HashMap<String, Object>();
    protected final PropertyChangeSupport desktopPropsSupport = Toolkit.createPropertyChangeSupport(this);
    private static final int LONG_BITS = 64;
    private int[] calls = new int[64];
    private static volatile long enabledOnToolkitMask;
    private AWTEventListener eventListener = null;
    private WeakHashMap<AWTEventListener, SelectiveAWTEventListener> listener2SelectiveListener = new WeakHashMap();

    protected Toolkit() {
    }

    protected void loadSystemColors(int[] systemColors) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
    }

    public void setDynamicLayout(boolean dynamic) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        if (this != Toolkit.getDefaultToolkit()) {
            Toolkit.getDefaultToolkit().setDynamicLayout(dynamic);
        }
    }

    protected boolean isDynamicLayoutSet() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        if (this != Toolkit.getDefaultToolkit()) {
            return Toolkit.getDefaultToolkit().isDynamicLayoutSet();
        }
        return false;
    }

    public boolean isDynamicLayoutActive() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        if (this != Toolkit.getDefaultToolkit()) {
            return Toolkit.getDefaultToolkit().isDynamicLayoutActive();
        }
        return false;
    }

    public abstract Dimension getScreenSize() throws HeadlessException;

    public abstract int getScreenResolution() throws HeadlessException;

    public Insets getScreenInsets(GraphicsConfiguration gc) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        if (this != Toolkit.getDefaultToolkit()) {
            return Toolkit.getDefaultToolkit().getScreenInsets(gc);
        }
        return new Insets(0, 0, 0, 0);
    }

    public abstract ColorModel getColorModel() throws HeadlessException;

    @Deprecated
    public abstract String[] getFontList();

    @Deprecated
    public abstract FontMetrics getFontMetrics(Font var1);

    public abstract void sync();

    private static void initAssistiveTechnologies() {
        final String sep = File.separator;
        final Properties properties = new Properties();
        atNames = AccessController.doPrivileged(new PrivilegedAction<String>(){

            @Override
            public String run() {
                String classNames;
                String magPresent;
                FileInputStream in;
                File propsFile2;
                try {
                    propsFile2 = new File(System.getProperty("user.home") + sep + ".accessibility.properties");
                    in = new FileInputStream(propsFile2);
                    try {
                        properties.load(in);
                    }
                    finally {
                        in.close();
                    }
                }
                catch (Exception propsFile2) {
                    // empty catch block
                }
                if (properties.size() == 0) {
                    try {
                        propsFile2 = new File(System.getProperty("java.home") + sep + "conf" + sep + "accessibility.properties");
                        in = new FileInputStream(propsFile2);
                        try {
                            properties.load(in);
                        }
                        finally {
                            in.close();
                        }
                    }
                    catch (Exception propsFile3) {
                        // empty catch block
                    }
                }
                if ((magPresent = System.getProperty("javax.accessibility.screen_magnifier_present")) == null && (magPresent = properties.getProperty("screen_magnifier_present", null)) != null) {
                    System.setProperty("javax.accessibility.screen_magnifier_present", magPresent);
                }
                if ((classNames = System.getProperty("javax.accessibility.assistive_technologies")) == null && (classNames = properties.getProperty("assistive_technologies", null)) != null) {
                    System.setProperty("javax.accessibility.assistive_technologies", classNames);
                }
                return classNames;
            }
        });
    }

    private static void newAWTError(Throwable e, String s) {
        AWTError newAWTError = new AWTError(s);
        newAWTError.initCause(e);
        throw newAWTError;
    }

    private static void fallbackToLoadClassForAT(String atName) {
        try {
            Class<?> c = Class.forName(atName, false, ClassLoader.getSystemClassLoader());
            c.getConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (ClassNotFoundException e) {
            Toolkit.newAWTError(e, "Assistive Technology not found: " + atName);
        }
        catch (InstantiationException e) {
            Toolkit.newAWTError(e, "Could not instantiate Assistive Technology: " + atName);
        }
        catch (IllegalAccessException e) {
            Toolkit.newAWTError(e, "Could not access Assistive Technology: " + atName);
        }
        catch (Exception e) {
            Toolkit.newAWTError(e, "Error trying to install Assistive Technology: " + atName);
        }
    }

    private static void loadAssistiveTechnologies() {
        if (atNames != null && !atNames.isBlank()) {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            Set names = Arrays.stream(atNames.split(",")).map(String::trim).collect(Collectors.toSet());
            HashMap providers = new HashMap();
            AccessController.doPrivileged(() -> {
                try {
                    for (AccessibilityProvider p : ServiceLoader.load(AccessibilityProvider.class, cl)) {
                        String name = p.getName();
                        if (!names.contains(name) || providers.containsKey(name)) continue;
                        p.activate();
                        providers.put(name, p);
                    }
                }
                catch (Exception | ServiceConfigurationError e) {
                    Toolkit.newAWTError(e, "Could not load or activate service provider");
                }
                return null;
            });
            names.stream().filter(n -> !providers.containsKey(n)).forEach(Toolkit::fallbackToLoadClassForAT);
        }
    }

    public static synchronized Toolkit getDefaultToolkit() {
        if (toolkit == null) {
            toolkit = PlatformGraphicsInfo.createToolkit();
            if (GraphicsEnvironment.isHeadless() && !(toolkit instanceof HeadlessToolkit)) {
                toolkit = new HeadlessToolkit(toolkit);
            }
            if (!GraphicsEnvironment.isHeadless()) {
                Toolkit.loadAssistiveTechnologies();
            }
        }
        return toolkit;
    }

    public abstract Image getImage(String var1);

    public abstract Image getImage(URL var1);

    public abstract Image createImage(String var1);

    public abstract Image createImage(URL var1);

    public abstract boolean prepareImage(Image var1, int var2, int var3, ImageObserver var4);

    public abstract int checkImage(Image var1, int var2, int var3, ImageObserver var4);

    public abstract Image createImage(ImageProducer var1);

    public Image createImage(byte[] imagedata) {
        return this.createImage(imagedata, 0, imagedata.length);
    }

    public abstract Image createImage(byte[] var1, int var2, int var3);

    public abstract PrintJob getPrintJob(Frame var1, String var2, Properties var3);

    public PrintJob getPrintJob(Frame frame, String jobtitle, JobAttributes jobAttributes, PageAttributes pageAttributes) {
        if (this != Toolkit.getDefaultToolkit()) {
            return Toolkit.getDefaultToolkit().getPrintJob(frame, jobtitle, jobAttributes, pageAttributes);
        }
        return this.getPrintJob(frame, jobtitle, null);
    }

    public abstract void beep();

    public abstract Clipboard getSystemClipboard() throws HeadlessException;

    public Clipboard getSystemSelection() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        if (this != Toolkit.getDefaultToolkit()) {
            return Toolkit.getDefaultToolkit().getSystemSelection();
        }
        GraphicsEnvironment.checkHeadless();
        return null;
    }

    @Deprecated(since="10")
    public int getMenuShortcutKeyMask() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        return 2;
    }

    public int getMenuShortcutKeyMaskEx() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        return 128;
    }

    public boolean getLockingKeyState(int keyCode) throws UnsupportedOperationException {
        GraphicsEnvironment.checkHeadless();
        if (keyCode != 20 && keyCode != 144 && keyCode != 145 && keyCode != 262) {
            throw new IllegalArgumentException("invalid key for Toolkit.getLockingKeyState");
        }
        throw new UnsupportedOperationException("Toolkit.getLockingKeyState");
    }

    public void setLockingKeyState(int keyCode, boolean on) throws UnsupportedOperationException {
        GraphicsEnvironment.checkHeadless();
        if (keyCode != 20 && keyCode != 144 && keyCode != 145 && keyCode != 262) {
            throw new IllegalArgumentException("invalid key for Toolkit.setLockingKeyState");
        }
        throw new UnsupportedOperationException("Toolkit.setLockingKeyState");
    }

    protected static Container getNativeContainer(Component c) {
        return c.getNativeContainer();
    }

    public Cursor createCustomCursor(Image cursor, Point hotSpot, String name) throws IndexOutOfBoundsException, HeadlessException {
        if (this != Toolkit.getDefaultToolkit()) {
            return Toolkit.getDefaultToolkit().createCustomCursor(cursor, hotSpot, name);
        }
        return new Cursor(0);
    }

    public Dimension getBestCursorSize(int preferredWidth, int preferredHeight) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        if (this != Toolkit.getDefaultToolkit()) {
            return Toolkit.getDefaultToolkit().getBestCursorSize(preferredWidth, preferredHeight);
        }
        return new Dimension(0, 0);
    }

    public int getMaximumCursorColors() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        if (this != Toolkit.getDefaultToolkit()) {
            return Toolkit.getDefaultToolkit().getMaximumCursorColors();
        }
        return 0;
    }

    public boolean isFrameStateSupported(int state) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        if (this != Toolkit.getDefaultToolkit()) {
            return Toolkit.getDefaultToolkit().isFrameStateSupported(state);
        }
        return state == 0;
    }

    private static void setPlatformResources(ResourceBundle bundle) {
        platformResources = bundle;
    }

    private static native void initIDs();

    static void loadLibraries() {
        if (!loaded) {
            AccessController.doPrivileged(new PrivilegedAction<Void>(){

                @Override
                public Void run() {
                    System.loadLibrary("awt");
                    return null;
                }
            });
            loaded = true;
        }
    }

    private static void initStatic() {
        AWTAccessor.setToolkitAccessor(new AWTAccessor.ToolkitAccessor(){

            @Override
            public void setPlatformResources(ResourceBundle bundle) {
                Toolkit.setPlatformResources(bundle);
            }
        });
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                try {
                    resources = ResourceBundle.getBundle("sun.awt.resources.awt");
                }
                catch (MissingResourceException missingResourceException) {
                    // empty catch block
                }
                return null;
            }
        });
        Toolkit.loadLibraries();
        Toolkit.initAssistiveTechnologies();
        Toolkit.initIDs();
    }

    public static String getProperty(String key, String defaultValue) {
        if (platformResources != null) {
            try {
                return platformResources.getString(key);
            }
            catch (MissingResourceException missingResourceException) {
                // empty catch block
            }
        }
        if (resources != null) {
            try {
                return resources.getString(key);
            }
            catch (MissingResourceException missingResourceException) {
                // empty catch block
            }
        }
        return defaultValue;
    }

    public final EventQueue getSystemEventQueue() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(AWTPermissions.CHECK_AWT_EVENTQUEUE_PERMISSION);
        }
        return this.getSystemEventQueueImpl();
    }

    protected abstract EventQueue getSystemEventQueueImpl();

    static EventQueue getEventQueue() {
        return Toolkit.getDefaultToolkit().getSystemEventQueueImpl();
    }

    public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> abstractRecognizerClass, DragSource ds, Component c, int srcActions, DragGestureListener dgl) {
        return null;
    }

    public final synchronized Object getDesktopProperty(String propertyName) {
        if (this instanceof HeadlessToolkit) {
            return ((HeadlessToolkit)this).getUnderlyingToolkit().getDesktopProperty(propertyName);
        }
        if (this.desktopProperties.isEmpty()) {
            this.initializeDesktopProperties();
        }
        if (propertyName.equals("awt.dynamicLayoutSupported")) {
            return Toolkit.getDefaultToolkit().lazilyLoadDesktopProperty(propertyName);
        }
        Object value = this.desktopProperties.get(propertyName);
        if (value == null && (value = this.lazilyLoadDesktopProperty(propertyName)) != null) {
            this.setDesktopProperty(propertyName, value);
        }
        if (value instanceof RenderingHints) {
            value = ((RenderingHints)value).clone();
        }
        return value;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void setDesktopProperty(String name, Object newValue) {
        Object oldValue;
        if (this instanceof HeadlessToolkit) {
            ((HeadlessToolkit)this).getUnderlyingToolkit().setDesktopProperty(name, newValue);
            return;
        }
        Toolkit toolkit = this;
        synchronized (toolkit) {
            oldValue = this.desktopProperties.get(name);
            this.desktopProperties.put(name, newValue);
        }
        if (oldValue != null || newValue != null) {
            this.desktopPropsSupport.firePropertyChange(name, oldValue, newValue);
        }
    }

    protected Object lazilyLoadDesktopProperty(String name) {
        return null;
    }

    protected void initializeDesktopProperties() {
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener pcl) {
        this.desktopPropsSupport.addPropertyChangeListener(name, pcl);
    }

    public void removePropertyChangeListener(String name, PropertyChangeListener pcl) {
        this.desktopPropsSupport.removePropertyChangeListener(name, pcl);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return this.desktopPropsSupport.getPropertyChangeListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return this.desktopPropsSupport.getPropertyChangeListeners(propertyName);
    }

    public boolean isAlwaysOnTopSupported() {
        return true;
    }

    public abstract boolean isModalityTypeSupported(Dialog.ModalityType var1);

    public abstract boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType var1);

    private static AWTEventListener deProxyAWTEventListener(AWTEventListener l) {
        AWTEventListener localL = l;
        if (localL == null) {
            return null;
        }
        if (l instanceof AWTEventListenerProxy) {
            localL = (AWTEventListener)((AWTEventListenerProxy)l).getListener();
        }
        return localL;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addAWTEventListener(AWTEventListener listener, long eventMask) {
        AWTEventListener localL = Toolkit.deProxyAWTEventListener(listener);
        if (localL == null) {
            return;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(AWTPermissions.ALL_AWT_EVENTS_PERMISSION);
        }
        Toolkit toolkit = this;
        synchronized (toolkit) {
            SelectiveAWTEventListener selectiveListener = this.listener2SelectiveListener.get(localL);
            if (selectiveListener == null) {
                selectiveListener = new SelectiveAWTEventListener(localL, eventMask);
                this.listener2SelectiveListener.put(localL, selectiveListener);
                this.eventListener = ToolkitEventMulticaster.add(this.eventListener, selectiveListener);
            }
            selectiveListener.orEventMasks(eventMask);
            enabledOnToolkitMask |= eventMask;
            long mask = eventMask;
            for (int i = 0; i < 64 && mask != 0L; mask >>>= 1, ++i) {
                if ((mask & 1L) == 0L) continue;
                int n = i;
                this.calls[n] = this.calls[n] + 1;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeAWTEventListener(AWTEventListener listener) {
        AWTEventListener localL = Toolkit.deProxyAWTEventListener(listener);
        if (listener == null) {
            return;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(AWTPermissions.ALL_AWT_EVENTS_PERMISSION);
        }
        Toolkit toolkit = this;
        synchronized (toolkit) {
            SelectiveAWTEventListener selectiveListener = this.listener2SelectiveListener.get(localL);
            if (selectiveListener != null) {
                this.listener2SelectiveListener.remove(localL);
                int[] listenerCalls = selectiveListener.getCalls();
                for (int i = 0; i < 64; ++i) {
                    int n = i;
                    this.calls[n] = this.calls[n] - listenerCalls[i];
                    assert (this.calls[i] >= 0) : "Negative Listeners count";
                    if (this.calls[i] != 0) continue;
                    enabledOnToolkitMask &= 1L << i ^ 0xFFFFFFFFFFFFFFFFL;
                }
            }
            this.eventListener = ToolkitEventMulticaster.remove(this.eventListener, selectiveListener == null ? localL : selectiveListener);
        }
    }

    static boolean enabledOnToolkit(long eventMask) {
        return (enabledOnToolkitMask & eventMask) != 0L;
    }

    synchronized int countAWTEventListeners(long eventMask) {
        int ci = 0;
        while (eventMask != 0L) {
            eventMask >>>= 1;
            ++ci;
        }
        return this.calls[--ci];
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public AWTEventListener[] getAWTEventListeners() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(AWTPermissions.ALL_AWT_EVENTS_PERMISSION);
        }
        Toolkit toolkit = this;
        synchronized (toolkit) {
            EventListener[] la = ToolkitEventMulticaster.getListeners((EventListener)this.eventListener, AWTEventListener.class);
            AWTEventListener[] ret = new AWTEventListener[la.length];
            for (int i = 0; i < la.length; ++i) {
                SelectiveAWTEventListener sael = (SelectiveAWTEventListener)la[i];
                AWTEventListener tempL = sael.getListener();
                ret[i] = new AWTEventListenerProxy(sael.getEventMask(), tempL);
            }
            return ret;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public AWTEventListener[] getAWTEventListeners(long eventMask) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(AWTPermissions.ALL_AWT_EVENTS_PERMISSION);
        }
        Toolkit toolkit = this;
        synchronized (toolkit) {
            EventListener[] la = ToolkitEventMulticaster.getListeners((EventListener)this.eventListener, AWTEventListener.class);
            ArrayList<AWTEventListenerProxy> list = new ArrayList<AWTEventListenerProxy>(la.length);
            for (int i = 0; i < la.length; ++i) {
                SelectiveAWTEventListener sael = (SelectiveAWTEventListener)la[i];
                if ((sael.getEventMask() & eventMask) != eventMask) continue;
                list.add(new AWTEventListenerProxy(sael.getEventMask(), sael.getListener()));
            }
            return list.toArray(new AWTEventListener[0]);
        }
    }

    void notifyAWTEventListeners(AWTEvent theEvent) {
        if (this instanceof HeadlessToolkit) {
            ((HeadlessToolkit)this).getUnderlyingToolkit().notifyAWTEventListeners(theEvent);
            return;
        }
        AWTEventListener eventListener = this.eventListener;
        if (eventListener != null) {
            eventListener.eventDispatched(theEvent);
        }
    }

    public abstract Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight var1) throws HeadlessException;

    private static PropertyChangeSupport createPropertyChangeSupport(Toolkit toolkit) {
        if (toolkit instanceof SunToolkit || toolkit instanceof HeadlessToolkit) {
            return new DesktopPropertyChangeSupport(toolkit);
        }
        return new PropertyChangeSupport(toolkit);
    }

    public boolean areExtraMouseButtonsEnabled() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        return Toolkit.getDefaultToolkit().areExtraMouseButtonsEnabled();
    }

    static {
        loaded = false;
        Toolkit.initStatic();
    }

    private static class SelectiveAWTEventListener
    implements AWTEventListener {
        AWTEventListener listener;
        private long eventMask;
        int[] calls = new int[64];

        public AWTEventListener getListener() {
            return this.listener;
        }

        public long getEventMask() {
            return this.eventMask;
        }

        public int[] getCalls() {
            return this.calls;
        }

        public void orEventMasks(long mask) {
            this.eventMask |= mask;
            for (int i = 0; i < 64 && mask != 0L; mask >>>= 1, ++i) {
                if ((mask & 1L) == 0L) continue;
                int n = i;
                this.calls[n] = this.calls[n] + 1;
            }
        }

        SelectiveAWTEventListener(AWTEventListener l, long mask) {
            this.listener = l;
            this.eventMask = mask;
        }

        @Override
        public void eventDispatched(AWTEvent event) {
            long eventBit = 0L;
            eventBit = this.eventMask & 1L;
            if (eventBit != 0L && event.id >= 100 && event.id <= 103 || (eventBit = this.eventMask & 2L) != 0L && event.id >= 300 && event.id <= 301 || (eventBit = this.eventMask & 4L) != 0L && event.id >= 1004 && event.id <= 1005 || (eventBit = this.eventMask & 8L) != 0L && event.id >= 400 && event.id <= 402 || (eventBit = this.eventMask & 0x20000L) != 0L && event.id == 507 || (eventBit = this.eventMask & 0x20L) != 0L && (event.id == 503 || event.id == 506) || (eventBit = this.eventMask & 0x10L) != 0L && event.id != 503 && event.id != 506 && event.id != 507 && event.id >= 500 && event.id <= 507 || (eventBit = this.eventMask & 0x40L) != 0L && event.id >= 200 && event.id <= 209 || (eventBit = this.eventMask & 0x80L) != 0L && event.id >= 1001 && event.id <= 1001 || (eventBit = this.eventMask & 0x100L) != 0L && event.id >= 601 && event.id <= 601 || (eventBit = this.eventMask & 0x200L) != 0L && event.id >= 701 && event.id <= 701 || (eventBit = this.eventMask & 0x400L) != 0L && event.id >= 900 && event.id <= 900 || (eventBit = this.eventMask & 0x800L) != 0L && event.id >= 1100 && event.id <= 1101 || (eventBit = this.eventMask & 0x2000L) != 0L && event.id >= 800 && event.id <= 801 || (eventBit = this.eventMask & 0x4000L) != 0L && event.id >= 1200 && event.id <= 1200 || (eventBit = this.eventMask & 0x8000L) != 0L && event.id == 1400 || (eventBit = this.eventMask & 0x10000L) != 0L && (event.id == 1401 || event.id == 1402) || (eventBit = this.eventMask & 0x40000L) != 0L && event.id == 209 || (eventBit = this.eventMask & 0x80000L) != 0L && (event.id == 207 || event.id == 208) || (eventBit = this.eventMask & Integer.MIN_VALUE) != 0L && event instanceof UngrabEvent) {
                int ci = 0;
                long eMask = eventBit;
                while (eMask != 0L) {
                    eMask >>>= 1;
                    ++ci;
                }
                --ci;
                for (int i = 0; i < this.calls[ci]; ++i) {
                    this.listener.eventDispatched(event);
                }
            }
        }
    }

    private static class ToolkitEventMulticaster
    extends AWTEventMulticaster
    implements AWTEventListener {
        ToolkitEventMulticaster(AWTEventListener a, AWTEventListener b) {
            super(a, b);
        }

        static AWTEventListener add(AWTEventListener a, AWTEventListener b) {
            if (a == null) {
                return b;
            }
            if (b == null) {
                return a;
            }
            return new ToolkitEventMulticaster(a, b);
        }

        static AWTEventListener remove(AWTEventListener l, AWTEventListener oldl) {
            return (AWTEventListener)ToolkitEventMulticaster.removeInternal(l, oldl);
        }

        @Override
        protected EventListener remove(EventListener oldl) {
            if (oldl == this.a) {
                return this.b;
            }
            if (oldl == this.b) {
                return this.a;
            }
            AWTEventListener a2 = (AWTEventListener)ToolkitEventMulticaster.removeInternal(this.a, oldl);
            AWTEventListener b2 = (AWTEventListener)ToolkitEventMulticaster.removeInternal(this.b, oldl);
            if (a2 == this.a && b2 == this.b) {
                return this;
            }
            return ToolkitEventMulticaster.add(a2, b2);
        }

        @Override
        public void eventDispatched(AWTEvent event) {
            ((AWTEventListener)this.a).eventDispatched(event);
            ((AWTEventListener)this.b).eventDispatched(event);
        }
    }

    private static class DesktopPropertyChangeSupport
    extends PropertyChangeSupport {
        private static final StringBuilder PROP_CHANGE_SUPPORT_KEY = new StringBuilder("desktop property change support key");
        private final Object source;

        public DesktopPropertyChangeSupport(Object sourceBean) {
            super(sourceBean);
            this.source = sourceBean;
        }

        @Override
        public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            PropertyChangeSupport pcs = (PropertyChangeSupport)AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
            if (null == pcs) {
                pcs = new PropertyChangeSupport(this.source);
                AppContext.getAppContext().put(PROP_CHANGE_SUPPORT_KEY, pcs);
            }
            pcs.addPropertyChangeListener(propertyName, listener);
        }

        @Override
        public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            PropertyChangeSupport pcs = (PropertyChangeSupport)AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
            if (null != pcs) {
                pcs.removePropertyChangeListener(propertyName, listener);
            }
        }

        @Override
        public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
            PropertyChangeSupport pcs = (PropertyChangeSupport)AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
            if (null != pcs) {
                return pcs.getPropertyChangeListeners();
            }
            return new PropertyChangeListener[0];
        }

        @Override
        public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
            PropertyChangeSupport pcs = (PropertyChangeSupport)AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
            if (null != pcs) {
                return pcs.getPropertyChangeListeners(propertyName);
            }
            return new PropertyChangeListener[0];
        }

        @Override
        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            PropertyChangeSupport pcs = (PropertyChangeSupport)AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
            if (null == pcs) {
                pcs = new PropertyChangeSupport(this.source);
                AppContext.getAppContext().put(PROP_CHANGE_SUPPORT_KEY, pcs);
            }
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            PropertyChangeSupport pcs = (PropertyChangeSupport)AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
            if (null != pcs) {
                pcs.removePropertyChangeListener(listener);
            }
        }

        @Override
        public void firePropertyChange(final PropertyChangeEvent evt) {
            Object oldValue = evt.getOldValue();
            Object newValue = evt.getNewValue();
            String propertyName = evt.getPropertyName();
            if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
                return;
            }
            Runnable updater = new Runnable(){

                @Override
                public void run() {
                    PropertyChangeSupport pcs = (PropertyChangeSupport)AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
                    if (null != pcs) {
                        pcs.firePropertyChange(evt);
                    }
                }
            };
            AppContext currentAppContext = AppContext.getAppContext();
            for (AppContext appContext : AppContext.getAppContexts()) {
                if (null == appContext || appContext.isDisposed()) continue;
                if (currentAppContext == appContext) {
                    updater.run();
                    continue;
                }
                PeerEvent e = new PeerEvent(this.source, updater, 2L);
                SunToolkit.postEvent(appContext, e);
            }
        }
    }
}

