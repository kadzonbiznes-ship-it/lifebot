/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.JobAttributes;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.PageAttributes;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.PrintJob;
import java.awt.RenderingHints;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.SystemTray;
import java.awt.Taskbar;
import java.awt.TextArea;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.MouseDragGestureRecognizer;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethodDescriptor;
import java.awt.image.ColorModel;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CanvasPeer;
import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.DesktopPeer;
import java.awt.peer.DialogPeer;
import java.awt.peer.FileDialogPeer;
import java.awt.peer.FontPeer;
import java.awt.peer.FramePeer;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuItemPeer;
import java.awt.peer.MenuPeer;
import java.awt.peer.MouseInfoPeer;
import java.awt.peer.PanelPeer;
import java.awt.peer.PopupMenuPeer;
import java.awt.peer.RobotPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.SystemTrayPeer;
import java.awt.peer.TaskbarPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextFieldPeer;
import java.awt.peer.TrayIconPeer;
import java.awt.peer.WindowPeer;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.text.JTextComponent;
import sun.awt.AWTAccessor;
import sun.awt.AWTAutoShutdown;
import sun.awt.AWTPermissions;
import sun.awt.AppContext;
import sun.awt.DisplayChangedListener;
import sun.awt.LightweightFrame;
import sun.awt.SunToolkit;
import sun.awt.Win32GraphicsDevice;
import sun.awt.Win32GraphicsEnvironment;
import sun.awt.datatransfer.DataTransferer;
import sun.awt.util.PerformanceLogger;
import sun.awt.util.ThreadGroupUtils;
import sun.awt.windows.ThemeReader;
import sun.awt.windows.WButtonPeer;
import sun.awt.windows.WCanvasPeer;
import sun.awt.windows.WCheckboxMenuItemPeer;
import sun.awt.windows.WCheckboxPeer;
import sun.awt.windows.WChoicePeer;
import sun.awt.windows.WClipboard;
import sun.awt.windows.WCustomCursor;
import sun.awt.windows.WDataTransferer;
import sun.awt.windows.WDesktopPeer;
import sun.awt.windows.WDesktopProperties;
import sun.awt.windows.WDialogPeer;
import sun.awt.windows.WDragSourceContextPeer;
import sun.awt.windows.WEmbeddedFrame;
import sun.awt.windows.WEmbeddedFramePeer;
import sun.awt.windows.WFileDialogPeer;
import sun.awt.windows.WFontPeer;
import sun.awt.windows.WFramePeer;
import sun.awt.windows.WInputMethod;
import sun.awt.windows.WInputMethodDescriptor;
import sun.awt.windows.WKeyboardFocusManagerPeer;
import sun.awt.windows.WLabelPeer;
import sun.awt.windows.WLightweightFramePeer;
import sun.awt.windows.WListPeer;
import sun.awt.windows.WMenuBarPeer;
import sun.awt.windows.WMenuItemPeer;
import sun.awt.windows.WMenuPeer;
import sun.awt.windows.WMouseDragGestureRecognizer;
import sun.awt.windows.WMouseInfoPeer;
import sun.awt.windows.WPageDialog;
import sun.awt.windows.WPageDialogPeer;
import sun.awt.windows.WPanelPeer;
import sun.awt.windows.WPopupMenuPeer;
import sun.awt.windows.WPrintDialog;
import sun.awt.windows.WPrintDialogPeer;
import sun.awt.windows.WRobotPeer;
import sun.awt.windows.WScrollPanePeer;
import sun.awt.windows.WScrollbarPeer;
import sun.awt.windows.WSystemTrayPeer;
import sun.awt.windows.WTaskbarPeer;
import sun.awt.windows.WTextAreaPeer;
import sun.awt.windows.WTextFieldPeer;
import sun.awt.windows.WTrayIconPeer;
import sun.awt.windows.WWindowPeer;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;
import sun.java2d.d3d.D3DRenderQueue;
import sun.java2d.opengl.OGLRenderQueue;
import sun.print.PrintJob2D;
import sun.util.logging.PlatformLogger;

