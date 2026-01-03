/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.Adjustable;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.peer.ScrollbarPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EventListener;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleValue;

public class Scrollbar
extends Component
implements Adjustable,
Accessible {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    int value;
    int maximum;
    int minimum;
    int visibleAmount;
    int orientation;
    int lineIncrement = 1;
    int pageIncrement = 10;
    transient boolean isAdjusting;
    transient AdjustmentListener adjustmentListener;
    private static final String base = "scrollbar";
    private static int nameCounter = 0;
    private static final long serialVersionUID = 8451667562882310543L;
    private int scrollbarSerializedDataVersion = 1;

    private static native void initIDs();

    public Scrollbar() throws HeadlessException {
        this(1, 0, 10, 0, 100);
    }

    public Scrollbar(int orientation) throws HeadlessException {
        this(orientation, 0, 10, 0, 100);
    }

    public Scrollbar(int orientation, int value, int visible, int minimum, int maximum) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        switch (orientation) {
            case 0: 
            case 1: {
                this.orientation = orientation;
                break;
            }
            default: {
                throw new IllegalArgumentException("illegal scrollbar orientation");
            }
        }
        this.setValues(value, visible, minimum, maximum);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    String constructComponentName() {
        Class<Scrollbar> clazz = Scrollbar.class;
        synchronized (Scrollbar.class) {
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return base + nameCounter++;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addNotify() {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.peer == null) {
                this.peer = this.getComponentFactory().createScrollbar(this);
            }
            super.addNotify();
        }
    }

    @Override
    public int getOrientation() {
        return this.orientation;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setOrientation(int orientation) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (orientation == this.orientation) {
                return;
            }
            switch (orientation) {
                case 0: 
                case 1: {
                    this.orientation = orientation;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("illegal scrollbar orientation");
                }
            }
            if (this.peer != null) {
                this.removeNotify();
                this.addNotify();
                this.invalidate();
            }
        }
        if (this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleState", orientation == 1 ? AccessibleState.HORIZONTAL : AccessibleState.VERTICAL, orientation == 1 ? AccessibleState.VERTICAL : AccessibleState.HORIZONTAL);
        }
    }

    @Override
    public int getValue() {
        return this.value;
    }

    @Override
    public void setValue(int newValue) {
        this.setValues(newValue, this.visibleAmount, this.minimum, this.maximum);
    }

    @Override
    public int getMinimum() {
        return this.minimum;
    }

    @Override
    public void setMinimum(int newMinimum) {
        this.setValues(this.value, this.visibleAmount, newMinimum, this.maximum);
    }

    @Override
    public int getMaximum() {
        return this.maximum;
    }

    @Override
    public void setMaximum(int newMaximum) {
        if (newMaximum == Integer.MIN_VALUE) {
            newMaximum = -2147483647;
        }
        if (this.minimum >= newMaximum) {
            this.minimum = newMaximum - 1;
        }
        this.setValues(this.value, this.visibleAmount, this.minimum, newMaximum);
    }

    @Override
    public int getVisibleAmount() {
        return this.getVisible();
    }

    @Deprecated
    public int getVisible() {
        return this.visibleAmount;
    }

    @Override
    public void setVisibleAmount(int newAmount) {
        this.setValues(this.value, newAmount, this.minimum, this.maximum);
    }

    @Override
    public void setUnitIncrement(int v) {
        this.setLineIncrement(v);
    }

    @Deprecated
    public synchronized void setLineIncrement(int v) {
        int tmp;
        int n = tmp = v < 1 ? 1 : v;
        if (this.lineIncrement == tmp) {
            return;
        }
        this.lineIncrement = tmp;
        ScrollbarPeer peer = (ScrollbarPeer)this.peer;
        if (peer != null) {
            peer.setLineIncrement(this.lineIncrement);
        }
    }

    @Override
    public int getUnitIncrement() {
        return this.getLineIncrement();
    }

    @Deprecated
    public int getLineIncrement() {
        return this.lineIncrement;
    }

    @Override
    public void setBlockIncrement(int v) {
        this.setPageIncrement(v);
    }

    @Deprecated
    public synchronized void setPageIncrement(int v) {
        int tmp;
        int n = tmp = v < 1 ? 1 : v;
        if (this.pageIncrement == tmp) {
            return;
        }
        this.pageIncrement = tmp;
        ScrollbarPeer peer = (ScrollbarPeer)this.peer;
        if (peer != null) {
            peer.setPageIncrement(this.pageIncrement);
        }
    }

    @Override
    public int getBlockIncrement() {
        return this.getPageIncrement();
    }

    @Deprecated
    public int getPageIncrement() {
        return this.pageIncrement;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setValues(int value, int visible, int minimum, int maximum) {
        int oldValue;
        Scrollbar scrollbar = this;
        synchronized (scrollbar) {
            long maxMinusMin;
            if (minimum == Integer.MAX_VALUE) {
                minimum = 0x7FFFFFFE;
            }
            if (maximum <= minimum) {
                maximum = minimum + 1;
            }
            if ((maxMinusMin = (long)maximum - (long)minimum) > Integer.MAX_VALUE) {
                maxMinusMin = Integer.MAX_VALUE;
                maximum = minimum + (int)maxMinusMin;
            }
            if (visible > (int)maxMinusMin) {
                visible = (int)maxMinusMin;
            }
            if (visible < 1) {
                visible = 1;
            }
            if (value < minimum) {
                value = minimum;
            }
            if (value > maximum - visible) {
                value = maximum - visible;
            }
            oldValue = this.value;
            this.value = value;
            this.visibleAmount = visible;
            this.minimum = minimum;
            this.maximum = maximum;
            ScrollbarPeer peer = (ScrollbarPeer)this.peer;
            if (peer != null) {
                peer.setValues(value, this.visibleAmount, minimum, maximum);
            }
        }
        if (oldValue != value && this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleValue", oldValue, value);
        }
    }

    public boolean getValueIsAdjusting() {
        return this.isAdjusting;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setValueIsAdjusting(boolean b) {
        boolean oldValue;
        Scrollbar scrollbar = this;
        synchronized (scrollbar) {
            oldValue = this.isAdjusting;
            this.isAdjusting = b;
        }
        if (oldValue != b && this.accessibleContext != null) {
            this.accessibleContext.firePropertyChange("AccessibleState", oldValue ? AccessibleState.BUSY : null, b ? AccessibleState.BUSY : null);
        }
    }

    @Override
    public synchronized void addAdjustmentListener(AdjustmentListener l) {
        if (l == null) {
            return;
        }
        this.adjustmentListener = AWTEventMulticaster.add(this.adjustmentListener, l);
        this.newEventsOnly = true;
    }

    @Override
    public synchronized void removeAdjustmentListener(AdjustmentListener l) {
        if (l == null) {
            return;
        }
        this.adjustmentListener = AWTEventMulticaster.remove(this.adjustmentListener, l);
    }

    public synchronized AdjustmentListener[] getAdjustmentListeners() {
        return (AdjustmentListener[])this.getListeners(AdjustmentListener.class);
    }

    @Override
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        AdjustmentListener l = null;
        if (listenerType != AdjustmentListener.class) {
            return super.getListeners(listenerType);
        }
        l = this.adjustmentListener;
        return AWTEventMulticaster.getListeners((EventListener)l, listenerType);
    }

    @Override
    boolean eventEnabled(AWTEvent e) {
        if (e.id == 601) {
            return (this.eventMask & 0x100L) != 0L || this.adjustmentListener != null;
        }
        return super.eventEnabled(e);
    }

    @Override
    protected void processEvent(AWTEvent e) {
        if (e instanceof AdjustmentEvent) {
            this.processAdjustmentEvent((AdjustmentEvent)e);
            return;
        }
        super.processEvent(e);
    }

    protected void processAdjustmentEvent(AdjustmentEvent e) {
        AdjustmentListener listener = this.adjustmentListener;
        if (listener != null) {
            listener.adjustmentValueChanged(e);
        }
    }

    @Override
    protected String paramString() {
        return super.paramString() + ",val=" + this.value + ",vis=" + this.visibleAmount + ",min=" + this.minimum + ",max=" + this.maximum + (this.orientation == 1 ? ",vert" : ",horz") + ",isAdjusting=" + this.isAdjusting;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        AWTEventMulticaster.save(s, "adjustmentL", this.adjustmentListener);
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException, HeadlessException {
        Object keyOrNull;
        GraphicsEnvironment.checkHeadless();
        s.defaultReadObject();
        while (null != (keyOrNull = s.readObject())) {
            String key = ((String)keyOrNull).intern();
            if ("adjustmentL" == key) {
                this.addAdjustmentListener((AdjustmentListener)s.readObject());
                continue;
            }
            s.readObject();
        }
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleAWTScrollBar();
        }
        return this.accessibleContext;
    }

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            Scrollbar.initIDs();
        }
    }

    protected class AccessibleAWTScrollBar
    extends Component.AccessibleAWTComponent
    implements AccessibleValue {
        private static final long serialVersionUID = -344337268523697807L;

        protected AccessibleAWTScrollBar() {
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (Scrollbar.this.getValueIsAdjusting()) {
                states.add(AccessibleState.BUSY);
            }
            if (Scrollbar.this.getOrientation() == 1) {
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
            return Scrollbar.this.getValue();
        }

        @Override
        public boolean setCurrentAccessibleValue(Number n) {
            if (n instanceof Integer) {
                Scrollbar.this.setValue(n.intValue());
                return true;
            }
            return false;
        }

        @Override
        public Number getMinimumAccessibleValue() {
            return Scrollbar.this.getMinimum();
        }

        @Override
        public Number getMaximumAccessibleValue() {
            return Scrollbar.this.getMaximum();
        }
    }
}

