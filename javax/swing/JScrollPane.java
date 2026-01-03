/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRelation;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.Scrollable;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ScrollPaneUI;
import javax.swing.plaf.UIResource;

@JavaBean(defaultProperty="UI", description="A specialized container that manages a viewport, optional scrollbars and headers")
@SwingContainer(delegate="getViewport")
public class JScrollPane
extends JComponent
implements ScrollPaneConstants,
Accessible {
    private Border viewportBorder;
    private static final String uiClassID = "ScrollPaneUI";
    protected int verticalScrollBarPolicy = 20;
    protected int horizontalScrollBarPolicy = 30;
    protected JViewport viewport;
    protected JScrollBar verticalScrollBar;
    protected JScrollBar horizontalScrollBar;
    protected JViewport rowHeader;
    protected JViewport columnHeader;
    protected Component lowerLeft;
    protected Component lowerRight;
    protected Component upperLeft;
    protected Component upperRight;
    private boolean wheelScrollState = true;

    public JScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        this.setLayout(new ScrollPaneLayout.UIResource());
        this.setVerticalScrollBarPolicy(vsbPolicy);
        this.setHorizontalScrollBarPolicy(hsbPolicy);
        this.setViewport(this.createViewport());
        this.setVerticalScrollBar(this.createVerticalScrollBar());
        this.setHorizontalScrollBar(this.createHorizontalScrollBar());
        if (view != null) {
            this.setViewportView(view);
        }
        this.setUIProperty("opaque", true);
        this.updateUI();
        if (!this.getComponentOrientation().isLeftToRight()) {
            this.viewport.setViewPosition(new Point(Integer.MAX_VALUE, 0));
        }
    }

    public JScrollPane(Component view) {
        this(view, 20, 30);
    }

    public JScrollPane(int vsbPolicy, int hsbPolicy) {
        this(null, vsbPolicy, hsbPolicy);
    }

    public JScrollPane() {
        this(null, 20, 30);
    }

    @Override
    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel.")
    public ScrollPaneUI getUI() {
        return (ScrollPaneUI)this.ui;
    }

    public void setUI(ScrollPaneUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        this.setUI((ScrollPaneUI)UIManager.getUI(this));
    }

    @Override
    @BeanProperty(bound=false, hidden=true)
    public String getUIClassID() {
        return uiClassID;
    }

    @Override
    public void setLayout(LayoutManager layout) {
        if (layout instanceof ScrollPaneLayout) {
            super.setLayout(layout);
            ((ScrollPaneLayout)layout).syncWithScrollPane(this);
        } else if (layout == null) {
            super.setLayout(layout);
        } else {
            String s = "layout of JScrollPane must be a ScrollPaneLayout";
            throw new ClassCastException(s);
        }
    }

    @Override
    @BeanProperty(hidden=true)
    public boolean isValidateRoot() {
        return true;
    }

    public int getVerticalScrollBarPolicy() {
        return this.verticalScrollBarPolicy;
    }

    @BeanProperty(preferred=true, enumerationValues={"ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED", "ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER", "ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS"}, description="The scrollpane vertical scrollbar policy")
    public void setVerticalScrollBarPolicy(int policy) {
        switch (policy) {
            case 20: 
            case 21: 
            case 22: {
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid verticalScrollBarPolicy");
            }
        }
        int old = this.verticalScrollBarPolicy;
        this.verticalScrollBarPolicy = policy;
        this.firePropertyChange("verticalScrollBarPolicy", old, policy);
        this.revalidate();
        this.repaint();
    }

    public int getHorizontalScrollBarPolicy() {
        return this.horizontalScrollBarPolicy;
    }

    @BeanProperty(preferred=true, enumerationValues={"ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED", "ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER", "ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS"}, description="The scrollpane scrollbar policy")
    public void setHorizontalScrollBarPolicy(int policy) {
        switch (policy) {
            case 30: 
            case 31: 
            case 32: {
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid horizontalScrollBarPolicy");
            }
        }
        int old = this.horizontalScrollBarPolicy;
        this.horizontalScrollBarPolicy = policy;
        this.firePropertyChange("horizontalScrollBarPolicy", old, policy);
        this.revalidate();
        this.repaint();
    }

    public Border getViewportBorder() {
        return this.viewportBorder;
    }

    @BeanProperty(preferred=true, description="The border around the viewport.")
    public void setViewportBorder(Border viewportBorder) {
        Border oldValue = this.viewportBorder;
        this.viewportBorder = viewportBorder;
        this.firePropertyChange("viewportBorder", oldValue, viewportBorder);
    }

    @BeanProperty(bound=false)
    public Rectangle getViewportBorderBounds() {
        JScrollBar hsb;
        JScrollBar vsb;
        JViewport rowHead;
        Rectangle borderR = new Rectangle(this.getSize());
        Insets insets = this.getInsets();
        borderR.x = insets.left;
        borderR.y = insets.top;
        borderR.width -= insets.left + insets.right;
        borderR.height -= insets.top + insets.bottom;
        boolean leftToRight = SwingUtilities.isLeftToRight(this);
        JViewport colHead = this.getColumnHeader();
        if (colHead != null && colHead.isVisible()) {
            int colHeadHeight = colHead.getHeight();
            borderR.y += colHeadHeight;
            borderR.height -= colHeadHeight;
        }
        if ((rowHead = this.getRowHeader()) != null && rowHead.isVisible()) {
            int rowHeadWidth = rowHead.getWidth();
            if (leftToRight) {
                borderR.x += rowHeadWidth;
            }
            borderR.width -= rowHeadWidth;
        }
        if ((vsb = this.getVerticalScrollBar()) != null && vsb.isVisible()) {
            int vsbWidth = vsb.getWidth();
            if (!leftToRight) {
                borderR.x += vsbWidth;
            }
            borderR.width -= vsbWidth;
        }
        if ((hsb = this.getHorizontalScrollBar()) != null && hsb.isVisible()) {
            borderR.height -= hsb.getHeight();
        }
        return borderR;
    }

    public JScrollBar createHorizontalScrollBar() {
        return new ScrollBar(0);
    }

    @Transient
    public JScrollBar getHorizontalScrollBar() {
        return this.horizontalScrollBar;
    }

    @BeanProperty(expert=true, description="The horizontal scrollbar.")
    public void setHorizontalScrollBar(JScrollBar horizontalScrollBar) {
        JScrollBar old = this.getHorizontalScrollBar();
        this.horizontalScrollBar = horizontalScrollBar;
        if (horizontalScrollBar != null) {
            this.add((Component)horizontalScrollBar, "HORIZONTAL_SCROLLBAR");
        } else if (old != null) {
            this.remove(old);
        }
        this.firePropertyChange("horizontalScrollBar", old, horizontalScrollBar);
        this.revalidate();
        this.repaint();
    }

    public JScrollBar createVerticalScrollBar() {
        return new ScrollBar(1);
    }

    @Transient
    public JScrollBar getVerticalScrollBar() {
        return this.verticalScrollBar;
    }

    @BeanProperty(expert=true, description="The vertical scrollbar.")
    public void setVerticalScrollBar(JScrollBar verticalScrollBar) {
        JScrollBar old = this.getVerticalScrollBar();
        this.verticalScrollBar = verticalScrollBar;
        this.add((Component)verticalScrollBar, "VERTICAL_SCROLLBAR");
        this.firePropertyChange("verticalScrollBar", old, verticalScrollBar);
        this.revalidate();
        this.repaint();
    }

    protected JViewport createViewport() {
        return new JViewport();
    }

    public JViewport getViewport() {
        return this.viewport;
    }

    @BeanProperty(expert=true, visualUpdate=true, description="The viewport child for this scrollpane")
    public void setViewport(JViewport viewport) {
        JViewport old = this.getViewport();
        this.viewport = viewport;
        if (viewport != null) {
            this.add((Component)viewport, "VIEWPORT");
        } else if (old != null) {
            this.remove(old);
        }
        this.firePropertyChange("viewport", old, viewport);
        if (this.accessibleContext != null) {
            ((AccessibleJScrollPane)this.accessibleContext).resetViewPort();
        }
        this.revalidate();
        this.repaint();
    }

    public void setViewportView(Component view) {
        if (this.getViewport() == null) {
            this.setViewport(this.createViewport());
        }
        this.getViewport().setView(view);
    }

    @Transient
    public JViewport getRowHeader() {
        return this.rowHeader;
    }

    @BeanProperty(expert=true, description="The row header child for this scrollpane")
    public void setRowHeader(JViewport rowHeader) {
        JViewport old = this.getRowHeader();
        this.rowHeader = rowHeader;
        if (rowHeader != null) {
            this.add((Component)rowHeader, "ROW_HEADER");
        } else if (old != null) {
            this.remove(old);
        }
        this.firePropertyChange("rowHeader", old, rowHeader);
        this.revalidate();
        this.repaint();
    }

    public void setRowHeaderView(Component view) {
        if (this.getRowHeader() == null) {
            this.setRowHeader(this.createViewport());
        }
        this.getRowHeader().setView(view);
    }

    @Transient
    public JViewport getColumnHeader() {
        return this.columnHeader;
    }

    @BeanProperty(visualUpdate=true, description="The column header child for this scrollpane")
    public void setColumnHeader(JViewport columnHeader) {
        JViewport old = this.getColumnHeader();
        this.columnHeader = columnHeader;
        if (columnHeader != null) {
            this.add((Component)columnHeader, "COLUMN_HEADER");
        } else if (old != null) {
            this.remove(old);
        }
        this.firePropertyChange("columnHeader", old, columnHeader);
        this.revalidate();
        this.repaint();
    }

    public void setColumnHeaderView(Component view) {
        if (this.getColumnHeader() == null) {
            this.setColumnHeader(this.createViewport());
        }
        this.getColumnHeader().setView(view);
    }

    public Component getCorner(String key) {
        boolean isLeftToRight = this.getComponentOrientation().isLeftToRight();
        if (key.equals("LOWER_LEADING_CORNER")) {
            key = isLeftToRight ? "LOWER_LEFT_CORNER" : "LOWER_RIGHT_CORNER";
        } else if (key.equals("LOWER_TRAILING_CORNER")) {
            key = isLeftToRight ? "LOWER_RIGHT_CORNER" : "LOWER_LEFT_CORNER";
        } else if (key.equals("UPPER_LEADING_CORNER")) {
            key = isLeftToRight ? "UPPER_LEFT_CORNER" : "UPPER_RIGHT_CORNER";
        } else if (key.equals("UPPER_TRAILING_CORNER")) {
            String string = key = isLeftToRight ? "UPPER_RIGHT_CORNER" : "UPPER_LEFT_CORNER";
        }
        if (key.equals("LOWER_LEFT_CORNER")) {
            return this.lowerLeft;
        }
        if (key.equals("LOWER_RIGHT_CORNER")) {
            return this.lowerRight;
        }
        if (key.equals("UPPER_LEFT_CORNER")) {
            return this.upperLeft;
        }
        if (key.equals("UPPER_RIGHT_CORNER")) {
            return this.upperRight;
        }
        return null;
    }

    public void setCorner(String key, Component corner) {
        Component old;
        boolean isLeftToRight = this.getComponentOrientation().isLeftToRight();
        if (key.equals("LOWER_LEADING_CORNER")) {
            key = isLeftToRight ? "LOWER_LEFT_CORNER" : "LOWER_RIGHT_CORNER";
        } else if (key.equals("LOWER_TRAILING_CORNER")) {
            key = isLeftToRight ? "LOWER_RIGHT_CORNER" : "LOWER_LEFT_CORNER";
        } else if (key.equals("UPPER_LEADING_CORNER")) {
            key = isLeftToRight ? "UPPER_LEFT_CORNER" : "UPPER_RIGHT_CORNER";
        } else if (key.equals("UPPER_TRAILING_CORNER")) {
            String string = key = isLeftToRight ? "UPPER_RIGHT_CORNER" : "UPPER_LEFT_CORNER";
        }
        if (key.equals("LOWER_LEFT_CORNER")) {
            old = this.lowerLeft;
            this.lowerLeft = corner;
        } else if (key.equals("LOWER_RIGHT_CORNER")) {
            old = this.lowerRight;
            this.lowerRight = corner;
        } else if (key.equals("UPPER_LEFT_CORNER")) {
            old = this.upperLeft;
            this.upperLeft = corner;
        } else if (key.equals("UPPER_RIGHT_CORNER")) {
            old = this.upperRight;
            this.upperRight = corner;
        } else {
            throw new IllegalArgumentException("invalid corner key");
        }
        if (old != null) {
            this.remove(old);
        }
        if (corner != null) {
            this.add(corner, key);
        }
        this.firePropertyChange(key, old, corner);
        this.revalidate();
        this.repaint();
    }

    @Override
    public void setComponentOrientation(ComponentOrientation co) {
        super.setComponentOrientation(co);
        if (this.verticalScrollBar != null) {
            this.verticalScrollBar.setComponentOrientation(co);
        }
        if (this.horizontalScrollBar != null) {
            this.horizontalScrollBar.setComponentOrientation(co);
        }
    }

    @BeanProperty(description="Flag for enabling/disabling mouse wheel scrolling")
    public boolean isWheelScrollingEnabled() {
        return this.wheelScrollState;
    }

    @BeanProperty(description="Flag for enabling/disabling mouse wheel scrolling")
    public void setWheelScrollingEnabled(boolean handleWheel) {
        boolean old = this.wheelScrollState;
        this.wheelScrollState = handleWheel;
        this.firePropertyChange("wheelScrollingEnabled", old, handleWheel);
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
        String viewportString;
        String viewportBorderString = this.viewportBorder != null ? this.viewportBorder.toString() : "";
        String string = viewportString = this.viewport != null ? this.viewport.toString() : "";
        String verticalScrollBarPolicyString = this.verticalScrollBarPolicy == 20 ? "VERTICAL_SCROLLBAR_AS_NEEDED" : (this.verticalScrollBarPolicy == 21 ? "VERTICAL_SCROLLBAR_NEVER" : (this.verticalScrollBarPolicy == 22 ? "VERTICAL_SCROLLBAR_ALWAYS" : ""));
        String horizontalScrollBarPolicyString = this.horizontalScrollBarPolicy == 30 ? "HORIZONTAL_SCROLLBAR_AS_NEEDED" : (this.horizontalScrollBarPolicy == 31 ? "HORIZONTAL_SCROLLBAR_NEVER" : (this.horizontalScrollBarPolicy == 32 ? "HORIZONTAL_SCROLLBAR_ALWAYS" : ""));
        String horizontalScrollBarString = this.horizontalScrollBar != null ? this.horizontalScrollBar.toString() : "";
        String verticalScrollBarString = this.verticalScrollBar != null ? this.verticalScrollBar.toString() : "";
        String columnHeaderString = this.columnHeader != null ? this.columnHeader.toString() : "";
        String rowHeaderString = this.rowHeader != null ? this.rowHeader.toString() : "";
        String lowerLeftString = this.lowerLeft != null ? this.lowerLeft.toString() : "";
        String lowerRightString = this.lowerRight != null ? this.lowerRight.toString() : "";
        String upperLeftString = this.upperLeft != null ? this.upperLeft.toString() : "";
        String upperRightString = this.upperRight != null ? this.upperRight.toString() : "";
        return super.paramString() + ",columnHeader=" + columnHeaderString + ",horizontalScrollBar=" + horizontalScrollBarString + ",horizontalScrollBarPolicy=" + horizontalScrollBarPolicyString + ",lowerLeft=" + lowerLeftString + ",lowerRight=" + lowerRightString + ",rowHeader=" + rowHeaderString + ",upperLeft=" + upperLeftString + ",upperRight=" + upperRightString + ",verticalScrollBar=" + verticalScrollBarString + ",verticalScrollBarPolicy=" + verticalScrollBarPolicyString + ",viewport=" + viewportString + ",viewportBorder=" + viewportBorderString;
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJScrollPane();
        }
        return this.accessibleContext;
    }

    protected class ScrollBar
    extends JScrollBar
    implements UIResource {
        private boolean unitIncrementSet;
        private boolean blockIncrementSet;

        public ScrollBar(int orientation) {
            super(orientation);
            this.putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE);
        }

        @Override
        public void setUnitIncrement(int unitIncrement) {
            this.unitIncrementSet = true;
            this.putClientProperty("JScrollBar.fastWheelScrolling", null);
            super.setUnitIncrement(unitIncrement);
        }

        @Override
        public int getUnitIncrement(int direction) {
            JViewport vp = JScrollPane.this.getViewport();
            if (!this.unitIncrementSet && vp != null && vp.getView() instanceof Scrollable) {
                Scrollable view = (Scrollable)((Object)vp.getView());
                Rectangle vr = vp.getViewRect();
                return view.getScrollableUnitIncrement(vr, this.getOrientation(), direction);
            }
            return super.getUnitIncrement(direction);
        }

        @Override
        public void setBlockIncrement(int blockIncrement) {
            this.blockIncrementSet = true;
            this.putClientProperty("JScrollBar.fastWheelScrolling", null);
            super.setBlockIncrement(blockIncrement);
        }

        @Override
        public int getBlockIncrement(int direction) {
            JViewport vp = JScrollPane.this.getViewport();
            if (this.blockIncrementSet || vp == null) {
                return super.getBlockIncrement(direction);
            }
            if (vp.getView() instanceof Scrollable) {
                Scrollable view = (Scrollable)((Object)vp.getView());
                Rectangle vr = vp.getViewRect();
                return view.getScrollableBlockIncrement(vr, this.getOrientation(), direction);
            }
            if (this.getOrientation() == 1) {
                return vp.getExtentSize().height;
            }
            return vp.getExtentSize().width;
        }
    }

    protected class AccessibleJScrollPane
    extends JComponent.AccessibleJComponent
    implements ChangeListener,
    PropertyChangeListener {
        protected JViewport viewPort = null;

        public void resetViewPort() {
            if (this.viewPort != null) {
                this.viewPort.removeChangeListener(this);
                this.viewPort.removePropertyChangeListener(this);
            }
            this.viewPort = JScrollPane.this.getViewport();
            if (this.viewPort != null) {
                this.viewPort.addChangeListener(this);
                this.viewPort.addPropertyChangeListener(this);
            }
        }

        public AccessibleJScrollPane() {
            this.resetViewPort();
            JScrollBar scrollBar = JScrollPane.this.getHorizontalScrollBar();
            if (scrollBar != null) {
                this.setScrollBarRelations(scrollBar);
            }
            if ((scrollBar = JScrollPane.this.getVerticalScrollBar()) != null) {
                this.setScrollBarRelations(scrollBar);
            }
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SCROLL_PANE;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e == null) {
                throw new NullPointerException();
            }
            this.firePropertyChange("AccessibleVisibleData", false, true);
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if ((propertyName == "horizontalScrollBar" || propertyName == "verticalScrollBar") && e.getNewValue() instanceof JScrollBar) {
                this.setScrollBarRelations((JScrollBar)e.getNewValue());
            }
        }

        void setScrollBarRelations(JScrollBar scrollBar) {
            AccessibleRelation controlledBy = new AccessibleRelation(AccessibleRelation.CONTROLLED_BY, scrollBar);
            AccessibleRelation controllerFor = new AccessibleRelation(AccessibleRelation.CONTROLLER_FOR, JScrollPane.this);
            AccessibleContext ac = scrollBar.getAccessibleContext();
            ac.getAccessibleRelationSet().add(controllerFor);
            this.getAccessibleRelationSet().add(controlledBy);
        }
    }
}

