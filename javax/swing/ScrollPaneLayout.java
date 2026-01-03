/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.io.Serializable;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class ScrollPaneLayout
implements LayoutManager,
ScrollPaneConstants,
Serializable {
    protected JViewport viewport;
    protected JScrollBar vsb;
    protected JScrollBar hsb;
    protected JViewport rowHead;
    protected JViewport colHead;
    protected Component lowerLeft;
    protected Component lowerRight;
    protected Component upperLeft;
    protected Component upperRight;
    protected int vsbPolicy = 20;
    protected int hsbPolicy = 30;

    public void syncWithScrollPane(JScrollPane sp) {
        this.viewport = sp.getViewport();
        this.vsb = sp.getVerticalScrollBar();
        this.hsb = sp.getHorizontalScrollBar();
        this.rowHead = sp.getRowHeader();
        this.colHead = sp.getColumnHeader();
        this.lowerLeft = sp.getCorner("LOWER_LEFT_CORNER");
        this.lowerRight = sp.getCorner("LOWER_RIGHT_CORNER");
        this.upperLeft = sp.getCorner("UPPER_LEFT_CORNER");
        this.upperRight = sp.getCorner("UPPER_RIGHT_CORNER");
        this.vsbPolicy = sp.getVerticalScrollBarPolicy();
        this.hsbPolicy = sp.getHorizontalScrollBarPolicy();
    }

    protected Component addSingletonComponent(Component oldC, Component newC) {
        if (oldC != null && oldC != newC) {
            oldC.getParent().remove(oldC);
        }
        return newC;
    }

    @Override
    public void addLayoutComponent(String s, Component c) {
        if (s.equals("VIEWPORT")) {
            this.viewport = (JViewport)this.addSingletonComponent(this.viewport, c);
        } else if (s.equals("VERTICAL_SCROLLBAR")) {
            this.vsb = (JScrollBar)this.addSingletonComponent(this.vsb, c);
        } else if (s.equals("HORIZONTAL_SCROLLBAR")) {
            this.hsb = (JScrollBar)this.addSingletonComponent(this.hsb, c);
        } else if (s.equals("ROW_HEADER")) {
            this.rowHead = (JViewport)this.addSingletonComponent(this.rowHead, c);
        } else if (s.equals("COLUMN_HEADER")) {
            this.colHead = (JViewport)this.addSingletonComponent(this.colHead, c);
        } else if (s.equals("LOWER_LEFT_CORNER")) {
            this.lowerLeft = this.addSingletonComponent(this.lowerLeft, c);
        } else if (s.equals("LOWER_RIGHT_CORNER")) {
            this.lowerRight = this.addSingletonComponent(this.lowerRight, c);
        } else if (s.equals("UPPER_LEFT_CORNER")) {
            this.upperLeft = this.addSingletonComponent(this.upperLeft, c);
        } else if (s.equals("UPPER_RIGHT_CORNER")) {
            this.upperRight = this.addSingletonComponent(this.upperRight, c);
        } else {
            throw new IllegalArgumentException("invalid layout key " + s);
        }
    }

    @Override
    public void removeLayoutComponent(Component c) {
        if (c == this.viewport) {
            this.viewport = null;
        } else if (c == this.vsb) {
            this.vsb = null;
        } else if (c == this.hsb) {
            this.hsb = null;
        } else if (c == this.rowHead) {
            this.rowHead = null;
        } else if (c == this.colHead) {
            this.colHead = null;
        } else if (c == this.lowerLeft) {
            this.lowerLeft = null;
        } else if (c == this.lowerRight) {
            this.lowerRight = null;
        } else if (c == this.upperLeft) {
            this.upperLeft = null;
        } else if (c == this.upperRight) {
            this.upperRight = null;
        }
    }

    public int getVerticalScrollBarPolicy() {
        return this.vsbPolicy;
    }

    public void setVerticalScrollBarPolicy(int x) {
        switch (x) {
            case 20: 
            case 21: 
            case 22: {
                this.vsbPolicy = x;
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid verticalScrollBarPolicy");
            }
        }
    }

    public int getHorizontalScrollBarPolicy() {
        return this.hsbPolicy;
    }

    public void setHorizontalScrollBarPolicy(int x) {
        switch (x) {
            case 30: 
            case 31: 
            case 32: {
                this.hsbPolicy = x;
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid horizontalScrollBarPolicy");
            }
        }
    }

    public JViewport getViewport() {
        return this.viewport;
    }

    public JScrollBar getHorizontalScrollBar() {
        return this.hsb;
    }

    public JScrollBar getVerticalScrollBar() {
        return this.vsb;
    }

    public JViewport getRowHeader() {
        return this.rowHead;
    }

    public JViewport getColumnHeader() {
        return this.colHead;
    }

    public Component getCorner(String key) {
        if (key.equals("LOWER_LEFT_CORNER")) {
            return this.lowerLeft;
        }
        if (key.equals("LOWER_RIGHT_CORNER")) {
            return this.lowerRight;
        }
        if (key.equals("UPPER_LEFT_CORNER")) {
            return this.upperLeft;
        }
        if (key.equals("UPPER_RIGHT_CORNER")) {
            return this.upperRight;
        }
        return null;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Border viewportBorder;
        JScrollPane scrollPane = (JScrollPane)parent;
        this.vsbPolicy = scrollPane.getVerticalScrollBarPolicy();
        this.hsbPolicy = scrollPane.getHorizontalScrollBarPolicy();
        Insets insets = parent.getInsets();
        int prefWidth = insets.left + insets.right;
        int prefHeight = insets.top + insets.bottom;
        Dimension extentSize = null;
        Dimension viewSize = null;
        Component view = null;
        if (this.viewport != null) {
            extentSize = this.viewport.getPreferredSize();
            view = this.viewport.getView();
            viewSize = view != null ? view.getPreferredSize() : new Dimension(0, 0);
        }
        if (extentSize != null) {
            prefWidth += extentSize.width;
            prefHeight += extentSize.height;
        }
        if ((viewportBorder = scrollPane.getViewportBorder()) != null) {
            Insets vpbInsets = viewportBorder.getBorderInsets(parent);
            prefWidth += vpbInsets.left + vpbInsets.right;
            prefHeight += vpbInsets.top + vpbInsets.bottom;
        }
        if (this.rowHead != null && this.rowHead.isVisible()) {
            prefWidth += this.rowHead.getPreferredSize().width;
        }
        if (this.colHead != null && this.colHead.isVisible()) {
            prefHeight += this.colHead.getPreferredSize().height;
        }
        if (this.vsb != null && this.vsbPolicy != 21) {
            if (this.vsbPolicy == 22) {
                prefWidth += this.vsb.getPreferredSize().width;
            } else if (viewSize != null && extentSize != null) {
                boolean canScroll = true;
                if (view instanceof Scrollable) {
                    boolean bl = canScroll = !((Scrollable)((Object)view)).getScrollableTracksViewportHeight();
                }
                if (canScroll && viewSize.height > extentSize.height) {
                    prefWidth += this.vsb.getPreferredSize().width;
                }
            }
        }
        if (this.hsb != null && this.hsbPolicy != 31) {
            if (this.hsbPolicy == 32) {
                prefHeight += this.hsb.getPreferredSize().height;
            } else if (viewSize != null && extentSize != null) {
                boolean canScroll = true;
                if (view instanceof Scrollable) {
                    boolean bl = canScroll = !((Scrollable)((Object)view)).getScrollableTracksViewportWidth();
                }
                if (canScroll && viewSize.width > extentSize.width) {
                    prefHeight += this.hsb.getPreferredSize().height;
                }
            }
        }
        return new Dimension(prefWidth, prefHeight);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Dimension size;
        Border viewportBorder;
        JScrollPane scrollPane = (JScrollPane)parent;
        this.vsbPolicy = scrollPane.getVerticalScrollBarPolicy();
        this.hsbPolicy = scrollPane.getHorizontalScrollBarPolicy();
        Insets insets = parent.getInsets();
        int minWidth = insets.left + insets.right;
        int minHeight = insets.top + insets.bottom;
        if (this.viewport != null) {
            Dimension size2 = this.viewport.getMinimumSize();
            minWidth += size2.width;
            minHeight += size2.height;
        }
        if ((viewportBorder = scrollPane.getViewportBorder()) != null) {
            Insets vpbInsets = viewportBorder.getBorderInsets(parent);
            minWidth += vpbInsets.left + vpbInsets.right;
            minHeight += vpbInsets.top + vpbInsets.bottom;
        }
        if (this.rowHead != null && this.rowHead.isVisible()) {
            size = this.rowHead.getMinimumSize();
            minWidth += size.width;
            minHeight = Math.max(minHeight, size.height);
        }
        if (this.colHead != null && this.colHead.isVisible()) {
            size = this.colHead.getMinimumSize();
            minWidth = Math.max(minWidth, size.width);
            minHeight += size.height;
        }
        if (this.vsb != null && this.vsbPolicy != 21) {
            size = this.vsb.getMinimumSize();
            minWidth += size.width;
            minHeight = Math.max(minHeight, size.height);
        }
        if (this.hsb != null && this.hsbPolicy != 31) {
            size = this.hsb.getMinimumSize();
            minWidth = Math.max(minWidth, size.width);
            minHeight += size.height;
        }
        return new Dimension(minWidth, minHeight);
    }

    @Override
    public void layoutContainer(Container parent) {
        boolean hsbNeeded;
        boolean vsbNeeded;
        Scrollable sv;
        boolean isEmpty;
        Insets vpbInsets;
        Border viewportBorder;
        JScrollPane scrollPane = (JScrollPane)parent;
        this.vsbPolicy = scrollPane.getVerticalScrollBarPolicy();
        this.hsbPolicy = scrollPane.getHorizontalScrollBarPolicy();
        Rectangle availR = scrollPane.getBounds();
        availR.y = 0;
        availR.x = 0;
        Insets insets = parent.getInsets();
        availR.x = insets.left;
        availR.y = insets.top;
        availR.width -= insets.left + insets.right;
        availR.height -= insets.top + insets.bottom;
        boolean leftToRight = SwingUtilities.isLeftToRight(scrollPane);
        Rectangle colHeadR = new Rectangle(0, availR.y, 0, 0);
        if (this.colHead != null && this.colHead.isVisible()) {
            int colHeadHeight;
            colHeadR.height = colHeadHeight = Math.min(availR.height, this.colHead.getPreferredSize().height);
            availR.y += colHeadHeight;
            availR.height -= colHeadHeight;
        }
        Rectangle rowHeadR = new Rectangle(0, 0, 0, 0);
        if (this.rowHead != null && this.rowHead.isVisible()) {
            int rowHeadWidth;
            rowHeadR.width = rowHeadWidth = Math.min(availR.width, this.rowHead.getPreferredSize().width);
            availR.width -= rowHeadWidth;
            if (leftToRight) {
                rowHeadR.x = availR.x;
                availR.x += rowHeadWidth;
            } else {
                rowHeadR.x = availR.x + availR.width;
            }
        }
        if ((viewportBorder = scrollPane.getViewportBorder()) != null) {
            vpbInsets = viewportBorder.getBorderInsets(parent);
            availR.x += vpbInsets.left;
            availR.y += vpbInsets.top;
            availR.width -= vpbInsets.left + vpbInsets.right;
            availR.height -= vpbInsets.top + vpbInsets.bottom;
        } else {
            vpbInsets = new Insets(0, 0, 0, 0);
        }
        Component view = this.viewport != null ? this.viewport.getView() : null;
        Dimension viewPrefSize = view != null ? view.getPreferredSize() : new Dimension(0, 0);
        Dimension extentSize = this.viewport != null ? this.viewport.toViewCoordinates(availR.getSize()) : new Dimension(0, 0);
        boolean viewTracksViewportWidth = false;
        boolean viewTracksViewportHeight = false;
        boolean bl = isEmpty = availR.width < 0 || availR.height < 0;
        if (!isEmpty && view instanceof Scrollable) {
            sv = (Scrollable)((Object)view);
            viewTracksViewportWidth = sv.getScrollableTracksViewportWidth();
            viewTracksViewportHeight = sv.getScrollableTracksViewportHeight();
        } else {
            sv = null;
        }
        Rectangle vsbR = new Rectangle(0, availR.y - vpbInsets.top, 0, 0);
        if (this.vsbPolicy == 22) {
            vsbNeeded = true;
        } else if (this.vsbPolicy == 21) {
            vsbNeeded = false;
        } else {
            boolean bl2 = vsbNeeded = !viewTracksViewportHeight && viewPrefSize.height > extentSize.height;
        }
        if (this.vsb != null && vsbNeeded) {
            this.adjustForVSB(true, availR, vsbR, vpbInsets, leftToRight);
            extentSize = this.viewport.toViewCoordinates(availR.getSize());
        }
        Rectangle hsbR = new Rectangle(availR.x - vpbInsets.left, 0, 0, 0);
        if (this.hsbPolicy == 32) {
            hsbNeeded = true;
        } else if (this.hsbPolicy == 31) {
            hsbNeeded = false;
        } else {
            boolean bl3 = hsbNeeded = !viewTracksViewportWidth && viewPrefSize.width > extentSize.width;
        }
        if (this.hsb != null && hsbNeeded) {
            this.adjustForHSB(true, availR, hsbR, vpbInsets);
            if (this.vsb != null && !vsbNeeded && this.vsbPolicy != 21) {
                extentSize = this.viewport.toViewCoordinates(availR.getSize());
                boolean bl4 = vsbNeeded = viewPrefSize.height > extentSize.height;
                if (vsbNeeded) {
                    this.adjustForVSB(true, availR, vsbR, vpbInsets, leftToRight);
                }
            }
        }
        if (this.viewport != null) {
            this.viewport.setBounds(availR);
            if (sv != null) {
                extentSize = this.viewport.toViewCoordinates(availR.getSize());
                boolean oldHSBNeeded = hsbNeeded;
                boolean oldVSBNeeded = vsbNeeded;
                viewTracksViewportWidth = sv.getScrollableTracksViewportWidth();
                viewTracksViewportHeight = sv.getScrollableTracksViewportHeight();
                if (this.vsb != null && this.vsbPolicy == 20) {
                    boolean newVSBNeeded;
                    boolean bl5 = newVSBNeeded = !viewTracksViewportHeight && viewPrefSize.height > extentSize.height;
                    if (newVSBNeeded != vsbNeeded) {
                        vsbNeeded = newVSBNeeded;
                        this.adjustForVSB(vsbNeeded, availR, vsbR, vpbInsets, leftToRight);
                        extentSize = this.viewport.toViewCoordinates(availR.getSize());
                    }
                }
                if (this.hsb != null && this.hsbPolicy == 30) {
                    boolean newHSBbNeeded;
                    boolean bl6 = newHSBbNeeded = !viewTracksViewportWidth && viewPrefSize.width > extentSize.width;
                    if (newHSBbNeeded != hsbNeeded) {
                        hsbNeeded = newHSBbNeeded;
                        this.adjustForHSB(hsbNeeded, availR, hsbR, vpbInsets);
                        if (this.vsb != null && !vsbNeeded && this.vsbPolicy != 21) {
                            extentSize = this.viewport.toViewCoordinates(availR.getSize());
                            boolean bl7 = vsbNeeded = viewPrefSize.height > extentSize.height;
                            if (vsbNeeded) {
                                this.adjustForVSB(true, availR, vsbR, vpbInsets, leftToRight);
                            }
                        }
                    }
                }
                if (oldHSBNeeded != hsbNeeded || oldVSBNeeded != vsbNeeded) {
                    this.viewport.setBounds(availR);
                }
            }
        }
        vsbR.height = availR.height + vpbInsets.top + vpbInsets.bottom;
        hsbR.width = availR.width + vpbInsets.left + vpbInsets.right;
        rowHeadR.height = availR.height + vpbInsets.top + vpbInsets.bottom;
        rowHeadR.y = availR.y - vpbInsets.top;
        colHeadR.width = availR.width + vpbInsets.left + vpbInsets.right;
        colHeadR.x = availR.x - vpbInsets.left;
        if (this.rowHead != null) {
            this.rowHead.setBounds(rowHeadR);
        }
        if (this.colHead != null) {
            this.colHead.setBounds(colHeadR);
        }
        if (this.vsb != null) {
            if (vsbNeeded) {
                if (this.colHead != null && UIManager.getBoolean("ScrollPane.fillUpperCorner") && (leftToRight && this.upperRight == null || !leftToRight && this.upperLeft == null)) {
                    vsbR.y = colHeadR.y;
                    vsbR.height += colHeadR.height;
                }
                this.vsb.setVisible(true);
                this.vsb.setBounds(vsbR);
            } else {
                this.vsb.setVisible(false);
            }
        }
        if (this.hsb != null) {
            if (hsbNeeded) {
                if (this.rowHead != null && UIManager.getBoolean("ScrollPane.fillLowerCorner") && (leftToRight && this.lowerLeft == null || !leftToRight && this.lowerRight == null)) {
                    if (leftToRight) {
                        hsbR.x = rowHeadR.x;
                    }
                    hsbR.width += rowHeadR.width;
                }
                this.hsb.setVisible(true);
                this.hsb.setBounds(hsbR);
            } else {
                this.hsb.setVisible(false);
            }
        }
        if (this.lowerLeft != null) {
            this.lowerLeft.setBounds(leftToRight ? rowHeadR.x : vsbR.x, hsbR.y, leftToRight ? rowHeadR.width : vsbR.width, hsbR.height);
        }
        if (this.lowerRight != null) {
            this.lowerRight.setBounds(leftToRight ? vsbR.x : rowHeadR.x, hsbR.y, leftToRight ? vsbR.width : rowHeadR.width, hsbR.height);
        }
        if (this.upperLeft != null) {
            this.upperLeft.setBounds(leftToRight ? rowHeadR.x : vsbR.x, colHeadR.y, leftToRight ? rowHeadR.width : vsbR.width, colHeadR.height);
        }
        if (this.upperRight != null) {
            this.upperRight.setBounds(leftToRight ? vsbR.x : rowHeadR.x, colHeadR.y, leftToRight ? vsbR.width : rowHeadR.width, colHeadR.height);
        }
    }

    private void adjustForVSB(boolean wantsVSB, Rectangle available, Rectangle vsbR, Insets vpbInsets, boolean leftToRight) {
        int oldWidth = vsbR.width;
        if (wantsVSB) {
            int vsbWidth = Math.max(0, Math.min(this.vsb.getPreferredSize().width, available.width));
            available.width -= vsbWidth;
            vsbR.width = vsbWidth;
            if (leftToRight) {
                vsbR.x = available.x + available.width + vpbInsets.right;
            } else {
                vsbR.x = available.x - vpbInsets.left;
                available.x += vsbWidth;
            }
        } else {
            available.width += oldWidth;
        }
    }

    private void adjustForHSB(boolean wantsHSB, Rectangle available, Rectangle hsbR, Insets vpbInsets) {
        int oldHeight = hsbR.height;
        if (wantsHSB) {
            int hsbHeight = Math.max(0, Math.min(available.height, this.hsb.getPreferredSize().height));
            available.height -= hsbHeight;
            hsbR.y = available.y + available.height + vpbInsets.bottom;
            hsbR.height = hsbHeight;
        } else {
            available.height += oldHeight;
        }
    }

    @Deprecated
    public Rectangle getViewportBorderBounds(JScrollPane scrollpane) {
        return scrollpane.getViewportBorderBounds();
    }

    public static class UIResource
    extends ScrollPaneLayout
    implements javax.swing.plaf.UIResource {
    }
}

