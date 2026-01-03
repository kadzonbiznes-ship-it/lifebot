/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Vector;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleState;
import javax.swing.Action;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.PopupMenuUI;
import javax.swing.plaf.basic.BasicComboPopup;
import sun.awt.SunToolkit;
import sun.security.action.GetPropertyAction;

@JavaBean(defaultProperty="UI", description="A small window that pops up and displays a series of choices.")
@SwingContainer(value=false)
public class JPopupMenu
extends JComponent
implements Accessible,
MenuElement {
    private static final String uiClassID = "PopupMenuUI";
    private static final Object defaultLWPopupEnabledKey = new StringBuffer("JPopupMenu.defaultLWPopupEnabledKey");
    static boolean popupPostionFixDisabled = AccessController.doPrivileged(new GetPropertyAction("javax.swing.adjustPopupLocationToFit", "")).equals("false");
    transient Component invoker;
    transient Popup popup;
    transient Frame frame;
    private int desiredLocationX;
    private int desiredLocationY;
    private String label = null;
    private boolean paintBorder = true;
    private Insets margin = null;
    private boolean lightWeightPopup = true;
    private SingleSelectionModel selectionModel;
    private static final Object classLock = new Object();
    private static final boolean TRACE = false;
    private static final boolean VERBOSE = false;
    private static final boolean DEBUG = false;

    public static void setDefaultLightWeightPopupEnabled(boolean aFlag) {
        SwingUtilities.appContextPut(defaultLWPopupEnabledKey, aFlag);
    }

    public static boolean getDefaultLightWeightPopupEnabled() {
        Boolean b = (Boolean)SwingUtilities.appContextGet(defaultLWPopupEnabledKey);
        if (b == null) {
            SwingUtilities.appContextPut(defaultLWPopupEnabledKey, Boolean.TRUE);
            return true;
        }
        return b;
    }

    public JPopupMenu() {
        this(null);
    }

    public JPopupMenu(String label) {
        this.label = label;
        this.lightWeightPopup = JPopupMenu.getDefaultLightWeightPopupEnabled();
        this.setSelectionModel(new DefaultSingleSelectionModel());
        this.enableEvents(16L);
        this.setFocusTraversalKeysEnabled(false);
        this.updateUI();
    }

    @Override
    public PopupMenuUI getUI() {
        return (PopupMenuUI)this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel.")
    public void setUI(PopupMenuUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        this.setUI((PopupMenuUI)UIManager.getUI(this));
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    @Override
    protected void processFocusEvent(FocusEvent evt) {
        super.processFocusEvent(evt);
    }

    @Override
    protected void processKeyEvent(KeyEvent evt) {
        MenuSelectionManager.defaultManager().processKeyEvent(evt);
        if (evt.isConsumed()) {
            return;
        }
        super.processKeyEvent(evt);
    }

    public SingleSelectionModel getSelectionModel() {
        return this.selectionModel;
    }

    @BeanProperty(bound=false, expert=true, description="The selection model for the popup menu")
    public void setSelectionModel(SingleSelectionModel model) {
        this.selectionModel = model;
    }

    public JMenuItem add(JMenuItem menuItem) {
        super.add(menuItem);
        return menuItem;
    }

    public JMenuItem add(String s) {
        return this.add(new JMenuItem(s));
    }

    public JMenuItem add(Action a) {
        JMenuItem mi = this.createActionComponent(a);
        mi.setAction(a);
        this.add(mi);
        return mi;
    }

    Point adjustPopupLocationToFitScreen(int xPosition, int yPosition) {
        Point popupLocation = new Point(xPosition, yPosition);
        if (popupPostionFixDisabled || GraphicsEnvironment.isHeadless()) {
            return popupLocation;
        }
        GraphicsConfiguration gc = this.getCurrentGraphicsConfiguration(popupLocation);
        if (gc == null) {
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        }
        Rectangle scrBounds = gc.getBounds();
        Dimension popupSize = this.getPreferredSize();
        long popupRightX = (long)popupLocation.x + (long)popupSize.width;
        long popupBottomY = (long)popupLocation.y + (long)popupSize.height;
        int scrWidth = scrBounds.width;
        int scrHeight = scrBounds.height;
        if (!JPopupMenu.canPopupOverlapTaskBar()) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Insets scrInsets = toolkit.getScreenInsets(gc);
            scrBounds.x += scrInsets.left;
            scrBounds.y += scrInsets.top;
            scrWidth -= scrInsets.left + scrInsets.right;
            scrHeight -= scrInsets.top + scrInsets.bottom;
        }
        int scrRightX = scrBounds.x + scrWidth;
        int scrBottomY = scrBounds.y + scrHeight;
        if (popupRightX > (long)scrRightX) {
            popupLocation.x = scrRightX - popupSize.width;
        }
        if (popupBottomY > (long)scrBottomY) {
            popupLocation.y = scrBottomY - popupSize.height;
        }
        if (popupLocation.x < scrBounds.x) {
            popupLocation.x = scrBounds.x;
        }
        if (popupLocation.y < scrBounds.y) {
            popupLocation.y = scrBounds.y;
        }
        return popupLocation;
    }

    private GraphicsConfiguration getCurrentGraphicsConfiguration(Point popupLocation) {
        GraphicsConfiguration gc = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        for (int i = 0; i < gd.length; ++i) {
            GraphicsConfiguration dgc;
            if (gd[i].getType() != 0 || !(dgc = gd[i].getDefaultConfiguration()).getBounds().contains(popupLocation)) continue;
            gc = dgc;
            break;
        }
        if (gc == null && this.getInvoker() != null) {
            gc = this.getInvoker().getGraphicsConfiguration();
        }
        return gc;
    }

    static boolean canPopupOverlapTaskBar() {
        boolean result = true;
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk instanceof SunToolkit) {
            result = ((SunToolkit)tk).canPopupOverlapTaskBar();
        }
        return result;
    }

    protected JMenuItem createActionComponent(Action a) {
        JMenuItem mi = new JMenuItem(){

            @Override
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                PropertyChangeListener pcl = JPopupMenu.this.createActionChangeListener(this);
                if (pcl == null) {
                    pcl = super.createActionPropertyChangeListener(a);
                }
                return pcl;
            }
        };
        mi.setHorizontalTextPosition(11);
        mi.setVerticalTextPosition(0);
        return mi;
    }

    protected PropertyChangeListener createActionChangeListener(JMenuItem b) {
        return b.createActionPropertyChangeListener0(b.getAction());
    }

    @Override
    public void remove(int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        if (pos > this.getComponentCount() - 1) {
            throw new IllegalArgumentException("index greater than the number of items.");
        }
        super.remove(pos);
    }

    @BeanProperty(bound=false, expert=true, description="Determines whether lightweight popups are used when possible")
    public void setLightWeightPopupEnabled(boolean aFlag) {
        this.lightWeightPopup = aFlag;
    }

    public boolean isLightWeightPopupEnabled() {
        return this.lightWeightPopup;
    }

    public String getLabel() {
        return this.label;
    }

    @BeanProperty(description="The label for the popup menu.")
    public void setLabel(String label) {
        String oldValue = this.label;
        this.label = label;
        this.firePropertyChange("label", oldValue, label);
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldValue, label);
        }
        this.invalidate();
        this.repaint();
    }

    public void addSeparator() {
        this.add(new Separator());
    }

    public void insert(Action a, int index) {
        JMenuItem mi = this.createActionComponent(a);
        mi.setAction(a);
        this.insert(mi, index);
    }

    public void insert(Component component, int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        int nitems = this.getComponentCount();
        ArrayList<Component> tempItems = new ArrayList<Component>();
        for (int i = index; i < nitems; ++i) {
            tempItems.add(this.getComponent(index));
            this.remove(index);
        }
        this.add(component);
        for (Component tempItem : tempItems) {
            this.add(tempItem);
        }
    }

    public void addPopupMenuListener(PopupMenuListener l) {
        this.listenerList.add(PopupMenuListener.class, l);
    }

    public void removePopupMenuListener(PopupMenuListener l) {
        this.listenerList.remove(PopupMenuListener.class, l);
    }

    @BeanProperty(bound=false)
    public PopupMenuListener[] getPopupMenuListeners() {
        return (PopupMenuListener[])this.listenerList.getListeners(PopupMenuListener.class);
    }

    public void addMenuKeyListener(MenuKeyListener l) {
        this.listenerList.add(MenuKeyListener.class, l);
    }

    public void removeMenuKeyListener(MenuKeyListener l) {
        this.listenerList.remove(MenuKeyListener.class, l);
    }

    @BeanProperty(bound=false)
    public MenuKeyListener[] getMenuKeyListeners() {
        return (MenuKeyListener[])this.listenerList.getListeners(MenuKeyListener.class);
    }

    protected void firePopupMenuWillBecomeVisible() {
        Object[] listeners = this.listenerList.getListenerList();
        PopupMenuEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != PopupMenuListener.class) continue;
            if (e == null) {
                e = new PopupMenuEvent(this);
            }
            ((PopupMenuListener)listeners[i + 1]).popupMenuWillBecomeVisible(e);
        }
    }

    protected void firePopupMenuWillBecomeInvisible() {
        Object[] listeners = this.listenerList.getListenerList();
        PopupMenuEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != PopupMenuListener.class) continue;
            if (e == null) {
                e = new PopupMenuEvent(this);
            }
            ((PopupMenuListener)listeners[i + 1]).popupMenuWillBecomeInvisible(e);
        }
    }

    protected void firePopupMenuCanceled() {
        Object[] listeners = this.listenerList.getListenerList();
        PopupMenuEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != PopupMenuListener.class) continue;
            if (e == null) {
                e = new PopupMenuEvent(this);
            }
            ((PopupMenuListener)listeners[i + 1]).popupMenuCanceled(e);
        }
    }

    @Override
    boolean alwaysOnTop() {
        return true;
    }

    public void pack() {
        if (this.popup != null) {
            Dimension pref = this.getPreferredSize();
            if (pref == null || pref.width != this.getWidth() || pref.height != this.getHeight()) {
                this.showPopup();
            } else {
                this.validate();
            }
        }
    }

    private Window getMenuInvoker() {
        Component component = this.invoker;
        if (component instanceof Window) {
            Window menuInvoker = (Window)component;
            return menuInvoker;
        }
        return this.invoker == null ? null : SwingUtilities.getWindowAncestor(this.invoker);
    }

    @Override
    @BeanProperty(description="Makes the popup visible")
    public void setVisible(boolean b) {
        if (b == this.isVisible()) {
            return;
        }
        if (!b) {
            Boolean doCanceled = (Boolean)this.getClientProperty("JPopupMenu.firePopupMenuCanceled");
            if (doCanceled != null && doCanceled == Boolean.TRUE) {
                this.putClientProperty("JPopupMenu.firePopupMenuCanceled", Boolean.FALSE);
                this.firePopupMenuCanceled();
            }
            this.getSelectionModel().clearSelection();
        } else if (this.isPopupMenu()) {
            MenuElement[] me = new MenuElement[]{this};
            MenuSelectionManager.defaultManager().setSelectedPath(me);
        }
        if (b) {
            this.firePopupMenuWillBecomeVisible();
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            if (toolkit instanceof SunToolkit) {
                sunToolkit = (SunToolkit)toolkit;
                sunToolkit.dismissPopupOnFocusLostIfNeeded(this.getMenuInvoker());
            }
            this.showPopup();
            this.firePropertyChange("visible", Boolean.FALSE, Boolean.TRUE);
        } else if (this.popup != null) {
            this.firePopupMenuWillBecomeInvisible();
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            if (toolkit instanceof SunToolkit) {
                sunToolkit = (SunToolkit)toolkit;
                sunToolkit.dismissPopupOnFocusLostIfNeededCleanUp(this.getMenuInvoker());
            }
            this.popup.hide();
            this.popup = null;
            this.firePropertyChange("visible", Boolean.TRUE, Boolean.FALSE);
            if (this.isPopupMenu()) {
                MenuSelectionManager.defaultManager().clearSelectedPath();
            }
        }
    }

    private void showPopup() {
        Popup oldPopup = this.popup;
        if (oldPopup != null) {
            oldPopup.hide();
        }
        PopupFactory popupFactory = PopupFactory.getSharedInstance();
        if (this.isLightWeightPopupEnabled()) {
            popupFactory.setPopupType(0);
        } else {
            popupFactory.setPopupType(2);
        }
        Point p = this.adjustPopupLocationToFitScreen(this.desiredLocationX, this.desiredLocationY);
        this.desiredLocationX = p.x;
        this.desiredLocationY = p.y;
        Popup newPopup = this.getUI().getPopup(this, this.desiredLocationX, this.desiredLocationY);
        popupFactory.setPopupType(0);
        this.popup = newPopup;
        newPopup.show();
    }

    @Override
    public boolean isVisible() {
        return this.popup != null;
    }

    @Override
    @BeanProperty(description="The location of the popup menu.")
    public void setLocation(int x, int y) {
        int oldX = this.desiredLocationX;
        int oldY = this.desiredLocationY;
        this.desiredLocationX = x;
        this.desiredLocationY = y;
        if (this.popup != null && (x != oldX || y != oldY)) {
            this.showPopup();
        }
    }

    private boolean isPopupMenu() {
        return this.invoker != null && !(this.invoker instanceof JMenu);
    }

    public Component getInvoker() {
        return this.invoker;
    }

    @BeanProperty(bound=false, expert=true, description="The invoking component for the popup menu")
    public void setInvoker(Component invoker) {
        Component oldInvoker = this.invoker;
        this.invoker = invoker;
        if (oldInvoker != this.invoker && this.ui != null) {
            this.ui.uninstallUI(this);
            this.ui.installUI(this);
        }
        this.invalidate();
    }

    public void show(Component invoker, int x, int y) {
        this.setInvoker(invoker);
        Frame newFrame = JPopupMenu.getFrame(invoker);
        if (newFrame != this.frame && newFrame != null) {
            this.frame = newFrame;
            if (this.popup != null) {
                this.setVisible(false);
            }
        }
        if (invoker != null) {
            Point invokerOrigin = invoker.getLocationOnScreen();
            long lx = (long)invokerOrigin.x + (long)x;
            long ly = (long)invokerOrigin.y + (long)y;
            if (lx > Integer.MAX_VALUE) {
                lx = Integer.MAX_VALUE;
            }
            if (lx < Integer.MIN_VALUE) {
                lx = Integer.MIN_VALUE;
            }
            if (ly > Integer.MAX_VALUE) {
                ly = Integer.MAX_VALUE;
            }
            if (ly < Integer.MIN_VALUE) {
                ly = Integer.MIN_VALUE;
            }
            this.setLocation((int)lx, (int)ly);
        } else {
            this.setLocation(x, y);
        }
        this.setVisible(true);
    }

    JPopupMenu getRootPopupMenu() {
        Container container;
        JPopupMenu mp = this;
        while (mp != null && !mp.isPopupMenu() && mp.getInvoker() != null && (container = mp.getInvoker().getParent()) instanceof JPopupMenu) {
            JPopupMenu popupMenu;
            mp = popupMenu = (JPopupMenu)container;
        }
        return mp;
    }

    @Deprecated
    public Component getComponentAtIndex(int i) {
        return this.getComponent(i);
    }

    public int getComponentIndex(Component c) {
        int ncomponents = this.getComponentCount();
        Component[] component = this.getComponents();
        for (int i = 0; i < ncomponents; ++i) {
            Component comp = component[i];
            if (comp != c) continue;
            return i;
        }
        return -1;
    }

    @BeanProperty(description="The size of the popup menu")
    public void setPopupSize(Dimension d) {
        Dimension newSize;
        Dimension oldSize = this.getPreferredSize();
        this.setPreferredSize(d);
        if (this.popup != null && !oldSize.equals(newSize = this.getPreferredSize())) {
            this.showPopup();
        }
    }

    @BeanProperty(description="The size of the popup menu")
    public void setPopupSize(int width, int height) {
        this.setPopupSize(new Dimension(width, height));
    }

    @BeanProperty(expert=true, hidden=true, description="The selected component on the popup menu")
    public void setSelected(Component sel) {
        SingleSelectionModel model = this.getSelectionModel();
        int index = this.getComponentIndex(sel);
        model.setSelectedIndex(index);
    }

    public boolean isBorderPainted() {
        return this.paintBorder;
    }

    @BeanProperty(bound=false, description="Is the border of the popup menu painted")
    public void setBorderPainted(boolean b) {
        this.paintBorder = b;
        this.repaint();
    }

    @Override
    protected void paintBorder(Graphics g) {
        if (this.isBorderPainted()) {
            super.paintBorder(g);
        }
    }

    @BeanProperty(bound=false)
    public Insets getMargin() {
        if (this.margin == null) {
            return new Insets(0, 0, 0, 0);
        }
        return this.margin;
    }

    boolean isSubPopupMenu(JPopupMenu popup) {
        int ncomponents = this.getComponentCount();
        Component[] component = this.getComponents();
        for (int i = 0; i < ncomponents; ++i) {
            Component comp = component[i];
            if (!(comp instanceof JMenu)) continue;
            JMenu menu = (JMenu)comp;
            JPopupMenu subPopup = menu.getPopupMenu();
            if (subPopup == popup) {
                return true;
            }
            if (!subPopup.isSubPopupMenu(popup)) continue;
            return true;
        }
        return false;
    }

    private static Frame getFrame(Component c) {
        Component w;
        for (w = c; !(w instanceof Frame) && w != null; w = w.getParent()) {
        }
        return (Frame)w;
    }

    @Override
    protected String paramString() {
        String labelString = this.label != null ? this.label : "";
        String paintBorderString = this.paintBorder ? "true" : "false";
        String marginString = this.margin != null ? this.margin.toString() : "";
        String lightWeightPopupEnabledString = this.isLightWeightPopupEnabled() ? "true" : "false";
        return super.paramString() + ",desiredLocationX=" + this.desiredLocationX + ",desiredLocationY=" + this.desiredLocationY + ",label=" + labelString + ",lightWeightPopupEnabled=" + lightWeightPopupEnabledString + ",margin=" + marginString + ",paintBorder=" + paintBorderString;
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJPopupMenu();
        }
        return this.accessibleContext;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        Vector<Object> values = new Vector<Object>();
        s.defaultWriteObject();
        if (this.invoker != null) {
            values.addElement("invoker");
            values.addElement(this.invoker);
        }
        if (this.popup instanceof Serializable) {
            values.addElement("popup");
            values.addElement(this.popup);
        }
        s.writeObject(values);
        if (this.getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            count = (byte)(count - 1);
            JComponent.setWriteObjCounter(this, count);
            if (count == 0 && this.ui != null) {
                this.ui.installUI(this);
            }
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField f = s.readFields();
        int newDesiredLocationX = f.get("desiredLocationX", 0);
        int newDesiredLocationY = f.get("desiredLocationY", 0);
        Point p = this.adjustPopupLocationToFitScreen(newDesiredLocationX, newDesiredLocationY);
        this.desiredLocationX = p.x;
        this.desiredLocationY = p.y;
        this.label = (String)f.get("label", null);
        this.paintBorder = f.get("paintBorder", false);
        this.margin = (Insets)f.get("margin", null);
        this.lightWeightPopup = f.get("lightWeightPopup", false);
        this.selectionModel = (SingleSelectionModel)f.get("selectionModel", null);
        int indexCounter = 0;
        Vector values = (Vector)s.readObject();
        int maxCounter = values.size();
        if (indexCounter < maxCounter && values.elementAt(indexCounter).equals("invoker")) {
            this.invoker = (Component)values.elementAt(++indexCounter);
            ++indexCounter;
        }
        if (indexCounter < maxCounter && values.elementAt(indexCounter).equals("popup")) {
            this.popup = (Popup)values.elementAt(++indexCounter);
            ++indexCounter;
        }
    }

    @Override
    public void processMouseEvent(MouseEvent event, MenuElement[] path, MenuSelectionManager manager) {
    }

    @Override
    public void processKeyEvent(KeyEvent e, MenuElement[] path, MenuSelectionManager manager) {
        MenuKeyEvent mke = new MenuKeyEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar(), path, manager);
        this.processMenuKeyEvent(mke);
        if (mke.isConsumed()) {
            e.consume();
        }
    }

    private void processMenuKeyEvent(MenuKeyEvent e) {
        switch (e.getID()) {
            case 401: {
                this.fireMenuKeyPressed(e);
                break;
            }
            case 402: {
                this.fireMenuKeyReleased(e);
                break;
            }
            case 400: {
                this.fireMenuKeyTyped(e);
                break;
            }
        }
    }

    private void fireMenuKeyPressed(MenuKeyEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != MenuKeyListener.class) continue;
            ((MenuKeyListener)listeners[i + 1]).menuKeyPressed(event);
        }
    }

    private void fireMenuKeyReleased(MenuKeyEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != MenuKeyListener.class) continue;
            ((MenuKeyListener)listeners[i + 1]).menuKeyReleased(event);
        }
    }

    private void fireMenuKeyTyped(MenuKeyEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != MenuKeyListener.class) continue;
            ((MenuKeyListener)listeners[i + 1]).menuKeyTyped(event);
        }
    }

    @Override
    public void menuSelectionChanged(boolean isIncluded) {
        if (this.invoker instanceof JMenu) {
            JMenu m = (JMenu)this.invoker;
            if (isIncluded) {
                m.setPopupMenuVisible(true);
            } else {
                m.setPopupMenuVisible(false);
            }
        }
        if (this.isPopupMenu() && !isIncluded) {
            this.setVisible(false);
        }
    }

    @Override
    @BeanProperty(bound=false)
    public MenuElement[] getSubElements() {
        int i;
        ArrayList<MenuElement> tmp = new ArrayList<MenuElement>();
        int c = this.getComponentCount();
        for (i = 0; i < c; ++i) {
            Component m = this.getComponent(i);
            if (!(m instanceof MenuElement)) continue;
            tmp.add((MenuElement)((Object)m));
        }
        MenuElement[] result = new MenuElement[tmp.size()];
        c = tmp.size();
        for (i = 0; i < c; ++i) {
            result[i] = (MenuElement)tmp.get(i);
        }
        return result;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    public boolean isPopupTrigger(MouseEvent e) {
        return this.getUI().isPopupTrigger(e);
    }

    public static class Separator
    extends JSeparator {
        public Separator() {
            super(0);
        }

        @Override
        public String getUIClassID() {
            return "PopupMenuSeparatorUI";
        }
    }

    protected class AccessibleJPopupMenu
    extends JComponent.AccessibleJComponent
    implements PropertyChangeListener {
        protected AccessibleJPopupMenu() {
            JPopupMenu.this.addPropertyChangeListener(this);
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.POPUP_MENU;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (propertyName == "visible") {
                if (e.getOldValue() == Boolean.FALSE && e.getNewValue() == Boolean.TRUE) {
                    this.handlePopupIsVisibleEvent(true);
                } else if (e.getOldValue() == Boolean.TRUE && e.getNewValue() == Boolean.FALSE) {
                    this.handlePopupIsVisibleEvent(false);
                }
            }
        }

        private void handlePopupIsVisibleEvent(boolean visible) {
            if (visible) {
                this.firePropertyChange("AccessibleState", null, AccessibleState.VISIBLE);
                this.fireActiveDescendant();
            } else {
                this.firePropertyChange("AccessibleState", AccessibleState.VISIBLE, null);
            }
        }

        private void fireActiveDescendant() {
            if (JPopupMenu.this instanceof BasicComboPopup) {
                AccessibleContext invokerContext;
                JList<Object> popupList = ((BasicComboPopup)JPopupMenu.this).getList();
                if (popupList == null) {
                    return;
                }
                AccessibleContext ac = popupList.getAccessibleContext();
                AccessibleSelection selection = ac.getAccessibleSelection();
                if (selection == null) {
                    return;
                }
                Accessible a = selection.getAccessibleSelection(0);
                if (a == null) {
                    return;
                }
                AccessibleContext selectedItem = a.getAccessibleContext();
                if (selectedItem != null && JPopupMenu.this.invoker != null && (invokerContext = JPopupMenu.this.invoker.getAccessibleContext()) != null) {
                    invokerContext.firePropertyChange("AccessibleActiveDescendant", null, selectedItem);
                }
            }
        }
    }
}

