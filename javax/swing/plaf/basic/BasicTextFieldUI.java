/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.FieldView;
import javax.swing.text.GlyphView;
import javax.swing.text.JTextComponent;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class BasicTextFieldUI
extends BasicTextUI {
    public static ComponentUI createUI(JComponent c) {
        return new BasicTextFieldUI();
    }

    @Override
    protected String getPropertyPrefix() {
        return "TextField";
    }

    @Override
    public View create(Element elem) {
        String kind;
        Document doc = elem.getDocument();
        Object i18nFlag = doc.getProperty("i18n");
        if (Boolean.TRUE.equals(i18nFlag) && (kind = elem.getName()) != null) {
            if (kind.equals("content")) {
                return new GlyphView(this, elem){

                    @Override
                    public float getMinimumSpan(int axis) {
                        return this.getPreferredSpan(axis);
                    }
                };
            }
            if (kind.equals("paragraph")) {
                return new I18nFieldView(elem);
            }
        }
        return new FieldView(elem);
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        View rootView = this.getRootView((JTextComponent)c);
        if (rootView.getViewCount() > 0) {
            Insets insets = c.getInsets();
            if ((height = height - insets.top - insets.bottom) > 0) {
                int baseline = insets.top;
                View fieldView = rootView.getView(0);
                int vspan = (int)fieldView.getPreferredSpan(1);
                if (height != vspan) {
                    int slop = height - vspan;
                    baseline += slop / 2;
                }
                if (fieldView instanceof I18nFieldView) {
                    int fieldBaseline = BasicHTML.getBaseline(fieldView, width - insets.left - insets.right, height);
                    if (fieldBaseline < 0) {
                        return -1;
                    }
                    baseline += fieldBaseline;
                } else {
                    FontMetrics fm = c.getFontMetrics(c.getFont());
                    baseline += fm.getAscent();
                }
                return baseline;
            }
        }
        return -1;
    }

    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
        super.getBaselineResizeBehavior(c);
        return Component.BaselineResizeBehavior.CENTER_OFFSET;
    }

    static class I18nFieldView
    extends ParagraphView {
        I18nFieldView(Element elem) {
            super(elem);
        }

        @Override
        public int getFlowSpan(int index) {
            return Integer.MAX_VALUE;
        }

        @Override
        protected void setJustification(int j) {
        }

        static boolean isLeftToRight(Component c) {
            return c.getComponentOrientation().isLeftToRight();
        }

        Shape adjustAllocation(Shape a) {
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
                        if (I18nFieldView.isLeftToRight(c)) {
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
            super.paint(g, this.adjustAllocation(a));
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
        public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
            return super.modelToView(p0, b0, p1, b1, this.adjustAllocation(a));
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
}

