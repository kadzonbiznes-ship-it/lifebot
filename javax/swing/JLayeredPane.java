/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import sun.awt.SunToolkit;

@JavaBean(defaultProperty="accessibleContext")
public class JLayeredPane
extends JComponent
implements Accessible {
    public static final Integer DEFAULT_LAYER = 0;
    public static final Integer PALETTE_LAYER = 100;
    public static final Integer MODAL_LAYER = 200;
    public static final Integer POPUP_LAYER = 300;
    public static final Integer DRAG_LAYER = 400;
    public static final Integer FRAME_CONTENT_LAYER = -30000;
    public static final String LAYER_PROPERTY = "layeredContainerLayer";
    private Hashtable<Component, Integer> componentToLayer;
    private boolean optimizedDrawingPossible = true;

    public JLayeredPane() {
        this.setLayout(null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void validateOptimizedDrawing() {
        boolean layeredComponentFound = false;
        Object object = this.getTreeLock();
        synchronized (object) {
            for (Component c : this.getComponents()) {
                Integer layer = null;
                if (!SunToolkit.isInstanceOf(c, "javax.swing.JInternalFrame") && (!(c instanceof JComponent) || (layer = (Integer)((JComponent)c).getClientProperty(LAYER_PROPERTY)) == null) || FRAME_CONTENT_LAYER.equals(layer)) continue;
                layeredComponentFound = true;
                break;
            }
        }
        this.optimizedDrawingPossible = !layeredComponentFound;
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        int layer;
        if (constraints instanceof Integer) {
            layer = (Integer)constraints;
            this.setLayer(comp, layer);
        } else {
            layer = this.getLayer(comp);
        }
        int pos = this.insertIndexForLayer(layer, index);
        super.addImpl(comp, constraints, pos);
        comp.validate();
        comp.repaint();
        this.validateOptimizedDrawing();
    }

    @Override
    public void remove(int index) {
        Component c = this.getComponent(index);
        super.remove(index);
        if (c != null && !(c instanceof JComponent)) {
            this.getComponentToLayer().remove(c);
        }
        this.validateOptimizedDrawing();
    }

    @Override
    public void removeAll() {
        Component[] children = this.getComponents();
        Hashtable<Component, Integer> cToL = this.getComponentToLayer();
        for (int counter = children.length - 1; counter >= 0; --counter) {
            Component c = children[counter];
            if (c == null || c instanceof JComponent) continue;
            cToL.remove(c);
        }
        super.removeAll();
    }

    @Override
    @BeanProperty(bound=false)
    public boolean isOptimizedDrawingEnabled() {
        return this.optimizedDrawingPossible;
    }

    public static void putLayer(JComponent c, int layer) {
        c.putClientProperty(LAYER_PROPERTY, layer);
    }

    public static int getLayer(JComponent c) {
        Integer i = (Integer)c.getClientProperty(LAYER_PROPERTY);
        if (i != null) {
            return i;
        }
        return DEFAULT_LAYER;
    }

    public static JLayeredPane getLayeredPaneAbove(Component c) {
        Container parent;
        if (c == null) {
            return null;
        }
        for (parent = c.getParent(); parent != null && !(parent instanceof JLayeredPane); parent = parent.getParent()) {
        }
        return (JLayeredPane)parent;
    }

    public void setLayer(Component c, int layer) {
        this.setLayer(c, layer, -1);
    }

    public void setLayer(Component c, int layer, int position) {
        Integer layerObj = this.getObjectForLayer(layer);
        if (layer == this.getLayer(c) && position == this.getPosition(c)) {
            this.repaint(c.getBounds());
            return;
        }
        if (c instanceof JComponent) {
            ((JComponent)c).putClientProperty(LAYER_PROPERTY, layerObj);
        } else {
            this.getComponentToLayer().put(c, layerObj);
        }
        if (c.getParent() == null || c.getParent() != this) {
            this.repaint(c.getBounds());
            return;
        }
        int index = this.insertIndexForLayer(c, layer, position);
        this.setComponentZOrder(c, index);
        this.repaint(c.getBounds());
    }

    public int getLayer(Component c) {
        Integer i = c instanceof JComponent ? (Integer)((JComponent)c).getClientProperty(LAYER_PROPERTY) : this.getComponentToLayer().get(c);
        if (i == null) {
            return DEFAULT_LAYER;
        }
        return i;
    }

    public int getIndexOf(Component c) {
        int count = this.getComponentCount();
        for (int i = 0; i < count; ++i) {
            if (c != this.getComponent(i)) continue;
            return i;
        }
        return -1;
    }

    public void moveToFront(Component c) {
        this.setPosition(c, 0);
    }

    public void moveToBack(Component c) {
        this.setPosition(c, -1);
    }

    public void setPosition(Component c, int position) {
        this.setLayer(c, this.getLayer(c), position);
    }

    public int getPosition(Component c) {
        int pos = 0;
        this.getComponentCount();
        int startLocation = this.getIndexOf(c);
        if (startLocation == -1) {
            return -1;
        }
        int startLayer = this.getLayer(c);
        for (int i = startLocation - 1; i >= 0; --i) {
            int curLayer = this.getLayer(this.getComponent(i));
            if (curLayer == startLayer) {
                ++pos;
                continue;
            }
            return pos;
        }
        return pos;
    }

    public int highestLayer() {
        if (this.getComponentCount() > 0) {
            return this.getLayer(this.getComponent(0));
        }
        return 0;
    }

    public int lowestLayer() {
        int count = this.getComponentCount();
        if (count > 0) {
            return this.getLayer(this.getComponent(count - 1));
        }
        return 0;
    }

    public int getComponentCountInLayer(int layer) {
        int layerCount = 0;
        int count = this.getComponentCount();
        for (int i = 0; i < count; ++i) {
            int curLayer = this.getLayer(this.getComponent(i));
            if (curLayer == layer) {
                ++layerCount;
                continue;
            }
            if (layerCount > 0 || curLayer < layer) break;
        }
        return layerCount;
    }

    public Component[] getComponentsInLayer(int layer) {
        int layerCount = 0;
        Component[] results = new Component[this.getComponentCountInLayer(layer)];
        int count = this.getComponentCount();
        for (int i = 0; i < count; ++i) {
            int curLayer = this.getLayer(this.getComponent(i));
            if (curLayer == layer) {
                results[layerCount++] = this.getComponent(i);
                continue;
            }
            if (layerCount > 0 || curLayer < layer) break;
        }
        return results;
    }

    @Override
    public void paint(Graphics g) {
        if (this.isOpaque()) {
            Rectangle r = g.getClipBounds();
            Color c = this.getBackground();
            if (c == null) {
                c = Color.lightGray;
            }
            g.setColor(c);
            if (r != null) {
                g.fillRect(r.x, r.y, r.width, r.height);
            } else {
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
        }
        super.paint(g);
    }

    protected Hashtable<Component, Integer> getComponentToLayer() {
        if (this.componentToLayer == null) {
            this.componentToLayer = new Hashtable(4);
        }
        return this.componentToLayer;
    }

    protected Integer getObjectForLayer(int layer) {
        switch (layer) {
            case 0: {
                return DEFAULT_LAYER;
            }
            case 100: {
                return PALETTE_LAYER;
            }
            case 200: {
                return MODAL_LAYER;
            }
            case 300: {
                return POPUP_LAYER;
            }
            case 400: {
                return DRAG_LAYER;
            }
        }
        return layer;
    }

    protected int insertIndexForLayer(int layer, int position) {
        return this.insertIndexForLayer(null, layer, position);
    }

    private int insertIndexForLayer(Component comp, int layer, int position) {
        int layerStart = -1;
        int layerEnd = -1;
        int componentCount = this.getComponentCount();
        ArrayList<Component> compList = new ArrayList<Component>(componentCount);
        for (int index = 0; index < componentCount; ++index) {
            if (this.getComponent(index) == comp) continue;
            compList.add(this.getComponent(index));
        }
        int count = compList.size();
        for (int i = 0; i < count; ++i) {
            int curLayer = this.getLayer((Component)compList.get(i));
            if (layerStart == -1 && curLayer == layer) {
                layerStart = i;
            }
            if (curLayer >= layer) continue;
            if (i == 0) {
                layerStart = 0;
                layerEnd = 0;
                break;
            }
            layerEnd = i;
            break;
        }
        if (layerStart == -1 && layerEnd == -1) {
            return count;
        }
        if (layerStart != -1 && layerEnd == -1) {
            layerEnd = count;
        }
        if (layerEnd != -1 && layerStart == -1) {
            layerStart = layerEnd;
        }
        if (position == -1) {
            return layerEnd;
        }
        if (position > -1 && layerStart + position <= layerEnd) {
            return layerStart + position;
        }
        return layerEnd;
    }

    @Override
    protected String paramString() {
        String optimizedDrawingPossibleString = this.optimizedDrawingPossible ? "true" : "false";
        return super.paramString() + ",optimizedDrawingPossible=" + optimizedDrawingPossibleString;
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJLayeredPane();
        }
        return this.accessibleContext;
    }

    protected class AccessibleJLayeredPane
    extends JComponent.AccessibleJComponent {
        protected AccessibleJLayeredPane() {
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.LAYERED_PANE;
        }
    }
}

