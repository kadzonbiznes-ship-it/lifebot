/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.KeyboardFocusManager;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuComponent;
import java.awt.MenuContainer;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.ScrollPaneAdjustable;
import java.awt.SystemColor;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.color.ICC_Profile;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.dnd.peer.DropTargetContextPeer;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.InvocationEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferStrategy;
import java.awt.peer.ComponentPeer;
import java.awt.peer.MenuComponentPeer;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlContext;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.accessibility.AccessibleBundle;
import javax.accessibility.AccessibleContext;
import sun.awt.AppContext;
import sun.awt.FwDispatcher;
import sun.awt.RequestFocusController;
import sun.java2d.cmm.Profile;

public final class AWTAccessor {
    private static ICC_ProfileAccessor iccProfileAccessor;
    private static ComponentAccessor componentAccessor;
    private static ContainerAccessor containerAccessor;
    private static WindowAccessor windowAccessor;
    private static AWTEventAccessor awtEventAccessor;
    private static InputEventAccessor inputEventAccessor;
    private static MouseEventAccessor mouseEventAccessor;
    private static FrameAccessor frameAccessor;
    private static KeyboardFocusManagerAccessor kfmAccessor;
    private static MenuComponentAccessor menuComponentAccessor;
    private static EventQueueAccessor eventQueueAccessor;
    private static PopupMenuAccessor popupMenuAccessor;
    private static FileDialogAccessor fileDialogAccessor;
    private static ScrollPaneAdjustableAccessor scrollPaneAdjustableAccessor;
    private static CheckboxMenuItemAccessor checkboxMenuItemAccessor;
    private static CursorAccessor cursorAccessor;
    private static MenuBarAccessor menuBarAccessor;
    private static MenuItemAccessor menuItemAccessor;
    private static MenuAccessor menuAccessor;
    private static KeyEventAccessor keyEventAccessor;
    private static ClientPropertyKeyAccessor clientPropertyKeyAccessor;
    private static SystemTrayAccessor systemTrayAccessor;
    private static TrayIconAccessor trayIconAccessor;
    private static DefaultKeyboardFocusManagerAccessor defaultKeyboardFocusManagerAccessor;
    private static SequencedEventAccessor sequencedEventAccessor;
    private static ToolkitAccessor toolkitAccessor;
    private static InvocationEventAccessor invocationEventAccessor;
    private static SystemColorAccessor systemColorAccessor;
    private static AccessibleContextAccessor accessibleContextAccessor;
    private static AccessibleBundleAccessor accessibleBundleAccessor;
    private static DragSourceContextAccessor dragSourceContextAccessor;
    private static DropTargetContextAccessor dropTargetContextAccessor;

    private AWTAccessor() {
    }

    public static void setICC_ProfileAccessor(ICC_ProfileAccessor ipa) {
        iccProfileAccessor = ipa;
    }

