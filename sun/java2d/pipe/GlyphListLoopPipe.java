/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.pipe.GlyphListPipe;
import sun.java2d.pipe.LoopBasedPipe;

public abstract class GlyphListLoopPipe
extends GlyphListPipe
implements LoopBasedPipe {
    @Override
    protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl, int aaHint) {
        int prevLimit = 0;
        boolean isColor = false;
        int len = gl.getNumGlyphs();
        gl.startGlyphIteration();
        if (GlyphList.canContainColorGlyphs()) {
            for (int i = 0; i < len; ++i) {
                boolean newIsColor = gl.isColorGlyph(i);
                if (newIsColor == isColor) continue;
                this.drawGlyphListSegment(sg2d, gl, prevLimit, i, aaHint, isColor);
                prevLimit = i;
                isColor = newIsColor;
            }
        }
        this.drawGlyphListSegment(sg2d, gl, prevLimit, len, aaHint, isColor);
    }

    private void drawGlyphListSegment(SunGraphics2D sg2d, GlyphList gl, int fromglyph, int toGlyph, int aaHint, boolean isColor) {
        if (fromglyph >= toGlyph) {
            return;
        }
        if (isColor) {
            sg2d.loops.drawGlyphListColorLoop.DrawGlyphListColor(sg2d, sg2d.surfaceData, gl, fromglyph, toGlyph);
        } else {
            switch (aaHint) {
                case 1: {
                    sg2d.loops.drawGlyphListLoop.DrawGlyphList(sg2d, sg2d.surfaceData, gl, fromglyph, toGlyph);
                    return;
                }
                case 2: {
                    sg2d.loops.drawGlyphListAALoop.DrawGlyphListAA(sg2d, sg2d.surfaceData, gl, fromglyph, toGlyph);
                    return;
                }
                case 4: 
                case 6: {
                    sg2d.loops.drawGlyphListLCDLoop.DrawGlyphListLCD(sg2d, sg2d.surfaceData, gl, fromglyph, toGlyph);
                    return;
                }
            }
        }
    }
}

