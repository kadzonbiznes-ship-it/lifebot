/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import jdk.internal.misc.Unsafe;
import sun.font.FontStrike;
import sun.font.FontStrikeDisposer;
import sun.font.GlyphDisposedListener;
import sun.java2d.Disposer;
import sun.java2d.pipe.BufferedContext;
import sun.java2d.pipe.RenderQueue;
import sun.java2d.pipe.hw.AccelGraphicsConfig;

public final class StrikeCache {
    static final Unsafe unsafe = Unsafe.getUnsafe();
    static ReferenceQueue<Object> refQueue = Disposer.getQueue();
    static ArrayList<GlyphDisposedListener> disposeListeners = new ArrayList(1);
    static int MINSTRIKES = 8;
    static int recentStrikeIndex = 0;
    static FontStrike[] recentStrikes;
    static boolean cacheRefTypeWeak;
    static int nativeAddressSize;
    static int glyphInfoSize;
    static int xAdvanceOffset;
    static int yAdvanceOffset;
    static int boundsOffset;
    static int widthOffset;
    static int heightOffset;
    static int rowBytesOffset;
    static int topLeftXOffset;
    static int topLeftYOffset;
    static int pixelDataOffset;
    static int cacheCellOffset;
    static int managedOffset;
    static long invisibleGlyphPtr;

    static native void getGlyphCacheDescription(long[] var0);

