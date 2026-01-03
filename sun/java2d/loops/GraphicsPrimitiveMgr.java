/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.Comparator;
import sun.awt.SunHints;
import sun.java2d.SunGraphics2D;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.CustomComponent;
import sun.java2d.loops.GeneralRenderer;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveProxy;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.XORComposite;

public final class GraphicsPrimitiveMgr {
    private static final boolean debugTrace = false;
    private static GraphicsPrimitive[] primitives;
    private static boolean needssort;
    private static Comparator<GraphicsPrimitive> primSorter;
    private static Comparator<Object> primFinder;

    private static native void initIDs(Class<?> var0, Class<?> var1, Class<?> var2, Class<?> var3, Class<?> var4, Class<?> var5, Class<?> var6, Class<?> var7, Class<?> var8, Class<?> var9, Class<?> var10);

    private static native void registerNativeLoops();

    private GraphicsPrimitiveMgr() {
    }

    public static synchronized void register(GraphicsPrimitive[] newPrimitives) {
        GraphicsPrimitive[] devCollection = primitives;
        int oldSize = 0;
        int newSize = newPrimitives.length;
        if (devCollection != null) {
            oldSize = devCollection.length;
        }
        GraphicsPrimitive[] temp = new GraphicsPrimitive[oldSize + newSize];
        if (devCollection != null) {
            System.arraycopy(devCollection, 0, temp, 0, oldSize);
        }
        System.arraycopy(newPrimitives, 0, temp, oldSize, newSize);
        needssort = true;
        primitives = temp;
    }

    public static synchronized GraphicsPrimitive locate(int primTypeID, SurfaceType dsttype) {
        return GraphicsPrimitiveMgr.locate(primTypeID, SurfaceType.OpaqueColor, CompositeType.Src, dsttype);
    }

    public static synchronized GraphicsPrimitive locate(int primTypeID, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        GraphicsPrimitive prim = GraphicsPrimitiveMgr.locatePrim(primTypeID, srctype, comptype, dsttype);
        if (prim == null && (prim = GeneralPrimitives.locate(primTypeID)) != null && (prim = prim.makePrimitive(srctype, comptype, dsttype)) != null && GraphicsPrimitive.traceflags != 0) {
            prim = prim.traceWrap();
        }
        return prim;
    }

    public static synchronized GraphicsPrimitive locatePrim(int primTypeID, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        PrimitiveSpec spec = new PrimitiveSpec();
        for (SurfaceType dst = dsttype; dst != null; dst = dst.getSuperType()) {
            for (SurfaceType src = srctype; src != null; src = src.getSuperType()) {
                for (CompositeType cmp = comptype; cmp != null; cmp = cmp.getSuperType()) {
                    spec.uniqueID = GraphicsPrimitive.makeUniqueID(primTypeID, src, cmp, dst);
                    GraphicsPrimitive prim = GraphicsPrimitiveMgr.locate(spec);
                    if (prim == null) continue;
                    return prim;
                }
            }
        }
        return null;
    }

    private static GraphicsPrimitive locate(PrimitiveSpec spec) {
        GraphicsPrimitive[] devCollection;
        if (needssort) {
            if (GraphicsPrimitive.traceflags != 0) {
                for (int i = 0; i < primitives.length; ++i) {
                    GraphicsPrimitiveMgr.primitives[i] = primitives[i].traceWrap();
                }
            }
            Arrays.sort(primitives, primSorter);
            needssort = false;
        }
        if ((devCollection = primitives) == null) {
            return null;
        }
        int index = Arrays.binarySearch(devCollection, spec, primFinder);
        if (index >= 0) {
            GraphicsPrimitive prim = devCollection[index];
            if (prim instanceof GraphicsPrimitiveProxy) {
                devCollection[index] = prim = ((GraphicsPrimitiveProxy)prim).instantiate();
            }
            return prim;
        }
        return null;
    }

