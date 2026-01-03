/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Component;
import java.awt.Insets;
import java.io.Serializable;

public class GridBagConstraints
implements Cloneable,
Serializable {
    public static final int RELATIVE = -1;
    public static final int REMAINDER = 0;
    public static final int NONE = 0;
    public static final int BOTH = 1;
    public static final int HORIZONTAL = 2;
    public static final int VERTICAL = 3;
    public static final int CENTER = 10;
    public static final int NORTH = 11;
    public static final int NORTHEAST = 12;
    public static final int EAST = 13;
    public static final int SOUTHEAST = 14;
    public static final int SOUTH = 15;
    public static final int SOUTHWEST = 16;
    public static final int WEST = 17;
    public static final int NORTHWEST = 18;
    public static final int PAGE_START = 19;
    public static final int PAGE_END = 20;
    public static final int LINE_START = 21;
    public static final int LINE_END = 22;
    public static final int FIRST_LINE_START = 23;
    public static final int FIRST_LINE_END = 24;
    public static final int LAST_LINE_START = 25;
    public static final int LAST_LINE_END = 26;
    public static final int BASELINE = 256;
    public static final int BASELINE_LEADING = 512;
    public static final int BASELINE_TRAILING = 768;
    public static final int ABOVE_BASELINE = 1024;
    public static final int ABOVE_BASELINE_LEADING = 1280;
    public static final int ABOVE_BASELINE_TRAILING = 1536;
    public static final int BELOW_BASELINE = 1792;
    public static final int BELOW_BASELINE_LEADING = 2048;
    public static final int BELOW_BASELINE_TRAILING = 2304;
    public int gridx;
    public int gridy;
    public int gridwidth;
    public int gridheight;
    public double weightx;
    public double weighty;
    public int anchor;
    public int fill;
    public Insets insets;
    public int ipadx;
    public int ipady;
    int tempX;
    int tempY;
    int tempWidth;
    int tempHeight;
    int minWidth;
    int minHeight;
    transient int ascent;
    transient int descent;
    transient Component.BaselineResizeBehavior baselineResizeBehavior;
    transient int centerPadding;
    transient int centerOffset;
    private static final long serialVersionUID = -1000070633030801713L;

    public GridBagConstraints() {
        this.gridx = -1;
        this.gridy = -1;
        this.gridwidth = 1;
        this.gridheight = 1;
        this.weightx = 0.0;
        this.weighty = 0.0;
        this.anchor = 10;
        this.fill = 0;
        this.insets = new Insets(0, 0, 0, 0);
        this.ipadx = 0;
        this.ipady = 0;
    }

    public GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady) {
        this.gridx = gridx;
        this.gridy = gridy;
        this.gridwidth = gridwidth;
        this.gridheight = gridheight;
        this.fill = fill;
        this.ipadx = ipadx;
        this.ipady = ipady;
        this.insets = insets;
        this.anchor = anchor;
        this.weightx = weightx;
        this.weighty = weighty;
    }

    public Object clone() {
        try {
            GridBagConstraints c = (GridBagConstraints)super.clone();
            c.insets = (Insets)this.insets.clone();
            return c;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    boolean isVerticallyResizable() {
        return this.fill == 1 || this.fill == 3;
    }
}

