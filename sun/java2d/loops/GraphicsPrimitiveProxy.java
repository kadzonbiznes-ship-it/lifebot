/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.SurfaceType;

public class GraphicsPrimitiveProxy
extends GraphicsPrimitive {
    private Class<?> owner;
    private String relativeClassName;

    public GraphicsPrimitiveProxy(Class<?> owner, String relativeClassName, String methodSignature, int primID, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primID, srctype, comptype, dsttype);
        this.owner = owner;
        this.relativeClassName = relativeClassName;
    }

    GraphicsPrimitive instantiate() {
        String name = GraphicsPrimitiveProxy.getPackageName(this.owner.getName()) + "." + this.relativeClassName;
        try {
            Class<?> clazz = Class.forName(name);
            GraphicsPrimitive p = (GraphicsPrimitive)clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            if (!this.satisfiesSameAs(p)) {
                throw new RuntimeException("Primitive " + String.valueOf(p) + " incompatible with proxy for " + name);
            }
            return p;
        }
        catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex.toString());
        }
    }

    private static String getPackageName(String className) {
        int lastDotIdx = className.lastIndexOf(46);
        if (lastDotIdx < 0) {
            return className;
        }
        return className.substring(0, lastDotIdx);
    }

    @Override
    public GraphicsPrimitive traceWrap() {
        return this.instantiate().traceWrap();
    }
}

