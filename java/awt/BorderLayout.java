/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.io.Serializable;

public class BorderLayout
implements LayoutManager2,
Serializable {
    int hgap;
    int vgap;
    Component north;
    Component west;
    Component east;
    Component south;
    Component center;
    Component firstLine;
    Component lastLine;
    Component firstItem;
    Component lastItem;
    public static final String NORTH = "North";
    public static final String SOUTH = "South";
    public static final String EAST = "East";
    public static final String WEST = "West";
    public static final String CENTER = "Center";
    public static final String BEFORE_FIRST_LINE = "First";
    public static final String AFTER_LAST_LINE = "Last";
    public static final String BEFORE_LINE_BEGINS = "Before";
    public static final String AFTER_LINE_ENDS = "After";
    public static final String PAGE_START = "First";
    public static final String PAGE_END = "Last";
    public static final String LINE_START = "Before";
    public static final String LINE_END = "After";
    private static final long serialVersionUID = -8658291919501921765L;

    public BorderLayout() {
        this(0, 0);
    }

    public BorderLayout(int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        Object object = comp.getTreeLock();
        synchronized (object) {
            if (constraints != null && !(constraints instanceof String)) {
                throw new IllegalArgumentException("cannot add to layout: constraint must be a string (or null)");
            }
            this.addLayoutComponent((String)constraints, comp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    @Deprecated
    public void addLayoutComponent(String name, Component comp) {
        Object object = comp.getTreeLock();
        synchronized (object) {
            if (name == null) {
                name = CENTER;
            }
            if (CENTER.equals(name)) {
                this.center = comp;
            } else if (NORTH.equals(name)) {
                this.north = comp;
            } else if (SOUTH.equals(name)) {
                this.south = comp;
            } else if (EAST.equals(name)) {
                this.east = comp;
            } else if (WEST.equals(name)) {
                this.west = comp;
            } else if ("First".equals(name)) {
                this.firstLine = comp;
            } else if ("Last".equals(name)) {
                this.lastLine = comp;
            } else if ("Before".equals(name)) {
                this.firstItem = comp;
            } else if ("After".equals(name)) {
                this.lastItem = comp;
            } else {
                throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + name);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeLayoutComponent(Component comp) {
        Object object = comp.getTreeLock();
        synchronized (object) {
            if (comp == this.center) {
                this.center = null;
            } else if (comp == this.north) {
                this.north = null;
            } else if (comp == this.south) {
                this.south = null;
            } else if (comp == this.east) {
                this.east = null;
            } else if (comp == this.west) {
                this.west = null;
            }
            if (comp == this.firstLine) {
                this.firstLine = null;
            } else if (comp == this.lastLine) {
                this.lastLine = null;
            } else if (comp == this.firstItem) {
                this.firstItem = null;
            } else if (comp == this.lastItem) {
                this.lastItem = null;
            }
        }
    }

    public Component getLayoutComponent(Object constraints) {
        if (CENTER.equals(constraints)) {
            return this.center;
        }
        if (NORTH.equals(constraints)) {
            return this.north;
        }
        if (SOUTH.equals(constraints)) {
            return this.south;
        }
        if (WEST.equals(constraints)) {
            return this.west;
        }
        if (EAST.equals(constraints)) {
            return this.east;
        }
        if ("First".equals(constraints)) {
            return this.firstLine;
        }
        if ("Last".equals(constraints)) {
            return this.lastLine;
        }
        if ("Before".equals(constraints)) {
            return this.firstItem;
        }
        if ("After".equals(constraints)) {
            return this.lastItem;
        }
        throw new IllegalArgumentException("cannot get component: unknown constraint: " + String.valueOf(constraints));
    }

    public Component getLayoutComponent(Container target, Object constraints) {
        boolean ltr = target.getComponentOrientation().isLeftToRight();
        Component result = null;
        if (NORTH.equals(constraints)) {
            result = this.firstLine != null ? this.firstLine : this.north;
        } else if (SOUTH.equals(constraints)) {
            result = this.lastLine != null ? this.lastLine : this.south;
        } else if (WEST.equals(constraints)) {
            Component component = result = ltr ? this.firstItem : this.lastItem;
            if (result == null) {
                result = this.west;
            }
        } else if (EAST.equals(constraints)) {
            Component component = result = ltr ? this.lastItem : this.firstItem;
            if (result == null) {
                result = this.east;
            }
        } else if (CENTER.equals(constraints)) {
            result = this.center;
        } else {
            throw new IllegalArgumentException("cannot get component: invalid constraint: " + String.valueOf(constraints));
        }
        return result;
    }

    public Object getConstraints(Component comp) {
        if (comp == null) {
            return null;
        }
        if (comp == this.center) {
            return CENTER;
        }
        if (comp == this.north) {
            return NORTH;
        }
        if (comp == this.south) {
            return SOUTH;
        }
        if (comp == this.west) {
            return WEST;
        }
        if (comp == this.east) {
            return EAST;
        }
        if (comp == this.firstLine) {
            return "First";
        }
        if (comp == this.lastLine) {
            return "Last";
        }
        if (comp == this.firstItem) {
            return "Before";
        }
        if (comp == this.lastItem) {
            return "After";
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension minimumLayoutSize(Container target) {
        Object object = target.getTreeLock();
        synchronized (object) {
            Dimension d;
            Dimension dim = new Dimension(0, 0);
            boolean ltr = target.getComponentOrientation().isLeftToRight();
            Component c = null;
            c = this.getChild(EAST, ltr);
            if (c != null) {
                d = c.getMinimumSize();
                dim.width += d.width + this.hgap;
                dim.height = Math.max(d.height, dim.height);
            }
            if ((c = this.getChild(WEST, ltr)) != null) {
                d = c.getMinimumSize();
                dim.width += d.width + this.hgap;
                dim.height = Math.max(d.height, dim.height);
            }
            if ((c = this.getChild(CENTER, ltr)) != null) {
                d = c.getMinimumSize();
                dim.width += d.width;
                dim.height = Math.max(d.height, dim.height);
            }
            if ((c = this.getChild(NORTH, ltr)) != null) {
                d = c.getMinimumSize();
                dim.width = Math.max(d.width, dim.width);
                dim.height += d.height + this.vgap;
            }
            if ((c = this.getChild(SOUTH, ltr)) != null) {
                d = c.getMinimumSize();
                dim.width = Math.max(d.width, dim.width);
                dim.height += d.height + this.vgap;
            }
            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;
            return dim;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension preferredLayoutSize(Container target) {
        Object object = target.getTreeLock();
        synchronized (object) {
            Dimension d;
            Dimension dim = new Dimension(0, 0);
            boolean ltr = target.getComponentOrientation().isLeftToRight();
            Component c = null;
            c = this.getChild(EAST, ltr);
            if (c != null) {
                d = c.getPreferredSize();
                dim.width += d.width + this.hgap;
                dim.height = Math.max(d.height, dim.height);
            }
            if ((c = this.getChild(WEST, ltr)) != null) {
                d = c.getPreferredSize();
                dim.width += d.width + this.hgap;
                dim.height = Math.max(d.height, dim.height);
            }
            if ((c = this.getChild(CENTER, ltr)) != null) {
                d = c.getPreferredSize();
                dim.width += d.width;
                dim.height = Math.max(d.height, dim.height);
            }
            if ((c = this.getChild(NORTH, ltr)) != null) {
                d = c.getPreferredSize();
                dim.width = Math.max(d.width, dim.width);
                dim.height += d.height + this.vgap;
            }
            if ((c = this.getChild(SOUTH, ltr)) != null) {
                d = c.getPreferredSize();
                dim.width = Math.max(d.width, dim.width);
                dim.height += d.height + this.vgap;
            }
            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;
            return dim;
        }
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void layoutContainer(Container target) {
        Object object = target.getTreeLock();
        synchronized (object) {
            Dimension d;
            Insets insets = target.getInsets();
            int top = insets.top;
            int bottom = target.height - insets.bottom;
            int left = insets.left;
            int right = target.width - insets.right;
            boolean ltr = target.getComponentOrientation().isLeftToRight();
            Component c = null;
            c = this.getChild(NORTH, ltr);
            if (c != null) {
                c.setSize(right - left, c.height);
                d = c.getPreferredSize();
                c.setBounds(left, top, right - left, d.height);
                top += d.height + this.vgap;
            }
            if ((c = this.getChild(SOUTH, ltr)) != null) {
                c.setSize(right - left, c.height);
                d = c.getPreferredSize();
                c.setBounds(left, bottom - d.height, right - left, d.height);
                bottom -= d.height + this.vgap;
            }
            if ((c = this.getChild(EAST, ltr)) != null) {
                c.setSize(c.width, bottom - top);
                d = c.getPreferredSize();
                c.setBounds(right - d.width, top, d.width, bottom - top);
                right -= d.width + this.hgap;
            }
            if ((c = this.getChild(WEST, ltr)) != null) {
                c.setSize(c.width, bottom - top);
                d = c.getPreferredSize();
                c.setBounds(left, top, d.width, bottom - top);
                left += d.width + this.hgap;
            }
            if ((c = this.getChild(CENTER, ltr)) != null) {
                c.setBounds(left, top, right - left, bottom - top);
            }
        }
    }

    private Component getChild(String key, boolean ltr) {
        Component result = null;
        if (key == NORTH) {
            result = this.firstLine != null ? this.firstLine : this.north;
        } else if (key == SOUTH) {
            result = this.lastLine != null ? this.lastLine : this.south;
        } else if (key == WEST) {
            Component component = result = ltr ? this.firstItem : this.lastItem;
            if (result == null) {
                result = this.west;
            }
        } else if (key == EAST) {
            Component component = result = ltr ? this.lastItem : this.firstItem;
            if (result == null) {
                result = this.east;
            }
        } else if (key == CENTER) {
            result = this.center;
        }
        if (result != null && !result.visible) {
            result = null;
        }
        return result;
    }

    public String toString() {
        return this.getClass().getName() + "[hgap=" + this.hgap + ",vgap=" + this.vgap + "]";
    }
}

