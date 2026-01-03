/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class FlowLayout
implements LayoutManager,
Serializable {
    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;
    public static final int LEADING = 3;
    public static final int TRAILING = 4;
    int align;
    int newAlign;
    int hgap;
    int vgap;
    private boolean alignOnBaseline;
    private static final long serialVersionUID = -7262534875583282631L;
    private static final int currentSerialVersion = 1;
    private int serialVersionOnStream = 1;

    public FlowLayout() {
        this(1, 5, 5);
    }

    public FlowLayout(int align) {
        this(align, 5, 5);
    }

    public FlowLayout(int align, int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
        this.setAlignment(align);
    }

    public int getAlignment() {
        return this.newAlign;
    }

    public void setAlignment(int align) {
        this.newAlign = align;
        switch (align) {
            case 3: {
                this.align = 0;
                break;
            }
            case 4: {
                this.align = 2;
                break;
            }
            default: {
                this.align = align;
            }
        }
    }

    public int getHgap() {
        return this.hgap;
    }

    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    public int getVgap() {
        return this.vgap;
    }

    public void setVgap(int vgap) {
        this.vgap = vgap;
    }

    public void setAlignOnBaseline(boolean alignOnBaseline) {
        this.alignOnBaseline = alignOnBaseline;
    }

    public boolean getAlignOnBaseline() {
        return this.alignOnBaseline;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension preferredLayoutSize(Container target) {
        Object object = target.getTreeLock();
        synchronized (object) {
            Dimension dim = new Dimension(0, 0);
            int nmembers = target.getComponentCount();
            boolean firstVisibleComponent = true;
            boolean useBaseline = this.getAlignOnBaseline();
            int maxAscent = 0;
            int maxDescent = 0;
            for (int i = 0; i < nmembers; ++i) {
                int baseline;
                Component m = target.getComponent(i);
                if (!m.isVisible()) continue;
                Dimension d = m.getPreferredSize();
                dim.height = Math.max(dim.height, d.height);
                if (firstVisibleComponent) {
                    firstVisibleComponent = false;
                } else {
                    dim.width += this.hgap;
                }
                dim.width += d.width;
                if (!useBaseline || (baseline = m.getBaseline(d.width, d.height)) < 0) continue;
                maxAscent = Math.max(maxAscent, baseline);
                maxDescent = Math.max(maxDescent, d.height - baseline);
            }
            if (useBaseline) {
                dim.height = Math.max(maxAscent + maxDescent, dim.height);
            }
            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right + this.hgap * 2;
            dim.height += insets.top + insets.bottom + this.vgap * 2;
            return dim;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension minimumLayoutSize(Container target) {
        Object object = target.getTreeLock();
        synchronized (object) {
            boolean useBaseline = this.getAlignOnBaseline();
            Dimension dim = new Dimension(0, 0);
            int nmembers = target.getComponentCount();
            int maxAscent = 0;
            int maxDescent = 0;
            boolean firstVisibleComponent = true;
            for (int i = 0; i < nmembers; ++i) {
                int baseline;
                Component m = target.getComponent(i);
                if (!m.visible) continue;
                Dimension d = m.getMinimumSize();
                dim.height = Math.max(dim.height, d.height);
                if (firstVisibleComponent) {
                    firstVisibleComponent = false;
                } else {
                    dim.width += this.hgap;
                }
                dim.width += d.width;
                if (!useBaseline || (baseline = m.getBaseline(d.width, d.height)) < 0) continue;
                maxAscent = Math.max(maxAscent, baseline);
                maxDescent = Math.max(maxDescent, dim.height - baseline);
            }
            if (useBaseline) {
                dim.height = Math.max(maxAscent + maxDescent, dim.height);
            }
            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right + this.hgap * 2;
            dim.height += insets.top + insets.bottom + this.vgap * 2;
            return dim;
        }
    }

    private int moveComponents(Container target, int x, int y, int width, int height, int rowStart, int rowEnd, boolean ltr, boolean useBaseline, int[] ascent, int[] descent) {
        switch (this.newAlign) {
            case 0: {
                x += ltr ? 0 : width;
                break;
            }
            case 1: {
                x += width / 2;
                break;
            }
            case 2: {
                x += ltr ? width : 0;
                break;
            }
            case 3: {
                break;
            }
            case 4: {
                x += width;
            }
        }
        int maxAscent = 0;
        int nonbaselineHeight = 0;
        int baselineOffset = 0;
        if (useBaseline) {
            int maxDescent = 0;
            for (int i = rowStart; i < rowEnd; ++i) {
                Component m = target.getComponent(i);
                if (!m.visible) continue;
                if (ascent[i] >= 0) {
                    maxAscent = Math.max(maxAscent, ascent[i]);
                    maxDescent = Math.max(maxDescent, descent[i]);
                    continue;
                }
                nonbaselineHeight = Math.max(m.getHeight(), nonbaselineHeight);
            }
            height = Math.max(maxAscent + maxDescent, nonbaselineHeight);
            baselineOffset = (height - maxAscent - maxDescent) / 2;
        }
        for (int i = rowStart; i < rowEnd; ++i) {
            Component m = target.getComponent(i);
            if (!m.isVisible()) continue;
            int cy = useBaseline && ascent[i] >= 0 ? y + baselineOffset + maxAscent - ascent[i] : y + (height - m.height) / 2;
            if (ltr) {
                m.setLocation(x, cy);
            } else {
                m.setLocation(target.width - x - m.width, cy);
            }
            x += m.width + this.hgap;
        }
        return height;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void layoutContainer(Container target) {
        Object object = target.getTreeLock();
        synchronized (object) {
            Insets insets = target.getInsets();
            int maxwidth = target.width - (insets.left + insets.right + this.hgap * 2);
            int nmembers = target.getComponentCount();
            int x = 0;
            int y = insets.top + this.vgap;
            int rowh = 0;
            int start = 0;
            boolean ltr = target.getComponentOrientation().isLeftToRight();
            boolean useBaseline = this.getAlignOnBaseline();
            int[] ascent = null;
            int[] descent = null;
            if (useBaseline) {
                ascent = new int[nmembers];
                descent = new int[nmembers];
            }
            for (int i = 0; i < nmembers; ++i) {
                Component m = target.getComponent(i);
                if (!m.isVisible()) continue;
                Dimension d = m.getPreferredSize();
                m.setSize(d.width, d.height);
                if (useBaseline) {
                    int baseline = m.getBaseline(d.width, d.height);
                    if (baseline >= 0) {
                        ascent[i] = baseline;
                        descent[i] = d.height - baseline;
                    } else {
                        ascent[i] = -1;
                    }
                }
                if (x == 0 || x + d.width <= maxwidth) {
                    if (x > 0) {
                        x += this.hgap;
                    }
                    x += d.width;
                    rowh = Math.max(rowh, d.height);
                    continue;
                }
                rowh = this.moveComponents(target, insets.left + this.hgap, y, maxwidth - x, rowh, start, i, ltr, useBaseline, ascent, descent);
                x = d.width;
                y += this.vgap + rowh;
                rowh = d.height;
                start = i;
            }
            this.moveComponents(target, insets.left + this.hgap, y, maxwidth - x, rowh, start, nmembers, ltr, useBaseline, ascent, descent);
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.serialVersionOnStream < 1) {
            this.setAlignment(this.align);
        }
        this.serialVersionOnStream = 1;
    }

    public String toString() {
        String str = "";
        switch (this.align) {
            case 0: {
                str = ",align=left";
                break;
            }
            case 1: {
                str = ",align=center";
                break;
            }
            case 2: {
                str = ",align=right";
                break;
            }
            case 3: {
                str = ",align=leading";
                break;
            }
            case 4: {
                str = ",align=trailing";
            }
        }
        return this.getClass().getName() + "[hgap=" + this.hgap + ",vgap=" + this.vgap + str + "]";
    }
}