public final class WToolkit
extends SunToolkit
implements Runnable {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.windows.WToolkit");
    public static final String XPSTYLE_THEME_ACTIVE = "win.xpstyle.themeActive";
    WClipboard clipboard;
    private Hashtable<String, FontPeer> cacheFontPeer;
    private WDesktopProperties wprops;
    protected boolean dynamicLayoutSetting = false;
    private static boolean areExtraMouseButtonsEnabled = true;
    private static boolean loaded = false;
    private final Object anchor = new Object();
    private boolean inited = false;
    private static WMouseInfoPeer wPeer;
    static ColorModel screenmodel;
    private static ExecutorService displayChangeExecutor;
    private static final String prefix = "DnD.Cursor.";
    private static final String postfix = ".32x32";
    private static final String awtPrefix = "awt.";
    private static final String dndPrefix = "DnD.";
    private static final WeakReference<Component> NULL_COMPONENT_WR;
    private volatile WeakReference<Component> compOnTouchDownEvent = NULL_COMPONENT_WR;
    private volatile WeakReference<Component> compOnMousePressedEvent = NULL_COMPONENT_WR;

    private static native void initIDs();

    public static void loadLibraries() {
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

    private static native String getWindowsVersion();

    public static native boolean embeddedInit();

    public static native boolean embeddedDispose();

    public native void embeddedEventLoopIdleProcessing();

    private static native void postDispose();

    private static native boolean startToolkitThread(Runnable var0, ThreadGroup var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public WToolkit() {
        Object name2;
        if (PerformanceLogger.loggingEnabled()) {
            PerformanceLogger.setTime("WToolkit construction");
        }
        Disposer.addRecord(this.anchor, new ToolkitDisposer());
        AWTAutoShutdown.notifyToolkitThreadBusy();
        ThreadGroup rootTG = AccessController.doPrivileged(ThreadGroupUtils::getRootThreadGroup);
        if (!WToolkit.startToolkitThread(this, rootTG)) {
            name2 = "AWT-Windows";
            AccessController.doPrivileged(() -> {
                Thread toolkitThread = new Thread(rootTG, this, "AWT-Windows", 0L, false);
                toolkitThread.setDaemon(true);
                toolkitThread.start();
                return null;
            });
        }
        try {
            name2 = this;
            synchronized (name2) {
                while (!this.inited) {
                    this.wait();
                }
            }
        }
        catch (InterruptedException name2) {
            // empty catch block
        }
        this.setDynamicLayout(true);
        String extraButtons = "sun.awt.enableExtraMouseButtons";
        AccessController.doPrivileged(() -> {
            areExtraMouseButtonsEnabled = Boolean.parseBoolean(System.getProperty("sun.awt.enableExtraMouseButtons", "true"));
            System.setProperty("sun.awt.enableExtraMouseButtons", "" + areExtraMouseButtonsEnabled);
            return null;
        });
        WToolkit.setExtraMouseButtonsEnabledNative(areExtraMouseButtonsEnabled);
    }

    private void registerShutdownHook() {
        AccessController.doPrivileged(() -> {
            Thread shutdown = new Thread(ThreadGroupUtils.getRootThreadGroup(), this::shutdown, "ToolkitShutdown", 0L, false);
            shutdown.setContextClassLoader(null);
            Runtime.getRuntime().addShutdownHook(shutdown);
            return null;
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        AccessController.doPrivileged(() -> {
            Thread.currentThread().setContextClassLoader(null);
            Thread.currentThread().setPriority(6);
            return null;
        });
        boolean startPump = this.init();
        if (startPump) {
            this.registerShutdownHook();
        }
        WToolkit wToolkit = this;
        synchronized (wToolkit) {
            this.inited = true;
            this.notifyAll();
        }
        if (startPump) {
            this.eventLoop();
        }
    }

    private native boolean init();

    private native void eventLoop();

    private native void shutdown();

    static native void startSecondaryEventLoop();

    static native void quitSecondaryEventLoop();

    @Override
    public ButtonPeer createButton(Button target) {
        WButtonPeer peer = new WButtonPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public TextFieldPeer createTextField(TextField target) {
        WTextFieldPeer peer = new WTextFieldPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public LabelPeer createLabel(Label target) {
        WLabelPeer peer = new WLabelPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public ListPeer createList(List target) {
        WListPeer peer = new WListPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public CheckboxPeer createCheckbox(Checkbox target) {
        WCheckboxPeer peer = new WCheckboxPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public ScrollbarPeer createScrollbar(Scrollbar target) {
        WScrollbarPeer peer = new WScrollbarPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public ScrollPanePeer createScrollPane(ScrollPane target) {
        WScrollPanePeer peer = new WScrollPanePeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public TextAreaPeer createTextArea(TextArea target) {
        WTextAreaPeer peer = new WTextAreaPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public ChoicePeer createChoice(Choice target) {
        WChoicePeer peer = new WChoicePeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public FramePeer createFrame(Frame target) {
        WFramePeer peer = new WFramePeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public FramePeer createLightweightFrame(LightweightFrame target) {
        WLightweightFramePeer peer = new WLightweightFramePeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public CanvasPeer createCanvas(Canvas target) {
        WCanvasPeer peer = new WCanvasPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public void disableBackgroundErase(Canvas canvas) {
        WCanvasPeer peer = (WCanvasPeer)AWTAccessor.getComponentAccessor().getPeer(canvas);
        if (peer == null) {
            throw new IllegalStateException("Canvas must have a valid peer");
        }
        peer.disableBackgroundErase();
    }

    @Override
    public PanelPeer createPanel(Panel target) {
        WPanelPeer peer = new WPanelPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public WindowPeer createWindow(Window target) {
        WWindowPeer peer = new WWindowPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public DialogPeer createDialog(Dialog target) {
        WDialogPeer peer = new WDialogPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public FileDialogPeer createFileDialog(FileDialog target) {
        WFileDialogPeer peer = new WFileDialogPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public MenuBarPeer createMenuBar(MenuBar target) {
        WMenuBarPeer peer = new WMenuBarPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public MenuPeer createMenu(Menu target) {
        WMenuPeer peer = new WMenuPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public PopupMenuPeer createPopupMenu(PopupMenu target) {
        WPopupMenuPeer peer = new WPopupMenuPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public MenuItemPeer createMenuItem(MenuItem target) {
        WMenuItemPeer peer = new WMenuItemPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem target) {
        WCheckboxMenuItemPeer peer = new WCheckboxMenuItemPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public RobotPeer createRobot(GraphicsDevice screen) throws AWTException {
        if (screen instanceof Win32GraphicsDevice) {
            return new WRobotPeer();
        }
        return super.createRobot(screen);
    }

    public WEmbeddedFramePeer createEmbeddedFrame(WEmbeddedFrame target) {
        WEmbeddedFramePeer peer = new WEmbeddedFramePeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    WPrintDialogPeer createWPrintDialog(WPrintDialog target) {
        WPrintDialogPeer peer = new WPrintDialogPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    WPageDialogPeer createWPageDialog(WPageDialog target) {
        WPageDialogPeer peer = new WPageDialogPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public TrayIconPeer createTrayIcon(TrayIcon target) {
        WTrayIconPeer peer = new WTrayIconPeer(target);
        WToolkit.targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public SystemTrayPeer createSystemTray(SystemTray target) {
        return new WSystemTrayPeer(target);
    }

    @Override
    public boolean isTraySupported() {
        return true;
    }

    @Override
    public DataTransferer getDataTransferer() {
        return WDataTransferer.getInstanceImpl();
    }

    @Override
    public KeyboardFocusManagerPeer getKeyboardFocusManagerPeer() throws HeadlessException {
        return WKeyboardFocusManagerPeer.getInstance();
    }

    @Override
    public synchronized MouseInfoPeer getMouseInfoPeer() {
        if (wPeer == null) {
            wPeer = new WMouseInfoPeer();
        }
        return wPeer;
    }

    private native void setDynamicLayoutNative(boolean var1);

    @Override
    public void setDynamicLayout(boolean b) {
        if (b == this.dynamicLayoutSetting) {
            return;
        }
        this.dynamicLayoutSetting = b;
        this.setDynamicLayoutNative(b);
    }

    @Override
    protected boolean isDynamicLayoutSet() {
        return this.dynamicLayoutSetting;
    }

    private native boolean isDynamicLayoutSupportedNative();

    @Override
    public boolean isDynamicLayoutActive() {
        return this.isDynamicLayoutSet() && this.isDynamicLayoutSupported();
    }

    @Override
    public boolean isFrameStateSupported(int state) {
        switch (state) {
            case 0: 
            case 1: 
            case 6: {
                return true;
            }
        }
        return false;
    }

    static native ColorModel makeColorModel();

    @Override
    public Insets getScreenInsets(GraphicsConfiguration gc) {
        GraphicsDevice gd = gc.getDevice();
        if (!(gd instanceof Win32GraphicsDevice)) {
            return super.getScreenInsets(gc);
        }
        return this.getScreenInsets(((Win32GraphicsDevice)gd).getScreen());
    }

    @Override
    public int getScreenResolution() {
        Win32GraphicsEnvironment ge = (Win32GraphicsEnvironment)GraphicsEnvironment.getLocalGraphicsEnvironment();
        return ge.getXResolution();
    }

    private native Insets getScreenInsets(int var1);

    @Override
    public FontPeer getFontPeer(String name, int style) {
        FontPeer retval = null;
        String lcName = name.toLowerCase();
        if (null != this.cacheFontPeer && null != (retval = this.cacheFontPeer.get(lcName + style))) {
            return retval;
        }
        retval = new WFontPeer(name, style);
        if (retval != null) {
            if (null == this.cacheFontPeer) {
                this.cacheFontPeer = new Hashtable(5, 0.9f);
            }
            if (null != this.cacheFontPeer) {
                this.cacheFontPeer.put(lcName + style, retval);
            }
        }
        return retval;
    }

    private native void nativeSync();

    @Override
    public void sync() {
        this.nativeSync();
        OGLRenderQueue.sync();
        D3DRenderQueue.sync();
    }

    @Override
    public PrintJob getPrintJob(Frame frame, String doctitle, Properties props) {
        return this.getPrintJob(frame, doctitle, null, null);
    }

    @Override
    public PrintJob getPrintJob(Frame frame, String doctitle, JobAttributes jobAttributes, PageAttributes pageAttributes) {
        if (frame == null) {
            throw new NullPointerException("frame must not be null");
        }
        PrintJob2D printJob = new PrintJob2D(frame, doctitle, jobAttributes, pageAttributes);
        if (!printJob.printDialog()) {
            printJob = null;
        }
        return printJob;
    }

    @Override
    public native void beep();

    @Override
    public boolean getLockingKeyState(int key) {
        if (key != 20 && key != 144 && key != 145 && key != 262) {
            throw new IllegalArgumentException("invalid key for Toolkit.getLockingKeyState");
        }
        return this.getLockingKeyStateNative(key);
    }

    private native boolean getLockingKeyStateNative(int var1);

    @Override
    public void setLockingKeyState(int key, boolean on) {
        if (key != 20 && key != 144 && key != 145 && key != 262) {
            throw new IllegalArgumentException("invalid key for Toolkit.setLockingKeyState");
        }
        this.setLockingKeyStateNative(key, on);
    }

    private native void setLockingKeyStateNative(int var1, boolean var2);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Clipboard getSystemClipboard() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(AWTPermissions.ACCESS_CLIPBOARD_PERMISSION);
        }
        WToolkit wToolkit = this;
        synchronized (wToolkit) {
            if (this.clipboard == null) {
                this.clipboard = new WClipboard();
            }
        }
        return this.clipboard;
    }

    @Override
    protected native void loadSystemColors(int[] var1);

    public static Object targetToPeer(Object target) {
        return SunToolkit.targetToPeer(target);
    }

    public static void targetDisposedPeer(Object target, Object peer) {
        SunToolkit.targetDisposedPeer(target, peer);
    }

    @Override
    public InputMethodDescriptor getInputMethodAdapterDescriptor() {
        return new WInputMethodDescriptor();
    }

    @Override
    public Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight highlight) {
        return WInputMethod.mapInputMethodHighlight(highlight);
    }

    @Override
    public boolean enableInputMethodsForTextComponent() {
        return true;
    }

    @Override
    public Locale getDefaultKeyboardLocale() {
        Locale locale = WInputMethod.getNativeLocale();
        if (locale == null) {
            return super.getDefaultKeyboardLocale();
        }
        return locale;
    }

    @Override
    public Cursor createCustomCursor(Image cursor, Point hotSpot, String name) throws IndexOutOfBoundsException {
        return new WCustomCursor(cursor, hotSpot, name);
    }

    @Override
    public Dimension getBestCursorSize(int preferredWidth, int preferredHeight) {
        return new Dimension(WCustomCursor.getCursorWidth(), WCustomCursor.getCursorHeight());
    }

    @Override
    public native int getMaximumCursorColors();

    static void paletteChanged() {
        GraphicsEnvironment lge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (lge instanceof DisplayChangedListener) {
            ((DisplayChangedListener)((Object)lge)).paletteChanged();
        }
    }

    public static void displayChanged() {
        Runnable runnable = () -> {
            GraphicsEnvironment lge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (lge instanceof DisplayChangedListener) {
                ((DisplayChangedListener)((Object)lge)).displayChanged();
            }
        };
        if (AppContext.getAppContext() != null) {
            EventQueue.invokeLater(runnable);
        } else {
            if (displayChangeExecutor == null) {
                displayChangeExecutor = Executors.newFixedThreadPool(1, r -> {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setDaemon(true);
                    return t;
                });
            }
            displayChangeExecutor.submit(runnable);
        }
    }

    @Override
    public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException {
        LightweightFrame f = SunToolkit.getLightweightFrame(dge.getComponent());
        if (f != null) {
            return f.createDragSourceContextPeer(dge);
        }
        return WDragSourceContextPeer.createDragSourceContextPeer(dge);
    }

    @Override
    public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> abstractRecognizerClass, DragSource ds, Component c, int srcActions, DragGestureListener dgl) {
        LightweightFrame f = SunToolkit.getLightweightFrame(c);
        if (f != null) {
            return f.createDragGestureRecognizer(abstractRecognizerClass, ds, c, srcActions, dgl);
        }
        if (MouseDragGestureRecognizer.class.equals(abstractRecognizerClass)) {
            return (T)new WMouseDragGestureRecognizer(ds, c, srcActions, dgl);
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected Object lazilyLoadDesktopProperty(String name) {
        if (name.startsWith(prefix)) {
            String cursorName = name.substring(prefix.length()) + postfix;
            try {
                return Cursor.getSystemCustomCursor(cursorName);
            }
            catch (AWTException awte) {
                throw new RuntimeException("cannot load system cursor: " + cursorName, awte);
            }
        }
        if (name.equals("awt.dynamicLayoutSupported")) {
            return this.isDynamicLayoutSupported();
        }
        if (WDesktopProperties.isWindowsProperty(name) || name.startsWith(awtPrefix) || name.startsWith(dndPrefix)) {
            WToolkit wToolkit = this;
            synchronized (wToolkit) {
                this.lazilyInitWProps();
                return this.desktopProperties.get(name);
            }
        }
        return super.lazilyLoadDesktopProperty(name);
    }

    private synchronized void lazilyInitWProps() {
        if (this.wprops == null) {
            this.wprops = new WDesktopProperties(this);
            this.updateProperties(this.wprops.getProperties());
        }
    }

    private synchronized boolean isDynamicLayoutSupported() {
        boolean nativeDynamic = this.isDynamicLayoutSupportedNative();
        this.lazilyInitWProps();
        Boolean prop = (Boolean)this.desktopProperties.get("awt.dynamicLayoutSupported");
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("In WTK.isDynamicLayoutSupported()   nativeDynamic == " + nativeDynamic + "   wprops.dynamic == " + prop);
        }
        if (prop == null || nativeDynamic != prop) {
            this.windowsSettingChange();
            return nativeDynamic;
        }
        return prop;
    }

    private void windowsSettingChange() {
        Map<String, Object> props = this.getWProps();
        if (props == null) {
            return;
        }
        this.updateXPStyleEnabled(props.get(XPSTYLE_THEME_ACTIVE));
        if (AppContext.getAppContext() == null) {
            this.updateProperties(props);
        } else {
            EventQueue.invokeLater(() -> this.updateProperties(props));
        }
    }

    private synchronized void updateProperties(Map<String, Object> props) {
        if (null == props) {
            return;
        }
        this.updateXPStyleEnabled(props.get(XPSTYLE_THEME_ACTIVE));
        for (String propName : props.keySet()) {
            Object val = props.get(propName);
            if (log.isLoggable(PlatformLogger.Level.FINER)) {
                log.finer("changed " + propName + " to " + String.valueOf(val));
            }
            this.setDesktopProperty(propName, val);
        }
    }

    private synchronized Map<String, Object> getWProps() {
        return this.wprops != null ? this.wprops.getProperties() : null;
    }

    private void updateXPStyleEnabled(Object dskProp) {
        ThemeReader.xpStyleEnabled = Boolean.TRUE.equals(dskProp);
    }

    @Override
    public synchronized void addPropertyChangeListener(String name, PropertyChangeListener pcl) {
        if (name == null) {
            return;
        }
        if (WDesktopProperties.isWindowsProperty(name) || name.startsWith(awtPrefix) || name.startsWith(dndPrefix)) {
            this.lazilyInitWProps();
        }
        super.addPropertyChangeListener(name, pcl);
    }

    @Override
    protected synchronized void initializeDesktopProperties() {
        this.desktopProperties.put("DnD.Autoscroll.initialDelay", 50);
        this.desktopProperties.put("DnD.Autoscroll.interval", 50);
        this.desktopProperties.put("DnD.isDragImageSupported", Boolean.TRUE);
        this.desktopProperties.put("Shell.shellFolderManager", "sun.awt.shell.Win32ShellFolderManager2");
    }

    @Override
    protected synchronized RenderingHints getDesktopAAHints() {
        if (this.wprops == null) {
            return null;
        }
        return this.wprops.getDesktopAAHints();
    }

    @Override
    public boolean isModalityTypeSupported(Dialog.ModalityType modalityType) {
        return modalityType == null || modalityType == Dialog.ModalityType.MODELESS || modalityType == Dialog.ModalityType.DOCUMENT_MODAL || modalityType == Dialog.ModalityType.APPLICATION_MODAL || modalityType == Dialog.ModalityType.TOOLKIT_MODAL;
    }

    @Override
    public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType exclusionType) {
        return exclusionType == null || exclusionType == Dialog.ModalExclusionType.NO_EXCLUDE || exclusionType == Dialog.ModalExclusionType.APPLICATION_EXCLUDE || exclusionType == Dialog.ModalExclusionType.TOOLKIT_EXCLUDE;
    }

    public static WToolkit getWToolkit() {
        WToolkit toolkit = (WToolkit)Toolkit.getDefaultToolkit();
        return toolkit;
    }

    @Override
    public boolean useBufferPerWindow() {
        return !Win32GraphicsEnvironment.isDWMCompositionEnabled();
    }

    @Override
    public void grab(Window w) {
        Object peer = AWTAccessor.getComponentAccessor().getPeer(w);
        if (peer != null) {
            ((WWindowPeer)peer).grab();
        }
    }

    @Override
    public void ungrab(Window w) {
        Object peer = AWTAccessor.getComponentAccessor().getPeer(w);
        if (peer != null) {
            ((WWindowPeer)peer).ungrab();
        }
    }

    private boolean isComponentValidForTouchKeyboard(Component comp) {
        return comp != null && comp.isEnabled() && comp.isFocusable() && (comp instanceof TextComponent && ((TextComponent)comp).isEditable() || comp instanceof JTextComponent && ((JTextComponent)comp).isEditable());
    }

    @Override
    public void showOrHideTouchKeyboard(Component comp, AWTEvent e) {
        FocusEvent fe;
        if (!(comp instanceof TextComponent) && !(comp instanceof JTextComponent)) {
            return;
        }
        if (e instanceof MouseEvent && this.isComponentValidForTouchKeyboard(comp)) {
            MouseEvent me = (MouseEvent)e;
            if (me.getID() == 501) {
                if (AWTAccessor.getMouseEventAccessor().isCausedByTouchEvent(me)) {
                    this.compOnTouchDownEvent = new WeakReference<Component>(comp);
                } else {
                    this.compOnMousePressedEvent = new WeakReference<Component>(comp);
                }
            } else if (me.getID() == 502) {
                if (AWTAccessor.getMouseEventAccessor().isCausedByTouchEvent(me)) {
                    if (this.compOnTouchDownEvent.get() == comp) {
                        this.showTouchKeyboard(true);
                    }
                    this.compOnTouchDownEvent = NULL_COMPONENT_WR;
                } else {
                    if (this.compOnMousePressedEvent.get() == comp) {
                        this.showTouchKeyboard(false);
                    }
                    this.compOnMousePressedEvent = NULL_COMPONENT_WR;
                }
            }
        } else if (e instanceof FocusEvent && (fe = (FocusEvent)e).getID() == 1005 && !this.isComponentValidForTouchKeyboard(fe.getOppositeComponent())) {
            this.hideTouchKeyboard();
        }
    }

    private native void showTouchKeyboard(boolean var1);

    private native void hideTouchKeyboard();

    @Override
    public native boolean syncNativeQueue(long var1);

    @Override
    public boolean isDesktopSupported() {
        return true;
    }

    @Override
    public DesktopPeer createDesktopPeer(Desktop target) {
        return new WDesktopPeer();
    }

    @Override
    public boolean isTaskbarSupported() {
        return WTaskbarPeer.isTaskbarSupported();
    }

    @Override
    public TaskbarPeer createTaskbarPeer(Taskbar target) {
        return new WTaskbarPeer();
    }

    private static native void setExtraMouseButtonsEnabledNative(boolean var0);

    @Override
    public boolean areExtraMouseButtonsEnabled() throws HeadlessException {
        return areExtraMouseButtonsEnabled;
    }

    private synchronized native int getNumberOfButtonsImpl();

    @Override
    public int getNumberOfButtons() {
        if (numberOfButtons == 0) {
            numberOfButtons = this.getNumberOfButtonsImpl();
        }
        return numberOfButtons > 20 ? 20 : numberOfButtons;
    }

    @Override
    public boolean isWindowOpacitySupported() {
        return true;
    }

    @Override
    public boolean isWindowShapingSupported() {
        return true;
    }

    @Override
    public boolean isWindowTranslucencySupported() {
        return true;
    }

    @Override
    public boolean isTranslucencyCapable(GraphicsConfiguration gc) {
        return true;
    }

    @Override
    public boolean needUpdateWindow() {
        return true;
    }

    static {
        WToolkit.loadLibraries();
        WToolkit.initIDs();
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("Win version: " + WToolkit.getWindowsVersion());
        }
        wPeer = null;
        NULL_COMPONENT_WR = new WeakReference<Object>(null);
    }

    static class ToolkitDisposer
    implements DisposerRecord {
        ToolkitDisposer() {
        }

        @Override
        public void dispose() {
            WToolkit.postDispose();
        }
    }
}

