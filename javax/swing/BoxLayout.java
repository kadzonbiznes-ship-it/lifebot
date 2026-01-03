/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.AWTError;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.beans.ConstructorProperties;
import java.io.PrintStream;
import java.io.Serializable;
import javax.swing.SizeRequirements;

public class BoxLayout
implements LayoutManager2,
Serializable {
    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int LINE_AXIS = 2;
    public static final int PAGE_AXIS = 3;
    private int axis;
    private Container target;
    private transient SizeRequirements[] xChildren;
    private transient SizeRequirements[] yChildren;
    private transient SizeRequirements xTotal;
    private transient SizeRequirements yTotal;
    private transient PrintStream dbg;

    @ConstructorProperties(value={"target", "axis"})
    public BoxLayout(Container target, int axis) {
        if (axis != 0 && axis != 1 && axis != 2 && axis != 3) {
            throw new AWTError("Invalid axis");
        }
        this.axis = axis;
        this.target = target;
    }

    BoxLayout(Container target, int axis, PrintStream dbg) {
        this(target, axis);
        this.dbg = dbg;
    }

    public final Container getTarget() {
        return this.target;
    }

    public final int getAxis() {
        return this.axis;
    }

    @Override
    public synchronized void invalidateLayout(Container target) {
        this.checkContainer(target);
        this.xChildren = null;
        this.yChildren = null;
        this.xTotal = null;
        this.yTotal = null;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        this.invalidateLayout(comp.getParent());
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        this.invalidateLayout(comp.getParent());
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        this.invalidateLayout(comp.getParent());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension preferredLayoutSize(Container target) {
        Dimension size;
        BoxLayout boxLayout = this;
        synchronized (boxLayout) {
            this.checkContainer(target);
            this.checkRequests();
            size = new Dimension(this.xTotal.preferred, this.yTotal.preferred);
        }
        Insets insets = target.getInsets();
        size.width = (int)Math.min((long)size.width + (long)insets.left + (long)insets.right, Integer.MAX_VALUE);
        size.height = (int)Math.min((long)size.height + (long)insets.top + (long)insets.bottom, Integer.MAX_VALUE);
        return size;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension size;
        BoxLayout boxLayout = this;
        synchronized (boxLayout) {
            this.checkContainer(target);
            this.checkRequests();
            size = new Dimension(this.xTotal.minimum, this.yTotal.minimum);
        }
        Insets insets = target.getInsets();
        size.width = (int)Math.min((long)size.width + (long)insets.left + (long)insets.right, Integer.MAX_VALUE);
        size.height = (int)Math.min((long)size.height + (long)insets.top + (long)insets.bottom, Integer.MAX_VALUE);
        return size;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension maximumLayoutSize(Container target) {
        Dimension size;
        BoxLayout boxLayout = this;
        synchronized (boxLayout) {
            this.checkContainer(target);
            this.checkRequests();
            size = new Dimension(this.xTotal.maximum, this.yTotal.maximum);
        }
        Insets insets = target.getInsets();
        size.width = (int)Math.min((long)size.width + (long)insets.left + (long)insets.right, Integer.MAX_VALUE);
        size.height = (int)Math.min((long)size.height + (long)insets.top + (long)insets.bottom, Integer.MAX_VALUE);
        return size;
    }

    @Override
    public synchronized float getLayoutAlignmentX(Container target) {
        this.checkContainer(target);
        this.checkRequests();
        return this.xTotal.alignment;
    }

    @Override
    public synchronized float getLayoutAlignmentY(Container target) {
        this.checkContainer(target);
        this.checkRequests();
        return this.yTotal.alignment;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void layoutContainer(Container target) {
        Component c;
        int i;
        this.checkContainer(target);
        int nChildren = target.getComponentCount();
        int[] xOffsets = new int[nChildren];
        int[] xSpans = new int[nChildren];
        int[] yOffsets = new int[nChildren];
        int[] ySpans = new int[nChildren];
        Dimension alloc = target.getSize();
        Insets in = target.getInsets();
        alloc.width -= in.left + in.right;
        alloc.height -= in.top + in.bottom;
        ComponentOrientation o = target.getComponentOrientation();
        int absoluteAxis = this.resolveAxis(this.axis, o);
        boolean ltr = absoluteAxis != this.axis ? o.isLeftToRight() : true;
        BoxLayout boxLayout = this;
        synchronized (boxLayout) {
            this.checkRequests();
            if (absoluteAxis == 0) {
                SizeRequirements.calculateTiledPositions(alloc.width, this.xTotal, this.xChildren, xOffsets, xSpans, ltr);
                SizeRequirements.calculateAlignedPositions(alloc.height, this.yTotal, this.yChildren, yOffsets, ySpans);
            } else {
                SizeRequirements.calculateAlignedPositions(alloc.width, this.xTotal, this.xChildren, xOffsets, xSpans, ltr);
                SizeRequirements.calculateTiledPositions(alloc.height, this.yTotal, this.yChildren, yOffsets, ySpans);
            }
        }
        for (i = 0; i < nChildren; ++i) {
            c = target.getComponent(i);
            c.setBounds((int)Math.min((long)in.left + (long)xOffsets[i], Integer.MAX_VALUE), (int)Math.min((long)in.top + (long)yOffsets[i], Integer.MAX_VALUE), xSpans[i], ySpans[i]);
        }
        if (this.dbg != null) {
            for (i = 0; i < nChildren; ++i) {
                c = target.getComponent(i);
                this.dbg.println(c.toString());
                this.dbg.println("X: " + String.valueOf(this.xChildren[i]));
                this.dbg.println("Y: " + String.valueOf(this.yChildren[i]));
            }
        }
    }

    void checkContainer(Container target) {
        if (this.target != target) {
            throw new AWTError("BoxLayout can't be shared");
        }
    }

    void checkRequests() {
        if (this.xChildren == null || this.yChildren == null) {
            int n = this.target.getComponentCount();
            this.xChildren = new SizeRequirements[n];
            this.yChildren = new SizeRequirements[n];
            for (int i = 0; i < n; ++i) {
                Component c = this.target.getComponent(i);
                if (!c.isVisible()) {
                    this.xChildren[i] = new SizeRequirements(0, 0, 0, c.getAlignmentX());
                    this.yChildren[i] = new SizeRequirements(0, 0, 0, c.getAlignmentY());
                    continue;
                }
                Dimension min = c.getMinimumSize();
                Dimension typ = c.getPreferredSize();
                Dimension max = c.getMaximumSize();
                this.xChildren[i] = new SizeRequirements(min.width, typ.width, max.width, c.getAlignmentX());
                this.yChildren[i] = new SizeRequirements(min.height, typ.height, max.height, c.getAlignmentY());
            }
            int absoluteAxis = this.resolveAxis(this.axis, this.target.getComponentOrientation());
            if (absoluteAxis == 0) {
                this.xTotal = SizeRequirements.getTiledSizeRequirements(this.xChildren);
                this.yTotal = SizeRequirements.getAlignedSizeRequirements(this.yChildren);
            } else {
                this.xTotal = SizeRequirements.getAlignedSizeRequirements(this.xChildren);
                this.yTotal = SizeRequirements.getTiledSizeRequirements(this.yChildren);
            }
        }
    }

    private int resolveAxis(int axis, ComponentOrientation o) {
        int absoluteAxis = axis == 2 ? (o.isHorizontal() ? 0 : 1) : (axis == 3 ? (o.isHorizontal() ? 1 : 0) : axis);
        return absoluteAxis;
    }
}

