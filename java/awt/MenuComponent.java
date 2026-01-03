/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTError;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.MenuContainer;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.peer.MenuComponentPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Locale;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleStateSet;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.ComponentFactory;

public abstract class MenuComponent
implements Serializable {
    volatile transient MenuComponentPeer peer;
    volatile transient MenuContainer parent;
    private volatile transient AppContext appContext;
    private volatile Font font;
    private volatile String name;
    private volatile boolean nameExplicitlySet;
    volatile boolean newEventsOnly;
    private volatile transient AccessControlContext acc = AccessController.getContext();
    static final String actionListenerK = "actionL";
    static final String itemListenerK = "itemL";
    private static final long serialVersionUID = -4536902356223894379L;
    AccessibleContext accessibleContext = null;

    final AccessControlContext getAccessControlContext() {
        if (this.acc == null) {
            throw new SecurityException("MenuComponent is missing AccessControlContext");
        }
        return this.acc;
    }

    public MenuComponent() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.appContext = AppContext.getAppContext();
    }

    String constructComponentName() {
        return null;
    }

    final ComponentFactory getComponentFactory() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (toolkit instanceof ComponentFactory) {
            return (ComponentFactory)((Object)toolkit);
        }
        throw new AWTError("UI components are unsupported by: " + String.valueOf(toolkit));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getName() {
        if (this.name == null && !this.nameExplicitlySet) {
            MenuComponent menuComponent = this;
            synchronized (menuComponent) {
                if (this.name == null && !this.nameExplicitlySet) {
                    this.name = this.constructComponentName();
                }
            }
        }
        return this.name;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setName(String name) {
        MenuComponent menuComponent = this;
        synchronized (menuComponent) {
            this.name = name;
            this.nameExplicitlySet = true;
        }
    }

    public MenuContainer getParent() {
        return this.getParent_NoClientCode();
    }

    final MenuContainer getParent_NoClientCode() {
        return this.parent;
    }

    public Font getFont() {
        Font font = this.font;
        if (font != null) {
            return font;
        }
        MenuContainer parent = this.parent;
        if (parent != null) {
            return parent.getFont();
        }
        return null;
    }

    final Font getFont_NoClientCode() {
        Font font = this.font;
        if (font != null) {
            return font;
        }
        MenuContainer parent = this.parent;
        if (parent != null) {
            if (parent instanceof Component) {
                font = ((Component)parent).getFont_NoClientCode();
            } else if (parent instanceof MenuComponent) {
                font = ((MenuComponent)((Object)parent)).getFont_NoClientCode();
            }
        }
        return font;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setFont(Font f) {
        Object object = this.getTreeLock();
        synchronized (object) {
            this.font = f;
            MenuComponentPeer peer = this.peer;
            if (peer != null) {
                peer.setFont(f);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeNotify() {
        Object object = this.getTreeLock();
        synchronized (object) {
            MenuComponentPeer p = this.peer;
            if (p != null) {
                Toolkit.getEventQueue().removeSourceEvents(this, true);
                this.peer = null;
                p.dispose();
            }
        }
    }

    @Deprecated
    public boolean postEvent(Event evt) {
        MenuContainer parent = this.parent;
        if (parent != null) {
            parent.postEvent(evt);
        }
        return false;
    }

    public final void dispatchEvent(AWTEvent e) {
        this.dispatchEventImpl(e);
    }

    /*
     * Enabled aggressive block sorting
     */
    void dispatchEventImpl(AWTEvent e) {
        Event olde;
        block5: {
            block4: {
                EventQueue.setCurrentEventAndMostRecentTime(e);
                Toolkit.getDefaultToolkit().notifyAWTEventListeners(e);
                if (this.newEventsOnly) break block4;
                MenuContainer menuContainer = this.parent;
                if (!(menuContainer instanceof MenuComponent)) break block5;
                MenuComponent mc = (MenuComponent)((Object)menuContainer);
                if (!mc.newEventsOnly) break block5;
            }
            if (this.eventEnabled(e)) {
                this.processEvent(e);
                return;
            }
            if (!(e instanceof ActionEvent)) return;
            if (this.parent == null) return;
            e.setSource(this.parent);
            ((MenuComponent)((Object)this.parent)).dispatchEvent(e);
            return;
        }
        if ((olde = e.convertToOld()) == null) return;
        this.postEvent(olde);
    }

    boolean eventEnabled(AWTEvent e) {
        return false;
    }

    protected void processEvent(AWTEvent e) {
    }

    protected String paramString() {
        String thisName = this.getName();
        return thisName != null ? thisName : "";
    }

    public String toString() {
        return this.getClass().getName() + "[" + this.paramString() + "]";
    }

    protected final Object getTreeLock() {
        return Component.LOCK;
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException, HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.acc = AccessController.getContext();
        s.defaultReadObject();
        this.appContext = AppContext.getAppContext();
    }

    private static native void initIDs();

    public AccessibleContext getAccessibleContext() {
        return this.accessibleContext;
    }

    int getAccessibleIndexInParent() {
        MenuContainer localParent = this.parent;
        if (!(localParent instanceof MenuComponent)) {
            return -1;
        }
        MenuComponent localParentMenu = (MenuComponent)((Object)localParent);
        return localParentMenu.getAccessibleChildIndex(this);
    }

    int getAccessibleChildIndex(MenuComponent child) {
        return -1;
    }

    AccessibleStateSet getAccessibleStateSet() {
        AccessibleStateSet states = new AccessibleStateSet();
        return states;
    }

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            MenuComponent.initIDs();
        }
        AWTAccessor.setMenuComponentAccessor(new AWTAccessor.MenuComponentAccessor(){

            @Override
            public AppContext getAppContext(MenuComponent menuComp) {
                return menuComp.appContext;
            }

            @Override
            public void setAppContext(MenuComponent menuComp, AppContext appContext) {
                menuComp.appContext = appContext;
            }

            @Override
            public <T extends MenuComponentPeer> T getPeer(MenuComponent menuComp) {
                return (T)menuComp.peer;
            }

            @Override
            public MenuContainer getParent(MenuComponent menuComp) {
                return menuComp.parent;
            }

            @Override
            public void setParent(MenuComponent menuComp, MenuContainer menuContainer) {
                menuComp.parent = menuContainer;
            }

            @Override
            public Font getFont_NoClientCode(MenuComponent menuComp) {
                return menuComp.getFont_NoClientCode();
            }
        });
    }

    protected abstract class AccessibleAWTMenuComponent
    extends AccessibleContext
    implements Serializable,
    AccessibleComponent,
    AccessibleSelection {
        private static final long serialVersionUID = -4269533416223798698L;

        protected AccessibleAWTMenuComponent() {
        }

        @Override
        public AccessibleSelection getAccessibleSelection() {
            return this;
        }

        @Override
        public String getAccessibleName() {
            return this.accessibleName;
        }

        @Override
        public String getAccessibleDescription() {
            return this.accessibleDescription;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.AWT_COMPONENT;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            return MenuComponent.this.getAccessibleStateSet();
        }

        @Override
        public Accessible getAccessibleParent() {
            if (this.accessibleParent != null) {
                return this.accessibleParent;
            }
            MenuContainer parent = MenuComponent.this.getParent();
            if (parent instanceof Accessible) {
                return (Accessible)((Object)parent);
            }
            return null;
        }

        @Override
        public int getAccessibleIndexInParent() {
            return MenuComponent.this.getAccessibleIndexInParent();
        }

        @Override
        public int getAccessibleChildrenCount() {
            return 0;
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            return null;
        }

        @Override
        public Locale getLocale() {
            MenuContainer parent = MenuComponent.this.getParent();
            if (parent instanceof Component) {
                return ((Component)parent).getLocale();
            }
            return Locale.getDefault();
        }

        @Override
        public AccessibleComponent getAccessibleComponent() {
            return this;
        }

        @Override
        public Color getBackground() {
            return null;
        }

        @Override
        public void setBackground(Color c) {
        }

        @Override
        public Color getForeground() {
            return null;
        }

        @Override
        public void setForeground(Color c) {
        }

        @Override
        public Cursor getCursor() {
            return null;
        }

        @Override
        public void setCursor(Cursor cursor) {
        }

        @Override
        public Font getFont() {
            return MenuComponent.this.getFont();
        }

        @Override
        public void setFont(Font f) {
            MenuComponent.this.setFont(f);
        }

        @Override
        public FontMetrics getFontMetrics(Font f) {
            return null;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void setEnabled(boolean b) {
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @Override
        public void setVisible(boolean b) {
        }

        @Override
        public boolean isShowing() {
            return true;
        }

        @Override
        public boolean contains(Point p) {
            return false;
        }

        @Override
        public Point getLocationOnScreen() {
            return null;
        }

        @Override
        public Point getLocation() {
            return null;
        }

        @Override
        public void setLocation(Point p) {
        }

        @Override
        public Rectangle getBounds() {
            return null;
        }

        @Override
        public void setBounds(Rectangle r) {
        }

        @Override
        public Dimension getSize() {
            return null;
        }

        @Override
        public void setSize(Dimension d) {
        }

        @Override
        public Accessible getAccessibleAt(Point p) {
            return null;
        }

        @Override
        public boolean isFocusTraversable() {
            return true;
        }

        @Override
        public void requestFocus() {
        }

        @Override
        public void addFocusListener(FocusListener l) {
        }

        @Override
        public void removeFocusListener(FocusListener l) {
        }

        @Override
        public int getAccessibleSelectionCount() {
            return 0;
        }

        @Override
        public Accessible getAccessibleSelection(int i) {
            return null;
        }

        @Override
        public boolean isAccessibleChildSelected(int i) {
            return false;
        }

        @Override
        public void addAccessibleSelection(int i) {
        }

        @Override
        public void removeAccessibleSelection(int i) {
        }

        @Override
        public void clearAccessibleSelection() {
        }

        @Override
        public void selectAllAccessibleSelection() {
        }
    }
}

