/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleValue;
import javax.swing.Icon;
import javax.swing.InternalFrameFocusTraversalPolicy;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.DesktopIconUI;
import javax.swing.plaf.InternalFrameUI;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.UngrabEvent;
import sun.swing.SwingUtilities2;

@JavaBean(defaultProperty="JMenuBar", description="A frame container which is contained within another window.")
@SwingContainer(delegate="getContentPane")
public class JInternalFrame
extends JComponent
implements Accessible,
WindowConstants,
RootPaneContainer {
    private static final String uiClassID = "InternalFrameUI";
    protected JRootPane rootPane;
    protected boolean rootPaneCheckingEnabled = false;
    protected boolean closable;
    protected boolean isClosed;
    protected boolean maximizable;
    protected boolean isMaximum;
    protected boolean iconable;
    protected boolean isIcon;
    protected boolean resizable;
    protected boolean isSelected;
    protected Icon frameIcon;
    protected String title;
    protected JDesktopIcon desktopIcon;
    private Cursor lastCursor;
    private boolean opened;
    private Rectangle normalBounds = null;
    private int defaultCloseOperation = 2;
    private Component lastFocusOwner;
    public static final String CONTENT_PANE_PROPERTY = "contentPane";
    public static final String MENU_BAR_PROPERTY = "JMenuBar";
    public static final String TITLE_PROPERTY = "title";
    public static final String LAYERED_PANE_PROPERTY = "layeredPane";
    public static final String ROOT_PANE_PROPERTY = "rootPane";
    public static final String GLASS_PANE_PROPERTY = "glassPane";
    public static final String FRAME_ICON_PROPERTY = "frameIcon";
    public static final String IS_SELECTED_PROPERTY = "selected";
    public static final String IS_CLOSED_PROPERTY = "closed";
    public static final String IS_MAXIMUM_PROPERTY = "maximum";
    public static final String IS_ICON_PROPERTY = "icon";
    private static final Object PROPERTY_CHANGE_LISTENER_KEY = new StringBuilder("InternalFramePropertyChangeListener");
    boolean isDragging = false;
    boolean danger = false;

    private static void addPropertyChangeListenerIfNecessary() {
        if (AppContext.getAppContext().get(PROPERTY_CHANGE_LISTENER_KEY) == null) {
            FocusPropertyChangeListener focusListener = new FocusPropertyChangeListener();
            AppContext.getAppContext().put(PROPERTY_CHANGE_LISTENER_KEY, focusListener);
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(focusListener);
        }
    }

    private static void updateLastFocusOwner(Component component) {
        if (component != null) {
            for (Component parent = component; parent != null && !(parent instanceof Window); parent = parent.getParent()) {
                if (!(parent instanceof JInternalFrame)) continue;
                ((JInternalFrame)parent).setLastFocusOwner(component);
            }
        }
    }

    public JInternalFrame() {
        this("", false, false, false, false);
    }

    public JInternalFrame(String title) {
        this(title, false, false, false, false);
    }

    public JInternalFrame(String title, boolean resizable) {
        this(title, resizable, false, false, false);
    }

    public JInternalFrame(String title, boolean resizable, boolean closable) {
        this(title, resizable, closable, false, false);
    }

    public JInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable) {
        this(title, resizable, closable, maximizable, false);
    }

    public JInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
        this.setRootPane(this.createRootPane());
        this.setLayout(new BorderLayout());
        this.title = title;
        this.resizable = resizable;
        this.closable = closable;
        this.maximizable = maximizable;
        this.isMaximum = false;
        this.iconable = iconifiable;
        this.isIcon = false;
        this.setVisible(false);
        this.setRootPaneCheckingEnabled(true);
        this.desktopIcon = new JDesktopIcon(this);
        this.updateUI();
        SunToolkit.checkAndSetPolicy(this);
        JInternalFrame.addPropertyChangeListenerIfNecessary();
    }

    protected JRootPane createRootPane() {
        return new JRootPane();
    }

    @Override
    public InternalFrameUI getUI() {
        return (InternalFrameUI)this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel.")
    public void setUI(InternalFrameUI ui) {
        boolean checkingEnabled = this.isRootPaneCheckingEnabled();
        try {
            this.setRootPaneCheckingEnabled(false);
            super.setUI(ui);
        }
        finally {
            this.setRootPaneCheckingEnabled(checkingEnabled);
        }
    }

    @Override
    public void updateUI() {
        this.setUI((InternalFrameUI)UIManager.getUI(this));
        this.invalidate();
        if (this.desktopIcon != null) {
            this.desktopIcon.updateUIWhenHidden();
        }
    }

    void updateUIWhenHidden() {
        this.setUI((InternalFrameUI)UIManager.getUI(this));
        this.invalidate();
        Component[] children = this.getComponents();
        if (children != null) {
            for (Component child : children) {
                SwingUtilities.updateComponentTreeUI(child);
            }
        }
    }

    @Override
    @BeanProperty(bound=false, description="UIClassID")
    public String getUIClassID() {
        return uiClassID;
    }

    protected boolean isRootPaneCheckingEnabled() {
        return this.rootPaneCheckingEnabled;
    }

    @BeanProperty(hidden=true, description="Whether the add and setLayout methods are forwarded")
    protected void setRootPaneCheckingEnabled(boolean enabled) {
        this.rootPaneCheckingEnabled = enabled;
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        if (this.isRootPaneCheckingEnabled()) {
            this.getContentPane().add(comp, constraints, index);
        } else {
            super.addImpl(comp, constraints, index);
        }
    }

    @Override
    public void remove(Component comp) {
        int oldCount = this.getComponentCount();
        super.remove(comp);
        if (oldCount == this.getComponentCount()) {
            this.getContentPane().remove(comp);
        }
    }

    @Override
    public void setLayout(LayoutManager manager) {
        if (this.isRootPaneCheckingEnabled()) {
            this.getContentPane().setLayout(manager);
        } else {
            super.setLayout(manager);
        }
    }

    @Deprecated
    public JMenuBar getMenuBar() {
        return this.getRootPane().getMenuBar();
    }

    public JMenuBar getJMenuBar() {
        return this.getRootPane().getJMenuBar();
    }

    @Deprecated
    public void setMenuBar(JMenuBar m) {
        JMenuBar oldValue = this.getMenuBar();
        this.getRootPane().setJMenuBar(m);
        this.firePropertyChange(MENU_BAR_PROPERTY, oldValue, m);
    }

    @BeanProperty(preferred=true, description="The menu bar for accessing pulldown menus from this internal frame.")
    public void setJMenuBar(JMenuBar m) {
        JMenuBar oldValue = this.getMenuBar();
        this.getRootPane().setJMenuBar(m);
        this.firePropertyChange(MENU_BAR_PROPERTY, oldValue, m);
    }

    @Override
    public Container getContentPane() {
        return this.getRootPane().getContentPane();
    }

    @Override
    @BeanProperty(hidden=true, description="The client area of the internal frame where child components are normally inserted.")
    public void setContentPane(Container c) {
        Container oldValue = this.getContentPane();
        this.getRootPane().setContentPane(c);
        this.firePropertyChange(CONTENT_PANE_PROPERTY, oldValue, c);
    }

    @Override
    public JLayeredPane getLayeredPane() {
        return this.getRootPane().getLayeredPane();
    }

    @Override
    @BeanProperty(hidden=true, description="The pane which holds the various desktop layers.")
    public void setLayeredPane(JLayeredPane layered) {
        JLayeredPane oldValue = this.getLayeredPane();
        this.getRootPane().setLayeredPane(layered);
        this.firePropertyChange(LAYERED_PANE_PROPERTY, oldValue, layered);
    }

    @Override
    public Component getGlassPane() {
        return this.getRootPane().getGlassPane();
    }

    @Override
    @BeanProperty(hidden=true, description="A transparent pane used for menu rendering.")
    public void setGlassPane(Component glass) {
        Component oldValue = this.getGlassPane();
        this.getRootPane().setGlassPane(glass);
        this.firePropertyChange(GLASS_PANE_PROPERTY, oldValue, glass);
    }

    @Override
    @BeanProperty(hidden=true, description="The root pane used by this internal frame.")
    public JRootPane getRootPane() {
        return this.rootPane;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void setRootPane(JRootPane root) {
        if (this.rootPane != null) {
            this.remove(this.rootPane);
        }
        JRootPane oldValue = this.getRootPane();
        this.rootPane = root;
        if (this.rootPane != null) {
            boolean checkingEnabled = this.isRootPaneCheckingEnabled();
            try {
                this.setRootPaneCheckingEnabled(false);
                this.add((Component)this.rootPane, "Center");
            }
            finally {
                this.setRootPaneCheckingEnabled(checkingEnabled);
            }
        }
        this.firePropertyChange(ROOT_PANE_PROPERTY, oldValue, root);
    }

    @BeanProperty(preferred=true, description="Indicates whether this internal frame can be closed.")
    public void setClosable(boolean b) {
        Boolean oldValue = this.closable ? Boolean.TRUE : Boolean.FALSE;
        Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
        this.closable = b;
        this.firePropertyChange("closable", oldValue, newValue);
    }

    public boolean isClosable() {
        return this.closable;
    }

    public boolean isClosed() {
        return this.isClosed;
    }

    @BeanProperty(description="Indicates whether this internal frame has been closed.")
    public void setClosed(boolean b) throws PropertyVetoException {
        Boolean newValue;
        if (this.isClosed == b) {
            return;
        }
        Boolean oldValue = this.isClosed ? Boolean.TRUE : Boolean.FALSE;
        Boolean bl = newValue = b ? Boolean.TRUE : Boolean.FALSE;
        if (b) {
            this.fireInternalFrameEvent(25550);
        }
        this.fireVetoableChange(IS_CLOSED_PROPERTY, oldValue, newValue);
        this.isClosed = b;
        if (this.isClosed) {
            this.setVisible(false);
        }
        this.firePropertyChange(IS_CLOSED_PROPERTY, oldValue, newValue);
        if (this.isClosed) {
            this.dispose();
        } else if (!this.opened) {
            // empty if block
        }
    }

    @BeanProperty(preferred=true, description="Determines whether this internal frame can be resized by the user.")
    public void setResizable(boolean b) {
        Boolean oldValue = this.resizable ? Boolean.TRUE : Boolean.FALSE;
        Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
        this.resizable = b;
        this.firePropertyChange("resizable", oldValue, newValue);
    }

    public boolean isResizable() {
        return this.isMaximum ? false : this.resizable;
    }

    @BeanProperty(preferred=true, description="Determines whether this internal frame can be iconified.")
    public void setIconifiable(boolean b) {
        Boolean oldValue = this.iconable ? Boolean.TRUE : Boolean.FALSE;
        Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
        this.iconable = b;
        this.firePropertyChange("iconable", oldValue, newValue);
    }

    public boolean isIconifiable() {
        return this.iconable;
    }

    public boolean isIcon() {
        return this.isIcon;
    }

    @BeanProperty(description="The image displayed when this internal frame is minimized.")
    public void setIcon(boolean b) throws PropertyVetoException {
        if (this.isIcon == b) {
            return;
        }
        this.firePropertyChange("ancestor", null, this.getParent());
        Boolean oldValue = this.isIcon ? Boolean.TRUE : Boolean.FALSE;
        Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
        this.fireVetoableChange(IS_ICON_PROPERTY, oldValue, newValue);
        this.isIcon = b;
        this.firePropertyChange(IS_ICON_PROPERTY, oldValue, newValue);
        if (b) {
            this.fireInternalFrameEvent(25552);
        } else {
            this.fireInternalFrameEvent(25553);
        }
    }

    @BeanProperty(preferred=true, description="Determines whether this internal frame can be maximized.")
    public void setMaximizable(boolean b) {
        Boolean oldValue = this.maximizable ? Boolean.TRUE : Boolean.FALSE;
        Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
        this.maximizable = b;
        this.firePropertyChange("maximizable", oldValue, newValue);
    }

    public boolean isMaximizable() {
        return this.maximizable;
    }

    public boolean isMaximum() {
        return this.isMaximum;
    }

    @BeanProperty(description="Indicates whether this internal frame is maximized.")
    public void setMaximum(boolean b) throws PropertyVetoException {
        if (this.isMaximum == b) {
            return;
        }
        Boolean oldValue = this.isMaximum ? Boolean.TRUE : Boolean.FALSE;
        Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
        this.fireVetoableChange(IS_MAXIMUM_PROPERTY, oldValue, newValue);
        this.isMaximum = b;
        this.firePropertyChange(IS_MAXIMUM_PROPERTY, oldValue, newValue);
    }

    public String getTitle() {
        return this.title;
    }

    @BeanProperty(preferred=true, description="The text displayed in the title bar.")
    public void setTitle(String title) {
        String oldValue = this.title;
        this.title = title;
        this.firePropertyChange(TITLE_PROPERTY, oldValue, title);
    }

    @BeanProperty(description="Indicates whether this internal frame is currently the active frame.")
    public void setSelected(boolean selected) throws PropertyVetoException {
        if (selected && this.isSelected) {
            this.restoreSubcomponentFocus();
            return;
        }
        if (this.isSelected == selected || selected && (this.isIcon ? !this.desktopIcon.isShowing() : !this.isShowing())) {
            return;
        }
        Boolean oldValue = this.isSelected ? Boolean.TRUE : Boolean.FALSE;
        Boolean newValue = selected ? Boolean.TRUE : Boolean.FALSE;
        this.fireVetoableChange(IS_SELECTED_PROPERTY, oldValue, newValue);
        if (selected) {
            this.restoreSubcomponentFocus();
        }
        this.isSelected = selected;
        this.firePropertyChange(IS_SELECTED_PROPERTY, oldValue, newValue);
        if (this.isSelected) {
            this.fireInternalFrameEvent(25554);
        } else {
            this.fireInternalFrameEvent(25555);
            try {
                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new UngrabEvent(this));
            }
            catch (SecurityException e) {
                this.dispatchEvent(new UngrabEvent(this));
            }
        }
        this.repaint();
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    @BeanProperty(description="The icon shown in the top-left corner of this internal frame.")
    public void setFrameIcon(Icon icon) {
        Icon oldIcon = this.frameIcon;
        this.frameIcon = icon;
        this.firePropertyChange(FRAME_ICON_PROPERTY, oldIcon, icon);
    }

    public Icon getFrameIcon() {
        return this.frameIcon;
    }

    public void moveToFront() {
        if (this.isIcon()) {
            if (this.getDesktopIcon().getParent() instanceof JLayeredPane) {
                ((JLayeredPane)this.getDesktopIcon().getParent()).moveToFront(this.getDesktopIcon());
            }
        } else if (this.getParent() instanceof JLayeredPane) {
            ((JLayeredPane)this.getParent()).moveToFront(this);
        }
    }

    public void moveToBack() {
        if (this.isIcon()) {
            if (this.getDesktopIcon().getParent() instanceof JLayeredPane) {
                ((JLayeredPane)this.getDesktopIcon().getParent()).moveToBack(this.getDesktopIcon());
            }
        } else if (this.getParent() instanceof JLayeredPane) {
            ((JLayeredPane)this.getParent()).moveToBack(this);
        }
    }

    @BeanProperty(bound=false)
    public Cursor getLastCursor() {
        return this.lastCursor;
    }

    @Override
    public void setCursor(Cursor cursor) {
        if (cursor == null) {
            this.lastCursor = null;
            super.setCursor(cursor);
            return;
        }
        int type = cursor.getType();
        if (type != 4 && type != 5 && type != 6 && type != 7 && type != 8 && type != 9 && type != 10 && type != 11) {
            this.lastCursor = cursor;
        }
        super.setCursor(cursor);
    }

    @BeanProperty(bound=false, expert=true, description="Specifies what desktop layer is used.")
    public void setLayer(Integer layer) {
        Container container = this.getParent();
        if (container instanceof JLayeredPane) {
            JLayeredPane p = (JLayeredPane)container;
            p.setLayer(this, layer, p.getPosition(this));
        } else {
            JLayeredPane.putLayer(this, layer);
            if (this.getParent() != null) {
                this.getParent().repaint(this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }
        }
    }

    @BeanProperty(bound=false, expert=true, description="Specifies what desktop layer is used.")
    public void setLayer(int layer) {
        this.setLayer((Integer)layer);
    }

    public int getLayer() {
        return JLayeredPane.getLayer(this);
    }

    @BeanProperty(bound=false)
    public JDesktopPane getDesktopPane() {
        Container p;
        for (p = this.getParent(); p != null && !(p instanceof JDesktopPane); p = p.getParent()) {
        }
        if (p == null) {
            for (p = this.getDesktopIcon().getParent(); p != null && !(p instanceof JDesktopPane); p = p.getParent()) {
            }
        }
        return (JDesktopPane)p;
    }

    @BeanProperty(description="The icon shown when this internal frame is minimized.")
    public void setDesktopIcon(JDesktopIcon d) {
        if (d == null) {
            throw new NullPointerException("JDesktopIcon is null");
        }
        JDesktopIcon oldValue = this.getDesktopIcon();
        this.desktopIcon = d;
        this.firePropertyChange("desktopIcon", oldValue, d);
    }

    public JDesktopIcon getDesktopIcon() {
        return this.desktopIcon;
    }

    public Rectangle getNormalBounds() {
        if (this.normalBounds != null) {
            return this.normalBounds;
        }
        return this.getBounds();
    }

    public void setNormalBounds(Rectangle r) {
        this.normalBounds = r;
    }

    public Component getFocusOwner() {
        if (this.isSelected()) {
            return this.lastFocusOwner;
        }
        return null;
    }

    @BeanProperty(bound=false)
    public Component getMostRecentFocusOwner() {
        if (this.isSelected()) {
            return this.getFocusOwner();
        }
        if (this.lastFocusOwner != null) {
            return this.lastFocusOwner;
        }
        FocusTraversalPolicy policy = this.getFocusTraversalPolicy();
        if (policy instanceof InternalFrameFocusTraversalPolicy) {
            return ((InternalFrameFocusTraversalPolicy)policy).getInitialComponent(this);
        }
        Component toFocus = policy.getDefaultComponent(this);
        if (toFocus != null) {
            return toFocus;
        }
        return this.getContentPane();
    }

    public void restoreSubcomponentFocus() {
        if (this.isIcon()) {
            SwingUtilities2.compositeRequestFocus(this.getDesktopIcon());
        } else {
            Component component = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
            if (component == null || !SwingUtilities.isDescendingFrom(component, this)) {
                this.setLastFocusOwner(this.getMostRecentFocusOwner());
                if (this.lastFocusOwner == null) {
                    this.setLastFocusOwner(this.getContentPane());
                }
                this.lastFocusOwner.requestFocus();
            }
        }
    }

    private void setLastFocusOwner(Component component) {
        this.lastFocusOwner = component;
    }

    @Override
    public void reshape(int x, int y, int width, int height) {
        super.reshape(x, y, width, height);
        this.validate();
        this.repaint();
    }

    public void addInternalFrameListener(InternalFrameListener l) {
        this.listenerList.add(InternalFrameListener.class, l);
        this.enableEvents(0L);
    }

    public void removeInternalFrameListener(InternalFrameListener l) {
        this.listenerList.remove(InternalFrameListener.class, l);
    }

    @BeanProperty(bound=false)
    public InternalFrameListener[] getInternalFrameListeners() {
        return (InternalFrameListener[])this.listenerList.getListeners(InternalFrameListener.class);
    }

    protected void fireInternalFrameEvent(int id) {
        Object[] listeners = this.listenerList.getListenerList();
        AWTEvent e = null;
        block9: for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != InternalFrameListener.class) continue;
            if (e == null) {
                e = new InternalFrameEvent(this, id);
            }
            switch (e.getID()) {
                case 25549: {
                    ((InternalFrameListener)listeners[i + 1]).internalFrameOpened((InternalFrameEvent)e);
                    continue block9;
                }
                case 25550: {
                    ((InternalFrameListener)listeners[i + 1]).internalFrameClosing((InternalFrameEvent)e);
                    continue block9;
                }
                case 25551: {
                    ((InternalFrameListener)listeners[i + 1]).internalFrameClosed((InternalFrameEvent)e);
                    continue block9;
                }
                case 25552: {
                    ((InternalFrameListener)listeners[i + 1]).internalFrameIconified((InternalFrameEvent)e);
                    continue block9;
                }
                case 25553: {
                    ((InternalFrameListener)listeners[i + 1]).internalFrameDeiconified((InternalFrameEvent)e);
                    continue block9;
                }
                case 25554: {
                    ((InternalFrameListener)listeners[i + 1]).internalFrameActivated((InternalFrameEvent)e);
                    continue block9;
                }
                case 25555: {
                    ((InternalFrameListener)listeners[i + 1]).internalFrameDeactivated((InternalFrameEvent)e);
                    continue block9;
                }
            }
        }
    }

    public void doDefaultCloseAction() {
        this.fireInternalFrameEvent(25550);
        switch (this.defaultCloseOperation) {
            case 0: {
                break;
            }
            case 1: {
                this.setVisible(false);
                if (!this.isSelected()) break;
                try {
                    this.setSelected(false);
                }
                catch (PropertyVetoException propertyVetoException) {}
                break;
            }
            case 2: {
                try {
                    this.fireVetoableChange(IS_CLOSED_PROPERTY, Boolean.FALSE, Boolean.TRUE);
                    this.isClosed = true;
                    this.setVisible(false);
                    this.firePropertyChange(IS_CLOSED_PROPERTY, Boolean.FALSE, Boolean.TRUE);
                    this.dispose();
                }
                catch (PropertyVetoException propertyVetoException) {}
                break;
            }
        }
    }

    public void setDefaultCloseOperation(int operation) {
        this.defaultCloseOperation = operation;
    }

    public int getDefaultCloseOperation() {
        return this.defaultCloseOperation;
    }

    public void pack() {
        try {
            if (this.isIcon()) {
                this.setIcon(false);
            } else if (this.isMaximum()) {
                this.setMaximum(false);
            }
        }
        catch (PropertyVetoException e) {
            return;
        }
        this.setSize(this.getPreferredSize());
        this.validate();
    }

    @Override
    public void show() {
        if (this.isVisible()) {
            return;
        }
        if (!this.opened) {
            this.fireInternalFrameEvent(25549);
            this.opened = true;
        }
        this.getDesktopIcon().setVisible(true);
        this.toFront();
        super.show();
        if (this.isIcon) {
            return;
        }
        if (!this.isSelected()) {
            try {
                this.setSelected(true);
            }
            catch (PropertyVetoException propertyVetoException) {
                // empty catch block
            }
        }
    }

    @Override
    public void hide() {
        if (this.isIcon()) {
            this.getDesktopIcon().setVisible(false);
        }
        super.hide();
    }

    public void dispose() {
        if (this.isVisible()) {
            this.setVisible(false);
        }
        if (!this.isClosed) {
            this.firePropertyChange(IS_CLOSED_PROPERTY, Boolean.FALSE, Boolean.TRUE);
            this.isClosed = true;
        }
        this.fireInternalFrameEvent(25551);
        try {
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new UngrabEvent(this));
        }
        catch (SecurityException e) {
            this.dispatchEvent(new UngrabEvent(this));
        }
    }

    public void toFront() {
        this.moveToFront();
    }

    public void toBack() {
        this.moveToBack();
    }

    @Override
    public final void setFocusCycleRoot(boolean focusCycleRoot) {
    }

    @Override
    public final boolean isFocusCycleRoot() {
        return true;
    }

    @Override
    @BeanProperty(bound=false)
    public final Container getFocusCycleRootAncestor() {
        return null;
    }

    @BeanProperty(bound=false)
    public final String getWarningString() {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (this.getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            count = (byte)(count - 1);
            JComponent.setWriteObjCounter(this, count);
            if (count == 0 && this.ui != null) {
                boolean old = this.isRootPaneCheckingEnabled();
                try {
                    this.setRootPaneCheckingEnabled(false);
                    this.ui.installUI(this);
                }
                finally {
                    this.setRootPaneCheckingEnabled(old);
                }
            }
        }
    }

    @Override
    void compWriteObjectNotify() {
        boolean old = this.isRootPaneCheckingEnabled();
        try {
            this.setRootPaneCheckingEnabled(false);
            super.compWriteObjectNotify();
        }
        finally {
            this.setRootPaneCheckingEnabled(old);
        }
    }

    @Override
    protected String paramString() {
        String openedString;
        String rootPaneString = this.rootPane != null ? this.rootPane.toString() : "";
        String rootPaneCheckingEnabledString = this.rootPaneCheckingEnabled ? "true" : "false";
        String closableString = this.closable ? "true" : "false";
        String isClosedString = this.isClosed ? "true" : "false";
        String maximizableString = this.maximizable ? "true" : "false";
        String isMaximumString = this.isMaximum ? "true" : "false";
        String iconableString = this.iconable ? "true" : "false";
        String isIconString = this.isIcon ? "true" : "false";
        String resizableString = this.resizable ? "true" : "false";
        String isSelectedString = this.isSelected ? "true" : "false";
        String frameIconString = this.frameIcon != null ? this.frameIcon.toString() : "";
        String titleString = this.title != null ? this.title : "";
        String desktopIconString = this.desktopIcon != null ? this.desktopIcon.toString() : "";
        String string = openedString = this.opened ? "true" : "false";
        String defaultCloseOperationString = this.defaultCloseOperation == 1 ? "HIDE_ON_CLOSE" : (this.defaultCloseOperation == 2 ? "DISPOSE_ON_CLOSE" : (this.defaultCloseOperation == 0 ? "DO_NOTHING_ON_CLOSE" : ""));
        return super.paramString() + ",closable=" + closableString + ",defaultCloseOperation=" + defaultCloseOperationString + ",desktopIcon=" + desktopIconString + ",frameIcon=" + frameIconString + ",iconable=" + iconableString + ",isClosed=" + isClosedString + ",isIcon=" + isIconString + ",isMaximum=" + isMaximumString + ",isSelected=" + isSelectedString + ",maximizable=" + maximizableString + ",opened=" + openedString + ",resizable=" + resizableString + ",rootPane=" + rootPaneString + ",rootPaneCheckingEnabled=" + rootPaneCheckingEnabledString + ",title=" + titleString;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (this.isDragging) {
            this.danger = true;
        }
        super.paintComponent(g);
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJInternalFrame();
        }
        return this.accessibleContext;
    }

    private static class FocusPropertyChangeListener
    implements PropertyChangeListener {
        private FocusPropertyChangeListener() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName() == "permanentFocusOwner") {
                JInternalFrame.updateLastFocusOwner((Component)e.getNewValue());
            }
        }
    }

    public static class JDesktopIcon
    extends JComponent
    implements Accessible {
        JInternalFrame internalFrame;

        public JDesktopIcon(JInternalFrame f) {
            this.setVisible(false);
            this.setInternalFrame(f);
            this.updateUI();
        }

        @Override
        public DesktopIconUI getUI() {
            return (DesktopIconUI)this.ui;
        }

        public void setUI(DesktopIconUI ui) {
            super.setUI(ui);
        }

        public JInternalFrame getInternalFrame() {
            return this.internalFrame;
        }

        public void setInternalFrame(JInternalFrame f) {
            this.internalFrame = f;
        }

        public JDesktopPane getDesktopPane() {
            if (this.getInternalFrame() != null) {
                return this.getInternalFrame().getDesktopPane();
            }
            return null;
        }

        @Override
        public void updateUI() {
            boolean hadUI = this.ui != null;
            this.setUI((DesktopIconUI)UIManager.getUI(this));
            this.invalidate();
            Dimension r = this.getPreferredSize();
            this.setSize(r.width, r.height);
            if (this.internalFrame != null && this.internalFrame.getUI() != null) {
                SwingUtilities.updateComponentTreeUI(this.internalFrame);
            }
        }

        void updateUIWhenHidden() {
            this.setUI((DesktopIconUI)UIManager.getUI(this));
            Dimension r = this.getPreferredSize();
            this.setSize(r.width, r.height);
            this.invalidate();
            Component[] children = this.getComponents();
            if (children != null) {
                for (Component child : children) {
                    SwingUtilities.updateComponentTreeUI(child);
                }
            }
        }

        @Override
        public String getUIClassID() {
            return "DesktopIconUI";
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            if (this.getUIClassID().equals("DesktopIconUI")) {
                byte count = JComponent.getWriteObjCounter(this);
                count = (byte)(count - 1);
                JComponent.setWriteObjCounter(this, count);
                if (count == 0 && this.ui != null) {
                    this.ui.installUI(this);
                }
            }
        }

        @Override
        public AccessibleContext getAccessibleContext() {
            if (this.accessibleContext == null) {
                this.accessibleContext = new AccessibleJDesktopIcon();
            }
            return this.accessibleContext;
        }

        protected class AccessibleJDesktopIcon
        extends JComponent.AccessibleJComponent
        implements AccessibleValue {
            protected AccessibleJDesktopIcon() {
            }

            @Override
            public AccessibleRole getAccessibleRole() {
                return AccessibleRole.DESKTOP_ICON;
            }

            @Override
            public AccessibleValue getAccessibleValue() {
                return this;
            }

            @Override
            public Number getCurrentAccessibleValue() {
                AccessibleContext a = JDesktopIcon.this.getInternalFrame().getAccessibleContext();
                AccessibleValue v = a.getAccessibleValue();
                if (v != null) {
                    return v.getCurrentAccessibleValue();
                }
                return null;
            }

            @Override
            public boolean setCurrentAccessibleValue(Number n) {
                if (n == null) {
                    return false;
                }
                AccessibleContext a = JDesktopIcon.this.getInternalFrame().getAccessibleContext();
                AccessibleValue v = a.getAccessibleValue();
                if (v != null) {
                    return v.setCurrentAccessibleValue(n);
                }
                return false;
            }

            @Override
            public Number getMinimumAccessibleValue() {
                AccessibleContext a = JDesktopIcon.this.getInternalFrame().getAccessibleContext();
                if (a instanceof AccessibleValue) {
                    return ((AccessibleValue)((Object)a)).getMinimumAccessibleValue();
                }
                return null;
            }

            @Override
            public Number getMaximumAccessibleValue() {
                AccessibleContext a = JDesktopIcon.this.getInternalFrame().getAccessibleContext();
                if (a instanceof AccessibleValue) {
                    return ((AccessibleValue)((Object)a)).getMaximumAccessibleValue();
                }
                return null;
            }
        }
    }

    protected class AccessibleJInternalFrame
    extends JComponent.AccessibleJComponent
    implements AccessibleValue {
        protected AccessibleJInternalFrame() {
        }

        @Override
        public String getAccessibleName() {
            String name = this.accessibleName;
            if (name == null) {
                name = (String)JInternalFrame.this.getClientProperty("AccessibleName");
            }
            if (name == null) {
                name = JInternalFrame.this.getTitle();
            }
            return name;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.INTERNAL_FRAME;
        }

        @Override
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        @Override
        public Number getCurrentAccessibleValue() {
            return JInternalFrame.this.getLayer();
        }

        @Override
        public boolean setCurrentAccessibleValue(Number n) {
            if (n == null) {
                return false;
            }
            JInternalFrame.this.setLayer((Integer)n.intValue());
            return true;
        }

        @Override
        public Number getMinimumAccessibleValue() {
            return Integer.MIN_VALUE;
        }

        @Override
        public Number getMaximumAccessibleValue() {
            return Integer.MAX_VALUE;
        }
    }
}

