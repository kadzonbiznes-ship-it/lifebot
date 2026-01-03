/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.plaf.MenuItemUI;

@JavaBean(defaultProperty="UIClassID", description="An item which can be selected in a menu.")
@SwingContainer(value=false)
public class JMenuItem
extends AbstractButton
implements Accessible,
MenuElement {
    private static final String uiClassID = "MenuItemUI";
    private static final boolean TRACE = false;
    private static final boolean VERBOSE = false;
    private static final boolean DEBUG = false;
    private boolean isMouseDragged = false;
    private KeyStroke accelerator;

    public JMenuItem() {
        this(null, null);
    }

    public JMenuItem(Icon icon) {
        this(null, icon);
    }

    public JMenuItem(String text) {
        this(text, null);
    }

    public JMenuItem(Action a) {
        this();
        this.setAction(a);
    }

    public JMenuItem(String text, Icon icon) {
        this.setModel(new DefaultButtonModel());
        this.init(text, icon);
        this.initFocusability();
    }

    public JMenuItem(String text, int mnemonic) {
        this.setModel(new DefaultButtonModel());
        this.init(text, null);
        this.setMnemonic(mnemonic);
        this.initFocusability();
    }

    @Override
    public void setModel(ButtonModel newModel) {
        super.setModel(newModel);
        if (newModel instanceof DefaultButtonModel) {
            ((DefaultButtonModel)newModel).setMenuItem(true);
        }
    }

    void initFocusability() {
        this.setFocusable(false);
    }

    @Override
    protected void init(String text, Icon icon) {
        if (text != null) {
            this.setText(text);
        }
        if (icon != null) {
            this.setIcon(icon);
        }
        this.addFocusListener(new MenuItemFocusListener());
        this.setUIProperty("borderPainted", Boolean.FALSE);
        this.setFocusPainted(false);
        this.setHorizontalTextPosition(11);
        this.setHorizontalAlignment(10);
        this.updateUI();
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the LookAndFeel.")
    public void setUI(MenuItemUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        this.setUI((MenuItemUI)UIManager.getUI(this));
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    @BeanProperty(bound=false, hidden=true, description="Mouse release will fire an action event")
    public void setArmed(boolean b) {
        ButtonModel model = this.getModel();
        boolean oldValue = model.isArmed();
        if (model.isArmed() != b) {
            model.setArmed(b);
        }
    }

    public boolean isArmed() {
        ButtonModel model = this.getModel();
        return model.isArmed();
    }

    @Override
    @BeanProperty(preferred=true, description="The enabled state of the component.")
    public void setEnabled(boolean b) {
        if (!b && !UIManager.getBoolean("MenuItem.disabledAreNavigable")) {
            this.setArmed(false);
        }
        super.setEnabled(b);
    }

    @Override
    boolean alwaysOnTop() {
        return SwingUtilities.getAncestorOfClass(JInternalFrame.class, this) == null;
    }

    @BeanProperty(preferred=true, description="The keystroke combination which will invoke the JMenuItem's actionlisteners without navigating the menu hierarchy")
    public void setAccelerator(KeyStroke keyStroke) {
        KeyStroke oldAccelerator = this.accelerator;
        this.accelerator = keyStroke;
        this.repaint();
        this.revalidate();
        this.firePropertyChange("accelerator", oldAccelerator, this.accelerator);
    }

    public KeyStroke getAccelerator() {
        return this.accelerator;
    }

    @Override
    protected void configurePropertiesFromAction(Action a) {
        super.configurePropertiesFromAction(a);
        this.configureAcceleratorFromAction(a);
    }

    @Override
    void setIconFromAction(Action a) {
        Icon icon = null;
        if (a != null) {
            icon = (Icon)a.getValue("SmallIcon");
        }
        this.setIcon(icon);
    }

    @Override
    void largeIconChanged(Action a) {
    }

    @Override
    void smallIconChanged(Action a) {
        this.setIconFromAction(a);
    }

    void configureAcceleratorFromAction(Action a) {
        KeyStroke ks = a == null ? null : (KeyStroke)a.getValue("AcceleratorKey");
        this.setAccelerator(ks);
    }

    @Override
    protected void actionPropertyChanged(Action action, String propertyName) {
        if (propertyName == "AcceleratorKey") {
            this.configureAcceleratorFromAction(action);
        } else {
            super.actionPropertyChanged(action, propertyName);
        }
    }

    @Override
    public void processMouseEvent(MouseEvent e, MenuElement[] path, MenuSelectionManager manager) {
        this.processMenuDragMouseEvent(new MenuDragMouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), path, manager));
    }

    @Override
    public void processKeyEvent(KeyEvent e, MenuElement[] path, MenuSelectionManager manager) {
        MenuKeyEvent mke = new MenuKeyEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar(), path, manager);
        this.processMenuKeyEvent(mke);
        if (mke.isConsumed()) {
            e.consume();
        }
    }

    public void processMenuDragMouseEvent(MenuDragMouseEvent e) {
        switch (e.getID()) {
            case 504: {
                this.isMouseDragged = false;
                this.fireMenuDragMouseEntered(e);
                break;
            }
            case 505: {
                this.isMouseDragged = false;
                this.fireMenuDragMouseExited(e);
                break;
            }
            case 506: {
                this.isMouseDragged = true;
                this.fireMenuDragMouseDragged(e);
                break;
            }
            case 502: {
                if (!this.isMouseDragged) break;
                this.fireMenuDragMouseReleased(e);
                break;
            }
        }
    }

    public void processMenuKeyEvent(MenuKeyEvent e) {
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

    protected void fireMenuDragMouseEntered(MenuDragMouseEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != MenuDragMouseListener.class) continue;
            ((MenuDragMouseListener)listeners[i + 1]).menuDragMouseEntered(event);
        }
    }

    protected void fireMenuDragMouseExited(MenuDragMouseEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != MenuDragMouseListener.class) continue;
            ((MenuDragMouseListener)listeners[i + 1]).menuDragMouseExited(event);
        }
    }

    protected void fireMenuDragMouseDragged(MenuDragMouseEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != MenuDragMouseListener.class) continue;
            ((MenuDragMouseListener)listeners[i + 1]).menuDragMouseDragged(event);
        }
    }

    protected void fireMenuDragMouseReleased(MenuDragMouseEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != MenuDragMouseListener.class) continue;
            ((MenuDragMouseListener)listeners[i + 1]).menuDragMouseReleased(event);
        }
    }

    protected void fireMenuKeyPressed(MenuKeyEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != MenuKeyListener.class) continue;
            ((MenuKeyListener)listeners[i + 1]).menuKeyPressed(event);
        }
    }

    protected void fireMenuKeyReleased(MenuKeyEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != MenuKeyListener.class) continue;
            ((MenuKeyListener)listeners[i + 1]).menuKeyReleased(event);
        }
    }

    protected void fireMenuKeyTyped(MenuKeyEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != MenuKeyListener.class) continue;
            ((MenuKeyListener)listeners[i + 1]).menuKeyTyped(event);
        }
    }

    @Override
    public void menuSelectionChanged(boolean isIncluded) {
        this.setArmed(isIncluded);
    }

    @Override
    @BeanProperty(bound=false)
    public MenuElement[] getSubElements() {
        return new MenuElement[0];
    }

    @Override
    public Component getComponent() {
        return this;
    }

    public void addMenuDragMouseListener(MenuDragMouseListener l) {
        this.listenerList.add(MenuDragMouseListener.class, l);
    }

    public void removeMenuDragMouseListener(MenuDragMouseListener l) {
        this.listenerList.remove(MenuDragMouseListener.class, l);
    }

    @BeanProperty(bound=false)
    public MenuDragMouseListener[] getMenuDragMouseListeners() {
        return (MenuDragMouseListener[])this.listenerList.getListeners(MenuDragMouseListener.class);
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

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (this.getUIClassID().equals(uiClassID)) {
            this.updateUI();
        }
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
            this.accessibleContext = new AccessibleJMenuItem();
        }
        return this.accessibleContext;
    }

    private static class MenuItemFocusListener
    implements FocusListener,
    Serializable {
        private MenuItemFocusListener() {
        }

        @Override
        public void focusGained(FocusEvent event) {
        }

        @Override
        public void focusLost(FocusEvent event) {
            JMenuItem mi = (JMenuItem)event.getSource();
            if (mi.isFocusPainted()) {
                mi.repaint();
            }
        }
    }

    protected class AccessibleJMenuItem
    extends AbstractButton.AccessibleAbstractButton
    implements ChangeListener {
        private boolean isArmed = false;
        private boolean hasFocus = false;
        private boolean isPressed = false;
        private boolean isSelected = false;

        AccessibleJMenuItem() {
            JMenuItem.this.addChangeListener(this);
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.MENU_ITEM;
        }

        private void fireAccessibilityFocusedEvent(JMenuItem toCheck) {
            MenuElement menuItem;
            MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
            if (path.length > 0 && toCheck == (menuItem = path[path.length - 1])) {
                this.firePropertyChange("AccessibleState", null, AccessibleState.FOCUSED);
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            this.firePropertyChange("AccessibleVisibleData", false, true);
            if (JMenuItem.this.getModel().isArmed()) {
                if (!this.isArmed) {
                    this.isArmed = true;
                    this.firePropertyChange("AccessibleState", null, AccessibleState.ARMED);
                    this.fireAccessibilityFocusedEvent(JMenuItem.this);
                }
            } else if (this.isArmed) {
                this.isArmed = false;
                this.firePropertyChange("AccessibleState", AccessibleState.ARMED, null);
            }
            if (JMenuItem.this.isFocusOwner()) {
                if (!this.hasFocus) {
                    this.hasFocus = true;
                    this.firePropertyChange("AccessibleState", null, AccessibleState.FOCUSED);
                }
            } else if (this.hasFocus) {
                this.hasFocus = false;
                this.firePropertyChange("AccessibleState", AccessibleState.FOCUSED, null);
            }
            if (JMenuItem.this.getModel().isPressed()) {
                if (!this.isPressed) {
                    this.isPressed = true;
                    this.firePropertyChange("AccessibleState", null, AccessibleState.PRESSED);
                }
            } else if (this.isPressed) {
                this.isPressed = false;
                this.firePropertyChange("AccessibleState", AccessibleState.PRESSED, null);
            }
            if (JMenuItem.this.getModel().isSelected()) {
                if (!this.isSelected) {
                    this.isSelected = true;
                    this.firePropertyChange("AccessibleState", null, AccessibleState.CHECKED);
                    this.fireAccessibilityFocusedEvent(JMenuItem.this);
                }
            } else if (this.isSelected) {
                this.isSelected = false;
                this.firePropertyChange("AccessibleState", AccessibleState.CHECKED, null);
            }
        }
    }
}

