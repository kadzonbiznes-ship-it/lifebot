/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.pipe.GlyphListLoopPipe;

public class LCDTextRenderer
extends GlyphListLoopPipe {
    @Override
    protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl) {
        this.drawGlyphList(sg2d, gl, 4);
    }
}

