/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleValue;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.UIResource;

@JavaBean(defaultProperty="UI", description="A component that supports selecting a integer value from a range.")
@SwingContainer(value=false)
public class JSlider
extends JComponent
implements SwingConstants,
Accessible {
    private static final String uiClassID = "SliderUI";
    private boolean paintTicks = false;
    private boolean paintTrack = true;
    private boolean paintLabels = false;
    private boolean isInverted = false;
    protected BoundedRangeModel sliderModel;
    protected int majorTickSpacing;
    protected int minorTickSpacing;
    protected boolean snapToTicks = false;
    boolean snapToValue = true;
    protected int orientation;
    private Dictionary labelTable;
    protected ChangeListener changeListener = this.createChangeListener();
    protected transient ChangeEvent changeEvent = null;

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

    public JSlider() {
        this(0, 0, 100, 50);
    }

    public JSlider(int orientation) {
        this(orientation, 0, 100, 50);
    }

    public JSlider(int min, int max) {
        this(0, min, max, (min + max) / 2);
    }

    public JSlider(int min, int max, int value) {
        this(0, min, max, value);
    }

    public JSlider(int orientation, int min, int max, int value) {
        this.checkOrientation(orientation);
        this.orientation = orientation;
        this.setModel(new DefaultBoundedRangeModel(value, 0, min, max));
        this.updateUI();
    }

    public JSlider(BoundedRangeModel brm) {
        this.orientation = 0;
        this.setModel(brm);
        this.updateUI();
    }

    @Override
    public SliderUI getUI() {
        return (SliderUI)this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the slider's LookAndFeel.")
    public void setUI(SliderUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        this.setUI((SliderUI)UIManager.getUI(this));
        this.updateLabelUIs();
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
        return this.sliderModel;
    }

    @BeanProperty(description="The sliders BoundedRangeModel.")
    public void setModel(BoundedRangeModel newModel) {
        BoundedRangeModel oldModel = this.getModel();
        if (oldModel != null) {
            oldModel.removeChangeListener(this.changeListener);
        }
        this.sliderModel = newModel;
        if (newModel != null) {
            newModel.addChangeListener(this.changeListener);
        }
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleValue", oldModel == null ? null : Integer.valueOf(oldModel.getValue()), newModel == null ? null : Integer.valueOf(newModel.getValue()));
        }
        this.firePropertyChange("model", oldModel, this.sliderModel);
    }

    public int getValue() {
        return this.getModel().getValue();
    }

    @BeanProperty(bound=false, preferred=true, description="The sliders current value.")
    public void setValue(int n) {
        BoundedRangeModel m = this.getModel();
        int oldValue = m.getValue();
        if (oldValue == n) {
            return;
        }
        m.setValue(n);
    }

    public int getMinimum() {
        return this.getModel().getMinimum();
    }

    @BeanProperty(preferred=true, description="The sliders minimum value.")
    public void setMinimum(int minimum) {
        int oldMin = this.getModel().getMinimum();
        this.getModel().setMinimum(minimum);
        this.firePropertyChange("minimum", (Object)oldMin, (Object)minimum);
    }

    public int getMaximum() {
        return this.getModel().getMaximum();
    }

    @BeanProperty(preferred=true, description="The sliders maximum value.")
    public void setMaximum(int maximum) {
        int oldMax = this.getModel().getMaximum();
        this.getModel().setMaximum(maximum);
        this.firePropertyChange("maximum", (Object)oldMax, (Object)maximum);
    }

    public boolean getValueIsAdjusting() {
        return this.getModel().getValueIsAdjusting();
    }

    @BeanProperty(bound=false, expert=true, description="True if the slider knob is being dragged.")
    public void setValueIsAdjusting(boolean b) {
        BoundedRangeModel m = this.getModel();
        boolean oldValue = m.getValueIsAdjusting();
        m.setValueIsAdjusting(b);
        if (oldValue != b && this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleState", oldValue ? AccessibleState.BUSY : null, b ? AccessibleState.BUSY : null);
        }
    }

    public int getExtent() {
        return this.getModel().getExtent();
    }

    @BeanProperty(bound=false, expert=true, description="Size of the range covered by the knob.")
    public void setExtent(int extent) {
        this.getModel().setExtent(extent);
    }

    public int getOrientation() {
        return this.orientation;
    }

    @BeanProperty(preferred=true, visualUpdate=true, enumerationValues={"JSlider.VERTICAL", "JSlider.HORIZONTAL"}, description="Set the scrollbars orientation to either VERTICAL or HORIZONTAL.")
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

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        this.updateLabelSizes();
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        if (!this.isShowing()) {
            return false;
        }
        Enumeration elements = this.labelTable.elements();
        while (elements.hasMoreElements()) {
            JLabel label;
            Component component = (Component)elements.nextElement();
            if (!(component instanceof JLabel) || !SwingUtilities.doesIconReferenceImage((label = (JLabel)component).getIcon(), img) && !SwingUtilities.doesIconReferenceImage(label.getDisabledIcon(), img)) continue;
            return super.imageUpdate(img, infoflags, x, y, w, h);
        }
        return false;
    }

    public Dictionary getLabelTable() {
        return this.labelTable;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="Specifies what labels will be drawn for any given value.")
    public void setLabelTable(Dictionary labels) {
        Dictionary oldTable = this.labelTable;
        this.labelTable = labels;
        this.updateLabelUIs();
        this.firePropertyChange("labelTable", oldTable, this.labelTable);
        if (labels != oldTable) {
            this.revalidate();
            this.repaint();
        }
    }

    protected void updateLabelUIs() {
        Dictionary labelTable = this.getLabelTable();
        if (labelTable == null) {
            return;
        }
        Enumeration labels = labelTable.keys();
        while (labels.hasMoreElements()) {
            JComponent component = (JComponent)labelTable.get(labels.nextElement());
            component.updateUI();
            component.setSize(component.getPreferredSize());
        }
    }

    private void updateLabelSizes() {
        Dictionary labelTable = this.getLabelTable();
        if (labelTable != null) {
            Enumeration labels = labelTable.elements();
            while (labels.hasMoreElements()) {
                JComponent component = (JComponent)labels.nextElement();
                component.setSize(component.getPreferredSize());
            }
        }
    }

    public Hashtable<Integer, JComponent> createStandardLabels(int increment) {
        return this.createStandardLabels(increment, this.getMinimum());
    }

    public Hashtable<Integer, JComponent> createStandardLabels(int increment, int start) {
        if (start > this.getMaximum() || start < this.getMinimum()) {
            throw new IllegalArgumentException("Slider label start point out of range.");
        }
        if (increment <= 0) {
            throw new IllegalArgumentException("Label incremement must be > 0");
        }
        class SmartHashtable
        extends Hashtable<Integer, JComponent>
        implements PropertyChangeListener {
            int increment = 0;
            int start = 0;
            boolean startAtMin = false;

            public SmartHashtable(int increment, int start) {
                this.increment = increment;
                this.start = start;
                this.startAtMin = start == JSlider.this.getMinimum();
                this.createLabels();
            }

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals("minimum") && this.startAtMin) {
                    this.start = JSlider.this.getMinimum();
                }
                if (e.getPropertyName().equals("minimum") || e.getPropertyName().equals("maximum")) {
                    Integer key;
                    Enumeration keys = JSlider.this.getLabelTable().keys();
                    Hashtable<Integer, JComponent> hashtable = new Hashtable<Integer, JComponent>();
                    while (keys.hasMoreElements()) {
                        key = (Integer)keys.nextElement();
                        JComponent value = (JComponent)JSlider.this.labelTable.get(key);
                        if (value instanceof 1SmartHashtable.LabelUIResource) continue;
                        hashtable.put(key, value);
                    }
                    this.clear();
                    this.createLabels();
                    keys = hashtable.keys();
                    while (keys.hasMoreElements()) {
                        key = (Integer)keys.nextElement();
                        this.put(key, (JComponent)hashtable.get(key));
                    }
                    ((JSlider)e.getSource()).setLabelTable(this);
                }
            }

            void createLabels() {
                for (int labelIndex = this.start; labelIndex <= JSlider.this.getMaximum(); labelIndex += this.increment) {
                    this.put(labelIndex, new 1SmartHashtable.LabelUIResource("" + labelIndex, 0));
                }
            }

            class 1SmartHashtable.LabelUIResource
            extends JLabel
            implements UIResource {
                public 1SmartHashtable.LabelUIResource(String text, int alignment) {
                    super(text, alignment);
                    this.setName("Slider.label");
                }

                @Override
                public Font getFont() {
                    Font font = super.getFont();
                    if (font != null && !(font instanceof UIResource)) {
                        return font;
                    }
                    return JSlider.this.getFont();
                }

                @Override
                public Color getForeground() {
                    Color fg = super.getForeground();
                    if (fg != null && !(fg instanceof UIResource)) {
                        return fg;
                    }
                    if (!(JSlider.this.getForeground() instanceof UIResource)) {
                        return JSlider.this.getForeground();
                    }
                    return fg;
                }
            }
        }
        SmartHashtable table = new SmartHashtable(increment, start);
        Dictionary labelTable = this.getLabelTable();
        if (labelTable instanceof PropertyChangeListener) {
            PropertyChangeListener listener = (PropertyChangeListener)((Object)labelTable);
            this.removePropertyChangeListener(listener);
        }
        this.addPropertyChangeListener(table);
        return table;
    }

    public boolean getInverted() {
        return this.isInverted;
    }

    @BeanProperty(visualUpdate=true, description="If true reverses the slider values from their normal order")
    public void setInverted(boolean b) {
        boolean oldValue = this.isInverted;
        this.isInverted = b;
        this.firePropertyChange("inverted", oldValue, this.isInverted);
        if (b != oldValue) {
            this.repaint();
        }
    }

    public int getMajorTickSpacing() {
        return this.majorTickSpacing;
    }

    @BeanProperty(visualUpdate=true, description="Sets the number of values between major tick marks.")
    public void setMajorTickSpacing(int n) {
        int oldValue = this.majorTickSpacing;
        this.majorTickSpacing = n;
        if (this.labelTable == null && this.getMajorTickSpacing() > 0 && this.getPaintLabels()) {
            this.setLabelTable(this.createStandardLabels(this.getMajorTickSpacing()));
        }
        this.firePropertyChange("majorTickSpacing", oldValue, this.majorTickSpacing);
        if (this.majorTickSpacing != oldValue && this.getPaintTicks()) {
            this.repaint();
        }
    }

    public int getMinorTickSpacing() {
        return this.minorTickSpacing;
    }

    @BeanProperty(visualUpdate=true, description="Sets the number of values between minor tick marks.")
    public void setMinorTickSpacing(int n) {
        int oldValue = this.minorTickSpacing;
        this.minorTickSpacing = n;
        this.firePropertyChange("minorTickSpacing", oldValue, this.minorTickSpacing);
        if (this.minorTickSpacing != oldValue && this.getPaintTicks()) {
            this.repaint();
        }
    }

    public boolean getSnapToTicks() {
        return this.snapToTicks;
    }

    boolean getSnapToValue() {
        return this.snapToValue;
    }

    @BeanProperty(description="If true snap the knob to the nearest tick mark.")
    public void setSnapToTicks(boolean b) {
        boolean oldValue = this.snapToTicks;
        this.snapToTicks = b;
        this.firePropertyChange("snapToTicks", oldValue, this.snapToTicks);
    }

    @BeanProperty(description="If true snap the knob to the nearest slider value.")
    void setSnapToValue(boolean b) {
        boolean oldValue = this.snapToValue;
        this.snapToValue = b;
        this.firePropertyChange("snapToValue", oldValue, this.snapToValue);
    }

    public boolean getPaintTicks() {
        return this.paintTicks;
    }

    @BeanProperty(visualUpdate=true, description="If true tick marks are painted on the slider.")
    public void setPaintTicks(boolean b) {
        boolean oldValue = this.paintTicks;
        this.paintTicks = b;
        this.firePropertyChange("paintTicks", oldValue, this.paintTicks);
        if (this.paintTicks != oldValue) {
            this.revalidate();
            this.repaint();
        }
    }

    public boolean getPaintTrack() {
        return this.paintTrack;
    }

    @BeanProperty(visualUpdate=true, description="If true, the track is painted on the slider.")
    public void setPaintTrack(boolean b) {
        boolean oldValue = this.paintTrack;
        this.paintTrack = b;
        this.firePropertyChange("paintTrack", oldValue, this.paintTrack);
        if (this.paintTrack != oldValue) {
            this.repaint();
        }
    }

    public boolean getPaintLabels() {
        return this.paintLabels;
    }

    @BeanProperty(visualUpdate=true, description="If true labels are painted on the slider.")
    public void setPaintLabels(boolean b) {
        boolean oldValue = this.paintLabels;
        this.paintLabels = b;
        if (this.labelTable == null && this.getMajorTickSpacing() > 0) {
            this.setLabelTable(this.createStandardLabels(this.getMajorTickSpacing()));
        }
        this.firePropertyChange("paintLabels", oldValue, this.paintLabels);
        if (this.paintLabels != oldValue) {
            this.revalidate();
            this.repaint();
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
        String paintTicksString = this.paintTicks ? "true" : "false";
        String paintTrackString = this.paintTrack ? "true" : "false";
        String paintLabelsString = this.paintLabels ? "true" : "false";
        String isInvertedString = this.isInverted ? "true" : "false";
        String snapToTicksString = this.snapToTicks ? "true" : "false";
        String snapToValueString = this.snapToValue ? "true" : "false";
        String orientationString = this.orientation == 0 ? "HORIZONTAL" : "VERTICAL";
        return super.paramString() + ",isInverted=" + isInvertedString + ",majorTickSpacing=" + this.majorTickSpacing + ",minorTickSpacing=" + this.minorTickSpacing + ",orientation=" + orientationString + ",paintLabels=" + paintLabelsString + ",paintTicks=" + paintTicksString + ",paintTrack=" + paintTrackString + ",snapToTicks=" + snapToTicksString + ",snapToValue=" + snapToValueString;
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJSlider();
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
            JSlider.this.fireStateChanged();
        }
    }

    protected class AccessibleJSlider
    extends JComponent.AccessibleJComponent
    implements AccessibleValue,
    ChangeListener,
    AccessibleAction {
        private int oldModelValue;

        protected AccessibleJSlider() {
            this.oldModelValue = JSlider.this.getModel().getValue();
            JSlider.this.addChangeListener(this);
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (JSlider.this.getValueIsAdjusting()) {
                states.add(AccessibleState.BUSY);
            }
            if (JSlider.this.getOrientation() == 1) {
                states.add(AccessibleState.VERTICAL);
            } else {
                states.add(AccessibleState.HORIZONTAL);
            }
            return states;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e == null) {
                throw new NullPointerException();
            }
            int newModelValue = JSlider.this.getModel().getValue();
            this.firePropertyChange("AccessibleValue", this.oldModelValue, newModelValue);
            this.oldModelValue = newModelValue;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SLIDER;
        }

        @Override
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        @Override
        public Number getCurrentAccessibleValue() {
            return JSlider.this.getValue();
        }

        @Override
        public boolean setCurrentAccessibleValue(Number n) {
            if (n == null) {
                return false;
            }
            JSlider.this.setValue(n.intValue());
            return true;
        }

        @Override
        public Number getMinimumAccessibleValue() {
            return JSlider.this.getMinimum();
        }

        @Override
        public Number getMaximumAccessibleValue() {
            BoundedRangeModel model = JSlider.this.getModel();
            return model.getMaximum() - model.getExtent();
        }

        @Override
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        @Override
        public int getAccessibleActionCount() {
            return 2;
        }

        @Override
        public String getAccessibleActionDescription(int i) {
            if (i == 0) {
                return AccessibleAction.INCREMENT;
            }
            if (i == 1) {
                return AccessibleAction.DECREMENT;
            }
            return null;
        }

        @Override
        public boolean doAccessibleAction(int i) {
            if (i < 0 || i > 1) {
                return false;
            }
            int delta = i > 0 ? -1 : 1;
            JSlider.this.setValue(this.oldModelValue + delta);
            return true;
        }
    }
}

