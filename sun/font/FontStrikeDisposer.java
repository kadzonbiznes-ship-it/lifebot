/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentHashMap;
import sun.font.Font2D;
import sun.font.FontStrike;
import sun.font.FontStrikeDesc;
import sun.font.StrikeCache;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;

class FontStrikeDisposer
implements DisposerRecord,
Disposer.PollDisposable {
    ConcurrentHashMap<FontStrikeDesc, Reference<FontStrike>> strikeCache;
    FontStrikeDesc desc;
    long[] longGlyphImages;
    int[] intGlyphImages;
    int[][] segIntGlyphImages;
    long[][] segLongGlyphImages;
    long pScalerContext = 0L;
    boolean disposed = false;
    boolean comp = false;

    public FontStrikeDisposer(Font2D font2D, FontStrikeDesc desc, long pContext, int[] images) {
        this.strikeCache = font2D.strikeCache;
        this.desc = desc;
        this.pScalerContext = pContext;
        this.intGlyphImages = images;
    }

    public FontStrikeDisposer(Font2D font2D, FontStrikeDesc desc, long pContext, long[] images) {
        this.strikeCache = font2D.strikeCache;
        this.desc = desc;
        this.pScalerContext = pContext;
        this.longGlyphImages = images;
    }

    public FontStrikeDisposer(Font2D font2D, FontStrikeDesc desc, long pContext) {
        this.strikeCache = font2D.strikeCache;
        this.desc = desc;
        this.pScalerContext = pContext;
    }

    public FontStrikeDisposer(Font2D font2D, FontStrikeDesc desc) {
        this.strikeCache = font2D.strikeCache;
        this.desc = desc;
        this.comp = true;
    }

    @Override
    public synchronized void dispose() {
        if (!this.disposed) {
            FontStrike o;
            Reference<FontStrike> ref = this.strikeCache.get(this.desc);
            if (ref != null && (o = ref.get()) == null) {
                this.strikeCache.remove(this.desc);
            }
            StrikeCache.disposeStrike(this);
            this.disposed = true;
        }
    }
}

