/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.SurfaceType;

public class DrawGlyphListLCD
extends GraphicsPrimitive {
    public static final String methodSignature = "DrawGlyphListLCD(...)".toString();
    public static final int primTypeID = DrawGlyphListLCD.makePrimTypeID();

    public static DrawGlyphListLCD locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (DrawGlyphListLCD)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    protected DrawGlyphListLCD(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public DrawGlyphListLCD(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void DrawGlyphListLCD(SunGraphics2D var1, SurfaceData var2, GlyphList var3, int var4, int var5);

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceDrawGlyphListLCD(this);
    }

    private static class TraceDrawGlyphListLCD
    extends DrawGlyphListLCD {
        DrawGlyphListLCD target;

        public TraceDrawGlyphListLCD(DrawGlyphListLCD target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void DrawGlyphListLCD(SunGraphics2D sg2d, SurfaceData dest, GlyphList glyphs, int fromGlyph, int toGlyph) {
            TraceDrawGlyphListLCD.tracePrimitive(this.target);
            this.target.DrawGlyphListLCD(sg2d, dest, glyphs, fromGlyph, toGlyph);
        }
    }
}

