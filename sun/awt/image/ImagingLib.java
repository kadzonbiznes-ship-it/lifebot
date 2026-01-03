/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.WritableRaster;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.awt.image.SunWritableRaster;

public class ImagingLib {
    static boolean useLib = true;
    static boolean verbose = false;
    private static final int NUM_NATIVE_OPS = 3;
    private static final int LOOKUP_OP = 0;
    private static final int AFFINE_OP = 1;
    private static final int CONVOLVE_OP = 2;
    private static Class<?>[] nativeOpClass = new Class[3];

    private static native boolean init();

    public static native int transformBI(BufferedImage var0, BufferedImage var1, double[] var2, int var3);

    public static native int transformRaster(Raster var0, Raster var1, double[] var2, int var3);

    public static native int convolveBI(BufferedImage var0, BufferedImage var1, Kernel var2, int var3);

    public static native int convolveRaster(Raster var0, Raster var1, Kernel var2, int var3);

    public static native int lookupByteBI(BufferedImage var0, BufferedImage var1, byte[][] var2);

    public static native int lookupByteRaster(Raster var0, Raster var1, byte[][] var2);

    private static int getNativeOpIndex(Class<?> opClass) {
        int opIndex = -1;
        for (int i = 0; i < 3; ++i) {
            if (opClass != nativeOpClass[i]) continue;
            opIndex = i;
            break;
        }
        return opIndex;
    }

    public static WritableRaster filter(RasterOp op, Raster src, WritableRaster dst) {
        if (!useLib) {
            return null;
        }
        if (dst == null) {
            dst = op.createCompatibleDestRaster(src);
        }
        WritableRaster retRaster = null;
        switch (ImagingLib.getNativeOpIndex(op.getClass())) {
            case 0: {
                ByteLookupTable bt;
                LookupTable table = ((LookupOp)op).getTable();
                if (table.getOffset() != 0) {
                    return null;
                }
                if (!(table instanceof ByteLookupTable) || ImagingLib.lookupByteRaster(src, dst, (bt = (ByteLookupTable)table).getTable()) <= 0) break;
                retRaster = dst;
                break;
            }
            case 1: {
                AffineTransformOp bOp = (AffineTransformOp)op;
                double[] matrix = new double[6];
                bOp.getTransform().getMatrix(matrix);
                if (ImagingLib.transformRaster(src, dst, matrix, bOp.getInterpolationType()) <= 0) break;
                retRaster = dst;
                break;
            }
            case 2: {
                ConvolveOp cOp = (ConvolveOp)op;
                if (ImagingLib.convolveRaster(src, dst, cOp.getKernel(), cOp.getEdgeCondition()) <= 0) break;
                retRaster = dst;
                break;
            }
        }
        if (retRaster != null) {
            SunWritableRaster.markDirty(retRaster);
        }
        return retRaster;
    }

    public static BufferedImage filter(BufferedImageOp op, BufferedImage src, BufferedImage dst) {
        if (verbose) {
            System.out.println("in filter and op is " + String.valueOf(op) + "bufimage is " + String.valueOf(src) + " and " + String.valueOf(dst));
        }
        if (!useLib) {
            return null;
        }
        if (dst == null) {
            dst = op.createCompatibleDestImage(src, null);
        }
        BufferedImage retBI = null;
        switch (ImagingLib.getNativeOpIndex(op.getClass())) {
            case 0: {
                ByteLookupTable bt;
                LookupTable table = ((LookupOp)op).getTable();
                if (table.getOffset() != 0) {
                    return null;
                }
                if (!(table instanceof ByteLookupTable) || ImagingLib.lookupByteBI(src, dst, (bt = (ByteLookupTable)table).getTable()) <= 0) break;
                retBI = dst;
                break;
            }
            case 1: {
                AffineTransformOp bOp = (AffineTransformOp)op;
                double[] matrix = new double[6];
                AffineTransform xform = bOp.getTransform();
                bOp.getTransform().getMatrix(matrix);
                if (ImagingLib.transformBI(src, dst, matrix, bOp.getInterpolationType()) <= 0) break;
                retBI = dst;
                break;
            }
            case 2: {
                ConvolveOp cOp = (ConvolveOp)op;
                if (ImagingLib.convolveBI(src, dst, cOp.getKernel(), cOp.getEdgeCondition()) <= 0) break;
                retBI = dst;
                break;
            }
        }
        if (retBI != null) {
            SunWritableRaster.markDirty(retBI);
        }
        return retBI;
    }

    static {
        PrivilegedAction<Boolean> doMlibInitialization = new PrivilegedAction<Boolean>(){

            @Override
            public Boolean run() {
                try {
                    System.loadLibrary("mlib_image");
                }
                catch (UnsatisfiedLinkError e) {
                    return Boolean.FALSE;
                }
                boolean success = ImagingLib.init();
                return success;
            }
        };
        useLib = AccessController.doPrivileged(doMlibInitialization);
        try {
            ImagingLib.nativeOpClass[0] = Class.forName("java.awt.image.LookupOp");
        }
        catch (ClassNotFoundException e) {
            System.err.println("Could not find class: " + String.valueOf(e));
        }
        try {
            ImagingLib.nativeOpClass[1] = Class.forName("java.awt.image.AffineTransformOp");
        }
        catch (ClassNotFoundException e) {
            System.err.println("Could not find class: " + String.valueOf(e));
        }
        try {
            ImagingLib.nativeOpClass[2] = Class.forName("java.awt.image.ConvolveOp");
        }
        catch (ClassNotFoundException e) {
            System.err.println("Could not find class: " + String.valueOf(e));
        }
    }
}