    private static void writeLog(String str) {
    }

    public static void testPrimitiveInstantiation() {
        GraphicsPrimitiveMgr.testPrimitiveInstantiation(false);
    }

    public static void testPrimitiveInstantiation(boolean verbose) {
        int resolved = 0;
        int unresolved = 0;
        GraphicsPrimitive[] prims = primitives;
        for (int j = 0; j < prims.length; ++j) {
            GraphicsPrimitive p = prims[j];
            if (p instanceof GraphicsPrimitiveProxy) {
                GraphicsPrimitive r = ((GraphicsPrimitiveProxy)p).instantiate();
                if (!r.getSignature().equals(p.getSignature()) || r.getUniqueID() != p.getUniqueID()) {
                    System.out.println("r.getSignature == " + r.getSignature());
                    System.out.println("r.getUniqueID == " + r.getUniqueID());
                    System.out.println("p.getSignature == " + p.getSignature());
                    System.out.println("p.getUniqueID == " + p.getUniqueID());
                    throw new RuntimeException("Primitive " + String.valueOf(p) + " returns wrong signature for " + String.valueOf(r.getClass()));
                }
                ++unresolved;
                p = r;
                if (!verbose) continue;
                System.out.println(p);
                continue;
            }
            if (verbose) {
                System.out.println(String.valueOf(p) + " (not proxied).");
            }
            ++resolved;
        }
        System.out.println(resolved + " graphics primitives were not proxied.");
        System.out.println(unresolved + " proxied graphics primitives resolved correctly.");
        System.out.println(resolved + unresolved + " total graphics primitives");
    }

    public static void main(String[] argv) {
        if (needssort) {
            Arrays.sort(primitives, primSorter);
            needssort = false;
        }
        GraphicsPrimitiveMgr.testPrimitiveInstantiation(argv.length > 0);
    }

    static {
        needssort = true;
        GraphicsPrimitiveMgr.initIDs(GraphicsPrimitive.class, SurfaceType.class, CompositeType.class, SunGraphics2D.class, Color.class, AffineTransform.class, XORComposite.class, AlphaComposite.class, Path2D.class, Path2D.Float.class, SunHints.class);
        CustomComponent.register();
        GeneralRenderer.register();
        GraphicsPrimitiveMgr.registerNativeLoops();
        primSorter = new Comparator<GraphicsPrimitive>(){

            @Override
            public int compare(GraphicsPrimitive o1, GraphicsPrimitive o2) {
                int id1 = o1.getUniqueID();
                int id2 = o2.getUniqueID();
                return Integer.compare(id1, id2);
            }
        };
        primFinder = new Comparator<Object>(){

            @Override
            public int compare(Object o1, Object o2) {
                int id1 = ((GraphicsPrimitive)o1).getUniqueID();
                int id2 = ((PrimitiveSpec)o2).uniqueID;
                return Integer.compare(id1, id2);
            }
        };
    }

    static final class GeneralPrimitives {
        private static GraphicsPrimitive[] primitives;

        GeneralPrimitives() {
        }

        static synchronized void register(GraphicsPrimitive gen) {
            if (primitives == null) {
                primitives = new GraphicsPrimitive[]{gen};
                return;
            }
            int len = primitives.length;
            GraphicsPrimitive[] newGen = new GraphicsPrimitive[len + 1];
            System.arraycopy(primitives, 0, newGen, 0, len);
            newGen[len] = gen;
            primitives = newGen;
        }

        static synchronized GraphicsPrimitive locate(int primTypeID) {
            if (primitives == null) {
                return null;
            }
            for (int i = 0; i < primitives.length; ++i) {
                GraphicsPrimitive prim = primitives[i];
                if (prim.getPrimTypeID() != primTypeID) continue;
                return prim;
            }
            return null;
        }
    }

    private static class PrimitiveSpec {
        public int uniqueID;

        private PrimitiveSpec() {
        }
    }
}

