/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayoutInfo;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Hashtable;

public class GridBagLayout
implements LayoutManager2,
Serializable {
    static final int EMPIRICMULTIPLIER = 2;
    protected static final int MAXGRIDSIZE = 512;
    protected static final int MINSIZE = 1;
    protected static final int PREFERREDSIZE = 2;
    protected Hashtable<Component, GridBagConstraints> comptable = new Hashtable();
    protected GridBagConstraints defaultConstraints = new GridBagConstraints();
    protected GridBagLayoutInfo layoutInfo;
    public int[] columnWidths;
    public int[] rowHeights;
    public double[] columnWeights;
    public double[] rowWeights;
    private Component componentAdjusting;
    transient boolean rightToLeft = false;
    private static final long serialVersionUID = 8838754796412211005L;

    public void setConstraints(Component comp, GridBagConstraints constraints) {
        this.comptable.put(comp, (GridBagConstraints)constraints.clone());
    }

    public GridBagConstraints getConstraints(Component comp) {
        GridBagConstraints constraints = this.comptable.get(comp);
        if (constraints == null) {
            this.setConstraints(comp, this.defaultConstraints);
            constraints = this.comptable.get(comp);
        }
        return (GridBagConstraints)constraints.clone();
    }

    protected GridBagConstraints lookupConstraints(Component comp) {
        GridBagConstraints constraints = this.comptable.get(comp);
        if (constraints == null) {
            this.setConstraints(comp, this.defaultConstraints);
            constraints = this.comptable.get(comp);
        }
        return constraints;
    }

    private void removeConstraints(Component comp) {
        this.comptable.remove(comp);
    }

    public Point getLayoutOrigin() {
        Point origin = new Point(0, 0);
        if (this.layoutInfo != null) {
            origin.x = this.layoutInfo.startx;
            origin.y = this.layoutInfo.starty;
        }
        return origin;
    }

    public int[][] getLayoutDimensions() {
        if (this.layoutInfo == null) {
            return new int[2][0];
        }
        int[][] dim = new int[][]{new int[this.layoutInfo.width], new int[this.layoutInfo.height]};
        System.arraycopy(this.layoutInfo.minWidth, 0, dim[0], 0, this.layoutInfo.width);
        System.arraycopy(this.layoutInfo.minHeight, 0, dim[1], 0, this.layoutInfo.height);
        return dim;
    }

    public double[][] getLayoutWeights() {
        if (this.layoutInfo == null) {
            return new double[2][0];
        }
        double[][] weights = new double[][]{new double[this.layoutInfo.width], new double[this.layoutInfo.height]};
        System.arraycopy(this.layoutInfo.weightX, 0, weights[0], 0, this.layoutInfo.width);
        System.arraycopy(this.layoutInfo.weightY, 0, weights[1], 0, this.layoutInfo.height);
        return weights;
    }

    public Point location(int x, int y) {
        int i;
        Point loc = new Point(0, 0);
        if (this.layoutInfo == null) {
            return loc;
        }
        int d = this.layoutInfo.startx;
        if (!this.rightToLeft) {
            for (i = 0; i < this.layoutInfo.width && (d += this.layoutInfo.minWidth[i]) <= x; ++i) {
            }
        } else {
            for (i = this.layoutInfo.width - 1; i >= 0 && d <= x; d += this.layoutInfo.minWidth[i], --i) {
            }
            ++i;
        }
        loc.x = i;
        d = this.layoutInfo.starty;
        for (i = 0; i < this.layoutInfo.height && (d += this.layoutInfo.minHeight[i]) <= y; ++i) {
        }
        loc.y = i;
        return loc;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints instanceof GridBagConstraints) {
            this.setConstraints(comp, (GridBagConstraints)constraints);
        } else if (constraints != null) {
            throw new IllegalArgumentException("cannot add to layout: constraints must be a GridBagConstraint");
        }
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        this.removeConstraints(comp);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        GridBagLayoutInfo info = this.getLayoutInfo(parent, 2);
        return this.getMinSize(parent, info);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        GridBagLayoutInfo info = this.getLayoutInfo(parent, 1);
        return this.getMinSize(parent, info);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container parent) {
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container parent) {
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container target) {
    }

    @Override
    public void layoutContainer(Container parent) {
        this.arrangeGrid(parent);
    }

    public String toString() {
        return this.getClass().getName();
    }

    protected GridBagLayoutInfo getLayoutInfo(Container parent, int sizeflag) {
        return this.GetLayoutInfo(parent, sizeflag);
    }

    private long[] preInitMaximumArraySizes(Container parent) {
        Component[] components = parent.getComponents();
        int preMaximumArrayXIndex = 0;
        int preMaximumArrayYIndex = 0;
        long[] returnArray = new long[2];
        for (int compId = 0; compId < components.length; ++compId) {
            Component comp = components[compId];
            if (!comp.isVisible()) continue;
            GridBagConstraints constraints = this.lookupConstraints(comp);
            int curX = constraints.gridx;
            int curY = constraints.gridy;
            int curWidth = constraints.gridwidth;
            int curHeight = constraints.gridheight;
            if (curX < 0) {
                curX = ++preMaximumArrayYIndex;
            }
            if (curY < 0) {
                curY = ++preMaximumArrayXIndex;
            }
            if (curWidth <= 0) {
                curWidth = 1;
            }
            if (curHeight <= 0) {
                curHeight = 1;
            }
            preMaximumArrayXIndex = Math.max(curY + curHeight, preMaximumArrayXIndex);
            preMaximumArrayYIndex = Math.max(curX + curWidth, preMaximumArrayYIndex);
        }
        returnArray[0] = preMaximumArrayXIndex;
        returnArray[1] = preMaximumArrayYIndex;
        return returnArray;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected GridBagLayoutInfo GetLayoutInfo(Container parent, int sizeflag) {
        Object object = parent.getTreeLock();
        synchronized (object) {
            int pixels_diff;
            int py;
            int i;
            int px;
            GridBagConstraints constraints;
            Component comp;
            int compindex;
            Component[] components = parent.getComponents();
            int curX = 0;
            int curY = 0;
            int curWidth = 1;
            int curHeight = 1;
            int maximumArrayXIndex = 0;
            int maximumArrayYIndex = 0;
            int layoutHeight = 0;
            int layoutWidth = 0;
            int curCol = -1;
            int curRow = -1;
            long[] arraySizes = this.preInitMaximumArraySizes(parent);
            maximumArrayXIndex = 2L * arraySizes[0] > Integer.MAX_VALUE ? Integer.MAX_VALUE : 2 * (int)arraySizes[0];
            int n = maximumArrayYIndex = 2L * arraySizes[1] > Integer.MAX_VALUE ? Integer.MAX_VALUE : 2 * (int)arraySizes[1];
            if (this.rowHeights != null) {
                maximumArrayXIndex = Math.max(maximumArrayXIndex, this.rowHeights.length);
            }
            if (this.columnWidths != null) {
                maximumArrayYIndex = Math.max(maximumArrayYIndex, this.columnWidths.length);
            }
            int[] xMaxArray = new int[maximumArrayXIndex];
            int[] yMaxArray = new int[maximumArrayYIndex];
            boolean hasBaseline = false;
            for (compindex = 0; compindex < components.length; ++compindex) {
                comp = components[compindex];
                if (!comp.isVisible()) continue;
                constraints = this.lookupConstraints(comp);
                curX = constraints.gridx;
                curY = constraints.gridy;
                curWidth = constraints.gridwidth;
                if (curWidth <= 0) {
                    curWidth = 1;
                }
                if ((curHeight = constraints.gridheight) <= 0) {
                    curHeight = 1;
                }
                if (curX < 0 && curY < 0) {
                    if (curRow >= 0) {
                        curY = curRow;
                    } else if (curCol >= 0) {
                        curX = curCol;
                    } else {
                        curY = 0;
                    }
                }
                if (curX < 0) {
                    px = 0;
                    for (i = curY; i < curY + curHeight; ++i) {
                        px = Math.max(px, xMaxArray[i]);
                    }
                    if ((curX = px - curX - 1) < 0) {
                        curX = 0;
                    }
                } else if (curY < 0) {
                    py = 0;
                    for (i = curX; i < curX + curWidth; ++i) {
                        py = Math.max(py, yMaxArray[i]);
                    }
                    if ((curY = py - curY - 1) < 0) {
                        curY = 0;
                    }
                }
                if (layoutWidth < (px = curX + curWidth)) {
                    layoutWidth = px;
                }
                if (layoutHeight < (py = curY + curHeight)) {
                    layoutHeight = py;
                }
                for (i = curX; i < curX + curWidth; ++i) {
                    yMaxArray[i] = py;
                }
                for (i = curY; i < curY + curHeight; ++i) {
                    xMaxArray[i] = px;
                }
                Dimension d = sizeflag == 2 ? comp.getPreferredSize() : comp.getMinimumSize();
                constraints.minWidth = d.width;
                constraints.minHeight = d.height;
                if (this.calculateBaseline(comp, constraints, d)) {
                    hasBaseline = true;
                }
                if (constraints.gridheight == 0 && constraints.gridwidth == 0) {
                    curCol = -1;
                    curRow = -1;
                }
                if (constraints.gridheight == 0 && curRow < 0) {
                    curCol = curX + curWidth;
                    continue;
                }
                if (constraints.gridwidth != 0 || curCol >= 0) continue;
                curRow = curY + curHeight;
            }
            if (this.columnWidths != null && layoutWidth < this.columnWidths.length) {
                layoutWidth = this.columnWidths.length;
            }
            if (this.rowHeights != null && layoutHeight < this.rowHeights.length) {
                layoutHeight = this.rowHeights.length;
            }
            GridBagLayoutInfo r = new GridBagLayoutInfo(layoutWidth, layoutHeight);
            curCol = -1;
            curRow = -1;
            Arrays.fill(xMaxArray, 0);
            Arrays.fill(yMaxArray, 0);
            int[] maxAscent = null;
            int[] maxDescent = null;
            short[] baselineType = null;
            if (hasBaseline) {
                maxAscent = new int[layoutHeight];
                r.maxAscent = maxAscent;
                maxDescent = new int[layoutHeight];
                r.maxDescent = maxDescent;
                baselineType = new short[layoutHeight];
                r.baselineType = baselineType;
                r.hasBaseline = true;
            }
            block18: for (compindex = 0; compindex < components.length; ++compindex) {
                comp = components[compindex];
                if (!comp.isVisible()) continue;
                constraints = this.lookupConstraints(comp);
                curX = constraints.gridx;
                curY = constraints.gridy;
                curWidth = constraints.gridwidth;
                curHeight = constraints.gridheight;
                if (curX < 0 && curY < 0) {
                    if (curRow >= 0) {
                        curY = curRow;
                    } else if (curCol >= 0) {
                        curX = curCol;
                    } else {
                        curY = 0;
                    }
                }
                if (curX < 0) {
                    if (curHeight <= 0 && (curHeight += r.height - curY) < 1) {
                        curHeight = 1;
                    }
                    px = 0;
                    for (i = curY; i < curY + curHeight; ++i) {
                        px = Math.max(px, xMaxArray[i]);
                    }
                    if ((curX = px - curX - 1) < 0) {
                        curX = 0;
                    }
                } else if (curY < 0) {
                    if (curWidth <= 0 && (curWidth += r.width - curX) < 1) {
                        curWidth = 1;
                    }
                    py = 0;
                    for (i = curX; i < curX + curWidth; ++i) {
                        py = Math.max(py, yMaxArray[i]);
                    }
                    if ((curY = py - curY - 1) < 0) {
                        curY = 0;
                    }
                }
                if (curWidth <= 0 && (curWidth += r.width - curX) < 1) {
                    curWidth = 1;
                }
                if (curHeight <= 0 && (curHeight += r.height - curY) < 1) {
                    curHeight = 1;
                }
                px = curX + curWidth;
                py = curY + curHeight;
                for (i = curX; i < curX + curWidth; ++i) {
                    yMaxArray[i] = py;
                }
                for (i = curY; i < curY + curHeight; ++i) {
                    xMaxArray[i] = px;
                }
                if (constraints.gridheight == 0 && constraints.gridwidth == 0) {
                    curCol = -1;
                    curRow = -1;
                }
                if (constraints.gridheight == 0 && curRow < 0) {
                    curCol = curX + curWidth;
                } else if (constraints.gridwidth == 0 && curCol < 0) {
                    curRow = curY + curHeight;
                }
                constraints.tempX = curX;
                constraints.tempY = curY;
                constraints.tempWidth = curWidth;
                constraints.tempHeight = curHeight;
                int anchor = constraints.anchor;
                if (!hasBaseline) continue;
                switch (anchor) {
                    case 256: 
                    case 512: 
                    case 768: {
                        if (constraints.ascent < 0) continue block18;
                        if (curHeight == 1) {
                            maxAscent[curY] = Math.max(maxAscent[curY], constraints.ascent);
                            maxDescent[curY] = Math.max(maxDescent[curY], constraints.descent);
                        } else if (constraints.baselineResizeBehavior == Component.BaselineResizeBehavior.CONSTANT_DESCENT) {
                            maxDescent[curY + curHeight - 1] = Math.max(maxDescent[curY + curHeight - 1], constraints.descent);
                        } else {
                            maxAscent[curY] = Math.max(maxAscent[curY], constraints.ascent);
                        }
                        if (constraints.baselineResizeBehavior == Component.BaselineResizeBehavior.CONSTANT_DESCENT) {
                            int n2 = curY + curHeight - 1;
                            baselineType[n2] = (short)(baselineType[n2] | 1 << constraints.baselineResizeBehavior.ordinal());
                            continue block18;
                        }
                        int n3 = curY;
                        baselineType[n3] = (short)(baselineType[n3] | 1 << constraints.baselineResizeBehavior.ordinal());
                        continue block18;
                    }
                    case 1024: 
                    case 1280: 
                    case 1536: {
                        pixels_diff = constraints.minHeight + constraints.insets.top + constraints.ipady;
                        maxAscent[curY] = Math.max(maxAscent[curY], pixels_diff);
                        maxDescent[curY] = Math.max(maxDescent[curY], constraints.insets.bottom);
                        continue block18;
                    }
                    case 1792: 
                    case 2048: 
                    case 2304: {
                        pixels_diff = constraints.minHeight + constraints.insets.bottom + constraints.ipady;
                        maxDescent[curY] = Math.max(maxDescent[curY], pixels_diff);
                        maxAscent[curY] = Math.max(maxAscent[curY], constraints.insets.top);
                    }
                }
            }
            r.weightX = new double[maximumArrayYIndex];
            r.weightY = new double[maximumArrayXIndex];
            r.minWidth = new int[maximumArrayYIndex];
            r.minHeight = new int[maximumArrayXIndex];
            if (this.columnWidths != null) {
                System.arraycopy(this.columnWidths, 0, r.minWidth, 0, this.columnWidths.length);
            }
            if (this.rowHeights != null) {
                System.arraycopy(this.rowHeights, 0, r.minHeight, 0, this.rowHeights.length);
            }
            if (this.columnWeights != null) {
                System.arraycopy(this.columnWeights, 0, r.weightX, 0, Math.min(r.weightX.length, this.columnWeights.length));
            }
            if (this.rowWeights != null) {
                System.arraycopy(this.rowWeights, 0, r.weightY, 0, Math.min(r.weightY.length, this.rowWeights.length));
            }
            int nextSize = Integer.MAX_VALUE;
            i = 1;
            while (i != Integer.MAX_VALUE) {
                for (compindex = 0; compindex < components.length; ++compindex) {
                    double wt;
                    double weight;
                    int k;
                    double weight_diff;
                    comp = components[compindex];
                    if (!comp.isVisible()) continue;
                    constraints = this.lookupConstraints(comp);
                    if (constraints.tempWidth == i) {
                        px = constraints.tempX + constraints.tempWidth;
                        weight_diff = constraints.weightx;
                        for (k = constraints.tempX; k < px; ++k) {
                            weight_diff -= r.weightX[k];
                        }
                        if (weight_diff > 0.0) {
                            weight = 0.0;
                            for (k = constraints.tempX; k < px; ++k) {
                                weight += r.weightX[k];
                            }
                            k = constraints.tempX;
                            while (weight > 0.0 && k < px) {
                                wt = r.weightX[k];
                                double dx = wt * weight_diff / weight;
                                int n4 = k++;
                                r.weightX[n4] = r.weightX[n4] + dx;
                                weight_diff -= dx;
                                weight -= wt;
                            }
                            int n5 = px - 1;
                            r.weightX[n5] = r.weightX[n5] + weight_diff;
                        }
                        pixels_diff = constraints.minWidth + constraints.ipadx + constraints.insets.left + constraints.insets.right;
                        for (k = constraints.tempX; k < px; ++k) {
                            pixels_diff -= r.minWidth[k];
                        }
                        if (pixels_diff > 0) {
                            weight = 0.0;
                            for (k = constraints.tempX; k < px; ++k) {
                                weight += r.weightX[k];
                            }
                            k = constraints.tempX;
                            while (weight > 0.0 && k < px) {
                                wt = r.weightX[k];
                                int dx = (int)(wt * (double)pixels_diff / weight);
                                int n6 = k++;
                                r.minWidth[n6] = r.minWidth[n6] + dx;
                                pixels_diff -= dx;
                                weight -= wt;
                            }
                            int n7 = px - 1;
                            r.minWidth[n7] = r.minWidth[n7] + pixels_diff;
                        }
                    } else if (constraints.tempWidth > i && constraints.tempWidth < nextSize) {
                        nextSize = constraints.tempWidth;
                    }
                    if (constraints.tempHeight == i) {
                        py = constraints.tempY + constraints.tempHeight;
                        weight_diff = constraints.weighty;
                        for (k = constraints.tempY; k < py; ++k) {
                            weight_diff -= r.weightY[k];
                        }
                        if (weight_diff > 0.0) {
                            weight = 0.0;
                            for (k = constraints.tempY; k < py; ++k) {
                                weight += r.weightY[k];
                            }
                            k = constraints.tempY;
                            while (weight > 0.0 && k < py) {
                                wt = r.weightY[k];
                                double dy = wt * weight_diff / weight;
                                int n8 = k++;
                                r.weightY[n8] = r.weightY[n8] + dy;
                                weight_diff -= dy;
                                weight -= wt;
                            }
                            int n9 = py - 1;
                            r.weightY[n9] = r.weightY[n9] + weight_diff;
                        }
                        pixels_diff = -1;
                        if (hasBaseline) {
                            switch (constraints.anchor) {
                                case 256: 
                                case 512: 
                                case 768: {
                                    if (constraints.ascent < 0) break;
                                    if (constraints.tempHeight == 1) {
                                        pixels_diff = maxAscent[constraints.tempY] + maxDescent[constraints.tempY];
                                        break;
                                    }
                                    if (constraints.baselineResizeBehavior != Component.BaselineResizeBehavior.CONSTANT_DESCENT) {
                                        pixels_diff = maxAscent[constraints.tempY] + constraints.descent;
                                        break;
                                    }
                                    pixels_diff = constraints.ascent + maxDescent[constraints.tempY + constraints.tempHeight - 1];
                                    break;
                                }
                                case 1024: 
                                case 1280: 
                                case 1536: {
                                    pixels_diff = constraints.insets.top + constraints.minHeight + constraints.ipady + maxDescent[constraints.tempY];
                                    break;
                                }
                                case 1792: 
                                case 2048: 
                                case 2304: {
                                    pixels_diff = maxAscent[constraints.tempY] + constraints.minHeight + constraints.insets.bottom + constraints.ipady;
                                }
                            }
                        }
                        if (pixels_diff == -1) {
                            pixels_diff = constraints.minHeight + constraints.ipady + constraints.insets.top + constraints.insets.bottom;
                        }
                        for (k = constraints.tempY; k < py; ++k) {
                            pixels_diff -= r.minHeight[k];
                        }
                        if (pixels_diff <= 0) continue;
                        weight = 0.0;
                        for (k = constraints.tempY; k < py; ++k) {
                            weight += r.weightY[k];
                        }
                        k = constraints.tempY;
                        while (weight > 0.0 && k < py) {
                            wt = r.weightY[k];
                            int dy = (int)(wt * (double)pixels_diff / weight);
                            int n10 = k++;
                            r.minHeight[n10] = r.minHeight[n10] + dy;
                            pixels_diff -= dy;
                            weight -= wt;
                        }
                        int n11 = py - 1;
                        r.minHeight[n11] = r.minHeight[n11] + pixels_diff;
                        continue;
                    }
                    if (constraints.tempHeight <= i || constraints.tempHeight >= nextSize) continue;
                    nextSize = constraints.tempHeight;
                }
                i = nextSize;
                nextSize = Integer.MAX_VALUE;
            }
            return r;
        }
    }

    private boolean calculateBaseline(Component c, GridBagConstraints constraints, Dimension size) {
        int anchor = constraints.anchor;
        if (anchor == 256 || anchor == 512 || anchor == 768) {
            int w = size.width + constraints.ipadx;
            int h = size.height + constraints.ipady;
            constraints.ascent = c.getBaseline(w, h);
            if (constraints.ascent >= 0) {
                int baseline = constraints.ascent;
                constraints.descent = h - constraints.ascent + constraints.insets.bottom;
                constraints.ascent += constraints.insets.top;
                constraints.baselineResizeBehavior = c.getBaselineResizeBehavior();
                constraints.centerPadding = 0;
                if (constraints.baselineResizeBehavior == Component.BaselineResizeBehavior.CENTER_OFFSET) {
                    int nextBaseline = c.getBaseline(w, h + 1);
                    constraints.centerOffset = baseline - h / 2;
                    if (h % 2 == 0) {
                        if (baseline != nextBaseline) {
                            constraints.centerPadding = 1;
                        }
                    } else if (baseline == nextBaseline) {
                        --constraints.centerOffset;
                        constraints.centerPadding = 1;
                    }
                }
            }
            return true;
        }
        constraints.ascent = -1;
        return false;
    }

    protected void adjustForGravity(GridBagConstraints constraints, Rectangle r) {
        this.AdjustForGravity(constraints, r);
    }

    protected void AdjustForGravity(GridBagConstraints constraints, Rectangle r) {
        int cellY = r.y;
        int cellHeight = r.height;
        r.x = !this.rightToLeft ? (r.x += constraints.insets.left) : (r.x -= r.width - constraints.insets.right);
        r.width -= constraints.insets.left + constraints.insets.right;
        r.y += constraints.insets.top;
        r.height -= constraints.insets.top + constraints.insets.bottom;
        int diffx = 0;
        if (constraints.fill != 2 && constraints.fill != 1 && r.width > constraints.minWidth + constraints.ipadx) {
            diffx = r.width - (constraints.minWidth + constraints.ipadx);
            r.width = constraints.minWidth + constraints.ipadx;
        }
        int diffy = 0;
        if (constraints.fill != 3 && constraints.fill != 1 && r.height > constraints.minHeight + constraints.ipady) {
            diffy = r.height - (constraints.minHeight + constraints.ipady);
            r.height = constraints.minHeight + constraints.ipady;
        }
        switch (constraints.anchor) {
            case 256: {
                r.x += diffx / 2;
                this.alignOnBaseline(constraints, r, cellY, cellHeight);
                break;
            }
            case 512: {
                if (this.rightToLeft) {
                    r.x += diffx;
                }
                this.alignOnBaseline(constraints, r, cellY, cellHeight);
                break;
            }
            case 768: {
                if (!this.rightToLeft) {
                    r.x += diffx;
                }
                this.alignOnBaseline(constraints, r, cellY, cellHeight);
                break;
            }
            case 1024: {
                r.x += diffx / 2;
                this.alignAboveBaseline(constraints, r, cellY, cellHeight);
                break;
            }
            case 1280: {
                if (this.rightToLeft) {
                    r.x += diffx;
                }
                this.alignAboveBaseline(constraints, r, cellY, cellHeight);
                break;
            }
            case 1536: {
                if (!this.rightToLeft) {
                    r.x += diffx;
                }
                this.alignAboveBaseline(constraints, r, cellY, cellHeight);
                break;
            }
            case 1792: {
                r.x += diffx / 2;
                this.alignBelowBaseline(constraints, r, cellY, cellHeight);
                break;
            }
            case 2048: {
                if (this.rightToLeft) {
                    r.x += diffx;
                }
                this.alignBelowBaseline(constraints, r, cellY, cellHeight);
                break;
            }
            case 2304: {
                if (!this.rightToLeft) {
                    r.x += diffx;
                }
                this.alignBelowBaseline(constraints, r, cellY, cellHeight);
                break;
            }
            case 10: {
                r.x += diffx / 2;
                r.y += diffy / 2;
                break;
            }
            case 11: 
            case 19: {
                r.x += diffx / 2;
                break;
            }
            case 12: {
                r.x += diffx;
                break;
            }
            case 13: {
                r.x += diffx;
                r.y += diffy / 2;
                break;
            }
            case 14: {
                r.x += diffx;
                r.y += diffy;
                break;
            }
            case 15: 
            case 20: {
                r.x += diffx / 2;
                r.y += diffy;
                break;
            }
            case 16: {
                r.y += diffy;
                break;
            }
            case 17: {
                r.y += diffy / 2;
                break;
            }
            case 18: {
                break;
            }
            case 21: {
                if (this.rightToLeft) {
                    r.x += diffx;
                }
                r.y += diffy / 2;
                break;
            }
            case 22: {
                if (!this.rightToLeft) {
                    r.x += diffx;
                }
                r.y += diffy / 2;
                break;
            }
            case 23: {
                if (!this.rightToLeft) break;
                r.x += diffx;
                break;
            }
            case 24: {
                if (this.rightToLeft) break;
                r.x += diffx;
                break;
            }
            case 25: {
                if (this.rightToLeft) {
                    r.x += diffx;
                }
                r.y += diffy;
                break;
            }
            case 26: {
                if (!this.rightToLeft) {
                    r.x += diffx;
                }
                r.y += diffy;
                break;
            }
            default: {
                throw new IllegalArgumentException("illegal anchor value");
            }
        }
    }

    private void alignOnBaseline(GridBagConstraints cons, Rectangle r, int cellY, int cellHeight) {
        if (cons.ascent >= 0) {
            if (cons.baselineResizeBehavior == Component.BaselineResizeBehavior.CONSTANT_DESCENT) {
                int maxY = cellY + cellHeight - this.layoutInfo.maxDescent[cons.tempY + cons.tempHeight - 1] + cons.descent - cons.insets.bottom;
                if (!cons.isVerticallyResizable()) {
                    r.y = maxY - cons.minHeight;
                    r.height = cons.minHeight;
                } else {
                    r.height = maxY - cellY - cons.insets.top;
                }
            } else {
                int ascent = cons.ascent;
                int baseline = this.layoutInfo.hasConstantDescent(cons.tempY) ? cellHeight - this.layoutInfo.maxDescent[cons.tempY] : this.layoutInfo.maxAscent[cons.tempY];
                if (cons.baselineResizeBehavior == Component.BaselineResizeBehavior.OTHER) {
                    boolean fits = false;
                    ascent = this.componentAdjusting.getBaseline(r.width, r.height);
                    if (ascent >= 0) {
                        ascent += cons.insets.top;
                    }
                    if (ascent >= 0 && ascent <= baseline) {
                        if (baseline + (r.height - ascent - cons.insets.top) <= cellHeight - cons.insets.bottom) {
                            fits = true;
                        } else if (cons.isVerticallyResizable()) {
                            int ascent2 = this.componentAdjusting.getBaseline(r.width, cellHeight - cons.insets.bottom - baseline + ascent);
                            if (ascent2 >= 0) {
                                ascent2 += cons.insets.top;
                            }
                            if (ascent2 >= 0 && ascent2 <= ascent) {
                                r.height = cellHeight - cons.insets.bottom - baseline + ascent;
                                ascent = ascent2;
                                fits = true;
                            }
                        }
                    }
                    if (!fits) {
                        ascent = cons.ascent;
                        r.width = cons.minWidth;
                        r.height = cons.minHeight;
                    }
                }
                r.y = cellY + baseline - ascent + cons.insets.top;
                if (cons.isVerticallyResizable()) {
                    switch (cons.baselineResizeBehavior) {
                        case CONSTANT_ASCENT: {
                            r.height = Math.max(cons.minHeight, cellY + cellHeight - r.y - cons.insets.bottom);
                            break;
                        }
                        case CENTER_OFFSET: {
                            int upper = r.y - cellY - cons.insets.top;
                            int lower = cellY + cellHeight - r.y - cons.minHeight - cons.insets.bottom;
                            int delta = Math.min(upper, lower);
                            delta += delta;
                            if (delta > 0 && (cons.minHeight + cons.centerPadding + delta) / 2 + cons.centerOffset != baseline) {
                                --delta;
                            }
                            r.height = cons.minHeight + delta;
                            r.y = cellY + baseline - (r.height + cons.centerPadding) / 2 - cons.centerOffset;
                            break;
                        }
                        case OTHER: {
                            break;
                        }
                    }
                }
            }
        } else {
            this.centerVertically(cons, r, cellHeight);
        }
    }

    private void alignAboveBaseline(GridBagConstraints cons, Rectangle r, int cellY, int cellHeight) {
        if (this.layoutInfo.hasBaseline(cons.tempY)) {
            int maxY = this.layoutInfo.hasConstantDescent(cons.tempY) ? cellY + cellHeight - this.layoutInfo.maxDescent[cons.tempY] : cellY + this.layoutInfo.maxAscent[cons.tempY];
            if (cons.isVerticallyResizable()) {
                r.y = cellY + cons.insets.top;
                r.height = maxY - r.y;
            } else {
                r.height = cons.minHeight + cons.ipady;
                r.y = maxY - r.height;
            }
        } else {
            this.centerVertically(cons, r, cellHeight);
        }
    }

    private void alignBelowBaseline(GridBagConstraints cons, Rectangle r, int cellY, int cellHeight) {
        if (this.layoutInfo.hasBaseline(cons.tempY)) {
            r.y = this.layoutInfo.hasConstantDescent(cons.tempY) ? cellY + cellHeight - this.layoutInfo.maxDescent[cons.tempY] : cellY + this.layoutInfo.maxAscent[cons.tempY];
            if (cons.isVerticallyResizable()) {
                r.height = cellY + cellHeight - r.y - cons.insets.bottom;
            }
        } else {
            this.centerVertically(cons, r, cellHeight);
        }
    }

    private void centerVertically(GridBagConstraints cons, Rectangle r, int cellHeight) {
        if (!cons.isVerticallyResizable()) {
            r.y += Math.max(0, (cellHeight - cons.insets.top - cons.insets.bottom - cons.minHeight - cons.ipady) / 2);
        }
    }

    protected Dimension getMinSize(Container parent, GridBagLayoutInfo info) {
        return this.GetMinSize(parent, info);
    }

    protected Dimension GetMinSize(Container parent, GridBagLayoutInfo info) {
        int i;
        Dimension d = new Dimension();
        Insets insets = parent.getInsets();
        int t = 0;
        for (i = 0; i < info.width; ++i) {
            t += info.minWidth[i];
        }
        d.width = t + insets.left + insets.right;
        t = 0;
        for (i = 0; i < info.height; ++i) {
            t += info.minHeight[i];
        }
        d.height = t + insets.top + insets.bottom;
        return d;
    }

    protected void arrangeGrid(Container parent) {
        this.ArrangeGrid(parent);
    }

    protected void ArrangeGrid(Container parent) {
        int i;
        double weight;
        Insets insets = parent.getInsets();
        Component[] components = parent.getComponents();
        Rectangle r = new Rectangle();
        boolean bl = this.rightToLeft = !parent.getComponentOrientation().isLeftToRight();
        if (!(components.length != 0 || this.columnWidths != null && this.columnWidths.length != 0 || this.rowHeights != null && this.rowHeights.length != 0)) {
            return;
        }
        GridBagLayoutInfo info = this.getLayoutInfo(parent, 2);
        Dimension d = this.getMinSize(parent, info);
        if (parent.width < d.width || parent.height < d.height) {
            info = this.getLayoutInfo(parent, 1);
            d = this.getMinSize(parent, info);
        }
        this.layoutInfo = info;
        r.width = d.width;
        r.height = d.height;
        int diffw = parent.width - r.width;
        if (diffw != 0) {
            weight = 0.0;
            for (i = 0; i < info.width; ++i) {
                weight += info.weightX[i];
            }
            if (weight > 0.0) {
                for (i = 0; i < info.width; ++i) {
                    int dx = (int)((double)diffw * info.weightX[i] / weight);
                    int n = i;
                    info.minWidth[n] = info.minWidth[n] + dx;
                    r.width += dx;
                    if (info.minWidth[i] >= 0) continue;
                    r.width -= info.minWidth[i];
                    info.minWidth[i] = 0;
                }
            }
            diffw = parent.width - r.width;
        } else {
            diffw = 0;
        }
        int diffh = parent.height - r.height;
        if (diffh != 0) {
            weight = 0.0;
            for (i = 0; i < info.height; ++i) {
                weight += info.weightY[i];
            }
            if (weight > 0.0) {
                for (i = 0; i < info.height; ++i) {
                    int dy = (int)((double)diffh * info.weightY[i] / weight);
                    int n = i;
                    info.minHeight[n] = info.minHeight[n] + dy;
                    r.height += dy;
                    if (info.minHeight[i] >= 0) continue;
                    r.height -= info.minHeight[i];
                    info.minHeight[i] = 0;
                }
            }
            diffh = parent.height - r.height;
        } else {
            diffh = 0;
        }
        info.startx = diffw / 2 + insets.left;
        info.starty = diffh / 2 + insets.top;
        for (int compindex = 0; compindex < components.length; ++compindex) {
            Component comp = components[compindex];
            if (!comp.isVisible()) continue;
            GridBagConstraints constraints = this.lookupConstraints(comp);
            if (!this.rightToLeft) {
                r.x = info.startx;
                for (i = 0; i < constraints.tempX; ++i) {
                    r.x += info.minWidth[i];
                }
            } else {
                r.x = parent.width - (diffw / 2 + insets.right);
                for (i = 0; i < constraints.tempX; ++i) {
                    r.x -= info.minWidth[i];
                }
            }
            r.y = info.starty;
            for (i = 0; i < constraints.tempY; ++i) {
                r.y += info.minHeight[i];
            }
            r.width = 0;
            for (i = constraints.tempX; i < constraints.tempX + constraints.tempWidth; ++i) {
                r.width += info.minWidth[i];
            }
            r.height = 0;
            for (i = constraints.tempY; i < constraints.tempY + constraints.tempHeight; ++i) {
                r.height += info.minHeight[i];
            }
            this.componentAdjusting = comp;
            this.adjustForGravity(constraints, r);
            if (r.x < 0) {
                r.width += r.x;
                r.x = 0;
            }
            if (r.y < 0) {
                r.height += r.y;
                r.y = 0;
            }
            if (r.width <= 0 || r.height <= 0) {
                comp.setBounds(0, 0, 0, 0);
                continue;
            }
            if (comp.x == r.x && comp.y == r.y && comp.width == r.width && comp.height == r.height) continue;
            comp.setBounds(r.x, r.y, r.width, r.height);
        }
    }
}

