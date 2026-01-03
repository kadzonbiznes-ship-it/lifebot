/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.Serializable;

public class GridLayout
implements LayoutManager,
Serializable {
    private static final long serialVersionUID = -7411804673224730901L;
    int hgap;
    int vgap;
    int rows;
    int cols;

    public GridLayout() {
        this(1, 0, 0, 0);
    }

    public GridLayout(int rows, int cols) {
        this(rows, cols, 0, 0);
    }

    public GridLayout(int rows, int cols, int hgap, int vgap) {
        if (rows == 0 && cols == 0) {
            throw new IllegalArgumentException("rows and cols cannot both be zero");
        }
        this.rows = rows;
        this.cols = cols;
        this.hgap = hgap;
        this.vgap = vgap;
    }

    public int getRows() {
        return this.rows;
    }

    public void setRows(int rows) {
        if (rows == 0 && this.cols == 0) {
            throw new IllegalArgumentException("rows and cols cannot both be zero");
        }
        this.rows = rows;
    }

    public int getColumns() {
        return this.cols;
    }

    public void setColumns(int cols) {
        if (cols == 0 && this.rows == 0) {
            throw new IllegalArgumentException("rows and cols cannot both be zero");
        }
        this.cols = cols;
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
    public Dimension preferredLayoutSize(Container parent) {
        Object object = parent.getTreeLock();
        synchronized (object) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = this.rows;
            int ncols = this.cols;
            if (nrows > 0) {
                ncols = (ncomponents + nrows - 1) / nrows;
            } else {
                nrows = (ncomponents + ncols - 1) / ncols;
            }
            int w = 0;
            int h = 0;
            for (int i = 0; i < ncomponents; ++i) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getPreferredSize();
                if (w < d.width) {
                    w = d.width;
                }
                if (h >= d.height) continue;
                h = d.height;
            }
            return new Dimension(insets.left + insets.right + ncols * w + (ncols - 1) * this.hgap, insets.top + insets.bottom + nrows * h + (nrows - 1) * this.vgap);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Object object = parent.getTreeLock();
        synchronized (object) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = this.rows;
            int ncols = this.cols;
            if (nrows > 0) {
                ncols = (ncomponents + nrows - 1) / nrows;
            } else {
                nrows = (ncomponents + ncols - 1) / ncols;
            }
            int w = 0;
            int h = 0;
            for (int i = 0; i < ncomponents; ++i) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getMinimumSize();
                if (w < d.width) {
                    w = d.width;
                }
                if (h >= d.height) continue;
                h = d.height;
            }
            return new Dimension(insets.left + insets.right + ncols * w + (ncols - 1) * this.hgap, insets.top + insets.bottom + nrows * h + (nrows - 1) * this.vgap);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void layoutContainer(Container parent) {
        Object object = parent.getTreeLock();
        synchronized (object) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = this.rows;
            int ncols = this.cols;
            boolean ltr = parent.getComponentOrientation().isLeftToRight();
            if (ncomponents == 0) {
                return;
            }
            if (nrows > 0) {
                ncols = (ncomponents + nrows - 1) / nrows;
            } else {
                nrows = (ncomponents + ncols - 1) / ncols;
            }
            int totalGapsWidth = (ncols - 1) * this.hgap;
            int widthWOInsets = parent.width - (insets.left + insets.right);
            int widthOnComponent = (widthWOInsets - totalGapsWidth) / ncols;
            int extraWidthAvailable = (widthWOInsets - (widthOnComponent * ncols + totalGapsWidth)) / 2;
            int totalGapsHeight = (nrows - 1) * this.vgap;
            int heightWOInsets = parent.height - (insets.top + insets.bottom);
            int heightOnComponent = (heightWOInsets - totalGapsHeight) / nrows;
            int extraHeightAvailable = (heightWOInsets - (heightOnComponent * nrows + totalGapsHeight)) / 2;
            if (ltr) {
                int c = 0;
                int x = insets.left + extraWidthAvailable;
                while (c < ncols) {
                    int r = 0;
                    int y = insets.top + extraHeightAvailable;
                    while (r < nrows) {
                        int i = r * ncols + c;
                        if (i < ncomponents) {
                            parent.getComponent(i).setBounds(x, y, widthOnComponent, heightOnComponent);
                        }
                        ++r;
                        y += heightOnComponent + this.vgap;
                    }
                    ++c;
                    x += widthOnComponent + this.hgap;
                }
            } else {
                int c = 0;
                int x = parent.width - insets.right - widthOnComponent - extraWidthAvailable;
                while (c < ncols) {
                    int r = 0;
                    int y = insets.top + extraHeightAvailable;
                    while (r < nrows) {
                        int i = r * ncols + c;
                        if (i < ncomponents) {
                            parent.getComponent(i).setBounds(x, y, widthOnComponent, heightOnComponent);
                        }
                        ++r;
                        y += heightOnComponent + this.vgap;
                    }
                    ++c;
                    x -= widthOnComponent + this.hgap;
                }
            }
        }
    }

    public String toString() {
        return this.getClass().getName() + "[hgap=" + this.hgap + ",vgap=" + this.vgap + ",rows=" + this.rows + ",cols=" + this.cols + "]";
    }
}

