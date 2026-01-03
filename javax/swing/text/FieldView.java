/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.SegmentCache;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import javax.swing.text.ViewFactory;
import sun.swing.SwingUtilities2;

public class FieldView
extends PlainView {
    public FieldView(Element elem) {
        super(elem);
    }

    protected FontMetrics getFontMetrics() {
        Container c = this.getContainer();
        return c.getFontMetrics(c.getFont());
    }

    protected Shape adjustAllocation(Shape a) {
        if (a != null) {
            Container c;
            Rectangle bounds = a.getBounds();
            int vspan = (int)this.getPreferredSpan(1);
            int hspan = (int)this.getPreferredSpan(0);
            if (bounds.height != vspan) {
                int slop = bounds.height - vspan;
                bounds.y += slop / 2;
                bounds.height -= slop;
            }
            if ((c = this.getContainer()) instanceof JTextField) {
                int extent;
                JTextField field = (JTextField)c;
                BoundedRangeModel vis = field.getHorizontalVisibility();
                int max = Math.max(hspan, bounds.width);
                int value = vis.getValue();
                if (value + (extent = Math.min(max, bounds.width - 1)) > max) {
                    value = max - extent;
                }
                vis.setRangeProperties(value, extent, vis.getMinimum(), max, false);
                if (hspan < bounds.width) {
                    int slop = bounds.width - 1 - hspan;
                    int align = ((JTextField)c).getHorizontalAlignment();
                    if (Utilities.isLeftToRight(c)) {
                        if (align == 10) {
                            align = 2;
                        } else if (align == 11) {
                            align = 4;
                        }
                    } else if (align == 10) {
                        align = 4;
                    } else if (align == 11) {
                        align = 2;
                    }
                    switch (align) {
                        case 0: {
                            bounds.x += slop / 2;
                            bounds.width -= slop;
                            break;
                        }
                        case 4: {
                            bounds.x += slop;
                            bounds.width -= slop;
                        }
                    }
                } else {
                    bounds.width = hspan;
                    bounds.x -= vis.getValue();
                }
            }
            return bounds;
        }
        return null;
    }

    void updateVisibilityModel() {
        Container c = this.getContainer();
        if (c instanceof JTextField) {
            JTextField field = (JTextField)c;
            BoundedRangeModel vis = field.getHorizontalVisibility();
            int hspan = (int)this.getPreferredSpan(0);
            int extent = vis.getExtent();
            int maximum = Math.max(hspan, extent);
            extent = extent == 0 ? maximum : extent;
            int value = maximum - extent;
            int oldValue = vis.getValue();
            if (oldValue + extent > maximum) {
                oldValue = maximum - extent;
            }
            value = Math.max(0, Math.min(value, oldValue));
            vis.setRangeProperties(value, extent, 0, maximum, false);
        }
    }

    @Override
    public void paint(Graphics g, Shape a) {
        Rectangle r = (Rectangle)a;
        g.clipRect(r.x, r.y, r.width, r.height);
        super.paint(g, a);
    }

    @Override
    Shape adjustPaintRegion(Shape a) {
        return this.adjustAllocation(a);
    }

    @Override
    public float getPreferredSpan(int axis) {
        switch (axis) {
            case 0: {
                int width;
                Segment buff = SegmentCache.getSharedSegment();
                Document doc = this.getDocument();
                try {
                    FontMetrics fm = this.getFontMetrics();
                    doc.getText(0, doc.getLength(), buff);
                    width = Utilities.getTabbedTextWidth(buff, fm, 0, (TabExpander)this, 0);
                    if (buff.count > 0) {
                        Container c = this.getContainer();
                        this.firstLineOffset = SwingUtilities2.getLeftSideBearing(c instanceof JComponent ? (JComponent)c : null, fm, buff.array[buff.offset]);
                        this.firstLineOffset = Math.max(0, -this.firstLineOffset);
                    } else {
                        this.firstLineOffset = 0;
                    }
                }
                catch (BadLocationException bl) {
                    width = 0;
                }
                SegmentCache.releaseSharedSegment(buff);
                return width + this.firstLineOffset;
            }
        }
        return super.getPreferredSpan(axis);
    }

    @Override
    public int getResizeWeight(int axis) {
        if (axis == 0) {
            return 1;
        }
        return 0;
    }

    @Override
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        return super.modelToView(pos, this.adjustAllocation(a), b);
    }

    @Override
    public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
        return super.viewToModel(fx, fy, this.adjustAllocation(a), bias);
    }

    @Override
    public void insertUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        super.insertUpdate(changes, this.adjustAllocation(a), f);
        this.updateVisibilityModel();
    }

    @Override
    public void removeUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        super.removeUpdate(changes, this.adjustAllocation(a), f);
        this.updateVisibilityModel();
    }
}

