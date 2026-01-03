/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.AlphaComposite;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import sun.awt.image.BufImgSurfaceData;
import sun.awt.util.ThreadGroupUtils;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;
import sun.security.action.GetPropertyAction;

public abstract class GraphicsPrimitive {
    private String methodSignature;
    private int uniqueID;
    private static int unusedPrimID = 1;
    private SurfaceType sourceType;
    private CompositeType compositeType;
    private SurfaceType destType;
    private long pNativePrim;
    static HashMap<Object, int[]> traceMap;
    public static int traceflags;
    public static String tracefile;
    public static PrintStream traceout;
    public static final int TRACELOG = 1;
    public static final int TRACETIMESTAMP = 2;
    public static final int TRACECOUNTS = 4;
    private String cachedname;

    public static final synchronized int makePrimTypeID() {
        if (unusedPrimID > 255) {
            throw new InternalError("primitive id overflow");
        }
        return unusedPrimID++;
    }

    public static final synchronized int makeUniqueID(int primTypeID, SurfaceType src, CompositeType cmp, SurfaceType dst) {
        return primTypeID << 24 | dst.getUniqueID() << 16 | cmp.getUniqueID() << 8 | src.getUniqueID();
    }

    protected GraphicsPrimitive(String methodSignature, int primTypeID, SurfaceType sourceType, CompositeType compositeType, SurfaceType destType) {
        this.methodSignature = methodSignature;
        this.sourceType = sourceType;
        this.compositeType = compositeType;
        this.destType = destType;
        this.uniqueID = sourceType == null || compositeType == null || destType == null ? primTypeID << 24 : GraphicsPrimitive.makeUniqueID(primTypeID, sourceType, compositeType, destType);
    }

    protected GraphicsPrimitive(long pNativePrim, String methodSignature, int primTypeID, SurfaceType sourceType, CompositeType compositeType, SurfaceType destType) {
        this.pNativePrim = pNativePrim;
        this.methodSignature = methodSignature;
        this.sourceType = sourceType;
        this.compositeType = compositeType;
        this.destType = destType;
        this.uniqueID = sourceType == null || compositeType == null || destType == null ? primTypeID << 24 : GraphicsPrimitive.makeUniqueID(primTypeID, sourceType, compositeType, destType);
    }

    public final int getUniqueID() {
        return this.uniqueID;
    }

    public final String getSignature() {
        return this.methodSignature;
    }

    public final int getPrimTypeID() {
        return this.uniqueID >>> 24;
    }

    public final long getNativePrim() {
        return this.pNativePrim;
    }

    public final SurfaceType getSourceType() {
        return this.sourceType;
    }

    public final CompositeType getCompositeType() {
        return this.compositeType;
    }

    public final SurfaceType getDestType() {
        return this.destType;
    }

    public final boolean satisfies(String signature, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        if (signature != this.methodSignature) {
            return false;
        }
        while (true) {
            if (srctype == null) {
                return false;
            }
            if (srctype.equals(this.sourceType)) break;
            srctype = srctype.getSuperType();
        }
        while (true) {
            if (comptype == null) {
                return false;
            }
            if (comptype.equals(this.compositeType)) break;
            comptype = comptype.getSuperType();
        }
        while (true) {
            if (dsttype == null) {
                return false;
            }
            if (dsttype.equals(this.destType)) break;
            dsttype = dsttype.getSuperType();
        }
        return true;
    }

    final boolean satisfiesSameAs(GraphicsPrimitive other) {
        return this.methodSignature == other.methodSignature && this.sourceType.equals(other.sourceType) && this.compositeType.equals(other.compositeType) && this.destType.equals(other.destType);
    }

    protected GraphicsPrimitive makePrimitive(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        throw new InternalError("%s not implemented for %s, comp: %s, dst: %s".formatted(this.getClass().getName(), srctype, comptype, dsttype));
    }

    public abstract GraphicsPrimitive traceWrap();

    public static boolean tracingEnabled() {
        return traceflags != 0;
    }

    private static PrintStream getTraceOutputFile() {
        if (traceout == null) {
            FileOutputStream o;
            traceout = tracefile != null ? ((o = AccessController.doPrivileged(new PrivilegedAction<FileOutputStream>(){

                @Override
                public FileOutputStream run() {
                    try {
                        return new FileOutputStream(tracefile);
                    }
                    catch (FileNotFoundException e) {
                        return null;
                    }
                }
            })) != null ? new PrintStream(o) : System.err) : System.err;
        }
        return traceout;
    }

