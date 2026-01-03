/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.swing.Action;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.plaf.MenuItemUI;
import javax.swing.plaf.PopupMenuUI;

@JavaBean(description="A popup window containing menu items displayed in a menu bar.")
@SwingContainer
public class JMenu
extends JMenuItem
implements Accessible,
MenuElement {
    private static final String uiClassID = "MenuUI";
    private JPopupMenu popupMenu;
    private ChangeListener menuChangeListener = null;
    private MenuEvent menuEvent = null;
    private int delay;
    private Point customMenuLocation = null;
    private static final boolean TRACE = false;
    private static final boolean VERBOSE = false;
    private static final boolean DEBUG = false;
    protected WinListener popupListener;

    public JMenu() {
        this("");
    }

    public JMenu(String s) {
        super(s);
    }

    public JMenu(Action a) {
        this();
        this.setAction(a);
    }

    public JMenu(String s, boolean b) {
        this(s);
    }

    @Override
    void initFocusability() {
    }

    @Override
    public void updateUI() {
        this.setUI((MenuItemUI)UIManager.getUI(this));
        if (this.popupMenu != null) {
            this.popupMenu.setUI((PopupMenuUI)UIManager.getUI(this.popupMenu));
        }
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    @Override
    public void setModel(ButtonModel newModel) {
        ButtonModel oldModel = this.getModel();
        super.setModel(newModel);
        if (oldModel != null && this.menuChangeListener != null) {
            oldModel.removeChangeListener(this.menuChangeListener);
            this.menuChangeListener = null;
        }
        this.model = newModel;
        if (newModel != null) {
            this.menuChangeListener = this.createMenuChangeListener();
            newModel.addChangeListener(this.menuChangeListener);
        }
    }

    @Override
    public boolean isSelected() {
        return this.getModel().isSelected();
    }

    @Override
    @BeanProperty(expert=true, hidden=true, description="When the menu is selected, its popup child is shown.")
    public void setSelected(boolean b) {
        ButtonModel model = this.getModel();
        boolean oldValue = model.isSelected();
        if (b != model.isSelected()) {
            this.getModel().setSelected(b);
        }
    }

    public boolean isPopupMenuVisible() {
        this.ensurePopupMenuCreated();
        return this.popupMenu.isVisible();
    }

    @BeanProperty(bound=false, expert=true, hidden=true, description="The popup menu's visibility")
    public void setPopupMenuVisible(boolean b) {
        boolean isVisible = this.isPopupMenuVisible();
        if (b != isVisible && (this.isEnabled() || !b)) {
            this.ensurePopupMenuCreated();
            if (b && this.isShowing()) {
                Point p = this.getCustomMenuLocation();
                if (p == null) {
                    p = this.getPopupMenuOrigin();
                }
                this.getPopupMenu().show(this, p.x, p.y);
            } else {
                this.getPopupMenu().setVisible(false);
            }
        }
    }

    protected Point getPopupMenuOrigin() {
        int y;
        int x;
        Container parent;
        JPopupMenu pm = this.getPopupMenu();
        Dimension s = this.getSize();
        Dimension pmSize = pm.getSize();
        if (pmSize.width == 0) {
            pmSize = pm.getPreferredSize();
        }
        Point position = this.getLocationOnScreen();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        GraphicsConfiguration gc = this.getGraphicsConfiguration();
        Rectangle screenBounds = new Rectangle(toolkit.getScreenSize());
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        for (int i = 0; i < gd.length; ++i) {
            GraphicsConfiguration dgc;
            if (gd[i].getType() != 0 || !(dgc = gd[i].getDefaultConfiguration()).getBounds().contains(position)) continue;
            gc = dgc;
            break;
        }
        if (gc != null) {
            screenBounds = gc.getBounds();
            Insets screenInsets = toolkit.getScreenInsets(gc);
            screenBounds.width -= Math.abs(screenInsets.left + screenInsets.right);
            screenBounds.height -= Math.abs(screenInsets.top + screenInsets.bottom);
            position.x -= Math.abs(screenInsets.left);
            position.y -= Math.abs(screenInsets.top);
        }
        if ((parent = this.getParent()) instanceof JPopupMenu) {
            int xOffset = UIManager.getInt("Menu.submenuPopupOffsetX");
            int yOffset = UIManager.getInt("Menu.submenuPopupOffsetY");
            if (SwingUtilities.isLeftToRight(this)) {
                x = s.width + xOffset;
                if (position.x + x + pmSize.width >= screenBounds.width + screenBounds.x && screenBounds.width - s.width < 2 * (position.x - screenBounds.x)) {
                    x = 0 - xOffset - pmSize.width;
                }
            } else {
                x = 0 - xOffset - pmSize.width;
                if (position.x + x < screenBounds.x && screenBounds.width - s.width > 2 * (position.x - screenBounds.x)) {
                    x = s.width + xOffset;
                }
            }
            if (position.y + (y = yOffset) + pmSize.height >= screenBounds.height + screenBounds.y && screenBounds.height - s.height < 2 * (position.y - screenBounds.y)) {
                y = s.height - yOffset - pmSize.height;
            }
        } else {
            int xOffset = UIManager.getInt("Menu.menuPopupOffsetX");
            int yOffset = UIManager.getInt("Menu.menuPopupOffsetY");
            if (SwingUtilities.isLeftToRight(this)) {
                x = xOffset;
                if (position.x + x + pmSize.width >= screenBounds.width + screenBounds.x && screenBounds.width - s.width < 2 * (position.x - screenBounds.x)) {
                    x = s.width - xOffset - pmSize.width;
                }
            } else {
                x = s.width - xOffset - pmSize.width;
                if (position.x + x < screenBounds.x && screenBounds.width - s.width > 2 * (position.x - screenBounds.x)) {
                    x = xOffset;
                }
            }
            if (position.y + (y = s.height + yOffset) + pmSize.height >= screenBounds.height + screenBounds.y && screenBounds.height - s.height < 2 * (position.y - screenBounds.y)) {
                y = 0 - yOffset - pmSize.height;
            }
        }
        return new Point(x, y);
    }

    public int getDelay() {
        return this.delay;
    }

    @BeanProperty(bound=false, expert=true, description="The delay between menu selection and making the popup menu visible")
    public void setDelay(int d) {
        if (d < 0) {
            throw new IllegalArgumentException("Delay must be a positive integer");
        }
        this.delay = d;
    }

    private void ensurePopupMenuCreated() {
        if (this.popupMenu == null) {
            JMenu thisMenu = this;
            this.popupMenu = new JPopupMenu();
            this.popupMenu.setInvoker(this);
            this.popupListener = this.createWinListener(this.popupMenu);
        }
    }

    private Point getCustomMenuLocation() {
        return this.customMenuLocation;
    }

    public void setMenuLocation(int x, int y) {
        this.customMenuLocation = new Point(x, y);
        if (this.popupMenu != null) {
            this.popupMenu.setLocation(x, y);
        }
    }

    public JMenuItem add(JMenuItem menuItem) {
        this.ensurePopupMenuCreated();
        return this.popupMenu.add(menuItem);
    }

    @Override
    public Component add(Component c) {
        this.ensurePopupMenuCreated();
        this.popupMenu.add(c);
        return c;
    }

    @Override
    public Component add(Component c, int index) {
        this.ensurePopupMenuCreated();
        this.popupMenu.add(c, index);
        return c;
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

    protected JMenuItem createActionComponent(Action a) {
        JMenuItem mi = new JMenuItem(){

            @Override
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                PropertyChangeListener pcl = JMenu.this.createActionChangeListener(this);
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

    public void addSeparator() {
        this.ensurePopupMenuCreated();
        this.popupMenu.addSeparator();
    }

    public void insert(String s, int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        this.ensurePopupMenuCreated();
        this.popupMenu.insert(new JMenuItem(s), pos);
    }

    public JMenuItem insert(JMenuItem mi, int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        this.ensurePopupMenuCreated();
        this.popupMenu.insert(mi, pos);
        return mi;
    }

    public JMenuItem insert(Action a, int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        this.ensurePopupMenuCreated();
        JMenuItem mi = new JMenuItem(a);
        mi.setHorizontalTextPosition(11);
        mi.setVerticalTextPosition(0);
        this.popupMenu.insert(mi, pos);
        return mi;
    }

    public void insertSeparator(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        this.ensurePopupMenuCreated();
        this.popupMenu.insert(new JPopupMenu.Separator(), index);
    }

    public JMenuItem getItem(int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        Component c = this.getMenuComponent(pos);
        if (c instanceof JMenuItem) {
            JMenuItem mi = (JMenuItem)c;
            return mi;
        }
        return null;
    }

    @BeanProperty(bound=false)
    public int getItemCount() {
        return this.getMenuComponentCount();
    }

    @BeanProperty(bound=false)
    public boolean isTearOff() {
        throw new Error("boolean isTearOff() {} not yet implemented");
    }

    public void remove(JMenuItem item) {
        if (this.popupMenu != null) {
            this.popupMenu.remove(item);
        }
    }

    @Override
    public void remove(int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        if (pos > this.getItemCount()) {
            throw new IllegalArgumentException("index greater than the number of items.");
        }
        if (this.popupMenu != null) {
            this.popupMenu.remove(pos);
        }
    }

    @Override
    public void remove(Component c) {
        if (this.popupMenu != null) {
            this.popupMenu.remove(c);
        }
    }

    @Override
    public void removeAll() {
        if (this.popupMenu != null) {
            this.popupMenu.removeAll();
        }
    }

    @BeanProperty(bound=false)
    public int getMenuComponentCount() {
        int componentCount = 0;
        if (this.popupMenu != null) {
            componentCount = this.popupMenu.getComponentCount();
        }
        return componentCount;
    }

    public Component getMenuComponent(int n) {
        if (this.popupMenu != null) {
            return this.popupMenu.getComponent(n);
        }
        return null;
    }

    @BeanProperty(bound=false)
    public Component[] getMenuComponents() {
        if (this.popupMenu != null) {
            return this.popupMenu.getComponents();
        }
        return new Component[0];
    }

    @BeanProperty(bound=false)
    public boolean isTopLevelMenu() {
        return this.getParent() instanceof JMenuBar;
    }

    public boolean isMenuComponent(Component c) {
        JPopupMenu comp;
        if (c == this) {
            return true;
        }
        if (c instanceof JPopupMenu && (comp = (JPopupMenu)c) == this.getPopupMenu()) {
            return true;
        }
        int ncomponents = this.getMenuComponentCount();
        Component[] component = this.getMenuComponents();
        for (int i = 0; i < ncomponents; ++i) {
            JMenu subMenu;
            Component comp2 = component[i];
            if (comp2 == c) {
                return true;
            }
            if (!(comp2 instanceof JMenu) || !(subMenu = (JMenu)comp2).isMenuComponent(c)) continue;
            return true;
        }
        return false;
    }

    private Point translateToPopupMenu(Point p) {
        return this.translateToPopupMenu(p.x, p.y);
    }

    private Point translateToPopupMenu(int x, int y) {
        int newY;
        int newX;
        if (this.getParent() instanceof JPopupMenu) {
            newX = x - this.getSize().width;
            newY = y;
        } else {
            newX = x;
            newY = y - this.getSize().height;
        }
        return new Point(newX, newY);
    }

    @BeanProperty(bound=false)
    public JPopupMenu getPopupMenu() {
        this.ensurePopupMenuCreated();
        return this.popupMenu;
    }

    public void addMenuListener(MenuListener l) {
        this.listenerList.add(MenuListener.class, l);
    }

    public void removeMenuListener(MenuListener l) {
        this.listenerList.remove(MenuListener.class, l);
    }

    @BeanProperty(bound=false)
    public MenuListener[] getMenuListeners() {
        return (MenuListener[])this.listenerList.getListeners(MenuListener.class);
    }

    protected void fireMenuSelected() {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != MenuListener.class) continue;
            if (listeners[i + 1] == null) {
                throw new Error(this.getText() + " has a NULL Listener!! " + i);
            }
            if (this.menuEvent == null) {
                this.menuEvent = new MenuEvent(this);
            }
            ((MenuListener)listeners[i + 1]).menuSelected(this.menuEvent);
        }
    }

    protected void fireMenuDeselected() {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != MenuListener.class) continue;
            if (listeners[i + 1] == null) {
                throw new Error(this.getText() + " has a NULL Listener!! " + i);
            }
            if (this.menuEvent == null) {
                this.menuEvent = new MenuEvent(this);
            }
            ((MenuListener)listeners[i + 1]).menuDeselected(this.menuEvent);
        }
    }

    protected void fireMenuCanceled() {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != MenuListener.class) continue;
            if (listeners[i + 1] == null) {
                throw new Error(this.getText() + " has a NULL Listener!! " + i);
            }
            if (this.menuEvent == null) {
                this.menuEvent = new MenuEvent(this);
            }
            ((MenuListener)listeners[i + 1]).menuCanceled(this.menuEvent);
        }
    }

    @Override
    void configureAcceleratorFromAction(Action a) {
    }

    private ChangeListener createMenuChangeListener() {
        return new MenuChangeListener();
    }

    protected WinListener createWinListener(JPopupMenu p) {
        return new WinListener(p);
    }

    @Override
    public void menuSelectionChanged(boolean isIncluded) {
        this.setSelected(isIncluded);
    }

    @Override
    @BeanProperty(bound=false)
    public MenuElement[] getSubElements() {
        if (this.popupMenu == null) {
            return new MenuElement[0];
        }
        MenuElement[] result = new MenuElement[]{this.popupMenu};
        return result;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void applyComponentOrientation(ComponentOrientation o) {
        super.applyComponentOrientation(o);
        if (this.popupMenu != null) {
            int ncomponents = this.getMenuComponentCount();
            for (int i = 0; i < ncomponents; ++i) {
                this.getMenuComponent(i).applyComponentOrientation(o);
            }
            this.popupMenu.setComponentOrientation(o);
        }
    }

    @Override
    public void setComponentOrientation(ComponentOrientation o) {
        super.setComponentOrientation(o);
        if (this.popupMenu != null) {
            this.popupMenu.setComponentOrientation(o);
        }
    }

    @Override
    public void setAccelerator(KeyStroke keyStroke) {
        throw new Error("setAccelerator() is not defined for JMenu.  Use setMnemonic() instead.");
    }

    @Override
    protected void processKeyEvent(KeyEvent evt) {
        MenuSelectionManager.defaultManager().processKeyEvent(evt);
        if (evt.isConsumed()) {
            return;
        }
        super.processKeyEvent(evt);
    }

    @Override
    public void doClick(int pressTime) {
        MenuElement[] me = this.buildMenuElementArray(this);
        MenuSelectionManager.defaultManager().setSelectedPath(me);
    }

    private MenuElement[] buildMenuElementArray(JMenu leaf) {
        ArrayList<JComponent> elements = new ArrayList<JComponent>();
        Component current = leaf.getPopupMenu();
        while (true) {
            if (current instanceof JPopupMenu) {
                JPopupMenu pop = current;
                elements.add(0, pop);
                current = pop.getInvoker();
                continue;
            }
            if (!(current instanceof JMenu)) break;
            JMenu menu = (JMenu)current;
            elements.add(0, menu);
            current = menu.getParent();
        }
        if (current instanceof JMenuBar) {
            JMenuBar bar = (JMenuBar)current;
            elements.add(0, bar);
        }
        MenuElement[] me = elements.toArray(new MenuElement[0]);
        return me;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (this.getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            count = (byte)(count - 1);
            JComponent.setWriteObjCounter(this, count);
            if (count == 0 && this.ui != null) {
                this.ui.installUI(this);
            }
        }
    }

    @Override
    protected String paramString() {
        return super.paramString();
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJMenu();
        }
        return this.accessibleContext;
    }

    protected class WinListener
    extends WindowAdapter
    implements Serializable {
        JPopupMenu popupMenu;

        public WinListener(JPopupMenu p) {
            this.popupMenu = p;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            JMenu.this.setSelected(false);
        }
    }

    class MenuChangeListener
    implements ChangeListener,
    Serializable {
        boolean isSelected = false;

        MenuChangeListener() {
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            ButtonModel model = (ButtonModel)e.getSource();
            boolean modelSelected = model.isSelected();
            if (modelSelected != this.isSelected) {
                if (modelSelected) {
                    JMenu.this.fireMenuSelected();
                } else {
                    JMenu.this.fireMenuDeselected();
                }
                this.isSelected = modelSelected;
            }
        }
    }

    protected class AccessibleJMenu
    extends JMenuItem.AccessibleJMenuItem
    implements AccessibleSelection {
        protected AccessibleJMenu() {
            super(JMenu.this);
        }

        @Override
        public int getAccessibleChildrenCount() {
            Component[] children = JMenu.this.getMenuComponents();
            int count = 0;
            for (Component child : children) {
                if (!(child instanceof Accessible)) continue;
                ++count;
            }
            return count;
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            Component[] children = JMenu.this.getMenuComponents();
            int count = 0;
            for (Component child : children) {
                if (!(child instanceof Accessible)) continue;
                if (count == i) {
                    if (child instanceof JComponent) {
                        AccessibleContext ac = child.getAccessibleContext();
                        ac.setAccessibleParent(JMenu.this);
                    }
                    return (Accessible)((Object)child);
                }
                ++count;
            }
            return null;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.MENU;
        }

        @Override
        public AccessibleSelection getAccessibleSelection() {
            return this;
        }

        @Override
        public int getAccessibleSelectionCount() {
            MenuElement[] me = MenuSelectionManager.defaultManager().getSelectedPath();
            if (me != null) {
                for (int i = 0; i < me.length; ++i) {
                    if (me[i] != JMenu.this || i + 1 >= me.length) continue;
                    return 1;
                }
            }
            return 0;
        }

        @Override
        public Accessible getAccessibleSelection(int i) {
            if (i < 0 || i >= JMenu.this.getItemCount()) {
                return null;
            }
            MenuElement[] me = MenuSelectionManager.defaultManager().getSelectedPath();
            if (me != null) {
                for (int j = 0; j < me.length; ++j) {
                    if (me[j] != JMenu.this) continue;
                    while (++j < me.length) {
                        if (!(me[j] instanceof JMenuItem)) continue;
                        return (Accessible)((Object)me[j]);
                    }
                }
            }
            return null;
        }

        @Override
        public boolean isAccessibleChildSelected(int i) {
            MenuElement[] me = MenuSelectionManager.defaultManager().getSelectedPath();
            if (me != null) {
                JMenuItem mi = JMenu.this.getItem(i);
                for (int j = 0; j < me.length; ++j) {
                    if (me[j] != mi) continue;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void addAccessibleSelection(int i) {
            if (i < 0 || i >= JMenu.this.getItemCount()) {
                return;
            }
            JMenuItem mi = JMenu.this.getItem(i);
            if (mi != null) {
                if (mi instanceof JMenu) {
                    MenuElement[] me = JMenu.this.buildMenuElementArray((JMenu)mi);
                    MenuSelectionManager.defaultManager().setSelectedPath(me);
                } else {
                    MenuSelectionManager.defaultManager().setSelectedPath(null);
                }
            }
        }

        @Override
        public void removeAccessibleSelection(int i) {
            if (i < 0 || i >= JMenu.this.getItemCount()) {
                return;
            }
            JMenuItem mi = JMenu.this.getItem(i);
            if (mi instanceof JMenu && mi.isSelected()) {
                MenuElement[] old = MenuSelectionManager.defaultManager().getSelectedPath();
                MenuElement[] me = new MenuElement[old.length - 2];
                for (int j = 0; j < old.length - 2; ++j) {
                    me[j] = old[j];
                }
                MenuSelectionManager.defaultManager().setSelectedPath(me);
            }
        }

        @Override
        public void clearAccessibleSelection() {
            MenuElement[] old = MenuSelectionManager.defaultManager().getSelectedPath();
            if (old != null) {
                for (int j = 0; j < old.length; ++j) {
                    if (old[j] != JMenu.this) continue;
                    MenuElement[] me = new MenuElement[j + 1];
                    System.arraycopy(old, 0, me, 0, j);
                    me[j] = JMenu.this.getPopupMenu();
                    MenuSelectionManager.defaultManager().setSelectedPath(me);
                }
            }
        }

        @Override
        public void selectAllAccessibleSelection() {
        }
    }
}

