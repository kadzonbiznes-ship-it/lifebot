/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import sun.java2d.loops.DrawGlyphList;
import sun.java2d.loops.DrawGlyphListAA;
import sun.java2d.loops.DrawGlyphListColor;
import sun.java2d.loops.DrawGlyphListLCD;
import sun.java2d.loops.DrawLine;
import sun.java2d.loops.DrawParallelogram;
import sun.java2d.loops.DrawPath;
import sun.java2d.loops.DrawPolygons;
import sun.java2d.loops.DrawRect;
import sun.java2d.loops.FillParallelogram;
import sun.java2d.loops.FillPath;
import sun.java2d.loops.FillRect;
import sun.java2d.loops.FillSpans;
import sun.java2d.loops.GraphicsPrimitive;

public class RenderLoops {
    public static final int primTypeID = GraphicsPrimitive.makePrimTypeID();
    public DrawLine drawLineLoop;
    public FillRect fillRectLoop;
    public DrawRect drawRectLoop;
    public DrawPolygons drawPolygonsLoop;
    public DrawPath drawPathLoop;
    public FillPath fillPathLoop;
    public FillSpans fillSpansLoop;
    public FillParallelogram fillParallelogramLoop;
    public DrawParallelogram drawParallelogramLoop;
    public DrawGlyphList drawGlyphListLoop;
    public DrawGlyphListAA drawGlyphListAALoop;
    public DrawGlyphListLCD drawGlyphListLCDLoop;
    public DrawGlyphListColor drawGlyphListColorLoop;
}

