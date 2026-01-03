/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.security.AccessController;
import sun.awt.geom.PathConsumer2D;
import sun.java2d.pipe.AATileGenerator;
import sun.java2d.pipe.Region;
import sun.security.action.GetPropertyAction;

public abstract class RenderingEngine {
    private static RenderingEngine reImpl;

    public static synchronized RenderingEngine getInstance() {
        String reTrace;
        if (reImpl != null) {
            return reImpl;
        }
        GetPropertyAction gpa = new GetPropertyAction("sun.java2d.renderer");
        String reClass = AccessController.doPrivileged(gpa);
        if (reClass != null) {
            try {
                Class<?> cls = Class.forName(reClass);
                reImpl = (RenderingEngine)cls.getConstructor(new Class[0]).newInstance(new Object[0]);
            }
            catch (ReflectiveOperationException cls) {
                // empty catch block
            }
        }
        if (reImpl == null) {
            String marlinREClass = "sun.java2d.marlin.DMarlinRenderingEngine";
            try {
                Class<?> cls = Class.forName("sun.java2d.marlin.DMarlinRenderingEngine");
                reImpl = (RenderingEngine)cls.getConstructor(new Class[0]).newInstance(new Object[0]);
            }
            catch (ReflectiveOperationException cls) {
                // empty catch block
            }
        }
        if (reImpl == null) {
            throw new InternalError("No RenderingEngine module found");
        }
        gpa = new GetPropertyAction("sun.java2d.renderer.verbose");
        String verbose = AccessController.doPrivileged(gpa);
        if (verbose != null && verbose.startsWith("t")) {
            System.out.println("RenderingEngine = " + String.valueOf(reImpl));
        }
        if ((reTrace = AccessController.doPrivileged(gpa = new GetPropertyAction("sun.java2d.renderer.trace"))) != null) {
            reImpl = new Tracer(reImpl);
        }
        return reImpl;
    }

    public abstract Shape createStrokedShape(Shape var1, float var2, int var3, int var4, float var5, float[] var6, float var7);

    public abstract void strokeTo(Shape var1, AffineTransform var2, BasicStroke var3, boolean var4, boolean var5, boolean var6, PathConsumer2D var7);

    public void strokeTo(Shape src, AffineTransform at, Region clip, BasicStroke bs, boolean thin, boolean normalize, boolean antialias, PathConsumer2D consumer) {
        this.strokeTo(src, at, bs, thin, normalize, antialias, consumer);
    }

    public abstract AATileGenerator getAATileGenerator(Shape var1, AffineTransform var2, Region var3, BasicStroke var4, boolean var5, boolean var6, int[] var7);

    public abstract AATileGenerator getAATileGenerator(double var1, double var3, double var5, double var7, double var9, double var11, double var13, double var15, Region var17, int[] var18);

    public abstract float getMinimumAAPenSize();

    public static void feedConsumer(PathIterator pi, PathConsumer2D consumer) {
        float[] coords = new float[6];
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
                case 0: {
                    consumer.moveTo(coords[0], coords[1]);
                    break;
                }
                case 1: {
                    consumer.lineTo(coords[0], coords[1]);
                    break;
                }
                case 2: {
                    consumer.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                }
                case 3: {
                    consumer.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;
                }
                case 4: {
                    consumer.closePath();
                }
            }
            pi.next();
        }
    }

    static class Tracer
    extends RenderingEngine {
        RenderingEngine target;
        String name;

        public Tracer(RenderingEngine target) {
            this.target = target;
            this.name = target.getClass().getName();
        }

        @Override
        public Shape createStrokedShape(Shape src, float width, int caps, int join, float miterlimit, float[] dashes, float dashphase) {
            System.out.println(this.name + ".createStrokedShape(" + src.getClass().getName() + ", width = " + width + ", caps = " + caps + ", join = " + join + ", miter = " + miterlimit + ", dashes = " + String.valueOf(dashes) + ", dashphase = " + dashphase + ")");
            return this.target.createStrokedShape(src, width, caps, join, miterlimit, dashes, dashphase);
        }

        @Override
        public void strokeTo(Shape src, AffineTransform at, BasicStroke bs, boolean thin, boolean normalize, boolean antialias, PathConsumer2D consumer) {
            System.out.println(this.name + ".strokeTo(" + src.getClass().getName() + ", " + String.valueOf(at) + ", " + String.valueOf(bs) + ", " + (thin ? "thin" : "wide") + ", " + (normalize ? "normalized" : "pure") + ", " + (antialias ? "AA" : "non-AA") + ", " + consumer.getClass().getName() + ")");
            this.target.strokeTo(src, at, bs, thin, normalize, antialias, consumer);
        }

        @Override
        public void strokeTo(Shape src, AffineTransform at, Region clip, BasicStroke bs, boolean thin, boolean normalize, boolean antialias, PathConsumer2D consumer) {
            System.out.println(this.name + ".strokeTo(" + src.getClass().getName() + ", " + String.valueOf(at) + ", " + String.valueOf(clip) + ", " + String.valueOf(bs) + ", " + (thin ? "thin" : "wide") + ", " + (normalize ? "normalized" : "pure") + ", " + (antialias ? "AA" : "non-AA") + ", " + consumer.getClass().getName() + ")");
            this.target.strokeTo(src, at, clip, bs, thin, normalize, antialias, consumer);
        }

        @Override
        public float getMinimumAAPenSize() {
            System.out.println(this.name + ".getMinimumAAPenSize()");
            return this.target.getMinimumAAPenSize();
        }

        @Override
        public AATileGenerator getAATileGenerator(Shape s, AffineTransform at, Region clip, BasicStroke bs, boolean thin, boolean normalize, int[] bbox) {
            System.out.println(this.name + ".getAATileGenerator(" + s.getClass().getName() + ", " + String.valueOf(at) + ", " + String.valueOf(clip) + ", " + String.valueOf(bs) + ", " + (thin ? "thin" : "wide") + ", " + (normalize ? "normalized" : "pure") + ")");
            return this.target.getAATileGenerator(s, at, clip, bs, thin, normalize, bbox);
        }

        @Override
        public AATileGenerator getAATileGenerator(double x, double y, double dx1, double dy1, double dx2, double dy2, double lw1, double lw2, Region clip, int[] bbox) {
            System.out.println(this.name + ".getAATileGenerator(" + x + ", " + y + ", " + dx1 + ", " + dy1 + ", " + dx2 + ", " + dy2 + ", " + lw1 + ", " + lw2 + ", " + String.valueOf(clip) + ")");
            return this.target.getAATileGenerator(x, y, dx1, dy1, dx2, dy2, lw1, lw2, clip, bbox);
        }
    }
}

