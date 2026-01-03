/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import sun.awt.image.PixelConverter;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;

public class BufferedPaints {
    public static final int MULTI_MAX_FRACTIONS = 12;

    static void setPaint(RenderQueue rq, SunGraphics2D sg2d, Paint paint, int ctxflags) {
        if (sg2d.paintState <= 1) {
            BufferedPaints.setColor(rq, sg2d.pixel);
        } else {
            boolean useMask = (ctxflags & 2) != 0;
            switch (sg2d.paintState) {
                case 2: {
                    BufferedPaints.setGradientPaint(rq, sg2d, (GradientPaint)paint, useMask);
                    break;
                }
                case 3: {
                    BufferedPaints.setLinearGradientPaint(rq, sg2d, (LinearGradientPaint)paint, useMask);
                    break;
                }
                case 4: {
                    BufferedPaints.setRadialGradientPaint(rq, sg2d, (RadialGradientPaint)paint, useMask);
                    break;
                }
                case 5: {
                    BufferedPaints.setTexturePaint(rq, sg2d, (TexturePaint)paint, useMask);
                    break;
                }
            }
        }
    }

    static void resetPaint(RenderQueue rq) {
        rq.ensureCapacity(4);
        RenderBuffer buf = rq.getBuffer();
        buf.putInt(100);
    }

    private static void setColor(RenderQueue rq, int pixel) {
        rq.ensureCapacity(8);
        RenderBuffer buf = rq.getBuffer();
        buf.putInt(101);
        buf.putInt(pixel);
    }

    private static void setGradientPaint(RenderQueue rq, AffineTransform at, Color c1, Color c2, Point2D pt1, Point2D pt2, boolean isCyclic, boolean useMask) {
        double p3;
        double p1;
        double p0;
        PixelConverter pc = PixelConverter.ArgbPre.instance;
        int pixel1 = pc.rgbToPixel(c1.getRGB(), null);
        int pixel2 = pc.rgbToPixel(c2.getRGB(), null);
        double x = pt1.getX();
        double y = pt1.getY();
        at.translate(x, y);
        x = pt2.getX() - x;
        y = pt2.getY() - y;
        double len = Math.sqrt(x * x + y * y);
        at.rotate(x, y);
        at.scale(2.0 * len, 1.0);
        at.translate(-0.25, 0.0);
        try {
            at.invert();
            p0 = at.getScaleX();
            p1 = at.getShearX();
            p3 = at.getTranslateX();
        }
        catch (NoninvertibleTransformException e) {
            p3 = 0.0;
            p1 = 0.0;
            p0 = 0.0;
        }
        rq.ensureCapacityAndAlignment(44, 12);
        RenderBuffer buf = rq.getBuffer();
        buf.putInt(102);
        buf.putInt(useMask ? 1 : 0);
        buf.putInt(isCyclic ? 1 : 0);
        buf.putDouble(p0).putDouble(p1).putDouble(p3);
        buf.putInt(pixel1).putInt(pixel2);
    }

    private static void setGradientPaint(RenderQueue rq, SunGraphics2D sg2d, GradientPaint paint, boolean useMask) {
        BufferedPaints.setGradientPaint(rq, (AffineTransform)sg2d.transform.clone(), paint.getColor1(), paint.getColor2(), paint.getPoint1(), paint.getPoint2(), paint.isCyclic(), useMask);
    }

    private static void setTexturePaint(RenderQueue rq, SunGraphics2D sg2d, TexturePaint paint, boolean useMask) {
        double yp3;
        double yp1;
        double yp0;
        double xp3;
        double xp1;
        double xp0;
        BufferedImage bi = paint.getImage();
        SurfaceData dstData = sg2d.surfaceData;
        SurfaceData srcData = dstData.getSourceSurfaceData(bi, 0, CompositeType.SrcOver, null);
        boolean filter = sg2d.interpolationType != 1;
        AffineTransform at = (AffineTransform)sg2d.transform.clone();
        Rectangle2D anchor = paint.getAnchorRect();
        at.translate(anchor.getX(), anchor.getY());
        at.scale(anchor.getWidth(), anchor.getHeight());
        try {
            at.invert();
            xp0 = at.getScaleX();
            xp1 = at.getShearX();
            xp3 = at.getTranslateX();
            yp0 = at.getShearY();
            yp1 = at.getScaleY();
            yp3 = at.getTranslateY();
        }
        catch (NoninvertibleTransformException e) {
            yp3 = 0.0;
            yp1 = 0.0;
            yp0 = 0.0;
            xp3 = 0.0;
            xp1 = 0.0;
            xp0 = 0.0;
        }
        rq.ensureCapacityAndAlignment(68, 12);
        RenderBuffer buf = rq.getBuffer();
        buf.putInt(105);
        buf.putInt(useMask ? 1 : 0);
        buf.putInt(filter ? 1 : 0);
        buf.putLong(srcData.getNativeOps());
        buf.putDouble(xp0).putDouble(xp1).putDouble(xp3);
        buf.putDouble(yp0).putDouble(yp1).putDouble(yp3);
    }

    public static int convertSRGBtoLinearRGB(int color) {
        float input = (float)color / 255.0f;
        float output = input <= 0.04045f ? input / 12.92f : (float)Math.pow(((double)input + 0.055) / 1.055, 2.4);
        return Math.round(output * 255.0f);
    }