    public static synchronized void tracePrimitive(Object prim) {
        if ((traceflags & 4) != 0) {
            int[] o;
            if (traceMap == null) {
                traceMap = new HashMap();
                TraceReporter.setShutdownHook();
            }
            if ((o = traceMap.get(prim)) == null) {
                o = new int[1];
                traceMap.put(prim, o);
            }
            o[0] = o[0] + 1;
        }
        if ((traceflags & 1) != 0) {
            PrintStream ps = GraphicsPrimitive.getTraceOutputFile();
            if ((traceflags & 2) != 0) {
                ps.print(System.currentTimeMillis() + ": ");
            }
            ps.println(prim);
        }
    }

    protected void setupGeneralBinaryOp(GeneralBinaryOp gbo) {
        Blit convertres;
        Blit convertdst;
        int primID = gbo.getPrimTypeID();
        String methodSignature = gbo.getSignature();
        SurfaceType srctype = gbo.getSourceType();
        CompositeType comptype = gbo.getCompositeType();
        SurfaceType dsttype = gbo.getDestType();
        Blit convertsrc = GraphicsPrimitive.createConverter(srctype, SurfaceType.IntArgb);
        GraphicsPrimitive performop = GraphicsPrimitiveMgr.locatePrim(primID, SurfaceType.IntArgb, comptype, dsttype);
        if (performop != null) {
            convertdst = null;
            convertres = null;
        } else {
            performop = GraphicsPrimitive.getGeneralOp(primID, comptype);
            if (performop == null) {
                throw new InternalError("Cannot construct general op for " + methodSignature + " " + String.valueOf(comptype));
            }
            convertdst = GraphicsPrimitive.createConverter(dsttype, SurfaceType.IntArgb);
            convertres = GraphicsPrimitive.createConverter(SurfaceType.IntArgb, dsttype);
        }
        gbo.setPrimitives(convertsrc, convertdst, performop, convertres);
    }

    protected void setupGeneralUnaryOp(GeneralUnaryOp guo) {
        int primID = guo.getPrimTypeID();
        String methodSignature = guo.getSignature();
        CompositeType comptype = guo.getCompositeType();
        SurfaceType dsttype = guo.getDestType();
        Blit convertdst = GraphicsPrimitive.createConverter(dsttype, SurfaceType.IntArgb);
        GraphicsPrimitive performop = GraphicsPrimitive.getGeneralOp(primID, comptype);
        Blit convertres = GraphicsPrimitive.createConverter(SurfaceType.IntArgb, dsttype);
        if (convertdst == null || performop == null || convertres == null) {
            throw new InternalError("Cannot construct binary op for " + String.valueOf(comptype) + " " + String.valueOf(dsttype));
        }
        guo.setPrimitives(convertdst, performop, convertres);
    }

    protected static Blit createConverter(SurfaceType srctype, SurfaceType dsttype) {
        if (srctype.equals(dsttype)) {
            return null;
        }
        Blit cv = Blit.getFromCache(srctype, CompositeType.SrcNoEa, dsttype);
        if (cv == null) {
            throw new InternalError("Cannot construct converter for " + String.valueOf(srctype) + "=>" + String.valueOf(dsttype));
        }
        return cv;
    }

    protected static SurfaceData convertFrom(Blit ob, SurfaceData srcData, int srcX, int srcY, int w, int h, SurfaceData dstData) {
        return GraphicsPrimitive.convertFrom(ob, srcData, srcX, srcY, w, h, dstData, 2);
    }

    protected static SurfaceData convertFrom(Blit ob, SurfaceData srcData, int srcX, int srcY, int w, int h, SurfaceData dstData, int type) {
        if (dstData != null) {
            Rectangle r = dstData.getBounds();
            if (w > r.width || h > r.height) {
                dstData = null;
            }
        }
        if (dstData == null) {
            BufferedImage dstBI = new BufferedImage(w, h, type);
            dstData = BufImgSurfaceData.createData(dstBI);
        }
        ob.Blit(srcData, dstData, AlphaComposite.Src, null, srcX, srcY, 0, 0, w, h);
        return dstData;
    }

    protected static void convertTo(Blit ob, SurfaceData srcImg, SurfaceData dstImg, Region clip, int dstX, int dstY, int w, int h) {
        if (ob != null) {
            ob.Blit(srcImg, dstImg, AlphaComposite.Src, clip, 0, 0, dstX, dstY, w, h);
        }
    }

    protected static GraphicsPrimitive getGeneralOp(int primID, CompositeType comptype) {
        return GraphicsPrimitiveMgr.locatePrim(primID, SurfaceType.IntArgb, comptype, SurfaceType.IntArgb);
    }

