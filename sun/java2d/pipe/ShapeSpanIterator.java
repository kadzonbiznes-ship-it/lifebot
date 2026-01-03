/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.Rectangle;
import java.awt.geom.PathIterator;
import sun.awt.geom.PathConsumer2D;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.SpanIterator;

public final class ShapeSpanIterator
implements SpanIterator,
PathConsumer2D {
    long pData;

    public static native void initIDs();

    public ShapeSpanIterator(boolean adjust) {
        this.setNormalize(adjust);
    }

    public void appendPath(PathIterator pi) {
        float[] coords = new float[6];
        this.setRule(pi.getWindingRule());
        while (!pi.isDone()) {
            this.addSegment(pi.currentSegment(coords), coords);
            pi.next();
        }
        this.pathDone();
    }

    public native void appendPoly(int[] var1, int[] var2, int var3, int var4, int var5);

    private native void setNormalize(boolean var1);

    public void setOutputAreaXYWH(int x, int y, int w, int h) {
        this.setOutputAreaXYXY(x, y, Region.dimAdd(x, w), Region.dimAdd(y, h));
    }

    public native void setOutputAreaXYXY(int var1, int var2, int var3, int var4);

    public void setOutputArea(Rectangle r) {
        this.setOutputAreaXYWH(r.x, r.y, r.width, r.height);
    }

    public void setOutputArea(Region r) {
        this.setOutputAreaXYXY(r.getLoX(), r.getLoY(), r.getHiX(), r.getHiY());
    }

    public native void setRule(int var1);

    public native void addSegment(int var1, float[] var2);

    @Override
    public native void getPathBox(int[] var1);

    @Override
    public native void intersectClipBox(int var1, int var2, int var3, int var4);

    @Override
    public native boolean nextSpan(int[] var1);

    @Override
    public native void skipDownTo(int var1);

    @Override
    public native long getNativeIterator();

    public native void dispose();

    @Override
    public native void moveTo(float var1, float var2);

    @Override
    public native void lineTo(float var1, float var2);

    @Override
    public native void quadTo(float var1, float var2, float var3, float var4);

    @Override
    public native void curveTo(float var1, float var2, float var3, float var4, float var5, float var6);

    @Override
    public native void closePath();

    @Override
    public native void pathDone();

    @Override
    public native long getNativeConsumer();

    static {
        ShapeSpanIterator.initIDs();
    }
}