    private static void initStatic() {
        long[] nativeInfo = new long[13];
        StrikeCache.getGlyphCacheDescription(nativeInfo);
        nativeAddressSize = (int)nativeInfo[0];
        glyphInfoSize = (int)nativeInfo[1];
        xAdvanceOffset = (int)nativeInfo[2];
        yAdvanceOffset = (int)nativeInfo[3];
        widthOffset = (int)nativeInfo[4];
        heightOffset = (int)nativeInfo[5];
        rowBytesOffset = (int)nativeInfo[6];
        topLeftXOffset = (int)nativeInfo[7];
        topLeftYOffset = (int)nativeInfo[8];
        pixelDataOffset = (int)nativeInfo[9];
        invisibleGlyphPtr = nativeInfo[10];
        cacheCellOffset = (int)nativeInfo[11];
        managedOffset = (int)nativeInfo[12];
        if (nativeAddressSize < 4) {
            throw new InternalError("Unexpected address size for font data: " + nativeAddressSize);
        }
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                String refType = System.getProperty("sun.java2d.font.reftype", "soft");
                cacheRefTypeWeak = refType.equals("weak");
                String minStrikesStr = System.getProperty("sun.java2d.font.minstrikes");
                if (minStrikesStr != null) {
                    try {
                        MINSTRIKES = Integer.parseInt(minStrikesStr);
                        if (MINSTRIKES <= 0) {
                            MINSTRIKES = 1;
                        }
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                recentStrikes = new FontStrike[MINSTRIKES];
                return null;
            }
        });
    }

    static void refStrike(FontStrike strike) {
        int index = recentStrikeIndex;
        StrikeCache.recentStrikes[index] = strike;
        if (++index == MINSTRIKES) {
            index = 0;
        }
        recentStrikeIndex = index;
    }

    private static void doDispose(FontStrikeDisposer disposer) {
        if (disposer.intGlyphImages != null) {
            StrikeCache.freeCachedIntMemory(disposer.intGlyphImages, disposer.pScalerContext);
        } else if (disposer.longGlyphImages != null) {
            StrikeCache.freeCachedLongMemory(disposer.longGlyphImages, disposer.pScalerContext);
        } else if (disposer.segIntGlyphImages != null) {
            for (int i = 0; i < disposer.segIntGlyphImages.length; ++i) {
                if (disposer.segIntGlyphImages[i] == null) continue;
                StrikeCache.freeCachedIntMemory(disposer.segIntGlyphImages[i], disposer.pScalerContext);
                disposer.pScalerContext = 0L;
                disposer.segIntGlyphImages[i] = null;
            }
            if (disposer.pScalerContext != 0L) {
                StrikeCache.freeCachedIntMemory(new int[0], disposer.pScalerContext);
            }
        } else if (disposer.segLongGlyphImages != null) {
            for (int i = 0; i < disposer.segLongGlyphImages.length; ++i) {
                if (disposer.segLongGlyphImages[i] == null) continue;
                StrikeCache.freeCachedLongMemory(disposer.segLongGlyphImages[i], disposer.pScalerContext);
                disposer.pScalerContext = 0L;
                disposer.segLongGlyphImages[i] = null;
            }
            if (disposer.pScalerContext != 0L) {
                StrikeCache.freeCachedLongMemory(new long[0], disposer.pScalerContext);
            }
        } else if (disposer.pScalerContext != 0L) {
            if (StrikeCache.longAddresses()) {
                StrikeCache.freeCachedLongMemory(new long[0], disposer.pScalerContext);
            } else {
                StrikeCache.freeCachedIntMemory(new int[0], disposer.pScalerContext);
            }
        }
    }

    private static boolean longAddresses() {
        return nativeAddressSize == 8;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void disposeStrike(final FontStrikeDisposer disposer) {
        AccelGraphicsConfig agc;
        BufferedContext bc;
        GraphicsConfiguration gc;
        if (Disposer.pollingQueue) {
            StrikeCache.doDispose(disposer);
            return;
        }
        RenderQueue rq = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (!GraphicsEnvironment.isHeadless() && (gc = ge.getDefaultScreenDevice().getDefaultConfiguration()) instanceof AccelGraphicsConfig && (bc = (agc = (AccelGraphicsConfig)((Object)gc)).getContext()) != null) {
            rq = bc.getRenderQueue();
        }
        if (rq != null) {
            rq.lock();
            try {
                rq.flushAndInvokeNow(new Runnable(){

                    @Override
                    public void run() {
                        StrikeCache.doDispose(disposer);
                        Disposer.pollRemove();
                    }
                });
            }
            finally {
                rq.unlock();
            }
        } else {
            StrikeCache.doDispose(disposer);
        }
    }

    static native void freeIntPointer(int var0);

    static native void freeLongPointer(long var0);

    private static native void freeIntMemory(int[] var0, long var1);

    private static native void freeLongMemory(long[] var0, long var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void freeCachedIntMemory(int[] glyphPtrs, long pContext) {
        ArrayList<GlyphDisposedListener> arrayList = disposeListeners;
        synchronized (arrayList) {
            if (disposeListeners.size() > 0) {
                ArrayList<Long> gids = null;
                for (int i = 0; i < glyphPtrs.length; ++i) {
                    if (glyphPtrs[i] == 0 || unsafe.getByte(glyphPtrs[i] + managedOffset) != 0) continue;
                    if (gids == null) {
                        gids = new ArrayList<Long>();
                    }
                    gids.add(Long.valueOf(glyphPtrs[i]));
                }
                if (gids != null) {
                    StrikeCache.notifyDisposeListeners(gids);
                }
            }
        }
        StrikeCache.freeIntMemory(glyphPtrs, pContext);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void freeCachedLongMemory(long[] glyphPtrs, long pContext) {
        ArrayList<GlyphDisposedListener> arrayList = disposeListeners;
        synchronized (arrayList) {
            if (disposeListeners.size() > 0) {
                ArrayList<Long> gids = null;
                for (int i = 0; i < glyphPtrs.length; ++i) {
                    if (glyphPtrs[i] == 0L || unsafe.getByte(glyphPtrs[i] + (long)managedOffset) != 0) continue;
                    if (gids == null) {
                        gids = new ArrayList<Long>();
                    }
                    gids.add(glyphPtrs[i]);
                }
                if (gids != null) {
                    StrikeCache.notifyDisposeListeners(gids);
                }
            }
        }
        StrikeCache.freeLongMemory(glyphPtrs, pContext);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void addGlyphDisposedListener(GlyphDisposedListener listener) {
        ArrayList<GlyphDisposedListener> arrayList = disposeListeners;
        synchronized (arrayList) {
            disposeListeners.add(listener);
        }
    }

    private static void notifyDisposeListeners(ArrayList<Long> glyphs) {
        for (GlyphDisposedListener listener : disposeListeners) {
            listener.glyphDisposed(glyphs);
        }
    }

    public static Reference<FontStrike> getStrikeRef(FontStrike strike) {
        return StrikeCache.getStrikeRef(strike, cacheRefTypeWeak);
    }

    public static Reference<FontStrike> getStrikeRef(FontStrike strike, boolean weak) {
        if (strike.disposer == null) {
            if (weak) {
                return new WeakReference<FontStrike>(strike);
            }
            return new SoftReference<FontStrike>(strike);
        }
        if (weak) {
            return new WeakDisposerRef(strike);
        }
        return new SoftDisposerRef(strike);
    }

    static {
        StrikeCache.initStatic();
    }

    static class WeakDisposerRef
    extends WeakReference<FontStrike>
    implements DisposableStrike {
        private FontStrikeDisposer disposer;

        @Override
        public FontStrikeDisposer getDisposer() {
            return this.disposer;
        }

        WeakDisposerRef(FontStrike strike) {
            super(strike, refQueue);
            this.disposer = strike.disposer;
            Disposer.addReference(this, this.disposer);
        }
    }

    static class SoftDisposerRef
    extends SoftReference<FontStrike>
    implements DisposableStrike {
        private FontStrikeDisposer disposer;

        @Override
        public FontStrikeDisposer getDisposer() {
            return this.disposer;
        }

        SoftDisposerRef(FontStrike strike) {
            super(strike, refQueue);
            this.disposer = strike.disposer;
            Disposer.addReference(this, this.disposer);
        }
    }

    static interface DisposableStrike {
        public FontStrikeDisposer getDisposer();
    }
}