    private static int colorToIntArgbPrePixel(Color c, boolean linear) {
        int rgb = c.getRGB();
        if (!linear && rgb >> 24 == -1) {
            return rgb;
        }
        int a = rgb >>> 24;
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        if (linear) {
            r = BufferedPaints.convertSRGBtoLinearRGB(r);
            g = BufferedPaints.convertSRGBtoLinearRGB(g);
            b = BufferedPaints.convertSRGBtoLinearRGB(b);
        }
        int a2 = a + (a >> 7);
        r = r * a2 >> 8;
        g = g * a2 >> 8;
        b = b * a2 >> 8;
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static int[] convertToIntArgbPrePixels(Color[] colors, boolean linear) {
        int[] pixels = new int[colors.length];
        for (int i = 0; i < colors.length; ++i) {
            pixels[i] = BufferedPaints.colorToIntArgbPrePixel(colors[i], linear);
        }
        return pixels;
    }

    private static void setLinearGradientPaint(RenderQueue rq, SunGraphics2D sg2d, LinearGradientPaint paint, boolean useMask) {
        float p3;
        float p1;
        float p0;
        boolean linear = paint.getColorSpace() == MultipleGradientPaint.ColorSpaceType.LINEAR_RGB;
        Color[] colors = paint.getColors();
        int numStops = colors.length;
        Point2D pt1 = paint.getStartPoint();
        Point2D pt2 = paint.getEndPoint();
        AffineTransform at = paint.getTransform();
        at.preConcatenate(sg2d.transform);
        if (!linear && numStops == 2 && paint.getCycleMethod() != MultipleGradientPaint.CycleMethod.REPEAT) {
            boolean isCyclic = paint.getCycleMethod() != MultipleGradientPaint.CycleMethod.NO_CYCLE;
            BufferedPaints.setGradientPaint(rq, at, colors[0], colors[1], pt1, pt2, isCyclic, useMask);
            return;
        }
        int cycleMethod = paint.getCycleMethod().ordinal();
        float[] fractions = paint.getFractions();
        int[] pixels = BufferedPaints.convertToIntArgbPrePixels(colors, linear);
        double x = pt1.getX();
        double y = pt1.getY();
        at.translate(x, y);
        x = pt2.getX() - x;
        y = pt2.getY() - y;
        double len = Math.sqrt(x * x + y * y);
        at.rotate(x, y);
        at.scale(len, 1.0);
        try {
            at.invert();
            p0 = (float)at.getScaleX();
            p1 = (float)at.getShearX();
            p3 = (float)at.getTranslateX();
        }
        catch (NoninvertibleTransformException e) {
            p3 = 0.0f;
            p1 = 0.0f;
            p0 = 0.0f;
        }
        rq.ensureCapacity(32 + numStops * 4 * 2);
        RenderBuffer buf = rq.getBuffer();
        buf.putInt(103);
        buf.putInt(useMask ? 1 : 0);
        buf.putInt(linear ? 1 : 0);
        buf.putInt(cycleMethod);
        buf.putInt(numStops);
        buf.putFloat(p0);
        buf.putFloat(p1);
        buf.putFloat(p3);
        buf.put(fractions);
        buf.put(pixels);
    }

    private static void setRadialGradientPaint(RenderQueue rq, SunGraphics2D sg2d, RadialGradientPaint paint, boolean useMask) {
        boolean linear = paint.getColorSpace() == MultipleGradientPaint.ColorSpaceType.LINEAR_RGB;
        int cycleMethod = paint.getCycleMethod().ordinal();
        float[] fractions = paint.getFractions();
        Color[] colors = paint.getColors();
        int numStops = colors.length;
        int[] pixels = BufferedPaints.convertToIntArgbPrePixels(colors, linear);
        Point2D center = paint.getCenterPoint();
        Point2D focus = paint.getFocusPoint();
        float radius = paint.getRadius();
        double cx = center.getX();
        double cy = center.getY();
        double fx = focus.getX();
        double fy = focus.getY();
        AffineTransform at = paint.getTransform();
        at.preConcatenate(sg2d.transform);
        focus = at.transform(focus, focus);
        at.translate(cx, cy);
        at.rotate(fx - cx, fy - cy);
        at.scale(radius, radius);
        try {
            at.invert();
        }
        catch (Exception e) {
            at.setToScale(0.0, 0.0);
        }
        focus = at.transform(focus, focus);
        fx = Math.min(focus.getX(), 0.99);
        rq.ensureCapacity(48 + numStops * 4 * 2);
        RenderBuffer buf = rq.getBuffer();
        buf.putInt(104);
        buf.putInt(useMask ? 1 : 0);
        buf.putInt(linear ? 1 : 0);
        buf.putInt(numStops);
        buf.putInt(cycleMethod);
        buf.putFloat((float)at.getScaleX());
        buf.putFloat((float)at.getShearX());
        buf.putFloat((float)at.getTranslateX());
        buf.putFloat((float)at.getShearY());
        buf.putFloat((float)at.getScaleY());
        buf.putFloat((float)at.getTranslateY());
        buf.putFloat((float)fx);
        buf.put(fractions);
        buf.put(pixels);
    }
}

