/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JApplet;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JWindow;
import javax.swing.LookAndFeel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.PopupMenuUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.DefaultMenuLayout;
import javax.swing.plaf.basic.LazyActionMap;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.UngrabEvent;
import sun.swing.UIAction;

public class BasicPopupMenuUI
extends PopupMenuUI {
    static final StringBuilder MOUSE_GRABBER_KEY = new StringBuilder("javax.swing.plaf.basic.BasicPopupMenuUI.MouseGrabber");
    static final StringBuilder MENU_KEYBOARD_HELPER_KEY = new StringBuilder("javax.swing.plaf.basic.BasicPopupMenuUI.MenuKeyboardHelper");
    protected JPopupMenu popupMenu = null;
    private transient PopupMenuListener popupMenuListener = null;
    private MenuKeyListener menuKeyListener = null;
    private static boolean checkedUnpostPopup;
    private static boolean unpostPopup;

    public static ComponentUI createUI(JComponent x) {
        return new BasicPopupMenuUI();
    }

    public BasicPopupMenuUI() {
        BasicLookAndFeel.needsEventHelper = true;
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf instanceof BasicLookAndFeel) {
            ((BasicLookAndFeel)laf).installAWTEventListener();
        }
    }

    @Override
    public void installUI(JComponent c) {
        this.popupMenu = (JPopupMenu)c;
        this.installDefaults();
        this.installListeners();
        this.installKeyboardActions();
    }

    public void installDefaults() {
        if (this.popupMenu.getLayout() == null || this.popupMenu.getLayout() instanceof UIResource) {
            this.popupMenu.setLayout(new DefaultMenuLayout(this.popupMenu, 1));
        }
        LookAndFeel.installProperty(this.popupMenu, "opaque", Boolean.TRUE);
        LookAndFeel.installBorder(this.popupMenu, "PopupMenu.border");
        LookAndFeel.installColorsAndFont(this.popupMenu, "PopupMenu.background", "PopupMenu.foreground", "PopupMenu.font");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void installListeners() {
        if (this.popupMenuListener == null) {
            this.popupMenuListener = new BasicPopupMenuListener();
        }
        this.popupMenu.addPopupMenuListener(this.popupMenuListener);
        if (this.menuKeyListener == null) {
            this.menuKeyListener = new BasicMenuKeyListener();
        }
        this.popupMenu.addMenuKeyListener(this.menuKeyListener);
        AppContext context = AppContext.getAppContext();
        StringBuilder stringBuilder = MOUSE_GRABBER_KEY;
        synchronized (stringBuilder) {
            MouseGrabber mouseGrabber = (MouseGrabber)context.get(MOUSE_GRABBER_KEY);
            if (mouseGrabber == null) {
                mouseGrabber = new MouseGrabber();
                context.put(MOUSE_GRABBER_KEY, mouseGrabber);
            }
        }
        stringBuilder = MENU_KEYBOARD_HELPER_KEY;
        synchronized (stringBuilder) {
            MenuKeyboardHelper helper = (MenuKeyboardHelper)context.get(MENU_KEYBOARD_HELPER_KEY);
            if (helper == null) {
                helper = new MenuKeyboardHelper();
                context.put(MENU_KEYBOARD_HELPER_KEY, helper);
                MenuSelectionManager msm = MenuSelectionManager.defaultManager();
                msm.addChangeListener(helper);
            }
        }
    }

    protected void installKeyboardActions() {
    }

    static InputMap getInputMap(JPopupMenu popup, JComponent c) {
        ComponentInputMap windowInputMap = null;
        Object[] bindings = (Object[])UIManager.get("PopupMenu.selectedWindowInputMapBindings");
        if (bindings != null) {
            Object[] km;
            windowInputMap = LookAndFeel.makeComponentInputMap(c, bindings);
            if (!popup.getComponentOrientation().isLeftToRight() && (km = (Object[])UIManager.get("PopupMenu.selectedWindowInputMapBindings.RightToLeft")) != null) {
                ComponentInputMap rightToLeftInputMap = LookAndFeel.makeComponentInputMap(c, km);
                ((InputMap)rightToLeftInputMap).setParent(windowInputMap);
                windowInputMap = rightToLeftInputMap;
            }
        }
        return windowInputMap;
    }

    static ActionMap getActionMap() {
        return LazyActionMap.getActionMap(BasicPopupMenuUI.class, "PopupMenu.actionMap");
    }

    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions("cancel"));
        map.put(new Actions("selectNext"));
        map.put(new Actions("selectPrevious"));
        map.put(new Actions("selectParent"));
        map.put(new Actions("selectChild"));
        map.put(new Actions("return"));
        BasicLookAndFeel.installAudioActionMap(map);
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.uninstallDefaults();
        this.uninstallListeners();
        this.uninstallKeyboardActions();
        this.popupMenu = null;
    }

    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(this.popupMenu);
    }

    protected void uninstallListeners() {
        if (this.popupMenuListener != null) {
            this.popupMenu.removePopupMenuListener(this.popupMenuListener);
        }
        if (this.menuKeyListener != null) {
            this.popupMenu.removeMenuKeyListener(this.menuKeyListener);
        }
    }

    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIActionMap(this.popupMenu, null);
        SwingUtilities.replaceUIInputMap(this.popupMenu, 2, null);
    }

    static MenuElement getFirstPopup() {
        MenuSelectionManager msm = MenuSelectionManager.defaultManager();
        MenuElement[] p = msm.getSelectedPath();
        MenuElement me = null;
        for (int i = 0; me == null && i < p.length; ++i) {
            if (!(p[i] instanceof JPopupMenu)) continue;
            me = p[i];
        }
        return me;
    }

    static JPopupMenu getLastPopup() {
        MenuSelectionManager msm = MenuSelectionManager.defaultManager();
        MenuElement[] p = msm.getSelectedPath();
        JPopupMenu popup = null;
        for (int i = p.length - 1; popup == null && i >= 0; --i) {
            if (!(p[i] instanceof JPopupMenu)) continue;
            popup = (JPopupMenu)p[i];
        }
        return popup;
    }

    static List<JPopupMenu> getPopups() {
        MenuSelectionManager msm = MenuSelectionManager.defaultManager();
        MenuElement[] p = msm.getSelectedPath();
        ArrayList<JPopupMenu> list = new ArrayList<JPopupMenu>(p.length);
        for (MenuElement element : p) {
            if (!(element instanceof JPopupMenu)) continue;
            list.add((JPopupMenu)element);
        }
        return list;
    }

    @Override
    public boolean isPopupTrigger(MouseEvent e) {
        return e.getID() == 502 && (e.getModifiers() & 4) != 0;
    }

    private static boolean checkInvokerEqual(MenuElement present, MenuElement last) {
        Component invokerPresent = present.getComponent();
        Component invokerLast = last.getComponent();
        if (invokerPresent instanceof JPopupMenu) {
            invokerPresent = ((JPopupMenu)invokerPresent).getInvoker();
        }
        if (invokerLast instanceof JPopupMenu) {
            invokerLast = ((JPopupMenu)invokerLast).getInvoker();
        }
        return invokerPresent == invokerLast;
    }

    private static MenuElement nextEnabledChild(MenuElement[] e, int fromIndex, int toIndex) {
        for (int i = fromIndex; i <= toIndex; ++i) {
            Component comp;
            if (e[i] == null || (comp = e[i].getComponent()) == null || !comp.isEnabled() && !UIManager.getBoolean("MenuItem.disabledAreNavigable") || !comp.isVisible()) continue;
            return e[i];
        }
        return null;
    }

    private static MenuElement previousEnabledChild(MenuElement[] e, int fromIndex, int toIndex) {
        for (int i = fromIndex; i >= toIndex; --i) {
            Component comp;
            if (e[i] == null || (comp = e[i].getComponent()) == null || !comp.isEnabled() && !UIManager.getBoolean("MenuItem.disabledAreNavigable") || !comp.isVisible()) continue;
            return e[i];
        }
        return null;
    }

    static MenuElement findEnabledChild(MenuElement[] e, int fromIndex, boolean forward) {
        MenuElement result;
        if (forward) {
            result = BasicPopupMenuUI.nextEnabledChild(e, fromIndex + 1, e.length - 1);
            if (result == null) {
                result = BasicPopupMenuUI.nextEnabledChild(e, 0, fromIndex - 1);
            }
        } else {
            result = BasicPopupMenuUI.previousEnabledChild(e, fromIndex - 1, 0);
            if (result == null) {
                result = BasicPopupMenuUI.previousEnabledChild(e, e.length - 1, fromIndex + 1);
            }
        }
        return result;
    }

    static MenuElement findEnabledChild(MenuElement[] e, MenuElement elem, boolean forward) {
        for (int i = 0; i < e.length; ++i) {
            if (e[i] != elem) continue;
            return BasicPopupMenuUI.findEnabledChild(e, i, forward);
        }
        return null;
    }

    private static class BasicPopupMenuListener
    implements PopupMenuListener {
        private BasicPopupMenuListener() {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            BasicLookAndFeel.playSound((JPopupMenu)e.getSource(), "PopupMenu.popupSound");
        }
    }

    private class BasicMenuKeyListener
    implements MenuKeyListener {
        MenuElement menuToOpen = null;

        private BasicMenuKeyListener() {
        }

        @Override
        public void menuKeyTyped(MenuKeyEvent e) {
            if (this.menuToOpen != null) {
                JPopupMenu subpopup = ((JMenu)this.menuToOpen).getPopupMenu();
                MenuElement subitem = BasicPopupMenuUI.findEnabledChild(subpopup.getSubElements(), -1, true);
                ArrayList<MenuElement> lst = new ArrayList<MenuElement>(Arrays.asList(e.getPath()));
                lst.add(this.menuToOpen);
                lst.add(subpopup);
                if (subitem != null) {
                    lst.add(subitem);
                }
                MenuElement[] newPath = new MenuElement[]{};
                newPath = lst.toArray(newPath);
                MenuSelectionManager.defaultManager().setSelectedPath(newPath);
                e.consume();
            }
            this.menuToOpen = null;
        }

        @Override
        public void menuKeyPressed(MenuKeyEvent e) {
            char keyChar = e.getKeyChar();
            if (!Character.isLetterOrDigit(keyChar)) {
                return;
            }
            MenuSelectionManager manager = e.getMenuSelectionManager();
            MenuElement[] path = e.getPath();
            MenuElement[] items = BasicPopupMenuUI.this.popupMenu.getSubElements();
            int currentIndex = -1;
            int matches = 0;
            int firstMatch = -1;
            int[] indexes = null;
            for (int j = 0; j < items.length; ++j) {
                if (!(items[j] instanceof JMenuItem)) continue;
                JMenuItem item = (JMenuItem)items[j];
                int mnemonic = item.getMnemonic();
                if (item.isEnabled() && item.isVisible() && this.lower(keyChar) == this.lower(mnemonic)) {
                    if (matches == 0) {
                        firstMatch = j;
                        ++matches;
                    } else {
                        if (indexes == null) {
                            indexes = new int[items.length];
                            indexes[0] = firstMatch;
                        }
                        indexes[matches++] = j;
                    }
                }
                if (!item.isArmed() && !item.isSelected()) continue;
                currentIndex = matches - 1;
            }
            if (matches != 0) {
                if (matches == 1) {
                    JMenuItem item = (JMenuItem)items[firstMatch];
                    if (item instanceof JMenu) {
                        this.menuToOpen = item;
                    } else if (item.isEnabled()) {
                        manager.clearSelectedPath();
                        item.doClick();
                    }
                    e.consume();
                } else {
                    MenuElement newItem = items[indexes[(currentIndex + 1) % matches]];
                    MenuElement[] newPath = new MenuElement[path.length + 1];
                    System.arraycopy(path, 0, newPath, 0, path.length);
                    newPath[path.length] = newItem;
                    manager.setSelectedPath(newPath);
                    e.consume();
                }
            }
        }

        @Override
        public void menuKeyReleased(MenuKeyEvent e) {
        }

        private char lower(char keyChar) {
            return Character.toLowerCase(keyChar);
        }

        private char lower(int mnemonic) {
            return Character.toLowerCase((char)mnemonic);
        }
    }

    static class MouseGrabber
    implements ChangeListener,
    AWTEventListener,
    ComponentListener,
    WindowListener {
        Window grabbedWindow;
        MenuElement[] lastPathSelected;

        public MouseGrabber() {
            MenuSelectionManager msm = MenuSelectionManager.defaultManager();
            msm.addChangeListener(this);
            this.lastPathSelected = msm.getSelectedPath();
            if (this.lastPathSelected.length != 0) {
                this.grabWindow(this.lastPathSelected);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void uninstall() {
            StringBuilder stringBuilder = MOUSE_GRABBER_KEY;
            synchronized (stringBuilder) {
                MenuSelectionManager.defaultManager().removeChangeListener(this);
                this.ungrabWindow();
                AppContext.getAppContext().remove(MOUSE_GRABBER_KEY);
            }
        }

        void grabWindow(MenuElement[] newPath) {
            final Toolkit tk = Toolkit.getDefaultToolkit();
            AccessController.doPrivileged(new PrivilegedAction<Object>(){

                @Override
                public Object run() {
                    tk.addAWTEventListener(this, -2147352464L);
                    return null;
                }
            });
            Component invoker = newPath[0].getComponent();
            if (invoker instanceof JPopupMenu) {
                invoker = ((JPopupMenu)invoker).getInvoker();
            }
            Window window = invoker == null ? null : (this.grabbedWindow = invoker instanceof Window ? (Window)invoker : SwingUtilities.getWindowAncestor(invoker));
            if (this.grabbedWindow != null) {
                if (tk instanceof SunToolkit) {
                    ((SunToolkit)tk).grab(this.grabbedWindow);
                } else {
                    this.grabbedWindow.addComponentListener(this);
                    this.grabbedWindow.addWindowListener(this);
                }
            }
        }

        void ungrabWindow() {
            final Toolkit tk = Toolkit.getDefaultToolkit();
            AccessController.doPrivileged(new PrivilegedAction<Object>(){

                @Override
                public Object run() {
                    tk.removeAWTEventListener(this);
                    return null;
                }
            });
            this.realUngrabWindow();
        }

        void realUngrabWindow() {
            Toolkit tk = Toolkit.getDefaultToolkit();
            if (this.grabbedWindow != null) {
                if (tk instanceof SunToolkit) {
                    ((SunToolkit)tk).ungrab(this.grabbedWindow);
                } else {
                    this.grabbedWindow.removeComponentListener(this);
                    this.grabbedWindow.removeWindowListener(this);
                }
                this.grabbedWindow = null;
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            MenuSelectionManager msm = MenuSelectionManager.defaultManager();
            MenuElement[] p = msm.getSelectedPath();
            if (this.lastPathSelected.length == 0 && p.length != 0) {
                this.grabWindow(p);
            }
            if (this.lastPathSelected.length != 0 && p.length == 0) {
                this.ungrabWindow();
            }
            this.lastPathSelected = p;
        }

        @Override
        public void eventDispatched(AWTEvent ev) {
            if (ev instanceof UngrabEvent) {
                this.cancelPopupMenu();
                return;
            }
            if (!(ev instanceof MouseEvent)) {
                return;
            }
            MouseEvent me = (MouseEvent)ev;
            Component src = me.getComponent();
            switch (me.getID()) {
                case 501: {
                    if (this.isInPopup(src) || src instanceof JMenu && ((JMenu)src).isSelected()) {
                        return;
                    }
                    if (src instanceof JComponent && ((JComponent)src).getClientProperty("doNotCancelPopup") == BasicComboBoxUI.HIDE_POPUP_KEY) break;
                    this.cancelPopupMenu();
                    boolean consumeEvent = UIManager.getBoolean("PopupMenu.consumeEventOnClose");
                    if (!consumeEvent || src instanceof MenuElement) break;
                    me.consume();
                    break;
                }
                case 502: {
                    if (!(src instanceof MenuElement) && this.isInPopup(src) || !(src instanceof JMenu) && src instanceof JMenuItem) break;
                    MenuSelectionManager.defaultManager().processMouseEvent(me);
                    break;
                }
                case 506: {
                    if (!(src instanceof MenuElement) && this.isInPopup(src)) break;
                    MenuSelectionManager.defaultManager().processMouseEvent(me);
                    break;
                }
                case 507: {
                    if (this.isInPopup(src) || src instanceof JComboBox && ((JComboBox)src).isPopupVisible() || src instanceof JWindow && ((JWindow)src).isVisible() || src instanceof JMenuItem && ((JMenuItem)src).isVisible() || src instanceof JFrame) {
                        return;
                    }
                    this.cancelPopupMenu();
                }
            }
        }

        boolean isInPopup(Component src) {
            for (Component c = src; c != null && !(c instanceof Applet) && !(c instanceof Window); c = c.getParent()) {
                if (!(c instanceof JPopupMenu)) continue;
                return true;
            }
            return false;
        }

        void cancelPopupMenu() {
            try {
                List<JPopupMenu> popups = BasicPopupMenuUI.getPopups();
                for (JPopupMenu popup : popups) {
                    popup.putClientProperty("JPopupMenu.firePopupMenuCanceled", Boolean.TRUE);
                }
                MenuSelectionManager.defaultManager().clearSelectedPath();
            }
            catch (Error | RuntimeException e) {
                this.realUngrabWindow();
                throw e;
            }
        }

        @Override
        public void componentResized(ComponentEvent e) {
            this.cancelPopupMenu();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            this.cancelPopupMenu();
        }

        @Override
        public void componentShown(ComponentEvent e) {
            this.cancelPopupMenu();
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            this.cancelPopupMenu();
        }

        @Override
        public void windowClosing(WindowEvent e) {
            this.cancelPopupMenu();
        }

        @Override
        public void windowClosed(WindowEvent e) {
            this.cancelPopupMenu();
        }

        @Override
        public void windowIconified(WindowEvent e) {
            this.cancelPopupMenu();
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            this.cancelPopupMenu();
        }

        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }
    }

    static class MenuKeyboardHelper
    implements ChangeListener,
    KeyListener {
        private Component lastFocused = null;
        private MenuElement[] lastPathSelected = new MenuElement[0];
        private JPopupMenu lastPopup;
        private JRootPane invokerRootPane;
        private ActionMap menuActionMap = BasicPopupMenuUI.getActionMap();
        private InputMap menuInputMap;
        private boolean focusTraversalKeysEnabled;
        private boolean receivedKeyPressed = false;
        private FocusListener rootPaneFocusListener = new FocusAdapter(){

            @Override
            public void focusGained(FocusEvent ev) {
                Component opposite = ev.getOppositeComponent();
                if (opposite != null) {
                    lastFocused = opposite;
                }
                ev.getComponent().removeFocusListener(this);
            }
        };

        MenuKeyboardHelper() {
        }

        void removeItems() {
            if (this.lastFocused != null) {
                Window cfw;
                if (!this.lastFocused.requestFocusInWindow() && (cfw = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow()) != null && "###focusableSwingPopup###".equals(cfw.getName())) {
                    this.lastFocused.requestFocus();
                }
                this.lastFocused = null;
            }
            if (this.invokerRootPane != null) {
                this.invokerRootPane.removeKeyListener(this);
                this.invokerRootPane.setFocusTraversalKeysEnabled(this.focusTraversalKeysEnabled);
                this.removeUIInputMap(this.invokerRootPane, this.menuInputMap);
                this.removeUIActionMap(this.invokerRootPane, this.menuActionMap);
                this.invokerRootPane = null;
            }
            this.receivedKeyPressed = false;
        }

        JPopupMenu getActivePopup(MenuElement[] path) {
            for (int i = path.length - 1; i >= 0; --i) {
                MenuElement elem = path[i];
                if (!(elem instanceof JPopupMenu)) continue;
                return (JPopupMenu)elem;
            }
            return null;
        }

        void addUIInputMap(JComponent c, InputMap map) {
            InputMap parent;
            InputMap lastNonUI = null;
            for (parent = c.getInputMap(2); parent != null && !(parent instanceof UIResource); parent = parent.getParent()) {
                lastNonUI = parent;
            }
            if (lastNonUI == null) {
                c.setInputMap(2, map);
            } else {
                lastNonUI.setParent(map);
            }
            map.setParent(parent);
        }

        void addUIActionMap(JComponent c, ActionMap map) {
            ActionMap parent;
            ActionMap lastNonUI = null;
            for (parent = c.getActionMap(); parent != null && !(parent instanceof UIResource); parent = parent.getParent()) {
                lastNonUI = parent;
            }
            if (lastNonUI == null) {
                c.setActionMap(map);
            } else {
                lastNonUI.setParent(map);
            }
            map.setParent(parent);
        }

        void removeUIInputMap(JComponent c, InputMap map) {
            InputMap im = null;
            for (InputMap parent = c.getInputMap(2); parent != null; parent = parent.getParent()) {
                if (parent == map) {
                    if (im == null) {
                        c.setInputMap(2, map.getParent());
                        break;
                    }
                    im.setParent(map.getParent());
                    break;
                }
                im = parent;
            }
        }

        void removeUIActionMap(JComponent c, ActionMap map) {
            ActionMap im = null;
            for (ActionMap parent = c.getActionMap(); parent != null; parent = parent.getParent()) {
                if (parent == map) {
                    if (im == null) {
                        c.setActionMap(map.getParent());
                        break;
                    }
                    im.setParent(map.getParent());
                    break;
                }
                im = parent;
            }
        }

        /*
         * WARNING - void declaration
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public void stateChanged(ChangeEvent ev) {
            if (!(UIManager.getLookAndFeel() instanceof BasicLookAndFeel)) {
                this.uninstall();
                return;
            }
            MenuSelectionManager msm = (MenuSelectionManager)ev.getSource();
            MenuElement[] p = msm.getSelectedPath();
            JPopupMenu popup = this.getActivePopup(p);
            if (popup != null && !popup.isFocusable()) {
                return;
            }
            if (this.lastPathSelected.length != 0 && p.length != 0 && !BasicPopupMenuUI.checkInvokerEqual(p[0], this.lastPathSelected[0])) {
                this.removeItems();
                this.lastPathSelected = new MenuElement[0];
            }
            if (this.lastPathSelected.length == 0 && p.length > 0) {
                void var5_10;
                if (popup == null) {
                    if (p.length != 2 || !(p[0] instanceof JMenuBar) || !(p[1] instanceof JMenu)) return;
                    JComponent jComponent = (JComponent)((Object)p[1]);
                    popup = ((JMenu)jComponent).getPopupMenu();
                } else {
                    Component c = popup.getInvoker();
                    if (c instanceof JFrame) {
                        JRootPane jRootPane = ((JFrame)c).getRootPane();
                    } else if (c instanceof JDialog) {
                        JRootPane jRootPane = ((JDialog)c).getRootPane();
                    } else if (c instanceof JApplet) {
                        JRootPane jRootPane = ((JApplet)c).getRootPane();
                    } else {
                        while (!(c instanceof JComponent)) {
                            if (c == null) {
                                return;
                            }
                            c = c.getParent();
                        }
                        JComponent jComponent = (JComponent)c;
                    }
                }
                this.lastFocused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                this.invokerRootPane = SwingUtilities.getRootPane((Component)var5_10);
                if (this.invokerRootPane != null) {
                    this.invokerRootPane.addFocusListener(this.rootPaneFocusListener);
                    this.invokerRootPane.requestFocus(true);
                    this.invokerRootPane.addKeyListener(this);
                    this.focusTraversalKeysEnabled = this.invokerRootPane.getFocusTraversalKeysEnabled();
                    this.invokerRootPane.setFocusTraversalKeysEnabled(false);
                    this.menuInputMap = BasicPopupMenuUI.getInputMap(popup, this.invokerRootPane);
                    this.addUIInputMap(this.invokerRootPane, this.menuInputMap);
                    this.addUIActionMap(this.invokerRootPane, this.menuActionMap);
                }
            } else if (this.lastPathSelected.length != 0 && p.length == 0) {
                this.removeItems();
                this.menuInputMap = null;
            } else if (popup != this.lastPopup) {
                this.receivedKeyPressed = false;
            }
            this.lastPathSelected = p;
            this.lastPopup = popup;
        }

        @Override
        public void keyPressed(KeyEvent ev) {
            this.receivedKeyPressed = true;
            MenuSelectionManager.defaultManager().processKeyEvent(ev);
        }

        @Override
        public void keyReleased(KeyEvent ev) {
            if (this.receivedKeyPressed) {
                this.receivedKeyPressed = false;
                MenuSelectionManager.defaultManager().processKeyEvent(ev);
            }
        }

        @Override
        public void keyTyped(KeyEvent ev) {
            if (this.receivedKeyPressed) {
                MenuSelectionManager.defaultManager().processKeyEvent(ev);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void uninstall() {
            StringBuilder stringBuilder = MENU_KEYBOARD_HELPER_KEY;
            synchronized (stringBuilder) {
                MenuSelectionManager.defaultManager().removeChangeListener(this);
                AppContext.getAppContext().remove(MENU_KEYBOARD_HELPER_KEY);
            }
        }
    }

    private static class Actions
    extends UIAction {
        private static final String CANCEL = "cancel";
        private static final String SELECT_NEXT = "selectNext";
        private static final String SELECT_PREVIOUS = "selectPrevious";
        private static final String SELECT_PARENT = "selectParent";
        private static final String SELECT_CHILD = "selectChild";
        private static final String RETURN = "return";
        private static final boolean FORWARD = true;
        private static final boolean BACKWARD = false;
        private static final boolean PARENT = false;
        private static final boolean CHILD = true;

        Actions(String key) {
            super(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String key = this.getName();
            if (key == CANCEL) {
                this.cancel();
            } else if (key == SELECT_NEXT) {
                this.selectItem(true);
            } else if (key == SELECT_PREVIOUS) {
                this.selectItem(false);
            } else if (key == SELECT_PARENT) {
                this.selectParentChild(false);
            } else if (key == SELECT_CHILD) {
                this.selectParentChild(true);
            } else if (key == RETURN) {
                this.doReturn();
            }
        }

        private void doReturn() {
            KeyboardFocusManager fmgr = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            Component focusOwner = fmgr.getFocusOwner();
            if (focusOwner != null && !(focusOwner instanceof JRootPane)) {
                return;
            }
            MenuSelectionManager msm = MenuSelectionManager.defaultManager();
            MenuElement[] path = msm.getSelectedPath();
            if (path.length > 0) {
                MenuElement lastElement = path[path.length - 1];
                if (lastElement instanceof JMenu) {
                    MenuElement[] newPath = new MenuElement[path.length + 1];
                    System.arraycopy(path, 0, newPath, 0, path.length);
                    newPath[path.length] = ((JMenu)lastElement).getPopupMenu();
                    msm.setSelectedPath(newPath);
                } else if (lastElement instanceof JMenuItem) {
                    JMenuItem mi = (JMenuItem)lastElement;
                    if (mi.getUI() instanceof BasicMenuItemUI) {
                        ((BasicMenuItemUI)mi.getUI()).doClick(msm);
                    } else {
                        msm.clearSelectedPath();
                        mi.doClick(0);
                    }
                }
            }
        }

        private void selectParentChild(boolean direction) {
            MenuSelectionManager msm = MenuSelectionManager.defaultManager();
            MenuElement[] path = msm.getSelectedPath();
            int len = path.length;
            if (!direction) {
                int popupIndex = len - 1;
                if (len > 2 && (path[popupIndex] instanceof JPopupMenu || path[--popupIndex] instanceof JPopupMenu) && !((JMenu)path[popupIndex - 1]).isTopLevelMenu()) {
                    MenuElement[] newPath = new MenuElement[popupIndex];
                    System.arraycopy(path, 0, newPath, 0, popupIndex);
                    msm.setSelectedPath(newPath);
                    return;
                }
            } else if (len > 0 && path[len - 1] instanceof JMenu && !((JMenu)path[len - 1]).isTopLevelMenu()) {
                MenuElement[] newPath;
                JMenu menu = (JMenu)path[len - 1];
                JPopupMenu popup = menu.getPopupMenu();
                MenuElement[] subs = popup.getSubElements();
                MenuElement item = BasicPopupMenuUI.findEnabledChild(subs, -1, true);
                if (item == null) {
                    newPath = new MenuElement[len + 1];
                } else {
                    newPath = new MenuElement[len + 2];
                    newPath[len + 1] = item;
                }
                System.arraycopy(path, 0, newPath, 0, len);
                newPath[len] = popup;
                msm.setSelectedPath(newPath);
                return;
            }
            if (len > 1 && path[0] instanceof JMenuBar) {
                MenuElement nextMenu;
                MenuElement currentMenu = path[1];
                if (path[0].getComponent().getComponentOrientation().equals(ComponentOrientation.RIGHT_TO_LEFT)) {
                    boolean bl = direction = !direction;
                }
                if ((nextMenu = BasicPopupMenuUI.findEnabledChild(path[0].getSubElements(), currentMenu, direction)) != null && nextMenu != currentMenu) {
                    MenuElement[] newSelection = len == 2 ? new MenuElement[]{path[0], nextMenu} : new MenuElement[]{path[0], nextMenu, ((JMenu)nextMenu).getPopupMenu()};
                    msm.setSelectedPath(newSelection);
                }
            }
        }

        private void selectItem(boolean direction) {
            MenuSelectionManager msm = MenuSelectionManager.defaultManager();
            MenuElement[] path = msm.getSelectedPath();
            if (path.length == 0) {
                return;
            }
            int len = path.length;
            if (len == 1 && path[0] instanceof JPopupMenu) {
                JPopupMenu popup = (JPopupMenu)path[0];
                MenuElement[] newPath = new MenuElement[]{popup, BasicPopupMenuUI.findEnabledChild(popup.getSubElements(), -1, direction)};
                msm.setSelectedPath(newPath);
            } else if (len == 2 && path[0] instanceof JMenuBar && path[1] instanceof JMenu) {
                MenuElement[] newPath;
                JPopupMenu popup = ((JMenu)path[1]).getPopupMenu();
                MenuElement next = BasicPopupMenuUI.findEnabledChild(popup.getSubElements(), -1, true);
                if (next != null) {
                    newPath = new MenuElement[4];
                    newPath[3] = next;
                } else {
                    newPath = new MenuElement[3];
                }
                System.arraycopy(path, 0, newPath, 0, 2);
                newPath[2] = popup;
                msm.setSelectedPath(newPath);
            } else if (path[len - 1] instanceof JPopupMenu && path[len - 2] instanceof JMenu) {
                JMenu menu = (JMenu)path[len - 2];
                JPopupMenu popup = menu.getPopupMenu();
                MenuElement next = BasicPopupMenuUI.findEnabledChild(popup.getSubElements(), -1, direction);
                if (next != null) {
                    MenuElement[] newPath = new MenuElement[len + 1];
                    System.arraycopy(path, 0, newPath, 0, len);
                    newPath[len] = next;
                    msm.setSelectedPath(newPath);
                } else if (len > 2 && path[len - 3] instanceof JPopupMenu && (next = BasicPopupMenuUI.findEnabledChild((popup = (JPopupMenu)path[len - 3]).getSubElements(), menu, direction)) != null && next != menu) {
                    MenuElement[] newPath = new MenuElement[len - 1];
                    System.arraycopy(path, 0, newPath, 0, len - 2);
                    newPath[len - 2] = next;
                    msm.setSelectedPath(newPath);
                }
            } else {
                MenuElement[] subs = path[len - 2].getSubElements();
                MenuElement nextChild = BasicPopupMenuUI.findEnabledChild(subs, path[len - 1], direction);
                if (nextChild == null) {
                    nextChild = BasicPopupMenuUI.findEnabledChild(subs, -1, direction);
                }
                if (nextChild != null) {
                    path[len - 1] = nextChild;
                    msm.setSelectedPath(path);
                }
            }
        }

        private void cancel() {
            String mode;
            JPopupMenu lastPopup = BasicPopupMenuUI.getLastPopup();
            if (lastPopup != null) {
                lastPopup.putClientProperty("JPopupMenu.firePopupMenuCanceled", Boolean.TRUE);
            }
            if ("hideMenuTree".equals(mode = UIManager.getString("Menu.cancelMode"))) {
                MenuSelectionManager.defaultManager().clearSelectedPath();
            } else {
                this.shortenSelectedPath();
            }
        }

        private void shortenSelectedPath() {
            MenuElement previousElement;
            MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
            if (path.length <= 2) {
                MenuSelectionManager.defaultManager().clearSelectedPath();
                return;
            }
            int value = 2;
            MenuElement lastElement = path[path.length - 1];
            JPopupMenu lastPopup = BasicPopupMenuUI.getLastPopup();
            if (lastElement == lastPopup && (previousElement = path[path.length - 2]) instanceof JMenu) {
                JMenu lastMenu = (JMenu)previousElement;
                value = lastMenu.isEnabled() && lastPopup.getComponentCount() > 0 ? 1 : 3;
            }
            if (path.length - value <= 2 && !UIManager.getBoolean("Menu.preserveTopLevelSelection")) {
                value = path.length;
            }
            MenuElement[] newPath = new MenuElement[path.length - value];
            System.arraycopy(path, 0, newPath, 0, path.length - value);
            MenuSelectionManager.defaultManager().setSelectedPath(newPath);
        }
    }
}