    public static String simplename(Field[] fields, Object o) {
        for (int i = 0; i < fields.length; ++i) {
            Field f = fields[i];
            try {
                if (o != f.get(null)) continue;
                return f.getName();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return "\"" + o.toString() + "\"";
    }

    public static String simplename(SurfaceType st) {
        return GraphicsPrimitive.simplename(SurfaceType.class.getDeclaredFields(), st);
    }

    public static String simplename(CompositeType ct) {
        return GraphicsPrimitive.simplename(CompositeType.class.getDeclaredFields(), ct);
    }

    public String toString() {
        if (this.cachedname == null) {
            String sig = this.methodSignature;
            int index = sig.indexOf(40);
            if (index >= 0) {
                sig = sig.substring(0, index);
            }
            this.cachedname = this.getClass().getName() + "::" + sig + "(" + GraphicsPrimitive.simplename(this.sourceType) + ", " + GraphicsPrimitive.simplename(this.compositeType) + ", " + GraphicsPrimitive.simplename(this.destType) + ")";
        }
        return this.cachedname;
    }

    static {
        GetPropertyAction gpa = new GetPropertyAction("sun.java2d.trace");
        String trace = AccessController.doPrivileged(gpa);
        if (trace != null) {
            boolean verbose = false;
            int traceflags = 0;
            StringTokenizer st = new StringTokenizer(trace, ",");
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                if (tok.equalsIgnoreCase("count")) {
                    traceflags |= 4;
                    continue;
                }
                if (tok.equalsIgnoreCase("log")) {
                    traceflags |= 1;
                    continue;
                }
                if (tok.equalsIgnoreCase("timestamp")) {
                    traceflags |= 2;
                    continue;
                }
                if (tok.equalsIgnoreCase("verbose")) {
                    verbose = true;
                    continue;
                }
                if (tok.regionMatches(true, 0, "out:", 0, 4)) {
                    tracefile = tok.substring(4);
                    continue;
                }
                if (!tok.equalsIgnoreCase("help")) {
                    System.err.println("unrecognized token: " + tok);
                }
                System.err.println("usage: -Dsun.java2d.trace=[log[,timestamp]],[count],[out:<filename>],[help],[verbose]");
            }
            if (verbose) {
                System.err.print("GraphicsPrimitive logging ");
                if ((traceflags & 1) != 0) {
                    System.err.println("enabled");
                    System.err.print("GraphicsPrimitive timestamps ");
                    if ((traceflags & 2) != 0) {
                        System.err.println("enabled");
                    } else {
                        System.err.println("disabled");
                    }
                } else {
                    System.err.println("[and timestamps] disabled");
                }
                System.err.print("GraphicsPrimitive invocation counts ");
                if ((traceflags & 4) != 0) {
                    System.err.println("enabled");
                } else {
                    System.err.println("disabled");
                }
                System.err.print("GraphicsPrimitive trace output to ");
                if (tracefile == null) {
                    System.err.println("System.err");
                } else {
                    System.err.println("file '" + tracefile + "'");
                }
            }
            GraphicsPrimitive.traceflags = traceflags;
        }
    }

    public static class TraceReporter
    implements Runnable {
        public static void setShutdownHook() {
            AccessController.doPrivileged(() -> {
                TraceReporter t = new TraceReporter();
                Thread thread = new Thread(ThreadGroupUtils.getRootThreadGroup(), t, "TraceReporter", 0L, false);
                thread.setContextClassLoader(null);
                Runtime.getRuntime().addShutdownHook(thread);
                return null;
            });
        }

        @Override
        public void run() {
            PrintStream ps = GraphicsPrimitive.getTraceOutputFile();
            long total = 0L;
            int numprims = 0;
            for (Map.Entry<Object, int[]> me : traceMap.entrySet()) {
                Object prim = me.getKey();
                int[] count = me.getValue();
                if (count[0] == 1) {
                    ps.print("1 call to ");
                } else {
                    ps.print(count[0] + " calls to ");
                }
                ps.println(prim);
                ++numprims;
                total += (long)count[0];
            }
            if (numprims == 0) {
                ps.println("No graphics primitives executed");
            } else if (numprims > 1) {
                ps.println(total + " total calls to " + numprims + " different primitives");
            }
        }
    }

    protected static interface GeneralBinaryOp {
        public void setPrimitives(Blit var1, Blit var2, GraphicsPrimitive var3, Blit var4);

        public SurfaceType getSourceType();

        public CompositeType getCompositeType();

        public SurfaceType getDestType();

        public String getSignature();

        public int getPrimTypeID();
    }

    protected static interface GeneralUnaryOp {
        public void setPrimitives(Blit var1, GraphicsPrimitive var2, Blit var3);

        public CompositeType getCompositeType();

        public SurfaceType getDestType();

        public String getSignature();

        public int getPrimTypeID();
    }
}

