/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleIcon;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleValue;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.UIResource;
import sun.swing.SwingUtilities2;

@JavaBean(defaultProperty="UI", description="A component which provides a tab folder metaphor for displaying one component from a set of components.")
@SwingContainer
public class JTabbedPane
extends JComponent
implements Serializable,
Accessible,
SwingConstants {
    public static final int WRAP_TAB_LAYOUT = 0;
    public static final int SCROLL_TAB_LAYOUT = 1;
    private static final String uiClassID = "TabbedPaneUI";
    protected int tabPlacement = 1;
    private int tabLayoutPolicy;
    protected SingleSelectionModel model;
    private boolean haveRegistered;
    protected ChangeListener changeListener = null;
    private List<Page> pages;
    private Component visComp = null;
    protected transient ChangeEvent changeEvent = null;

    public JTabbedPane() {
        this(1, 0);
    }

    public JTabbedPane(int tabPlacement) {
        this(tabPlacement, 0);
    }

    public JTabbedPane(int tabPlacement, int tabLayoutPolicy) {
        this.setTabPlacement(tabPlacement);
        this.setTabLayoutPolicy(tabLayoutPolicy);
        this.pages = new ArrayList<Page>(1);
        this.setModel(new DefaultSingleSelectionModel());
        this.updateUI();
    }

    @Override
    public TabbedPaneUI getUI() {
        return (TabbedPaneUI)this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the tabbedpane's LookAndFeel")
    public void setUI(TabbedPaneUI ui) {
        super.setUI(ui);
        for (int i = 0; i < this.getTabCount(); ++i) {
            Icon icon = this.pages.get((int)i).disabledIcon;
            if (!(icon instanceof UIResource)) continue;
            this.setDisabledIconAt(i, null);
        }
    }

    @Override
    public void updateUI() {
        this.setUI((TabbedPaneUI)UIManager.getUI(this));
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    protected ChangeListener createChangeListener() {
        return new ModelListener();
    }

    public void addChangeListener(ChangeListener l) {
        this.listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        this.listenerList.remove(ChangeListener.class, l);
    }

    @BeanProperty(bound=false)
    public ChangeListener[] getChangeListeners() {
        return (ChangeListener[])this.listenerList.getListeners(ChangeListener.class);
    }

    protected void fireStateChanged() {
        int selIndex = this.getSelectedIndex();
        if (selIndex < 0) {
            if (this.visComp != null && this.visComp.isVisible()) {
                this.visComp.setVisible(false);
            }
            this.visComp = null;
        } else {
            Component newComp = this.getComponentAt(selIndex);
            if (newComp != null && newComp != this.visComp) {
                boolean shouldChangeFocus = false;
                if (this.visComp != null) {
                    boolean bl = shouldChangeFocus = SwingUtilities.findFocusOwner(this.visComp) != null;
                    if (this.visComp.isVisible()) {
                        this.visComp.setVisible(false);
                    }
                }
                if (!newComp.isVisible()) {
                    newComp.setVisible(true);
                }
                if (shouldChangeFocus) {
                    SwingUtilities2.tabbedPaneChangeFocusTo(newComp);
                }
                this.visComp = newComp;
            }
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ChangeListener.class) continue;
            if (this.changeEvent == null) {
                this.changeEvent = new ChangeEvent(this);
            }
            ((ChangeListener)listeners[i + 1]).stateChanged(this.changeEvent);
        }
    }

    public SingleSelectionModel getModel() {
        return this.model;
    }

    @BeanProperty(description="The tabbedpane's SingleSelectionModel.")
    public void setModel(SingleSelectionModel model) {
        SingleSelectionModel oldModel = this.getModel();
        if (oldModel != null) {
            oldModel.removeChangeListener(this.changeListener);
            this.changeListener = null;
        }
        this.model = model;
        if (model != null) {
            this.changeListener = this.createChangeListener();
            model.addChangeListener(this.changeListener);
        }
        this.firePropertyChange("model", oldModel, model);
        this.repaint();
    }

    public int getTabPlacement() {
        return this.tabPlacement;
    }

    @BeanProperty(preferred=true, visualUpdate=true, enumerationValues={"JTabbedPane.TOP", "JTabbedPane.LEFT", "JTabbedPane.BOTTOM", "JTabbedPane.RIGHT"}, description="The tabbedpane's tab placement.")
    public void setTabPlacement(int tabPlacement) {
        JTabbedPane.checkTabPlacement(tabPlacement);
        if (this.tabPlacement != tabPlacement) {
            int oldValue = this.tabPlacement;
            this.tabPlacement = tabPlacement;
            this.firePropertyChange("tabPlacement", oldValue, tabPlacement);
            this.revalidate();
            this.repaint();
        }
    }

    private static void checkTabPlacement(int tabPlacement) {
        if (tabPlacement != 1 && tabPlacement != 2 && tabPlacement != 3 && tabPlacement != 4) {
            throw new IllegalArgumentException("illegal tab placement: must be TOP, BOTTOM, LEFT, or RIGHT");
        }
    }

    public int getTabLayoutPolicy() {
        return this.tabLayoutPolicy;
    }

    @BeanProperty(preferred=true, visualUpdate=true, enumerationValues={"JTabbedPane.WRAP_TAB_LAYOUT", "JTabbedPane.SCROLL_TAB_LAYOUT"}, description="The tabbedpane's policy for laying out the tabs")
    public void setTabLayoutPolicy(int tabLayoutPolicy) {
        JTabbedPane.checkTabLayoutPolicy(tabLayoutPolicy);
        if (this.tabLayoutPolicy != tabLayoutPolicy) {
            int oldValue = this.tabLayoutPolicy;
            this.tabLayoutPolicy = tabLayoutPolicy;
            this.firePropertyChange("tabLayoutPolicy", oldValue, tabLayoutPolicy);
            this.revalidate();
            this.repaint();
        }
    }

    private static void checkTabLayoutPolicy(int tabLayoutPolicy) {
        if (tabLayoutPolicy != 0 && tabLayoutPolicy != 1) {
            throw new IllegalArgumentException("illegal tab layout policy: must be WRAP_TAB_LAYOUT or SCROLL_TAB_LAYOUT");
        }
    }

    @Transient
    public int getSelectedIndex() {
        return this.model.getSelectedIndex();
    }

    @BeanProperty(bound=false, preferred=true, description="The tabbedpane's selected tab index.")
    public void setSelectedIndex(int index) {
        if (index != -1) {
            this.checkIndex(index);
        }
        this.setSelectedIndexImpl(index, true);
    }

    private void setSelectedIndexImpl(int index, boolean doAccessibleChanges) {
        int oldIndex = this.model.getSelectedIndex();
        Page oldPage = null;
        Page newPage = null;
        String oldName = null;
        boolean bl = doAccessibleChanges = doAccessibleChanges && oldIndex != index;
        if (doAccessibleChanges) {
            if (this.accessibleContext != null) {
                oldName = this.accessibleContext.getAccessibleName();
            }
            if (oldIndex >= 0) {
                oldPage = this.pages.get(oldIndex);
            }
            if (index >= 0) {
                newPage = this.pages.get(index);
            }
        }
        this.model.setSelectedIndex(index);
        if (doAccessibleChanges) {
            this.changeAccessibleSelection(oldPage, oldName, newPage);
        }
    }

    private void changeAccessibleSelection(Page oldPage, String oldName, Page newPage) {
        if (this.accessibleContext == null) {
            return;
        }
        if (oldPage != null) {
            oldPage.firePropertyChange("AccessibleState", AccessibleState.SELECTED, null);
        }
        if (newPage != null) {
            newPage.firePropertyChange("AccessibleState", null, AccessibleState.SELECTED);
        }
        this.accessibleContext.firePropertyChange("AccessibleName", oldName, this.accessibleContext.getAccessibleName());
    }

    @Transient
    public Component getSelectedComponent() {
        int index = this.getSelectedIndex();
        if (index == -1) {
            return null;
        }
        return this.getComponentAt(index);
    }

    @BeanProperty(bound=false, preferred=true, description="The tabbedpane's selected component.")
    public void setSelectedComponent(Component c) {
        int index = this.indexOfComponent(c);
        if (index == -1) {
            throw new IllegalArgumentException("component not found in tabbed pane");
        }
        this.setSelectedIndex(index);
    }

    public void insertTab(String title, Icon icon, Component component, String tip, int index) {
        int newIndex = index;
        int removeIndex = this.indexOfComponent(component);
        if (component != null && removeIndex != -1) {
            this.removeTabAt(removeIndex);
            if (newIndex > removeIndex) {
                --newIndex;
            }
        }
        int selectedIndex = this.getSelectedIndex();
        this.pages.add(newIndex, new Page(this, title != null ? title : "", icon, null, component, tip));
        if (component != null) {
            this.addImpl(component, null, -1);
            component.setVisible(false);
        } else {
            this.firePropertyChange("indexForNullComponent", -1, index);
        }
        if (this.pages.size() == 1) {
            this.setSelectedIndex(0);
        }
        if (selectedIndex >= newIndex) {
            this.setSelectedIndexImpl(selectedIndex + 1, false);
        }
        if (!this.haveRegistered && tip != null) {
            ToolTipManager.sharedInstance().registerComponent(this);
            this.haveRegistered = true;
        }
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", null, component);
        }
        this.revalidate();
        this.repaint();
    }

    public void addTab(String title, Icon icon, Component component, String tip) {
        this.insertTab(title, icon, component, tip, this.pages.size());
    }

    public void addTab(String title, Icon icon, Component component) {
        this.insertTab(title, icon, component, null, this.pages.size());
    }

    public void addTab(String title, Component component) {
        this.insertTab(title, null, component, null, this.pages.size());
    }

    @Override
    public Component add(Component component) {
        if (!(component instanceof UIResource)) {
            this.addTab(component.getName(), component);
        } else {
            super.add(component);
        }
        return component;
    }

    @Override
    public Component add(String title, Component component) {
        if (!(component instanceof UIResource)) {
            this.addTab(title, component);
        } else {
            super.add(title, component);
        }
        return component;
    }

    @Override
    public Component add(Component component, int index) {
        if (!(component instanceof UIResource)) {
            this.insertTab(component.getName(), null, component, null, index == -1 ? this.getTabCount() : index);
        } else {
            super.add(component, index);
        }
        return component;
    }

    @Override
    public void add(Component component, Object constraints) {
        if (!(component instanceof UIResource)) {
            if (constraints instanceof String) {
                this.addTab((String)constraints, component);
            } else if (constraints instanceof Icon) {
                this.addTab(null, (Icon)constraints, component);
            } else {
                this.add(component);
            }
        } else {
            super.add(component, constraints);
        }
    }

    @Override
    public void add(Component component, Object constraints, int index) {
        if (!(component instanceof UIResource)) {
            Icon icon = constraints instanceof Icon ? (Icon)constraints : null;
            String title = constraints instanceof String ? (String)constraints : null;
            this.insertTab(title, icon, component, null, index == -1 ? this.getTabCount() : index);
        } else {
            super.add(component, constraints, index);
        }
    }

    private void clearAccessibleParent(Component c) {
        AccessibleContext ac = c.getAccessibleContext();
        if (ac != null) {
            ac.setAccessibleParent(null);
        }
    }

    public void removeTabAt(int index) {
        this.checkIndex(index);
        Component component = this.getComponentAt(index);
        boolean shouldChangeFocus = false;
        int selected = this.getSelectedIndex();
        String oldName = null;
        if (component == this.visComp) {
            shouldChangeFocus = SwingUtilities.findFocusOwner(this.visComp) != null;
            this.visComp = null;
        }
        if (this.accessibleContext != null) {
            if (index == selected) {
                this.pages.get(index).firePropertyChange("AccessibleState", AccessibleState.SELECTED, null);
                oldName = this.accessibleContext.getAccessibleName();
            }
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", component, null);
        }
        this.setTabComponentAt(index, null);
        this.pages.remove(index);
        this.putClientProperty("__index_to_remove__", index);
        if (selected > index) {
            this.setSelectedIndexImpl(selected - 1, false);
        } else if (selected >= this.getTabCount()) {
            this.setSelectedIndexImpl(selected - 1, false);
            Page newSelected = selected != 0 ? this.pages.get(selected - 1) : null;
            this.changeAccessibleSelection(null, oldName, newSelected);
        } else if (index == selected) {
            this.fireStateChanged();
            this.changeAccessibleSelection(null, oldName, this.pages.get(index));
        }
        if (component != null) {
            Component[] components = this.getComponents();
            int i = components.length;
            while (--i >= 0) {
                if (components[i] != component) continue;
                super.remove(i);
                component.setVisible(true);
                this.clearAccessibleParent(component);
                break;
            }
        }
        if (shouldChangeFocus) {
            SwingUtilities2.tabbedPaneChangeFocusTo(this.getSelectedComponent());
        }
        this.revalidate();
        this.repaint();
    }

    @Override
    public void remove(Component component) {
        int index = this.indexOfComponent(component);
        if (index != -1) {
            this.removeTabAt(index);
        } else {
            Component[] children = this.getComponents();
            for (int i = 0; i < children.length; ++i) {
                if (component != children[i]) continue;
                super.remove(i);
                break;
            }
        }
    }

    @Override
    public void remove(int index) {
        this.removeTabAt(index);
    }

    @Override
    public void removeAll() {
        this.setSelectedIndexImpl(-1, true);
        int tabCount = this.getTabCount();
        while (tabCount-- > 0) {
            this.removeTabAt(tabCount);
        }
    }

    @BeanProperty(bound=false)
    public int getTabCount() {
        return this.pages.size();
    }

    @BeanProperty(bound=false)
    public int getTabRunCount() {
        if (this.ui != null) {
            return ((TabbedPaneUI)this.ui).getTabRunCount(this);
        }
        return 0;
    }

    public String getTitleAt(int index) {
        return this.pages.get((int)index).title;
    }

    public Icon getIconAt(int index) {
        return this.pages.get((int)index).icon;
    }

    public Icon getDisabledIconAt(int index) {
        Page page = this.pages.get(index);
        if (page.disabledIcon == null) {
            page.disabledIcon = UIManager.getLookAndFeel().getDisabledIcon(this, page.icon);
        }
        return page.disabledIcon;
    }

    public String getToolTipTextAt(int index) {
        return this.pages.get((int)index).tip;
    }

    public Color getBackgroundAt(int index) {
        return this.pages.get(index).getBackground();
    }

    public Color getForegroundAt(int index) {
        return this.pages.get(index).getForeground();
    }

    public boolean isEnabledAt(int index) {
        return this.pages.get(index).isEnabled();
    }

    public Component getComponentAt(int index) {
        return this.pages.get((int)index).component;
    }

    public int getMnemonicAt(int tabIndex) {
        this.checkIndex(tabIndex);
        Page page = this.pages.get(tabIndex);
        return page.getMnemonic();
    }

    public int getDisplayedMnemonicIndexAt(int tabIndex) {
        this.checkIndex(tabIndex);
        Page page = this.pages.get(tabIndex);
        return page.getDisplayedMnemonicIndex();
    }

    public Rectangle getBoundsAt(int index) {
        this.checkIndex(index);
        if (this.ui != null) {
            return ((TabbedPaneUI)this.ui).getTabBounds(this, index);
        }
        return null;
    }

    @BeanProperty(preferred=true, visualUpdate=true, description="The title at the specified tab index.")
    public void setTitleAt(int index, String title) {
        Page page = this.pages.get(index);
        String oldTitle = page.title;
        page.title = title;
        if (oldTitle != title) {
            this.firePropertyChange("indexForTitle", -1, index);
        }
        page.updateDisplayedMnemonicIndex();
        if (oldTitle != title && this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldTitle, title);
        }
        if (title == null || oldTitle == null || !title.equals(oldTitle)) {
            this.revalidate();
            this.repaint();
        }
    }

    @BeanProperty(preferred=true, visualUpdate=true, description="The icon at the specified tab index.")
    public void setIconAt(int index, Icon icon) {
        Page page = this.pages.get(index);
        Icon oldIcon = page.icon;
        if (icon != oldIcon) {
            page.icon = icon;
            if (page.disabledIcon instanceof UIResource) {
                page.disabledIcon = null;
            }
            if (this.accessibleContext != null) {
                this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldIcon, icon);
            }
            this.revalidate();
            this.repaint();
        }
    }

    @BeanProperty(preferred=true, visualUpdate=true, description="The disabled icon at the specified tab index.")
    public void setDisabledIconAt(int index, Icon disabledIcon) {
        Icon oldIcon = this.pages.get((int)index).disabledIcon;
        this.pages.get((int)index).disabledIcon = disabledIcon;
        if (disabledIcon != oldIcon && !this.isEnabledAt(index)) {
            this.revalidate();
            this.repaint();
        }
    }

    @BeanProperty(preferred=true, description="The tooltip text at the specified tab index.")
    public void setToolTipTextAt(int index, String toolTipText) {
        String oldToolTipText = this.pages.get((int)index).tip;
        this.pages.get((int)index).tip = toolTipText;
        if (oldToolTipText != toolTipText && this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldToolTipText, toolTipText);
        }
        if (!this.haveRegistered && toolTipText != null) {
            ToolTipManager.sharedInstance().registerComponent(this);
            this.haveRegistered = true;
        }
    }

    @BeanProperty(preferred=true, visualUpdate=true, description="The background color at the specified tab index.")
    public void setBackgroundAt(int index, Color background) {
        Rectangle tabBounds;
        Color oldBg = this.pages.get((int)index).background;
        this.pages.get(index).setBackground(background);
        if (!(background != null && oldBg != null && background.equals(oldBg) || (tabBounds = this.getBoundsAt(index)) == null)) {
            this.repaint(tabBounds);
        }
    }

    @BeanProperty(preferred=true, visualUpdate=true, description="The foreground color at the specified tab index.")
    public void setForegroundAt(int index, Color foreground) {
        Rectangle tabBounds;
        Color oldFg = this.pages.get((int)index).foreground;
        this.pages.get(index).setForeground(foreground);
        if (!(foreground != null && oldFg != null && foreground.equals(oldFg) || (tabBounds = this.getBoundsAt(index)) == null)) {
            this.repaint(tabBounds);
        }
    }

    public void setEnabledAt(int index, boolean enabled) {
        boolean oldEnabled = this.pages.get(index).isEnabled();
        this.pages.get(index).setEnabled(enabled);
        if (enabled != oldEnabled) {
            this.revalidate();
            this.repaint();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @BeanProperty(visualUpdate=true, description="The component at the specified tab index.")
    public void setComponentAt(int index, Component component) {
        Page page = this.pages.get(index);
        if (component != page.component) {
            boolean selectedPage;
            boolean shouldChangeFocus = false;
            if (page.component != null) {
                shouldChangeFocus = SwingUtilities.findFocusOwner(page.component) != null;
                Object object = this.getTreeLock();
                synchronized (object) {
                    int count = this.getComponentCount();
                    Component[] children = this.getComponents();
                    for (int i = 0; i < count; ++i) {
                        if (children[i] != page.component) continue;
                        super.remove(i);
                        this.clearAccessibleParent(children[i]);
                    }
                }
            }
            page.component = component;
            boolean bl = selectedPage = this.getSelectedIndex() == index;
            if (selectedPage) {
                if (this.visComp != null && this.visComp.isVisible() && !this.visComp.equals(component)) {
                    this.visComp.setVisible(false);
                }
                this.visComp = component;
            }
            if (component != null) {
                component.setVisible(selectedPage);
                this.addImpl(component, null, -1);
                if (shouldChangeFocus) {
                    SwingUtilities2.tabbedPaneChangeFocusTo(component);
                }
            } else {
                this.repaint();
            }
            this.revalidate();
        }
    }

    @BeanProperty(visualUpdate=true, description="the index into the String to draw the keyboard character mnemonic at")
    public void setDisplayedMnemonicIndexAt(int tabIndex, int mnemonicIndex) {
        this.checkIndex(tabIndex);
        Page page = this.pages.get(tabIndex);
        page.setDisplayedMnemonicIndex(mnemonicIndex);
    }

    @BeanProperty(visualUpdate=true, description="The keyboard mnenmonic, as a KeyEvent VK constant, for the specified tab")
    public void setMnemonicAt(int tabIndex, int mnemonic) {
        this.checkIndex(tabIndex);
        Page page = this.pages.get(tabIndex);
        page.setMnemonic(mnemonic);
        this.firePropertyChange("mnemonicAt", null, null);
    }

    public int indexOfTab(String title) {
        for (int i = 0; i < this.getTabCount(); ++i) {
            if (!this.getTitleAt(i).equals(title == null ? "" : title)) continue;
            return i;
        }
        return -1;
    }

    public int indexOfTab(Icon icon) {
        for (int i = 0; i < this.getTabCount(); ++i) {
            Icon tabIcon = this.getIconAt(i);
            if ((tabIcon == null || !tabIcon.equals(icon)) && (tabIcon != null || tabIcon != icon)) continue;
            return i;
        }
        return -1;
    }

    public int indexOfComponent(Component component) {
        for (int i = 0; i < this.getTabCount(); ++i) {
            Component c = this.getComponentAt(i);
            if ((c == null || !c.equals(component)) && (c != null || c != component)) continue;
            return i;
        }
        return -1;
    }

    public int indexAtLocation(int x, int y) {
        if (this.ui != null) {
            return ((TabbedPaneUI)this.ui).tabForCoordinate(this, x, y);
        }
        return -1;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int index;
        if (this.ui != null && (index = ((TabbedPaneUI)this.ui).tabForCoordinate(this, event.getX(), event.getY())) != -1) {
            return this.pages.get((int)index).tip;
        }
        return super.getToolTipText(event);
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= this.pages.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Tab count: " + this.pages.size());
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
    void compWriteObjectNotify() {
        super.compWriteObjectNotify();
        if (this.getToolTipText() == null && this.haveRegistered) {
            ToolTipManager.sharedInstance().unregisterComponent(this);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField f = s.readFields();
        int newTabPlacement = f.get("tabPlacement", 1);
        JTabbedPane.checkTabPlacement(newTabPlacement);
        this.tabPlacement = newTabPlacement;
        int newTabLayoutPolicy = f.get("tabLayoutPolicy", 0);
        JTabbedPane.checkTabLayoutPolicy(newTabLayoutPolicy);
        this.tabLayoutPolicy = newTabLayoutPolicy;
        this.model = (SingleSelectionModel)f.get("model", null);
        this.haveRegistered = f.get("haveRegistered", false);
        this.changeListener = (ChangeListener)f.get("changeListener", null);
        this.pages = (List)f.get("pages", null);
        this.visComp = (Component)f.get("visComp", null);
        if (this.ui != null && this.getUIClassID().equals(uiClassID)) {
            this.ui.installUI(this);
        }
        if (this.getToolTipText() == null && this.haveRegistered) {
            ToolTipManager.sharedInstance().registerComponent(this);
        }
    }

    @Override
    protected String paramString() {
        String tabPlacementString = this.tabPlacement == 1 ? "TOP" : (this.tabPlacement == 3 ? "BOTTOM" : (this.tabPlacement == 2 ? "LEFT" : (this.tabPlacement == 4 ? "RIGHT" : "")));
        String haveRegisteredString = this.haveRegistered ? "true" : "false";
        return super.paramString() + ",haveRegistered=" + haveRegisteredString + ",tabPlacement=" + tabPlacementString;
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJTabbedPane();
            int count = this.getTabCount();
            for (int i = 0; i < count; ++i) {
                this.pages.get(i).initAccessibleContext();
            }
        }
        return this.accessibleContext;
    }

    @BeanProperty(preferred=true, visualUpdate=true, description="The tab component at the specified tab index.")
    public void setTabComponentAt(int index, Component component) {
        if (component != null && this.indexOfComponent(component) != -1) {
            throw new IllegalArgumentException("Component is already added to this JTabbedPane");
        }
        Component oldValue = this.getTabComponentAt(index);
        if (component != oldValue) {
            int tabComponentIndex = this.indexOfTabComponent(component);
            if (tabComponentIndex != -1) {
                this.setTabComponentAt(tabComponentIndex, null);
            }
            this.pages.get((int)index).tabComponent = component;
            this.firePropertyChange("indexForTabComponent", -1, index);
        }
    }

    public Component getTabComponentAt(int index) {
        return this.pages.get((int)index).tabComponent;
    }

    public int indexOfTabComponent(Component tabComponent) {
        for (int i = 0; i < this.getTabCount(); ++i) {
            Component c = this.getTabComponentAt(i);
            if (c != tabComponent) continue;
            return i;
        }
        return -1;
    }

    private class Page
    extends AccessibleContext
    implements Serializable,
    Accessible,
    AccessibleComponent,
    AccessibleValue {
        String title;
        Color background;
        Color foreground;
        Icon icon;
        Icon disabledIcon;
        JTabbedPane parent;
        Component component;
        String tip;
        boolean enabled = true;
        boolean needsUIUpdate;
        int mnemonic = -1;
        int mnemonicIndex = -1;
        Component tabComponent;

        Page(JTabbedPane parent, String title, Icon icon, Icon disabledIcon, Component component, String tip) {
            this.title = title;
            this.icon = icon;
            this.disabledIcon = disabledIcon;
            this.parent = parent;
            this.setAccessibleParent(parent);
            this.component = component;
            this.tip = tip;
            this.initAccessibleContext();
        }

        void initAccessibleContext() {
            AccessibleContext ac;
            if (JTabbedPane.this.accessibleContext != null && this.component instanceof Accessible && (ac = this.component.getAccessibleContext()) != null) {
                ac.setAccessibleParent(this);
            }
        }

        void setMnemonic(int mnemonic) {
            this.mnemonic = mnemonic;
            this.updateDisplayedMnemonicIndex();
        }

        int getMnemonic() {
            return this.mnemonic;
        }

        void setDisplayedMnemonicIndex(int mnemonicIndex) {
            if (this.mnemonicIndex != mnemonicIndex) {
                String t = this.getTitle();
                if (mnemonicIndex != -1 && (t == null || mnemonicIndex < 0 || mnemonicIndex >= t.length())) {
                    throw new IllegalArgumentException("Invalid mnemonic index: " + mnemonicIndex);
                }
                this.mnemonicIndex = mnemonicIndex;
                JTabbedPane.this.firePropertyChange("displayedMnemonicIndexAt", null, null);
            }
        }

        int getDisplayedMnemonicIndex() {
            return this.mnemonicIndex;
        }

        void updateDisplayedMnemonicIndex() {
            this.setDisplayedMnemonicIndex(SwingUtilities.findDisplayedMnemonicIndex(this.getTitle(), this.mnemonic));
        }

        @Override
        public AccessibleContext getAccessibleContext() {
            return this;
        }

        @Override
        public String getAccessibleName() {
            if (this.accessibleName != null) {
                return this.accessibleName;
            }
            return this.getTitle();
        }

        @Override
        public String getAccessibleDescription() {
            if (this.accessibleDescription != null) {
                return this.accessibleDescription;
            }
            if (this.tip != null) {
                return this.tip;
            }
            return null;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PAGE_TAB;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = this.parent.getAccessibleContext().getAccessibleStateSet();
            states.add(AccessibleState.SELECTABLE);
            if (this.getPageIndex() == this.parent.getSelectedIndex()) {
                states.add(AccessibleState.SELECTED);
            }
            return states;
        }

        @Override
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        @Override
        public Number getCurrentAccessibleValue() {
            return this.getPageIndex() == this.parent.getSelectedIndex() ? Integer.valueOf(1) : Integer.valueOf(0);
        }

        @Override
        public boolean setCurrentAccessibleValue(Number n) {
            if (this.getPageIndex() != this.parent.getSelectedIndex()) {
                if (n.intValue() != 0) {
                    this.parent.setSelectedIndex(this.getPageIndex());
                }
            } else if (n.intValue() == 0) {
                return false;
            }
            return true;
        }

        @Override
        public Number getMinimumAccessibleValue() {
            return 0;
        }

        @Override
        public Number getMaximumAccessibleValue() {
            return 1;
        }

        @Override
        public int getAccessibleIndexInParent() {
            return this.getPageIndex();
        }

        @Override
        public int getAccessibleChildrenCount() {
            if (this.component instanceof Accessible) {
                return 1;
            }
            return 0;
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            if (this.component instanceof Accessible) {
                return (Accessible)((Object)this.component);
            }
            return null;
        }

        @Override
        public Locale getLocale() {
            return this.parent.getLocale();
        }

        @Override
        public AccessibleComponent getAccessibleComponent() {
            return this;
        }

        @Override
        public Color getBackground() {
            return this.background != null ? this.background : this.parent.getBackground();
        }

        @Override
        public void setBackground(Color c) {
            this.background = c;
        }

        @Override
        public Color getForeground() {
            return this.foreground != null ? this.foreground : this.parent.getForeground();
        }

        @Override
        public void setForeground(Color c) {
            this.foreground = c;
        }

        @Override
        public Cursor getCursor() {
            return this.parent.getCursor();
        }

        @Override
        public void setCursor(Cursor c) {
            this.parent.setCursor(c);
        }

        @Override
        public Font getFont() {
            return this.parent.getFont();
        }

        @Override
        public void setFont(Font f) {
            this.parent.setFont(f);
        }

        @Override
        public FontMetrics getFontMetrics(Font f) {
            return this.parent.getFontMetrics(f);
        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }

        @Override
        public void setEnabled(boolean b) {
            this.enabled = b;
        }

        @Override
        public boolean isVisible() {
            return this.parent.isVisible();
        }

        @Override
        public void setVisible(boolean b) {
            this.parent.setVisible(b);
        }

        @Override
        public boolean isShowing() {
            return this.parent.isShowing();
        }

        @Override
        public boolean contains(Point p) {
            Rectangle r = this.getBounds();
            return r.contains(p);
        }

        @Override
        public Point getLocationOnScreen() {
            Point parentLocation;
            try {
                parentLocation = this.parent.getLocationOnScreen();
            }
            catch (IllegalComponentStateException icse) {
                return null;
            }
            Point componentLocation = this.getLocation();
            if (parentLocation == null || componentLocation == null) {
                return null;
            }
            componentLocation.translate(parentLocation.x, parentLocation.y);
            return componentLocation;
        }

        @Override
        public Point getLocation() {
            Rectangle r = this.getBounds();
            return r == null ? null : new Point(r.x, r.y);
        }

        @Override
        public void setLocation(Point p) {
        }

        @Override
        public Rectangle getBounds() {
            return this.parent.getUI().getTabBounds(this.parent, this.getPageIndex());
        }

        @Override
        public void setBounds(Rectangle r) {
        }

        @Override
        public Dimension getSize() {
            Rectangle r = this.getBounds();
            return r == null ? null : new Dimension(r.width, r.height);
        }

        @Override
        public void setSize(Dimension d) {
        }

        @Override
        public Accessible getAccessibleAt(Point p) {
            if (this.component instanceof Accessible) {
                return (Accessible)((Object)this.component);
            }
            return null;
        }

        @Override
        public boolean isFocusTraversable() {
            return false;
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
        public AccessibleIcon[] getAccessibleIcon() {
            AccessibleIcon accessibleIcon = null;
            if (this.enabled && this.icon instanceof ImageIcon) {
                ac = ((ImageIcon)this.icon).getAccessibleContext();
                accessibleIcon = (AccessibleIcon)((Object)ac);
            } else if (!this.enabled && this.disabledIcon instanceof ImageIcon) {
                ac = ((ImageIcon)this.disabledIcon).getAccessibleContext();
                accessibleIcon = (AccessibleIcon)((Object)ac);
            }
            if (accessibleIcon != null) {
                AccessibleIcon[] returnIcons = new AccessibleIcon[]{accessibleIcon};
                return returnIcons;
            }
            return null;
        }

        private String getTitle() {
            return JTabbedPane.this.getTitleAt(this.getPageIndex());
        }

        private int getPageIndex() {
            int index = this.component != null || this.component == null && this.tabComponent == null ? this.parent.indexOfComponent(this.component) : this.parent.indexOfTabComponent(this.tabComponent);
            return index;
        }
    }

    protected class ModelListener
    implements ChangeListener,
    Serializable {
        protected ModelListener() {
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JTabbedPane.this.fireStateChanged();
        }
    }

    protected class AccessibleJTabbedPane
    extends JComponent.AccessibleJComponent
    implements AccessibleSelection,
    ChangeListener {
        @Override
        public String getAccessibleName() {
            if (this.accessibleName != null) {
                return this.accessibleName;
            }
            String cp = (String)JTabbedPane.this.getClientProperty("AccessibleName");
            if (cp != null) {
                return cp;
            }
            int index = JTabbedPane.this.getSelectedIndex();
            if (index >= 0) {
                return JTabbedPane.this.pages.get(index).getAccessibleName();
            }
            return super.getAccessibleName();
        }

        public AccessibleJTabbedPane() {
            JTabbedPane.this.model.addChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            Object o = e.getSource();
            this.firePropertyChange("AccessibleSelection", null, o);
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PAGE_TAB_LIST;
        }

        @Override
        public int getAccessibleChildrenCount() {
            return JTabbedPane.this.getTabCount();
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            if (i < 0 || i >= JTabbedPane.this.getTabCount()) {
                return null;
            }
            return JTabbedPane.this.pages.get(i);
        }

        @Override
        public AccessibleSelection getAccessibleSelection() {
            return this;
        }

        @Override
        public Accessible getAccessibleAt(Point p) {
            int tab = ((TabbedPaneUI)JTabbedPane.this.ui).tabForCoordinate(JTabbedPane.this, p.x, p.y);
            if (tab == -1) {
                tab = JTabbedPane.this.getSelectedIndex();
            }
            return this.getAccessibleChild(tab);
        }

        @Override
        public int getAccessibleSelectionCount() {
            return 1;
        }

        @Override
        public Accessible getAccessibleSelection(int i) {
            int index = JTabbedPane.this.getSelectedIndex();
            if (index == -1) {
                return null;
            }
            return JTabbedPane.this.pages.get(index);
        }

        @Override
        public boolean isAccessibleChildSelected(int i) {
            return i == JTabbedPane.this.getSelectedIndex();
        }

        @Override
        public void addAccessibleSelection(int i) {
            JTabbedPane.this.setSelectedIndex(i);
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

