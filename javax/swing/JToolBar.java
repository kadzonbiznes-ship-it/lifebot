/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleStateSet;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingContainer;
import javax.swing.UIManager;
import javax.swing.plaf.ToolBarUI;
import javax.swing.plaf.UIResource;

@JavaBean(defaultProperty="UI", description="A component which displays commonly used controls or Actions.")
@SwingContainer
public class JToolBar
extends JComponent
implements SwingConstants,
Accessible {
    private static final String uiClassID = "ToolBarUI";
    private boolean paintBorder = true;
    private Insets margin = null;
    private boolean floatable = true;
    private int orientation = 0;

    public JToolBar() {
        this(0);
    }

    public JToolBar(int orientation) {
        this(null, orientation);
    }

    public JToolBar(String name) {
        this(name, 0);
    }

    public JToolBar(String name, int orientation) {
        this.setName(name);
        this.checkOrientation(orientation);
        this.orientation = orientation;
        DefaultToolBarLayout layout = new DefaultToolBarLayout(orientation);
        this.setLayout(layout);
        this.addPropertyChangeListener(layout);
        this.updateUI();
    }

    @Override
    public ToolBarUI getUI() {
        return (ToolBarUI)this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel.")
    public void setUI(ToolBarUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        this.setUI((ToolBarUI)UIManager.getUI(this));
        if (this.getLayout() == null) {
            this.setLayout(new DefaultToolBarLayout(this.getOrientation()));
        }
        this.invalidate();
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
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

    public Component getComponentAtIndex(int i) {
        int ncomponents = this.getComponentCount();
        if (i >= 0 && i < ncomponents) {
            Component[] component = this.getComponents();
            return component[i];
        }
        return null;
    }

    @BeanProperty(expert=true, description="The margin between the tool bar's border and contents")
    public void setMargin(Insets m) {
        Insets old = this.margin;
        this.margin = m;
        this.firePropertyChange("margin", old, m);
        this.revalidate();
        this.repaint();
    }

    public Insets getMargin() {
        if (this.margin == null) {
            return new Insets(0, 0, 0, 0);
        }
        return this.margin;
    }

    public boolean isBorderPainted() {
        return this.paintBorder;
    }

    @BeanProperty(expert=true, description="Does the tool bar paint its borders?")
    public void setBorderPainted(boolean b) {
        if (this.paintBorder != b) {
            boolean old = this.paintBorder;
            this.paintBorder = b;
            this.firePropertyChange("borderPainted", old, b);
            this.revalidate();
            this.repaint();
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        if (this.isBorderPainted()) {
            super.paintBorder(g);
        }
    }

    public boolean isFloatable() {
        return this.floatable;
    }

    @BeanProperty(preferred=true, description="Can the tool bar be made to float by the user?")
    public void setFloatable(boolean b) {
        if (this.floatable != b) {
            boolean old = this.floatable;
            this.floatable = b;
            this.firePropertyChange("floatable", old, b);
            this.revalidate();
            this.repaint();
        }
    }

    public int getOrientation() {
        return this.orientation;
    }

    @BeanProperty(preferred=true, enumerationValues={"SwingConstants.HORIZONTAL", "SwingConstants.VERTICAL"}, description="The current orientation of the tool bar")
    public void setOrientation(int o) {
        this.checkOrientation(o);
        if (this.orientation != o) {
            int old = this.orientation;
            this.orientation = o;
            this.firePropertyChange("orientation", old, o);
            this.revalidate();
            this.repaint();
        }
    }

    @BeanProperty(preferred=true, visualUpdate=true, description="Will draw rollover button borders in the toolbar.")
    public void setRollover(boolean rollover) {
        this.putClientProperty("JToolBar.isRollover", rollover ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean isRollover() {
        Boolean rollover = (Boolean)this.getClientProperty("JToolBar.isRollover");
        if (rollover != null) {
            return rollover;
        }
        return false;
    }

    private void checkOrientation(int orientation) {
        switch (orientation) {
            case 0: 
            case 1: {
                break;
            }
            default: {
                throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
            }
        }
    }

    public void addSeparator() {
        this.addSeparator(null);
    }

    public void addSeparator(Dimension size) {
        Separator s = new Separator(size);
        this.add(s);
    }

    public JButton add(Action a) {
        JButton b = this.createActionComponent(a);
        b.setAction(a);
        this.add(b);
        return b;
    }

    protected JButton createActionComponent(Action a) {
        JButton b = new JButton(){

            @Override
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                PropertyChangeListener pcl = JToolBar.this.createActionChangeListener(this);
                if (pcl == null) {
                    pcl = super.createActionPropertyChangeListener(a);
                }
                return pcl;
            }
        };
        if (a != null && (a.getValue("SmallIcon") != null || a.getValue("SwingLargeIconKey") != null)) {
            b.setHideActionText(true);
        }
        b.setHorizontalTextPosition(0);
        b.setVerticalTextPosition(3);
        return b;
    }

    protected PropertyChangeListener createActionChangeListener(JButton b) {
        return null;
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        if (comp instanceof Separator) {
            if (this.getOrientation() == 1) {
                ((Separator)comp).setOrientation(0);
            } else {
                ((Separator)comp).setOrientation(1);
            }
        }
        super.addImpl(comp, constraints, index);
        if (comp instanceof JButton) {
            ((JButton)comp).setDefaultCapable(false);
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
        String paintBorderString = this.paintBorder ? "true" : "false";
        String marginString = this.margin != null ? this.margin.toString() : "";
        String floatableString = this.floatable ? "true" : "false";
        String orientationString = this.orientation == 0 ? "HORIZONTAL" : "VERTICAL";
        return super.paramString() + ",floatable=" + floatableString + ",margin=" + marginString + ",orientation=" + orientationString + ",paintBorder=" + paintBorderString;
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        LayoutManager oldMgr = this.getLayout();
        if (oldMgr instanceof PropertyChangeListener) {
            this.removePropertyChangeListener((PropertyChangeListener)((Object)oldMgr));
        }
        super.setLayout(mgr);
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJToolBar();
        }
        return this.accessibleContext;
    }

    private class DefaultToolBarLayout
    implements LayoutManager2,
    Serializable,
    PropertyChangeListener,
    UIResource {
        BoxLayout lm;

        DefaultToolBarLayout(int orientation) {
            this.lm = orientation == 1 ? new BoxLayout(JToolBar.this, 3) : new BoxLayout(JToolBar.this, 2);
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
            this.lm.addLayoutComponent(name, comp);
        }

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
            this.lm.addLayoutComponent(comp, constraints);
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            this.lm.removeLayoutComponent(comp);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return this.lm.preferredLayoutSize(target);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return this.lm.minimumLayoutSize(target);
        }

        @Override
        public Dimension maximumLayoutSize(Container target) {
            return this.lm.maximumLayoutSize(target);
        }

        @Override
        public void layoutContainer(Container target) {
            this.lm.layoutContainer(target);
        }

        @Override
        public float getLayoutAlignmentX(Container target) {
            return this.lm.getLayoutAlignmentX(target);
        }

        @Override
        public float getLayoutAlignmentY(Container target) {
            return this.lm.getLayoutAlignmentY(target);
        }

        @Override
        public void invalidateLayout(Container target) {
            this.lm.invalidateLayout(target);
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if (name.equals("orientation")) {
                int o = (Integer)e.getNewValue();
                this.lm = o == 1 ? new BoxLayout(JToolBar.this, 3) : new BoxLayout(JToolBar.this, 2);
            }
        }
    }

    public static class Separator
    extends JSeparator {
        private Dimension separatorSize;

        public Separator() {
            this(null);
        }

        public Separator(Dimension size) {
            super(0);
            this.setSeparatorSize(size);
        }

        @Override
        public String getUIClassID() {
            return "ToolBarSeparatorUI";
        }

        public void setSeparatorSize(Dimension size) {
            if (size != null) {
                this.separatorSize = size;
            } else {
                super.updateUI();
            }
            this.invalidate();
        }

        public Dimension getSeparatorSize() {
            return this.separatorSize;
        }

        @Override
        public Dimension getMinimumSize() {
            if (this.separatorSize != null) {
                return this.separatorSize.getSize();
            }
            return super.getMinimumSize();
        }

        @Override
        public Dimension getMaximumSize() {
            if (this.separatorSize != null) {
                return this.separatorSize.getSize();
            }
            return super.getMaximumSize();
        }

        @Override
        public Dimension getPreferredSize() {
            if (this.separatorSize != null) {
                return this.separatorSize.getSize();
            }
            return super.getPreferredSize();
        }
    }

    protected class AccessibleJToolBar
    extends JComponent.AccessibleJComponent {
        protected AccessibleJToolBar() {
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            return states;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TOOL_BAR;
        }
    }
}

