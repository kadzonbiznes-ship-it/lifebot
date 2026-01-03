/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Graphics;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.Format;
import java.text.NumberFormat;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleValue;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingContainer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ProgressBarUI;

@JavaBean(defaultProperty="UI", description="A component that displays an integer value.")
@SwingContainer(value=false)
public class JProgressBar
extends JComponent
implements SwingConstants,
Accessible {
    private static final String uiClassID = "ProgressBarUI";
    protected int orientation;
    protected boolean paintBorder;
    protected BoundedRangeModel model;
    protected String progressString;
    protected boolean paintString;
    private static final int defaultMinimum = 0;
    private static final int defaultMaximum = 100;
    private static final int defaultOrientation = 0;
    protected transient ChangeEvent changeEvent = null;
    protected ChangeListener changeListener = null;
    private transient Format format;
    private boolean indeterminate;

    public JProgressBar() {
        this(0);
    }

    public JProgressBar(int orient) {
        this(orient, 0, 100);
    }

    public JProgressBar(int min, int max) {
        this(0, min, max);
    }

    public JProgressBar(int orient, int min, int max) {
        this.setModel(new DefaultBoundedRangeModel(min, 0, min, max));
        this.updateUI();
        this.setOrientation(orient);
        this.setBorderPainted(true);
        this.setStringPainted(false);
        this.setString(null);
        this.setIndeterminate(false);
    }

    public JProgressBar(BoundedRangeModel newModel) {
        this.setModel(newModel);
        this.updateUI();
        this.setOrientation(0);
        this.setBorderPainted(true);
        this.setStringPainted(false);
        this.setString(null);
        this.setIndeterminate(false);
    }

    public int getOrientation() {
        return this.orientation;
    }

    @BeanProperty(preferred=true, visualUpdate=true, description="Set the progress bar's orientation.")
    public void setOrientation(int newOrientation) {
        if (this.orientation != newOrientation) {
            switch (newOrientation) {
                case 0: 
                case 1: {
                    int oldOrientation = this.orientation;
                    this.orientation = newOrientation;
                    this.firePropertyChange("orientation", oldOrientation, newOrientation);
                    if (this.accessibleContext == null) break;
                    this.accessibleContext.firePropertyChange("AccessibleState", oldOrientation == 1 ? AccessibleState.VERTICAL : AccessibleState.HORIZONTAL, this.orientation == 1 ? AccessibleState.VERTICAL : AccessibleState.HORIZONTAL);
                    break;
                }
                default: {
                    throw new IllegalArgumentException(newOrientation + " is not a legal orientation");
                }
            }
            this.revalidate();
        }
    }

    public boolean isStringPainted() {
        return this.paintString;
    }

    @BeanProperty(visualUpdate=true, description="Whether the progress bar should render a string.")
    public void setStringPainted(boolean b) {
        boolean oldValue = this.paintString;
        this.paintString = b;
        this.firePropertyChange("stringPainted", oldValue, this.paintString);
        if (this.paintString != oldValue) {
            this.revalidate();
            this.repaint();
        }
    }

    public String getString() {
        if (this.progressString != null) {
            return this.progressString;
        }
        if (this.format == null) {
            this.format = NumberFormat.getPercentInstance();
        }
        return this.format.format(this.getPercentComplete());
    }

    @BeanProperty(visualUpdate=true, description="Specifies the progress string to paint")
    public void setString(String s) {
        String oldValue = this.progressString;
        this.progressString = s;
        this.firePropertyChange("string", oldValue, this.progressString);
        if (this.progressString == null || oldValue == null || !this.progressString.equals(oldValue)) {
            this.repaint();
        }
    }

    @BeanProperty(bound=false)
    public double getPercentComplete() {
        long span = this.model.getMaximum() - this.model.getMinimum();
        double currentValue = this.model.getValue();
        double pc = (currentValue - (double)this.model.getMinimum()) / (double)span;
        return pc;
    }

    public boolean isBorderPainted() {
        return this.paintBorder;
    }

    @BeanProperty(visualUpdate=true, description="Whether the progress bar should paint its border.")
    public void setBorderPainted(boolean b) {
        boolean oldValue = this.paintBorder;
        this.paintBorder = b;
        this.firePropertyChange("borderPainted", oldValue, this.paintBorder);
        if (this.paintBorder != oldValue) {
            this.repaint();
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        if (this.isBorderPainted()) {
            super.paintBorder(g);
        }
    }

    @Override
    public ProgressBarUI getUI() {
        return (ProgressBarUI)this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel.")
    public void setUI(ProgressBarUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        this.setUI((ProgressBarUI)UIManager.getUI(this));
    }

    @Override
    @BeanProperty(bound=false, expert=true, description="A string that specifies the name of the look-and-feel class.")
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
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ChangeListener.class) continue;
            if (this.changeEvent == null) {
                this.changeEvent = new ChangeEvent(this);
            }
            ((ChangeListener)listeners[i + 1]).stateChanged(this.changeEvent);
        }
    }

    public BoundedRangeModel getModel() {
        return this.model;
    }

    @BeanProperty(bound=false, expert=true, description="The data model used by the JProgressBar.")
    public void setModel(BoundedRangeModel newModel) {
        BoundedRangeModel oldModel = this.getModel();
        if (newModel != oldModel) {
            if (oldModel != null) {
                oldModel.removeChangeListener(this.changeListener);
                this.changeListener = null;
            }
            this.model = newModel;
            if (newModel != null) {
                this.changeListener = this.createChangeListener();
                newModel.addChangeListener(this.changeListener);
            }
            if (this.accessibleContext != null) {
                this.accessibleContext.firePropertyChange("AccessibleValue", oldModel == null ? null : Integer.valueOf(oldModel.getValue()), newModel == null ? null : Integer.valueOf(newModel.getValue()));
            }
            if (this.model != null) {
                this.model.setExtent(0);
            }
            this.repaint();
        }
    }

    public int getValue() {
        return this.getModel().getValue();
    }

    public int getMinimum() {
        return this.getModel().getMinimum();
    }

    public int getMaximum() {
        return this.getModel().getMaximum();
    }

    @BeanProperty(bound=false, preferred=true, description="The progress bar's current value.")
    public void setValue(int n) {
        BoundedRangeModel brm = this.getModel();
        int oldValue = brm.getValue();
        brm.setValue(n);
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleValue", oldValue, brm.getValue());
        }
    }

    @BeanProperty(bound=false, preferred=true, description="The progress bar's minimum value.")
    public void setMinimum(int n) {
        this.getModel().setMinimum(n);
    }

    @BeanProperty(bound=false, preferred=true, description="The progress bar's maximum value.")
    public void setMaximum(int n) {
        this.getModel().setMaximum(n);
    }

    public void setIndeterminate(boolean newValue) {
        boolean oldValue = this.indeterminate;
        this.indeterminate = newValue;
        this.firePropertyChange("indeterminate", oldValue, this.indeterminate);
    }

    @BeanProperty(bound=false, description="Is the progress bar indeterminate (true) or normal (false)?")
    public boolean isIndeterminate() {
        return this.indeterminate;
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
        String paintBorderString = this.paintBorder ? "true" : "false";
        String progressStringString = this.progressString != null ? this.progressString : "";
        String paintStringString = this.paintString ? "true" : "false";
        String indeterminateString = this.indeterminate ? "true" : "false";
        return super.paramString() + ",orientation=" + orientationString + ",paintBorder=" + paintBorderString + ",paintString=" + paintStringString + ",progressString=" + progressStringString + ",indeterminateString=" + indeterminateString;
    }

    @Override
    @BeanProperty(bound=false, expert=true, description="The AccessibleContext associated with this ProgressBar.")
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJProgressBar();
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
            JProgressBar.this.fireStateChanged();
        }
    }

    protected class AccessibleJProgressBar
    extends JComponent.AccessibleJComponent
    implements AccessibleValue {
        protected AccessibleJProgressBar() {
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (JProgressBar.this.getModel().getValueIsAdjusting()) {
                states.add(AccessibleState.BUSY);
            }
            if (JProgressBar.this.getOrientation() == 1) {
                states.add(AccessibleState.VERTICAL);
            } else {
                states.add(AccessibleState.HORIZONTAL);
            }
            return states;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PROGRESS_BAR;
        }

        @Override
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        @Override
        public Number getCurrentAccessibleValue() {
            return JProgressBar.this.getValue();
        }

        @Override
        public boolean setCurrentAccessibleValue(Number n) {
            if (n == null) {
                return false;
            }
            JProgressBar.this.setValue(n.intValue());
            return true;
        }

        @Override
        public Number getMinimumAccessibleValue() {
            return JProgressBar.this.getMinimum();
        }

        @Override
        public Number getMaximumAccessibleValue() {
            return JProgressBar.this.model.getMaximum() - JProgressBar.this.model.getExtent();
        }
    }
}

