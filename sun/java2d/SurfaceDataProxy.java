/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.security.AccessController;
import sun.awt.DisplayChangedListener;
import sun.awt.image.SurfaceManager;
import sun.java2d.StateTrackable;
import sun.java2d.StateTracker;
import sun.java2d.SunGraphicsEnvironment;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.BlitBg;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.security.action.GetPropertyAction;

public abstract class SurfaceDataProxy
implements DisplayChangedListener,
SurfaceManager.FlushableCacheData {
    private static boolean cachingAllowed = true;
    private static int defaultThreshold;
    public static SurfaceDataProxy UNCACHED;
    private int threshold;
    private StateTracker srcTracker;
    private int numtries;
    private SurfaceData cachedSD;
    private StateTracker cacheTracker;
    private boolean valid;

    public static boolean isCachingAllowed() {
        return cachingAllowed;
    }

    public abstract boolean isSupportedOperation(SurfaceData var1, int var2, CompositeType var3, Color var4);

    public abstract SurfaceData validateSurfaceData(SurfaceData var1, SurfaceData var2, int var3, int var4);

    public StateTracker getRetryTracker(SurfaceData srcData) {
        return new CountdownTracker(this.threshold);
    }

    public SurfaceDataProxy() {
        this(defaultThreshold);
    }

    public SurfaceDataProxy(int threshold) {
        this.threshold = threshold;
        this.srcTracker = StateTracker.NEVER_CURRENT;
        this.cacheTracker = StateTracker.NEVER_CURRENT;
        this.valid = true;
    }

    public boolean isValid() {
        return this.valid;
    }

    public void invalidate() {
        this.valid = false;
    }

    @Override
    public boolean flush(boolean deaccelerated) {
        if (deaccelerated) {
            this.invalidate();
        }
        this.flush();
        return !this.isValid();
    }

    public synchronized void flush() {
        SurfaceData csd = this.cachedSD;
        this.cachedSD = null;
        this.cacheTracker = StateTracker.NEVER_CURRENT;
        if (csd != null) {
            csd.flush();
        }
    }

    public boolean isAccelerated() {
        return this.isValid() && this.srcTracker.isCurrent() && this.cacheTracker.isCurrent();
    }

    protected void activateDisplayListener() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge instanceof SunGraphicsEnvironment) {
            ((SunGraphicsEnvironment)ge).addDisplayChangedListener(this);
        }
    }

    @Override
    public void displayChanged() {
        this.flush();
    }

    @Override
    public void paletteChanged() {
        this.srcTracker = StateTracker.NEVER_CURRENT;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SurfaceData replaceData(SurfaceData srcData, int txtype, CompositeType comp, Color bgColor) {
        if (this.isSupportedOperation(srcData, txtype, comp, bgColor)) {
            if (!this.srcTracker.isCurrent()) {
                SurfaceDataProxy surfaceDataProxy = this;
                synchronized (surfaceDataProxy) {
                    this.numtries = this.threshold;
                    this.srcTracker = srcData.getStateTracker();
                    this.cacheTracker = StateTracker.NEVER_CURRENT;
                }
                if (!this.srcTracker.isCurrent()) {
                    if (srcData.getState() == StateTrackable.State.UNTRACKABLE) {
                        this.invalidate();
                        this.flush();
                    }
                    return srcData;
                }
            }
            SurfaceData csd = this.cachedSD;
            if (!this.cacheTracker.isCurrent()) {
                SurfaceDataProxy surfaceDataProxy = this;
                synchronized (surfaceDataProxy) {
                    if (this.numtries > 0) {
                        --this.numtries;
                        return srcData;
                    }
                }
                Rectangle r = srcData.getBounds();
                int w = r.width;
                int h = r.height;
                StateTracker curTracker = this.srcTracker;
                csd = this.validateSurfaceData(srcData, csd, w, h);
                if (csd == null) {
                    SurfaceDataProxy surfaceDataProxy2 = this;
                    synchronized (surfaceDataProxy2) {
                        if (curTracker == this.srcTracker) {
                            this.cacheTracker = this.getRetryTracker(srcData);
                            this.cachedSD = null;
                        }
                    }
                    return srcData;
                }
                this.updateSurfaceData(srcData, csd, w, h);
                if (!csd.isValid()) {
                    return srcData;
                }
                SurfaceDataProxy surfaceDataProxy3 = this;
                synchronized (surfaceDataProxy3) {
                    if (curTracker == this.srcTracker && curTracker.isCurrent()) {
                        this.cacheTracker = csd.getStateTracker();
                        this.cachedSD = csd;
                    }
                }
            }
            if (csd != null) {
                return csd;
            }
        }
        return srcData;
    }

    public void updateSurfaceData(SurfaceData srcData, SurfaceData dstData, int w, int h) {
        SurfaceType srcType = srcData.getSurfaceType();
        SurfaceType dstType = dstData.getSurfaceType();
        Blit blit = Blit.getFromCache(srcType, CompositeType.SrcNoEa, dstType);
        blit.Blit(srcData, dstData, AlphaComposite.Src, null, 0, 0, 0, 0, w, h);
        dstData.markDirty();
    }

    public void updateSurfaceDataBg(SurfaceData srcData, SurfaceData dstData, int w, int h, Color bgColor) {
        SurfaceType srcType = srcData.getSurfaceType();
        SurfaceType dstType = dstData.getSurfaceType();
        BlitBg blitbg = BlitBg.getFromCache(srcType, CompositeType.SrcNoEa, dstType);
        blitbg.BlitBg(srcData, dstData, AlphaComposite.Src, null, bgColor.getRGB(), 0, 0, 0, 0, w, h);
        dstData.markDirty();
    }

    static {
        String manimg = AccessController.doPrivileged(new GetPropertyAction("sun.java2d.managedimages"));
        if ("false".equals(manimg)) {
            cachingAllowed = false;
            System.out.println("Disabling managed images");
        }
        defaultThreshold = 1;
        String num = AccessController.doPrivileged(new GetPropertyAction("sun.java2d.accthreshold"));
        if (num != null) {
            try {
                int parsed = Integer.parseInt(num);
                if (parsed >= 0) {
                    defaultThreshold = parsed;
                    System.out.println("New Default Acceleration Threshold: " + defaultThreshold);
                }
            }
            catch (NumberFormatException e) {
                System.err.println("Error setting new threshold:" + String.valueOf(e));
            }
        }
        UNCACHED = new SurfaceDataProxy(0){

            @Override
            public boolean isAccelerated() {
                return false;
            }

            @Override
            public boolean isSupportedOperation(SurfaceData srcData, int txtype, CompositeType comp, Color bgColor) {
                return false;
            }

            @Override
            public SurfaceData validateSurfaceData(SurfaceData srcData, SurfaceData cachedData, int w, int h) {
                throw new InternalError("UNCACHED should never validate SDs");
            }

            @Override
            public SurfaceData replaceData(SurfaceData srcData, int txtype, CompositeType comp, Color bgColor) {
                return srcData;
            }
        };
    }

    public static class CountdownTracker
    implements StateTracker {
        private int countdown;

        public CountdownTracker(int threshold) {
            this.countdown = threshold;
        }

        @Override
        public synchronized boolean isCurrent() {
            return --this.countdown >= 0;
        }
    }
}

