/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.LayerUI;

public final class JLayer<V extends Component>
extends JComponent
implements Scrollable,
PropertyChangeListener,
Accessible {
    private V view;
    private LayerUI<? super V> layerUI;
    private JPanel glassPane;
    private long eventMask;
    private transient boolean isPaintCalling;
    private transient boolean isPaintImmediatelyCalling;
    private transient boolean isImageUpdateCalling;
    private static final LayerEventController eventController = new LayerEventController();

    public JLayer() {
        this(null);
    }

    public JLayer(V view) {
        this(view, new LayerUI());
    }

    public JLayer(V view, LayerUI<V> ui) {
        this.setGlassPane(this.createGlassPane());
        this.setView(view);
        this.setUI(ui);
    }

    public V getView() {
        return this.view;
    }

    public void setView(V view) {
        V oldView = this.getView();
        if (oldView != null) {
            super.remove((Component)oldView);
        }
        if (view != null) {
            super.addImpl((Component)view, null, this.getComponentCount());
        }
        this.view = view;
        this.firePropertyChange("view", oldView, view);
        this.revalidate();
        this.repaint();
    }

    public void setUI(LayerUI<? super V> ui) {
        this.layerUI = ui;
        super.setUI(ui);
    }

    @Override
    public LayerUI<? super V> getUI() {
        return this.layerUI;
    }

    public JPanel getGlassPane() {
        return this.glassPane;
    }

    public void setGlassPane(JPanel glassPane) {
        JPanel oldGlassPane = this.getGlassPane();
        boolean isGlassPaneVisible = false;
        if (oldGlassPane != null) {
            isGlassPaneVisible = oldGlassPane.isVisible();
            super.remove(oldGlassPane);
        }
        if (glassPane != null) {
            glassPane.setMixingCutoutShape(new Rectangle());
            glassPane.setVisible(isGlassPaneVisible);
            super.addImpl(glassPane, null, 0);
        }
        this.glassPane = glassPane;
        this.firePropertyChange("glassPane", oldGlassPane, glassPane);
        this.revalidate();
        this.repaint();
    }

    public JPanel createGlassPane() {
        return new DefaultLayerGlassPane();
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        if (mgr != null) {
            throw new IllegalArgumentException("JLayer.setLayout() not supported");
        }
    }

    @Override
    public void setBorder(Border border) {
        if (this.view instanceof JComponent) {
            ((JComponent)this.view).setBorder(border);
        }
    }

    @Override
    public Border getBorder() {
        if (this.view instanceof JComponent) {
            return ((JComponent)this.view).getBorder();
        }
        return null;
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        throw new UnsupportedOperationException("Adding components to JLayer is not supported, use setView() or setGlassPane() instead");
    }

    @Override
    public void remove(Component comp) {
        if (comp == null) {
            super.remove(comp);
        } else if (comp == this.getView()) {
            this.setView(null);
        } else if (comp == this.getGlassPane()) {
            this.setGlassPane(null);
        } else {
            super.remove(comp);
        }
    }

    @Override
    public void removeAll() {
        if (this.view != null) {
            this.setView(null);
        }
        if (this.glassPane != null) {
            this.setGlassPane(null);
        }
    }

    @Override
    protected boolean isPaintingOrigin() {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void paintImmediately(int x, int y, int w, int h) {
        if (!this.isPaintImmediatelyCalling && this.getUI() != null) {
            this.isPaintImmediatelyCalling = true;
            try {
                ((LayerUI)this.getUI()).paintImmediately(x, y, w, h, this);
            }
            finally {
                this.isPaintImmediatelyCalling = false;
            }
        } else {
            super.paintImmediately(x, y, w, h);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        if (!this.isImageUpdateCalling && this.getUI() != null) {
            this.isImageUpdateCalling = true;
            try {
                boolean bl = ((LayerUI)this.getUI()).imageUpdate(img, infoflags, x, y, w, h, this);
                return bl;
            }
            finally {
                this.isImageUpdateCalling = false;
            }
        }
        return super.imageUpdate(img, infoflags, x, y, w, h);
    }

    @Override
    public void paint(Graphics g) {
        if (!this.isPaintCalling) {
            this.isPaintCalling = true;
            try {
                super.paintComponent(g);
            }
            finally {
                this.isPaintCalling = false;
            }
        } else {
            super.paint(g);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (this.getUI() != null) {
            ((LayerUI)this.getUI()).applyPropertyChange(evt, this);
        }
    }

    public void setLayerEventMask(long layerEventMask) {
        long oldEventMask = this.getLayerEventMask();
        this.eventMask = layerEventMask;
        this.firePropertyChange("layerEventMask", oldEventMask, layerEventMask);
        if (layerEventMask != oldEventMask) {
            this.disableEvents(oldEventMask);
            this.enableEvents(this.eventMask);
            if (this.isDisplayable()) {
                eventController.updateAWTEventListener(oldEventMask, layerEventMask);
            }
        }
    }

    public long getLayerEventMask() {
        return this.eventMask;
    }

    @Override
    public void updateUI() {
        if (this.getUI() != null) {
            ((LayerUI)this.getUI()).updateUI(this);
        }
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        if (this.getView() instanceof Scrollable) {
            return ((Scrollable)this.getView()).getPreferredScrollableViewportSize();
        }
        return this.getPreferredSize();
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (this.getView() instanceof Scrollable) {
            return ((Scrollable)this.getView()).getScrollableBlockIncrement(visibleRect, orientation, direction);
        }
        return orientation == 1 ? visibleRect.height : visibleRect.width;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        if (this.getView() instanceof Scrollable) {
            return ((Scrollable)this.getView()).getScrollableTracksViewportHeight();
        }
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        if (this.getView() instanceof Scrollable) {
            return ((Scrollable)this.getView()).getScrollableTracksViewportWidth();
        }
        return false;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (this.getView() instanceof Scrollable) {
            return ((Scrollable)this.getView()).getScrollableUnitIncrement(visibleRect, orientation, direction);
        }
        return 1;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        LayerUI newLayerUI;
        ObjectInputStream.GetField f = s.readFields();
        this.view = (Component)f.get("view", null);
        this.glassPane = (JPanel)f.get("glassPane", null);
        this.eventMask = f.get("eventMask", 0L);
        if (this.eventMask != 0L) {
            eventController.updateAWTEventListener(0L, this.eventMask);
        }
        if ((newLayerUI = (LayerUI)f.get("layerUI", null)) != null) {
            this.setUI(newLayerUI);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        eventController.updateAWTEventListener(0L, this.eventMask);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        eventController.updateAWTEventListener(this.eventMask, 0L);
    }

    @Override
    public void doLayout() {
        if (this.getUI() != null) {
            ((LayerUI)this.getUI()).doLayout(this);
        }
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new JComponent.AccessibleJComponent(){

                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PANEL;
                }
            };
        }
        return this.accessibleContext;
    }

    private static class DefaultLayerGlassPane
    extends JPanel {
        public DefaultLayerGlassPane() {
            this.setOpaque(false);
        }

        @Override
        public boolean contains(int x, int y) {
            for (int i = 0; i < this.getComponentCount(); ++i) {
                Component c = this.getComponent(i);
                Point point = SwingUtilities.convertPoint(this, new Point(x, y), c);
                if (!c.isVisible() || !c.contains(point)) continue;
                return true;
            }
            if (this.getMouseListeners().length == 0 && this.getMouseMotionListeners().length == 0 && this.getMouseWheelListeners().length == 0 && !this.isCursorSet()) {
                return false;
            }
            return super.contains(x, y);
        }
    }

    private static class LayerEventController
    implements AWTEventListener {
        private ArrayList<Long> layerMaskList = new ArrayList();
        private long currentEventMask;
        private static final long ACCEPTED_EVENTS = 231487L;

        private LayerEventController() {
        }

        @Override
        public void eventDispatched(AWTEvent event) {
            Object source = event.getSource();
            if (source instanceof Component) {
                for (Component component = (Component)source; component != null; component = component.getParent()) {
                    JLayer l;
                    ComponentUI ui;
                    if (!(component instanceof JLayer) || (ui = (l = (JLayer)component).getUI()) == null || !this.isEventEnabled(l.getLayerEventMask(), event.getID()) || event instanceof InputEvent && ((InputEvent)event).isConsumed()) continue;
                    ((LayerUI)ui).eventDispatched(event, l);
                }
            }
        }

        private void updateAWTEventListener(long oldEventMask, long newEventMask) {
            if (oldEventMask != 0L) {
                this.layerMaskList.remove(oldEventMask);
            }
            if (newEventMask != 0L) {
                this.layerMaskList.add(newEventMask);
            }
            long combinedMask = 0L;
            for (Long mask : this.layerMaskList) {
                combinedMask |= mask.longValue();
            }
            if ((combinedMask &= 0x3883FL) == 0L) {
                this.removeAWTEventListener();
            } else if (this.getCurrentEventMask() != combinedMask) {
                this.removeAWTEventListener();
                this.addAWTEventListener(combinedMask);
            }
            this.currentEventMask = combinedMask;
        }

        private long getCurrentEventMask() {
            return this.currentEventMask;
        }

        private void addAWTEventListener(final long eventMask) {
            AccessController.doPrivileged(new PrivilegedAction<Void>(){
                final /* synthetic */ LayerEventController this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public Void run() {
                    Toolkit.getDefaultToolkit().addAWTEventListener(this.this$0, eventMask);
                    return null;
                }
            });
        }

        private void removeAWTEventListener() {
            AccessController.doPrivileged(new PrivilegedAction<Void>(){

                @Override
                public Void run() {
                    Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                    return null;
                }
            });
        }

        private boolean isEventEnabled(long eventMask, int id) {
            return (eventMask & 1L) != 0L && id >= 100 && id <= 103 || (eventMask & 2L) != 0L && id >= 300 && id <= 301 || (eventMask & 4L) != 0L && id >= 1004 && id <= 1005 || (eventMask & 8L) != 0L && id >= 400 && id <= 402 || (eventMask & 0x20000L) != 0L && id == 507 || (eventMask & 0x20L) != 0L && (id == 503 || id == 506) || (eventMask & 0x10L) != 0L && id != 503 && id != 506 && id != 507 && id >= 500 && id <= 507 || (eventMask & 0x800L) != 0L && id >= 1100 && id <= 1101 || (eventMask & 0x8000L) != 0L && id == 1400 || (eventMask & 0x10000L) != 0L && (id == 1401 || id == 1402);
        }
    }
}