    public static ICC_ProfileAccessor getICC_ProfileAccessor() {
        ICC_ProfileAccessor access = iccProfileAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(ICC_Profile.class);
            access = iccProfileAccessor;
        }
        return access;
    }

    public static void setComponentAccessor(ComponentAccessor ca) {
        componentAccessor = ca;
    }

    public static ComponentAccessor getComponentAccessor() {
        ComponentAccessor access = componentAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(Component.class);
            access = componentAccessor;
        }
        return access;
    }

    public static void setContainerAccessor(ContainerAccessor ca) {
        containerAccessor = ca;
    }

    public static ContainerAccessor getContainerAccessor() {
        ContainerAccessor access = containerAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(Container.class);
            access = containerAccessor;
        }
        return access;
    }

    public static void setWindowAccessor(WindowAccessor wa) {
        windowAccessor = wa;
    }

    public static WindowAccessor getWindowAccessor() {
        WindowAccessor access = windowAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(Window.class);
            access = windowAccessor;
        }
        return access;
    }

    public static void setAWTEventAccessor(AWTEventAccessor aea) {
        awtEventAccessor = aea;
    }

    public static AWTEventAccessor getAWTEventAccessor() {
        AWTEventAccessor access = awtEventAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(AWTEvent.class);
            access = awtEventAccessor;
        }
        return access;
    }

    public static void setInputEventAccessor(InputEventAccessor iea) {
        inputEventAccessor = iea;
    }

    public static InputEventAccessor getInputEventAccessor() {
        InputEventAccessor access = inputEventAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(InputEvent.class);
            access = inputEventAccessor;
        }
        return access;
    }

    public static void setMouseEventAccessor(MouseEventAccessor mea) {
        mouseEventAccessor = mea;
    }

    public static MouseEventAccessor getMouseEventAccessor() {
        MouseEventAccessor access = mouseEventAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(MouseEvent.class);
            access = mouseEventAccessor;
        }
        return access;
    }

    public static void setFrameAccessor(FrameAccessor fa) {
        frameAccessor = fa;
    }

    public static FrameAccessor getFrameAccessor() {
        FrameAccessor access = frameAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(Frame.class);
            access = frameAccessor;
        }
        return access;
    }

    public static void setKeyboardFocusManagerAccessor(KeyboardFocusManagerAccessor kfma) {
        kfmAccessor = kfma;
    }

    public static KeyboardFocusManagerAccessor getKeyboardFocusManagerAccessor() {
        KeyboardFocusManagerAccessor access = kfmAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(KeyboardFocusManager.class);
            access = kfmAccessor;
        }
        return access;
    }

    public static void setMenuComponentAccessor(MenuComponentAccessor mca) {
        menuComponentAccessor = mca;
    }

    public static MenuComponentAccessor getMenuComponentAccessor() {
        MenuComponentAccessor access = menuComponentAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(MenuComponent.class);
            access = menuComponentAccessor;
        }
        return access;
    }

    public static void setEventQueueAccessor(EventQueueAccessor eqa) {
        eventQueueAccessor = eqa;
    }

    public static EventQueueAccessor getEventQueueAccessor() {
        EventQueueAccessor access = eventQueueAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(EventQueue.class);
            access = eventQueueAccessor;
        }
        return access;
    }

    public static void setPopupMenuAccessor(PopupMenuAccessor pma) {
        popupMenuAccessor = pma;
    }

    public static PopupMenuAccessor getPopupMenuAccessor() {
        PopupMenuAccessor access = popupMenuAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(PopupMenu.class);
            access = popupMenuAccessor;
        }
        return access;
    }

    public static void setFileDialogAccessor(FileDialogAccessor fda) {
        fileDialogAccessor = fda;
    }

    public static FileDialogAccessor getFileDialogAccessor() {
        FileDialogAccessor access = fileDialogAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(FileDialog.class);
            access = fileDialogAccessor;
        }
        return access;
    }

    public static void setScrollPaneAdjustableAccessor(ScrollPaneAdjustableAccessor adj) {
        scrollPaneAdjustableAccessor = adj;
    }

    public static ScrollPaneAdjustableAccessor getScrollPaneAdjustableAccessor() {
        ScrollPaneAdjustableAccessor access = scrollPaneAdjustableAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(ScrollPaneAdjustable.class);
            access = scrollPaneAdjustableAccessor;
        }
        return access;
    }

    public static void setCheckboxMenuItemAccessor(CheckboxMenuItemAccessor cmia) {
        checkboxMenuItemAccessor = cmia;
    }

    public static CheckboxMenuItemAccessor getCheckboxMenuItemAccessor() {
        CheckboxMenuItemAccessor access = checkboxMenuItemAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(CheckboxMenuItemAccessor.class);
            access = checkboxMenuItemAccessor;
        }
        return access;
    }

    public static void setCursorAccessor(CursorAccessor ca) {
        cursorAccessor = ca;
    }

    public static CursorAccessor getCursorAccessor() {
        CursorAccessor access = cursorAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(CursorAccessor.class);
            access = cursorAccessor;
        }
        return access;
    }

    public static void setMenuBarAccessor(MenuBarAccessor mba) {
        menuBarAccessor = mba;
    }

    public static MenuBarAccessor getMenuBarAccessor() {
        MenuBarAccessor access = menuBarAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(MenuBarAccessor.class);
            access = menuBarAccessor;
        }
        return access;
    }

    public static void setMenuItemAccessor(MenuItemAccessor mia) {
        menuItemAccessor = mia;
    }

    public static MenuItemAccessor getMenuItemAccessor() {
        MenuItemAccessor access = menuItemAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(MenuItemAccessor.class);
            access = menuItemAccessor;
        }
        return access;
    }

    public static void setMenuAccessor(MenuAccessor ma) {
        menuAccessor = ma;
    }

    public static MenuAccessor getMenuAccessor() {
        MenuAccessor access = menuAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(MenuAccessor.class);
            access = menuAccessor;
        }
        return access;
    }

    public static void setKeyEventAccessor(KeyEventAccessor kea) {
        keyEventAccessor = kea;
    }

    public static KeyEventAccessor getKeyEventAccessor() {
        KeyEventAccessor access = keyEventAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(KeyEventAccessor.class);
            access = keyEventAccessor;
        }
        return access;
    }

    public static void setClientPropertyKeyAccessor(ClientPropertyKeyAccessor cpka) {
        clientPropertyKeyAccessor = cpka;
    }

    public static ClientPropertyKeyAccessor getClientPropertyKeyAccessor() {
        ClientPropertyKeyAccessor access = clientPropertyKeyAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(ClientPropertyKeyAccessor.class);
            access = clientPropertyKeyAccessor;
        }
        return access;
    }

    public static void setSystemTrayAccessor(SystemTrayAccessor sta) {
        systemTrayAccessor = sta;
    }

    public static SystemTrayAccessor getSystemTrayAccessor() {
        SystemTrayAccessor access = systemTrayAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(SystemTrayAccessor.class);
            access = systemTrayAccessor;
        }
        return access;
    }

    public static void setTrayIconAccessor(TrayIconAccessor tia) {
        trayIconAccessor = tia;
    }

    public static TrayIconAccessor getTrayIconAccessor() {
        TrayIconAccessor access = trayIconAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(TrayIconAccessor.class);
            access = trayIconAccessor;
        }
        return access;
    }

    public static void setDefaultKeyboardFocusManagerAccessor(DefaultKeyboardFocusManagerAccessor dkfma) {
        defaultKeyboardFocusManagerAccessor = dkfma;
    }

    public static DefaultKeyboardFocusManagerAccessor getDefaultKeyboardFocusManagerAccessor() {
        DefaultKeyboardFocusManagerAccessor access = defaultKeyboardFocusManagerAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(DefaultKeyboardFocusManagerAccessor.class);
            access = defaultKeyboardFocusManagerAccessor;
        }
        return access;
    }

    public static void setSequencedEventAccessor(SequencedEventAccessor sea) {
        sequencedEventAccessor = sea;
    }

    public static SequencedEventAccessor getSequencedEventAccessor() {
        SequencedEventAccessor access = sequencedEventAccessor;
        if (access == null) {
            try {
                AWTAccessor.ensureClassInitialized(Class.forName("java.awt.SequencedEvent"));
            }
            catch (ClassNotFoundException classNotFoundException) {
                // empty catch block
            }
            access = sequencedEventAccessor;
        }
        return access;
    }

    public static void setToolkitAccessor(ToolkitAccessor ta) {
        toolkitAccessor = ta;
    }

    public static ToolkitAccessor getToolkitAccessor() {
        ToolkitAccessor access = toolkitAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(Toolkit.class);
            access = toolkitAccessor;
        }
        return access;
    }

    public static void setInvocationEventAccessor(InvocationEventAccessor invocationEventAccessor) {
        AWTAccessor.invocationEventAccessor = invocationEventAccessor;
    }

    public static InvocationEventAccessor getInvocationEventAccessor() {
        return invocationEventAccessor;
    }

    public static SystemColorAccessor getSystemColorAccessor() {
        SystemColorAccessor access = systemColorAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(SystemColor.class);
            access = systemColorAccessor;
        }
        return access;
    }

    public static void setSystemColorAccessor(SystemColorAccessor systemColorAccessor) {
        AWTAccessor.systemColorAccessor = systemColorAccessor;
    }

    public static AccessibleContextAccessor getAccessibleContextAccessor() {
        AccessibleContextAccessor access = accessibleContextAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(AccessibleContext.class);
            access = accessibleContextAccessor;
        }
        return access;
    }

    public static void setAccessibleBundleAccessor(AccessibleBundleAccessor accessor) {
        accessibleBundleAccessor = accessor;
    }

    public static AccessibleBundleAccessor getAccessibleBundleAccessor() {
        AccessibleBundleAccessor access = accessibleBundleAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(AccessibleBundle.class);
            access = accessibleBundleAccessor;
        }
        return access;
    }

    public static void setAccessibleContextAccessor(AccessibleContextAccessor accessor) {
        accessibleContextAccessor = accessor;
    }

    public static DragSourceContextAccessor getDragSourceContextAccessor() {
        DragSourceContextAccessor access = dragSourceContextAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(DragSourceContext.class);
            access = dragSourceContextAccessor;
        }
        return access;
    }

    public static void setDragSourceContextAccessor(DragSourceContextAccessor accessor) {
        dragSourceContextAccessor = accessor;
    }

    public static DropTargetContextAccessor getDropTargetContextAccessor() {
        DropTargetContextAccessor access = dropTargetContextAccessor;
        if (access == null) {
            AWTAccessor.ensureClassInitialized(DropTargetContext.class);
            access = dropTargetContextAccessor;
        }
        return access;
    }

    public static void setDropTargetContextAccessor(DropTargetContextAccessor accessor) {
        dropTargetContextAccessor = accessor;
    }

    private static void ensureClassInitialized(Class<?> c) {
        try {
            MethodHandles.lookup().ensureInitialized(c);
        }
        catch (IllegalAccessException illegalAccessException) {
            // empty catch block
        }
    }

    public static interface ICC_ProfileAccessor {
        public Profile cmmProfile(ICC_Profile var1);
    }

    public static interface ComponentAccessor {
        public void setBackgroundEraseDisabled(Component var1, boolean var2);

        public boolean getBackgroundEraseDisabled(Component var1);

        public Rectangle getBounds(Component var1);

        public void setGraphicsConfiguration(Component var1, GraphicsConfiguration var2);

        public void requestFocus(Component var1, FocusEvent.Cause var2);

        public boolean canBeFocusOwner(Component var1);

        public boolean isVisible(Component var1);

        public void setRequestFocusController(RequestFocusController var1);

        public AppContext getAppContext(Component var1);

        public void setAppContext(Component var1, AppContext var2);

        public Container getParent(Component var1);

        public void setParent(Component var1, Container var2);

        public void setSize(Component var1, int var2, int var3);

        public Point getLocation(Component var1);

        public void setLocation(Component var1, int var2, int var3);

        public boolean isEnabled(Component var1);

        public boolean isDisplayable(Component var1);

        public Cursor getCursor(Component var1);

        public <T extends ComponentPeer> T getPeer(Component var1);

        public void setPeer(Component var1, ComponentPeer var2);

        public boolean isLightweight(Component var1);

        public boolean getIgnoreRepaint(Component var1);

        public int getWidth(Component var1);

        public int getHeight(Component var1);

        public int getX(Component var1);

        public int getY(Component var1);

        public Color getForeground(Component var1);

        public Color getBackground(Component var1);

        public void setBackground(Component var1, Color var2);

        public Font getFont(Component var1);

        public void processEvent(Component var1, AWTEvent var2);

        public AccessControlContext getAccessControlContext(Component var1);

        public void revalidateSynchronously(Component var1);

        public void createBufferStrategy(Component var1, int var2, BufferCapabilities var3) throws AWTException;

        public BufferStrategy getBufferStrategy(Component var1);
    }

    public static interface ContainerAccessor {
        public void validateUnconditionally(Container var1);

        public Component findComponentAt(Container var1, int var2, int var3, boolean var4);

        public void startLWModal(Container var1);

        public void stopLWModal(Container var1);
    }

    public static interface WindowAccessor {
        public void updateWindow(Window var1);

        public void setSecurityWarningSize(Window var1, int var2, int var3);

        public Point2D calculateSecurityWarningPosition(Window var1, double var2, double var4, double var6, double var8);

        public void setLWRequestStatus(Window var1, boolean var2);

        public boolean isAutoRequestFocus(Window var1);

        public boolean isTrayIconWindow(Window var1);

        public void setTrayIconWindow(Window var1, boolean var2);

        public Window[] getOwnedWindows(Window var1);
    }

    public static interface AWTEventAccessor {
        public void setPosted(AWTEvent var1);

        public void setSystemGenerated(AWTEvent var1);

        public boolean isSystemGenerated(AWTEvent var1);

        public AccessControlContext getAccessControlContext(AWTEvent var1);

        public byte[] getBData(AWTEvent var1);

        public void setBData(AWTEvent var1, byte[] var2);
    }

    public static interface InputEventAccessor {
        public int[] getButtonDownMasks();

        public boolean canAccessSystemClipboard(InputEvent var1);

        public void setCanAccessSystemClipboard(InputEvent var1, boolean var2);
    }

    public static interface MouseEventAccessor {
        public boolean isCausedByTouchEvent(MouseEvent var1);

        public void setCausedByTouchEvent(MouseEvent var1, boolean var2);
    }

    public static interface FrameAccessor {
        public void setExtendedState(Frame var1, int var2);

        public int getExtendedState(Frame var1);

        public Rectangle getMaximizedBounds(Frame var1);
    }

    public static interface KeyboardFocusManagerAccessor {
        public int shouldNativelyFocusHeavyweight(Component var1, Component var2, boolean var3, boolean var4, long var5, FocusEvent.Cause var7);

        public boolean processSynchronousLightweightTransfer(Component var1, Component var2, boolean var3, boolean var4, long var5);

        public void removeLastFocusRequest(Component var1);

        public Component getMostRecentFocusOwner(Window var1);

        public void setMostRecentFocusOwner(Window var1, Component var2);

        public KeyboardFocusManager getCurrentKeyboardFocusManager(AppContext var1);

        public Container getCurrentFocusCycleRoot();
    }

    public static interface MenuComponentAccessor {
        public AppContext getAppContext(MenuComponent var1);

        public void setAppContext(MenuComponent var1, AppContext var2);

        public <T extends MenuComponentPeer> T getPeer(MenuComponent var1);

        public MenuContainer getParent(MenuComponent var1);

        public void setParent(MenuComponent var1, MenuContainer var2);

        public Font getFont_NoClientCode(MenuComponent var1);
    }

    public static interface EventQueueAccessor {
        public Thread getDispatchThread(EventQueue var1);

        public boolean isDispatchThreadImpl(EventQueue var1);

        public void removeSourceEvents(EventQueue var1, Object var2, boolean var3);

        public boolean noEvents(EventQueue var1);

        public void wakeup(EventQueue var1, boolean var2);

        public void invokeAndWait(Object var1, Runnable var2) throws InterruptedException, InvocationTargetException;

        public void setFwDispatcher(EventQueue var1, FwDispatcher var2);

        public long getMostRecentEventTime(EventQueue var1);
    }

    public static interface PopupMenuAccessor {
        public boolean isTrayIconPopup(PopupMenu var1);
    }

    public static interface FileDialogAccessor {
        public void setFiles(FileDialog var1, File[] var2);

        public void setFile(FileDialog var1, String var2);

        public void setDirectory(FileDialog var1, String var2);

        public boolean isMultipleMode(FileDialog var1);
    }

    public static interface ScrollPaneAdjustableAccessor {
        public void setTypedValue(ScrollPaneAdjustable var1, int var2, int var3);
    }

    public static interface CheckboxMenuItemAccessor {
        public boolean getState(CheckboxMenuItem var1);
    }

    public static interface CursorAccessor {
        public long getPData(Cursor var1);

        public void setPData(Cursor var1, long var2);

        public int getType(Cursor var1);
    }

    public static interface MenuBarAccessor {
        public Menu getHelpMenu(MenuBar var1);

        public Vector<Menu> getMenus(MenuBar var1);
    }

    public static interface MenuItemAccessor {
        public boolean isEnabled(MenuItem var1);

        public String getActionCommandImpl(MenuItem var1);

        public boolean isItemEnabled(MenuItem var1);

        public String getLabel(MenuItem var1);

        public MenuShortcut getShortcut(MenuItem var1);
    }

    public static interface MenuAccessor {
        public Vector<MenuItem> getItems(Menu var1);
    }

    public static interface KeyEventAccessor {
        public void setRawCode(KeyEvent var1, long var2);

        public void setPrimaryLevelUnicode(KeyEvent var1, long var2);

        public void setExtendedKeyCode(KeyEvent var1, long var2);

        public Component getOriginalSource(KeyEvent var1);

        public boolean isProxyActive(KeyEvent var1);
    }

    public static interface ClientPropertyKeyAccessor {
        public Object getJComponent_TRANSFER_HANDLER();
    }

    public static interface SystemTrayAccessor {
        public void firePropertyChange(SystemTray var1, String var2, Object var3, Object var4);
    }

    public static interface TrayIconAccessor {
        public void addNotify(TrayIcon var1) throws AWTException;

        public void removeNotify(TrayIcon var1);
    }

    public static interface DefaultKeyboardFocusManagerAccessor {
        public void consumeNextKeyTyped(DefaultKeyboardFocusManager var1, KeyEvent var2);
    }

    public static interface SequencedEventAccessor {
        public AWTEvent getNested(AWTEvent var1);

        public boolean isSequencedEvent(AWTEvent var1);

        public AWTEvent create(AWTEvent var1);
    }

    public static interface ToolkitAccessor {
        public void setPlatformResources(ResourceBundle var1);
    }

    public static interface InvocationEventAccessor {
        public void dispose(InvocationEvent var1);
    }

    public static interface SystemColorAccessor {
        public void updateSystemColors();
    }

    public static interface AccessibleContextAccessor {
        public void setAppContext(AccessibleContext var1, AppContext var2);

        public AppContext getAppContext(AccessibleContext var1);

        public Object getNativeAXResource(AccessibleContext var1);

        public void setNativeAXResource(AccessibleContext var1, Object var2);
    }

    public static interface AccessibleBundleAccessor {
        public String getKey(AccessibleBundle var1);
    }

    public static interface DragSourceContextAccessor {
        public DragSourceContextPeer getPeer(DragSourceContext var1);
    }

    public static interface DropTargetContextAccessor {
        public void reset(DropTargetContext var1);

        public void setDropTargetContextPeer(DropTargetContext var1, DropTargetContextPeer var2);
    }
}

