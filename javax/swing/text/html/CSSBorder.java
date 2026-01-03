/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import javax.swing.border.AbstractBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.CSS;

class CSSBorder
extends AbstractBorder {
    static final int COLOR = 0;
    static final int STYLE = 1;
    static final int WIDTH = 2;
    static final int TOP = 0;
    static final int RIGHT = 1;
    static final int BOTTOM = 2;
    static final int LEFT = 3;
    static final CSS.Attribute[][] ATTRIBUTES = new CSS.Attribute[][]{{CSS.Attribute.BORDER_TOP_COLOR, CSS.Attribute.BORDER_RIGHT_COLOR, CSS.Attribute.BORDER_BOTTOM_COLOR, CSS.Attribute.BORDER_LEFT_COLOR}, {CSS.Attribute.BORDER_TOP_STYLE, CSS.Attribute.BORDER_RIGHT_STYLE, CSS.Attribute.BORDER_BOTTOM_STYLE, CSS.Attribute.BORDER_LEFT_STYLE}, {CSS.Attribute.BORDER_TOP_WIDTH, CSS.Attribute.BORDER_RIGHT_WIDTH, CSS.Attribute.BORDER_BOTTOM_WIDTH, CSS.Attribute.BORDER_LEFT_WIDTH}};
    static final CSS.CssValue[] PARSERS = new CSS.CssValue[]{new CSS.ColorValue(), new CSS.BorderStyle(), new CSS.BorderWidthValue(null, 0)};
    static final Object[] DEFAULTS = new Object[]{CSS.Attribute.BORDER_COLOR, PARSERS[1].parseCssValue(CSS.Attribute.BORDER_STYLE.getDefaultValue()), PARSERS[2].parseCssValue(CSS.Attribute.BORDER_WIDTH.getDefaultValue())};
    final AttributeSet attrs;
    static Map<CSS.Value, BorderPainter> borderPainters = new HashMap<CSS.Value, BorderPainter>();

    CSSBorder(AttributeSet attrs) {
        this.attrs = attrs;
    }

    private Color getBorderColor(int side) {
        CSS.ColorValue cv;
        Object o = this.attrs.getAttribute(ATTRIBUTES[0][side]);
        if (o instanceof CSS.ColorValue) {
            cv = (CSS.ColorValue)o;
        } else {
            cv = (CSS.ColorValue)this.attrs.getAttribute(CSS.Attribute.COLOR);
            if (cv == null) {
                cv = (CSS.ColorValue)PARSERS[0].parseCssValue(CSS.Attribute.COLOR.getDefaultValue());
            }
        }
        return cv.getValue();
    }

    private int getBorderWidth(int side) {
        int width = 0;
        CSS.BorderStyle bs = (CSS.BorderStyle)this.attrs.getAttribute(ATTRIBUTES[1][side]);
        if (bs != null && bs.getValue() != CSS.Value.NONE) {
            CSS.LengthValue bw = (CSS.LengthValue)this.attrs.getAttribute(ATTRIBUTES[2][side]);
            if (bw == null) {
                bw = (CSS.LengthValue)DEFAULTS[2];
            }
            width = (int)bw.getValue(true);
        }
        return width;
    }

    private int[] getWidths() {
        int[] widths = new int[4];
        for (int i = 0; i < widths.length; ++i) {
            widths[i] = this.getBorderWidth(i);
        }
        return widths;
    }

    private CSS.Value getBorderStyle(int side) {
        CSS.BorderStyle style = (CSS.BorderStyle)this.attrs.getAttribute(ATTRIBUTES[1][side]);
        if (style == null) {
            style = (CSS.BorderStyle)DEFAULTS[1];
        }
        return style.getValue();
    }

    private Polygon getBorderShape(int side) {
        Polygon shape = null;
        int[] widths = this.getWidths();
        if (widths[side] != 0) {
            shape = new Polygon(new int[4], new int[4], 0);
            shape.addPoint(0, 0);
            shape.addPoint(-widths[(side + 3) % 4], -widths[side]);
            shape.addPoint(widths[(side + 1) % 4], -widths[side]);
            shape.addPoint(0, 0);
        }
        return shape;
    }

    private BorderPainter getBorderPainter(int side) {
        CSS.Value style = this.getBorderStyle(side);
        return borderPainters.get(style);
    }

    static Color getAdjustedColor(Color c, double factor) {
        double f = 1.0 - Math.min(Math.abs(factor), 1.0);
        double inc = factor > 0.0 ? 255.0 * (1.0 - f) : 0.0;
        return new Color((int)((double)c.getRed() * f + inc), (int)((double)c.getGreen() * f + inc), (int)((double)c.getBlue() * f + inc));
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        int[] widths = this.getWidths();
        insets.set(widths[0], widths[3], widths[2], widths[1]);
        return insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (!(g instanceof Graphics2D)) {
            return;
        }
        Graphics2D g2 = (Graphics2D)g.create();
        int[] widths = this.getWidths();
        int intX = x + widths[3];
        int intY = y + widths[0];
        int intWidth = width - (widths[1] + widths[3]);
        int intHeight = height - (widths[0] + widths[2]);
        int[][] intCorners = new int[][]{{intX, intY}, {intX + intWidth, intY}, {intX + intWidth, intY + intHeight}, {intX, intY + intHeight}};
        for (int i = 0; i < 4; ++i) {
            CSS.Value style = this.getBorderStyle(i);
            Polygon shape = this.getBorderShape(i);
            if (style == CSS.Value.NONE || shape == null) continue;
            int sideLength = i % 2 == 0 ? intWidth : intHeight;
            shape.xpoints[2] = shape.xpoints[2] + sideLength;
            shape.xpoints[3] = shape.xpoints[3] + sideLength;
            Color color = this.getBorderColor(i);
            BorderPainter painter = this.getBorderPainter(i);
            double angle = (double)i * Math.PI / 2.0;
            g2.setClip(g.getClip());
            g2.translate(intCorners[i][0], intCorners[i][1]);
            g2.rotate(angle);
            g2.clip(shape);
            painter.paint(shape, g2, color, i);
            g2.rotate(-angle);
            g2.translate(-intCorners[i][0], -intCorners[i][1]);
        }
        g2.dispose();
    }

    static void registerBorderPainter(CSS.Value style, BorderPainter painter) {
        borderPainters.put(style, painter);
    }

    static {
        CSSBorder.registerBorderPainter(CSS.Value.NONE, new NullPainter());
        CSSBorder.registerBorderPainter(CSS.Value.HIDDEN, new NullPainter());
        CSSBorder.registerBorderPainter(CSS.Value.SOLID, new SolidPainter());
        CSSBorder.registerBorderPainter(CSS.Value.DOUBLE, new DoublePainter());
        CSSBorder.registerBorderPainter(CSS.Value.DOTTED, new DottedDashedPainter(1));
        CSSBorder.registerBorderPainter(CSS.Value.DASHED, new DottedDashedPainter(3));
        CSSBorder.registerBorderPainter(CSS.Value.GROOVE, new GrooveRidgePainter(CSS.Value.GROOVE));
        CSSBorder.registerBorderPainter(CSS.Value.RIDGE, new GrooveRidgePainter(CSS.Value.RIDGE));
        CSSBorder.registerBorderPainter(CSS.Value.INSET, new InsetOutsetPainter(CSS.Value.INSET));
        CSSBorder.registerBorderPainter(CSS.Value.OUTSET, new InsetOutsetPainter(CSS.Value.OUTSET));
    }

    static interface BorderPainter {
        public void paint(Polygon var1, Graphics var2, Color var3, int var4);
    }

    static class NullPainter
    implements BorderPainter {
        NullPainter() {
        }

        @Override
        public void paint(Polygon shape, Graphics g, Color color, int side) {
        }
    }

    static class SolidPainter
    implements BorderPainter {
        SolidPainter() {
        }

        @Override
        public void paint(Polygon shape, Graphics g, Color color, int side) {
            g.setColor(color);
            g.fillPolygon(shape);
        }
    }

    static class DoublePainter
    extends StrokePainter {
        DoublePainter() {
        }

        @Override
        public void paint(Polygon shape, Graphics g, Color color, int side) {
            Rectangle r = shape.getBounds();
            int length = Math.max(r.height / 3, 1);
            int[] lengthPattern = new int[]{length, length};
            Color[] colorPattern = new Color[]{color, null};
            this.paintStrokes(r, g, 1, lengthPattern, colorPattern);
        }
    }

    static class DottedDashedPainter
    extends StrokePainter {
        final int factor;

        DottedDashedPainter(int factor) {
            this.factor = factor;
        }

        @Override
        public void paint(Polygon shape, Graphics g, Color color, int side) {
            Rectangle r = shape.getBounds();
            int length = r.height * this.factor;
            int[] lengthPattern = new int[]{length, length};
            Color[] colorPattern = new Color[]{color, null};
            this.paintStrokes(r, g, 0, lengthPattern, colorPattern);
        }
    }

    static class GrooveRidgePainter
    extends ShadowLightPainter {
        final CSS.Value type;

        GrooveRidgePainter(CSS.Value type) {
            this.type = type;
        }

        @Override
        public void paint(Polygon shape, Graphics g, Color color, int side) {
            Color[] colorArray;
            Rectangle r = shape.getBounds();
            int length = Math.max(r.height / 2, 1);
            int[] lengthPattern = new int[]{length, length};
            if ((side + 1) % 4 < 2 == (this.type == CSS.Value.GROOVE)) {
                Color[] colorArray2 = new Color[2];
                colorArray2[0] = GrooveRidgePainter.getShadowColor(color);
                colorArray = colorArray2;
                colorArray2[1] = GrooveRidgePainter.getLightColor(color);
            } else {
                Color[] colorArray3 = new Color[2];
                colorArray3[0] = GrooveRidgePainter.getLightColor(color);
                colorArray = colorArray3;
                colorArray3[1] = GrooveRidgePainter.getShadowColor(color);
            }
            Color[] colorPattern = colorArray;
            this.paintStrokes(r, g, 1, lengthPattern, colorPattern);
        }
    }

    static class InsetOutsetPainter
    extends ShadowLightPainter {
        CSS.Value type;

        InsetOutsetPainter(CSS.Value type) {
            this.type = type;
        }

        @Override
        public void paint(Polygon shape, Graphics g, Color color, int side) {
            g.setColor((side + 1) % 4 < 2 == (this.type == CSS.Value.INSET) ? InsetOutsetPainter.getShadowColor(color) : InsetOutsetPainter.getLightColor(color));
            g.fillPolygon(shape);
        }
    }

    static abstract class ShadowLightPainter
    extends StrokePainter {
        ShadowLightPainter() {
        }

        static Color getShadowColor(Color c) {
            return CSSBorder.getAdjustedColor(c, -0.3);
        }

        static Color getLightColor(Color c) {
            return CSSBorder.getAdjustedColor(c, 0.7);
        }
    }

    static abstract class StrokePainter
    implements BorderPainter {
        StrokePainter() {
        }

        void paintStrokes(Rectangle r, Graphics g, int axis, int[] lengthPattern, Color[] colorPattern) {
            int end;
            boolean xAxis = axis == 0;
            int start = 0;
            int n = end = xAxis ? r.width : r.height;
            while (start < end) {
                int length;
                for (int i = 0; i < lengthPattern.length && start < end; start += length, ++i) {
                    length = lengthPattern[i];
                    Color c = colorPattern[i];
                    if (c == null) continue;
                    int x = r.x + (xAxis ? start : 0);
                    int y = r.y + (xAxis ? 0 : start);
                    int width = xAxis ? length : r.width;
                    int height = xAxis ? r.height : length;
                    g.setColor(c);
                    g.fillRect(x, y, width, height);
                }
            }
        }
    }
}

