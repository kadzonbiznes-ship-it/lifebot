/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Adjustable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleValue;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.SwingContainer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ScrollBarUI;

@JavaBean(defaultProperty="UI", description="A component that helps determine the visible content range of an area.")
@SwingContainer(value=false)
public class JScrollBar
extends JComponent
implements Adjustable,
Accessible {
    private static final String uiClassID = "ScrollBarUI";
    private ChangeListener fwdAdjustmentEvents = new ModelListener();
    protected BoundedRangeModel model;
    protected int orientation;
    protected int unitIncrement;
    protected int blockIncrement;

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

    public JScrollBar(int orientation, int value, int extent, int min, int max) {
        this.checkOrientation(orientation);
        this.unitIncrement = 1;
        this.blockIncrement = extent == 0 ? 1 : extent;
        this.orientation = orientation;
        this.model = new DefaultBoundedRangeModel(value, extent, min, max);
        this.model.addChangeListener(this.fwdAdjustmentEvents);
        this.setRequestFocusEnabled(false);
        this.updateUI();
    }

    public JScrollBar(int orientation) {
        this(orientation, 0, 10, 0, 100);
    }

    public JScrollBar() {
        this(1);
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel")
    public void setUI(ScrollBarUI ui) {
        super.setUI(ui);
    }

    @Override
    public ScrollBarUI getUI() {
        return (ScrollBarUI)this.ui;
    }

    @Override
    public void updateUI() {
        this.setUI((ScrollBarUI)UIManager.getUI(this));
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    @Override
    public int getOrientation() {
        return this.orientation;
    }

    @BeanProperty(preferred=true, visualUpdate=true, enumerationValues={"JScrollBar.VERTICAL", "JScrollBar.HORIZONTAL"}, description="The scrollbar's orientation.")
    public void setOrientation(int orientation) {
        this.checkOrientation(orientation);
        int oldValue = this.orientation;
        this.orientation = orientation;
        this.firePropertyChange("orientation", oldValue, orientation);
        if (oldValue != orientation && this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleState", oldValue == 1 ? AccessibleState.VERTICAL : AccessibleState.HORIZONTAL, orientation == 1 ? AccessibleState.VERTICAL : AccessibleState.HORIZONTAL);
        }
        if (orientation != oldValue) {
            this.revalidate();
        }
    }

    public BoundedRangeModel getModel() {
        return this.model;
    }

    @BeanProperty(expert=true, description="The scrollbar's BoundedRangeModel.")
    public void setModel(BoundedRangeModel newModel) {
        Integer oldValue = null;
        BoundedRangeModel oldModel = this.model;
        if (this.model != null) {
            this.model.removeChangeListener(this.fwdAdjustmentEvents);
            oldValue = this.model.getValue();
        }
        this.model = newModel;
        if (this.model != null) {
            this.model.addChangeListener(this.fwdAdjustmentEvents);
        }
        this.firePropertyChange("model", oldModel, this.model);
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleValue", oldValue, this.model.getValue());
        }
    }

    public int getUnitIncrement(int direction) {
        return this.unitIncrement;
    }

    @Override
    @BeanProperty(preferred=true, description="The scrollbar's unit increment.")
    public void setUnitIncrement(int unitIncrement) {
        int oldValue = this.unitIncrement;
        this.unitIncrement = unitIncrement;
        this.firePropertyChange("unitIncrement", oldValue, unitIncrement);
    }

    public int getBlockIncrement(int direction) {
        return this.blockIncrement;
    }

    @Override
    @BeanProperty(preferred=true, description="The scrollbar's block increment.")
    public void setBlockIncrement(int blockIncrement) {
        int oldValue = this.blockIncrement;
        this.blockIncrement = blockIncrement;
        this.firePropertyChange("blockIncrement", oldValue, blockIncrement);
    }

    @Override
    public int getUnitIncrement() {
        return this.unitIncrement;
    }

    @Override
    public int getBlockIncrement() {
        return this.blockIncrement;
    }

    @Override
    public int getValue() {
        return this.getModel().getValue();
    }

    @Override
    @BeanProperty(bound=false, preferred=true, description="The scrollbar's current value.")
    public void setValue(int value) {
        BoundedRangeModel m = this.getModel();
        int oldValue = m.getValue();
        m.setValue(value);
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleValue", oldValue, m.getValue());
        }
    }

    @Override
    public int getVisibleAmount() {
        return this.getModel().getExtent();
    }

    @Override
    @BeanProperty(bound=false, preferred=true, description="The amount of the view that is currently visible.")
    public void setVisibleAmount(int extent) {
        this.getModel().setExtent(extent);
    }

    @Override
    public int getMinimum() {
        return this.getModel().getMinimum();
    }

    @Override
    @BeanProperty(bound=false, preferred=true, description="The scrollbar's minimum value.")
    public void setMinimum(int minimum) {
        this.getModel().setMinimum(minimum);
    }

    @Override
    public int getMaximum() {
        return this.getModel().getMaximum();
    }

    @Override
    @BeanProperty(bound=false, preferred=true, description="The scrollbar's maximum value.")
    public void setMaximum(int maximum) {
        this.getModel().setMaximum(maximum);
    }

    public boolean getValueIsAdjusting() {
        return this.getModel().getValueIsAdjusting();
    }

    @BeanProperty(bound=false, expert=true, description="True if the scrollbar thumb is being dragged.")
    public void setValueIsAdjusting(boolean b) {
        BoundedRangeModel m = this.getModel();
        boolean oldValue = m.getValueIsAdjusting();
        m.setValueIsAdjusting(b);
        if (oldValue != b && this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleState", oldValue ? AccessibleState.BUSY : null, b ? AccessibleState.BUSY : null);
        }
    }

    public void setValues(int newValue, int newExtent, int newMin, int newMax) {
        BoundedRangeModel m = this.getModel();
        int oldValue = m.getValue();
        m.setRangeProperties(newValue, newExtent, newMin, newMax, m.getValueIsAdjusting());
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleValue", oldValue, m.getValue());
        }
    }

    @Override
    public void addAdjustmentListener(AdjustmentListener l) {
        this.listenerList.add(AdjustmentListener.class, l);
    }

    @Override
    public void removeAdjustmentListener(AdjustmentListener l) {
        this.listenerList.remove(AdjustmentListener.class, l);
    }

    @BeanProperty(bound=false)
    public AdjustmentListener[] getAdjustmentListeners() {
        return (AdjustmentListener[])this.listenerList.getListeners(AdjustmentListener.class);
    }

    protected void fireAdjustmentValueChanged(int id, int type, int value) {
        this.fireAdjustmentValueChanged(id, type, value, this.getValueIsAdjusting());
    }

    private void fireAdjustmentValueChanged(int id, int type, int value, boolean isAdjusting) {
        Object[] listeners = this.listenerList.getListenerList();
        AdjustmentEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != AdjustmentListener.class) continue;
            if (e == null) {
                e = new AdjustmentEvent(this, id, type, value, isAdjusting);
            }
            ((AdjustmentListener)listeners[i + 1]).adjustmentValueChanged(e);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension pref = this.getPreferredSize();
        if (this.orientation == 1) {
            return new Dimension(pref.width, 5);
        }
        return new Dimension(5, pref.height);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension pref = this.getPreferredSize();
        if (this.getOrientation() == 1) {
            return new Dimension(pref.width, Short.MAX_VALUE);
        }
        return new Dimension(Short.MAX_VALUE, pref.height);
    }

    @Override
    public void setEnabled(boolean x) {
        Component[] children;
        super.setEnabled(x);
        for (Component child : children = this.getComponents()) {
            child.setEnabled(x);
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
        String orientationString = this.orientation == 0 ? "HORIZONTAL" : "VERTICAL";
        return super.paramString() + ",blockIncrement=" + this.blockIncrement + ",orientation=" + orientationString + ",unitIncrement=" + this.unitIncrement;
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJScrollBar();
        }
        return this.accessibleContext;
    }

    private class ModelListener
    implements ChangeListener,
    Serializable {
        private ModelListener() {
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            Object obj = e.getSource();
            if (obj instanceof BoundedRangeModel) {
                int id = 601;
                int type = 5;
                BoundedRangeModel model = (BoundedRangeModel)obj;
                int value = model.getValue();
                boolean isAdjusting = model.getValueIsAdjusting();
                JScrollBar.this.fireAdjustmentValueChanged(id, type, value, isAdjusting);
            }
        }
    }

    protected class AccessibleJScrollBar
    extends JComponent.AccessibleJComponent
    implements AccessibleValue {
        protected AccessibleJScrollBar() {
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (JScrollBar.this.getValueIsAdjusting()) {
                states.add(AccessibleState.BUSY);
            }
            if (JScrollBar.this.getOrientation() == 1) {
                states.add(AccessibleState.VERTICAL);
            } else {
                states.add(AccessibleState.HORIZONTAL);
            }
            return states;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SCROLL_BAR;
        }

        @Override
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        @Override
        public Number getCurrentAccessibleValue() {
            return JScrollBar.this.getValue();
        }

        @Override
        public boolean setCurrentAccessibleValue(Number n) {
            if (n == null) {
                return false;
            }
            JScrollBar.this.setValue(n.intValue());
            return true;
        }

        @Override
        public Number getMinimumAccessibleValue() {
            return JScrollBar.this.getMinimum();
        }

        @Override
        public Number getMaximumAccessibleValue() {
            return JScrollBar.this.model.getMaximum() - JScrollBar.this.model.getExtent();
        }
    }
}

